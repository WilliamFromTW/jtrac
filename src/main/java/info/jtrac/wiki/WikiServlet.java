package info.jtrac.wiki;

import info.jtrac.Jtrac;
import info.jtrac.domain.User;
import info.jtrac.wicket.JtracSession;
import info.jtrac.wiki.events.*;

import java.io.*;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.*;
import javax.servlet.http.*;

import org.stringtree.factory.Fetcher;
import org.stringtree.factory.FetcherHelper;
import org.stringtree.factory.Repository;
import org.stringtree.util.*;
import org.stringtree.util.tract.Tract;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import org.apache.wicket.Application;
import org.apache.wicket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WikiServlet extends HttpServlet 
{
	static final long serialVersionUID = -4666835724623261987L;

    private static final Logger logger = LoggerFactory.getLogger(WikiServlet.class);

	private static final EventBus eventBus = EventBus.getDefault();

	private static final SimpleDateFormat wikiFormat = new SimpleDateFormat("d MMM yyyy");

	private ServletContext sc;
	private Jtrac jtrac;
	private String baseUrl;

	private static final String settingsFileName = "wiki.prp";
	private ConcurrentMap<String,Set<Long>> emailsSent = new ConcurrentHashMap<>();

	private transient Model model;
	private transient Policy policy;
	private File baseDir;
	private transient FileRepository driver = null;

	/* called by EventBus */
	@Subscribe
	public void onEvent (UpdatePropertiesEvent event) {
		writeProperties(true);
	}

	private void setPolicy (Policy policy, String name, Object value)
	{
		if (!policy.contains(name))
		{
			policy.put(name, value);
		}
	}

	private void writeProperties (boolean changeWatches) {
		File propFile = new File(baseDir, settingsFileName);
		if (!propFile.exists() || propFile.canWrite()) try {
			logger.debug("updating external config file '" + propFile + "'" + (changeWatches ? ", updating watches" : ""));
			Properties propsOld = new Properties();
			propsOld.putAll(policy.getMap());
			FileOutputStream out = new FileOutputStream(propFile);
			propsOld.store(out, "Wiki configuration properties");
			out.close();
		} catch (IOException ioe) {
			logger.error(ioe.getMessage());
		}
	}

	private void setPolicy (File dir)
	{
		baseDir = dir;
		logger.debug("located external storage dir '" + baseDir + "'");

		try
		{
			File propFile = new File(baseDir, settingsFileName);

			logger.debug("looking for external config file '" + propFile + "'");

			if (propFile.exists() && propFile.canRead())
			{
				logger.debug("reading external config file '" + propFile + "'");
				Properties propsOld = new Properties();
				FileInputStream in = new FileInputStream(propFile);
				propsOld.load(in);
				in.close();
				policy.putAll(propsOld);
			}

			setPolicy(policy, "repository-location", "pages");

			writeProperties(false);
		}
		catch(IOException ioe)
		{
			logger.error(ioe.getMessage());
		}

		driver = new FileRepository();
		driver.setPolicy(policy);

        LucenePageRepository repository = new LucenePageRepository(driver, Integer.parseInt(jtrac.loadConfig("wiki.maxChangeLogSize", "-1")));
		boolean isPublic = "true".equals(jtrac.loadConfig("wiki.public", "false"));
		boolean allowsHtml = "true".equals(jtrac.loadConfig("wiki.markdown.html", "false"));

		ClassicToHTMLFilter filter = new ClassicToHTMLFilter(repository, baseUrl, !isPublic && allowsHtml);
		HTMLRenderer renderer = new HTMLRenderer(filter);
		model = new Model(repository, renderer, filter, policy);

		eventBus.postSticky(new PolicyEvent(policy, model));
	}

	@Override
	public void init (ServletConfig conf) throws ServletException {
		super.init(conf);
		sc = conf.getServletContext();

		ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(sc);
		jtrac = (Jtrac) applicationContext.getBean("jtrac");
		String baseDir = jtrac.getJtracHome();
		baseUrl = jtrac.loadConfig("jtrac.url.base");
		if (baseUrl == null) {
			baseUrl = "/";
		} else if (! baseUrl.endsWith("/"))
			baseUrl = baseUrl + "/";

		policy = new Policy(new File(baseDir));
		setPolicy(new File(baseDir));
		setPolicy(policy, "filterModes", "view,search,diff,preview");
		setPolicy(policy, "viewMode", "view");
		setPolicy(policy, "editMode", "edit");
		setPolicy(policy, "searchMode", "search");
		setPolicy(policy, "diffMode", "diff");
		setPolicy(policy, "updateMode", "update");
		setPolicy(policy, "previewMode", "preview");
        setPolicy(policy, "repository-location", baseDir);

		logger.debug("file.encoding="+System.getProperty("file.encoding"));
        if (policy.get("encoding") != null)
            Utils.setEncoding(policy.get("encoding"));

		eventBus.register(this);
	}

	@Override
	public void destroy() {
		eventBus.unregister(this);
	}

	private void show (HttpServletRequest request, HttpServletResponse response, String pageName, String mode, String author)
		throws IOException, ServletException
	{
		User user = getUser(request);
		Long userId = (user == null) ? null : user.getId();

		pageName = model.ensurePage(pageName);
		Tract page = model.get(pageName);

		if (mode.equals(policy.get("searchMode")))
		{
            page = model.search(pageName, request);
		}
		else if (mode.equals(policy.get("diffMode")))
		{
			page = model.get(pageName);
			boolean toCurrent = request.getParameter("cur") != null;
			page.put("diff", model.diff(pageName, toCurrent));

			Set<Long> users = emailsSent.get(pageName);
			if (users != null && userId != null) {
				users.remove(userId);
				logger.debug("removing user #"+userId+" from "+pageName+" after diff");
			}
		}
		else if (mode.equals(policy.get("editMode")))
		{
			page = model.get(pageName);
		}
		else if (mode.equals(policy.get("previewMode")))
		{
			page = new Page("Preview", request.getParameter("content"));
		}
		else	// must be viewMode
		{
			page = model.get(pageName);

			String redirectTo = page.getAttribute("redirect.to");
			if (redirectTo != null && redirectTo.length() > 0) {
				response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
				response.setHeader("Location", baseUrl+"wiki/view?"+redirectTo);
				return;
			}

			Set<Long> users = emailsSent.get(pageName);
			if (users != null && userId != null) {
				users.remove(userId);
				logger.debug("removing user #"+userId+" from "+pageName+" after view");
			}
		}

		boolean filterpage = ! "false".equals(page.getAttribute("filter"));
		String filtermodes = policy.get("filterModes");
		boolean isFiltered = filterpage &&
			filtermodes != null && filtermodes.indexOf(mode) >= 0;

		boolean isBackup = pageName.startsWith(FileRepository.backupDirName);

		model.render(page, mode, isFiltered, isBackup, request, response);
	}

	private String getMode (HttpServletRequest request)
	{
		String mode = request.getServletPath();

		// strip preceding path info from mode (if present)
		int sep = mode.lastIndexOf("/");
		if (sep > -1) {
			mode = mode.substring(sep);
		}

		if ("".equals(mode) || "/".equals(mode)) {
			mode = policy.get("viewMode");
		} else {
			mode = mode.substring(1);
		}

		return mode;
	}

	private String getAuthor (HttpServletRequest request)
	{
		// the actual IP address is stored in this special header
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.isEmpty()) {
			 ip = request.getRemoteAddr();
		}

		StringBuilder buf = new StringBuilder();
		if (!StringUtils.isBlank(ip))
		{
			buf.append(ip).append(" ");
		}

		User user = getUser(request);
		if (user != null)
		{
			buf.append(user.getName());
		}

		return buf.toString();
	}

	protected User getUser (HttpServletRequest request) {
		HttpSession session = request.getSession();
		JtracSession jtSession = (JtracSession) session.getAttribute("wicket:jtrac-app:session");
		if (jtSession == null)
			return null;
		else
			return jtSession.getUser();
	}

	@Override
	public void doGet (HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		boolean isPublic = "true".equals(jtrac.loadConfig("wiki.public", "false"));

		User user = getUser(request);
		if (user == null || user.getName().equals("Guest")) {
			if (! isPublic) {
				response.sendRedirect("/app/login");
				return;
			}
			user = null;
		}
		request.setAttribute("locale", (user == null) ? "en" : user.getLocale());

		// take the page name from the query string
		// unescape and normalize allow us to use umlauts in view mode:
		//     StadtspaziergängeBerlin and StadtspaziergaengeBerlin both point to the same page
		String pageName = Utils.normalizeText(Utils.unescape(request.getQueryString()));
		pageName = model.ensurePage(pageName);
		if (pageName.contains("&")) {
			// might be "desktop" or "mobile" parameter
			pageName = pageName.substring(0, pageName.indexOf("&"));
		}

		String mode = getMode(request);
		String author = getAuthor(request);

		request.setAttribute("colorGray", jtrac.loadConfig("jtrac.color.gray", "#CCCCCC"));
		request.setAttribute("colorHeader", jtrac.loadConfig("jtrac.color.header", "#E1ECFE"));
		request.setAttribute("colorLightBlue", jtrac.loadConfig("jtrac.color.lightblue", "#E1ECFE"));
		request.setAttribute("colorMediumBlue", jtrac.loadConfig("jtrac.color.mediumblue", "#C3D9FF"));
		request.setAttribute("colorDarkBlue", jtrac.loadConfig("jtrac.color.darkblue", "#0000D9"));
		request.setAttribute("colorError", jtrac.loadConfig("jtrac.color.error", "#CC2200"));
		request.setAttribute("colorErrorBg", jtrac.loadConfig("jtrac.color.errorbg", "#FFB6C1"));
		request.setAttribute("jtracVersion", jtrac.getReleaseVersion());
		request.setAttribute("user", user);

		String watches = policy.get("watches."+pageName);
		request.setAttribute("isWatching", user!=null && watches!=null && watches.contains(","+user.getId()+","));

		if (mode.equals(policy.get("searchMode"))) {
			show(request, response, Utils.sanitize(request.getParameter("q")), mode, author);
		} else {
			show(request, response, Utils.sanitize(pageName), mode, author);
		}
	}

	@Override
	public void doPost (HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		User user = getUser(request);
		if (user == null || user.getName().equals("Guest")) {
			response.sendRedirect("/app/login");
			return;
		}

		String page = Utils.sanitize(request.getParameter("page"));
		String content = request.getParameter("content");
		String author = getAuthor(request);
		String mode = getMode(request);

		if (mode.equals(policy.get("updateMode")))
		{
			if (!page.startsWith(FileRepository.backupDirName)) {
				String comment = request.getParameter("comment");
				String seoName = request.getParameter("seo.name");
				String redirectTo = request.getParameter("redirect.to");

				// don't write or log anything if nothing changed
				if (null != model.update(page, content, comment, user.getName(), seoName, redirectTo)) {
					if (model.contains(page)) {
						model.logLink(page, baseUrl, author);
					}
				}

				Long userMadeChange = getUser(request).getId();
				String sendEmails = policy.get("watches."+page);
				if (sendEmails != null && !sendEmails.trim().isEmpty()) {
					Date now = new Date();
					String[] usersToSendTo = sendEmails.trim().replaceAll("[,;]", " ").split("\\s+");
					for (String userToSendTo : usersToSendTo) {
						if (StringUtils.isBlank(userToSendTo))
							continue;
						Long userToSendToId = Long.valueOf(userToSendTo);
						if (userMadeChange.equals(userToSendToId)) {
							logger.debug("not sending email to self");
							continue;
						}
						logger.debug("user #"+userToSendToId+", email to send to user #"+userMadeChange);

						// don't send mail if user got one already
						Set<Long> users = emailsSent.get(page);
						if (users != null && users.contains(userToSendToId)) {
							logger.debug("skip user #"+userMadeChange+" because user got one already");
							continue;
						}

						jtrac.sendWikiPageUpdated(jtrac.loadUser(userMadeChange), jtrac.loadUser(userToSendToId),
												page, baseUrl+"wiki/view?"+page, baseUrl+"wiki/diff?"+page);

						// add user to emailsSent
						if (users == null) {
							users = new HashSet<Long>();
							emailsSent.put(page, users);
						}
						users.add(userToSendToId);
						logger.debug("adding user #"+userToSendToId+" to sent emails for "+page);
					}
				}
            }
		   	response.sendRedirect("view?" + Utils.escape(page));
		}
		else
		{
			show(request, response, page, mode, author);
		}
	}

}

