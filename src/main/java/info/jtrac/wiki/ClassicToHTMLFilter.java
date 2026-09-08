package info.jtrac.wiki;

import info.jtrac.wicket.JtracApplication;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.stringtree.util.ReaderUtils;
import org.stringtree.util.StringUtils;
import org.stringtree.util.tract.Tract;

import org.commonmark.Extension;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.*;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.ins.InsExtension;
//import com.novocode.ornate.commonmark.SubscriptExtension;
//import com.novocode.ornate.commonmark.SuperscriptExtension;

public class ClassicToHTMLFilter
{
	private static final Pattern CAMELCASE_PATTERN = Pattern.compile("(?<![\\p{Ll}\\._-])(?<!old/)~?([\\p{Lu}]+[\\p{Ll}0-9]+){2,}");
	// with a negative lookbehind so it doesn't match links to previous versions, and also not if the 
	// preceding character is a lowercase letter or a number, or one of various other characters

	private Parser parser;
	private HtmlRenderer renderer;
	private LucenePageRepository repository;
	private String baseUrl;

	public ClassicToHTMLFilter (LucenePageRepository repository, String baseUrl, boolean enableMarkdownHtml)
	{
		this.repository = repository;
		this.baseUrl = baseUrl;

		Set<Extension> extensions = new HashSet<>();
		extensions.add(AutolinkExtension.create());
		extensions.add(StrikethroughExtension.create());
		extensions.add(InsExtension.create());
		extensions.add(TablesExtension.create());
//		extensions.add(SubscriptExtension.create());
//		extensions.add(SuperscriptExtension.create());

		parser = Parser.builder()
				.extensions(extensions)
				.build();
		renderer = HtmlRenderer.builder()
				.extensions(extensions)
				.attributeProviderFactory(new AttributeProviderFactory() {
					@Override
					public AttributeProvider create (AttributeProviderContext context) {
						return new AttributeProvider() {
							@Override
							public void setAttributes (Node node, String tagName, Map<String, String> attributes) {
								if (node instanceof Link) {
									// external links open in a new window/tab
									String href = attributes.get("href");
									if (! href.startsWith(baseUrl))
										attributes.put("target", "_blank");
								} else if (tagName.equals("table") || tagName.equals("th") || tagName.equals("td")) {
									// put border around tables
									attributes.put("class", "border");
								}
							}
						};
					}
				})
				.escapeHtml(!enableMarkdownHtml)
				.sanitizeUrls(true)
				.build();
	}

	public void filter (Reader reader, Writer out)
		throws IOException
	{
		String content = ReaderUtils.readReader(reader);

		// find CamelCase words, and either links them to the respective page (if they exist)
		// or add a ? which links to the page where one can create that page
		// But don't do either if the CamelCase word is preceded by ~
		if (content != null) {
			Node document = parser.parse(content);
			content = renderer.render(document).trim();
			StringBuilder sb = new StringBuilder(content.length());
			Matcher matcher = CAMELCASE_PATTERN.matcher(content);
			int pos = 0;
			while (matcher.find()) {
				sb.append(content.substring(pos, matcher.start()));
				String pageName = matcher.group(0);
				if (content.charAt(matcher.start()) == '~') {
					// ignore if CamelCase word is preceded by ~
					sb.append(pageName.substring(1));
				} else {
					if (repository.contains(pageName)) {
						sb.append(String.format("<a href='view?%s'>%s</a>", pageName, pageName));
					} else {
						sb.append(String.format("%s<a href='edit?%s'>?</a>", pageName, pageName));
					}
				}
				pos = matcher.end();
			}
			if (pos < content.length())
				sb.append(content.substring(pos, content.length()));
			content = sb.toString();
		}

		out.write(StringUtils.nullToEmpty(content));
	}

	public String makeLinkSource(String name)
	{
		return "<a href='view?" + name + "'>" + name + "</a>";
	}
}
