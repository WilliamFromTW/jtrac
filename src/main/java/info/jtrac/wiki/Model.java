package info.jtrac.wiki;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.servlet.*;
import javax.servlet.http.*;

import org.stringtree.factory.Container;
import org.stringtree.factory.DelegatedTractFetcher;
import org.stringtree.util.StringUtils;
import org.stringtree.util.tract.Tract;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Model
	extends DelegatedTractFetcher
	implements Container
{
    private static final Logger logger = LoggerFactory.getLogger(Model.class);

	private static final SimpleDateFormat wikiLogDateFormat = new SimpleDateFormat("E MMM dd HH:mm:ss z YYYY");

	private LucenePageRepository repository;
	private HTMLRenderer renderer;
	private ClassicToHTMLFilter filter;
	private Policy policy;

	public Model (LucenePageRepository repository, HTMLRenderer renderer, ClassicToHTMLFilter filter, Policy policy)
	{
		super(repository);
		this.repository = repository;
		this.renderer = renderer;
		this.filter = filter;
		this.policy = policy;
	}

	public String ensurePage (String page)
	{
		if (page==null || page.trim().isEmpty()) {
			page = "HomePage";
		} else {
			page = Utils.unescape(page);
		}

		return page;
	}

	public synchronized Tract update (String name, String content, String comment, String userName, String seoName, String redirectTo)
	{
		Tract page = repository.get(name);
		page.setContent(content);
		if (! StringUtils.isBlank(comment))
			page.put("edit.comment", comment);
		else
			page.put("edit.comment", "none");
		if (! StringUtils.isBlank(userName))
			page.put("edit.user", userName);
		page.put("seo.name", seoName);
		page.put("redirect.to", redirectTo);
		repository.put(name, page);
        return page;
	}

	public Page search (String pattern, HttpServletRequest request)
		throws IOException
	{
		List<LucenePageRepository.SearchResult> results = new ArrayList<>();

		for (LucenePageRepository.SearchResult result : repository.matchingPages(pattern.toLowerCase())) {
			// skip page if it redirects elsewhere, or if it has no exact hits
			Tract resultPage = get(result.getName());
			String redirect = (String) resultPage.get("redirect.to");
			if (redirect != null && redirect.trim().length() > 0)
				continue;

			results.add(result);
		}
		request.setAttribute("results", results);

		Page page = new Page("Search Results");
		page.put("pattern", pattern);
		return page;
	}

	public void render (Tract page, String mode, boolean isFiltered, boolean isBackup, HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException
	{
		// add all policy entries as request attributes
		for (Iterator it = policy.list(); it.hasNext(); ) {
			String name = (String) it.next();
			if (! name.contains(".")) {
				//logger.info("adding "+name+"="+policy.get(name));
				request.setAttribute(name, policy.get(name));
			}
		}

		//logger.info(page.getContent());
		renderer.render(page, mode, isFiltered, isBackup, request, response);
	}

	public boolean contains (String name)
	{
		return repository.contains(name);
	}

	public void logLink (String pageName, String baseUrl, String author)
	{
		StringBuilder ret = new StringBuilder(" ");
		ret.append(wikiLogDateFormat.format(new Date()));
		ret.append(" ...... ");
		ret.append(pageName);
		if (!StringUtils.isBlank(author))
		{
			ret.append(" ...... ");
			ret.append(author);
		}
		ret.append("\n\n");
		repository.append("RecentChanges", ret.toString());
	}

	public String makeLinkSource(String name)
	{
		return filter.makeLinkSource(name);
	}

	public Object getObject(String name)
	{
		return repository.get(name);
	}

	public Tract get(String name)
	{
		String ensured = ensurePage(name);

		return repository.get(ensured);
	}

	public String diff (String pageName, boolean toCurrent)
	{
		String ret = "";
		Tract page = get(pageName);
		String previousName = get(pageName).getAttribute("previous.version");
		if (toCurrent) {
			int idx = pageName.indexOf(".");
			if (idx > 0) {
				previousName = pageName;
				pageName = pageName.substring(0, idx).substring(4);
				page = get(pageName);
			}
		}
		logger.debug("pageName="+pageName+", previousName="+previousName);

		if (!StringUtils.isBlank(previousName) && !"old/EmptyPage".equals(previousName))
		{
			Tract previous = get(previousName);
			boolean hardChanges=false, softChanges=false;;
			DiffMatchPatch dmp = new DiffMatchPatch();
			dmp.Diff_Timeout = 2.0f;
			StringBuilder dmpOut = new StringBuilder(1000);
			dmpOut.append("<p class='diff'><table class='difftable' width='95%' border='1'>\n" +
				"<tr><th>Line</th><th>Added</th><th>Removed</th></tr>\n");
			LinkedList<DiffMatchPatch.Patch> patches = dmp.patch_make(previous.getContent(), page.getContent());
			for (DiffMatchPatch.Patch patch : patches) {
				//System.out.println("start1="+patch.start1+", start2="+patch.start2);
				//System.out.println(patch.toString());

				dmp.diff_cleanupSemantic(patch.diffs);
				for (DiffMatchPatch.Diff diff : patch.diffs) {
					switch (diff.operation) {
						case EQUAL:
							break;
						case INSERT:
							//System.out.println(diff.toString());
							if (diff.text.trim().length() > 0) {
								int lineNo = 1;
								int searchFrom = patch.start2;
								while (page.getContent().indexOf(diff.text, searchFrom) < 0)
									searchFrom--;
								for (int i=0; i<=searchFrom; i++)
									if (page.getContent().charAt(i) == '\n') {
										lineNo++;
										//System.out.println(lineNo+" : "+i);
									}
								dmpOut.append("<tr>");
								dmpOut.append("<td class='lineno'>"+lineNo+"</td>");
								dmpOut.append("<td class='added'>");
								dmpOut.append(Utils.xmlEncode(diff.text));
								dmpOut.append("</td><td>&nbsp;</td></tr>\n");
								hardChanges = true;
							} else {
								softChanges = true;
							}
							break;
						case DELETE:
							//System.out.println(diff.toString());
							if (diff.text.trim().length() > 0) {
								int lineNo = 1;
								int searchFrom = patch.start1;
								while (previous.getContent().indexOf(diff.text, searchFrom) < 0)
									searchFrom--;
								for (int i=0; i<=searchFrom; i++)
									if (previous.getContent().charAt(i) == '\n') {
										lineNo++;
										//System.out.println(lineNo+" : "+i);
									}
								dmpOut.append("<tr>");
								dmpOut.append("<td class='lineno'>"+lineNo+"</td>");
								dmpOut.append("<td>&nbsp;</td><td class='removed'>");
								dmpOut.append(Utils.xmlEncode(diff.text));
								dmpOut.append("</td></tr>\n");
								hardChanges = true;
							} else {
								softChanges = true;
							}
							break;
						default:
							logger.error("diff result="+diff.operation+", now what?");
					}
				}
			}
			dmpOut.append("</table></p>\n");
			if (hardChanges) {
				ret = dmpOut.toString();
			} else if (softChanges) {
				ret = blockmessage("No significant changes (only whitespace)");
			}
		}
		else
		{
			ret = blockmessage("No Previous Version.");
		}

		return ret;
	}

    public String message (String text)
    {
        return "<tr><td colspan='5' class='diffmessage'>" + text + "</td></tr>";
    }

    public String blockmessage (String text)
    {
        return "<p class='diff'><table class='difftable' width='95%' border='1'>\n"	+ message(text) + "</table></p>\n";
    }
}
