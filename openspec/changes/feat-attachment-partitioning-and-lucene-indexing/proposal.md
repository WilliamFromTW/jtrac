## Why

目前 JTrac 的附件儲存採用根目錄平鋪結構（`${jtrac.home}/attachments/{filePrefix}_{fileName}`），所有專案附件混存在同一目錄下，缺乏依專案（Space）隔離管理的清晰層次，難以進行目錄級別的資料封存與權限控管。同時，系統內的 Lucene 全文檢索僅索引工單摘要、詳細描述與留言，附件內文完全未被納入索引，導致使用者無法透過關鍵字搜尋附件中的重要內容。

此外，當使用者由歷史經典發行版 JTrac 2.3.3（採用 HSQLDB 1.8 與舊版平鋪附件）進行升級時，缺乏一套貫穿「資料庫升級、Schema 補丁、附件目錄重構、全文索引重建」的完整自動化流水線。本提案旨在解決此儲存結構混亂問題，並引入安全有防護網的附件全文檢索能力與舊版無痛升級機制。

## What Changes

- **專案隔離附件目錄結構（選項 C）**：實體附件儲存路徑重構為 `${jtrac.home}/attachments/{spaceId}_{spacePrefix}/{filePrefix}_{fileName}`，依專案空間進行實體目錄隔離。
- **舊版孤兒檔案安全隔離**：遷移過程中若發現資料庫無關聯記錄之舊附件，集中隔離移入 `attachments/0_ORPHAN/`，保持根目錄整潔且零資料遺失。
- **雙軌讀取防呆安全網（Dual-Read Fallback）**：查檔邏輯優先檢索專案隔離子目錄，相容舊版根目錄平鋪路徑，保證遷移期間 0% 發生 404 破圖或找不到檔案。
- **JTrac 2.3.3 舊環境四階段自動升級流水線**：
  1. 伺服器啟動自動偵測 HSQLDB 1.8，建立快照備份並轉譯升級為 2.x 現代庫。
  2. 自動補齊缺失之系統參數（`pageSize`、索引防護門檻）並相容舊版 MD5 密碼雜湊。
  3. 自動執行附件實體結構化遷移（Option C）。
  4. 升級完成後自動於背景非同步觸發全量 Lucene 索引重建。
- **安全附件全文檢索（Lucene Attachment Indexing）**：
  - **白名單支援**：`.xlsx`、`.docx`、`.pdf`、`.txt`、`.csv`、`.md`、`.log`。
  - **黑名單排除**：明確排除舊版二進位格式（`.doc`、`.xls`、`.ppt`）與非文字二進位檔（`.zip`、`.exe`、圖片等），不抽取內文但保持附件正常下載。
  - **智慧防亂碼轉碼（SmartCharsetDetector）**：針對 CSV / TXT 實施「BOM 識別 -> 嚴格 UTF-8 校驗 -> 系統編碼降級轉換」三道防線。
  - **零依賴 Office 抽取**：使用 JDK 內建串流（`ZipInputStream` + `XMLStreamReader`）抽取 `.xlsx` 與 `.docx`，強制標準 UTF-8，絕無亂碼且零 WAR 體積膨脹。
  - **PDF 抽取支援**：引入成熟純 Java 的 Apache PDFBox（`pdfbox:2.0.31`，約 2.7MB）。
  - **防護網門檻參數化**：預設單檔 10MB、抽取 50,000 字元上限，收納於 `config` 表（`attachment.index.maxSizeMb`、`attachment.index.maxChars`）。
  - **緊湊倒排索引**：抽取內容採用 `Store.NO, Index.TOKENIZED`，僅存倒排索引不重複儲存原文，防止索引庫膨脹。

## Capabilities

### New Capabilities
- `attachment-partitioning-and-indexing`: 涵蓋附件專案目錄分區儲存（選項 C）、孤兒檔案隔離、雙軌查檔安全網、JTrac 2.3.3 四階段升級流水線、白名單安全抽取與黑名單排除、防亂碼轉碼器、防護網門檻參數化與背景非同步索引重建。

### Modified Capabilities
<!-- 本變更未修改既有規格之核心需求 -->

## Impact

- **實體儲存結構**：`${jtrac.home}/attachments/` 目錄將增加 `{spaceId}_{spacePrefix}/` 及 `0_ORPHAN/` 子目錄。
- **後端程式庫依賴**：在 `pom.xml` 引入 `org.apache.pdfbox:pdfbox:2.0.31`。
- **後端類別異動與新增**：
  - 更新 [`AttachmentUtils`](file:///W:/developer/project/github/jtrac/src/main/java/info/jtrac/util/AttachmentUtils.java) 支援專案子目錄定位與雙軌查檔。
  - 新增 `AttachmentStorageMigrator` 執行啟動期平鋪目錄自動遷移。
  - 新增 `AttachmentTextExtractor` 與 `SmartCharsetDetector` 執行安全文字抽取與轉碼。
  - 更新 [`History.createDocument()`](file:///W:/developer/project/github/jtrac/src/main/java/info/jtrac/domain/History.java) 與 [`Indexer`](file:///W:/developer/project/github/jtrac/src/main/java/info/jtrac/lucene/Indexer.java) 將附件內容納入全文檢索。
  - 更新 [`JtracImpl.rebuildIndexes()`](file:///W:/developer/project/github/jtrac/src/main/java/info/jtrac/JtracImpl.java) 批次抽取附件內文。
- **資料庫參數**：在 `config` 表新增 `attachment.index.maxSizeMb` 與 `attachment.index.maxChars` 預設參數。
- **向下相容性**：完全相容 JTrac 歷史發行版檔案與外部關聯資料庫，舊版平鋪附件在遷移完成前均可透過 Fallback 機制正常讀取。
