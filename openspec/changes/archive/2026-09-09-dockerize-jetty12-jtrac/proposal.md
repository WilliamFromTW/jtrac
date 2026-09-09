## Why

JTrac 歷經現代化架構重構（升級至 Java 11/17、Spring 5.3、Hibernate 5.6 與 Wicket 9）後，系統已具備長期運行的穩固基礎。然而，目前系統部署仍高度依賴本機手動配置 JDK、Maven 與 Servlet 容器（如 Jetty/Tomcat），對於跨平台伺服器遷移、雲端部署、CI/CD 自動化建置及無伺服器/NAS 環境存在進入門檻。

為此，本專案需要一個原生、標準且開箱即用的 Docker 容器化方案，結合 Adoptium 官方維護之 Eclipse Temurin 17 JRE 與官方 Jetty 12 伺服器，讓任何開發者與系統管理者自 GitHub 下載專案後，僅需一行原生 Docker 指令即可完成應用編譯打包與容器化啟動。

## What Changes

- **建立專屬 Docker 建置目錄 (`docker/`)**：獨立收納容器化所需之各項建置與運行配置，避免污染專案根目錄。
- **多階段極小化建置 (Multi-stage Build)**：
  - **Stage 1 (Builder)**：採用 `maven:3.9-eclipse-temurin-17`，於容器內部執行 `mvn clean package -DskipTests -B`，免除宿主機安裝 Maven 之依賴。
  - **Stage 2 (Runtime)**：採用官方 `jetty:12-jre17-eclipse-temurin`，啟用 `ee8-deploy` 模組原生相容 Servlet 4.0 (`javax.servlet`)。
- **多國語系字型環境整合**：於容器底層安裝 `fontconfig`、`fonts-noto-cjk`（中日韓）、`fonts-noto-core` 與 `fonts-dejavu-core`（歐系重音與越南文聲調），根除 PDF 與工單附件文字抽取時缺字與亂碼問題。
- **權限防呆與 Volume 自動適配 (`entrypoint.sh`)**：啟動時由 root 檢查掛載目錄權限，確保 `/jtrac-data` 擁有者為 `jetty:jetty` (UID 999) 後自動降權執行，兼顧安全與本機 Volume 掛載即用性。
- **資料庫連線雙軌支援**：支援透過環境變數（`DATABASE_URL`、`DATABASE_USERNAME` 等）自動渲染連線設定，未傳入時自動無縫啟用內建 HSQLDB。
- **完整跨平台輔助腳本**：提供 `build.sh`、`build.bat`、`run.sh`、`run.bat` 與標準 `docker-compose.yml`。

## Capabilities

### New Capabilities
- `docker-deployment`: 提供基於 Eclipse Temurin 17 與 Jetty 12 的原生多階段 Docker 建置、開箱即用容器化執行環境、多國語系字型支援與 Volume 資料持久化。

### Modified Capabilities
<!-- 本次變更為純容器化部署新增，既有應用程式行為無需求變更 -->

## Impact

- **專案結構**：新增 `docker/` 目錄，包含 `Dockerfile`、`docker-compose.yml`、`entrypoint.sh`、`.dockerignore`、快速建置腳本與說明文件。
- **系統相依性**：不影響本機既有的 Jetty 10 / Tomcat 9 手動部署方式，提供平行之容器化方案。
- **文件影響**：同步更新 `docs/build/` 與 `docs/admin/` 8 種語系文件，新增 Docker 快速上手章節。
