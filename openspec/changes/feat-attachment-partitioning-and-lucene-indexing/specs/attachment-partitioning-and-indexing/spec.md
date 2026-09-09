## Purpose

本規格定義 JTrac 附件實體儲存之專案隔離架構（選項 C）、JTrac 2.3.3 與 HSQLDB 1.8 舊版全自動四階段升級流水線、孤兒檔案隔離處置、雙軌查檔安全網，以及具備副檔名白名單/黑名單排除（明確排除舊版 doc/xls）、智慧編碼轉碼防亂碼與單檔容量/字數門檻防護之 Lucene 全文檢索索引機制。

## ADDED Requirements

### Requirement: 專案空間隔離之附件實體目錄結構 (Attachment Storage Partitioning)
系統 SHALL 將工單上傳之實體附件檔案，依其所屬專案空間（Space）儲存於獨立的子目錄中，路徑格式嚴格遵循 `${jtrac.home}/attachments/{spaceId}_{spacePrefix}/{filePrefix}_{fileName}`。

#### Scenario: 上傳新工單或留言附件
- **WHEN** 使用者在專案 ID 為 `1`、代碼為 `DEFAULT` 的空間中建立工單或新增歷程並上傳名為 `spec.xlsx` 的檔案
- **THEN** 系統將實體檔案儲存於 `${jtrac.home}/attachments/1_DEFAULT/{filePrefix}_spec.xlsx`

#### Scenario: 跨專案儲存空間實體隔離
- **WHEN** 不同專案上傳同名檔案
- **THEN** 各檔案分別儲存在各自的 `{spaceId}_{spacePrefix}` 子目錄中，互不覆蓋與干擾

---

### Requirement: 雙軌讀取防呆安全網 (Dual-Read Fallback Guardrail)
系統在檢視、下載或串流讀取附件實體檔案時，SHALL 優先自專案隔離子目錄讀取；若該子目錄下無對應檔案，SHALL 自動降級檢查平鋪根目錄 `${jtrac.home}/attachments/{filePrefix}_{fileName}`，確保升級遷移期間 0% 發生 404 錯誤。

#### Scenario: 優先從專案子目錄讀取已遷移附件
- **WHEN** 使用者點擊下載已完成遷移之附件
- **THEN** 系統直接自 `${jtrac.home}/attachments/{spaceId}_{spacePrefix}/{filePrefix}_{fileName}` 提供檔案

#### Scenario: 自動降級至根目錄讀取未遷移檔案
- **WHEN** 附件實體檔案尚未完成目錄遷移，仍在根目錄 `${jtrac.home}/attachments/` 下
- **THEN** 系統自動觸發降級查檔並成功提供檔案下載，不拋出 FileNotFoundException

---

### Requirement: 舊版平鋪附件自動遷移與孤兒檔案隔離 (Attachment Storage Migration)
系統在伺服器啟動階段 SHALL 自動掃描 `attachments/` 根目錄；若發現舊版平鋪檔案，SHALL 透過資料庫關聯查詢所屬專案並移動至專案子目錄；若資料庫無對應關聯（已被刪除或遺失之孤兒檔案），SHALL 集中隔離移入 `attachments/0_ORPHAN/`。

#### Scenario: 正常平鋪舊附件自動分群搬移
- **WHEN** 伺服器啟動並在 `attachments/` 根目錄偵測到歷史平鋪檔案 `{filePrefix}_{fileName}`
- **THEN** 系統自動建立目標目錄 `attachments/{spaceId}_{spacePrefix}/` 並將檔案安全搬移，同時記錄遷移日誌

#### Scenario: 孤兒檔案安全隔離
- **WHEN** 根目錄中之檔案在資料庫中查無任何關聯記錄
- **THEN** 系統自動建立 `attachments/0_ORPHAN/` 並將該孤兒檔案移入，記錄警告日誌且絕不刪除檔案

---

### Requirement: JTrac 2.3.3 四階段全自動升級流水線 (End-to-End Upgrade Pipeline)
系統在啟動時 SHALL 依序執行完整之四階段升級程序：(1) HSQLDB 1.8 資料庫升級與備份、(2) Schema 與設定補丁、(3) 附件目錄 Option C 遷移、(4) 背景非同步 Lucene 全文索引重建。

