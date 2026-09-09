# 實作任務清單：移除 Excel 模組、清理 POI 依賴與備份 ZIP 整合 SQL 傾印檔

## 1. 徹底移除 Excel 模組與 POI 依賴 (WAR 瘦身)

- [ ] 1.1 從 `pom.xml` 中移除 `org.apache.poi:poi` (5.2.5) 依賴，並驗證 `mvn dependency:tree` 不再包含 POI 相關 jar
- [ ] 1.2 刪除後端類別 `info.jtrac.util.ExcelUtils.java`、`info.jtrac.domain.ExcelFile.java` 及測試類別 `info.jtrac.domain.ExcelFileTest.java`，並清理相關引用
- [ ] 1.3 刪除 Wicket 頁面 `ExcelImportPage`、`ExcelImportSpacePage`、`ExcelImportColumnPage`、`ExcelImportRowPage` (包含所有對應的 `.java` 與 `.html`)
- [ ] 1.4 修改 `ItemListPanel.html` 與 `ItemListPanel.java`，移除「匯出 Excel」連結與圖示，並確認工單清單頁面編譯通過
- [ ] 1.5 修改 `OptionsPage.html` 與 `OptionsPage.java`，移除「從 Excel 匯入」連結，並確認系統選項頁面編譯通過
- [ ] 1.6 清理 8 種多語系資源檔（`messages*.properties`）中不再使用之 Excel 字典鍵值

## 2. 備份匯出引擎升級 — 產出標準 SQL 傾印腳本 (jtrac-dump.sql)

- [ ] 2.1 在 `BackupExportService` 中設計並實作 `generateSqlDump(SystemBackupData data, Connection conn)` 方法，包含 Header 資訊（來源 DB、版本、產出時間）
- [ ] 2.2 實作通用 ANSI DDL 生成邏輯，並在各表建表語法前以註解提供 MySQL、PostgreSQL 與 HSQLDB 專屬方言語法
- [ ] 2.3 實作 14 張資料表依外鍵拓撲排序產出之 ANSI `INSERT INTO` 敘述，精確處理單引號跳脫（`''`）、Timestamp、Boolean、NULL 數值
- [ ] 2.4 在 `jtrac-dump.sql` 末尾產生各主流資料庫之 Sequence / Auto-Increment 重置提示語法
- [ ] 2.5 擴充 `ZipBundleService`，在建構備份 ZIP 串流時，將產出的 `jtrac-dump.sql` 寫入 ZIP 根目錄
- [ ] 2.6 撰寫單元測試驗證 `jtrac-dump.sql` 產生內容之完整性、字串跳脫正確性與 ZIP 封裝正確性

## 3. 全系統編譯驗證、本機 Jetty 10 部署與 Git Commit

- [ ] 3.1 執行 `mvn clean test`，確認全系統所有單元測試與整合測試通過（零錯誤、零失敗）
- [ ] 3.2 執行 `mvn package`，驗證 WAR 封裝成功且大小明顯縮減
- [ ] 3.3 將 WAR 熱部署至本機 Jetty 10，驗證前端介面無死連結且備份下載 ZIP 內確實含有完整 `jtrac-dump.sql`
- [ ] 3.4 遵循 Conventional Commits 英文規範，執行 `git add .` 與 `git commit` 提交所有變更
