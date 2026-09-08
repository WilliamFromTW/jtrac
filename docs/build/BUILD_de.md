# JTrac Kompilierungs- und Build-Leitfaden (Deutsch)

[English](BUILD_en.md) | [繁體中文](BUILD_zh-TW.md) | [简体中文](BUILD_zh-CN.md) | [日本語](BUILD_ja.md) | [Tiếng Việt](BUILD_vi.md) | [Deutsch](BUILD_de.md) | [Español](BUILD_es.md) | [Français](BUILD_fr.md)

Dieser Leitfaden beschreibt das Erstellen, Kompilieren und Paketieren des JTrac 2.3.3-2.0.0 Projekts sowie das Deployment auf modernen Web-Containern (Jetty 10/12, Tomcat 9/10/11).

---

## 1. Voraussetzungen

- **Betriebssystem**: Windows / Linux / macOS
- **Java Development Kit (JDK)**: **JDK 11 oder JDK 17** (JDK 17 empfohlen, z. B. `W:\developer\jdk-17.0.9` oder JDK 11 `W:\developer\jdk-11.0.28`)
  > [!IMPORTANT]
  > Durch die Modernisierung auf Spring 5.3, Hibernate 5.6 und Wicket 9 ist das Kompilierungsziel Java 11. **JDK 8 wird nicht mehr unterstützt**.
- **Apache Maven**: Version 3.9.x oder höher

### Windows Umgebungsvariablen
```powershell
$env:JAVA_HOME = "W:\developer\jdk-17.0.9"
$env:PATH = "W:\developer\apache-maven-3.9.9\bin;$env:PATH"
```

Überprüfung:
```bash
mvn -version
```

---

## 2. Gängige Build-Befehle

| Befehl | Beschreibung |
|---|---|
| `mvn clean compile` | Bereinigt den Cache und kompiliert `src/main/java` neu |
| `mvn test-compile` | Kompiliert die Unit-Tests |
| `mvn test` | Führt alle Unit-Tests aus (JUnit 5 + eingebettete HSQLDB) |
| `mvn package` | Erstellt das produktive WAR-Archiv (`target/jtrac.war`) |
| `mvn package -DskipTests` | Schnelles Paketieren (ohne Tests) |
| `mvn clean` | Löscht den `target/`-Ordner |

---

## 3. WAR-Paketstruktur (`WEB-INF/lib/`)

Die generierte [`target/jtrac.war`](../../target/jtrac.war) enthält alle modernen Abhängigkeiten:
- `spring-core-5.3.37.jar`
- `wicket-core-9.16.0.jar`
- `hibernate-core-5.6.15.Final.jar`
- `spring-security-core-5.8.14.jar`
- `hsqldb-2.7.2.jar`

---

## 4. Web-Container-Matrix und Bereitstellung

JTrac 2.3.3-2.0.0 basiert auf der Servlet 4.0-Spezifikation (`javax.servlet`):

| Web-Container | Version | Bereitstellungsmethode |
|---|---|---|
| **Jetty 10.x** | 10.0.x (Empfohlen) | **Direkt**: Kopieren Sie `target/jtrac.war` nach `webapps/ROOT.war`. |
| **Jetty 12.x** | 12.0.x (Aktuell) | **Nativ**: Aktivieren Sie das `ee8`-Modul:<br/>`java -jar start.jar --add-modules=server,http,ee8-deploy,ee8-webapp` |
| **Tomcat 9.x** | 9.0.x (Empfohlen) | **Direkt**: Kopieren Sie `target/jtrac.war` nach `webapps/ROOT.war`. |
| **Tomcat 10.x / 11.x** | 10.1.x / 11.0.x | **Automatische Migration**: Platzieren Sie die WAR in `webapps-javaee/` oder konvertieren Sie via `jakartaee-migration`. |

---

## 5. Datenbank-Upgrade

Beim Upgrade von 2.3.3-1.0.0:
- Externe Datenbanken (MySQL, PostgreSQL etc.): Führen Sie [`etc/sql/upgrade-to-2.0.0.sql`](../../etc/sql/upgrade-to-2.0.0.sql) aus.
- Eingebettete HSQLDB: Die Migration erfolgt beim Serverstart vollautomatisch mit Backup.

---

## 6. Eigenständiges HTML-Export-Tool (`jtrac-exporter`)

```cmd
mvn clean package -f tools/jtrac-exporter/pom.xml -DskipTests
java -jar tools/jtrac-exporter.jar ^
  --db-url="jdbc:hsqldb:file:./data/db/jtrac;shutdown=true;readonly=true" ^
  --attachments-dir="./data/attachments" ^
  --out="./export-output" ^
  --lang=de
```
