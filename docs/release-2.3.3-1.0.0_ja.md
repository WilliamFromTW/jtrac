# JTrac リリースノート (Release Notes) - 2.3.3-1.0.0

[English](release-2.3.3-1.0.0_en.md) | [繁體中文](release-2.3.3-1.0.0_zh-TW.md) | [简体中文](release-2.3.3-1.0.0_zh-CN.md) | [日本語](release-2.3.3-1.0.0_ja.md) | [Tiếng Việt](release-2.3.3-1.0.0_vi.md) | [Deutsch](release-2.3.3-1.0.0_de.md) | [Español](release-2.3.3-1.0.0_es.md) | [Français](release-2.3.3-1.0.0_fr.md)

---

[![Java](https://img.shields.io/badge/Java-8%20%7C%2011-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.8+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](../license.txt)
[![Fork From](https://img.shields.io/badge/Fork%20From-JTrac%202.3.3-blue)](https://jtrac.info)
[![Version](https://img.shields.io/badge/Version-2.3.3--1.0.0-blue.svg)](../pom.xml)

本リリースは、[JTrac 2.3.3 (https://jtrac.info)](https://jtrac.info) からの最初の現代化および機能拡張フォーク版です。コードベースの整理、セキュリティ権限の強化、8言語にわたる完全な UTF-8 多言語化、およびオフライン静的 HTML アーカイブ出力に注力しています。

---

## 📦 バージョン 2.3.3-1.0.0 主要機能 (Major Features)

1. **モジュール整理 (Module Cleanup)**：
   - 時代遅れの Wiki モジュールを完全に削除し、コードベースと依存関係をスリム化。
2. **Web バッチ HTML エクスポートと ZIP ダウンロード (Batch HTML Export & ZIP Download)**：
   - Web 管理画面から複数プロジェクトを一括でオフラインアーカイブする機能を実装。Issue の全履歴を自己完結型レスポンシブ静的 HTML として出力し、ZIP ファイルで一括ダウンロード可能。
3. **厳格なスペース権限管理 (Strict Space Permission Guardrails)**：
   - スペースへのアクセス検証を強化し、一般ユーザーは自身がアクセス権限を持つプロジェクトのみ閲覧・検索・エクスポートできるように制御。
4. **スタンドアロン CLI エクスポートツール (`tools/jtrac-exporter.jar`)**：
   - サーバー不要で動作する独立した CLI エクスポートツールを提供。JDBC 経由でデータベースに直接接続し、ダークテーマ対応の静的 HTML レポートを生成。
5. **8 言語の完全サポート (Full Multilingual Standard)**：
   - プロジェクト全体で UTF-8 エンコーディングを標準化し、8 言語（英語、繁体字中国語、簡体字中国語、日本語、ベトナム語、ドイツ語、スペイン語、フランス語）の UI 言語リソースを完全整備。

---

## 📜 リリース履歴 (Release History)

- **後続バージョン**：[JTrac リリースノート - 2.3.3-2.0.0](release-2.3.3-2.0.0_ja.md)

---

## ライセンス (License)

JTrac はオープンソースソフトウェアであり、[Apache Software License, Version 2.0](../license.txt) に基づいて公開されています。
