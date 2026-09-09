## 1. 依賴套件與參數基礎建設 (Dependencies & Configuration)

- [ ] 1.1 在 `pom.xml` 引入 `org.apache.pdfbox:pdfbox:2.0.31` 依賴，並執行 `mvn test-compile` 驗證依賴解析成功。
- [ ] 1.2 在 `HibernateJtracDao.createSchema()` 增加預設索引防護參數（`attachment.index.maxSizeMb=10`, `attachment.index.maxChars=50000`），並驗證啟動時正確寫入 `config` 表。

## 2. 智慧編碼轉碼與文字抽取器實作 (Text Extraction & Charset Guardrails)

- [ ] 2.1 實作 `SmartCharsetDetector` 工具類別，支援 BOM 偵測、Strict UTF-8 校驗、作業系統預設編碼與常用亞洲字集降級，並撰寫單元測試驗證 Big5、GBK 與 UTF-8 文字轉換無亂碼。
- [ ] 2.2 實作 `AttachmentTextExtractor` 工具類別，包含副檔名黑名單過濾（明確排除 `.doc`, `.xls`, `.ppt`, `.zip`, `.exe` 等）、白名單純文字/Office 串流解析（`.xlsx`, `.docx`, `.txt`, `.csv`, `.md`, `.log`）與 Apache PDFBox 整合（`.pdf`），並實施 10MB 與 50,000 字元截斷防護。
- [ ] 2.3 撰寫 `AttachmentTextExtractorTest` 單元測試，針對各格式檔案驗證白名單解析正常、黑名單直接排除、超大檔案/字數安全截斷且不拋出例外。

## 3. 純專案 ID 附件目錄結構與雙軌查檔安全網 (SpaceId Storage & Dual-Read Fallback)

- [ ] 3.1 修改 `AttachmentUtils.getFile()` 與相關輔助方法，支援純專案 ID 子目錄路徑 `${jtrac.home}/attachments/{spaceId}/{filePrefix}_{fileName}`，徹底免疫專案更名風險，並實作專案子目錄不存在時自動 Fallback 至根目錄之雙軌查檔機制。
- [ ] 3.2 修改 `JtracImpl.writeToFile()` 與上傳流程，新上傳之附件檔案一律寫入純專案 ID 子目錄路徑，並以單元測試驗證新檔案位置與下載讀取皆正常。

## 4. 舊版四階段全自動升級流水線 (Upgrade Pipeline & Migration)

- [ ] 4.1 實作 `AttachmentStorageMigrator` 工具類別，支援啟動時掃描 `attachments/` 根目錄，將歷史平鋪檔案依關聯查詢移動至對應的純專案 ID 子目錄，若為無關聯之孤兒檔案則隔離至 `attachments/0_ORPHAN/`。
- [ ] 4.2 串接伺服器啟動生命週期：整合 `HsqldbDatabaseMigrator`（HSQLDB 1.8 備份與升級）、`HibernateJtracDao.createSchema()`、`AttachmentStorageMigrator` 與遷移完成旗標，撰寫遷移流程測試驗證舊版環境無痛升級。

## 5. Lucene 附件全文檢索與非同步佇列處理 (Lucene Indexing & Async Queue)

- [ ] 5.1 修改 `History.createDocument()`、`Indexer.java` 與 `JtracImpl.java`，實作新上傳附件之背景非同步佇列（`ExecutorService`）文字抽取與 Lucene 索引寫入，保證 HTTP 請求極速回應且異常隔離。
- [ ] 5.2 修改 `JtracImpl.rebuildIndexes()` 與啟動後處理邏輯：升級完成後自動於背景執行非同步索引重建，且管理員於 UI 點擊「重建索引」時能即時透過進度條顯示進度。
- [ ] 5.3 撰寫整合測試驗證：上傳包含特定關鍵字之 `.xlsx` / `.pdf` / `.txt` 附件，執行全文檢索時可成功命中對應之工單與留言歷程。

## 6. 整合驗證與多語系文件標準 (Verification & Multilingual Docs)

- [ ] 6.1 執行完整的 Maven 建置與全套單元測試（`mvn clean test`），驗證所有測試案例 100% 通過。
- [ ] 6.2 遵循 8 種指定語系規範（en, zh-TW, zh-CN, es, de, fr, ja, vi），更新系統建置與升級文件，說明 JTrac 2.3.3 升級流程、選項 C 目錄結構與附件全文檢索功能。
