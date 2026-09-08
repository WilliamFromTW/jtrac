package info.jtrac.wiki;

import org.stringtree.util.tract.Tract;

public class Page extends Tract
{
	static final long serialVersionUID = 9159321418176589496L;

	public Page(String name, String text)
	{
		setContent(text);
		put("page.name", name);
	}

	public Page(String name)
	{
		this(name, "Describe '" + name + "' here...");
	}
}
