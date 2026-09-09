## Context

請參閱 `proposal.md` 與 `specs/system-backup-restore/spec.md`。
JTrac 底層採用 Spring Framework + Hibernate 5 + Apache Wicket 9 架構，資料庫可為內建 HSQLDB 2.x 或外部 RDBMS（如 MySQL、PostgreSQL），實體檔案則獨立存放於 `${jtrac.home}/attachments/`。本設計針對一鍵式全系統備份與具備防鎖死保護之還原引擎提供技術實作方案。

## Goals / Non-Goals

**Goals:**
- **單一整合 ZIP 封裝包**：將資料庫結構化資料與實體附件目錄打包為標準 ZIP 格式，供管理者一鍵下載與上傳還原。
- **跨資料庫通用資料結構**：採用應用層 JSON 序列化，解耦底層資料庫方言（Dialect），實現不同資料庫引擎（如 HSQLDB 轉移至 PostgreSQL）之間的互通。
- **管理者帳號防鎖死保護 (Anti-Lockout Shield)**：還原過程中保護當前執行作業之 SuperUser 帳密與管理權限，確保還原後 100% 可正常登入。
- **還原前自動安全快照 (Safety Snapshot)**：在覆寫現行系統前，於伺服器本機自動建立緊急快照，提供失敗回滾能力。
- **自動索引重建與連線維護**：還原後背景非同步觸發 Lucene 全量索引重建，管理員 Session 保持有效。

**Non-Goals:**
- **即時熱備份 / 雙機熱備**：本功能聚焦於管理員觸發的隨選（On-demand）全系統快照與還原，不處理分散式叢集或資料庫即時雙向同步。
- **單一工單或個別專案之局部匯入**：本功能專注於「全系統災難復原與伺服器搬遷」，專案層級的個別資料交換仍依循標準匯入匯出途徑。

## Decisions

### 1. 備份包 ZIP 結構設計
備份檔案命名規則為 `jtrac-backup-yyyyMMdd-HHmmss.zip`，內部結構如下：
```text
jtrac-backup-20260909-120000.zip
├── manifest.json              <-- 中繼資訊：JTrac 版本、備份時間、各資料表筆數統計、操作者
├── data/
│   ├── config.json            <-- 全域設定 (Config)
│   ├── metadata.json          <-- 自訂欄位與流程定義 (Metadata)
│   ├── spaces.json            <-- 專案空間 (Spaces)
│   ├── users.json             <-- 使用者與群組 (Users, UserSpaceRoles)
│   ├── items.json             <-- 工單主表與關聯 (Items, ItemItems)
│   ├── history.json           <-- 完整歷史討論歷程 (History)
│   └── attachments.json       <-- 附件中繼資料 (Attachments)
└── attachments/               <-- 實體附檔目錄
    ├── 1001/
    │   └── test.png
    └── 1002/
        └── log.txt
```
*替代方案比較*：考慮過純 SQL dump，但因 HSQLDB、MySQL 與 PostgreSQL 之語法、序列（Sequence）、型態定義嚴重衝突，無法跨庫通用；採用 JSON 結構化資料集能藉由 Hibernate 實體對映跨越所有資料庫。

### 2. 資料庫外鍵拓撲順序與還原管線 (Topological Restore Pipeline)
為避免外鍵約束衝突（Foreign Key Constraints），還原匯入程序依循嚴格的拓撲順序進行：
1. **清空舊資料**（逆向順序）：`ItemItem` -> `History` -> `Attachment` -> `Item` -> `UserSpaceRole` -> `User` -> `Space` -> `Metadata` -> `Config`。
2. **寫入新資料**（正向順序）：
   - 階段 1：`Config`, `Metadata`
   - 階段 2：`Space`
   - 階段 3：`User` 與 `UserSpaceRole`（套用防鎖死過濾保護）
   - 階段 4：`Item`（強制保留原 ID 與編號，重置 Hibernate Sequence 計數器）
   - 階段 5：`History`, `ItemItem`, `Attachment` 中繼資料
   - 階段 6：解壓縮覆蓋 `${jtrac.home}/attachments/` 目錄

### 3. 操作者帳號防鎖死保護實作 (Anti-Lockout Implementation)
- 還原作業開始時，由 Wicket Session 擷取當前登入的 `User` 實體（`currentOperator`），讀取其 `loginName`、`password`（現行 BCrypt 或 MD5 雜湊）與 `email`。
- 還原 `User` 階段：
  - 遍歷備份檔中的使用者清單：
    - 若 `backupUser.loginName.equalsIgnoreCase(currentOperator.loginName)`：
      - 保留該備份使用者的 `id`（確保工單的 `logged_by_id` 關聯正確）。
      - 將其 `password` 替換為 `currentOperator.password`。
      - 確保其具有 `Role.ROLE_ADMIN`。
      - 標記 `operatorMatched = true`。
  - 遍歷結束後，若 `!operatorMatched`：
    - 將 `currentOperator` 作為新使用者存入資料庫，並建立全域 `ROLE_ADMIN` 之 `UserSpaceRole`。

### 4. 還原前自動安全快照 (Pre-Restore Safety Snapshot)
- 在解開上傳的 ZIP 進行還原前，系統先在伺服器端 `${jtrac.home}/backups/snapshot-before-restore-YYYYMMDD-HHmmss.zip` 建立本機完整備份。
- 整個資料庫還原過程在 Spring `TransactionTemplate` 交易管理中執行，若發生非預期例外，資料庫操作全面 Rollback，並保留該快照檔案供維運人員檢視。

### 5. 管理介面與互動 (Wicket UI)
- 在 [`OptionsPage.java`](file:///W:/developer/project/github/jtrac/src/main/java/info/jtrac/wicket/OptionsPage.java) 加入 `backup` 連結（僅 `isSuperUser` 可見）。
- 實作 `BackupRestorePage.java`：
  - 匯出區塊：說明文字 +「立即下載全系統備份包」按鈕。
  - 還原區塊：檔案上傳元件（`FileUploadField`）+「確認還原」按鈕。
  - 防呆對話框：點擊確認還原時跳出確認提示，明確告知「系統將自動保留您當前登入之管理員帳號與密碼，並於還原前自動建立安全快照」。

## Risks / Trade-offs

- **[記憶體耗盡 (OOM) 風險]**：當工單數量高達數萬筆或附件目錄數 GB 時，若在記憶體中一次載入所有實體會導致記憶體溢位。
  - *緩解措施*：採用串流式處理（Streaming）。JSON 資料庫匯出使用 Jackson Streaming API 或批次分頁讀取；ZIP 壓縮直接對接到 `ZipOutputStream` 串流，實體檔案採用緩衝區檔案複製，避免將大檔案載入 JVM 記憶體。
- **[資料庫主鍵衝突與 Sequence 計數器]**：還原保留原有 ID 後，後續新建工單可能引發主鍵重複衝突。
  - *緩解措施*：還原完成後，系統主動查詢各表最大 ID（`SELECT MAX(id) FROM items` 等），並校準底層 Sequence 或 Hibernate 主鍵生成器至最新值。
- **[Lucene 索引同步]**：資料庫全面翻新後舊索引失效。
  - *緩解措施*：在獨立執行緒中調用現有的 `Indexer.rebuildIndexes()` 服務，不阻塞管理員 HTTP 回應。
