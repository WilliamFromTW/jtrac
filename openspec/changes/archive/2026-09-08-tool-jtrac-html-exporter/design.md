# 技術架構與設計文件 (Technical Design)

## 模組與目錄結構

本工具採獨立 Maven 模組設計，位於 `tools/jtrac-exporter/` 目錄下：

```
tools/jtrac-exporter/
├── pom.xml
└── src/
    └── main/
        └── java/
            └── info/
                └── jtrac/
                    └── exporter/
                        ├── Main.java                 # CLI 進入點、參數解析與執行調度
                        ├── config/
                        │   └── ExportConfig.java     # 執行期參數配置物件
                        ├── db/
                        │   └── DatabaseReader.java   # 原生 JDBC 驅動載入與跨 DB 查詢
                        ├── model/                    # 輕量資料傳輸物件 (DTOs)
                        │   ├── SpaceDto.java
                        │   ├── UserDto.java
                        │   ├── ItemDto.java
                        │   ├── HistoryDto.java
                        │   └── AttachmentDto.java
                        ├── i18n/
                        │   └── I18nMessages.java     # 5 國多語系字彙與標籤字典
                        └── html/
                            ├── HtmlEscaper.java      # 輕量 HTML 跳脫工具 (防止 XSS)
                            └── HtmlGenerator.java    # 討論串 Section 模板渲染與附件處理
```

---

## 核心類別設計

### 1. `ExportConfig`
封裝命令列參數：
- `dbUrl`（必填）
- `dbUser`（預設為 `sa`）
- `dbPassword`（預設為 `""`）
- `dbDriver`（可選，若無則依 URL 前綴自動判斷）
- `attachmentsDir`（可選）
- `outputDir`（預設 `./jtrac-html-export`）
- `lang`（預設 `zh-TW`，支援 `en`, `zh-CN`, `ja`, `vi`）
- `spaceFilter`（可選 Space 前綴代碼）

### 2. `DatabaseReader`
- **驅動程式自動偵測**：
  - `jdbc:mysql:` -> `com.mysql.cj.jdbc.Driver`
  - `jdbc:postgresql:` -> `org.postgresql.Driver`
  - `jdbc:hsqldb:` -> `org.hsqldb.jdbcDriver`
  - `jdbc:sqlserver:` -> `com.microsoft.sqlserver.jdbc.SQLServerDriver`
- **連線與查詢**：
  - 使用標準 `DriverManager.getConnection(url, user, password)`。
  - 執行相容於各大資料庫的 ANSI SQL：
    - `SELECT id, prefix_code, name, description FROM spaces ORDER BY id`
    - `SELECT id, login_name, name, email FROM users`
    - `SELECT id, space_id, sequence_num, summary, detail, status, severity, priority, logged_by, assigned_to, time_stamp, planned_effort FROM items ORDER BY space_id, sequence_num`
    - `SELECT id, item_id, comment, attachment_id, time_stamp, logged_by, assigned_to, status, severity, priority, actual_effort FROM history ORDER BY item_id, id`
    - `SELECT id, item_id, file_name, file_prefix FROM attachments`

### 3. `HtmlGenerator`
- **討論串版型 (Discussion Thread Section)**：
  - 以 `<section class="issue-card" id="{PREFIX}-{SEQ}">` 封裝單一 Issue。
  - 包含頂部 Meta 標籤列、原始描述區塊、以及按時間序列排列的 `<div class="history-thread">`。
- **附件處理**：
  - 若有指定附件目錄且實體檔案存在，將檔案從 `{attachmentsDir}/{filePrefix}_{fileName}` 複製至 `{outDir}/attachments/{filePrefix}_{fileName}`。
  - 圖檔副檔名（`.png`, `.jpg`, `.jpeg`, `.gif`, `.webp`）產生 `<img>` 預覽，點擊以新分頁檢視原圖。
  - 其他副檔名產生 `<a download>` 下載連結。
  - 檔案若不存在，標註 `(附件遺失 / Attachment Missing)`，確保匯出不中斷。
- **單一自包含 CSS**：
  - 內建現代化卡片式 CSS 樣式，支援桌面與行動裝置自適應瀏覽。

### 4. `I18nMessages`
- 支援 5 種語系：`zh-TW`、`en`、`zh-CN`、`ja`、`vi`。
- 涵蓋所有欄位名稱、狀態標籤 (Open / Closed 等)、附件文字與統計標題。

---

## 依賴管理 (`pom.xml`)
- Java 11 原生編譯。
- 僅引入現代 JDBC Driver 依賴：
  - `org.hsqldb:hsqldb:2.7.2`
  - `com.mysql:mysql-connector-j:8.3.0`
  - `org.postgresql:postgresql:42.7.3`
  - `com.microsoft.sqlserver:mssql-jdbc:12.6.1.jre11`
- 使用 `maven-shade-plugin` 產出 `jtrac-exporter.jar`，指定 `Main-Class` 為 `info.jtrac.exporter.Main`。
