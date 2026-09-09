# JTrac Docker Packaging & Deployment (Jetty 12 + Eclipse Temurin 17)

[English](README.md) | [繁體中文](README_zh-TW.md)

This directory provides native multi-stage Docker packaging and containerized runtime deployment environments for modernized JTrac.

---

## Features

- **Modernized Runtime Environment**: Based on official `jetty:12-jre17-eclipse-temurin` with `--add-modules=ee8-deploy,ee8-webapp`, natively supporting Servlet 4.0 (`javax.servlet`).
- **Multi-stage Build**: Automatically compiles `jtrac.war` from source using `maven:3.9-eclipse-temurin-17` with `-DskipTests`, requiring no local JDK or Maven installation.
- **Full Multilingual Font Support**: Pre-installed `fontconfig`, `fonts-noto-cjk` (CJK characters), `fonts-noto-core` (Vietnamese and diacritics), and `fonts-dejavu-core` (European accents), preventing missing glyphs (tofu) and garbled characters during PDF/Office full-text indexing and report generation.
- **Dynamic Volume Permission Fix & Secure Step-down**: The entrypoint runs as root on boot to automatically repair `/jtrac-data` ownership to `jetty:jetty` (UID 999), and then steps down using `gosu` to run Jetty securely without manual host `chown`.
- **Dynamic Database Configuration**: Supports environment variables (`DATABASE_URL`, `DATABASE_DRIVER`, etc.) to automatically configure `/jtrac-data/jtrac.properties`. Defaults to embedded HSQLDB 2.x when unset.

---

## Quick Start

### Method 1: Native Docker Command (Recommended)

From the `docker/` directory, build the image using the project root (`..`) as the build context:

```bash
cd docker
docker build -f Dockerfile -t jtrac:latest ..
docker run -d -p 8888:8080 -v jtrac_data:/jtrac-data --name jtrac jtrac:latest
```

Open `http://localhost:8888/` in your browser (default credentials: `admin` / `admin`).

---

### Method 2: Cross-Platform Helper Scripts

Convenient helper scripts are provided in this directory:

- **Windows**:
  ```cmd
  cd docker
  build.bat
  run.bat
  ```

- **Linux / macOS**:
  ```bash
  cd docker
  chmod +x *.sh
  ./build.sh
  ./run.sh
  ```

---

### Method 3: Docker Compose

```bash
cd docker
docker compose up -d
```

Stop the container:
```bash
docker compose down
```

---

### Method 4: Run Pre-built Image from Docker Hub

You can also run the official pre-built image directly from Docker Hub:
**[https://hub.docker.com/r/inmethod/jtrac](https://hub.docker.com/r/inmethod/jtrac)**

```bash
docker run -d -p 8888:8080 -v jtrac_data:/jtrac-data --name jtrac inmethod/jtrac:tag
```

---

## Data Persistence

All system configurations, database files, and uploaded attachments are persisted in `/jtrac-data`:

| Path | Description |
|---|---|
| `/jtrac-data/jtrac.properties` | Database connection driver and parameters |
| `/jtrac-data/db/` | Embedded HSQLDB 2.x database files |
| `/jtrac-data/attachments/` | Issue attachments partitioned by space ID |
| `/jtrac-data/indexes/` | Lucene full-text search index repository |
| `/jtrac-data/backups/` | Full system backup snapshots |

### Host Directory Mount Example:
```bash
docker run -d \
  -p 8888:8080 \
  -v /path/to/host/data:/jtrac-data \
  --name jtrac \
  jtrac:latest
```
*(The entrypoint automatically fixes directory ownership on boot).*

---

## External Database Connection via Environment Variables

To connect to an external relational database (MySQL, PostgreSQL, Oracle, etc.), supply environment variables during container startup:

| Environment Variable | Description | Example (MySQL) |
|---|---|---|
| `DATABASE_URL` | JDBC connection URL | `jdbc:mysql://db:3306/jtrac?useUnicode=true&characterEncoding=UTF-8` |
| `DATABASE_DRIVER` | JDBC driver class name | `com.mysql.cj.jdbc.Driver` |
| `DATABASE_USERNAME` | Database username | `jtrac` |
| `DATABASE_PASSWORD` | Database password | `secret123` |
| `HIBERNATE_DIALECT` | (Optional) Hibernate dialect | `org.hibernate.dialect.MySQL8Dialect` |

### Example with External MySQL:
```bash
docker run -d \
  -p 8888:8080 \
  -v jtrac_data:/jtrac-data \
  -e DATABASE_URL="jdbc:mysql://192.168.1.50:3306/jtrac?useUnicode=true&characterEncoding=UTF-8" \
  -e DATABASE_DRIVER="com.mysql.cj.jdbc.Driver" \
  -e DATABASE_USERNAME="jtrac" \
  -e DATABASE_PASSWORD="password" \
  -e HIBERNATE_DIALECT="org.hibernate.dialect.MySQL8Dialect" \
  --name jtrac \
  jtrac:latest
```

---

## Custom JVM Options

Default JVM parameters:
`-Djtrac.home=/jtrac-data -Dfile.encoding=UTF-8 -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=25.0`

Override by passing the `JAVA_OPTIONS` environment variable:
```bash
docker run -d \
  -p 8888:8080 \
  -v jtrac_data:/jtrac-data \
  -e JAVA_OPTIONS="-Xms1g -Xmx2g -Djtrac.home=/jtrac-data -Dfile.encoding=UTF-8" \
  --name jtrac \
  jtrac:latest
```
