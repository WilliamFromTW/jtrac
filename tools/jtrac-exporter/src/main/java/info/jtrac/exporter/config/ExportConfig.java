package info.jtrac.exporter.config;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ExportConfig {

    private String dbUrl;
    private String dbUser = "sa";
    private String dbPassword = "";
    private String dbDriver;
    private File attachmentsDir;
    private File outputDir = new File("./jtrac-html-export");
    private String lang = "zh-TW";
    private String spaceFilter;
    private Set<String> targetSpacePrefixCodes = new HashSet<String>();
    private boolean help = false;

    public static ExportConfig parse(String[] args) {
        ExportConfig config = new ExportConfig();
        if (args == null || args.length == 0) {
            config.setHelp(true);
            return config;
        }

        for (String arg : args) {
            if (arg.equals("--help") || arg.equals("-h") || arg.equals("-?")) {
                config.setHelp(true);
                return config;
            } else if (arg.startsWith("--db-url=")) {
                config.setDbUrl(arg.substring("--db-url=".length()).trim());
            } else if (arg.startsWith("--db-user=")) {
                config.setDbUser(arg.substring("--db-user=".length()).trim());
            } else if (arg.startsWith("--db-password=") || arg.startsWith("--db-pwd=")) {
                int eq = arg.indexOf('=');
                config.setDbPassword(arg.substring(eq + 1).trim());
            } else if (arg.startsWith("--db-driver=")) {
                config.setDbDriver(arg.substring("--db-driver=".length()).trim());
            } else if (arg.startsWith("--attachments-dir=")) {
                String p = arg.substring("--attachments-dir=".length()).trim();
                if (!p.isEmpty()) {
                    config.setAttachmentsDir(new File(p));
                }
            } else if (arg.startsWith("--out=") || arg.startsWith("--output-dir=")) {
                int eq = arg.indexOf('=');
                String p = arg.substring(eq + 1).trim();
                if (!p.isEmpty()) {
                    config.setOutputDir(new File(p));
                }
            } else if (arg.startsWith("--lang=")) {
                config.setLang(arg.substring("--lang=".length()).trim());
            } else if (arg.startsWith("--space=")) {
                config.setSpaceFilter(arg.substring("--space=".length()).trim());
            } else if (arg.startsWith("--jtrac-home=")) {
                // Convenience helper: auto set db-url and attachments-dir if db-url not yet given
                String home = arg.substring("--jtrac-home=".length()).trim();
                File homeDir = new File(home);
                if (config.getDbUrl() == null) {
                    File dbFile = new File(homeDir, "db/jtrac");
                    String path = dbFile.getAbsolutePath().replace('\\', '/');
                    config.setDbUrl("jdbc:hsqldb:file:" + path + ";shutdown=true;hsqldb.lock_file=false;readonly=true");
                }
                if (config.getAttachmentsDir() == null) {
                    File attDir = new File(homeDir, "attachments");
                    if (attDir.exists() && attDir.isDirectory()) {
                        config.setAttachmentsDir(attDir);
                    }
                }
            }
        }

        return config;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public String getDbDriver() {
        return dbDriver;
    }

    public void setDbDriver(String dbDriver) {
        this.dbDriver = dbDriver;
    }

    public File getAttachmentsDir() {
        return attachmentsDir;
    }

    public void setAttachmentsDir(File attachmentsDir) {
        this.attachmentsDir = attachmentsDir;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getSpaceFilter() {
        return spaceFilter;
    }

    public void setSpaceFilter(String spaceFilter) {
        this.spaceFilter = spaceFilter;
        this.targetSpacePrefixCodes.clear();
        if (spaceFilter != null && !spaceFilter.trim().isEmpty()) {
            String[] parts = spaceFilter.split("[,;]");
            for (String p : parts) {
                String trimmed = p.trim();
                if (!trimmed.isEmpty()) {
                    this.targetSpacePrefixCodes.add(trimmed.toUpperCase());
                }
            }
        }
    }

    public Set<String> getTargetSpacePrefixCodes() {
        return targetSpacePrefixCodes;
    }

    public void setTargetSpacePrefixCodes(Collection<String> codes) {
        this.targetSpacePrefixCodes.clear();
        if (codes != null) {
            for (String c : codes) {
                if (c != null && !c.trim().isEmpty()) {
                    this.targetSpacePrefixCodes.add(c.trim().toUpperCase());
                }
            }
        }
    }

    public boolean hasSpaceFilter() {
        return !targetSpacePrefixCodes.isEmpty();
    }

    public boolean isSpaceAllowed(String prefixCode) {
        if (targetSpacePrefixCodes.isEmpty()) {
            return true;
        }
        return prefixCode != null && targetSpacePrefixCodes.contains(prefixCode.trim().toUpperCase());
    }

    public boolean isHelp() {
        return help;
    }

    public void setHelp(boolean help) {
        this.help = help;
    }
}
