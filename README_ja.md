# JTrac (機能強化モダンフォーク版)

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

本プロジェクトは、[JTrac 2.3.3 (https://jtrac.info)](https://jtrac.info) の近代化・機能強化フォーク版です。軽量性、高互換性、オフライン静的アーカイブ機能、洗練されたモダン UI、およびエンタープライズグレードのセキュリティ標準を備えた課題管理（Issue Tracking）システムを提供します。本プロジェクトは OpenSpec v1.12.0 仕様駆動プロセスを採用し、Antigravity の支援のもとでアーキテクチャ刷新を行っています。

---

## 多言語ビルドガイド / Multilingual Build Guides

| 言語 / Language | ビルドガイド / Build Guide |
|---|---|
| **日本語 (Japanese)** | [日本語ビルド・コンパイル詳細ガイド](docs/build/BUILD_ja.md) |
| **English** | [English Build & Compilation Guide](docs/build/BUILD_en.md) |
| **繁體中文 (Traditional Chinese)** | [繁體中文編譯與建置完整指南](docs/build/BUILD_zh-TW.md) |
| **简体中文 (Simplified Chinese)** | [简体中文编译与构建完整指南](docs/build/BUILD_zh-CN.md) |
| **Tiếng Việt (Vietnamese)** | [Hướng dẫn biên dịch và đóng gói Tiếng Việt](docs/build/BUILD_vi.md) |
| **Deutsch (German)** | [Deutscher Kompilierungs- und Build-Leitfaden](docs/build/BUILD_de.md) |
| **Español (Spanish)** | [Guía de compilación y construcción en español](docs/build/BUILD_es.md) |
| **Français (French)** | [Guide complet de compilation et de construction en français](docs/build/BUILD_fr.md) |

---

## 多言語システム管理者ガイド / Multilingual Administrator Guides

| 言語 / Language | 管理者ガイド / Admin Guide |
|---|---|
| **日本語 (Japanese)** | [日本語システム管理者総合ガイド](docs/admin/ADMIN_GUIDE_ja.md) |
| **English** | [English Administrator & System Configuration Guide](docs/admin/ADMIN_GUIDE_en.md) |
| **繁體中文 (Traditional Chinese)** | [繁體中文系統管理者完整指南](docs/admin/ADMIN_GUIDE_zh-TW.md) |
| **简体中文 (Simplified Chinese)** | [简体中文系统管理员完整指南](docs/admin/ADMIN_GUIDE_zh-CN.md) |
| **Tiếng Việt (Vietnamese)** | [Hướng dẫn Quản trị viên và Cấu hình Hệ thống](docs/admin/ADMIN_GUIDE_vi.md) |
| **Deutsch (German)** | [Deutscher Systemadministrator- und Konfigurationsleitfaden](docs/admin/ADMIN_GUIDE_de.md) |
| **Español (Spanish)** | [Guía del Administrador del Sistema y Configuración en Español](docs/admin/ADMIN_GUIDE_es.md) |
| **Français (French)** | [Guide d'administration et de configuration du système en français](docs/admin/ADMIN_GUIDE_fr.md) |

---

## 本フォークの主要な更新履歴 (Changelog & Major Updates)

### 🚀 バージョン 2.3.3-2.0.0 メジャーアップデート (Major Architectural Modernization)

1. **バックエンドコアの全面近代化 (Spring 5.3 + Hibernate 5.6 + JUnit 5)**：
   - Spring Framework 5.3.37 へアップグレード。非推奨の `HibernateTemplate` および `TimerFactoryBean` を完全撤去。
   - Hibernate ORM 5.6.15.Final への移行、ネイティブ `SessionFactory` および JPA 準拠クエリの導入。
   - 保守終了した `spring-modules-lucene` を排除し、軽量ネイティブ Lucene API 実装へ刷新。
   - 単体テストフレームワークを JUnit 5 (Jupiter) へ刷新。
2. **セキュリティ基盤の刷新 (Spring Security 5.8 + BCrypt 透過的移行)**：
   - 脆弱性を抱える旧 Acegi Security 1.0.7 を完全撤去し、Spring Security 5.8.14 を導入。
   - ハイブリッド暗号化 `JtracHybridPasswordEncoder` を実装：従来の MD5 ハッシュを検証しつつ、ユーザーの初回ログイン成功時にダウンタイムなしで自動的に強固な BCrypt ハッシュへとアップグレード。
3. **Web プレゼンテーション層の刷新 (Apache Wicket 9.16.0)**：
   - 2008 年の Wicket 1.3.7 からモダンな Wicket 9.16.0 へ全面移行。モデルとコンポーネントを型安全なジェネリクス化（`IModel<T>`）。
   - Servlet 4.0 コンテナ（Jetty 10.0.26、Jetty 12、Tomcat 9、Tomcat 10+）に完全適合。
4. **ユーザーおよびスペース管理のページネーション機能 (Pagination & Config)**：
   - ユーザー一覧 (`UserListPage`) およびスペース一覧 (`SpaceListPage`) で件数切り替え（10, 25, 50, 100, 全件）に対応。
   - `config` テーブルに `users.list.pageSize` と `spaces.list.pageSize` を登録し、デフォルト値を柔軟に設定可能。
5. **ロール割り当て Ajax イベントの不具合修正 (Role Allocation Ajax Fix)**：
   - Ajax イベントを標準 DOM の `"change"` イベントへ修正し、選択状態の即時更新および選択解除時の保護ガードを実装。
6. **グローバル静的リソースフィルター (Static Resource Filter)**：
   - `StaticResourceFilter` を導入し、深層パス（例: `/app/space/allocate/...`）における `../resources/*` の 404 画像リンク切れを根治。
7. **ファイルアップロードコンポーネントのモデルバインディング修正**：
   - `ItemFormPage` および `ItemViewFormPanel` の `FileUploadField` に独立した Model を割り当て、実行時のプロパティ解決例外を根絶。
8. **データベースの移行と SQL ガイド**：
   - 専用アップグレードスクリプト [`etc/sql/upgrade-to-2.0.0.sql`](etc/sql/upgrade-to-2.0.0.sql) を提供（MySQL、PostgreSQL、SQL Server、Oracle 対応）。
   - 内蔵 `HsqldbDatabaseMigrator` により、起動時に HSQLDB 1.8 を自動バックアップおよび 2.x へ無停止移行。
9. **Excel モジュールの完全削除と WAR 軽量化 (Excel Module Removal & POI Deprecation)**：
   - レガシーな Excel インポート・エクスポート機能および Apache POI 依存関係を完全に削除し、WAR パッケージサイズを 3 MB 以上軽量化。
10. **全システムバックアップ ZIP への SQL ダンプ追加 (Integrated SQL Dump in Backup Bundle)**：
    - 全システムバックアップ ZIP に、ANSI DDL、MySQL/PostgreSQL/HSQLDB 方言注釈、外部キー依存順 ANSI INSERT 文、シーケンス再設定文を含む統合 SQL ダンプファイル `jtrac-dump.sql` を追加。DBA による手動復旧やDB移行に対応。

---

## 開発技術とアーキテクチャ (Technologies & Architecture)

- **コア言語**：Java 11 / 17
- **Web フレームワーク**：Apache Wicket 9.16.0
- **IoC / DI**：Spring Framework 5.3.37
- **セキュリティ**：Spring Security 5.8.14 (BCrypt)
- **ORM**：Hibernate ORM 5.6.15.Final
- **対応データベース**：HSQLDB 2.x（内蔵）、MySQL / MariaDB、PostgreSQL、Microsoft SQL Server、Oracle
- **対応 Web コンテナ**：
  - **Jetty 10.x**（ネイティブ対応、即時運用可能、Jetty 10.0.26 にて実機検証済）
  - **Jetty 12.x**（`ee8` モジュールを有効化してネイティブ動作）
  - **Tomcat 9.x**（ネイティブ対応、即時運用可能）
  - **Tomcat 10.x / 11.x**（`webapps-javaee/` 自動変換または `jakartaee-migration` ツール対応）
- **ビルドツール**：Apache Maven 3.9+
- **仕様管理**：OpenSpec v1.12.0、Antigravity

---

## クイックスタート：ビルドとデプロイ (Quick Start)

### 1. アプリケーションのビルド (WAR)
```bash
# ソースコードのコンパイル (JDK 11 または JDK 17 が必要)
mvn clean compile

# テスト実行と WAR パッケージング
mvn package

# 高速パッケージング (テストスキップ)
mvn package -DskipTests
```
成果物：`target/jtrac.war`。

### 2. データベースのアップグレード (2.3.3-1.0.0 からの移行時)
- MySQL / PostgreSQL / SQL Server / Oracle の場合：[`etc/sql/upgrade-to-2.0.0.sql`](etc/sql/upgrade-to-2.0.0.sql) を実行してください。
- 内蔵 HSQLDB の場合：サーバー起動時に自動でバックアップおよび構造アップグレードが行われるため、手動作業は不要です。

### 3. スタンドアロン HTML エクスポートツールのビルド (CLI)
```bash
mvn clean package -f tools/jtrac-exporter/pom.xml -DskipTests
# 出力先: tools/jtrac-exporter.jar
```

---

## ライセンス (License)

JTrac はオープンソースソフトウェアであり、[Apache Software License, Version 2.0](license.txt) に基づいて公開されています。
