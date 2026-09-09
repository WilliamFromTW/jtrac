# JTrac ビルド・コンパイル詳細ガイド (日本語)

[English](BUILD_en.md) | [繁體中文](BUILD_zh-TW.md) | [简体中文](BUILD_zh-CN.md) | [日本語](BUILD_ja.md) | [Tiếng Việt](BUILD_vi.md) | [Deutsch](BUILD_de.md) | [Español](BUILD_es.md) | [Français](BUILD_fr.md)

本ガイドでは、JTrac 2.3.3-2.0.0 プロジェクトのビルド、コンパイル、パッケージング手順、Maven 依存関係、WAR 構造、および各種 Web コンテナ（Jetty 10/12、Tomcat 9/10/11）へのデプロイ方法を解説します。

---

## 1. 事前準備環境

- **OS**：Windows / Linux / macOS
- **JDK**：**JDK 11 または JDK 17**（JDK 17 推奨、例: `W:\developer\jdk-17.0.9` または JDK 11 `W:\developer\jdk-11.0.28`）
  > [!IMPORTANT]
  > Spring 5.3、Hibernate 5.6、Wicket 9 へのアップグレードに伴い、コンパイル対象は Java 11 です。**JDK 8 はサポートされていません**。
- **Apache Maven**：Maven 3.9.x 以上

### Windows 環境変数設定例
```powershell
$env:JAVA_HOME = "W:\developer\jdk-17.0.9"
$env:PATH = "W:\developer\apache-maven-3.9.9\bin;$env:PATH"
```

確認コマンド：
```bash
mvn -version
```

---

## 2. 主なビルドコマンド

| コマンド | 説明 |
|---|---|
| `mvn clean compile` | キャッシュを消去し `src/main/java` を再コンパイル |
| `mvn test-compile` | `src/test/java` の単体テストをコンパイル |
| `mvn test` | 全テストを実行（JUnit 5 + 内蔵 HSQLDB、外部 DB 不要） |
| `mvn package` | テスト実行および本番 WAR パッケージング (`target/jtrac.war`) |
| `mvn package -DskipTests` | テストをスキップして高速パッケージング |
| `mvn clean` | `target/` のビルド成果物を消去 |

---

## 3. WAR パッケージの構造 (`WEB-INF/lib/`)

`mvn package` で生成される [`target/jtrac.war`](../../target/jtrac.war) の内部構成：

```text
jtrac.war
├── META-INF/
│   └── MANIFEST.MF
├── WEB-INF/
│   ├── classes/                 <-- コンパイル済みクラスおよび UTF-8 プロパティ
│   │   ├── info/jtrac/...
│   │   └── messages*.properties
│   ├── lib/                     <-- 現代の依存ライブラリ一式
│   │   ├── spring-core-5.3.37.jar
│   │   ├── wicket-core-9.16.0.jar
│   │   ├── hibernate-core-5.6.15.Final.jar
│   │   ├── spring-security-core-5.8.14.jar
│   │   ├── hsqldb-2.7.2.jar
│   │   └── ...
│   └── web.xml                  <-- Servlet 4.0 仕様設定
└── resources/
```

---

## 4. Web コンテナ対応表とデプロイ方法

JTrac 2.3.3-2.0.0 は Servlet 4.0 仕様（`javax.servlet`）に準拠しています：

| Web コンテナ | 対応バージョン | デプロイ手順 |
|---|---|---|
| **Jetty 10.x** | 10.0.x（推奨） | **即時動作**：`target/jtrac.war` を `webapps/ROOT.war` に配置。 |
| **Jetty 12.x** | 12.0.x（最新） | **ネイティブ**：内蔵 `ee8` モジュールを有効化：<br/>`java -jar start.jar --add-modules=server,http,ee8-deploy,ee8-webapp` |
| **Tomcat 9.x** | 9.0.x（推奨） | **即時動作**：`target/jtrac.war` を `webapps/ROOT.war` に配置。 |
| **Tomcat 10.x / 11.x** | 10.1.x / 11.0.x | **自動変換**：`webapps-javaee/` ディレクトリに配置、または `jakartaee-migration` で変換後配置。 |

### 4.1 データディレクトリ (`jtrac.home`) の判定優先順位とコンテナ設定

JTrac のデータおよび添付ファイル保存ディレクトリは、システム変数 `jtrac.home` によって制御されます。[`JtracConfigurer`](../../src/main/java/info/jtrac/config/JtracConfigurer.java) により以下の厳格な 4 段階の優先順位で決定されます：

