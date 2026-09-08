# JTrac (Enhanced Fork)

[![Java](https://img.shields.io/badge/Java-8%20%7C%2011-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](license.txt)
[![Fork From](https://img.shields.io/badge/Fork%20From-JTrac%202.3.3-blue)](https://jtrac.info)
[![OpenSpec](https://img.shields.io/badge/OpenSpec-v1.12.0-brightgreen.svg)](openspec/specs/README.md)

本專案為 [JTrac 2.3.3 (https://jtrac.info)](https://jtrac.info) 的現代化增強 Fork 版本。致力於提供更輕量、高相容性、具備離線靜態歸檔與現代化 UI 體驗的 Issue Tracking 系統。本專案開發過程中使用 OpenSpec v1.12.0 規格驅動流程，並由 Antigravity 1.1.27 輔助開發、架構重構與品質把關。

---

## 多語系建置手冊 / Multilingual Build Guides

| 語言 / Language | 建置指南 / Build Guide |
|---|---|
| **繁體中文 (Traditional Chinese)** | [繁體中文編譯與建置完整指南](docs/build/BUILD_zh-TW.md) |
| **English** | [English Build & Compilation Guide](docs/build/BUILD_en.md) |
| **简体中文 (Simplified Chinese)** | [简体中文编译与构建完整指南](docs/build/BUILD_zh-CN.md) |
| **日本語 (Japanese)** | [日本語ビルド・コンパイル詳細ガイド](docs/build/BUILD_ja.md) |
| **Tiếng Việt (Vietnamese)** | [Hướng dẫn biên dịch và đóng gói Tiếng Việt](docs/build/BUILD_vi.md) |

---

## 本 Fork 版本重點更新 (Changelog & Major Updates)

### 1. 模組清理：完全移除 Wiki (Removed Wiki Module)
- 徹底移除非核心且過時的 Wiki 模組代碼、頁面元件及導航列圖示，精簡系統架構，專注於高效穩定的 Issue 追蹤核心。

### 2. 新增功能 (New Features)
- **Web 端批次 HTML 匯出與 ZIP 下載**：
  - 於主導航列整合「匯出 HTML」功能，使用者可於網頁直接勾選複數專案空間（支援「全選 / 全部取消」），線上即時打包下載完整靜態 HTML 討論串與附件 ZIP 檔。
- **嚴格空間權限守門員 (Permission Guardrails)**：
  - 前後端雙層嚴格把關：一般使用者僅能檢視、選擇並下載自己具有成員權限的專案空間（系統管理員維持全域權限），具備空清單阻擋機制與友善提醒。
- **獨立命令列 HTML 匯出工具 (`tools/jtrac-exporter.jar`)**：
  - 無需啟動 Web 伺服器，直接透過 JDBC 連線字串將資料庫匯出為離線響應式 HTML 報表。
  - 支援多語系靜態報表、附件直接連結，並內建 100% 離線純 CSS 深色主題 (Dark Mode 切換開關)，完全不依賴外部網路或 CDN。
- **多語系支援完整補完 (Full i18n Coverage)**：
  - 重新校訂並補齊繁體中文 (`zh_TW`) 與簡體中文 (`zh_CN`) 翻譯。
  - 補齊各語系遺漏之狀態與按鈕鍵值（如專案空間啟用狀態 `space_form.isActive` 等）。

### 3. Bug 修復 (Bug Fixes)
- **導航列按鈕與標籤置中問題 (Navigation Header Centering)**：
  - 修復導航列中「儀表板、搜尋、匯出、選項、登出、帳號名稱」等按鈕文字與圖示垂直基準線不一致及靠右偏移問題。
  - 全面採用 Flexbox inline-flex 置中對齊，按鈕加上 3px 微圓角與 hover 互動底色，資訊徽章標籤獨立呈現。
- **歷史討論串歷程優化 (Thread History Cleanup)**：
  - 修復匯出 HTML 時討論串第一筆總是重複出現無任何備註的「Open」冗餘記錄，僅保留具備實質意義的留言與狀態變更歷程。
- **解決 Hibernate `LazyInitializationException`**：
  - 修復空間中繼資料 (`space.metadata`) 延遲載入產生的 Session 關閉異常，改採預先積極載入 (Eager Fetch / Initialize) 保障穩定運行。
- **解決 Maven UTF-8 資源過濾與二進位圖檔損壞**：
  - 移除過時的 `native2ascii` 轉換，全專案規範化純 UTF-8 編碼。
  - 修正 Maven resource filtering 誤將二進位檔案（gif、png、jar）當文字過濾導致圖檔損壞之問題。

---

## 開發技術與架構 (Technologies & Architecture)

- **核心語言**：Java 1.8 / 11
- **Web 框架**：Apache Wicket 1.3
- **IoC 與控制反轉**：Spring Framework 2.5
- **ORM 與資料庫**：Hibernate 3 / HSQLDB（內建）、支援 MySQL, PostgreSQL, MS SQL, Oracle
- **建置工具**：Apache Maven 3.9+（輸出 WAR 套件）
- **輔助工具與開發規格**：OpenSpec v1.12.0、Antigravity 1.1.27
- **編碼規範**：全系統 100% UTF-8

---

## 快速開始：編譯與部署 (Quick Start)

### 1. 編譯主程式 (WAR)
```bash
# 編譯主程式碼
mvn compile

# 快速打包 WAR 檔（跳過測試）
mvn package -DskipTests
```
產出套件位於：`target/jtrac.war`，可直接部署於 Jetty 或 Tomcat。

### 2. 建置獨立 HTML 匯出工具 (CLI)
```bash
# 建置獨立可執行檔
mvn clean package -f tools/jtrac-exporter/pom.xml -DskipTests
# 產出位置: tools/jtrac-exporter.jar

# 執行本地 HSQLDB 匯出範例
java -jar tools/jtrac-exporter.jar \
  --db-url="jdbc:hsqldb:file:./data/db/jtrac;shutdown=true;readonly=true" \
  --attachments-dir="./data/attachments" \
  --out="./export-output" \
  --lang=zh-TW
```

---

## 專案規格文件 (Specifications)

本專案採用 OpenSpec 規格驅動開發：
- [專案主規格目錄](openspec/specs/README.md)
- [HTML Exporter 規格文件](openspec/specs/html-exporter/spec.md)
- [語系與資源規格文件](openspec/specs/i18n-resources/spec.md)
- [建置手冊規格文件](openspec/specs/build-documentation/spec.md)
- [專案開發鐵律](.agents/AGENTS.md)

---

## 授權條款 (License)

JTrac 為開源軟體，遵循 [Apache Software License, Version 2.0](license.txt)。
