# JTrac (增強版 Fork)

[English](README.md) | [繁體中文](README_zh-TW.md) | [简体中文](README_zh-CN.md) | [日本語](README_ja.md) | [Tiếng Việt](README_vi.md) | [Deutsch](README_de.md) | [Español](README_es.md) | [Français](README_fr.md)

---

[![Java](https://img.shields.io/badge/Java-11%20%7C%2017-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](license.txt)
[![Fork From](https://img.shields.io/badge/Fork%20From-JTrac%202.3.3-blue)](https://jtrac.info)
[![Version](https://img.shields.io/badge/Version-2.3.3--2.0.0-blue.svg)](pom.xml)
[![Wicket](https://img.shields.io/badge/Wicket-9.16.0-blue.svg)](https://wicket.apache.org/)
[![Spring](https://img.shields.io/badge/Spring-5.3.37-brightgreen.svg)](https://spring.io/)
[![OpenSpec](https://img.shields.io/badge/OpenSpec-v1.12.0-brightgreen.svg)](openspec/specs/README.md)

本專案為 [JTrac 2.3.3 (https://jtrac.info)](https://jtrac.info) 的現代化增強 Fork 版本。致力於提供更輕量、高相容性、具備離線靜態歸檔、現代化 UI 與最新企業級安全規格的 Issue Tracking 系統。本專案開發過程中使用 OpenSpec v1.12.0 規格驅動流程，並由 Antigravity 輔助架構重構與品質把關。

---

## 多語系建置手冊 / Multilingual Build Guides

| 語言 / Language | 建置指南 / Build Guide |
|---|---|
| **繁體中文 (Traditional Chinese)** | [繁體中文編譯與建置完整指南](docs/build/BUILD_zh-TW.md) |
| **English** | [English Build & Compilation Guide](docs/build/BUILD_en.md) |
| **简体中文 (Simplified Chinese)** | [简体中文编译与构建完整指南](docs/build/BUILD_zh-CN.md) |
| **日本語 (Japanese)** | [日本語ビルド・コンパイル詳細ガイド](docs/build/BUILD_ja.md) |
| **Tiếng Việt (Vietnamese)** | [Hướng dẫn biên dịch và đóng gói Tiếng Việt](docs/build/BUILD_vi.md) |
| **Deutsch (German)** | [Deutscher Kompilierungs- und Build-Leitfaden](docs/build/BUILD_de.md) |
| **Español (Spanish)** | [Guía de compilación y construcción en español](docs/build/BUILD_es.md) |
| **Français (French)** | [Guide complet de compilation et de construction en français](docs/build/BUILD_fr.md) |

---

## 多語系系統管理指南 / Multilingual Administrator Guides

| 語言 / Language | 管理者指南 / Admin Guide |
|---|---|
| **繁體中文 (Traditional Chinese)** | [繁體中文系統管理者完整指南](docs/admin/ADMIN_GUIDE_zh-TW.md) |
| **English** | [English Administrator & System Configuration Guide](docs/admin/ADMIN_GUIDE_en.md) |
| **简体中文 (Simplified Chinese)** | [简体中文系统管理员完整指南](docs/admin/ADMIN_GUIDE_zh-CN.md) |
| **日本語 (Japanese)** | [日本語システム管理者総合ガイド](docs/admin/ADMIN_GUIDE_ja.md) |
| **Tiếng Việt (Vietnamese)** | [Hướng dẫn Quản trị viên và Cấu hình Hệ thống](docs/admin/ADMIN_GUIDE_vi.md) |
| **Deutsch (German)** | [Deutscher Systemadministrator- und Konfigurationsleitfaden](docs/admin/ADMIN_GUIDE_de.md) |
| **Español (Spanish)** | [Guía del Administrador del Sistema y Configuración en Español](docs/admin/ADMIN_GUIDE_es.md) |
| **Français (French)** | [Guide d'administration et de configuration du système en français](docs/admin/ADMIN_GUIDE_fr.md) |

---

## 本 Fork 版本重點更新 (Changelog & Major Updates)

### 🚀 版本 2.3.3-2.0.0 重大升級 (Major Architectural Modernization)

1. **後端核心架構全面現代化 (Spring 5.3 + Hibernate 5.6 + JUnit 5)**：
   - 全面升級至 Spring Framework 5.3.37，移除已廢棄的 `HibernateTemplate` 與 `TimerFactoryBean`。
   - 升級至 Hibernate ORM 5.6.15.Final，原生 SessionFactory 管理與 JPA 規範查詢。
   - 全文檢索脫離已停護的 `spring-modules-lucene`，改用原生輕量 Lucene 檢索實作。
   - 單元測試全面現代化升級至 JUnit 5 (Jupiter)。
2. **安全性架構全面重構 (Spring Security 5.8 + BCrypt 平滑遷移)**：
   - 徹底移除過時且存在安全隱患的 Acegi Security 1.0.7，引進標準 Spring Security 5.8.14。
   - 實作雙模相容 `JtracHybridPasswordEncoder`：相容舊有 MD5 密碼雜湊，並於使用者登入成功時無感自動重新雜湊升級為強安全的 BCrypt，資料庫升級無需人為介入重設密碼。
3. **Web 表現層升級至 Apache Wicket 9.16.0**：
   - 徹底揮別 2008 年的 Wicket 1.3.7，升級至現代化 Wicket 9.16.0，元件與模型全面泛型化（`IModel<T>`）。
   - 支援 Servlet 4.0 現代容器（如 Jetty 10.0.26、Jetty 12、Tomcat 9 與 Tomcat 10+）。
4. **使用者管理與專案空間分頁導覽 (Pagination & System Config)**：
   - 使用者列表 (`UserListPage`) 與專案列表 (`SpaceListPage`) 升級支援自訂分頁（10, 25, 50, 100, 全部），避免大量資料效能瓶頸。
   - 於 `config` 表註冊全域參數 `users.list.pageSize` 與 `spaces.list.pageSize`，支援預設值自訂。
5. **成員指派與角色授權事件修復 (Role Allocation Ajax Fix)**：
   - 修正專案分配頁面的 Ajax 事件為標準 DOM 原生 `"change"` 事件，修復選取未即時更新及清空防呆。
6. **全域靜態資源過濾器 (Static Resource Filter)**：
   - 引入 `StaticResourceFilter`，根治多層路徑（如 `/app/space/allocate/...`）下 `../resources/*` 破圖 404 問題，並補齊缺漏圖示。
7. **表單附件上傳模型綁定修復 (FileUpload Model Binding)**：
   - 為 `ItemFormPage`、`ItemViewFormPanel` 與 `ExcelImportPage` 的 `FileUploadField` 顯式綁定獨立 Model，根除向實體類別反射查無 `file` 屬性的 Wicket 執行期例外。
8. **資料庫平滑升級與 SQL 指南**：
   - 提供專屬升級腳本 [`etc/sql/upgrade-to-2.0.0.sql`](etc/sql/upgrade-to-2.0.0.sql)，支援 MySQL、PostgreSQL、SQL Server、Oracle。
   - 內建 `HsqldbDatabaseMigrator`，於啟動時自動備份並無痛升級 HSQLDB 1.8 至 2.x。

---

### 📦 先前版本累積功能 (Cumulative Features from 2.3.3-1.0.0)

- **模組清理**：完全移除過時的 Wiki 模組。
- **Web 端批次 HTML 匯出與 ZIP 下載**：支援多空間批次離線封存。
- **嚴格空間權限守門員**：一般使用者僅能檢視與匯出具權限之專案。
- **獨立命令列匯出工具 (`tools/jtrac-exporter.jar`)**：免伺服器透過 JDBC 直接輸出響應式靜態 HTML 報表與深色主題。
- **8 國多語系全面覆蓋**：全專案標準化 UTF-8 與全語系介面資源補全。

---

## 開發技術與架構 (Technologies & Architecture)

- **核心語言**：Java 11 / 17
- **Web 框架**：Apache Wicket 9.16.0
- **後端 IoC**：Spring Framework 5.3.37
- **安全防護**：Spring Security 5.8.14 (BCrypt 密碼加密)
- **ORM 與持久層**：Hibernate ORM 5.6.15.Final
- **支援資料庫**：HSQLDB 2.x（內建預設）、MySQL / MariaDB、PostgreSQL、Microsoft SQL Server、Oracle
- **支援 Web 容器**：
  - **Jetty 10.x**（原生支援，開箱即用，實機驗證於 Jetty 10.0.26）
  - **Jetty 12.x**（啟用 `ee8` 模組原生運行）
  - **Tomcat 9.x**（原生支援，開箱即用）
  - **Tomcat 10.x / 11.x**（透過 `webapps-javaee/` 自動轉換或 `jakartaee-migration` 轉檔）
- **建置工具**：Apache Maven 3.9+
- **輔助規範**：OpenSpec v1.12.0、Antigravity

---

## 快速開始：編譯與部署 (Quick Start)

### 1. 編譯主程式 (WAR)
```bash
# 編譯主程式碼 (需 JDK 11 或 JDK 17)
mvn clean compile

# 執行測試並封裝 WAR 檔
mvn package

# 快速打包 WAR 檔（跳過測試）
mvn package -DskipTests
```
產出套件位於：`target/jtrac.war`。

### 2. 資料庫升級 (若從 2.3.3-1.0.0 升級)
- 若使用 MySQL / PostgreSQL / SQL Server / Oracle：請執行 [`etc/sql/upgrade-to-2.0.0.sql`](etc/sql/upgrade-to-2.0.0.sql)。
- 若使用內建 HSQLDB：啟動時將自動備份並自動完成結構升級，不需手動操作。

### 3. 建置獨立 HTML 匯出工具 (CLI)
```bash
mvn clean package -f tools/jtrac-exporter/pom.xml -DskipTests
# 產出位置: tools/jtrac-exporter.jar
```

---

## 專案規格文件 (Specifications)

- [專案主規格目錄](openspec/specs/README.md)
- [HTML Exporter 規格文件](openspec/specs/html-exporter/spec.md)
- [語系與資源規格文件](openspec/specs/i18n-resources/spec.md)
- [建置手冊規格文件](openspec/specs/build-documentation/spec.md)
- [後端架構現代化規格](openspec/changes/archive/2026-09-08-modernize-backend-spring5-security-hibernate5/specs/backend-persistence/spec.md)
- [後端安全認證規格](openspec/changes/archive/2026-09-08-modernize-backend-spring5-security-hibernate5/specs/backend-security/spec.md)
- [專案開發鐵律](.agents/AGENTS.md)

---

## 授權條款 (License)

JTrac 為開源軟體，遵循 [Apache Software License, Version 2.0](license.txt)。
