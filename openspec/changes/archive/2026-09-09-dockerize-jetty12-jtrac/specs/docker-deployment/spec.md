## Purpose

本功能提供基於 Eclipse Temurin 17 與官方 Jetty 12 的原生多階段 Docker 建置、開箱即用容器化執行環境、多國語系字型支援與 Volume 資料持久化機制，讓使用者能一鍵完成 JTrac 系統之容器化封裝與部署。

## ADDED Requirements

### Requirement: 多階段 Dockerfile 建置與 WAR 部署
系統 SHALL 提供多階段 Dockerfile，於第一階段（Builder）採用 `maven:3.9-eclipse-temurin-17` 自動編譯打包產出 `target/jtrac.war`，於第二階段（Runtime）採用官方 `jetty:12-jre17-eclipse-temurin` 映像檔，並透過 `--add-modules=ee8-deploy` 啟用 EE8 執行環境以支援 Servlet 4.0 (`javax.servlet`) 規格。

#### Scenario: 成功編譯與打包映像檔
- **WHEN** 使用者於 `docker/` 目錄執行 `docker build -f Dockerfile -t jtrac:latest ..`
- **THEN** Docker 順利完成 Maven 跳過測試編譯，並將 `jtrac.war` 部署至 Jetty 之 `webapps/ROOT.war`，生成可獨立運行的輕量化映像檔。

### Requirement: 多國語系字型環境支援
容器執行環境 SHALL 安裝系統字型管理套件 `fontconfig`，以及完整之多國語系字型包（涵蓋中日韓 `fonts-noto-cjk`、歐系字母 `fonts-dejavu-core` 與越南文聲調 `fonts-noto-core`），並於建置期間建立系統字型快取。

#### Scenario: PDF 文字抽取與報表多語系渲染
- **WHEN** 系統於容器內執行附件 PDF/Office 檔案之文字抽取或全文檢索索引重建
- **THEN** Java 繪圖與字型子系統能正確找到 CJK、歐洲字母及越南文聲調之字形對照，0% 發生字型映射缺失或方框缺字（tofu）。

### Requirement: 容器身分安全性與 Volume 權限自適應
容器啟動流程 SHALL 具備 `entrypoint.sh` 自動適配機制，於開機時以 root 權限檢查持久化目錄 `/jtrac-data`，自動修復該目錄及其內部檔案之所有權為 `jetty:jetty`（UID 999），隨後自動降權切換為 `jetty` 非 root 使用者啟動 Jetty 伺服器。

#### Scenario: 本機掛載目錄啟動無權限錯誤
- **WHEN** 使用者以本機 Volume（如 `-v jtrac_data:/jtrac-data` 或 `-v /host/path:/jtrac-data`）啟動容器
- **THEN** 系統開機時自動校正目錄擁有者，Jetty 在 `jetty` 使用者身分下具備對 `/jtrac-data` 的完整讀寫權限，不拋出 Permission Denied 例外。

### Requirement: 資料庫連線環境變數自動注入
`entrypoint.sh` SHALL 在啟動 Jetty 前檢查系統環境變數：若偵測到 `DATABASE_URL` 存在，自動將相關連線屬性（`database.driver`、`database.url`、`database.username`、`database.password`、`hibernate.dialect`）渲染寫入 `/jtrac-data/jtrac.properties`；若未偵測到環境變數且目錄下無設定檔，則允許系統使用預設之內建 HSQLDB 2.x。

#### Scenario: 預設啟動內建 HSQLDB
- **WHEN** 使用者未設定任何資料庫環境變數啟動全新容器
- **THEN** 系統自動在 `/jtrac-data/db/` 建立內建 HSQLDB 2.x 資料庫並成功初始化資料庫結構。

#### Scenario: 注入外部資料庫連線設定
- **WHEN** 使用者啟動容器時帶入 `-e DATABASE_URL=jdbc:mysql://db:3306/jtrac` 及帳號密碼
- **THEN** 容器自動將該連線參數寫入 `jtrac.properties`，JTrac 啟動時直接連線至指定之外部資料庫。

### Requirement: 跨平台操作腳本與 Compose 配置
`docker/` 目錄 SHALL 提供標準 `docker-compose.yml`、Linux/macOS 腳本（`build.sh`、`run.sh`）與 Windows 批次檔（`build.bat`、`run.bat`），封裝標準建置與啟動命令。

#### Scenario: 透過腳本一鍵建置與啟動
- **WHEN** 使用者進入 `docker/` 目錄並執行 `build.bat`（或 `build.sh`）及 `run.bat`（或 `run.sh`）
- **THEN** 系統自動以正確之 Context 路徑（`..`）建置映像檔，並以正確之 Port（8888 對映 8080）與 Volume（`jtrac_data:/jtrac-data`）啟動容器服務。
