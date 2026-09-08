# OpenSpec 專案主規格文件清單 (Master Specs)

本文件自動同步匯總 JTrac 專案中所有已歸檔與實作之規格說明書（Specifications）。

---

## 規格目錄 (Capabilities Index)

| 規格代碼 (Capability) | 名稱與範疇 | 目的說明 (Purpose) | 狀態 |
|---|---|---|---|
| [`i18n-resources`](i18n-resources/spec.md) | 多國語系資源與過濾規範 | 定義 JTrac 專案中多國語系資源檔案之 UTF-8 編碼規範與 Maven 資源處理隔離規則，確保在不同作業系統與 JDK 環境下建置及執行時皆能正確處理字元編碼，並避免框架變數被構建工具誤替換。 | Active |
| [`build-documentation`](build-documentation/spec.md) | 多語系建置與編譯文件規範 | 規範 JTrac 專案之多語系建置與編譯技術文件結構，確保全球開發者皆能在其母語或慣用語言環境下，清楚理解 Maven 建置指令、依賴套件本機快取下載機制，以及 WAR 封裝檔內部依賴整合原理。 | Active |
| [`html-exporter`](html-exporter/spec.md) | 獨立命令列 HTML 討論串匯出工具 | 定義獨立命令列工具 `jtrac-exporter.jar` 之功能規格與資料處理邏輯。透過指定 JDBC 連線字串存取遠端或本地資料庫，相容 HSQLDB 1.8 歷史庫，在零舊版依賴下將議題、討論串歷程與附件匯出為支援離線明暗主題與 5 國多語系之高對比靜態 HTML 報表。 | Active |

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
    F --> G
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
    C --> H[Maven 編譯與依賴快取說明]
    D --> H
    E --> H
    F --> H
    G --> H
    H --> I[WAR WEB-INF/lib 第三方套件封裝解析]
```

### 3. `html-exporter` 命令列 JDBC 討論串匯出架構

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

---

*最後自動更新時間：2026-09-08*