1. **第 1 優先**：`WEB-INF/classes/jtrac-init.properties` 内の `jtrac.home`。
2. **第 2 優先 (本番環境推奨)**：JVM システムプロパティ `-Djtrac.home=...`。
3. **第 3 優先**：Servlet Context 初期化パラメータ（`web.xml` または Tomcat Context XML 内の `jtrac.home`）。
4. **第 4 優先 (デフォルト・フォールバック)**：`System.getProperty("user.home") + "/.jtrac"`。
   - **Tomcat での注意点**：Linux 環境で `root` ユーザーとして Tomcat を起動し、第 1〜3 優先の設定を行っていない場合、JTrac は自動的に `/root/.jtrac` をデータディレクトリとして使用します。
   - **ローカル Jetty 開発環境**：`start-jtrac.bat` で `-Djtrac.home=data` が指定されているため、`W:\developer\jetty-10.0.26\data\` にデータが保存されます。

#### データディレクトリの標準構造 (`jtrac.home`)：
- `jtrac.properties`：データベース接続設定、URL、アカウントおよび Hibernate 方言。
- `db/`：内蔵 HSQLDB データベースファイル（`jtrac.script`、`jtrac.data` 等）。
- `attachments/`：添付ファイル格納場所（プロジェクト ID ごとに `attachments/{spaceId}/` で分割）。
- `indexes/`：Lucene 全文検索インデックス。
- `backups/`：リストア実行前に自動生成される安全スナップショット（Safety Snapshot）。
- `logs/`：アプリケーション実行ログ（`jtrac.log`）。

#### コンテナ別 `jtrac.home` 設定方法：
- **Linux Tomcat (`bin/setenv.sh`)**：
  ```bash
  export CATALINA_OPTS="$CATALINA_OPTS -Djtrac.home=/var/jtrac-data"
  ```
- **Windows Tomcat (`bin/setenv.bat`)**：
  ```cmd
  set "CATALINA_OPTS=%CATALINA_OPTS% -Djtrac.home=D:/jtrac-data"
  ```
- **Jetty / コマンドライン起動**：
  ```bash
  java -Djtrac.home=/var/jtrac-data -jar start.jar
  ```

---

## 5. データベースおよびストレージのアップグレード
 
2.3.3-1.0.0 からアップグレードする場合：
- **外部 DB（MySQL、PostgreSQL など）**：[`etc/sql/upgrade-to-2.0.0.sql`](../../etc/sql/upgrade-to-2.0.0.sql) を実行。
- **内蔵 HSQLDB**：サーバー起動時に自動バックアップおよび 2.x への移行が自動実行されます。
- **添付ファイル保存移行**：`AttachmentStorageMigrator` により起動時に旧添付ファイルをプロジェクト ID 別フォルダ（`${jtrac.home}/attachments/{spaceId}/`）へ自動再配置、孤児ファイルは `attachments/0_ORPHAN/` に隔離。
- **Lucene 全文検索**：`.xlsx`、`.docx`（純 JDK ストリーミング OpenXML）、`.pdf`（Apache PDFBox 2.0.31）、`.txt`、`.csv`、`.md`、`.log` のテキスト抽出と `SmartCharsetDetector` による文字化け防止を標準搭載。

---

## 6. スタンドアロン HTML エクスポートツール (`jtrac-exporter`)

```cmd
mvn clean package -f tools/jtrac-exporter/pom.xml -DskipTests
java -jar tools/jtrac-exporter.jar ^
  --db-url="jdbc:hsqldb:file:./data/db/jtrac;shutdown=true;readonly=true" ^
  --attachments-dir="./data/attachments" ^
  --out="./export-output" ^
  --lang=ja
```

---

## 7. ネイティブ Docker コンテナ構築と実行 (Eclipse Temurin 17 + Jetty 12)

Docker を利用したマルチステージビルド環境を提供しており、ローカルに JDK や Maven をインストールすることなく実行可能です：

### 7.1 ネイティブ Docker コマンド (推奨)
`docker/` ディレクトリに移動し、プロジェクトルート (`..`) をビルドコンテキストとして指定して構築・起動します：
```bash
cd docker
docker build -f Dockerfile -t jtrac:latest ..
docker run -d -p 8888:8080 -v jtrac_data:/jtrac-data --name jtrac jtrac:latest
```

### 7.2 クロスプラットフォーム補助スクリプト & Docker Compose
- **Windows**：`docker/` で `build.bat` と `run.bat` を実行
- **Linux / macOS**：`docker/` で `./build.sh` と `./run.sh` を実行
- **Docker Compose**：`docker/` で `docker compose up -d` を実行

起動完了後、ブラウザで `http://localhost:8888/` にアクセスします（初期管理者アカウント：`admin` / `admin`）。外部 DB 接続および環境変数の詳細は [`docker/README.md`](../../docker/README.md) を参照してください。

