## 1. 全域 Viewport 注入與 CSS 變數 / 深色模式基礎設定

- [x] 1.1 在 `BasePage.html`、`LoginPage.html` 與 `LogoutPage.html` 的 `<head>` 區塊注入 `<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=5.0">`，透過瀏覽器檢視網頁原始碼驗證標籤正確存在。
- [x] 1.2 在 `jtrac.css` 中宣告 CSS Custom Properties（現代配色、間距、圓角）與 `@media (prefers-color-scheme: dark)` 深色模式樣式，在深色模式系統下驗證介面背景色與文字對比度符合無障礙可讀性。

## 2. 純 CSS 響應式頂部導航與漢堡折疊選單實作

- [x] 2.1 在 `HeaderPanel.html` 注入純 CSS Checkbox Hack（`<input type="checkbox" id="nav-toggle">` 與漢堡按鈕 `<label for="nav-toggle">`），並在 `jtrac.css` 實作小於等於 768px 下的折疊與彈出選單樣式，驗證點擊漢堡圖示時選單平滑展開與收合且無 JS 依賴。
- [x] 2.2 調整手機端導航項目的觸控熱區至至少 44x44px，確認在行動直式畫面中點選導航項目能正常跳轉且不干擾 Wicket 內部狀態。

## 3. 問題清單行動自適應卡片流與精簡工具列實作

- [x] 3.1 在 `jtrac.css` 針對小於等於 768px 螢幕設定 `.jtrac-list` 轉卡片流樣式（`tbody`, `tr`, `td` 設定為區塊元素，隱藏桌機表頭 `tr.sortable`，增加卡片白底、圓角與陰影），在手機解析度下驗證問題列表呈現為單一直列卡片流。
- [x] 3.2 在 `jtrac.css` 設定手機端工具列樣式，隱藏次要的 Excel/XML 匯出按鈕，放大分頁按鈕與篩選觸控區域，驗證在 768px 寬度以下工具列按鈕清晰易點擊。

## 4. 儀表板專案空間狀態展開型卡片實作

- [x] 4.1 在 `jtrac.css` 針對 `DashboardPage` 儀表板表格實作響應式卡片樣式，在手機端將專案空間表格轉為狀態展開型卡片並直列排版，驗證手機寬度下可清楚檢視各狀態件數與點擊篩選連結。

## 5. 問題詳細頁資訊垂直堆疊、時間軸化與回覆表單折疊實作

- [x] 5.1 在 `jtrac.css` 與 `ItemViewPage.html` 設定屬性表格於手機端自動轉為垂直兩欄或單欄堆疊排版，驗證欄位不再向右溢出邊界。
- [x] 5.2 在 `jtrac.css` 設定歷程紀錄面板轉換為垂直時間軸對話串樣式，驗證留言時間、人員、歷史變更內容在直式螢幕下排版易讀。
- [x] 5.3 在 `ItemViewPage.html` 為問題更新/回覆區塊加入原生 `<details>` / `<summary>` 行動端折疊機制，驗證手機端預設折疊、點擊可展開並能正常送出更新。

## 6. 後台管理表格平滑橫向滾動容器實作

- [x] 6.1 檢查並在 `UserListPage.html`、`SpaceListPage.html` 等管理表格外層包覆響應式容器 `.table-responsive`，並在 `jtrac.css` 加入 `overflow-x: auto; -webkit-overflow-scrolling: touch;`，驗證在手機端可平滑橫向滾動而不破壞整體頁面版面。

## 7. 完整建置測試、響應式驗證與正式部署

- [x] 7.1 執行 Maven 單元測試 `mvn clean test`，驗證所有後端業務邏輯與頁面渲染測試全數通過（零回歸）。
- [x] 7.2 打包 `mvn clean package -DskipTests` 並熱部署至 `W:\developer\jetty-10.0.26\webapps\ROOT.war`，在常見手機解析度（375px, 414px, 768px）驗證所有頁面響應式效果符合預期。
