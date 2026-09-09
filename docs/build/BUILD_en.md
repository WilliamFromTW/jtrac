# JTrac Build & Compilation Guide (English)

[English](BUILD_en.md) | [繁體中文](BUILD_zh-TW.md) | [简体中文](BUILD_zh-CN.md) | [日本語](BUILD_ja.md) | [Tiếng Việt](BUILD_vi.md) | [Deutsch](BUILD_de.md) | [Español](BUILD_es.md) | [Français](BUILD_fr.md)

This guide provides comprehensive instructions on building, compiling, and packaging the JTrac 2.3.3-2.0.0 project, along with details on Maven dependency resolution, WAR structure, and modern Web Container deployment (Jetty 10/12, Tomcat 9/10/11).

---

## 1. Prerequisites

Before compiling, verify that your local environment meets the following requirements:

- **Operating System**: Windows / Linux / macOS
- **Java Development Kit (JDK)**: **JDK 11 or JDK 17** (JDK 17 recommended, e.g., `W:\developer\jdk-17.0.9` or JDK 11 `W:\developer\jdk-11.0.28`)
  > [!IMPORTANT]
  > JTrac has been modernized to Spring 5.3, Hibernate 5.6, and Wicket 9 with bytecode target Java 11. **JDK 8 is no longer supported**.
- **Apache Maven**: Maven 3.9.x or later (e.g., `W:\developer\apache-maven-3.9.9`)

### Windows Environment Setup
In CMD or PowerShell, set your environment variables:
```powershell
$env:JAVA_HOME = "W:\developer\jdk-17.0.9"
$env:PATH = "W:\developer\apache-maven-3.9.9\bin;$env:PATH"
```

Verify setup:
```bash
mvn -version
```

---

## 2. Common Build Commands

Run the following commands from the root repository directory containing `pom.xml`:

| Command | Description |
|---|---|
| `mvn clean compile` | Clean previous caches and compile `src/main/java` with resource filtering |
| `mvn test-compile` | Compile unit test classes in `src/test/java` |
| `mvn test` | Run all unit tests (JUnit 5 + embedded HSQLDB, no external DB required) |
| `mvn package` | Run tests and package into production WAR (`target/jtrac.war`) |
| `mvn package -DskipTests` | Fast packaging without running unit tests |
| `mvn clean` | Clean all build artifacts in `target/` |

---

## 3. Maven Dependency Caching (`~/.m2/repository`)

All third-party libraries (Spring 5.3, Wicket 9, Hibernate 5.6, Spring Security 5.8) are declared in [`pom.xml`](../../pom.xml).

1. On initial build, Maven downloads all artifacts from Maven Central.
2. Artifacts are cached locally at `%USERPROFILE%\.m2\repository\` (Windows) or `~/.m2/repository/` (Linux/macOS).
3. Subsequent builds reuse the local cache completely offline.

---

## 4. WAR Package Structure (`WEB-INF/lib/`)

Running `mvn package` produces the production-ready archive [`target/jtrac.war`](../../target/jtrac.war):

```text
jtrac.war
├── META-INF/
│   └── MANIFEST.MF
├── WEB-INF/
│   ├── classes/                 <-- Compiled classes and UTF-8 properties
│   │   ├── info/jtrac/...
│   │   └── messages*.properties
│   ├── lib/                     <-- All modern third-party libraries
│   │   ├── spring-core-5.3.37.jar
│   │   ├── wicket-core-9.16.0.jar
│   │   ├── hibernate-core-5.6.15.Final.jar
│   │   ├── spring-security-core-5.8.14.jar
│   │   ├── hsqldb-2.7.2.jar
│   │   └── ...
│   └── web.xml                  <-- Servlet 4.0 configuration
└── resources/
```

- Servlet containers isolate `WEB-INF/lib/` automatically per webapp.
- The host container `lib/` directory must remain untouched.

---

## 5. Web Container Matrix & Deployment

JTrac 2.3.3-2.0.0 uses Servlet 4.0 specifications (`javax.servlet`) and is compatible with modern web containers:

| Container | Supported Version | Deployment Method |
|---|---|---|
| **Jetty 10.x** | 10.0.x (Recommended) | **Out-of-the-box**: Copy `target/jtrac.war` to `webapps/ROOT.war`. |
| **Jetty 12.x** | 12.0.x (Latest) | **Native**: Enable the `ee8` module:<br/>`java -jar start.jar --add-modules=server,http,ee8-deploy,ee8-webapp`. |
| **Tomcat 9.x** | 9.0.x (Recommended) | **Out-of-the-box**: Copy `target/jtrac.war` to `webapps/ROOT.war`. |
| **Tomcat 10.x / 11.x** | 10.1.x / 11.0.x | **Auto-migration**: Place `jtrac.war` into `webapps-javaee/`, or convert using `jakartaee-migration`. |

### Local Jetty 10 Example:
1. Copy `target/jtrac.war` to `W:\developer\jetty-10.0.26\webapps\ROOT.war`.
2. Start Jetty:
   ```powershell
   & "W:\developer\jdk-17.0.9\bin\java.exe" -jar W:\developer\jetty-10.0.26\start.jar
   ```
3. Open browser: `http://localhost:8888` (default: `admin` / `admin`).

