# 提案：行動端 RWD 響應式體驗與深色主題適配 (Mobile RWD Experience)

## Why

JTrac 原始 Web 介面採用 2008 年桌面導向之固定寬度與多層巢狀 Table 佈局，且全站缺少 Viewport Meta 宣告，導致在現代智慧型手機或平板等行動裝置上，頁面僅能以 980px+ 縮小模擬顯示，使用者必須不斷雙指縮放與左右拖拉才能閱讀內容與點擊連結。

為滿足工程師、測試人員與主管在行動端「隨時隨地快速查閱 Issue、掌握待辦狀態、流暢瀏覽歷史討論」之核心需求，本提案全面引入現代化響應式網頁設計（Responsive Web Design, RWD），重構導航列、清單與詳細頁為行動友善體驗，並自動適配現代手機系統深色模式。

## What Changes

- **全域響應式基石與色彩系統擴充**：
  - 於 `BasePage.html`、`LoginPage.html`、`LogoutPage.html` 等頁面 `<head>` 注入標準 `<meta name="viewport" content="width=device-width, initial-scale=1.0">`。
  - 在 `jtrac.css` 擴充行動斷點系統（以 $768\text{px}$ 為手機/直向平板界線），並透過 `@media (prefers-color-scheme: dark)` 自動適配手機系統深色模式。
- **純 CSS 零依賴折疊漢堡選單 (Hamburger Navigation)**：
  - 於手機窄螢幕（$\le 768\text{px}$）下將傳統雙 Float 表格轉為頂部常駐 Header（Logo + 當前專案 + 漢堡按鈕 `[☰]`）。
  - 採用純 CSS Checkbox 技巧控制抽屜展開/收合，完全零 JavaScript 依賴，免疫 Wicket Ajax 刷新導致的事件解綁問題。
  - 觸控區域全面優化至 44px+，符合行動人因工程。
- **問題清單行動卡片化 (Responsive Cards)**：
  - 問題清單（`ItemListPanel`）在手機端自動自表格轉為直立卡片流。
  - 採「精選核心摘要」排版：清晰展示 Issue ID、彩色狀態 Badge、粗體主旨、指派人、建立者、優先度與更新時間。
  - 清單頂部工具列採精簡專注模式：手機端隱藏次要之 Excel/XML 匯出按鈕，保留筆數連結並放大分頁導航按鈕觸控面積。
- **問題詳細頁與歷程時間軸化 (Item View & History)**：
  - 問題屬性由 6 欄跨列表格自適應重組為單欄/雙欄立體卡片。
  - 歷史變更與留言記錄（History）由寬版表格轉化為垂直時間軸討論串。
  - 底部的回覆與狀態更新表單（`ItemViewFormPanel`）在手機端預設折疊為「💬 新增回覆 / 變更狀態」按鈕，保持閱讀流暢度。
- **儀表板狀態展開型專案卡片 (Dashboard Cards)**：
  - 儀表板矩陣在手機端轉為專案空間獨立卡片。
  - 展示專案名稱、核心指標（我的待辦、我回報的、全部），並逐行條列各狀態（Open、In Progress、Closed 等）的即時件數與直達篩選按鈕。
- **後台管理表格自適應平滑滾動 (Admin Tables Scroll Wrapper)**：
  - 使用者管理 (`UserListPage`) 與專案管理 (`SpaceListPage`) 採用外層平滑橫向滾動容器包裹（`overflow-x: auto`），完整保留所有欄位與操作圖示，避免破版。

## Capabilities

### New Capabilities
- `mobile-rwd`: 規範全站行動端 RWD 響應式佈局標準、純 CSS 漢堡導航選單、問題清單卡片化呈現、詳細頁時間軸化、儀表板狀態展開卡片、以及系統深色模式自動調適。

### Modified Capabilities
（無現有 Spec 需求異動，本變更為全新行動體驗能力規格）

## Impact

- **前端樣式與靜態資源**：`src/main/webapp/resources/jtrac.css`。
- **頁面模板 (HTML)**：
  - `src/main/java/info/jtrac/wicket/BasePage.html`、`LoginPage.html`、`LogoutPage.html`。
  - `src/main/java/info/jtrac/wicket/HeaderPanel.html`。
  - `src/main/java/info/jtrac/wicket/ItemListPanel.html`。
  - `src/main/java/info/jtrac/wicket/ItemViewPanel.html`、`ItemViewPage.html`、`ItemViewFormPanel.html`。
  - `src/main/java/info/jtrac/wicket/DashboardPage.html`。
  - `src/main/java/info/jtrac/wicket/UserListPage.html`、`SpaceListPage.html`。
- **後端程式碼相容性**：不更動任何 Java 業務邏輯與 API，純前端 CSS 與 HTML 模板調整，零後端風險。
