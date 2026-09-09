# OpenSpec 專案主規格文件清單 (Master Specs)

本文件自動同步匯總 JTrac 專案中所有已歸檔與實作之規格說明書（Specifications）。

---

## 規格目錄 (Capabilities Index)

| 規格代碼 (Capability) | 名稱與範疇 | 目的說明 (Purpose) | 狀態 |
|---|---|---|---|
| [`i18n-resources`](i18n-resources/spec.md) | 多國語系資源與過濾規範 | 定義 JTrac 專案中多國語系資源檔案之 UTF-8 編碼規範與 Maven 資源處理隔離規則，確保在不同作業系統與 JDK 環境下建置及執行時皆能正確處理字元編碼，並避免框架變數被構建工具誤替換。 | Active |
| [`build-documentation`](build-documentation/spec.md) | 多語系建置與編譯文件規範 | 規範 JTrac 專案之多語系建置與編譯技術文件結構，確保全球開發者皆能在其母語或慣用語言環境下，清楚理解 Maven 建置指令、依賴套件本機快取下載機制，以及 WAR 封裝檔內部依賴整合原理。 | Active |
| [`html-exporter`](html-exporter/spec.md) | 獨立命令列與網頁即時串流 HTML 討論串匯出工具 | 定義獨立命令列工具 `jtrac-exporter.jar` 與網頁即時串流 ZIP 下載之功能規格與資料處理邏輯。支援指定 JDBC 連線字串或既有 Spring DataSource 存取資料庫，相容 HSQLDB 1.8 歷史庫，在零舊版依賴或行內連線下將議題、討論串歷程與附件匯出為支援離線明暗主題與 5 國多語系之高對比靜態 HTML 報表與 ZIP 串流下載。 | Active |
| [`backend-security`](backend-security/spec.md) | Spring Security 5.8 現代化安全認證與授權規範 | 規範 JTrac 系統以 Spring Security 5.8 替代過時 Acegi 1.0.7 之現代化安全認證與授權機制，包含雙模無痛密碼雜湊升級、LDAP/AD 整合與權限上下文管理。 | Active |
| [`backend-persistence`](backend-persistence/spec.md) | Hibernate 5.6 持久層 DAO 與原生 Lucene 全文檢索規範 | 規範 JTrac 資料持久層現代化架構，以原生 Hibernate 5.6 `SessionFactory` 重構 `HibernateJtracDao`，徹底解耦過時之 `HibernateDaoSupport` 與 `HibernateTemplate`，並整合資料表結構自動同步與原生輕量 Lucene 全文檢索。 | Active |
| [`mobile-rwd`](mobile-rwd/spec.md) | 全站行動端 RWD 響應式體驗與深色主題適配 | 為 JTrac 提供全站行動端響應式網頁設計（RWD），透過純 CSS 技術重構導航列、問題清單、詳細頁與儀表板，支援小螢幕卡片化呈現、漢堡折疊選單與系統深色模式自動切換，實現零外部依賴、輕量流暢的行動端 Issue 查閱體驗。 | Active |
| [`system-backup-restore`](system-backup-restore/spec.md) | 全系統備份、還原與防鎖死機制 | 提供 JTrac 系統最高管理員一鍵匯出包含結構化資料、`jtrac-dump.sql` 整合傾印檔與實體附件之單一 ZIP 壓縮包，並在還原時具備自動建立安全快照、最高管理員憑證防反鎖保護、外鍵拓撲批次注入、以及背景自動重建 Lucene 搜尋索引之高可用防護架構。 | Active |
| [`attachment-partitioning-and-indexing`](attachment-partitioning-and-indexing/spec.md) | 專案隔離附件目錄結構與 Lucene 全文檢索 | 定義 JTrac 附件實體儲存之專案隔離架構（純專案 ID 結構，防改名風險）、JTrac 2.3.3 與 HSQLDB 1.8 舊版全自動四階段升級流水線、孤兒檔案隔離處置、雙軌查檔安全網，以及具備副檔名白名單/黑名單排除（明確排除舊版 doc/xls）、新上傳附件非同步佇列索引、智慧編碼轉碼防亂碼與單檔容量/字數門檻防護之 Lucene 全文檢索索引機制。 | Active |


---

## 系統架構與流程圖總覽

### 1. `i18n-resources` 資源過濾與 UTF-8 處理流程

```mermaid
flowchart TD
    A[Maven Build 啟動] --> B{資源類型判斷}
    B -->|版本資訊檔: jtrac-version.properties| C[啟用 filtering: true]
    B -->|多國語系檔: messages_*.properties 及其他資源| D[關閉 filtering: false]
    C --> E[注入 POM 版本號與 Timestamp]
    D --> F[以原始 UTF-8 位元組原樣複製到 target]
    E --> G[打包至 WAR 封裝檔]
    D --> G
    G --> H[執行期 Wicket / Spring 載入 UTF-8 資源]
```

