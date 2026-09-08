## 系統架構與網頁匯出流程圖

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

## ADDED Requirements

### Requirement: 網頁導覽列匯出按鈕與安全控管
系統 MUST 於全站共用之頂部導覽列（`HeaderPanel`）提供「📦 匯出 HTML」連結，且僅限已通過驗證或具備合法存取權限之使用者操作。

#### Scenario: 具權限使用者在導覽列看見匯出入口
- **WHEN** 已登入之使用者瀏覽 JTrac 任何頁面
- **THEN** 頂部導覽列呈現多語系之「📦 匯出 HTML」選項，點擊後導向匯出確認設定頁面

#### Scenario: 未登入使用者存取匯出頁面 (Guardrail)
- **WHEN** 未登入或訪客身分嘗試直接透過 URL 存取匯出頁面
- **THEN** 系統阻擋請求並自動重新導向至登入頁面，防止未授權資料外洩

---

### Requirement: 匯出確認對話頁面（模式二）與免責聲明
系統 MUST 在使用者進行下載前，提供專屬之設定確認頁面（`HtmlExportPage`），供使用者挑選匯出範圍、介面語系與附件選項，並以醒目區塊揭露內容無法翻譯之限制。

#### Scenario: 匯出設定與參數選擇
- **WHEN** 使用者進入匯出確認頁面
- **THEN** 介面提供「專案空間範圍（全部空間 / 目前空間）」、「UI 框架語系（繁中、英文、簡中、日語、越語）」與「包含實體附件打包」核取方塊

#### Scenario: 翻譯限制免責聲明顯示 (Guardrail)
- **WHEN** 檢視匯出設定頁面
- **THEN** 頁面以醒目警告框明確告知：「語系設定僅套用於網頁介面、欄位名稱與狀態標籤（UI 框架）；議題歷史主題、詳細描述與留言回覆為原始記錄，無法自動翻譯。」

---

### Requirement: 伺服器即時串流打包 ZIP 下載
系統 MUST 透過連線池直接在行內（In-Process）提取資料庫，並將靜態 HTML 文件與實體附件即時壓縮為 `.zip` 串流推送至客戶端，避免檔案鎖定與伺服器磁碟額外佔用。

#### Scenario: 使用者完成確認並開始下載
- **WHEN** 使用者於設定確認頁面點擊「開始匯出並下載 ZIP」按鈕
- **THEN** 伺服器即時以串流方式生成 ZIP 壓縮檔（預設命名為 `jtrac-export-YYYYMMDD.zip`），瀏覽器直接彈出檔案下載對話框

#### Scenario: 線上環境避開 HSQLDB 檔案鎖定
- **WHEN** 系統於 Jetty / Tomcat 等 Servlet 容器運行中執行網頁匯出
- **THEN** 匯出引擎直接透過已配置之 Spring `DataSource` 取得資料庫連線，絕不另行開啟實體檔案避免 `Database lock acquisition failure` 衝突

#### Scenario: 實體附件缺失或空專案空間之容錯 (Guardrail)
- **WHEN** 歷史紀錄中記錄之附件實體檔案已損毀或遺失，或選定之空間無任何議題
- **THEN** 系統在 ZIP 內生成對應之空狀態提示或缺少附件之警示 HTML，維持串流順暢完成下載，絕不發生 500 伺服器內部錯誤

---

### Requirement: 既有獨立命令列工具完整相容
既有之獨立 CLI 工具（`tools/jtrac-exporter`）之原始程式碼、Maven 建置指令與命令列呼叫參數 MUST 維持 100% 可用性與相容性。

#### Scenario: 使用者由命令列單獨編譯與執行
- **WHEN** 開發者於終端機執行 `mvn clean package -f tools/jtrac-exporter/pom.xml` 並以 `java -jar tools/jtrac-exporter.jar ...` 執行匯出
- **THEN** 工具正常編譯打包且所有 CLI 參數（`--db-url`, `--attachments-dir`, `--lang`, `--out`, `--space`）行為維持不變
