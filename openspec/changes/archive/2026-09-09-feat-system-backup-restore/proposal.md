## Why

JTrac 目前缺乏由 Web 管理者介面直接發起的「全系統備份與還原」功能。既有的 Excel 匯入僅支援單次批次新增工單，無法保留工單編號、無法匯入歷史歷程與附件；而傳統依賴底層資料庫工具（如 mysqldump 或直接複製資料庫檔案）操作門檻高，且無法跨不同資料庫（HSQLDB、MySQL、PostgreSQL）搬遷。

此外，在災難復原或環境搬遷時，若無差別覆蓋使用者資料庫，極易因舊備份中管理者密碼遺忘或帳號不存在，導致當前維運人員在還原後當場被「反鎖在門外（Account Lockout）」。因此，系統迫切需要一套可靠、支援實體附件與跨資料庫結構、具備「操作者帳號防鎖死保護」與「還原前自動快照」的一鍵式全系統備份與還原機制。

## What Changes

- **新增全系統備份包匯出功能 (Full Backup Export)**：
  - 管理者可一鍵匯出單一 `.zip` 封裝包（如 `jtrac-backup-YYYYMMDD-HHmmss.zip`）。
  - 壓縮包內包含應用層跨資料庫通用結構化資料（涵蓋 Config、Users、UserSpaceRoles、Spaces、Metadata、Items、ItemItems、History、Attachments）以及完整的實體附件目錄（`${jtrac.home}/attachments/`）。
- **新增全系統還原功能 (Full Restore Engine)**：
  - 提供管理者上傳備份 `.zip` 檔案進行全系統資料還原。
  - **還原前緊急快照 (Safety Snapshot)**：在清空與寫入新資料前，系統自動於伺服器端產生一份當前狀態的緊急快照備份，以防還原異常時復原。
  - **管理者帳號防鎖死保護 (Admin Anti-Lockout Shield)**：
    - 還原過程中，系統自動鎖定並保護當前執行還原之 SuperUser（最高管理員）憑證。
    - 若備份檔中存在同名帳號：保留其關聯歷史與工單，但密碼強制保留「現行登入中的密碼雜湊」，並確保具備 `ROLE_ADMIN`。
    - 若備份檔中無此帳號：還原後主動將當前操作者注入為 SuperUser。
  - **自動背景重建全文檢索索引 (Auto-Reindex)**：還原完成後，系統自動在背景重新建立 Lucene 搜尋索引，管理者 Session 保持有效無中斷。
- **新增管理者介面 (Admin UI)**：
  - 在管理功能清單（`OptionsPage`）中獨立新增「系統備份與還原 (Backup & Restore)」入口，嚴格限制僅最高管理員（`isSuperUser()`）可存取。

## Capabilities

### New Capabilities
- `system-backup-restore`: 涵蓋全系統 ZIP 備份包產出、跨資料庫通用資料序列化與還原、實體附件同步封裝、還原前緊急快照、管理者帳號防鎖死保護機制、以及還原後 Lucene 索引自動重建。

### Modified Capabilities
<!-- 無既有 Capabilities 之行為規格變更 -->

## Impact

- **後端架構**：
  - 新增備份與還原核心服務（如 `BackupRestoreService`），負責實體導出/導入、JSON/ZIP 處理、以及交易管理。
  - 新增針對管理者憑證防護與外鍵拓撲順序的還原管道。
- **Web 介面**：
  - 在 [`OptionsPage.java`](file:///W:/developer/project/github/jtrac/src/main/java/info/jtrac/wicket/OptionsPage.java) 與 [`OptionsPage.html`](file:///W:/developer/project/github/jtrac/src/main/java/info/jtrac/wicket/OptionsPage.html) 新增「系統備份與還原」入口連結。
  - 新增 `BackupRestorePage.java` 及對應 HTML 樣板，提供匯出下載、檔案上傳還原與進度狀態回報。
- **多語系資源**：
  - 於 `messages*.properties` 擴充備份、還原、進度、警告與成功提示之對應 i18n 訊息。
