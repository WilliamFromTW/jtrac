package info.jtrac.wiki;

import info.jtrac.wiki.events.*;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import info.jtrac.wicket.JtracSession;

public class REST extends HttpServlet {

	private static final EventBus eventBus = EventBus.getDefault();

	private Model model;
	private Policy policy;

	@Override
	public void init() throws ServletException {
		eventBus.register(this);
	}

	@Override
	public void destroy() {
		eventBus.unregister(this);
	}

	/* called by EventBus */
	@Subscribe(sticky = true)
	public void onEvent (PolicyEvent event) {
		model = event.getModel();
		policy = event.getPolicy();
	}

	@Override
	protected void doPost (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json;charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");

		PrintWriter out = response.getWriter();
		JsonObject json = new JsonObject();

		try {
			String op = request.getPathInfo();
			if (op == null) {
				json.put("result", "error");
				json.put("error", "operation is null");
			} else {
				String page = request.getQueryString();
				if (! model.contains(page)) {
					json.put("result", "error");
					json.put("error", "page "+page+" doesn't exist");
				} else {
					Long userId = getUserId(request);
					switch (op) {
						case "/watch":
							addWatch(userId, page);
							json.put("result", "watch");
							break;
						case "/unwatch":
							removeWatch(userId, page);
							json.put("result", "unwatch");
							break;
						default:
							json.put("result", "error");
							json.put("error", "op "+op+" ???");
					}
				}
			}
			//Wiki.info(json.toJson());
			out.print(json.toJson());
		} finally {
			out.close();
		}
	}

	protected Long getUserId (HttpServletRequest request) {
		HttpSession session = request.getSession();
		JtracSession jtSession = (JtracSession) session.getAttribute("wicket:jtrac-app:session");
		if (jtSession == null)
			return null;
		else
			return jtSession.getUser().getId();
	}

	private void addWatch (Long userId, String page) {
		String watches = policy.get("watches."+page);
		if (watches == null || watches.trim().isEmpty()) {
			policy.put("watches."+page, ","+userId+",");
		} else if (! watches.contains(","+userId+",")) {
			policy.put("watches."+page, watches+userId+",");
		}
		//Wiki.info("watches."+page+"="+policy.get("watches."+page));
		eventBus.post(new UpdatePropertiesEvent());
	}

	private void removeWatch (Long userId, String page) {
		String watches = policy.get("watches."+page);
		if (watches != null && watches.contains(","+userId+",")) {
			watches = watches.replaceAll(","+userId+",", ",");
			if (watches.indexOf(",") == watches.lastIndexOf(",")) {
				policy.remove("watches."+page);
			} else {
				policy.put("watches."+page, watches);
			}
			//Wiki.info("watches."+page+"="+policy.get("watches."+page));
			eventBus.post(new UpdatePropertiesEvent());
		}
	}
}