### 2. `build-documentation` 系統文件導覽架構

```mermaid
flowchart TD
    A[專案根目錄 README.md] -->|多語系連結導覽| B[docs/build/ 目錄]
    B --> C[BUILD_zh-TW.md 繁體中文]
    B --> D[BUILD_en.md English]
    B --> E[BUILD_zh-CN.md 简体中文]
    B --> F[BUILD_ja.md 日本語]
    B --> G[BUILD_vi.md Tiếng Việt]
    B --> J[BUILD_de.md Deutsch]
    B --> K[BUILD_es.md Español]
    B --> L[BUILD_fr.md Français]
    C --> H[Maven 編譯與依賴快取說明]
    D --> H
    E --> H
    F --> H
    G --> H
    J --> H
    K --> H
    L --> H
    H --> I[WAR WEB-INF/lib 第三方套件封裝解析]
```

### 3. `html-exporter` 討論串匯出架構

#### 3.1 命令列獨立 CLI 匯出架構
```mermaid
flowchart TD
    A[使用者命令列啟動 CLI] --> B{解析參數}
    B -->|必要: --db-url| C[載入對應 JDBC Driver]
    B -->|可選: --db-user, --db-password| C
    B -->|可選: --attachments-dir| D[附件目錄檢查]
    B -->|可選: --lang, --out, --space| E[語系與輸出路徑配置]
    
    C --> F[建立原生 JDBC Connection]
    F --> G[查詢 SPACES 專案空間清單]
    G --> H[查詢 USERS 使用者對照表]
    G --> I[查詢 ITEMS 議題資料]
    I --> J[查詢 HISTORY 討論串追蹤記錄]
    J --> K[查詢 ATTACHMENTS 附件記錄]
    
    H & I & J & K --> L[組裝討論串資料模型]
    D & L --> M[處理實體附件複製與縮圖標記]
    E & M --> N[依語系字典渲染 HTML 樣板]
    
    N --> O[產出 index.html 專案空間導覽索引]
    N --> P[產出 各 Space 討論串 HTML 頁面]
    M --> Q[輸出 attachments/ 靜態附件目錄]
```

#### 3.2 網頁介面即時串流 ZIP 下載架構
```mermaid
flowchart TD
    User([登入使用者]) -->|點擊導覽列 [📦 匯出 HTML]| Nav[HeaderPanel 導覽連結]
    Nav --> Page[HtmlExportPage 匯出確認頁面]
    
    subgraph Mode2 [模式二：匯出確認與參數配置]
        Page --> OptScope[選擇範圍: 全部專案空間 / 目前專案空間]
        Page --> OptLang[選擇 UI 語系: zh-TW / en / zh-CN / ja / vi]
        Page --> OptAttach[包含實體附件打包: 是 / 否]
        Page --> Disclaimer[⚠️ 醒目警示: 語系僅套用於介面框架，議題內容無法翻譯]
    end
    
    Page -->|點擊「開始匯出並下載 ZIP」| Action[ZipDownloadRequest]
    
    subgraph CoreEngine [行內記憶體串流匯出引擎 (ZipStreamExporter)]
        Action --> Conn[取得 Spring DataSource 既有連線]
        Conn --> Extract[讀取 Spaces / Items / History / Attachments]
        Extract --> Render[依指定語系渲染 HTML 頁面]
        OptAttach -->|勾選| CopyAttach[讀取實體附件串流]
        OptAttach -->|未勾選| SkipAttach[略過實體附件]
        Render & CopyAttach & SkipAttach --> ZipStream[寫入 ZipOutputStream]
    end
    
    ZipStream --> Response[Wicket WebResponse 直接串流下載]
    Response --> Browser([瀏覽器接收 jtrac-export-YYYYMMDD.zip])
```

### 4. `backend-security` 系統認證與密碼升級流程圖

```mermaid
flowchart TD
    Login["使用者登入 (帳號/密碼)"] --> ProviderMgr["ProviderManager"]
    ProviderMgr --> DaoProvider["DaoAuthenticationProvider"]
    DaoProvider --> HybridEncoder["JtracHybridPasswordEncoder"]
    
    HybridEncoder --> Match{"密碼比對是否成功?"}
    Match -- "否" --> Fail["拋出 BadCredentialsException 登入失敗"]
    Match -- "是" --> CheckMD5{"密碼是否為舊版 32 碼 MD5?"}
    CheckMD5 -- "是" --> Upgrade["以 BCrypt 重新雜湊並非同步更新資料庫"]
    CheckMD5 -- "否 (已是 BCrypt)" --> Success["驗證成功"]
    Upgrade --> Success
    Success --> SetCtx["寫入 SecurityContextHolder 完成登入"]
```

