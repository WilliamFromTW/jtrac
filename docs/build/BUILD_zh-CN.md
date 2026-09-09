# JTrac 编译与构建指南 (简体中文)

[English](BUILD_en.md) | [繁體中文](BUILD_zh-TW.md) | [简体中文](BUILD_zh-CN.md) | [日本語](BUILD_ja.md) | [Tiếng Việt](BUILD_vi.md) | [Deutsch](BUILD_de.md) | [Español](BUILD_es.md) | [Français](BUILD_fr.md)

本指南详细说明如何构建、编译与打包 JTrac 2.3.3-2.0.0 项目，并解析 Maven 依赖管理、WAR 封装结构以及跨世代 Web 容器（Jetty 10/12、Tomcat 9/10/11）的部署方案。

---

## 1. 前置环境需求

在开始编译之前，请确认您的本地开发环境符合以下条件：

- **操作系统**：Windows / Linux / macOS
- **Java 开发套件 (JDK)**：**JDK 11 或 JDK 17**（推荐使用 JDK 17，例如 `W:\developer\jdk-17.0.9` 或 JDK 11 `W:\developer\jdk-11.0.28`）
  > [!IMPORTANT]
  > 本现代化版本已升级至 Spring 5.3、Hibernate 5.6 与 Wicket 9，编译目标为 Java 11。**JDK 8 已不再支持**，请勿使用 JDK 8 进行编译。
- **Apache Maven**：Maven 3.9.x 以上版本（例如 `W:\developer\apache-maven-3.9.9`）

### Windows 本地环境配置示例
在 CMD 或 PowerShell 中加载环境变量：
```powershell
$env:JAVA_HOME = "W:\developer\jdk-17.0.9"
$env:PATH = "W:\developer\apache-maven-3.9.9\bin;$env:PATH"
```

验证环境命令：
```bash
mvn -version
```

---

## 2. 常用编译与构建命令

请在 JTrac 根目录下执行以下命令：

| 命令 | 说明 |
|---|---|
| `mvn clean compile` | 清理旧缓存并重新编译 `src/main/java`，处理 resources 资源过滤 |
| `mvn test-compile` | 编译 `src/test/java` 下的单体测试类 |
| `mvn test` | 执行所有单元测试（使用 JUnit 5 与内置 HSQLDB，免外部数据库） |
| `mvn package` | 执行测试并打包为正式 Web 应用程序包（产出 `target/jtrac.war`） |
| `mvn package -DskipTests` | 跳过单元测试，快速打包生成 `target/jtrac.war` |
| `mvn clean` | 清理 `target/` 目录下的编译缓存与产物 |

---

## 3. Maven 依赖套件自动下载机制 (`~/.m2/repository`)

JTrac 基于标准 Maven 架构开发，所有第三方依赖已在 [`pom.xml`](../../pom.xml) 中声明。
首次执行构建时，Maven 会自动从中央仓库下载依赖并缓存在本地（Windows: `%USERPROFILE%\.m2\repository\`，Linux/macOS: `~/.m2/repository/`）。开发者无需手动拷贝 JAR 文件。

---

## 4. 第三方库封装于 WAR 文件机制 (`WEB-INF/lib/`)

打包生成的 [`target/jtrac.war`](../../target/jtrac.war) 结构如下：

```text
jtrac.war
├── META-INF/
│   └── MANIFEST.MF
├── WEB-INF/
│   ├── classes/                 <-- 编译后的 class 与 UTF-8 资源文件
│   │   ├── info/jtrac/...
│   │   └── messages*.properties
│   ├── lib/                     <-- 现代第三方依赖库
│   │   ├── spring-core-5.3.37.jar
│   │   ├── wicket-core-9.16.0.jar
│   │   ├── hibernate-core-5.6.15.Final.jar
│   │   ├── spring-security-core-5.8.14.jar
│   │   ├── hsqldb-2.7.2.jar
│   │   └── ...
│   └── web.xml                  <-- Servlet 4.0 规范配置
└── resources/
```

- Servlet 容器会自动隔离每个 WAR 包的 `WEB-INF/lib/`。
- 宿主容器本身的 `lib/` 目录应保持干净，无需放入任何第三方 JAR。

---

## 5. Web 容器兼容性与部署矩阵 (Web Container Matrix)

JTrac 2.3.3-2.0.0 采用 Servlet 4.0 规范（`javax.servlet`），兼容主流现代 Web 容器：

| Web 容器 | 版本支持 | 部署方式 |
|---|---|---|
| **Jetty 10.x** | 10.0.x（推荐首选） | **开箱即用**：直接将 `target/jtrac.war` 复制至 `webapps/ROOT.war` 即可启动。 |
| **Jetty 12.x** | 12.0.x（最新版） | **原生支持**：启用内置 `ee8` 模块：<br/>`java -jar start.jar --add-modules=server,http,ee8-deploy,ee8-webapp`，即可直接部署 `jtrac.war`。 |
| **Tomcat 9.x** | 9.0.x（推荐首选） | **开箱即用**：直接将 `target/jtrac.war` 复制至 `webapps/ROOT.war` 即可启动。 |
| **Tomcat 10.x / 11.x** | 10.1.x / 11.0.x | **自动转换支持**：<br/>1. **方式 A**：将 `jtrac.war` 放入 Tomcat 的 `webapps-javaee/` 目录，容器启动时自动转换运行。<br/>2. **方式 B**：使用官方 `jakartaee-migration` 工具转换为 `jtrac-jakarta.war` 后部署至 `webapps/`。 |

---

## 6. 数据库升级与迁移指引

若从 2.3.3-1.0.0 升级：
1. **外部数据库 (MySQL / PostgreSQL / SQL Server / Oracle)**：
   - 执行升级脚本：[`etc/sql/upgrade-to-2.0.0.sql`](../../etc/sql/upgrade-to-2.0.0.sql)，注入默认分页大小参数。
2. **内置 HSQLDB**：
   - 系统启动时由 `HsqldbDatabaseMigrator` 自动备份并无缝迁移至 HSQLDB 2.x，无需手动干预。
3. **附件存储目录自动迁移**：
   - 服务器启动时由 `AttachmentStorageMigrator` 自动将平铺历史附件迁移至纯项目 ID 目录（`${jtrac.home}/attachments/{spaceId}/`），无关联孤儿文件隔离至 `attachments/0_ORPHAN/`。
4. **Lucene 附件全文检索**：
   - 支持 `.xlsx`、`.docx`（纯 JDK 流式 OpenXML 解析器）、`.pdf`（Apache PDFBox 2.0.31）、`.txt`、`.csv`、`.md`、`.log`，整合 `SmartCharsetDetector` 防止中文乱码，并内置 10MB 与 50,000 字符防护限制。

---

## 7. 独立 CLI HTML 导出工具 (`jtrac-exporter`)

```cmd
# 构建 Fat JAR
mvn clean package -f tools/jtrac-exporter/pom.xml -DskipTests
# 产出: tools/jtrac-exporter.jar

# 执行导出
java -jar tools/jtrac-exporter.jar ^
  --db-url="jdbc:hsqldb:file:./data/db/jtrac;shutdown=true;readonly=true" ^
  --attachments-dir="./data/attachments" ^
  --out="./export-output" ^
  --lang=zh-CN
```
