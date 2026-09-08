# 變更提案：專案空間匯出權限控管與討論串首筆 OPEN 快照清理

## Why

目前 JTrac 的 HTML 靜態匯出功能存在以下安全隱患與使用體驗問題：
1. **空間存取權限越權外洩 (Security Vulnerability)**：在網頁匯出確認頁面中，任何登入使用者若選擇「全部專案空間 (ALL)」，後端資料庫查詢會無差別讀取所有專案空間資料，導致一般使用者可下載其未被授權存取的機密專案議題與附件。
2. **討論串歷程資訊冗餘 (Redundant Thread History)**：HTML 匯出時，每個議題的「討論串歷程與留言記錄」第一筆固定為系統建立議題時強制插入的「OPEN」快照（且無任何留言內文），此資訊已完整呈現在上方的主卡片中。此冗餘記錄不僅重複佔據版面，更導致未曾被回覆的議題誤顯示為「1 則更新」，造成使用者困擾。

## What Changes

- **動態專案空間權限選項 (UI Scope Options)**：
  - 匯出確認頁面（`HtmlExportPage`）依據當前登入使用者的權限清單（`user.getSpaces()`，若為 SuperUser 則取得全站空間）動態產生範圍選項。
  - 提供「全部我有權限的專案空間 (共 N 個)」選項，以及個別專案空間單選項。
  - 進入頁面時若已位於特定空間，預設選取該空間；否則預設選取全部有權限之空間。
- **後端安全防護與越權阻擋 (Backend Guardrails & Access Control)**：
  - 於後端提取資料前進行嚴格權限驗證：非 SuperUser 且無該空間存取權限之請求一律拋出安全性拒絕存取例外（AccessDeniedException）。
  - 當選擇「全部」時，匯出引擎嚴格僅讀取並打包該使用者具備權限之專案空間資料。
- **排除首筆無留言 OPEN 歷程快照 (Thread History Cleanup)**：
  - 於資料讀取或樣板渲染層（`DatabaseReader` / `HtmlGenerator`）自動識別每個議題的初始建立快照（首筆且狀態為 OPEN 且留言為空）。
  - 將該快照從「討論串歷程與留言記錄」中排除，讓真正後續回覆從 `#1` 依序編號。
  - 討論串更新計數徽章（`thread-count-badge`）精準反映實際回覆數；若無任何後續留言，顯示「0 則更新」與空狀態提示。
- **獨立命令列工具 (CLI) 同步套用**：
  - 獨立 CLI 工具（`tools/jtrac-exporter`）同步套用排除首筆 OPEN 快照之資料處理邏輯，確保 CLI 離線產出與網頁匯出樣式一致。
- **多國語系擴充 (i18n)**：
  - 於 5 國語言資源中新增「全部我有權限的專案空間」之多語系支援。

## Capabilities

### Modified Capabilities
- `html-exporter`: 更新網頁匯出安全性規格（嚴格比對使用者空間授權）、擴充範圍選擇模型，並於討論串歷程規範中定義首筆無留言 OPEN 快照之自動過濾規則。

## Impact

- **修改程式碼**：
  - `src/main/java/info/jtrac/wicket/HtmlExportPage.java` & `HtmlExportPage.html`
  - `src/main/java/info/jtrac/exporter/config/ExportConfig.java`
  - `src/main/java/info/jtrac/exporter/db/DatabaseReader.java`
  - `src/main/java/info/jtrac/exporter/html/HtmlGenerator.java`
  - `tools/jtrac-exporter/...` 對應類別
  - `messages_*.properties`
- **相容性**：
  - 獨立 CLI 工具維持既有命令列參數相容性。
  - 資料庫實體資料不進行任何修改（純呈現與萃取層之安全性過濾）。
