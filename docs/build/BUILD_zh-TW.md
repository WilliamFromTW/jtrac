# JTrac 編譯與建置指南 (繁體中文)

[English](BUILD_en.md) | [繁體中文](BUILD_zh-TW.md) | [简体中文](BUILD_zh-CN.md) | [日本語](BUILD_ja.md) | [Tiếng Việt](BUILD_vi.md) | [Deutsch](BUILD_de.md) | [Español](BUILD_es.md) | [Français](BUILD_fr.md)

本指南詳細說明如何建置、編譯與打包 JTrac 2.3.3-2.0.0 專案，並深入解析 Maven 依賴管理、WAR 封裝結構、以及跨世代 Web 容器（Jetty 10/12、Tomcat 9/10/11）的部署方案。

---

## 1. 前置環境需求

在開始編譯之前，請確認您的本機開發環境符合以下條件：

- **作業系統**：Windows / Linux / macOS
- **Java 開發套件 (JDK)**：**JDK 11 或 JDK 17**（推薦使用 JDK 17，例如 `W:\developer\jdk-17.0.9` 或 JDK 11 `W:\developer\jdk-11.0.28`）
  > [!IMPORTANT]
  > 本現代化版本已升級至 Spring 5.3、Hibernate 5.6 與 Wicket 9，編譯目標為 Java 11。**JDK 8 已不再支援**，請勿使用 JDK 8 進行編譯。
- **Apache Maven**：Maven 3.9.x 以上版本（例如 `W:\developer\apache-maven-3.9.9`）

### Windows 本機環境載入範例
若您在 Windows 環境開發，可於命令提示字元 (CMD) 或 PowerShell 中載入環境變數：
```powershell
$env:JAVA_HOME = "W:\developer\jdk-17.0.9"
$env:PATH = "W:\developer\apache-maven-3.9.9\bin;$env:PATH"
```

驗證環境指令：
```bash
mvn -version
```
輸出應顯示正確的 Maven 與 Java 11/17 版本資訊。

---

## 2. 常用編譯與建置指令

請在 JTrac 專案根目錄（包含 `pom.xml` 的目錄）下執行以下指令：

| 指令 | 說明 |
|---|---|
| `mvn clean compile` | 清除舊快取並重新編譯 `src/main/java`，處理 resources 資源過濾 |
| `mvn test-compile` | 編譯 `src/test/java` 下的所有單元測試類別 |
| `mvn test` | 執行所有單元測試（使用 JUnit 5 與內建 HSQLDB，免外部資料庫） |
| `mvn package` | 執行測試並封裝為正式 Web 應用封裝檔（產出 `target/jtrac.war`） |
| `mvn package -DskipTests` | 跳過單元測試，快速打包生成 `target/jtrac.war` |
| `mvn clean` | 清除 `target/` 目錄下所有先前建置的編譯快取與暫存產物 |

---

## 3. Maven 依賴套件自動下載機制 (`~/.m2/repository`)

JTrac 是基於標準 Maven 架構開發，其所有的第三方依賴函式庫（包括 Spring 5.3、Apache Wicket 9、Hibernate 5.6、Spring Security 5.8 等）皆已宣告於 [`pom.xml`](../../pom.xml) 中。

