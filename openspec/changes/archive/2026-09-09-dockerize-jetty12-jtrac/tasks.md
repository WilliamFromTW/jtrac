## 1. 建置基礎架構與映像檔配置

- [x] 1.1 建立 `docker/.dockerignore` 排除不必要的本機建置產物、IDE 設定與快取檔案，驗證包含 `.git`, `target`, `.idea` 等排除項目
- [x] 1.2 建立 `docker/Dockerfile` 實作多階段建置（Builder: `maven:3.9-eclipse-temurin-17`，Runtime: `jetty:12-jre17-eclipse-temurin` 啟用 `ee8-deploy` 模組並安裝完整多國語系字型），並驗證語法結構
- [x] 1.3 建立 `docker/entrypoint.sh` 實作啟動流程（自動校正 `/jtrac-data` 擁有者為 `jetty:jetty`、支援環境變數渲染 `jtrac.properties`、使用 `gosu` 降權切換為 `jetty` 執行），驗證 Shell 語法正確

## 2. 跨平台啟動腳本與編排設定

- [x] 2.1 建立 `docker/docker-compose.yml` 定義 `jtrac` 服務、8888 埠映射、命名 Volume `jtrac_data` 與健康檢查配置，驗證 YAML 格式合法
- [x] 2.2 建立 Linux/macOS 輔助腳本 `docker/build.sh` 與 `docker/run.sh`，封裝標準建置與啟動指令，驗證 Shell 腳本語法
- [x] 2.3 建立 Windows 輔助批次檔 `docker/build.bat` 與 `docker/run.bat`，封裝標準建置與啟動指令，驗證批次指令語法

## 3. 多語系文件與技術指南更新

- [x] 3.1 建立 `docker/README.md` 說明原生 Docker 建置步驟、資料庫環境變數與 Volume 持久化配置
- [x] 3.2 同步更新多語系建置指南 `docs/build/BUILD_*.md`（8 國語系：en, zh-TW, zh-CN, es, de, fr, ja, vi）新增 Docker 容器化建置與執行章節
- [x] 3.3 同步更新多語系系統管理指南 `docs/admin/ADMIN_GUIDE_*.md`（8 國語系）新增 Docker 容器運維、Volume 備份與外部資料庫連線指引

## 4. 驗證與規格審核

- [x] 4.1 執行 `openspec validate dockerize-jetty12-jtrac --strict` 確保規格定義與任務完全符合 OpenSpec 驗證規範
- [x] 4.2 檢驗 `docker/` 目錄內之各項檔案完整性，確保所有指令路徑皆符合設計規範

