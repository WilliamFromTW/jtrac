# 變更提案：專案空間匯出權限控管、複選打包與討論串首筆 OPEN 快照清理

## Why

目前 JTrac 的 HTML 靜態匯出功能存在以下安全隱患與使用體驗問題：
1. **空間存取權限越權外洩 (Security Vulnerability)**：在網頁匯出確認頁面中，任何登入使用者若選擇「全部專案空間 (ALL)」，後端資料庫查詢會無差別讀取所有專案空間資料，導致一般使用者可下載其未被授權存取的機密專案議題與附件；且若使用者尚未被指派任何空間權限，導覽列依然顯示匯出連結。
2. **空間匯出缺乏彈性組合 (Lack of Multi-Selection)**：既有介面僅能單選全部或目前單一空間，無法自由勾選指定多個空間進行組合打包。
3. **討論串歷程資訊冗餘 (Redundant Thread History)**：HTML 匯出時，每個議題的「討論串歷程與留言記錄」第一筆固定為系統建立議題時強制插入的「OPEN」快照（且無任何留言內文），此資訊已完整呈現在上方的主卡片中。此冗餘記錄不僅重複佔據版面，更導致未曾被回覆的議題誤顯示為「1 則更新」，造成使用者困擾。

## What Changes

- **導覽列入口權限控管 (Header Navigation Guardrail)**：
  - 若登入使用者無任何專案空間存取權限（且非 SuperUser），全站導覽列（`HeaderPanel`）完全不顯示「📦 匯出 HTML」選項。
  - 若使用者嘗試直接以 URL 存取匯出頁面，系統強制阻擋存取並導向首頁。
- **核取方塊複選空間範圍 (Multi-Selection Checkboxes & UI Scope Options)**：
  - 匯出確認頁面（`HtmlExportPage`）動態列出該使用者**所有具備權限之專案空間**，採用**核取方塊複選 (Checkboxes)** 呈現，支援全選/自由勾選多個空間組合打包下載。
  - 防呆阻擋：若使用者未勾選任何空間，停用下載按鈕並給予提示。
- **後端安全防護與越權阻擋 (Backend Guardrails & Access Control)**：
  - 於後端提取資料前進行嚴格權限驗證：非 SuperUser 且無該空間存取權限之請求一律拋出安全性拒絕存取例外（AccessDeniedException）。
  - 匯出引擎嚴格僅讀取並打包使用者所勾選之合法授權空間資料。
- **排除首筆無留言 OPEN 歷程快照與零回覆區塊隱藏 (Thread History Cleanup & Zero-Reply Collapse)**：
  - 自動識別每個議題的初始建立快照（首筆且狀態為 OPEN 且留言為空）並予以排除。
  - 若排除後該議題**完全無任何後續留言或狀態變更**，**完全隱藏**「💬 討論串歷程與留言記錄」區塊，僅呈現主議題卡片，維持介面清爽。
  - 若有後續回覆，回覆序號從真正第 1 則回覆（`#1`）依序起算，更新徽章精準反映數量。
- **獨立命令列工具 (CLI) 同步升級**：
  - 獨立 CLI 工具（`tools/jtrac-exporter`）同步套用快照排除與零留言區塊隱藏邏輯。
  - CLI 參數 `--space` 升級支援逗號分隔（如 `--space=PROJ1,PROJ2`），支援離線多空間組合匯出。
- **多國語系擴充 (i18n)**：
  - 補齊核取方塊與多空間選擇之多國語系資源。

## Capabilities

### Modified Capabilities
- `html-exporter`: 更新導覽列權限顯示規則、新增核取方塊複選空間打包、強化後端權限驗證、規範首筆 OPEN 快照排除、零留言區塊隱藏，以及 CLI 支援逗點分隔多空間代碼。

## Impact

- **修改程式碼**：
  - `src/main/java/info/jtrac/wicket/HeaderPanel.java`
  - `src/main/java/info/jtrac/wicket/HtmlExportPage.java` & `HtmlExportPage.html`
  - `src/main/java/info/jtrac/exporter/config/ExportConfig.java`
  - `src/main/java/info/jtrac/exporter/db/DatabaseReader.java`
  - `src/main/java/info/jtrac/exporter/html/HtmlGenerator.java`
  - `tools/jtrac-exporter/...` 對應類別
  - `messages_*.properties`
- **相容性**：
  - CLI 工具完全相容舊版單空間與無參數全空間指令，並額外支援逗點多空間語法。
