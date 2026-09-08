# JTrac (增强版 Fork)

[English](README.md) | [繁體中文](README_zh-TW.md) | [简体中文](README_zh-CN.md) | [日本語](README_ja.md) | [Tiếng Việt](README_vi.md) | [Deutsch](README_de.md) | [Español](README_es.md)

---

[![Java](https://img.shields.io/badge/Java-8%20%7C%2011-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](license.txt)
[![Fork From](https://img.shields.io/badge/Fork%20From-JTrac%202.3.3-blue)](https://jtrac.info)
[![OpenSpec](https://img.shields.io/badge/OpenSpec-v1.12.0-brightgreen.svg)](openspec/specs/README.md)

本项目为 [JTrac 2.3.3 (https://jtrac.info)](https://jtrac.info) 的现代化增强 Fork 版本。致力于提供更轻量、高兼容性、具备离线静态归档与现代化 UI 体验的 Issue Tracking 系统。本项目开发过程中使用 OpenSpec v1.12.0 规格驱动流程，并由 Antigravity 1.1.27 辅助开发、架构重构与质量把关。

---

## 多语言构建手册 / Multilingual Build Guides

| 语言 / Language | 构建指南 / Build Guide |
|---|---|
| **简体中文 (Simplified Chinese)** | [简体中文编译与构建完整指南](docs/build/BUILD_zh-CN.md) |
| **English** | [English Build & Compilation Guide](docs/build/BUILD_en.md) |
| **繁體中文 (Traditional Chinese)** | [繁體中文編譯與建置完整指南](docs/build/BUILD_zh-TW.md) |
| **日本語 (Japanese)** | [日本語ビルド・コンパイル詳細ガイド](docs/build/BUILD_ja.md) |
| **Tiếng Việt (Vietnamese)** | [Hướng dẫn biên dịch và đóng gói Tiếng Việt](docs/build/BUILD_vi.md) |

---

## 本 Fork 版本重点更新 (Changelog & Major Updates)

### 1. 模块清理：完全移除 Wiki (Removed Wiki Module)
- 彻底移除非核心且过时的 Wiki 模块代码、页面组件及导航栏图标，精简系统架构，专注于高效稳定的 Issue 追踪核心。

### 2. 新增功能 (New Features)
- **Web 端批量 HTML 导出与 ZIP 下载**：
  - 于主导航栏整合“导出 HTML”功能，用户可于网页直接勾选多个项目空间（支持“全选 / 全部取消”），在线即时打包下载完整静态 HTML 讨论串与附件 ZIP 压缩包。
- **严格空间权限守门员 (Permission Guardrails)**：
  - 前后端双层严格把关：一般用户仅能查看、选择并下载自己具有成员权限的项目空间（系统管理员维持全局权限），具备空列表拦截机制与友好提示。
- **独立命令行 HTML 导出工具 (`tools/jtrac-exporter.jar`)**：
  - 无需启动 Web 服务器，直接通过 JDBC 连接字符串将数据库导出为离线响应式 HTML 报表。
  - 支持多语言静态报表、附件直接链接，并内置 100% 离线纯 CSS 深色主题 (Dark Mode 切换开关)，完全不依赖外部网络或 CDN。
- **多语言本地化完整补全 (Full i18n Coverage)**：
  - 重新校订并补齐简体中文 (`zh_CN`) 与繁体中文 (`zh_TW`) 翻译。
  - 补齐各语言缺失之状态与按钮键值（如项目空间启用状态 `space_form.isActive` 等）。

### 3. Bug 修复 (Bug Fixes)
- **导航栏按钮与标签居中对齐 (Navigation Header Centering)**：
  - 修复导航栏中“仪表盘、搜索、导出、选项、退出、账号名称”等按钮文字与图标垂直基准线不一致及靠右偏移问题。
  - 全面采用 Flexbox inline-flex 居中对齐，按钮加上 3px 微圆角与 hover 悬停交互底色，信息徽章标签独立呈现。
- **历史讨论串历程优化 (Thread History Cleanup)**：
  - 修复导出 HTML 时讨论串第一条总是重复出现无任何备注的“Open”冗余记录，仅保留具备实质意义的留言与状态变更历程。
- **解决 Hibernate `LazyInitializationException`**：
  - 修复空间元数据 (`space.metadata`) 延迟加载产生的 Session 关闭异常，改采预先积极加载 (Eager Fetch / Initialize) 保障稳定运行。
- **解决 Maven UTF-8 资源过滤与二进制图片损坏**：
  - 移除过时的 `native2ascii` 转换，全项目规范化纯 UTF-8 编码。
  - 修正 Maven resource filtering 误将二进制文件（gif、png、jar）当文本过滤导致图片损坏之问题。

---

## 开发技术与架构 (Technologies & Architecture)

- **核心语言**：Java 1.8 / 11
- **Web 框架**：Apache Wicket 1.3
- **IoC 与容器**：Spring Framework 2.5
- **ORM 与数据库**：Hibernate 3 / HSQLDB（内置）、支持 MySQL, PostgreSQL, MS SQL, Oracle
- **构建工具**：Apache Maven 3.9+（输出 WAR 包）
- **辅助工具与规范**：OpenSpec v1.12.0、Antigravity 1.1.27
- **编码规范**：全系统 100% UTF-8

---

## 快速开始：编译与部署 (Quick Start)

### 1. 编译主程序 (WAR)
```bash
# 编译主代码
mvn compile

# 快速打包 WAR 文件（跳过测试）
mvn package -DskipTests
```
产出文件位于：`target/jtrac.war`，可直接部署于 Jetty 或 Tomcat。

### 2. 构建独立 HTML 导出工具 (CLI)
```bash
# 构建独立可执行文件
mvn clean package -f tools/jtrac-exporter/pom.xml -DskipTests
# 产出位置: tools/jtrac-exporter.jar

# 执行本地 HSQLDB 导出示例
java -jar tools/jtrac-exporter.jar \
  --db-url="jdbc:hsqldb:file:./data/db/jtrac;shutdown=true;readonly=true" \
  --attachments-dir="./data/attachments" \
  --out="./export-output" \
  --lang=zh-CN
```

---

## 项目规格文件 (Specifications)

本项目采用 OpenSpec 规格驱动开发：
- [项目主规格目录](openspec/specs/README.md)
- [HTML Exporter 规格文件](openspec/specs/html-exporter/spec.md)
- [语言与资源规格文件](openspec/specs/i18n-resources/spec.md)
- [构建手册规格文件](openspec/specs/build-documentation/spec.md)
- [项目开发铁律](.agents/AGENTS.md)

---

## 授权条款 (License)

JTrac 为开源软件，遵循 [Apache Software License, Version 2.0](license.txt)。
