# 技術設計文件：移除 Excel 模組、POI 依賴與全系統備份整合 SQL 傾印檔

## Context

見 `proposal.md`。JTrac 系統現有的全系統備份與還原機制以 `BackupExportService` 與 `ZipBundleService` 為基礎，將資料庫實體序列化為 `data.json` 並與實體附件一同壓縮為 ZIP 檔。雖然 JSON 格式便於 JTrac 內建還原引擎進行防鎖死比對與物件對映，但對 DBA 或外部工具而言，缺乏標準 SQL 腳本會增加異質資料庫移轉與離線維運查詢的門檻。同時，系統中歷史殘留的 Excel 匯入匯出模組引入了 Apache POI 5.2.5 依賴（佔用顯著 WAR 體積），且已有更完善的獨立 HTML 匯出工具可取代工單檢視與報表需求。

## Goals / Non-Goals

**Goals:**
- 徹底移除 `org.apache.poi:poi` 依賴及所有與 Excel 相關之後端類別、Wicket 頁面與多語系字典鍵值，大幅縮減建置 WAR 檔案體積。
- 在 `BackupExportService` 中新增 ANSI 標準 DDL 與按 14 張資料表外鍵拓撲排序之 `INSERT INTO` 產生邏輯。
- SQL 傾印檔案前半段提供通用 DDL，並在註解中提供 MySQL、PostgreSQL 與 HSQLDB 的專屬建表語法。
- 在 `ZipBundleService` 打包流程中，將 `jtrac-dump.sql` 寫入 ZIP 根目錄，與 `manifest.json`、`data.json`、`attachments/` 平級並存。
- 確保所有資料型別（字串跳脫 `''`、Timestamp、Boolean、NULL）在各主流資料庫皆能無痛解析。

**Non-Goals:**
- 不修改 JTrac 系統內部介面上傳 ZIP 之還原引擎核心依據（還原時仍以 `data.json` 為主，確保交易隔離、快照與管理員防鎖死保護）。
- 不在此變更中實作基於 SQL 檔案的前端直接上傳執行功能（DBA 手動復原應直接使用命令列或資料庫客戶端執行 `jtrac-dump.sql`）。

## Decisions

### 1. 單一整合 SQL 檔案命名為 `jtrac-dump.sql`
- **決定**：備份 ZIP 壓縮檔內固定命名為 `jtrac-dump.sql`。
- **理由**：既有專案名稱前綴辨識度高，又明確指出該檔案為資料庫傾印（Dump）腳本，便於解壓縮後 DBA 一目了然。
- **替代方案**：考慮過 `backup.sql` 或 `database.sql`，但 `jtrac-dump.sql` 具備專案特異性，與其他系統備份混放時不易誤判。

### 2. DDL 採通用 ANSI 為主體，附帶 MySQL / PostgreSQL / HSQLDB 方言註解
- **決定**：在 `jtrac-dump.sql` 的 DDL 區段，優先產出通用標準 SQL-92 `CREATE TABLE` 語法，並在各表上方以 SQL 註解（`--`）完整標示針對 MySQL (InnoDB, UTF-8mb4)、PostgreSQL 以及 HSQLDB (2.x) 的語法細節（例如自增欄位宣告、外鍵約束等）。
- **理由**：通用語法具備最大可讀性，而詳細的方言註解則讓 DBA 無論將備份移轉至任何目標資料庫，都能迅速複製對應方言語法執行建表。

### 3. DML (INSERT INTO) 依 14 張資料表拓撲排序輸出
- **決定**：嚴格按照資料表外鍵正向相依順序輸出 `INSERT INTO`：
  1. `config`
  2. `spaces`
  3. `metadata`
  4. `space_sequence`
  5. `users`
  6. `user_space_roles`
  7. `tags`
  8. `storedsearch`
  9. `attachments`
  10. `items`
  11. `history`
  12. `item_users`
  13. `item_tags`
  14. `item_items`
- **理由**：確保任何關聯式資料庫在預設外鍵檢查開啟的狀態下，直接執行該 SQL 檔案皆不會發生外鍵約束違規錯誤（Foreign Key Constraint Violation）。

### 4. 徹底清除 Excel 相關模組以釋放套件空間
- **決定**：移除 `pom.xml` 中 `org.apache.poi:poi` 依賴，同時刪除 `ExcelUtils`、`ExcelFile`、`ExcelFileTest` 及 `ExcelImport*Page` 系列所有 `.java` 與 `.html`。
- **理由**：Excel 匯出匯入功能程式碼老舊，且 POI 依賴龐大，移除後可直接為 WAR 瘦身 3MB 以上，且已由 Mode 1/Mode 2 HTML 匯出器完美取代。

## Risks / Trade-offs

- **[風險 1] 大量字串內容含有單引號或換行字元導致 SQL 語法解析失敗**
  → *因應策略*：在 SQL 產生邏輯中實作專門的字串跳脫方法，將所有單引號轉譯為標準 SQL 的雙單引號（`'` -> `''`），換行與特殊字元保留原樣或正確封裝於單引號字串常數內。
- **[風險 2] 移除 Excel 頁面後殘留死連結或語系缺少警告**
  → *因應策略*：同步檢視並清理 `ItemListPanel.html`/`.java`、`OptionsPage.html`/`.java`，並自 8 國多語系資源檔中徹底移除對應的鍵值。
- **[風險 3] 備份 ZIP 中新增 SQL 檔對既有還原引擎造成干擾**
  → *因應策略*：還原引擎係直接尋找 ZIP 內的 `manifest.json` 與 `data.json`，額外加入的 `jtrac-dump.sql` 不會對還原流程造成任何負面影響，同時具備向前與向後相容性。

## Migration Plan

1. 刪除 Excel 相關程式碼、HTML 樣板與 `pom.xml` 依賴。
2. 實作 `BackupExportService.generateSqlDump` 與 `ZipBundleService` 整合。
3. 執行單元測試驗證 SQL 傾印語法產生正確性。
4. 執行 `mvn clean package` 驗證專案編譯與 WAR 瘦身產物。
5. 部署至本機 Jetty 10 驗證前端操作無死連結，且匯出之備份 ZIP 內確實含有完整且格式正確之 `jtrac-dump.sql`。
