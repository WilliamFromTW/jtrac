# JTrac (機能強化版 Fork)

[English](README.md) | [繁體中文](README_zh-TW.md) | [简体中文](README_zh-CN.md) | [日本語](README_ja.md) | [Tiếng Việt](README_vi.md) | [Deutsch](README_de.md) | [Español](README_es.md) | [Français](README_fr.md)

---

[![Java](https://img.shields.io/badge/Java-8%20%7C%2011-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](license.txt)
[![Fork From](https://img.shields.io/badge/Fork%20From-JTrac%202.3.3-blue)](https://jtrac.info)
[![Version](https://img.shields.io/badge/Version-2.3.3--1.0.0-blue.svg)](pom.xml)
[![OpenSpec](https://img.shields.io/badge/OpenSpec-v1.12.0-brightgreen.svg)](openspec/specs/README.md)

本プロジェクトは、[JTrac 2.3.3 (https://jtrac.info)](https://jtrac.info) の近代化・機能強化フォーク版です。軽量性、高互換性、オフライン静的アーカイブ機能、そして洗練されたモダン UI を備えた課題管理（Issue Tracking）システムを提供します。本プロジェクトの開発は OpenSpec v1.12.0 仕様駆動プロセスを採用し、Antigravity 1.1.27 の支援のもとでアーキテクチャ刷新・検証・品質管理を行っています。

---

## 多言語ビルドガイド / Multilingual Build Guides

| 言語 / Language | ビルドガイド / Build Guide |
|---|---|
| **日本語 (Japanese)** | [日本語ビルド・コンパイル詳細ガイド](docs/build/BUILD_ja.md) |
| **English** | [English Build & Compilation Guide](docs/build/BUILD_en.md) |
| **繁體中文 (Traditional Chinese)** | [繁體中文編譯與建置完整指南](docs/build/BUILD_zh-TW.md) |
| **简体中文 (Simplified Chinese)** | [简体中文编译与构建完整指南](docs/build/BUILD_zh-CN.md) |
| **Tiếng Việt (Vietnamese)** | [Hướng dẫn biên dịch và đóng gói Tiếng Việt](docs/build/BUILD_vi.md) |
| **Deutsch (German)** | [ドイツ語ビルド＆コンパイルガイド](docs/build/BUILD_de.md) |
| **Español (Spanish)** | [スペイン語ビルド＆コンパイルガイド](docs/build/BUILD_es.md) |
| **Français (French)** | [フランス語ビルド＆コンパイルガイド](docs/build/BUILD_fr.md) |

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

## 本 Fork バージョンの主要な更新点 (Changelog & Major Updates)

### 1. モジュールの整理：Wiki モジュールの完全削除 (Removed Wiki Module)
- コア機能ではない古い Wiki モジュールのコード、ページコンポーネント、ナビゲーションアイコンを完全に削除し、システムの軽量化と信頼性の高い課題追跡コア機能への集中を実現しました。

### 2. 新機能 (New Features)
- **Web 画面での一括 HTML エクスポートおよび ZIP ダウンロード**:
  - メインナビゲーションバーに「HTML エクスポート」機能を追加しました。Web 画面上で複数のプロジェクトスペースを選択（「すべて選択 / 選択解除」対応）し、添付ファイルを含む静的 HTML 課題スレッドを ZIP ファイルとして即時一括ダウンロード可能です。
- **厳格なスペース権限制御ガードレール (Permission Guardrails)**:
  - サーバーサイドおよびクライアントサイドの二重保護：一般ユーザーは自身に権限のあるプロジェクトスペースのみを閲覧・選択・ダウンロード可能です（システム管理者はグローバル権限を維持）。未選択時のガードレールとわかりやすいエラー表示を実装しています。
- **独立コマンドライン HTML エクスポートツール (`tools/jtrac-exporter.jar`)**:
  - Web サーバーを起動することなく、JDBC 接続文字列経由でデータベースからレスポンシブな多言語静的 HTML レポートを出力できます。
  - 外部 CDN やインターネット接続を一切必要としない、100% オフライン完全対応の純粋な CSS ダークモード（Dark Mode 切り替えスイッチ）を内蔵しています。
- **多言語対応の拡充と補完 (Full i18n Coverage)**:
  - 繁体字中国語 (`zh_TW`) および簡体字中国語 (`zh_CN`) の翻訳を総点検・補完。
  - すべての言語リソースで未定義だったステータスやボタンキー（プロジェクトの有効化ステータス `space_form.isActive` など）を補完しました。

### 3. 不具合修正 (Bug Fixes)
- **ナビゲーションバーのボタンおよびラベルの中央揃え (Navigation Header Centering)**:
  - 「ダッシュボード」「検索」「HTML エクスポート」「オプション」「ログアウト」「ユーザー名」などのアイコンとテキストの垂直方向のズレや右寄せ配置の崩れを解消しました。
  - Flexbox inline-flex による完全な上下・左右中央揃えを導入し、ボタンに 3px 角丸とホバー（hover）アニメーション背景色、情報バッジの独立デザインを適用しました。
- **スレッド履歴の最適化 (Thread History Cleanup)**:
  - HTML エクスポート時、コメントや変更内容が空である最初の冗長な「Open」ステータス履歴の出力を抑止し、実質的な議論と状態変更履歴のみを保持するよう改善しました。
- **Hibernate `LazyInitializationException` の解消**:
  - スペース設定メタデータ (`space.metadata`) へのアクセス時に発生していたセッション終了例外を、事前積極的読み込み（Eager Fetch / Initialize）により解消し、安定稼働を確保しました。
- **Maven UTF-8 リソースフィルタリングとバイナリ画像破損の防止**:
  - 過去の `native2ascii` 変換を廃止し、プロジェクト全体で標準的な UTF-8 エンコーディングを適用。
  - Maven のリソースフィルタリングによる画像ファイル（gif、png、jar）の破損バグを修正しました。

---

## 開発技術とアーキテクチャ (Technologies & Architecture)

- **コア言語**: Java 1.8 / 11
- **Web フレームワーク**: Apache Wicket 1.3
- **IoC コンテナ**: Spring Framework 2.5
- **ORM およびデータベース**: Hibernate 3 / 内蔵 HSQLDB、MySQL, PostgreSQL, MS SQL Server, Oracle 対応
- **ビルドツール**: Apache Maven 3.9+ (WAR パッケージング)
- **開発支援ツール・仕様**: OpenSpec v1.12.0、Antigravity 1.1.27
- **文字コード**: 全システム 100% UTF-8

---

## クイックスタート：ビルドとデプロイ (Quick Start)

### 1. メインアプリケーションのビルド (WAR)
```bash
# ソースコードのコンパイル
mvn compile

# WAR ファイルのパッケージング（テストをスキップ）
mvn package -DskipTests
```
生成されたパッケージは `target/jtrac.war` に配置され、Jetty または Tomcat に直接デプロイ可能です。

### 2. 独立 HTML エクスポートツールのビルドと実行 (CLI)
```bash
# 独立実行可能 JAR のパッケージング
mvn clean package -f tools/jtrac-exporter/pom.xml -DskipTests
# 出力先: tools/jtrac-exporter.jar

# ローカル HSQLDB でのエクスポート実行例
java -jar tools/jtrac-exporter.jar \
  --db-url="jdbc:hsqldb:file:./data/db/jtrac;shutdown=true;readonly=true" \
  --attachments-dir="./data/attachments" \
  --out="./export-output" \
  --lang=ja
```

---

## 仕様書とドキュメント (Specifications)

本プロジェクトは OpenSpec による仕様駆動開発を行っています：
- [メイン仕様書目次](openspec/specs/README.md)
- [HTML Exporter 仕様書](openspec/specs/html-exporter/spec.md)
- [多言語リソース仕様書](openspec/specs/i18n-resources/spec.md)
- [ビルドドキュメント仕様書](openspec/specs/build-documentation/spec.md)
- [開発ルール](.agents/AGENTS.md)

---

## ライセンス (License)

JTrac はオープンソースソフトウェアであり、[Apache Software License, Version 2.0](license.txt) のもとで公開されています。
