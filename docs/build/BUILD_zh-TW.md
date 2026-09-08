# JTrac 編譯與建置指南 (繁體中文)

本指南詳細說明如何建置、編譯與打包 JTrac 專案，並深入解析 Maven 依賴管理與 WAR 封裝機制。

---

## 1. 前置環境需求

在開始編譯之前，請確認您的本機開發環境符合以下條件：

- **作業系統**：Windows / Linux / macOS
- **Java 開發套件 (JDK)**：JDK 8 或 JDK 11（推薦使用 JDK 11，例如 `W:\developer\jdk-11.0.25.9-hotspot`）
- **Apache Maven**：Maven 3.9.x 以上版本（例如 `W:\developer\apache-maven-3.9.9`）

### Windows 本機環境載入
若您在 Windows 環境開發，請先於命令提示字元 (CMD) 中載入環境變數設定批次檔：
```cmd
call W:\developer\maven.bat
```
該腳本會自動將 Maven 與 JDK 11 加入當前終端連線的 `PATH` 與 `JAVA_HOME` 中。

驗證環境指令：
```cmd
mvn -version
```
輸出應顯示正確的 Maven 與 Java 11 版本資訊。

---

## 2. 常用編譯與建置指令

請在 JTrac 專案根目錄（包含 `pom.xml` 的目錄）下執行以下指令：

| 指令 | 說明 |
|---|---|
| `mvn compile` | 編譯 `src/main/java` 下的 137 個 Java 原始檔，並處理 resources 資源過濾 |
| `mvn test-compile` | 編譯 `src/test/java` 下的所有單元測試類別 |
| `mvn test` | 執行所有單元測試（使用內建記憶體型 HSQLDB，免外部資料庫） |
| `mvn package` | 執行測試並封裝為正式 Web 應用封裝檔（產出 `target/jtrac.war`） |
| `mvn package -DskipTests` | 跳過單元測試，快速打包生成 `target/jtrac.war` |
| `mvn clean` | 清除 `target/` 目錄下所有先前建置的編譯快取與暫存產物 |
| `mvn clean compile` | 清除舊產物並重新完整編譯 |

---

## 3. Maven 依賴套件自動下載機制 (`~/.m2/repository`)

JTrac 是基於標準 Maven 架構開發，其所有的第三方依賴函式庫（包括 Spring Framework、Apache Wicket、Hibernate、Acegi Security、Lucene 等）皆已宣告於 [`pom.xml`](../../pom.xml) 中。

### 自動下載與快取流程：
1. 當您首次執行 `mvn compile` 或 `mvn package` 時，Maven 會自動連線至遠端中央倉庫（Maven Central）。
2. Maven 會自動下載專案宣告的所有相依套件至使用者本機快取目錄：
   - **Windows**：`%USERPROFILE%\.m2\repository\`（例如 `C:\Users\username\.m2\repository\`）
   - **Linux / macOS**：`~/.m2/repository/`
3. 後續無論進行多少次編譯或離線打包，Maven 皆會直接自本機 `.m2` 快取讀取套件，**開發者完全不需要手動搜尋、下載或配置任何第三方 JAR 檔案**！

---

## 4. 第三方函式庫封裝於 WAR 檔案機制 (`WEB-INF/lib/`)

許多開發者常會詢問：「將 JTrac 部署到 Jetty 或 Tomcat 伺服器時，是否需要手動複製第三方 JAR 到伺服器的 `lib/` 目錄？」

**答案是：完全不需要！**

### WAR 封裝結構解析：
當您執行 `mvn package` 打包後，Maven 會自動組裝出標準的 Java Web 應用封裝檔：[`target/jtrac.war`](../../target/jtrac.war)。其內部結構如下：

```text
jtrac.war
├── META-INF/
│   └── MANIFEST.MF
├── WEB-INF/
│   ├── classes/                 <-- JTrac 自身編譯後的 class 與 UTF-8 資源檔
│   │   ├── info/jtrac/...
│   │   └── messages*.properties
│   ├── lib/                     <-- 【核心所在：全數 53 個第三方 JAR 都在這裡！】
│   │   ├── spring-2.5.6.jar
│   │   ├── wicket-1.3.7.jar
│   │   ├── hibernate-3.2.7.ga.jar
│   │   ├── stringtree.jar       <-- 專案自帶之專用套件亦自動打包於此
│   │   ├── hsqldb-2.7.2.jar
│   │   └── ... (其餘所有依賴庫)
│   └── web.xml                  <-- Servlet 3.1 規範設定
└── resources/
```

### 部署注意事項：
- **獨立隔離性**：Servlet 容器（如 Jetty 9.4、Jetty 12、Tomcat 9）在啟動時，會自動讀取並隔離每個 WAR 檔內部的 `WEB-INF/lib/`。
- **伺服器端純淨**：因此，伺服器本身的 `lib/` 目錄請保持乾淨，**切勿手動把第三方 JAR 拷貝進去**。
- **極簡部署**：只要將 `target/jtrac.war` 複製至伺服器的 `webapps/` 目錄（或更名為 `ROOT.war`），伺服器即可直接啟動運行！

---

## 5. 本機測試環境運行

打包完成後，若要在本機啟動 JTrac 進行可視化測試：

### 使用 Jetty 運行：
1. 將 `target/jtrac.war` 複製為 `W:\developer\jtrac-2.3.3\webapps\ROOT.war`。
2. 執行 `W:\developer\jtrac-2.3.3\start.bat`。
3. 開啟瀏覽器訪問：`http://localhost:8888`（預設管理員帳號密碼：`admin` / `admin`）。

---

## 6. 獨立 CLI 討論串匯出工具建置與執行 (jtrac-exporter)

專案內建一個完全獨立、零舊版框架依賴的命令列工具 `jtrac-exporter`，可直接透過 JDBC 連線字串存取本地或遠端資料庫，並將議題、歷史討論串與附件匯出為多語系靜態 HTML 報表。

### 6.1 編譯打包獨立工具 (Fat JAR)
在專案根目錄執行以下指令（免切換目錄）：
```cmd
mvn clean package -f tools/jtrac-exporter/pom.xml -DskipTests
```
編譯成功後，可執行檔會直接輸出至：
`tools/jtrac-exporter.jar`

### 6.2 執行匯出 (Command Mode)
在專案根目錄下，可使用相對路徑直接連線本地 HSQLDB 資料庫進行匯出：
```cmd
java -jar tools/jtrac-exporter.jar ^
  --db-url="jdbc:hsqldb:file:./data/db/jtrac;shutdown=true;readonly=true" ^
  --attachments-dir="./data/attachments" ^
  --out="./export-hsqldb" ^
  --lang=zh-TW
```

亦支援連線遠端 MySQL 或 PostgreSQL 自訂資料庫：
```cmd
java -jar tools/jtrac-exporter.jar ^
  --db-url="jdbc:mysql://192.168.1.100:3306/jtrac?useUnicode=true&characterEncoding=UTF-8" ^
  --db-user="jtrac" ^
  --db-password="your_password" ^
  --attachments-dir="/path/to/attachments" ^
  --out="./export-mysql" ^
  --lang=zh-TW
```
查看完整參數說明：`java -jar tools/jtrac-exporter.jar --help`。

