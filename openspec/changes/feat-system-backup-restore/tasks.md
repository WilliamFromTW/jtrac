## 1. 基礎結構與資料序列化服務 (Data Serialization & Packaging)

- [x] 1.1 定義備份 Manifest 與資料傳輸結構（包含 JTrac 版本、備份時間戳記、各資料表統計），並撰寫單元測試驗證 JSON 序列化與反序列化。
- [x] 1.2 實作跨資料庫通用資料匯出服務，依序讀取 Config, Metadata, Spaces, Users, UserSpaceRoles, Items, ItemItems, History, Attachments 實體並轉為結構化 JSON 資料。
- [x] 1.3 實作 ZIP 串流打包工具，將結構化資料與 `${jtrac.home}/attachments/` 目錄封裝為單一 ZIP 壓縮檔，並驗證 ZIP 解壓縮與檔案完整性。

## 2. 還原引擎與防鎖死核心邏輯 (Restore Engine & Anti-Lockout Shield)

- [x] 2.1 實作還原前自動緊急快照（Safety Snapshot）功能，在覆寫系統前自動備份至 `${jtrac.home}/backups/` 並驗證快照生成。
- [x] 2.2 實作操作者憑證防鎖死保護邏輯：捕捉當前登入之 SuperUser 憑證，還原時若備份中存在同名帳號則強制保留現行密碼雜湊與 ROLE_ADMIN，若不存在則主動注入為最高管理員，並撰寫單元測試驗證防鎖死行為。
- [x] 2.3 實作資料庫外鍵拓撲還原管線：在交易內依逆向相依清空舊資料、依正向相依倒灌新資料、並校準主鍵計數器（Sequence / Max ID），驗證資料表外鍵無任何約束衝突。
- [x] 2.4 實作實體附件目錄覆蓋與同步，並於還原完成後在背景自動觸發 Lucene 全量搜尋索引重建，驗證工單可立即被全文檢索。

## 3. 管理介面與使用者互動 (Wicket UI & Security)

- [x] 3.1 於 `OptionsPage.java` 與 `OptionsPage.html` 新增「系統備份與還原」選單項目，驗證僅具備 `isSuperUser()` 權限者可見。
- [x] 3.2 建立 `BackupRestorePage.java` 及對應 HTML 頁面，提供「一鍵匯出下載備份包」串流按鈕與「上傳 ZIP 執行還原」表單元件。
- [x] 3.3 於還原介面加入安全警告與防鎖死提示對話框，並在還原完成後呈現成功反饋訊息與無中斷 Session 維護。
- [x] 3.4 於 `messages*.properties` 等多語系資源檔擴充備份與還原之相關國際化字串。

## 4. 全系統整合驗證與測試 (Verification & Deployment)

- [x] 4.1 執行 Maven 測試套件，確保所有既有測試與新增之備份還原單元測試全數通過（`BUILD SUCCESS`）。
- [x] 4.2 執行端到端實機測試：於本地伺服器匯出備份、進行工單異動、上傳還原、驗證當前管理者仍可正常操作且所有歷史工單與附件完整復原。
