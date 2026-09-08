# JTrac システム管理者総合ガイド (日本語)

[English](ADMIN_GUIDE_en.md) | [繁體中文](ADMIN_GUIDE_zh-TW.md) | [简体中文](ADMIN_GUIDE_zh-CN.md) | [日本語](ADMIN_GUIDE_ja.md) | [Tiếng Việt](ADMIN_GUIDE_vi.md) | [Deutsch](ADMIN_GUIDE_de.md) | [Español](ADMIN_GUIDE_es.md) | [Français](ADMIN_GUIDE_fr.md)

---

## 目次
1. [初回ログインと初期資格情報](#1-初回ログインと初期資格情報)
2. [必須のシステム初期設定](#2-必須のシステム初期設定)
3. [システムアーキテクチャとメールフロー図 (Mermaid)](#3-システムアーキテクチャとメールフロー図-mermaid)
4. [管理者機能一覧](#4-管理者機能一覧)
5. [セキュリティ、DB移行および日常保守](#5-セキュリティdb移行および日常保守)

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

---

## 5. セキュリティ、DB移行および日常保守

1. **パスワードセキュリティ (BCrypt 自動移行)**：
   - Spring Security 5.8 (BCrypt) に対応。旧 MD5 パスワードはログイン時に自動で BCrypt へ安全に変換されます。
2. **データベースのアップグレード (2.3.3-1.0.0 からの移行)**：
   - 外部 DB：[`etc/sql/upgrade-to-2.0.0.sql`](../../etc/sql/upgrade-to-2.0.0.sql) を実行。
   - 内蔵 HSQLDB：起動時に自動バックアップおよび 2.x へ無停止移行。
3. **バックアップ**：
   - `data/db/` および `data/attachments/` の定期バックアップを実施してください。
