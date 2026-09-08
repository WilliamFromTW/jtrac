package info.jtrac.wiki;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.stringtree.factory.Container;
import org.stringtree.factory.Repository;
import org.stringtree.factory.TractFetcher;
import org.stringtree.factory.TractStorer;
import org.stringtree.util.tract.Tract;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LucenePageRepository 
	implements TractFetcher, TractStorer, Repository, Container
{
    private static final Logger logger = LoggerFactory.getLogger(LucenePageRepository.class);

	private final static DateFormat frikiFormat = new SimpleDateFormat("dd MMMM yyyy");

	public static final List<String> STOP_WORDS = Arrays.asList(StandardAnalyzer.STOP_WORDS);

    protected FileRepository driver;
    protected Policy policy = null;
	protected int maxChangeLogSize = -1;

	protected Directory dir;
	protected Analyzer analyzer;

	public LucenePageRepository (FileRepository driver, int maxChangeLogSize)
    {
        this.driver = driver;
        this.policy = driver.getPolicy();
        this.maxChangeLogSize = maxChangeLogSize;

		dir = new RAMDirectory();

		//analyzer = new StandardAnalyzer();
		analyzer = new StandardAnalyzer() {
			@Override
			public TokenStream tokenStream (String fieldName, Reader reader) {
				// Filters StandardTokenizer with LowerCaseFilter, PorterStemFilter
				// and StopFilter, using a list of given stop words (in this case, English).
				// Basically a StandardAnalyzer that also does Porter stemming (for English).
				final StandardTokenizer src = new StandardTokenizer(reader);
				TokenStream tok = new LowerCaseFilter(src);
				tok = new StopFilter(tok, STOP_WORDS);
				tok = new PorterStemFilter(tok);
				return tok;
			}
		};

		createIndex();
    }

	public Object getObject (String name)
	{
		Tract page = null;

		if (driver.isReadable(name))
		{
			page = driver.get(name);
		}

		if (page == null)
		{
			page = new Page(name);
		}

		return page;
	}

	public Tract get(String name)
	{
		Tract ret = (Tract) getObject(name);
		ret.put("escaped.name", Utils.escape(name));
		return ret;
	}

    public void put (String name, Tract page, boolean backup)
    {
		if (driver.isWritable(name))
		{
			if (backup)
			{
				String previous = driver.backup(name);
				page.put("previous.version", previous);
			}
			Date dt = new Date();
			page.put("last.edit", frikiFormat.format(dt));
			driver.put(name, page);
		}

        if (!name.equals("RecentChanges"))
            addPageToIndex(name, page);
    }

	public void put(String name, Tract page)
	{
		put(name, page, true);
	}

	public void put(String name, Object obj)
	{
		if (obj instanceof Tract)
		{
			put(name, (Tract)obj, false);
		}
		else
		{
			logger.error("Warning. Attempt to put unrecognized object '" + obj + "' into page repository");
		}
	}

	public Iterator allPageNames()
	{
		return driver.allPageNames();
	}

	public int getNumberPages() 
	{
		return driver.getNumberPages();
	}

	// this is used for the changelog page only
	public void append(String pageName, String text)
	{
		Tract page = get(pageName);
		String content = text + page.getContent();

		if (maxChangeLogSize > -1) {
			int nlIndex = content.indexOf("\n", maxChangeLogSize);
			if (nlIndex > -1)
				content = content.substring(0, nlIndex+1);
		}

		page.setContent(content);
		put(pageName, page, false);
	}

	public void remove(String name)
	{
		driver.remove(name);
	}

	public void clear()
	{
		driver.clear();
	}

	public boolean contains(String name)
	{
		return driver.contains(name);
	}

	public String escape(String name)
	{
		return Utils.escape(name);
	}

	public String unescape(String name)
	{
		return Utils.unescape(name);
	}

	// for future expansion
	private boolean matchAny = false;

    public List<SearchResult> matchingPages (String pattern) {
        List<SearchResult> ret = new ArrayList<>();

        if (pattern != null && pattern.length() > 0) try {
			logger.debug("searching for: " + pattern);

			pattern = pattern.replaceAll("\\\"|\\?|\\*|\\(|\\)|\\[|\\]|:|\\+|\\-|\\{|\\}|\\*|~|!|\"|\\\\|\\&", " ");

			StringBuilder queryString = new StringBuilder(100);
			queryString.append("(");
			StringTokenizer st = new StringTokenizer(pattern);
			boolean isFirst = true;
			while (st.hasMoreTokens()) {
				String word = st.nextToken();

				// ignore words that interfere with the Lucene query syntax
				String lcWord = word.toLowerCase();
				if ("and".equals(lcWord) || "or".equals(lcWord) || "not".equals(lcWord))
					continue;

				if (! isFirst) {
					if (matchAny)
						queryString.append(" OR ");
					else
						queryString.append(" AND ");
				}

				queryString.append("(title:").append(word).append("*")
							.append(" OR altTitle:").append(word).append("*")
							.append(" OR contents:").append(word).append("*)");

				isFirst = false;
			}
			// also search for the pattern in content directly
			queryString.append(")");
			queryString.append(" OR contents:\"").append(pattern.trim()).append("\"");
			logger.debug("query:"+queryString.toString());

            IndexSearcher searcher = new IndexSearcher(dir);
			QueryParser parser = new QueryParser("contents", analyzer);
			Query query = parser.parse(queryString.toString());
			Hits hits = searcher.search(query);

			int numDocs = hits.length();
            logger.debug(numDocs+" matching documents");
            for (int i = 0; i < numDocs; i++) {
                Document doc = hits.doc(i);
                if (doc.get("title") != null) {
					//logger.debug(doc.get("title"));
					Tract tract = get(doc.get("title"));
					String content = tract.getContent();
					String lcContent = content.toLowerCase();
					List<String> fragments = new ArrayList<>();

					st = new StringTokenizer(pattern);
					while (st.hasMoreTokens()) {
						String word = st.nextToken().toLowerCase();
						if (STOP_WORDS.contains(word))
							continue;

						int idx = 0;
						int next = lcContent.indexOf(word, idx);
						while (next != -1) {
							int start = next - 50;
							int end = next + 50;
							if (start < 0) {
								start = 0;
								end += 50;
							}
							if (end > lcContent.length()) {
								end = lcContent.length();
								start -= 50;
								if (start < 0)
									start = 0;
							}

							String fragment = content.substring(start, end);
							fragment = fragment.replaceAll("(?i)"+word, "<span class='red'>$0</span>");
							fragments.add(fragment);
							idx = end;
							if (idx == lcContent.length())
								break;
							else
								next = lcContent.indexOf(word, idx);
						}
					}

					ret.add(new SearchResult(doc.get("title"), doc.get("seo.name"), fragments));
				}
            }
        } catch (Exception ex) {
            logger.error("matchingPages: " + ex.getMessage());
        }

        ret.sort((o1, o2) -> {
				String name1 = ((SearchResult) o1).getName();
				String name2 = ((SearchResult) o2).getName();
				return name1.compareTo(name2);
		});

        return ret;
    }

    protected void addPageToIndex (String name, Tract page) {
		if (page == null)
			return;

        try {
			Term term = new Term("uniqueTitle", ":"+name+":");
			IndexReader reader = IndexReader.open(dir);
			int deleted = reader.deleteDocuments(term);
			reader.close();

			IndexWriter writer = new IndexWriter(dir, analyzer, false);

			String content = page.getContent();
			Document doc = new Document();

			doc.add(new Field("title", name, Field.Store.YES, Field.Index.TOKENIZED));
			if (page.get("seo.name") != null)
				doc.add(new Field("altTitle", (String) page.get("seo.name"), Field.Store.YES, Field.Index.TOKENIZED));
			doc.add(new Field("uniqueTitle", ":"+name+":", Field.Store.YES, Field.Index.UN_TOKENIZED));
			doc.add(new Field("contents", content, Field.Store.NO, Field.Index.TOKENIZED));

			logger.info("indexing "+name+", deleted "+deleted);

			writer.addDocument(doc);
			writer.close();
		} catch (Exception ex) {
			logger.warn("addPageToIndex: " + ex.getMessage());
		}
	}

    protected void createIndex() {
		try {
			IndexWriter writer = new IndexWriter(dir, analyzer, true);

			int numPages = 0;

			Iterator all = allPageNames();
			while (all.hasNext()) {
				String name = (String) all.next();
				// don't index the read and write logs
				if (name.equals("RecentChanges"))
					continue;

				Tract tract = get(name);
				if (tract != null) {
					// there's no point indexing a page that isn't displayed
					String redirectTo = tract.getAttribute("redirect.to");
					if (redirectTo != null && redirectTo.length() > 0)
						continue;

					//logger.debug("indexing " + name);
					String content = tract.getContent();
					if (content != null) {
						Document doc = new Document();

						// Add the title of the page as a field named "title". Use a Text field,
						// so that the index stores the title, and it becomes searchable
						doc.add(new Field("title", name, Field.Store.YES, Field.Index.TOKENIZED));
						doc.add(new Field("title", new StringBuilder(name).reverse().toString(), Field.Store.YES, Field.Index.TOKENIZED));

						// If a better title exists, put it in the index so we can display it instead of the raw page name
						if (tract.get("seo.name") != null)
							doc.add(new Field("altTitle", (String) tract.get("seo.name"), Field.Store.YES, Field.Index.TOKENIZED));

						// A pseudo-unique ID of the document is stored along with it.
						// That way we have a sure way of finding and deleting it from
						// the index when the page is updated.
						doc.add(new Field("uniqueTitle", ":"+name+":", Field.Store.YES, Field.Index.UN_TOKENIZED));

						// Add the contents of the file a field named "contents". Use an UnStored field,
						// so that the contents are indexed, but not actually stored in the index.
						doc.add(new Field("contents", content, Field.Store.NO, Field.Index.TOKENIZED));

						writer.addDocument(doc);
					}
					numPages++;
				}
			}
			logger.info("finished indexing " + numPages + " pages");

			writer.optimize();
			writer.close();
		} catch (Exception ex) {
			logger.error("createIndex: " + ex.getMessage());
			ex.printStackTrace();
		}
    }

	public class SearchResult {
		String title, altTitle;
		List<String> fragments;

		SearchResult (String title, String altTitle, List<String> fragments) {
			this.title = title;
			if (altTitle!=null && ! altTitle.isEmpty())
				this.altTitle = altTitle;
			this.fragments = fragments;
		}

		public String getName() { return title; }

		public String getAltTitle() { return altTitle; }

		public List<String> getFragments() { return fragments; }
	}
}