### 5. `backend-persistence` 資料持久層與檢索架構圖

```mermaid
flowchart TD
    subgraph ServiceLayer["業務服務層"]
        JtracImpl["JtracImpl (@Transactional)"]
    end

    subgraph PersistenceLayer["現代化持久層 (Hibernate 5.6)"]
        SessionFactory["org.hibernate.SessionFactory"]
        DAO["HibernateJtracDao"]
        HbmXML["jtrac.hbm.xml 實體對應"]
        Hbm2ddl["hibernate.hbm2ddl.auto=update"]
    end

    subgraph SearchLayer["全文檢索層 (原生 Lucene)"]
        Indexer["原生 Indexer"]
        IndexSearcher["原生 IndexSearcher"]
        LuceneDir["FSDirectory (jtrac.home/indexes)"]
    end

    JtracImpl --> DAO
    SessionFactory --> DAO
    HbmXML --> SessionFactory
    Hbm2ddl --> SessionFactory
    JtracImpl --> Indexer
    JtracImpl --> IndexSearcher
    Indexer --> LuceneDir
    IndexSearcher --> LuceneDir
```
 
### 6. `mobile-rwd` 行動端響應式與漢堡選單架構

```mermaid
flowchart TD
    Start([使用者瀏覽器載入頁面]) --> ReadViewport[讀取 Viewport Meta: width=device-width]
    ReadViewport --> CheckWidth{檢測螢幕視窗寬度}
    
    CheckWidth -->|> 768px 桌機 / 寬平板| DesktopLayout[桌機標準佈局]
    DesktopLayout --> D1[頂部雙向水平導航列]
    DesktopLayout --> D2[完整欄位多欄資料表格]
    DesktopLayout --> D3[顯示 XML 匯出按鈕]
    
    CheckWidth -->|<= 768px 手機 / 直向平板| MobileLayout[行動端 RWD 佈局]
    MobileLayout --> M1[頂部純 CSS 漢堡折疊選單]
    MobileLayout --> M2[清單自動轉為直立卡片流]
    MobileLayout --> M3[清單工具列隱藏次要匯出, 放大分頁按鈕]
    MobileLayout --> M4[詳細頁欄位堆疊, 回覆表單預設折疊]
    MobileLayout --> M5[後台管理表格套用平滑滾動容器]
    
    MobileLayout --> CheckTheme{檢測系統色彩模式}
    CheckTheme -->|深色模式 prefers-color-scheme: dark| DarkTheme[啟用夜間高對比暗色主題]
    CheckTheme -->|淺色模式 / 預設| LightTheme[套用經典標準藍白主題]
```

```mermaid
stateDiagram-v2
    [*] --> Collapsed: 預設載入狀態 (Checkbox 未勾選)
    
    state Collapsed {
        Header: 頂部常駐 Bar (Logo + Space Name)
        ToggleBtn: 漢堡圖示 [☰]
        Drawer: 抽屜選單隱藏 (display: none / max-height: 0)
    }
    
    Collapsed --> Expanded: 使用者觸控點擊 [☰] (觸發 label 勾選 checkbox)
    
    state Expanded {
        HeaderActive: 頂部常駐 Bar (高亮)
        CloseBtn: 關閉圖示 [✕]
        DrawerActive: 抽屜選單垂直滑出展開 (展示所有導覽連結, 高度 44px+)
    }
    
    Expanded --> Collapsed: 使用者點擊 [✕] 或點擊任一導航項目
```

### 7. `system-backup-restore` 全系統備份與還原防鎖死架構

#### 7.1 升級備份打包與 SQL Dump 產出流程 (Backup Export Flow with SQL Dump)
```mermaid
flowchart TD
    Start([管理員點擊「立即匯出全系統備份包」]) --> CheckAuth{檢查是否為 SuperUser?}
    CheckAuth -- 否 --> Deny[拒絕存取並顯示 403 錯誤]
    CheckAuth -- 是 --> QueryDB[從資料庫讀取系統所有實體資料]
    QueryDB --> GenJSON[序列化為跨資料庫標準結構化 JSON 資料: manifest.json + data.json]
    QueryDB --> GenSQL[產生單一整合 SQL 傾印檔: jtrac-dump.sql]
    subgraph "jtrac-dump.sql 產出細節"
        GenSQL --> DDLPart[生成通用 ANSI DDL 建表語法與 MySQL/PostgreSQL/HSQLDB 方言註解]
        DDLPart --> DMLPart[依 14 張表拓撲外鍵相依順序生成 ANSI INSERT INTO 語法]
        DMLPart --> SeqPart[生成 Sequence / Auto-Increment 校準重置提示註解]
    end
    GenJSON --> ScanAttach[掃描 attachments/ 實體附件目錄]
    SeqPart --> ZipBundle[將 JSON、jtrac-dump.sql 與實體附件壓縮為單一 ZIP]
    ScanAttach --> ZipBundle
    ZipBundle --> StreamDownload[輸出串流供瀏覽器下載備份包]
    StreamDownload --> End([完成備份匯出])
```

