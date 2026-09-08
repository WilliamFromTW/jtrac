## 1. 建立獨立工具模組骨架

- [x] 1.1 建立 `tools/jtrac-exporter/pom.xml`，配置 Java 11/8、現代 JDBC 驅動（HSQLDB 2.7.2, MySQL Connector/J, PostgreSQL, MS SQL）與 `maven-shade-plugin`

## 2. 實作資料模型與多語系字典

- [x] 2.1 實作 DTO 類別 (`SpaceDto`, `ItemDto`, `HistoryDto`, `AttachmentDto`, `UserDto`) 與參數設定類別 (`ExportConfig`)
- [x] 2.2 實作多語系字典類別 `I18nMessages`，支援繁中、英文、簡中、日文、越南文 5 種語系

## 3. 實作原生 JDBC 資料存取層

- [x] 3.1 實作 `DatabaseReader`，支援根據 URL 前綴自動判斷 Driver，連線遠端或自訂資料庫，並原生萃取 Spaces, Users, Items, History, Attachments

## 4. 實作 HTML 討論串樣板與附件複製

- [x] 4.1 實作 HTML 跳脫工具 `HtmlEscaper`，防止 XSS 攻擊
- [x] 4.2 實作 `HtmlGenerator`，渲染 JTrac 討論串卡片（一個 Issue 為一個 Section，緊密收納後續留言），支援附件複製、圖片縮圖預覽與遺失防呆機制，並產出導航索引頁 `index.html` 與各空間頁面

## 5. 實作 CLI 命令列進入點

- [x] 5.1 實作 `Main.java`，支援 `--db-url`（必填）、`--db-user`、`--db-password`、`--attachments-dir`、`--out`、`--lang`、`--help` 參數解析與完整匯出調度

## 6. 編譯、打包與功能驗證

- [x] 6.1 執行 Maven 打包生成可執行檔 `jtrac-exporter.jar`
- [x] 6.2 執行指令模式測試（包含 `--help` 顯示與真實/模擬資料庫匯出驗證）
