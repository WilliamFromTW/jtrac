# JTrac 编译与构建指南 (简体中文)

本指南详细说明如何构建、编译与打包 JTrac 项目，并深入解析 Maven 依赖管理与 WAR 封包机制。

---

## 1. 前置环境要求

在开始编译之前，请确认您的本地开发环境符合以下条件：

- **操作系统**：Windows / Linux / macOS
- **Java 开发工具包 (JDK)**：JDK 8 或 JDK 11（推荐使用 JDK 11，例如 `W:\developer\jdk-11.0.25.9-hotspot`）
- **Apache Maven**：Maven 3.9.x 以上版本（例如 `W:\developer\apache-maven-3.9.9`）

### Windows 本地环境配置
若您在 Windows 环境开发，请先在命令提示符 (CMD) 中运行环境配置批处理脚本：
```cmd
call W:\developer\maven.bat
```
该脚本会自动将 Maven 与 JDK 11 添加到当前终端会话的 `PATH` 与 `JAVA_HOME` 中。

验证环境命令：
```cmd
mvn -version
```
输出应显示正确的 Maven 与 Java 11 版本信息。

---

## 2. 常用编译与构建命令

请在 JTrac 项目根目录（包含 `pom.xml` 的目录）下执行以下命令：

| 命令 | 说明 |
|---|---|
| `mvn compile` | 编译 `src/main/java` 下的 137 个 Java 源文件，并处理 resources 资源过滤 |
| `mvn test-compile` | 编译 `src/test/java` 下的所有单元测试类 |
| `mvn test` | 运行所有单元测试（使用内置内存型 HSQLDB，无需外部数据库） |
| `mvn package` | 运行测试并打包为标准 Web 应用包（生成 `target/jtrac.war`） |
| `mvn package -DskipTests` | 跳过单元测试，快速打包生成 `target/jtrac.war` |
| `mvn clean` | 清理 `target/` 目录下所有先前构建的编译缓存与临时产物 |
| `mvn clean compile` | 清除旧产物并重新完整编译 |

---

## 3. Maven 依赖自动下载机制 (`~/.m2/repository`)

JTrac 基于标准 Maven 架构开发，其所有的第三方依赖库（包括 Spring Framework、Apache Wicket、Hibernate、Acegi Security、Lucene 等）均已声明于 [`pom.xml`](../../pom.xml) 中。

### 自动下载与缓存机制：
1. 当您首次执行 `mvn compile` 或 `mvn package` 时，Maven 会自动连接到远程中央仓库（Maven Central）。
2. Maven 会自动下载项目声明的所有依赖包到用户本地缓存目录：
   - **Windows**：`%USERPROFILE%\.m2\repository\`（例如 `C:\Users\username\.m2\repository\`）
   - **Linux / macOS**：`~/.m2/repository/`
3. 后续无论进行多少次编译或离线打包，Maven 都会直接从本地 `.m2` 缓存读取依赖库，**开发者完全不需要手动搜索、下载或配置任何第三方 JAR 文件**！

---

## 4. 第三方库封装于 WAR 文件机制 (`WEB-INF/lib/`)

许多开发者常会询问：“将 JTrac 部署到 Jetty 或 Tomcat 服务器时，是否需要手动复制第三方 JAR 到服务器的 `lib/` 目录？”

**答案是：完全不需要！**

### WAR 包结构解析：
当您执行 `mvn package` 打包后，Maven 会自动组装出标准的 Java Web 应用封装包：[`target/jtrac.war`](../../target/jtrac.war)。其内部结构如下：

```text
jtrac.war
├── META-INF/
│   └── MANIFEST.MF
├── WEB-INF/
│   ├── classes/                 <-- JTrac 自身编译后的 class 与 UTF-8 资源文件
│   │   ├── info/jtrac/...
│   │   └── messages*.properties
│   ├── lib/                     <-- 【核心所在：全部 53 个第三方 JAR 都在这里！】
│   │   ├── spring-2.5.6.jar
│   │   ├── wicket-1.3.7.jar
│   │   ├── hibernate-3.2.7.ga.jar
│   │   ├── stringtree.jar       <-- 项目自带之专用依赖亦自动打包于此
│   │   ├── hsqldb-2.7.2.jar
│   │   └── ... (其余所有依赖库)
│   └── web.xml                  <-- Servlet 3.1 规范配置
└── resources/
```

### 部署注意事项：
- **独立隔离性**：Servlet 容器（如 Jetty 9.4、Jetty 12、Tomcat 9）在启动时，会自动读取并隔离每个 WAR 包内部的 `WEB-INF/lib/`。
- **服务器端纯净**：因此，服务器本身的 `lib/` 目录请保持干净，**切勿手动将第三方 JAR 复制进去**。
- **极简部署**：只需将 `target/jtrac.war` 复制到服务器的 `webapps/` 目录（或更名为 `ROOT.war`），服务器即可直接启动运行！

---

## 5. 本地测试环境运行

打包完成后，若要在本地启动 JTrac 进行可视化测试：

### 使用 Jetty 运行：
1. 将 `target/jtrac.war` 复制为 `W:\developer\jtrac-2.3.3\webapps\ROOT.war`。
2. 运行 `W:\developer\jtrac-2.3.3\start.bat`。
3. 打开浏览器访问：`http://localhost:8888`（默认管理员账号密码：`admin` / `admin`）。