### 自動下載與快取流程：
1. 當您首次執行 `mvn compile` 或 `mvn package` 時，Maven 會自動連線至遠端中央倉庫（Maven Central）。
2. 下載的所有相依套件皆存放於使用者本機快取目錄：
   - **Windows**：`%USERPROFILE%\.m2\repository\`
   - **Linux / macOS**：`~/.m2/repository/`
3. 後續無論進行多少次編譯或離線打包，Maven 皆會直接自本機 `.m2` 快取讀取套件，**開發者完全不需要手動搜尋、下載或配置任何第三方 JAR 檔案**！

---

## 4. 第三方函式庫封裝於 WAR 檔案機制 (`WEB-INF/lib/`)

### WAR 封裝結構解析：
當您執行 `mvn package` 打包後，Maven 會自動組裝出標準的 Java Web 應用封裝檔：[`target/jtrac.war`](../../target/jtrac.war)。其內部結構如下：

```text
jtrac.war
├── META-INF/
│   └── MANIFEST.MF
├── WEB-INF/
│   ├── classes/                 <-- JTrac 編譯後的 class 與 UTF-8 資源檔
│   │   ├── info/jtrac/...
│   │   └── messages*.properties
│   ├── lib/                     <-- 【核心所在：所有現代化第三方 JAR 都在這裡！】
│   │   ├── spring-core-5.3.37.jar
│   │   ├── wicket-core-9.16.0.jar
│   │   ├── hibernate-core-5.6.15.Final.jar
│   │   ├── spring-security-core-5.8.14.jar
│   │   ├── hsqldb-2.7.2.jar
│   │   └── ... (其餘所有依賴庫)
│   └── web.xml                  <-- Servlet 4.0 規範設定
└── resources/
```

- **獨立隔離性**：Servlet 容器（如 Jetty、Tomcat）啟動時會自動讀取並隔離每個 WAR 內部的 `WEB-INF/lib/`。
- **極簡部署**：伺服器本身無需手動放置任何第三方 JAR，只需部署 WAR 檔即可！

---

## 5. Web 容器相容性與部署矩陣 (Web Container Matrix)

JTrac 2.3.3-2.0.0 核心採用 Servlet 4.0 規範（`javax.servlet`），能完美相容主流現代 Web 容器：

### 容器部署對照表：

| Web 容器 | 版本支援 | 部署方式 |
|---|---|---|
| **Jetty 10.x** | 10.0.x（推薦首選） | **開箱即用**：直接將 `target/jtrac.war` 複製至 `webapps/ROOT.war` 即可啟動。 |
| **Jetty 12.x** | 12.0.x（最新版） | **原生支援**：啟用內建 `ee8` 模組：<br/>`java -jar start.jar --add-modules=server,http,ee8-deploy,ee8-webapp`，即可直接部署 `jtrac.war`。 |
| **Tomcat 9.x** | 9.0.x（推薦首選） | **開箱即用**：直接將 `target/jtrac.war` 複製至 `webapps/ROOT.war` 即可啟動。 |
| **Tomcat 10.x / 11.x** | 10.1.x / 11.0.x | **自動轉換支援**：<br/>1. **方式 A**：將 `jtrac.war` 放入 Tomcat 的 `webapps-javaee/` 目錄，容器啟動時會自動轉換運行。<br/>2. **方式 B**：使用 Tomcat 官方 `jakartaee-migration` 工具轉換為 `jtrac-jakarta.war` 後直接部署至 `webapps/`。 |

### 本地 Jetty 10 部署實例：
1. 將 `target/jtrac.war` 複製為 `W:\developer\jetty-10.0.26\webapps\ROOT.war`。
2. 啟動 Jetty：
   ```powershell
   & "W:\developer\jdk-17.0.9\bin\java.exe" -jar W:\developer\jetty-10.0.26\start.jar
   ```
3. 開啟瀏覽器訪問：`http://localhost:8888`（預設管理員帳號：`admin` / 密碼：`admin`）。

---

## 6. 資料庫升級與遷移指引

若您是由舊版（2.3.3-1.0.0 或更早版本）升級：
1. **使用外部關聯式資料庫 (MySQL / PostgreSQL / SQL Server / Oracle)**：
   - 請在資料庫中執行升級腳本：[`etc/sql/upgrade-to-2.0.0.sql`](../../etc/sql/upgrade-to-2.0.0.sql)。
   - 該腳本會寫入使用者與專案列表的預設分頁筆數配置（預設 25 筆）。
2. **使用內建 HSQLDB**：
   - 系統在啟動時會自動調用 `HsqldbDatabaseMigrator`，全自動將舊版 HSQLDB 1.8 備份並無痛轉換至 HSQLDB 2.x 結構，**完全無需手動操作**。

---

## 7. 獨立 CLI 討論串匯出工具建置與執行 (jtrac-exporter)

專案內建完全獨立的命令列工具 `jtrac-exporter`，可透過 JDBC 連線字串存取資料庫，將議題與附件匯出為響應式 HTML 報表。

### 7.1 編譯打包獨立工具 (Fat JAR)
在專案根目錄執行：
```cmd
mvn clean package -f tools/jtrac-exporter/pom.xml -DskipTests
```
產出位置：`tools/jtrac-exporter.jar`。

### 7.2 執行匯出
```cmd
java -jar tools/jtrac-exporter.jar ^
  --db-url="jdbc:hsqldb:file:./data/db/jtrac;shutdown=true;readonly=true" ^
  --attachments-dir="./data/attachments" ^
  --out="./export-output" ^
  --lang=zh-TW
```
