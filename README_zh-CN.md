# JTrac (增强版 Fork)

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

本项目为 [JTrac 2.3.3 (https://jtrac.info)](https://jtrac.info) 的现代化增强 Fork 版本。致力于提供更轻量、高兼容性、具备离线静态归档、现代化 UI 与企业级安全规格的 Issue Tracking 系统。本项目开发过程中使用 OpenSpec v1.12.0 规格驱动流程，并由 Antigravity 辅助架构重构与质量把关。

---

## 多语言构建指南 / Multilingual Build Guides

| 语言 / Language | 构建指南 / Build Guide |
|---|---|
| **简体中文 (Simplified Chinese)** | [简体中文编译与构建完整指南](docs/build/BUILD_zh-CN.md) |
| **English** | [English Build & Compilation Guide](docs/build/BUILD_en.md) |
| **繁體中文 (Traditional Chinese)** | [繁體中文編譯與建置完整指南](docs/build/BUILD_zh-TW.md) |
| **日本語 (Japanese)** | [日本語ビルド・コンパイル詳細ガイド](docs/build/BUILD_ja.md) |
| **Tiếng Việt (Vietnamese)** | [Hướng dẫn biên dịch và đóng gói Tiếng Việt](docs/build/BUILD_vi.md) |
| **Deutsch (German)** | [Deutscher Kompilierungs- und Build-Leitfaden](docs/build/BUILD_de.md) |
| **Español (Spanish)** | [Guía de compilación y construcción en español](docs/build/BUILD_es.md) |
| **Français (French)** | [Guide complet de compilation et de construction en français](docs/build/BUILD_fr.md) |

---

## 多语言系统管理指南 / Multilingual Administrator Guides

| 语言 / Language | 管理员指南 / Admin Guide |
|---|---|
| **简体中文 (Simplified Chinese)** | [简体中文系统管理员完整指南](docs/admin/ADMIN_GUIDE_zh-CN.md) |
| **English** | [English Administrator & System Configuration Guide](docs/admin/ADMIN_GUIDE_en.md) |
| **繁體中文 (Traditional Chinese)** | [繁體中文系統管理者完整指南](docs/admin/ADMIN_GUIDE_zh-TW.md) |
| **日本語 (Japanese)** | [日本語システム管理者総合ガイド](docs/admin/ADMIN_GUIDE_ja.md) |
| **Tiếng Việt (Vietnamese)** | [Hướng dẫn Quản trị viên và Cấu hình Hệ thống](docs/admin/ADMIN_GUIDE_vi.md) |
| **Deutsch (German)** | [Deutscher Systemadministrator- und Konfigurationsleitfaden](docs/admin/ADMIN_GUIDE_de.md) |
| **Español (Spanish)** | [Guía del Administrador del Sistema y Configuración en Español](docs/admin/ADMIN_GUIDE_es.md) |
| **Français (French)** | [Guide d'administration et de configuration du système en français](docs/admin/ADMIN_GUIDE_fr.md) |

---

## 本 Fork 版本重点更新 (Changelog & Major Updates)

### 🚀 版本 2.3.3-2.0.0 重大升级 (Major Architectural Modernization)

1. **后端核心架构全面现代化 (Spring 5.3 + Hibernate 5.6 + JUnit 5)**：
   - 全面升级至 Spring Framework 5.3.37，移除废弃的 `HibernateTemplate` 与 `TimerFactoryBean`。
   - 升级至 Hibernate ORM 5.6.15.Final，原生 SessionFactory 管理与 JPA 规范查询。
   - 全文检索脱离已停止维护的 `spring-modules-lucene`，改用轻量原生 Lucene API。
   - 单元测试全面升级至 JUnit 5 (Jupiter)。
2. **安全性架构全面重构 (Spring Security 5.8 + BCrypt 平滑迁移)**：
   - 彻底移除过时且存在已知安全风险的 Acegi Security 1.0.7，引入标准 Spring Security 5.8.14。
   - 实现双模兼容 `JtracHybridPasswordEncoder`：兼容旧版 MD5 密码哈希，并在用户成功登录时自动重新哈希升级为高强度的 BCrypt，数据库平滑过渡无需人工重置密码。
3. **Web 表现层升级至 Apache Wicket 9.16.0**：
   - 彻底告别 2008 年的 Wicket 1.3.7，升级至现代化 Wicket 9.16.0，组件与模型全面泛型化（`IModel<T>`）。
   - 完美适配 Servlet 4.0 容器（Jetty 10.0.26、Jetty 12、Tomcat 9、Tomcat 10+）。
4. **用户管理与项目空间分页导航 (Pagination & System Config)**：
   - 用户列表 (`UserListPage`) 与项目空间列表 (`SpaceListPage`) 支持自定义分页（10, 25, 50, 100, 全部），消除大量数据加载性能瓶颈。
   - 于 `config` 表注册全局参数 `users.list.pageSize` 与 `spaces.list.pageSize`，支持默认分页大小配置。
5. **项目成员分配与角色授权事件修复 (Role Allocation Ajax Fix)**：
   - 修正项目分配页面 Ajax 监听事件为原生 `"change"` 事件，修复选择未实时更新及清空防呆。
6. **全局静态资源过滤器 (Static Resource Filter)**：
   - 引入 `StaticResourceFilter`，根治多层路径（如 `/app/space/allocate/...`）下 `../resources/*` 破图 404 问题，并补齐缺漏图标。
7. **表单附件上传模型绑定修复 (FileUpload Model Binding)**：
   - 为 `ItemFormPage` 与 `ItemViewFormPanel` 的 `FileUploadField` 显式绑定独立 Model，消除向实体类反射查找 `file` 属性的运行时异常。
8. **数据库平滑升级与 SQL 指南**：
   - 提供专用升级脚本 [`etc/sql/upgrade-to-2.0.0.sql`](etc/sql/upgrade-to-2.0.0.sql)，支持 MySQL、PostgreSQL、SQL Server、Oracle。
   - 内置 `HsqldbDatabaseMigrator`，启动时自动备份并平滑升级 HSQLDB 1.8 至 2.x。
9. **Excel 模块清理与 WAR 产物体积瘦身 (Excel Module Removal & POI Deprecation)**：
   - 彻底移除过时的 Excel 导入与导出模块，并完全删除 Apache POI 相关依赖，使 WAR 打包体积显著缩减超过 3 MB。
10. **全系统备份包升级 (Integrated SQL Dump in Backup Bundle)**：
    - 全系统备份 ZIP 压缩包内新增单文件整合 SQL 转储脚本 `jtrac-dump.sql`，包含通用 ANSI DDL、主流数据库方言注释、14 张数据表依外键拓扑排序之 ANSI INSERT 语句与 Sequence 自增重置指令，供 DBA 离线手动灾难恢复与跨库迁移。

---

### 📦 先前版本累积功能 (Cumulative Features from 2.3.3-1.0.0)

- **模块清理**：彻底移除过时的 Wiki 模块。
- **Web 端批量 HTML 导出与 ZIP 下载**：支持多空间批量离线归档。
- **严格空间权限守门员**：普通用户仅能查看与导出具备权限的项目。
- **独立命令行导出工具 (`tools/jtrac-exporter.jar`)**：免服务通过 JDBC 直出响应式 HTML 报表并支持深色主题。
- **8 种多语言全面覆盖**：全项目纯 UTF-8 规范化与全语系资源补全。

---

## 开发技术与架构 (Technologies & Architecture)

- **核心语言**：Java 11 / 17
- **Web 框架**：Apache Wicket 9.16.0
- **后端 IoC**：Spring Framework 5.3.37
- **安全防护**：Spring Security 5.8.14 (BCrypt)
- **ORM 与持久层**：Hibernate ORM 5.6.15.Final
- **支持数据库**：HSQLDB 2.x（内置默认）、MySQL / MariaDB、PostgreSQL、Microsoft SQL Server、Oracle
- **支持 Web 容器**：
  - **Jetty 10.x**（原生支持，开箱即用，实机验证于 Jetty 10.0.26）
  - **Jetty 12.x**（启用 `ee8` 模块原生运行）
  - **Tomcat 9.x**（原生支持，开箱即用）
  - **Tomcat 10.x / 11.x**（支持通过 `webapps-javaee/` 自动转换或 `jakartaee-migration` 转档）
- **构建工具**：Apache Maven 3.9+
- **辅助规范**：OpenSpec v1.12.0、Antigravity

---

## 快速开始：编译与部署 (Quick Start)

### 1. 编译主程序 (WAR)
```bash
# 编译主代码 (需 JDK 11 或 JDK 17)
mvn clean compile

# 执行测试并打包 WAR
mvn package

# 快速打包 WAR（跳过测试）
mvn package -DskipTests
```
产出包位于：`target/jtrac.war`。

### 2. 数据库升级 (若从 2.3.3-1.0.0 升级)
- 使用 MySQL / PostgreSQL / SQL Server / Oracle：请执行 [`etc/sql/upgrade-to-2.0.0.sql`](etc/sql/upgrade-to-2.0.0.sql)。
- 使用内置 HSQLDB：启动时将自动备份并自动完成结构升级，无需手动操作。

### 3. 构建独立 HTML 导出工具 (CLI)
```bash
mvn clean package -f tools/jtrac-exporter/pom.xml -DskipTests
# 产出位置: tools/jtrac-exporter.jar
```

---

## 授权条款 (License)

JTrac 为开源软件，遵循 [Apache Software License, Version 2.0](license.txt)。
