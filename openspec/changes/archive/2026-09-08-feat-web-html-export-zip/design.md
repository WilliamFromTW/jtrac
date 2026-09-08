# 技術設計文件：JTrac 網頁端一鍵匯出 HTML ZIP 壓縮檔與確認視窗功能

## Context

詳見 [proposal.md](file:///W:/developer/project/github/jtrac/openspec/changes/feat-web-html-export-zip/proposal.md)。
JTrac 運行於 Jetty/Tomcat 等 Servlet 容器時，內嵌的 HSQLDB 資料庫檔案會被容器行程獨佔鎖定。若透過外部 Process 呼叫 CLI 工具讀取實體檔案，將發生 `Database lock acquisition failure`。此外，遠端主機維運者難以登入伺服器磁碟取檔。因此，必須在 Wicket Web 層提供原生的行內（In-Process）匯出功能，並透過 HTTP 回應串流直接將打包後的 `.zip` 提供給使用者下載。

## Goals / Non-Goals

**Goals:**
- 在頂部導覽列 `HeaderPanel` 新增多語系「📦 匯出 HTML」入口。
- 提供符合「模式二」的專屬匯出確認頁面 `HtmlExportPage`：
  - 專案空間範圍選擇（全部空間 vs 當前空間）。
  - UI 框架語系下拉選單（支援繁中 `zh-TW`、英文 `en`、簡中 `zh-CN`、日語 `ja`、越語 `vi`）。
  - 包含實體附件打包核取方塊。
  - 醒目的警示提示（Notice/Guardrail）：「語系設定僅套用於網頁介面、欄位名稱與狀態標籤（UI 框架）；議題歷史主題、詳細描述與留言回覆為原始記錄，無法自動翻譯。」
- 實作行內 `ZipStreamExporter`，使用 Spring `DataSource` 現有連線取得資料庫資料，以 `ZipOutputStream` 串流即時生成並下載 `jtrac-export-YYYYMMDD.zip`。
- 完整保留 `tools/jtrac-exporter` 獨立命令列工具的建置與執行能力。

**Non-Goals:**
- 不對議題標題、歷史留言及內容進行機器翻譯（已於介面免責聲明中告知）。
- 不修改既有資料庫綱要（Schema）。
- 不實作複雜的背景排程或非同步隊列（行內匯出實測 2,000+ 議題僅需 1~2 秒，直接同步串流具備最佳使用者體驗與最低系統複雜度）。

## Decisions

### 1. 行內 Spring DataSource 連線 vs 外部 CLI 行程
- **決定**：在 Web 應用內直接透過 Spring `applicationContext.getBean("dataSource")` 獲取 `javax.sql.DataSource` 並建立連線。
- **理由**：HSQLDB 檔案模式僅允許單一行程開啟。行內連線直接利用既有連線池，不會觸發檔案鎖定例外，且無作業系統 Process 啟動與外部路徑依賴。
- **替代方案**：呼叫外部 `java -jar tools/jtrac-exporter.jar`，但會因為 HSQLDB `.lck` 檔案鎖定而失敗。

### 2. 記憶體/HTTP 即時串流 ZIP 下載 vs 伺服器實體磁碟暫存
- **決定**：使用 Wicket 的 `IRequestTarget` 搭配 `WebResponse.getOutputStream()`，以 `ZipOutputStream` 即時寫入每一筆 HTML 與附件檔案。
- **理由**：
  - 避免伺服器磁碟暫存檔殘留與磁碟空間耗盡風險。
  - 瀏覽器在伺服器壓縮的同時即開始接收下載，TTFB（Time to First Byte）極短。
- **替代方案**：先在伺服器 `temp` 目錄寫入 `.zip` 再提供下載，需要額外的定期清理機制與磁碟配額管理，增加維運負擔。

### 3. 專屬確認頁面 (`HtmlExportPage`) vs 即時下載 / JavaScript Modal
- **決定**：採用 Wicket 標準的設定確認頁面（模式二）。
- **理由**：
  - Wicket 1.3 的原生頁面機制與表單控制元件（DropDownChoice、CheckBox、RadioChoice）具備最佳的相容性與無狀態安全性。
  - 方便清楚展示翻譯限制警示條款，避免使用者對多語系輸出產生非預期的認知落差。
  - 使用者可自由決定是否要耗費流量與時間打包所有歷史附件。

### 4. 模組架構與程式碼共用
- **決定**：在主專案 `src/main/java/info/jtrac/exporter/` 建立共用匯出模組（包含 `DatabaseReader`、`HtmlGenerator`、`I18nMessages`、`HtmlEscaper`、DTOs 與 `ZipStreamExporter`）。
- **理由**：JTrac Web App 可以直接呼叫這些純 Java 類別進行資料庫讀取與 HTML 渲染；而 `tools/jtrac-exporter` 維持其獨立 pom.xml 與 standalone jar 結構，互不衝突。

## Risks / Trade-offs

- **[風險] 歷史紀錄中之附件在伺服器磁碟上已損毀或遺失**  
  → **緩解措施 (Guardrail)**：`ZipStreamExporter` 檢查實體檔案存在性。若遺失則記錄日誌、於 HTML 中維持註記，並在 ZIP 中略過該檔案，絕不中斷整個 ZIP 串流或拋出未攔截之伺服器例外。
- **[風險] 串流過程中客戶端關閉瀏覽器或中斷連線 (Client Abort)**  
  → **緩解措施**：捕捉 `ClientAbortException` 或 `IOException`，優雅關閉資料庫連線與資源，避免連線池洩漏。
- **[風險] 未授權存取**  
  → **緩解措施 (Guardrail)**：`HtmlExportPage` 於建構式驗證使用者是否已登入（`getPrincipal().getId() != 0`）；未登入者強制導向登入頁。
