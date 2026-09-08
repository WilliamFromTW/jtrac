package info.jtrac.wiki;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.stringtree.factory.CachedStringFetcher;
import org.stringtree.factory.StringFetcher;
import org.stringtree.factory.memory.MapStringRepository;
import org.stringtree.util.tract.Tract;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTMLRenderer
{
    private static final Logger logger = LoggerFactory.getLogger(HTMLRenderer.class);

	private static final SimpleDateFormat yymmddFormat = new SimpleDateFormat("d MMM yyyy");

	private ClassicToHTMLFilter filter;

	public HTMLRenderer (ClassicToHTMLFilter filter)
	{
		this.filter = filter;
	}

	private String convert (String content) throws IOException
	{
        StringWriter out = new StringWriter();
		filter.filter(new StringReader(content), out);
		return out.toString();
	}

	private String splitTitle (String name)
	{
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < name.length(); ++i)
		{
			char c = name.charAt(i);
			if (c == '_')
			{
				buf.append(' ');
			}
			else
			{
				if (i > 0 && Character.isUpperCase(c))
				{
					buf.append(' ');
				}
				buf.append(c);
			}
		}

		return buf.toString();
	}

	private void putDefaultValues (Map<String,String> map)
	{
		map.put("previous.version", "old/EmptyPage");
		map.put("page.name", "old/EmptyPage");
		map.put("last.edit", "never");
	}

	public void render (Tract page, String mode, boolean isFiltered, boolean isBackup, HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException
	{
		String content = page.getContent();

        if ("edit".equals(mode)) {
			// roundtrip HTML entities
			//logger.info("escaping HTML entities");
            try {
                Pattern p = Pattern.compile("&([A-Z]?[a-z]+|#[0-9]+);");
				//logger.info("matches?: " + p.matcher(content).find());
				if (p.matcher(content).find()) {
					Matcher m = p.matcher(content);
					int i = 0;
					StringBuilder sb = new StringBuilder(content.length()+100);
					while (m.find()) {
						//logger.info(m.group());
						sb.append(content.substring(i, m.start()));
						sb.append("&amp;");
						sb.append(m.group(0).substring(1));
						i = m.end();
					}
					if (i < content.length())
						sb.append(content.substring(i));
					content = sb.toString();
				}
            } catch (PatternSyntaxException psex) {
                logger.error("Can't construct regular expression - " + psex.getMessage());
            }
		}

		Map<String,String> tokens = new HashMap<>();
		putDefaultValues(tokens);

		if (isFiltered)
		{
			try {
				content = convert(content);
			} catch (Throwable ex) {
				// catching this isn't good style; I'm using it as a debugging aid here
				// because exceptions are being thrown deep down in the stringtree library,
				// and I have no idea what's causing those
				logger.error("caught exception during 'convert' call; page="+page.getAttribute("page.name")
							+"; mode="+mode+": "+ex.getMessage());

				tokens.put("MESSAGE", "An exception occurred - please try again.");

				StringWriter rexString = new StringWriter();
				ex.printStackTrace(new PrintWriter(rexString));
				logger.info(rexString.toString());
			}
		}

		tokens.putAll(page);  
		tokens.put("CONTENT", content);

		String title = (String) tokens.get("seo.name");  
		if (null == title || title.trim().length() == 0) {  
			title = splitTitle((String) tokens.get("page.name"));  
		}  
		tokens.put("TITLE", title);  

		// add all tokens as request attributes
		for (Map.Entry<String,String> entry : tokens.entrySet()) {
			String name = entry.getKey();
			// convert "one.two" to "oneTwo" because EL doesn't like dots in attribute names
			while (name.contains(".")) {
				int idx = name.indexOf(".");
				name = name.substring(0, idx) + name.substring(idx+1,idx+2).toUpperCase() + name.substring(idx+2);
			}
			//logger.info("adding "+name+" = "+entry.getValue());
			/*
			if (name.equals("lastEdit")) {
				String val = entry.getValue();
				try {
					val = yymmddFormat.format(Wiki.frikiFormat.parse(val));
				} catch (ParseException pex) {
					// ignore, it's likely "never"
					//logger.error("can't parse "+val);
				}
				request.setAttribute(name, val);
			} else {
				request.setAttribute(name, entry.getValue());
			}
			*/
				request.setAttribute(name, entry.getValue());
		}

		request.getRequestDispatcher("/WEB-INF/jsp/"+mode+".jsp").forward(request, response);
	}
}