#### 7.2 還原與防鎖死防護流程 (Restore & Anti-Lockout Shield Flow)
```mermaid
flowchart TD
    StartRestore([管理員上傳備份 ZIP 檔案]) --> VerifyZip{校驗 ZIP 結構與資料完整性}
    VerifyZip -- 失敗 --> ShowError[還原終止並顯示錯誤訊息]
    VerifyZip -- 成功 --> CaptureOp[暫存當前操作者憑證資訊: 帳號/密碼雜湊/權限]
    CaptureOp --> TakeSnapshot[自動建立還原前緊急快照 Safety Snapshot]
    TakeSnapshot --> ClearDB[交易內清空現有資料庫與附件目錄]
    ClearDB --> RestoreData[匯入專案設定、工單資料、歷史討論串與實體附件]
    RestoreData --> CheckUser{備份檔中是否存在當前登入之操作者帳號?}
    CheckUser -- 存在 --> MergeUser[保留該帳號關聯，但強制保留現行密碼雜湊與 ROLE_ADMIN]
    CheckUser -- 不存在 --> InjectUser[將當前操作者帳號主動注入為 SuperUser]
    MergeUser --> Reindex[背景自動觸發 Lucene 全文檢索索引重建]
    InjectUser --> Reindex
    Reindex --> SuccessNotice[提示還原成功，管理員 Session 保持有效無中斷]
    SuccessNotice --> EndRestore([還原完成])
```

### 8. `attachment-partitioning-and-indexing` 附件專案隔離儲存與全文檢索架構

#### 8.1 四階段全自動升級流水線
```mermaid
sequenceDiagram
    autonumber
    participant Boot as JTrac 啟動器
    participant DBMigrator as HsqldbDatabaseMigrator
    participant Hibernate as Hibernate LocalSessionFactory
    participant AttMigrator as AttachmentStorageMigrator
    participant Reindexer as Async Lucene Reindexer

    Boot->>DBMigrator: 檢查 data/db 檔案
    opt 偵測到 HSQLDB 1.8
        DBMigrator->>DBMigrator: 建立 backup-hsqldb-1.8-<timestamp>/ 備份
        DBMigrator->>DBMigrator: 轉譯腳本並無痛升級至 2.x
    end
    Boot->>Hibernate: createSchema() 檢查並補入缺少之 config 參數
    Boot->>AttMigrator: migrate(jtracHome, dao)
    opt 未遷移過平鋪附件
        AttMigrator->>AttMigrator: 查詢附件 filePrefix -> spaceId 關聯表
        AttMigrator->>AttMigrator: 搬移平鋪檔案至 attachments/{spaceId}/
        AttMigrator->>AttMigrator: 將無關聯孤兒檔案隔離至 attachments/0_ORPHAN/
        AttMigrator->>AttMigrator: 建立 .attachment_migrated 標記檔
    end
    Boot->>Reindexer: 啟動背景非同步執行緒重建 Lucene 全量索引
```

#### 8.2 新增附件非同步佇列抽取與雙軌查檔安全網
```mermaid
flowchart TD
    subgraph UploadFlow [新附件上傳流程]
        UploadReq([使用者上傳附件]) --> SaveDB[儲存 Attachment & History 資料庫記錄]
        SaveDB --> WriteDisk[寫入磁碟: attachments/{spaceId}/{prefix}_{filename}]
        WriteDisk --> Resp[極速回應 HTTP 成功]
        WriteDisk --> Enqueue[派送任務至 ExecutorService 背景佇列]
        Enqueue --> Extract[文字抽取器: SmartCharsetDetector + PDFBox/OpenXML]
        Extract --> Index[寫入或更新 Lucene 索引庫]
    end

    subgraph DownloadFlow [雙軌查檔安全網 Dual-Read Fallback]
        Req([使用者請求下載附件]) --> CheckSub{專案目錄是否存在?}
        CheckSub -- 是 --> ServeSub[讀取 attachments/{spaceId}/... 提供下載]
        CheckSub -- 否 --> CheckRoot{根目錄是否存在?}
        CheckRoot -- 是 --> ServeRoot[降級讀取 attachments/... 提供下載]
        CheckRoot -- 否 --> CheckOrphan{0_ORPHAN 目錄是否存在?}
        CheckOrphan -- 是 --> ServeOrphan[讀取 attachments/0_ORPHAN/... 提供下載]
        CheckOrphan -- 否 --> Err404[拋出 FileNotFoundException]
    end
```

---

*最後自動更新時間：2026-09-09*
