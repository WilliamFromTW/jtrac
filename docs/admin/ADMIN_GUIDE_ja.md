# JTrac システム管理者総合ガイド (日本語)

[English](ADMIN_GUIDE_en.md) | [繁體中文](ADMIN_GUIDE_zh-TW.md) | [简体中文](ADMIN_GUIDE_zh-CN.md) | [日本語](ADMIN_GUIDE_ja.md) | [Tiếng Việt](ADMIN_GUIDE_vi.md) | [Deutsch](ADMIN_GUIDE_de.md) | [Español](ADMIN_GUIDE_es.md) | [Français](ADMIN_GUIDE_fr.md)

---

## 目次
1. [初回ログインと初期資格情報](#1-初回ログインと初期資格情報)
2. [必須のシステム初期設定](#2-必須のシステム初期設定)
3. [システムアーキテクチャとメールフロー図 (Mermaid)](#3-システムアーキテクチャとメールフロー図-mermaid)
4. [管理者機能一覧](#4-管理者機能一覧)
5. [完全システムバックアップと復元・ロックアウト防止機構 (System Backup & Restore)](#5-完全システムバックアップと復元ロックアウト防止機構-system-backup--restore)
6. [セキュリティ、DB移行および日常保守](#6-セキュリティdb移行および日常保守)

---

## 1. 初回ログインと初期資格情報

- **システム URL**：`http://<サーバーIP>:<ポート>/`（例: `http://localhost:8888/`）
- **初期管理者アカウント**：`admin`
- **初期管理者パスワード**：`admin`

> [!WARNING]
> 初回ログイン後は直ちに画面右上の **OPTIONS** ➜ **Edit User Profile** よりパスワードを変更してください。

---

## 2. 必須のシステム初期設定

画面右上の **OPTIONS** ➜ **Manage Settings** にて以下を設定します：

### 1. `jtrac.url.base`（基準 URL - 最重要）
- **デフォルト**：`http://localhost/jtrac/`
- **推奨設定**：利用者がアクセスする正規の URL（末尾に必ずスラッシュ `/` を付与、例: `http://192.168.1.100:8888/` または `https://issues.yourcompany.com/`）。
- **理由**：通知メールのリンク生成のベースとなります。`localhost` のままでは外部端末からリンクを開くことができません。

---

### 2. `locale.default`（デフォルト言語）
- 推奨設定：`ja`（日本語）。

---

### 3. SMTP メールサーバー設定
- `mail.server.host`、`mail.server.port`、`mail.server.username`、`mail.server.password`、`mail.server.starttls.enable`、`mail.from`。

---

### 4. 詳細設定とページネーション
- `users.list.pageSize`：ユーザー一覧の1ページあたりの件数（デフォルト: `25`、選択肢: 10, 25, 50, 100, 全件）。
- `spaces.list.pageSize`：スペース一覧の1ページあたりの件数（デフォルト: `25`、選択肢: 10, 25, 50, 100, 全件）。
- `attachment.maxsize`：添付ファイル上限（MB、デフォルト `10`）。

---

## 3. システムアーキテクチャとメールフロー図 (Mermaid)

```mermaid
flowchart TD
    Start([JTrac の起動]) --> Login[初回ログイン<br/>admin / admin]
    Login --> ChangePwd[パスワード変更<br/>OPTIONS ➜ Edit User Profile]
    ChangePwd --> ConfigSettings[コアパラメータ設定<br/>OPTIONS ➜ Manage Settings]
    
    subgraph CriticalSettings [必須設定]
        ConfigSettings --> SetUrlBase["jtrac.url.base の設定<br/>(例: http://192.168.1.100:8888/)"]
        ConfigSettings --> SetLocale["locale.default の設定<br/>(例: ja)"]
        ConfigSettings --> SetSMTP["SMTP サーバーの設定<br/>(host / port / from)"]
        ConfigSettings --> SetPaging["ページネーション件数設定<br/>(users/spaces.list.pageSize)"]
    end
    
    CriticalSettings --> CreateSpaces[スペース作成<br/>OPTIONS ➜ Manage Spaces]
    CreateSpaces --> CreateUsers[ユーザー作成と権限割り当て<br/>OPTIONS ➜ Manage Users]
    CreateUsers --> Finish([運用開始])
```

---

## 4. 管理者機能一覧

| 項目 | 説明 |
|---|---|
| **Edit User Profile** | 管理者のメールアドレス、氏名、パスワードの変更。 |
| **Manage Users** | ユーザーの追加、ページネーション表示、パスワード初期化、権限設定。 |
| **Manage Spaces** | スペースの追加、ページネーション表示、カスタム項目、ロール設定。 |
| **Configure Links** | ナビゲーションバーへの外部リンク設定。 |
| **Manage Settings** | 基準 URL、SMTP、ページネーション等のグローバル設定。 |
| **Rebuild Indexes** | Lucene 全文検索インデックスの再構築。 |
| **Import From Excel** | Excel による課題の一括インポート。 |
| **Export HTML** | Web からの HTML 一括エクスポートと ZIP ダウンロード。 |
| **Backup & Restore** | 完全システムバックアップと復元：最高管理者専用。DBと添付ファイルをまとめた単一ZIPバックアップのダウンロードと、安全な復元（自動スナップショット作成およびロックアウト防止シールド付き）を提供。 |

---

## 5. 完全システムバックアップと復元・ロックアウト防止機構 (System Backup & Restore)

最高管理者（SuperUser）向けに、ネイティブな全システム災害復旧およびデータ移行機能を提供します：

1. **ワンクリック完全システムバックアップのエクスポート**:
   - **OPTIONS** ➜ **Backup & Restore** にアクセスします。
   - 「**バックアップをダウンロード (.zip)**」をクリックすると、すべてのDBエンティティ（設定、ユーザー、スペース、チケット、履歴、添付情報）がクロスDB標準JSONにシリアライズされ、実体添付ファイル（`${jtrac.home}/attachments/`）とともに単一のタイムスタンプ付き `.zip` アーカイブとしてブラウザからダウンロードされます。
2. **安全なシステム復元エンジン (Safe Restore Engine)**:
   - 正しい JTrac バックアップ `.zip` ファイルを選択し、上書き確認チェックボックスをオンにして「**復元を実行**」をクリックします。
   - **サーバー側自動緊急スナップショット (Safety Snapshot)**: 既存データを上書き・消去する直前に、サーバー上の `${jtrac.home}/backups/` 配下に現行システムの完全スナップショットが自動生成され、万一のトラブル時にも確実に復旧可能です。
   - **管理者アカウント・ロックアウト防止シールド (Anti-Lockout Credential Shield)**: 復元処理を実行している現在の管理者を自動識別します。バックアップ内の管理者パスワードが紛失または古い場合でも、**現在の操作者のアクティブなパスワードハッシュと最高管理権限 (`ROLE_ADMIN`) を強制的に保持**（バックアップに含まれない場合は新規注入）し、管理者が締め出されるリスクを完全に排除します。
   - **バックグラウンドでの非同期インデックス再構築**: 復元完了後、Lucene 全文検索インデックスがバックグラウンドで自動再構築されます。管理者のセッションは中断されることなく有効に維持されます。

---

## 6. セキュリティ、DB移行および日常保守

1. **パスワードセキュリティ (BCrypt 自動移行)**：
   - Spring Security 5.8 (BCrypt) に対応。旧 MD5 パスワードはログイン時に自動で BCrypt へ安全に変換されます。
2. **データベースのアップグレード (2.3.3-1.0.0 からの移行)**：
   - 外部 DB：[`etc/sql/upgrade-to-2.0.0.sql`](../../etc/sql/upgrade-to-2.0.0.sql) を実行。
   - 内蔵 HSQLDB：起動時に自動バックアップおよび 2.x へ無停止移行。
3. **バックアップ**：
   - 定期的に **OPTIONS** ➜ **Backup & Restore** より完全バックアップ（DBと添付ファイルを含む）をダウンロードすることをお勧めします。
   - `data/db/` および `data/attachments/` の定期バックアップを実施してください。

