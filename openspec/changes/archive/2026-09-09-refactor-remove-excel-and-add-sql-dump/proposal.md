## Why

JTrac 早期遺留之「匯出 Excel」與未實作完善之「從 Excel 匯入」功能長期未維護，且引入了體積龐大的 Apache POI 依賴庫（佔據數 MB 之 WAR 空間），同時系統中已提供功能更為完整、格式更靈活的 Mode 1 (CLI) 與 Mode 2 (Web) 多語系獨立 HTML 靜態資料匯出器。此外，全系統備份與還原機制目前僅產出 JSON 格式資料，缺乏標準關聯式資料庫 SQL 傾印腳本，造成 DBA 難以使用標準 SQL 工具直接檢閱、查詢或進行跨異質資料庫平台（如 HSQLDB、MySQL、PostgreSQL）的手動災難復原與資料遷移。

為了精簡系統體積（瘦身）並大幅提升備份資料之開放性與通用相容性，本變更將徹底移除 Excel 匯入匯出模組與 POI 依賴，並於全系統備份 ZIP 封裝中整合產出具備完整 DDL 與拓撲排序 DML 的標準 `jtrac-dump.sql` 腳本。

## What Changes

- **徹底移除 Excel 匯出與匯入功能**：
  - 移除 `pom.xml` 中的 `org.apache.poi:poi` (5.2.5) 依賴項，大幅縮減建置產物 WAR 檔案大小。
  - 刪除後端工具類別與領域模型：`ExcelUtils.java`、`ExcelFile.java` 以及測試類別 `ExcelFileTest.java`。
  - 刪除 Wicket 表現層頁面：`ExcelImportPage`、`ExcelImportSpacePage`、`ExcelImportColumnPage`、`ExcelImportRowPage` 及其關聯之 HTML 樣板。
  - 移除工單清單工具列（`ItemListPanel`）之「匯出 Excel」按鈕與點擊事件。
  - 移除系統選項頁面（`OptionsPage`）之「從 Excel 匯入」連結入口。
  - 清理 8 種語系資源檔（`messages*.properties`）中不再使用之 Excel 相關多國語系字典鍵值。
- **全系統備份 ZIP 整合 `jtrac-dump.sql` 腳本**：
  - 升級備份匯出引擎：在產出 `manifest.json`、`data.json` 與 `attachments/` 之基礎上，額外動態產生單一整合之 `jtrac-dump.sql` 並收錄至備份 ZIP 根目錄。
  - **DDL 結構區塊**：前半段提供適用於多數關聯式資料庫之通用 ANSI SQL `CREATE TABLE` 語法，並於註解中詳細列出針對 MySQL、PostgreSQL 與 HSQLDB 之專屬資料庫方言建表語法。
  - **DML 資料區塊**：依據系統 14 張資料表之外鍵正向相依關係（拓撲排序），產生標準 ANSI `INSERT INTO` 敘述，精確處理單引號倍增跳脫（`''`）、日期時間格式化、布林值與 `NULL` 欄位。
  - **校準提示區塊**：腳本末端附註主流資料庫之 Sequence / Auto-Increment 重置與校準 SQL 指令範例。
- **還原機制相容性確認**：
  - JTrac 系統內部上傳 ZIP 執行還原時，維持以 `data.json` 為核心資料來源（享有交易回滾、操作者防鎖死護盾與安全快照保障）。
  - `jtrac-dump.sql` 專門作為 DBA 手動災難復原、離線稽核、報表分析與跨系統資料庫遷移之開放資產。

## Capabilities

### New Capabilities
（無，本變更不新增獨立規格能力）

### Modified Capabilities
- `system-backup-restore`: 在全系統備份 ZIP 封裝中新增整合性 `jtrac-dump.sql` 傾印腳本，包含標準 DDL、多資料庫方言建表註解、14 張表拓撲相依 ANSI INSERT 敘述與 Sequence 校準提示。

## Impact

- **相依庫 (Dependencies)**：移除 `org.apache.poi:poi:5.2.5`，建置產物 WAR 檔案大小大幅瘦身。
- **表現層 (Web Presentation)**：`ItemListPanel` 與 `OptionsPage` 介面移除 Excel 相關按鈕與連結；刪除 4 組未完善的 `ExcelImport*Page` 頁面檔案。
- **核心服務 (Core Services)**：`BackupExportService` 新增 SQL 傾印生成邏輯；`ZipBundleService` 擴充 ZIP 打包串流以寫入 `jtrac-dump.sql`。
- **語系資源 (i18n)**：8 種語系資源檔（`en`, `zh-TW`, `zh-CN`, `es`, `de`, `fr`, `ja`, `vi`）移除廢棄鍵值。
- **相容性 (Compatibility)**：備份 ZIP 保持向後相容，且增加標準 SQL 格式以利外部工具直接處理。
