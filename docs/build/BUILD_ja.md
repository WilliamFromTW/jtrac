# JTrac ビルドおよびコンパイルガイド (日本語)

本ガイドでは、JTrac プロジェクトのビルド、コンパイル、パッケージングの手順、ならびに Maven 依存関係管理と WAR パッケージングアーキテクチャについて詳しく解説します。

---

## 1. 前提環境要件

ビルドを開始する前に、ローカル環境が以下の要件を満たしていることを確認してください。

- **オペレーティングシステム**: Windows / Linux / macOS
- **Java Development Kit (JDK)**: JDK 8 または JDK 11 (JDK 11 推奨、例: `W:\developer\jdk-11.0.25.9-hotspot`)
- **Apache Maven**: バージョン 3.9.x 以上 (例: `W:\developer\apache-maven-3.9.9`)

### Windows 環境の設定
Windows 環境では、コマンドプロンプト (CMD) で事前に環境設定スクリプトを実行してください。
```cmd
call W:\developer\maven.bat
```
このスクリプトにより、現在のセッションに Maven および JDK 11 の `PATH` と `JAVA_HOME` が正しく設定されます。

環境の確認コマンド:
```cmd
mvn -version
```
実行後、設定された Maven および Java 11 のバージョン情報が表示されます。

---

## 2. 主なビルドコマンド一覧

JTrac プロジェクトのルートディレクトリ (`pom.xml` があるフォルダ) で以下のコマンドを実行します。

| コマンド | 説明 |
|---|---|
| `mvn compile` | `src/main/java` 配下の 137 個の Java ソースファイルをコンパイルし、リソースフィルタリングを実行 |
| `mvn test-compile` | `src/test/java` 配下のテストクラスをコンパイル |
| `mvn test` | ユニットテストを実行 (内蔵インメモリ HSQLDB を使用するため外部 DB は不要) |
| `mvn package` | テストを実行し、Web アプリケーションアーカイブ (`target/jtrac.war`) をパッケージング |
| `mvn package -DskipTests` | テストをスキップして高速に `target/jtrac.war` を生成 |
| `mvn clean` | 前回のビルドで生成された `target/` フォルダ内のキャッシュおよび成果物を削除 |
| `mvn clean compile` | キャッシュを削除した上で再コンパイルを実行 |

---

## 3. Maven 依存関係自動ダウンロード機構 (`~/.m2/repository`)

JTrac は標準的な Maven アーキテクチャに基づいて構築されています。すべてのサードパーティライブラリ (Spring Framework、Apache Wicket、Hibernate、Acegi Security、Lucene など) は [`pom.xml`](../../pom.xml) 内に宣言されています。

### 自動ダウンロードおよびキャッシュの仕組み:
1. 初めて `mvn compile` または `mvn package` を実行した際、Maven は中央リポジトリ (Maven Central) に自動接続します。
2. 宣言されたすべての依存パッケージが、ユーザーのローカルキャッシュディレクトリに自動ダウンロードされます。
   - **Windows**: `%USERPROFILE%\.m2\repository\` (例: `C:\Users\username\.m2\repository\`)
   - **Linux / macOS**: `~/.m2/repository/`
3. 2 回目以降のビルドでは、すべてローカルの `.m2` キャッシュが使用されます。**開発者がサードパーティ製 JAR を手動で探したり、ダウンロード・設定したりする必要は一切ありません。**

---

## 4. WAR ファイルへのサードパーティライブラリ自動同梱構造 (`WEB-INF/lib/`)

「Jetty や Tomcat にデプロイする際、サードパーティ製 JAR をサーバーの `lib/` に手動コピーする必要があるか？」という疑問がよくあります。

**結論: 一切手動コピーする必要はありません！**

### WAR パッケージの内部構造:
`mvn package` を実行すると、Maven は自己完結型の Web アプリケーションアーカイブ [`target/jtrac.war`](../../target/jtrac.war) を生成します。内部構造は以下の通りです。

```text
jtrac.war
├── META-INF/
│   └── MANIFEST.MF
├── WEB-INF/
│   ├── classes/                 <-- コンパイルされた JTrac のクラスおよび UTF-8 リソース
│   │   ├── info/jtrac/...
│   │   └── messages*.properties
│   ├── lib/                     <-- 【重要: 全 53 個の依存 JAR はすべてここに内包されています！】
│   │   ├── spring-2.5.6.jar
│   │   ├── wicket-1.3.7.jar
│   │   ├── hibernate-3.2.7.ga.jar
│   │   ├── stringtree.jar       <-- プロジェクト専用ライブラリも自動同梱
│   │   ├── hsqldb-2.7.2.jar
│   │   └── ... (その他のすべてのライブラリ)
│   └── web.xml                  <-- Servlet 3.1 構成設定
└── resources/
```

### デプロイ時のポイント:
- **クラスローダーの独立性**: Jetty (Jetty 9.4, Jetty 12 `ee8`) や Tomcat などの Servlet コンテナは、各 WAR 内部の `WEB-INF/lib/` を自動的かつ独立してロードします。
- **サーバー本体のクリーン維持**: サーバー側の `lib/` フォルダには JTrac の JAR をコピーせず、クリーンな状態を保ってください。
- **シンプルなデプロイ**: `target/jtrac.war` をコンテナの `webapps/` フォルダに配置 (または `ROOT.war` にリネーム) するだけで起動可能です。

---

## 5. ローカルテスト環境での起動

パッケージング完了後、ローカルで Jetty を使用して動作確認を行う手順:

1. `target/jtrac.war` を `W:\developer\jtrac-2.3.3\webapps\ROOT.war` にコピー。
2. `W:\developer\jtrac-2.3.3\start.bat` を実行。
3. ブラウザで `http://localhost:8888` にアクセス (初期管理者アカウント: `admin` / `admin`)。

---

## 6. スタンドアロン CLI ディスカッションエクスポートツールのビルドと実行 (jtrac-exporter)

本プロジェクトには、レガシーフレームワークに一切依存せず、標準 JDBC 経由でローカルまたはリモートデータベースから課題、ディスカッション履歴、添付ファイルを多言語静的 HTML レポートとして出力するスタンドアロン CLI ツール `jtrac-exporter` が組み込まれています。

### 6.1 ツールのビルド (Fat JAR)
プロジェクトのルートディレクトリで以下のコマンドを実行します（ディレクトリ移動不要）:
```cmd
mvn clean package -f tools/jtrac-exporter/pom.xml -DskipTests
```
ビルド完了後、実行可能 JAR は以下に出力されます:
`tools/jtrac-exporter.jar`

### 6.2 エクスポートの実行 (Command Mode)
ルートディレクトリから、相対パスを用いてローカルの HSQLDB データベースからエクスポートする場合:
```cmd
java -jar tools/jtrac-exporter.jar ^
  --db-url="jdbc:hsqldb:file:./data/db/jtrac;shutdown=true;readonly=true" ^
  --attachments-dir="./data/attachments" ^
  --out="./export-hsqldb" ^
  --lang=ja
```

リモートの MySQL または PostgreSQL データベースへの接続もサポートしています:
```cmd
java -jar tools/jtrac-exporter.jar ^
  --db-url="jdbc:mysql://192.168.1.100:3306/jtrac?useUnicode=true&characterEncoding=UTF-8" ^
  --db-user="jtrac" ^
  --db-password="your_password" ^
  --attachments-dir="/path/to/attachments" ^
  --out="./export-mysql" ^
  --lang=ja
```
全オプションの確認: `java -jar tools/jtrac-exporter.jar --help`。

