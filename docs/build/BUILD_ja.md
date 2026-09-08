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

---

## 5. データベースのアップグレード

2.3.3-1.0.0 からアップグレードする場合：
- **外部 DB（MySQL、PostgreSQL など）**：[`etc/sql/upgrade-to-2.0.0.sql`](../../etc/sql/upgrade-to-2.0.0.sql) を実行。
- **内蔵 HSQLDB**：サーバー起動時に自動バックアップおよび 2.x への移行が自動実行されます。

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
