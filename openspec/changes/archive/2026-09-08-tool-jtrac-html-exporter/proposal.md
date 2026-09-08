# 變更提案：獨立命令列 JTrac HTML 討論串匯出工具 (tool-jtrac-html-exporter)

## 變更背景與動機 (Why)

JTrac 為經典之議題追蹤系統，其核心採用 Java 1.8、Spring 2.5、Apache Wicket 1.3 與 Hibernate 3 等歷史架構。當使用者需要對現有專案資料庫進行封存、資料備份、靜態查閱、歷史稽核或向無網路環境匯出紀錄時，過去往往受限於必須配置完整的 Servlet 容器 (如 Jetty/Tomcat) 與依賴環境。

為此，本提案規劃建立一個**完全獨立、零舊版依賴**的命令列工具 `jtrac-exporter.jar`，讓維運與開發人員僅需透過標準 Java 環境，指定 JDBC 連線字串，即可直接連線至遠端或本地之 JTrac 資料庫（支援 MySQL、PostgreSQL、HSQLDB、SQL Server），將所有的專案空間 (Spaces)、議題 (Items)、後續留言討論串 (History) 以及附加檔案 (Attachments) 完整匯出為高品質、現代化、自包含且支援 5 國語言的多語系靜態 HTML 討論串報表。

## 變更範疇與架構 (What)

1. **獨立模組與零舊版依賴 (Zero Legacy Dependencies)**：
   - 於 `tools/jtrac-exporter/` 建立專屬模組，與主系統 WAR 建置環境完全隔離。
   - **絕不依賴** Spring、Wicket、Hibernate、Acegi 等舊框架，純使用 Java 標準庫 (`java.sql.*` 原生 JDBC API)。
   - 打包現代版官方資料庫驅動 (MySQL Connector/J 8.3、PostgreSQL 42.7、HSQLDB 2.7.2、MS SQL Server 12.6)，透過 `maven-shade-plugin` 產出隨處可執行的 Fat JAR。

2. **以「指定 JDBC 連線字串」為核心連線機制 (Mode 2: JDBC Connection String)**：
   - 核心 CLI 參數為 `--db-url`、`--db-user`、`--db-password`、`--db-driver`，可靈活連線任何遠端或本地資料庫實例。
   - HSQLDB 預設帳號為 `sa`、密碼為空，連線 HSQLDB 免手動輸入帳密。
   - 支援 `--attachments-dir` 指定附件實體目錄，並內建完整防呆容錯機制（無附件目錄或檔案缺失時自動標註，不中斷匯出）。

3. **JTrac 討論串邏輯版型與多語系 (Discussion Thread HTML & i18n)**：
   - 遵循 JTrac 經典呈現邏輯：一個 Issue 即為一個獨立 `<section>` 卡片。
   - 該 Issue 的所有後續追蹤記錄與留言 (History) 依時間軸完整收納於同一 Section 內。
   - 圖片附件提供內嵌縮圖預覽，其他附件提供直接下載連結與檔案資訊。
   - 提供 Space 索引頁 (`index.html`) 與專案空間討論串頁面 (`<SPACE>.html`)。
   - 介面文字支援 5 國語言：繁體中文 (`zh-TW`)、English (`en`)、简体中文 (`zh-CN`)、日本語 (`ja`)、Tiếng Việt (`vi`)。

## 效益與驗證標準

- 開發者或管理者只需執行 `java -jar jtrac-exporter.jar --db-url="..."` 即可完成一鍵靜態討論串匯出。
- 產出的 HTML 可在任何現代瀏覽器離線閱讀，且版面自適應 (Responsive Design)。