#### Scenario: HSQLDB 1.8 舊資料庫自動備份與現代化升級
- **WHEN** 伺服器啟動於 `data/db/` 偵測到 HSQLDB 1.8 格式之資料庫
- **THEN** 系統自動建立時間戳快照目錄 `backup-hsqldb-1.8-<timestamp>/`，轉譯舊版腳本並升級為 HSQLDB 2.x 格式，成功建立資料庫連線

#### Scenario: 系統設定與舊版密碼相容
- **WHEN** 資料庫完成升級並啟動 Hibernate Session
- **THEN** 系統自動補齊缺漏之分頁參數與索引門檻參數，並支援舊版 MD5 密碼於登入時自動升級為 BCrypt

---

### Requirement: 附件全文檢索白名單與黑名單排除 (Attachment Full-Text Indexing Filter)
系統在建立 Lucene 全文索引時，SHALL 嚴格執行副檔名篩選：明確排除舊版二進位檔案（黑名單），僅針對符合現代開放標準之檔案（白名單）抽取純文字並加入倒排索引。

#### Scenario: 舊版 doc 與 xls 格式直接排除抽取
- **WHEN** 工單附件為 `.doc` 或 `.xls`（包含 `.ppt`、`.zip`、`.exe`、圖檔等）
- **THEN** 系統在索引時直接跳過內文抽取，附件檔案本身保持完好供正常下載

#### Scenario: 現代格式白名單安全抽取
- **WHEN** 工單附件為 `.xlsx`、`.docx`、`.pdf`、`.txt`、`.csv`、`.md`、`.log`
- **THEN** 系統執行文字抽取並寫入 Lucene Document 的 `text` 欄位進行索引

---

### Requirement: 抽取防亂碼與編碼轉換機制 (Encoding Conversion Guardrails)
系統在抽取附件文字時，SHALL 保證不產生亂碼：對於 OpenXML 檔案（`.xlsx`, `.docx`）強制以標準 UTF-8 串流解析；對於純文字與 CSV 檔案，實施「BOM 識別 -> 嚴格 UTF-8 校驗 -> 系統編碼降級」之轉碼機制。

#### Scenario: 現代 Office 檔案強制 UTF-8 零亂碼
- **WHEN** 系統讀取 `.xlsx` 的 `xl/sharedStrings.xml` 或 `.docx` 的 `word/document.xml`
- **THEN** 系統以 UTF-8 串流直接解析文字節點，保證抽取文字與原文件完全一致

#### Scenario: 非 UTF-8 的 CSV/TXT 檔案智慧轉碼
- **WHEN** 使用者上傳 Big5 (CP950) 或 GBK 編碼之 `.csv` 或 `.txt` 附件
- **THEN** 系統偵測到 UTF-8 解碼異常後，自動調用本地系統編碼或退避編碼轉換，安全提取乾淨純文字，不拋出例外

---

### Requirement: 索引防護門檻與背景非同步重建 (Guardrails and Async Reindexing)
系統 SHALL 對單一附件之文字抽取實施檔案容量上限（預設 10MB）與抽取字數上限（預設 50,000 字元）防護，相關參數收納於 `config` 表；升級完成後系統 SHALL 自動在背景非同步重建索引，並保留管理員於 UI 手動觸發的能力。

#### Scenario: 超過大小或字數門檻安全截斷
- **WHEN** 附件檔案超過 10MB 或抽取文字超過 50,000 字元
- **THEN** 系統自動截取前 50,000 字元建立索引或跳過超大檔案，防止 JVM OutOfMemoryError 與索引庫暴增

#### Scenario: 升級完成後自動觸發背景重建
- **WHEN** 伺服器啟動完成資料庫升級與附件遷移
- **THEN** 系統自動在背景執行 `JtracImpl.rebuildIndexes()`，不阻塞 HTTP 請求與使用者登入操作

#### Scenario: 管理員在 UI 手動觸發重建索引
- **WHEN** 管理員至後台「重建索引」頁面點擊「開始」
- **THEN** 系統在背景重整索引庫，並透過 Ajax 輪詢進度條即時更新百分比與筆數
