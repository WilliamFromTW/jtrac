# JTrac Docker 容器化建置與部署 (Jetty 12.x + Eclipse Temurin 17+)

[English](README.md) | [繁體中文](README_zh-TW.md)

本目錄提供 JTrac 現代化版本之原生 Docker 多階段建置與容器化執行環境。

---

## 特性亮點 (Features)

- **現代化基礎環境**：採用官方 `jetty:12-jre17-eclipse-temurin` 搭配 `ee8-deploy` 與 `ee8-webapp` 模組，原生支援 Servlet 4.0 (`javax.servlet`)。
- **多階段建置 (Multi-stage Build)**：使用 `maven:3.9-eclipse-temurin-17` 自動自原始碼編譯 `jtrac.war`，無需在本機預先安裝 JDK 或 Maven。
- **完整多國語系字型支援**：內建 `fontconfig`、`fonts-noto-cjk`（中日韓）、`fonts-noto-core`（越南語等音標字元）、`fonts-dejavu-core`（歐系重音字元），確保全文檢索字元抽取與報表繪圖 0% 缺字或亂碼。
- **權限自動校正與安全降權**：Entrypoint 啟動時自動檢查並修復掛載 Volume `/jtrac-data` 為 `jetty:jetty`，並透過 `gosu` 降權至非 root 帳號（UID 999）執行，安全且免手動 chown。

---

## 快速上手 (Quick Start)

### 方式一：原生 Docker 指令 (推薦)

進入 `docker` 目錄，以專案根目錄（`..`）作為 Build Context 進行建置：

```bash
cd docker
docker build -f Dockerfile -t jtrac:latest ..
docker run -d -p 8888:8080 -v jtrac_data:/jtrac-data --name jtrac jtrac:latest
```

服務啟動後，使用瀏覽器開啟：`http://localhost:8888/`
預設管理員帳密為：`admin` / `admin`

---

### 方式二：跨平台輔助腳本

本目錄提供針對不同作業系統之一鍵腳本：

- **Windows**：
  ```cmd
  cd docker
  build.bat
  run.bat
  ```

- **Linux / macOS**：
  ```bash
  cd docker
  chmod +x *.sh
  ./build.sh
  ./run.sh
  ```

---

### 方式三：直接運行 Docker Hub 官方映像檔

亦可直接拉取並執行已發佈於 Docker Hub 之官方映像檔：
**[https://hub.docker.com/r/inmethod/jtrac](https://hub.docker.com/r/inmethod/jtrac)**

```bash
docker run -d -p 8888:8080 -v jtrac_data:/jtrac-data --name jtrac inmethod/jtrac:latest
```
