# JTrac Build and Compilation Guide (English)

This guide provides step-by-step instructions for building, compiling, and packaging the JTrac project, along with an in-depth explanation of Maven dependency management and WAR packaging architecture.

---

## 1. Prerequisites

Before building the project, ensure your environment meets the following requirements:

- **Operating System**: Windows / Linux / macOS
- **Java Development Kit (JDK)**: JDK 8 or JDK 11 (JDK 11 recommended, e.g., `W:\developer\jdk-11.0.25.9-hotspot`)
- **Apache Maven**: Version 3.9.x or higher (e.g., `W:\developer\apache-maven-3.9.9`)

### Windows Environment Setup
On Windows, load the environment batch script in Command Prompt (CMD) before executing Maven:
```cmd
call W:\developer\maven.bat
```
This script configures `PATH` and `JAVA_HOME` for the current terminal session.

Verify your environment:
```cmd
mvn -version
```
The output should confirm the active Maven and Java versions.

---

## 2. Common Build Commands

Run the following commands from the JTrac project root directory (where `pom.xml` resides):

| Command | Description |
|---|---|
| `mvn compile` | Compiles 137 Java source files under `src/main/java` and processes resources |
| `mvn test-compile` | Compiles all unit test classes under `src/test/java` |
| `mvn test` | Runs unit tests (uses built-in in-memory HSQLDB; no external DB required) |
| `mvn package` | Runs tests and packages the full web application into `target/jtrac.war` |
| `mvn package -DskipTests` | Quickly builds and packages `target/jtrac.war`, skipping unit tests |
| `mvn clean` | Cleans up the `target/` directory and build artifacts |
| `mvn clean compile` | Cleans previous artifacts and re-compiles the entire codebase |

---

## 3. Automatic Dependency Management via Maven (`~/.m2/repository`)

JTrac is configured with standard Maven dependency management. All required third-party libraries (including Spring Framework, Apache Wicket, Hibernate, Acegi Security, Lucene, etc.) are formally declared in [`pom.xml`](../../pom.xml).

### Automatic Download & Cache Workflow:
1. When you first run `mvn compile` or `mvn package`, Maven connects to the remote repository (Maven Central).
2. All declared dependencies and transitive dependencies are automatically downloaded to your local user cache:
   - **Windows**: `%USERPROFILE%\.m2\repository\` (e.g., `C:\Users\username\.m2\repository\`)
   - **Linux / macOS**: `~/.m2/repository/`
3. Subsequent builds read directly from your local `.m2` repository cache. **You never need to manually search, download, or configure third-party JARs.**

---

## 4. Third-Party Library Bundling in WAR (`WEB-INF/lib/`)

A common question among developers is: "When deploying JTrac to a servlet container like Jetty or Tomcat, do I need to manually copy third-party JARs into the server's `lib/` directory?"

**Answer: Absolutely NOT!**

### WAR Package Architecture:
When you run `mvn package`, Maven automatically constructs a self-contained Web Application Archive: [`target/jtrac.war`](../../target/jtrac.war). Its internal structure includes:

```text
jtrac.war
├── META-INF/
│   └── MANIFEST.MF
├── WEB-INF/
│   ├── classes/                 <-- JTrac compiled classes and UTF-8 resource files
│   │   ├── info/jtrac/...
│   │   └── messages*.properties
│   ├── lib/                     <-- [ALL 53 third-party JARs are bundled here!]
│   │   ├── spring-2.5.6.jar
│   │   ├── wicket-1.3.7.jar
│   │   ├── hibernate-3.2.7.ga.jar
│   │   ├── stringtree.jar       <-- Bundled proprietary library
│   │   ├── hsqldb-2.7.2.jar
│   │   └── ... (all remaining dependency JARs)
│   └── web.xml                  <-- Servlet 3.1 configuration
└── resources/
```

### Deployment Key Points:
- **Classloader Isolation**: Servlet containers (e.g., Jetty 9.4, Jetty 12 `ee8`, Tomcat 9) automatically read and isolate libraries from `WEB-INF/lib/` for each deployed web application.
- **Clean Server Directory**: The server's own `lib/` folder must remain clean; **do not copy application JARs into the container's library folder**.
- **Effortless Deployment**: Simply place `target/jtrac.war` (or rename it to `ROOT.war`) inside the server's `webapps/` folder, and start the container.

---

## 5. Local Testing & Execution

After packaging, to run JTrac locally for interactive testing:

### Using Jetty:
1. Copy `target/jtrac.war` to `W:\developer\jtrac-2.3.3\webapps\ROOT.war`.
2. Run `W:\developer\jtrac-2.3.3\start.bat`.
3. Open your browser and navigate to: `http://localhost:8888` (Default administrator credentials: `admin` / `admin`).

---

## 6. Building & Running Standalone CLI HTML Exporter (jtrac-exporter)

The project includes a standalone CLI tool `jtrac-exporter` (with zero legacy framework dependencies) that exports JTrac database records directly into static, responsive HTML discussion threads with attachments via standard JDBC.

### 6.1 Build the Tool (Fat JAR)
Run this command from the project root directory (no need to switch directories):
```cmd
mvn clean package -f tools/jtrac-exporter/pom.xml -DskipTests
```
Upon successful build, the executable JAR is generated directly at:
`tools/jtrac-exporter.jar`

### 6.2 Execute Export (Command Mode)
From the project root, export from local embedded HSQLDB using relative paths:
```cmd
java -jar tools/jtrac-exporter.jar ^
  --db-url="jdbc:hsqldb:file:./data/db/jtrac;shutdown=true;readonly=true" ^
  --attachments-dir="./data/attachments" ^
  --out="./export-hsqldb" ^
  --lang=en
```

You can also connect to remote MySQL, PostgreSQL, or SQL Server databases:
```cmd
java -jar tools/jtrac-exporter.jar ^
  --db-url="jdbc:mysql://192.168.1.100:3306/jtrac?useUnicode=true&characterEncoding=UTF-8" ^
  --db-user="jtrac" ^
  --db-password="your_password" ^
  --attachments-dir="/path/to/attachments" ^
  --out="./export-mysql" ^
  --lang=en
```
To view all available CLI options, run: `java -jar tools/jtrac-exporter.jar --help`.

