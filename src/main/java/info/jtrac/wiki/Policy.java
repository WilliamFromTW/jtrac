package info.jtrac.wiki;

import java.io.File;
import java.util.Map;

import org.stringtree.factory.memory.MapStringRepository;
import org.stringtree.util.BooleanUtils;

public class Policy extends MapStringRepository
{
	private File baseDir;

	public Policy(Map map, File baseDir)
	{
		super(map);
		this.baseDir = baseDir;
	}

	public Policy(File baseDir)
	{
		this.baseDir = baseDir;
	}

	public boolean getBoolean(String name)
	{
		return BooleanUtils.booleanValue(getObject(name));
	}

	public boolean getBoolean(String name, boolean dfl)
    {
        return BooleanUtils.booleanValue(getObject(name), dfl);
    }

	public int getInteger(String name, int dfl)
    {
		try {
			return Integer.parseInt((String) getObject(name));
		} catch (NumberFormatException nfex) {
			return dfl;
		}
    }

	public File getFile(String name)
	{
		File ret = null;

		Object obj = getObject(name);
		if (obj != null)
		{
			if (obj instanceof File)
			{
				ret = (File)obj;
			}
			else
			{
				ret = new File(baseDir, (String)obj);
			}
		}

		return ret; 
	}

	public void putAll(Map other)
	{
		map.putAll(other);
	}
}