### 5.1 Data Directory (`jtrac.home`) Resolution Priority & Container Configuration

JTrac's primary data storage directory is governed by the `jtrac.home` system variable. In [`JtracConfigurer`](../../src/main/java/info/jtrac/config/JtracConfigurer.java), JTrac checks the following 4-tier resolution sequence:

1. **Priority 1**: `jtrac.home` configured in `WEB-INF/classes/jtrac-init.properties`.
2. **Priority 2 (Recommended for Production & Containers)**: JVM system property `-Djtrac.home=...`.
3. **Priority 3**: Servlet Context init-parameter `jtrac.home` (in `web.xml` or Tomcat context XML).
4. **Priority 4 (Default Fallback)**: `System.getProperty("user.home") + "/.jtrac"`.
   - **Why Tomcat Defaults to `/root/.jtrac`**: When Tomcat runs under Linux as the `root` user and Priorities 1–3 are not defined, JTrac automatically defaults to `/root/.jtrac`. If run under standard user `jtrac`, it defaults to `/home/jtrac/.jtrac`.
   - **Local Jetty Development**: `start-jtrac.bat` specifies `-Djtrac.home=data`, binding storage to `W:\developer\jetty-10.0.26\data\`.

#### Standard Structure of `jtrac.home`:
- `jtrac.properties`: Database connection URL, credentials, and Hibernate dialect.
- `db/`: Embedded HSQLDB files (`jtrac.script`, `jtrac.data`, etc.).
- `attachments/`: Physical attachments partitioned by Space ID (`attachments/{spaceId}/`).
- `indexes/`: Lucene full-text search indexes.
- `backups/`: Emergency safety snapshots created before system restores.
- `logs/`: Application runtime logs (`jtrac.log`).

#### Customizing `jtrac.home` in Containers:
- **Linux Tomcat (`bin/setenv.sh`)**:
  ```bash
  export CATALINA_OPTS="$CATALINA_OPTS -Djtrac.home=/var/jtrac-data"
  ```
- **Windows Tomcat (`bin/setenv.bat`)**:
  ```cmd
  set "CATALINA_OPTS=%CATALINA_OPTS% -Djtrac.home=D:/jtrac-data"
  ```
- **Jetty / Standalone JVM**:
  ```bash
  java -Djtrac.home=/var/jtrac-data -jar start.jar
  ```

---

## 6. Database & Storage Migration (Upgrading from 2.3.3-1.0.0)

- **External Databases (MySQL, PostgreSQL, SQL Server, Oracle)**: Run [`etc/sql/upgrade-to-2.0.0.sql`](../../etc/sql/upgrade-to-2.0.0.sql) to add pagination and index parameters.
- **Embedded HSQLDB**: `HsqldbDatabaseMigrator` automatically backs up and upgrades HSQLDB 1.8 to 2.x on server startup with zero manual configuration.
- **Attachment Storage Migration**: `AttachmentStorageMigrator` automatically partitions legacy flat attachments into pure numeric space directories (`attachments/{spaceId}/`), isolates unmapped files into `attachments/0_ORPHAN/`, and writes a `.attachment_migrated` marker.
- **Lucene Full-Text Search**: Built-in text extraction for `.xlsx`, `.docx` (pure JDK streaming OpenXML parser), `.pdf` (Apache PDFBox 2.0.31), `.txt`, `.csv`, `.md`, `.log` with `SmartCharsetDetector`.

---

## 7. Standalone CLI Exporter (`jtrac-exporter`)

### 7.1 Build Fat JAR
```cmd
mvn clean package -f tools/jtrac-exporter/pom.xml -DskipTests
```
Output: `tools/jtrac-exporter.jar`.

### 7.2 Run Export
```cmd
java -jar tools/jtrac-exporter.jar ^
  --db-url="jdbc:hsqldb:file:./data/db/jtrac;shutdown=true;readonly=true" ^
  --attachments-dir="./data/attachments" ^
  --out="./export-output" ^
  --lang=en
```

---

## 8. Native Docker Container Build & Deployment (Eclipse Temurin 17 + Jetty 12)

JTrac provides a multi-stage Docker build environment that requires no local JDK or Maven installation:

### 8.1 Native Docker Commands (Recommended)
Navigate to the `docker/` directory and build using the repository root (`..`) as the build context:
```bash
cd docker
docker build -f Dockerfile -t jtrac:latest ..
docker run -d -p 8888:8080 -v jtrac_data:/jtrac-data --name jtrac jtrac:latest
```

### 8.2 Cross-Platform Helper Scripts & Docker Compose
- **Windows**: Run `build.bat` and `run.bat` in `docker/`
- **Linux / macOS**: Run `./build.sh` and `./run.sh` in `docker/`
- **Docker Compose**: Run `docker compose up -d` in `docker/`

Once the container is running, open `http://localhost:8888/` in your browser (default credentials: `admin` / `admin`). For custom database connections and JVM options, refer to [`docker/README.md`](../../docker/README.md).

