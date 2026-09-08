## 1. 核心匯出引擎與串流打包實作 (Core Exporter & Zip Stream Engine)

- [x] 1.1 複製與整合核心匯出類別至 `src/main/java/info/jtrac/exporter/`，驗證資料結構與純 JDBC 存取邏輯相容
- [x] 1.2 擴充 `DatabaseReader` 支援傳入既有 `Connection` 與空間過濾（單一空間 vs 全部空間）功能
- [x] 1.3 實作 `ZipStreamExporter`，支援以 `ZipOutputStream` 直接將靜態 HTML 與實體附件寫入輸出串流，並加入附件缺失防呆機制

## 2. 多語系訊息與免責聲明資源配置 (i18n Resources & Translation Disclaimer)

- [x] 2.1 於 `src/main/resources/messages.properties`（繁中/預設）新增匯出導覽列按鈕、設定確認頁面欄位與翻譯限制免責聲明文字
- [x] 2.2 於其餘 4 國語系檔（`messages_en.properties`, `messages_zh_CN.properties`, `messages_ja.properties`, `messages_vi.properties`）補齊對應鍵值

## 3. Wicket 網頁導覽與確認頁面實作 (Wicket Navigation & HtmlExportPage)

- [x] 3.1 修改 `HeaderPanel.html` 與 `HeaderPanel.java`，於右側功能區新增「📦 匯出 HTML」連結，並確認未登入使用者隱藏或阻擋
- [x] 3.2 建立 `HtmlExportPage.html` 與 `HtmlExportPage.java`，實作範圍選擇（全部空間 / 當前空間）、UI 語系下拉選單、附件打包勾選方塊與醒目翻譯限制警示框
- [x] 3.3 於 `HtmlExportPage` 實作「開始匯出並下載 ZIP」按鈕事件，透過 Wicket `IRequestTarget` 觸發 `jtrac-export-YYYYMMDD.zip` 即時串流下載

## 4. 驗證、建置與版本控制 (Verification & Version Control)

- [x] 4.1 執行獨立 CLI 工具建置 `mvn clean package -f tools/jtrac-exporter/pom.xml`，驗證 `tools/jtrac-exporter.jar` 完整相容不受影響
- [x] 4.2 執行 JTrac 主專案打包建置 `mvn clean package -DskipTests`，驗證 WAR 封裝與各類別編譯無誤
- [x] 4.3 遵守專案鐵律執行 `git add .` 與 `git commit`，記錄功能變更
