# JTrac Docker 容器化建置與部署 (Jetty 12 + Eclipse Temurin 17)

本目錄提供 JTrac 現代化版本之原生 Docker 多階段建置與容器化執行環境。

---

## 特性亮點 (Features)

- **現代化基礎環境**：採用官方 `jetty:12-jre17-eclipse-temurin` 搭配 `ee8-deploy` 與 `ee8-webapp` 模組，原生支援 Servlet 4.0 (`javax.servlet`)。
- **多階段建置 (Multi-stage Build)**：使用 `maven:3.9-eclipse-temurin-17` 自動自原始碼編譯 `jtrac.war`，無需在本機預先安裝 JDK 或 Maven。
- **完整多國語系字型支援**：內建 `fontconfig`、`fonts-noto-cjk`（中日韓）、`fonts-noto-core`（越南語等音標字元）、`fonts-dejavu-core`（歐系重音字元），確保全文檢索字元抽取與報表繪圖 0% 缺字或亂碼。
- **權限自動校正與安全降權**：Entrypoint 啟動時自動檢查並修復掛載 Volume `/jtrac-data` 為 `jetty:jetty`，並透過 `gosu` 降權至非 root 帳號（UID 999）執行，安全且免手動 chown。
- **資料庫動態注入**：支援透過環境變數注入外部 MySQL / PostgreSQL / Oracle 連線設定，自動渲染至 `/jtrac-data/jtrac.properties`；未指定時預設使用內建升級版 HSQLDB 2.x。

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

### 方式三：Docker Compose

```bash
cd docker
docker compose up -d
```

停止容器：
```bash
docker compose down
```

---

## 資料持久化 (Data Persistence)

JTrac 所有系統設定、使用者資料庫與上傳附件皆統一持久化於 `/jtrac-data`：

| 目錄 / 檔案路徑 | 說明 |
|---|---|
| `/jtrac-data/jtrac.properties` | 系統資料庫連線驅動與參數設定檔 |
| `/jtrac-data/db/` | 內建 HSQLDB 2.x 資料庫實體檔案 |
| `/jtrac-data/attachments/` | 依專案 ID 分區儲存之工單歷史附件 |
| `/jtrac-data/indexes/` | Lucene 全文檢索索引庫（可由後台重建） |
| `/jtrac-data/backups/` | 全系統備份與還原安全快照 |

### 掛載本機宿主目錄範例：
```bash
docker run -d \
  -p 8888:8080 \
  -v /path/to/host/data:/jtrac-data \
  --name jtrac \
  jtrac:latest
```
*(無論宿主機目錄擁有者為何，Entrypoint 均會自動進行權限修復)*

---

## 外部資料庫連線 (External Database via Environment Variables)

若需連接外部關聯式資料庫，只需在 `docker run` 或 `docker-compose.yml` 中傳入環境變數，容器開機時會自動生成對應的 `jtrac.properties`：

| 環境變數 | 說明 | 範例值 (MySQL) |
|---|---|---|
| `DATABASE_URL` | JDBC 連線字串 | `jdbc:mysql://db:3306/jtrac?useUnicode=true&characterEncoding=UTF-8` |
| `DATABASE_DRIVER` | JDBC 驅動類別名稱 | `com.mysql.cj.jdbc.Driver` |
| `DATABASE_USERNAME` | 資料庫登入使用者名稱 | `jtrac` |
| `DATABASE_PASSWORD` | 資料庫密碼 | `secret123` |
| `HIBERNATE_DIALECT` | (選填) Hibernate 方言類別 | `org.hibernate.dialect.MySQL8Dialect` |

### 啟動外部資料庫容器範例：
```bash
docker run -d \
  -p 8888:8080 \
  -v jtrac_data:/jtrac-data \
  -e DATABASE_URL="jdbc:mysql://192.168.1.50:3306/jtrac?useUnicode=true&characterEncoding=UTF-8" \
  -e DATABASE_DRIVER="com.mysql.cj.jdbc.Driver" \
  -e DATABASE_USERNAME="jtrac" \
  -e DATABASE_PASSWORD="password" \
  -e HIBERNATE_DIALECT="org.hibernate.dialect.MySQL8Dialect" \
  --name jtrac \
  jtrac:latest
```

---

## JVM 參數自訂 (JVM Options)

預設 JVM 參數為：
`-Djtrac.home=/jtrac-data -Dfile.encoding=UTF-8 -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=25.0`

可透過傳遞 `JAVA_OPTIONS` 環境變數自訂：
```bash
docker run -d \
  -p 8888:8080 \
  -v jtrac_data:/jtrac-data \
  -e JAVA_OPTIONS="-Xms1g -Xmx2g -Djtrac.home=/jtrac-data -Dfile.encoding=UTF-8" \
  --name jtrac \
  jtrac:latest
```
