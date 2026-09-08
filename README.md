# JTrac (Enhanced Fork)

[English](README.md) | [繁體中文](README_zh-TW.md) | [简体中文](README_zh-CN.md) | [日本語](README_ja.md) | [Tiếng Việt](README_vi.md) | [Deutsch](README_de.md) | [Español](README_es.md) | [Français](README_fr.md)

---

[![Java](https://img.shields.io/badge/Java-8%20%7C%2011-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](license.txt)
[![Fork From](https://img.shields.io/badge/Fork%20From-JTrac%202.3.3-blue)](https://jtrac.info)
[![Version](https://img.shields.io/badge/Version-2.3.3--1.0.0-blue.svg)](pom.xml)
[![OpenSpec](https://img.shields.io/badge/OpenSpec-v1.12.0-brightgreen.svg)](openspec/specs/README.md)

This project is a modernized, enhanced fork of [JTrac 2.3.3 (https://jtrac.info)](https://jtrac.info). It aims to provide a lightweight, highly compatible issue-tracking system with offline static archiving capabilities and a refined modern user interface. Development of this project is driven by OpenSpec v1.12.0 specification processes and assisted by Antigravity 1.1.27 for architectural refactoring, verification, and code quality control.

---

## Multilingual Build Guides

| Language | Build Guide |
|---|---|
| **English** | [English Build & Compilation Guide](docs/build/BUILD_en.md) |
| **繁體中文 (Traditional Chinese)** | [繁體中文編譯與建置完整指南](docs/build/BUILD_zh-TW.md) |
| **简体中文 (Simplified Chinese)** | [简体中文编译与构建完整指南](docs/build/BUILD_zh-CN.md) |
| **日本語 (Japanese)** | [日本語ビルド・コンパイル詳細ガイド](docs/build/BUILD_ja.md) |
| **Tiếng Việt (Vietnamese)** | [Hướng dẫn biên dịch và đóng gói Tiếng Việt](docs/build/BUILD_vi.md) |
| **Deutsch (German)** | [Deutsche Bau- und Kompilierungsanleitung](docs/build/BUILD_de.md) |
| **Español (Spanish)** | [Guía de construcción y compilación en español](docs/build/BUILD_es.md) |
| **Français (French)** | [Guide de compilation et d'exécution en français](docs/build/BUILD_fr.md) |

---

## Multilingual Administrator Guides

| Language | Admin Guide |
|---|---|
| **English** | [English Administrator & System Configuration Guide](docs/admin/ADMIN_GUIDE_en.md) |
| **繁體中文 (Traditional Chinese)** | [繁體中文系統管理者完整指南](docs/admin/ADMIN_GUIDE_zh-TW.md) |
| **简体中文 (Simplified Chinese)** | [简体中文系统管理员完整指南](docs/admin/ADMIN_GUIDE_zh-CN.md) |
| **日本語 (Japanese)** | [日本語システム管理者総合ガイド](docs/admin/ADMIN_GUIDE_ja.md) |
| **Tiếng Việt (Vietnamese)** | [Hướng dẫn Quản trị viên và Cấu hình Hệ thống](docs/admin/ADMIN_GUIDE_vi.md) |
| **Deutsch (German)** | [Deutscher Systemadministrator- und Konfigurationsleitfaden](docs/admin/ADMIN_GUIDE_de.md) |
| **Español (Spanish)** | [Guía del Administrador del Sistema y Configuración en Español](docs/admin/ADMIN_GUIDE_es.md) |
| **Français (French)** | [Guide d'administration et de configuration du système en français](docs/admin/ADMIN_GUIDE_fr.md) |

---

## Major Updates in this Fork (Changelog)

### 1. Module Cleanup: Removed Wiki Module
- Completely removed the legacy, non-core Wiki module code, page components, and navigation icons to streamline the codebase and focus on a fast, reliable issue-tracking experience.

### 2. New Features
- **Web-based Batch HTML Export & ZIP Download**:
  - Integrated an "Export HTML" action into the main navigation header. Users can select multiple spaces (with "Select All" / "Deselect All" shortcuts) and download an offline static HTML archive with attachments bundled into a ZIP file.
- **Strict Space Permission Guardrails**:
  - Dual-layer server and client security verification: regular users can only see, select, and export spaces they are authorized members of (system administrators retain global access). Empty-selection and unauthorized access guardrails provide clear feedback.
- **Standalone CLI HTML Exporter (`tools/jtrac-exporter.jar`)**:
  - Direct JDBC connection tool to export databases into standalone, responsive, multi-language HTML threads without running the web server.
  - Built-in 100% offline pure CSS Dark Mode toggle that requires zero external CDN or internet access.
- **Complete Internationalization (i18n Coverage)**:
  - Audited and completed Traditional Chinese (`zh_TW`) and Simplified Chinese (`zh_CN`) localization.
  - Added missing labels across all language resource bundles (such as `space_form.isActive`).

### 3. Bug Fixes
- **Navigation Header Centering**:
  - Resolved alignment issues where header items ("Dashboard", "Search", "Export HTML", "Options", "Logout", username, space badge) were misaligned and right-shifted.
  - Standardized on Flexbox inline-flex vertical and horizontal centering, added 3px rounded borders, hover background highlights for actions, and dedicated badge styles.
- **Discussion Thread History Cleanup**:
  - Suppressed the redundant initial "Open" status history entry that lacked comments or field changes during HTML export, keeping only meaningful discussion updates.
- **Resolved Hibernate `LazyInitializationException`**:
  - Fixed session-closure exceptions when accessing space metadata by enforcing eager initialization and pre-loading on space configuration entities.
- **Fixed Maven UTF-8 Resource Filtering & Binary Asset Corruption**:
  - Standardized on native UTF-8 resource files without obsolete `native2ascii` escapes.
  - Fixed Maven resource filtering to prevent binary images (gif, png) from being corrupted during builds.

---

## Technologies & Architecture

- **Core Language**: Java 1.8 / 11
- **Web Framework**: Apache Wicket 1.3
- **IoC & Container**: Spring Framework 2.5
- **ORM & Database**: Hibernate 3 / Embedded HSQLDB; also supports MySQL, PostgreSQL, MS SQL Server, Oracle
- **Build Tool**: Apache Maven 3.9+ (WAR packaging)
- **Development & Spec Tools**: OpenSpec v1.12.0, Antigravity 1.1.27
- **Character Encoding**: 100% UTF-8

---

## Quick Start: Build & Deployment

### 1. Build Main Application (WAR)
```bash
# Compile source code
mvn compile

# Package WAR file (skipping tests)
mvn package -DskipTests
```
The output package will be located at: `target/jtrac.war`, ready to deploy on Jetty or Tomcat.

### 2. Build & Run Standalone HTML Exporter (CLI)
```bash
# Package the standalone executable JAR
mvn clean package -f tools/jtrac-exporter/pom.xml -DskipTests
# Output: tools/jtrac-exporter.jar

# Run export on a local HSQLDB database
java -jar tools/jtrac-exporter.jar \
  --db-url="jdbc:hsqldb:file:./data/db/jtrac;shutdown=true;readonly=true" \
  --attachments-dir="./data/attachments" \
  --out="./export-output" \
  --lang=en
```

---

## Specifications & Documentation

This project follows OpenSpec specification-driven development:
- [Master Specifications Directory](openspec/specs/README.md)
- [HTML Exporter Specification](openspec/specs/html-exporter/spec.md)
- [i18n & Resources Specification](openspec/specs/i18n-resources/spec.md)
- [Build Documentation Specification](openspec/specs/build-documentation/spec.md)
- [Project Development Rules](.agents/AGENTS.md)

---

## License

JTrac is open-source software released under the [Apache Software License, Version 2.0](license.txt).
