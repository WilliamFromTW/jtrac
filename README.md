# JTrac - Open Source Issue Tracking Web Application

[![Java](https://img.shields.io/badge/Java-8%20%7C%2011-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](license.txt)
[![OpenSpec](https://img.shields.io/badge/OpenSpec-spec--driven-brightgreen.svg)](openspec/specs/README.md)

---

## 🌐 Language Navigation / 多語系導覽 / 言語ナビゲーション / Điều hướng ngôn ngữ

| Language / 語言 / 言語 / Ngôn ngữ | Build Guide / 建置指南 / ビルドガイド / Hướng dẫn |
|---|---|
| **English** | [📖 English Build & Compilation Guide](docs/build/BUILD_en.md) |
| **繁體中文 (Traditional Chinese)** | [📖 繁體中文編譯與建置完整指南](docs/build/BUILD_zh-TW.md) |
| **简体中文 (Simplified Chinese)** | [📖 简体中文编译与构建完整指南](docs/build/BUILD_zh-CN.md) |
| **日本語 (Japanese)** | [📖 日本語ビルド・コンパイル詳細ガイド](docs/build/BUILD_ja.md) |
| **Tiếng Việt (Vietnamese)** | [📖 Hướng dẫn biên dịch và đóng gói Tiếng Việt](docs/build/BUILD_vi.md) |

---

## 📌 Project Overview / 專案簡介

**JTrac** is a fast, easy-to-use, and highly customizable generic issue-tracking web-application. 

### Key Features:
- **Customizable Workflow & Fields**: Easily configure custom fields, drop-down selections, and permissions per space.
- **Detailed History View**: Follow-up discussion threads (similar to forum threads) keeping all updates, status changes, and notes organized together.
- **File Attachments**: Upload and link attachments directly into discussion history entries.
- **Integrated Wiki**: Built-in wiki with Markdown and rich text formatting support.
- **Email Integration**: Automated email notifications upon ticket creation, updates, and reassignment.
- **Flexible Database**: Out-of-the-box embedded HSQLDB; also supports MySQL, PostgreSQL, MS SQL Server, and Oracle.

---

## 🛠 Tech Stack / 技術棧

- **Core Language**: Java 1.8 / 11
- **Web Framework**: Apache Wicket
- **Inversion of Control & MVC**: Spring Framework
- **ORM & Database**: Hibernate / HSQLDB (Embedded)
- **Build & Dependency Tool**: Apache Maven (Packaging: WAR)
- **Character Encoding**: 100% UTF-8 Compliant

---

## 🚀 Quick Start / 快速建置

### 1. Environment Setup / 環境準備 (Windows)
In Windows Command Prompt (CMD), load the environment script:
```cmd
call W:\developer\maven.bat
```

### 2. Build & Package / 編譯與打包
```cmd
# Compile source code / 編譯主程式碼
mvn compile

# Package WAR file (skipping tests) / 快速打包 WAR 檔
mvn package -DskipTests
```
The output WAR package will be generated at:
`target/jtrac.war`

---

## 💡 Important Build Architecture Notes / 重要建置機制說明

1. **Automatic Dependency Cache (`~/.m2/repository`) / 依賴自動下載**
   - Maven automatically downloads all required third-party libraries into your local cache directory (`~/.m2/repository`). 
   - **No manual JAR downloads are required.**

2. **Self-Contained WAR (`WEB-INF/lib/`) / 第三方套件全數封裝**
   - When running `mvn package`, all 53 third-party JARs are automatically bundled inside `target/jtrac.war` under `WEB-INF/lib/`.
   - When deploying to Servlet containers (Jetty 9.4, Jetty 12 `ee8`, Tomcat 9), simply deploy `jtrac.war`. **No external libraries need to be copied to the server.**

---

## 📦 Tools: JTrac Standalone HTML Exporter / 討論串靜態匯出工具

A zero-legacy-dependency, standalone CLI tool to export any JTrac database into static, responsive HTML discussion threads with attachments:
無需啟動 Web 伺服器，直接透過 JDBC 連線字串將 JTrac 議題、歷史討論串與附件匯出為多語系靜態 HTML 報表。

### Build & Run / 建置與執行:
```bash
# Build the standalone executable JAR / 打包獨立可執行檔
cd tools/jtrac-exporter
mvn clean package

# Run via JDBC Connection String (MySQL, PostgreSQL, HSQLDB, SQL Server)
java -jar target/jtrac-exporter.jar \
  --db-url="jdbc:mysql://host:3306/jtrac?useUnicode=true&characterEncoding=UTF-8" \
  --db-user="jtrac" \
  --db-password="password" \
  --attachments-dir="/path/to/attachments" \
  --out="./export-html" \
  --lang=zh-TW
```
For full options, run: `java -jar target/jtrac-exporter.jar --help`

---

## 📚 Documentation & Specifications / 相關規格文件

- [Master Specifications / 專案主規格目錄](openspec/specs/README.md)
- [HTML Exporter Specification / 討論串匯出規格](openspec/specs/html-exporter/spec.md)
- [Build Documentation Specification / 編譯文件規格](openspec/specs/build-documentation/spec.md)
- [i18n & Resources Specification / 語系與資源規格](openspec/specs/i18n-resources/spec.md)
- [Project Rules / 專案鐵律規範](.agents/AGENTS.md)

---

## 📄 License

JTrac is open-source software released under the [Apache Software License, Version 2.0](license.txt).
