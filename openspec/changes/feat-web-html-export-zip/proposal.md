# 提案：JTrac 網頁端一鍵匯出 HTML ZIP 壓縮檔與確認視窗功能

## Why

目前 JTrac 系統已具備獨立的命令列 HTML 匯出工具（`tools/jtrac-exporter`），能將票證資料與附件匯出為現代化離線 HTML。然而在實際線上維運環境中，Jetty 執行中的 JTrac 會鎖定本地 HSQLDB 資料庫檔案，導致管理員或專案經理無法在不關閉服務的情況下執行 CLI 匯出工具；此外，伺服器通常位於遠端主機，終端使用者或一般管理者難以進入命令列尋找匯出目錄。

為了提供直覺、安全且不干擾線上運作的匯出途徑，我們需要在 JTrac 網頁導覽列（`HeaderPanel`）中新增「📦 匯出 HTML」按鈕。點擊後開啟確認設定頁面（模式二），提供空間範圍選擇、介面語系切換（5國語言）、附件打包開關，並明確標示「語系僅套用於框架，歷史內容無法翻譯」之免責聲明。設定完成後直接將整包 HTML 與附件打包為 `.zip` 檔串流下載至使用者瀏覽器，同時完整保留既有的獨立 CLI 工具與編譯流程。

## What Changes

- **導覽列新增匯出連結**：在全站通用頂部導覽列 `HeaderPanel` 新增「📦 匯出 HTML」連結（具備 i18n 支援）。
- **匯出設定確認頁面 (`HtmlExportPage`)**：
  - **範圍選擇**：支援「全部專案空間 (All Spaces)」或「目前專案空間 (Current Space)」。
  - **語系選擇**：提供 5 種 UI 語系選單（繁體中文 `zh-TW`、英文 `en`、簡體中文 `zh-CN`、日語 `ja`、越南語 `vi`）。
  - **附件選項**：提供「包含實體附件打包」核取方塊。
  - **翻譯限制醒目說明**：介面上明確標註：「語系設定僅套用於網頁介面、欄位名稱與狀態標籤（UI 框架）；議題歷史主題、詳細描述與留言回覆為原始記錄，無法自動翻譯。」
- **網頁串流 ZIP 下載機制**：
  - 核心共用匯出邏輯（`info.jtrac.exporter.*`）整併或複用於 JTrac 主專案。
  - 透過 Spring 管理之現有連線池（`DataSource`）於 JVM 行內執行資料萃取，完全避開 HSQLDB 檔案鎖定衝突。
  - 動態以 `ZipOutputStream` 串流寫入 Wicket `WebResponse`，以 `jtrac-export-YYYYMMDD.zip` 即時下載，不佔用伺服器實體磁碟。
- **CLI 工具相容性保證**：
  - 保留 `tools/jtrac-exporter` 的所有命令列參數、standalone packaging 與建置流程，原有維運腳本與使用習慣維持 100% 相容。

## Capabilities

### Modified Capabilities
- `html-exporter`: 新增 Web 介面一鍵匯出 ZIP 功能，支援模式二設定確認對話頁面（空間範圍、5國框架語系、附件開關、內容無法翻譯提示）與即時串流下載，並維持原有獨立命令列工具。

## Impact

- **Web 介面與導覽**：修改 `HeaderPanel.html` / `HeaderPanel.java`，新增 `HtmlExportPage.html` / `HtmlExportPage.java`。
- **訊息檔**：在 `src/main/resources/messages*.properties`（zh_TW, en, zh_CN, ja, vi）新增對應鍵值。
- **後端架構**：在 `src/main/java/info/jtrac/exporter/` 提供共用匯出引擎與記憶體串流匯出器（`ZipStreamExporter`），供 Wicket 頁面調用。
- **向後相容**：`tools/jtrac-exporter` 仍可單獨 `mvn clean package` 並維持原本命令列功能。
