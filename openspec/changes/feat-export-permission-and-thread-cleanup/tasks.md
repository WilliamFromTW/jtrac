# 實作任務清單：專案空間匯出權限控管、複選打包與討論串首筆 OPEN 快照清理

## 1. 核心資料讀取器空間多選過濾與討論串快照排除

- [x] 1.1 在 `src/main/java/info/jtrac/exporter/config/ExportConfig.java` 中將空間篩選升級為支援複選集合（`targetSpacePrefixCodes`），並實作逗號分隔字串解析與大小寫不敏感比對方法。
- [x] 1.2 在 `src/main/java/info/jtrac/exporter/db/DatabaseReader.java` 中實作空間多選過濾，並在讀取議題歷史記錄後自動排除每個 Issue 首筆無留言之 OPEN 快照。
- [x] 1.3 在 `src/main/java/info/jtrac/exporter/html/HtmlGenerator.java` 中實作零回覆區塊隱藏：當排除快照後議題之歷程清單為空時，完全不渲染討論串容器區塊。
- [x] 1.4 同步更新獨立命令列工具 `tools/jtrac-exporter/` 內部對應之 `ExportConfig.java`、`DatabaseReader.java` 與 `HtmlGenerator.java`，確保獨立 CLI 工具功能與 Web 完全一致。

## 2. 網頁導覽入口與匯出確認頁面權限防呆

- [x] 2.1 在 `src/main/java/info/jtrac/wicket/HeaderPanel.java` 中為「📦 匯出 HTML」連結加上可見性判定：當一般使用者無任何空間權限且非 SuperUser 時，隱藏該連結。
- [x] 2.2 在 `src/main/java/info/jtrac/wicket/HtmlExportPage.java` 中解析當前使用者之授權空間，若無任何空間權限且非 SuperUser 則強制重定向至首頁。
- [x] 2.3 在 `src/main/java/info/jtrac/wicket/HtmlExportPage.java` 與 `HtmlExportPage.html` 中實作專案空間核取方塊複選元件（Checkboxes），提供未勾選防呆檢核與預設勾選邏輯。
- [x] 2.4 在 `HtmlExportPage.onSubmit` 實作後端防禦深度安全檢核，阻斷任何越權空間下載請求，並將所選之空間代碼集合傳入 `ExportConfig`。
- [x] 2.5 在多國語系資源檔（`messages_zh_TW.properties`, `messages_en.properties`, `messages_zh_CN.properties`, `messages_ja.properties`, `messages_vi.properties`）新增核取方塊與未勾選防呆提示鍵值。

## 3. 系統編譯與驗證

- [x] 3.1 執行主專案編譯與單元測試 `mvn test-compile`，驗證 Wicket 與 Spring 整合無語法或依賴錯誤。
- [x] 3.2 執行獨立 CLI 工具編譯打包 `mvn clean package -f tools/jtrac-exporter/pom.xml`，驗證 `jtrac-exporter.jar` 成功產出並支援 `--space=P1,P2` 參數。
- [x] 3.3 執行 `openspec validate feat-export-permission-and-thread-cleanup`，確認規格、提案與任務完全符合 OpenSpec 規範。
