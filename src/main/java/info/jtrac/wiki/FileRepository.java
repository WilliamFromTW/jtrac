package info.jtrac.wiki;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.stringtree.factory.Container;
import org.stringtree.factory.TractRepository;
import org.stringtree.util.tract.FileTractReader;
import org.stringtree.util.tract.FileTractWriter;
import org.stringtree.util.tract.Tract;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileRepository
	implements FilenameFilter, TractRepository, Container
{
    private static final Logger logger = LoggerFactory.getLogger(FileRepository.class);

	private static final DateFormat suffixFormat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");

	public static final String backupDirName = "old";
	private File dir, backup;
    private Policy policy = null;

    public void setPolicy (Policy policy)
	{
        this.policy = policy;
        this.dir = policy.getFile("repository-location");
		logger.info("Created File Page Repository at '" + dir + "'");

		backup = new File(dir, backupDirName);
		backup.mkdirs();
	}

    public Policy getPolicy() {
        return policy;
    }

	public Object getObject(String name)
	{
		return get(name);
	}

	public void put(String name, Object obj)
	{
		if (obj instanceof Tract) {
			put(name, (Tract)obj);
		} else {
			logger.error("Warning, attempt to put unknown object '" + obj + "' to Page repository");
		}
	}

	public void remove(String name)
	{
		// do nothing
	}

	public void clear()
	{
		// do nothing
	}

	private File pageFile(String name)
	{
		return new File(dir, Utils.escape(name));
	}

	private File backupFile(String name)
	{
		int prefix = backupDirName.length() + 1;
		String filename = name.substring(prefix);
		return new File(backup, Utils.escape(filename));
	}

	public boolean validName(String name)
	{
		return (name.indexOf('.')==-1 || name.startsWith(backupDirName)) && !backupDirName.equals(name);
	}

	public boolean isReadable(String name)
	{
		return validName(name) && name.indexOf(':')==-1
			&& !name.startsWith("/") && !name.startsWith("\\");
	}

	public boolean isWritable(String name)
	{
		return isReadable(name) && name.indexOf("/")==-1 && name.indexOf("\\") == -1;
	}

	protected void get(File file, Tract ret)
	{
		if (file.exists() && file.isFile() && file.canRead())
		{
			try
			{
				FileTractReader.load(ret, file, true);
				String name = file.getName();
			}
			catch(IOException ioe)
			{
				logger.error(ioe.getMessage());
			}
		}
	}

	public Tract get(String name)
	{
		Tract ret = new Page(name); 
		File file = (name.startsWith(backupDirName))
			? backupFile(name)
			: pageFile(name);

		get(file, ret);

		return ret;
	}

	public void put(File file, Tract page)
	{
		if ((file.exists() && file.isFile() && file.canWrite()) || !file.exists()) {
			try
			{
				FileTractWriter.store(page, file);
				String name = (String) page.get("page.name");
			}
			catch(IOException ioe)
			{
				logger.error(ioe.getMessage());
			}
		}
	}

	public void put(String name, Tract page)
	{
		page.put("page.name", name);
		put(pageFile(name), page);
	}

	public synchronized String backup (String name)
	{
		Tract page = get(name);
		String dated =  "." + suffixFormat.format(new Date());
		File file = new File(backup, Utils.escape(name) + dated);
		logger.debug("FileRepository.backup: backing up to "+file.getPath());
		try {
			if (! file.createNewFile())
				logger.error("FileRepository.backup: file did exist ???");
		} catch (IOException ioex) {
			logger.error("FileRepository.backup: "+ioex.getMessage());
		}
		put(file, page);
		name = backupDirName + "/" + name + dated;
		return name;
	}

	public boolean contains(String name)
	{
		return validName(name) && pageFile(name).canRead();
	}

	public boolean accept(File dir, String name)
	{
		return validName(name);
	}

	public Iterator allPageNames() 
	{
		List names = Arrays.asList(dir.list(this));
		List converted = new ArrayList(names.size());

		Iterator allFiles = names.iterator();
		while (allFiles.hasNext())
		{
			converted.add(Utils.unescape((String)allFiles.next()));
		} 

		converted.sort(null);
		return converted.iterator();
	}

	public int getNumberPages() 
	{
		return dir.list(this).length;
	}

	public void append(String name, String text)
	{
		File page = pageFile(name);
		if (page.canWrite())
		{
			try
			{
				Writer writer = new FileWriter(page.getAbsolutePath(), true);
				writer.write(text);
				writer.flush();
				writer.close();
			}
			catch(IOException ioe)
			{
				logger.error(ioe.getMessage());
			}
		}
	}
}
