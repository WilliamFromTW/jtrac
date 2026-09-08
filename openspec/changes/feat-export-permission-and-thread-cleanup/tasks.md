# 實作任務清單：專案空間匯出權限控管與討論串首筆 OPEN 快照清理

## 1. 核心資料讀取器空間過濾與討論串快照排除

- [ ] 1.1 在 `src/main/java/info/jtrac/exporter/config/ExportConfig.java` 新增 `allowedSpacePrefixCodes` 集合與存取方法，並提供大小寫不敏感之匹配支援。
- [ ] 1.2 在 `src/main/java/info/jtrac/exporter/db/DatabaseReader.java` 中實作空間授權過濾，並在讀取議題歷史記錄後自動排除每個 Issue 首筆無留言之 OPEN 快照，驗證快照排除邏輯生效。
- [ ] 1.3 同步更新獨立命令列工具 `tools/jtrac-exporter/` 內部之 `ExportConfig.java` 與 `DatabaseReader.java`，確保獨立 CLI 與 Web 行為 100% 一致。

## 2. 網頁匯出確認頁面動態權限與安全阻擋

- [ ] 2.1 在 `src/main/java/info/jtrac/wicket/HtmlExportPage.java` 中解析當前登入者之授權空間（一般使用者讀取 `user.getSpaces()`，SuperUser 讀取全庫空間），處理無任何空間權限之防呆提示。
- [ ] 2.2 動態構建匯出範圍單選選項（首項為「全部我有權限的專案空間」，後續為各個別空間），並依當前空間狀態正確設定預設選取項目。
- [ ] 2.3 在 `HtmlExportPage.onSubmit` 實作後端防禦深度安全檢核，阻斷任何越權空間下載請求，並將授權空間代碼集合傳入 `ExportConfig`。
- [ ] 2.4 在多國語系資源檔（`messages_zh_TW.properties`, `messages_en.properties`, `messages_zh_CN.properties`, `messages_ja.properties`, `messages_vi.properties`）新增 `html_export.scope.all_permitted` 鍵值。

## 3. 系統編譯與驗證

- [ ] 3.1 執行主專案編譯與單元測試 `mvn test-compile`，驗證 Wicket 與 Spring 整合無語法或依賴錯誤。
- [ ] 3.2 執行獨立 CLI 工具編譯打包 `mvn clean package -f tools/jtrac-exporter/pom.xml`，驗證 `jtrac-exporter.jar` 成功產出。
- [ ] 3.3 執行 `openspec validate feat-export-permission-and-thread-cleanup`，確認規格、提案與任務完全符合 OpenSpec 規範。
