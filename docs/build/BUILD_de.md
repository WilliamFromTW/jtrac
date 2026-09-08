# JTrac Bau- und Kompilierungsanleitung (Deutsch)

Diese Anleitung bietet eine Schritt-für-Schritt-Erklärung zum Erstellen, Kompilieren und Paketieren des JTrac-Projekts sowie eine detaillierte Erläuterung des Maven-Abhängigkeitsmanagements und der WAR-Paketierungsarchitektur.

---

## 1. Voraussetzungen

Stellen Sie vor dem Erstellen des Projekts sicher, dass Ihre Umgebung folgende Anforderungen erfüllt:

- **Betriebssystem**: Windows / Linux / macOS
- **Java Development Kit (JDK)**: JDK 8 oder JDK 11 (JDK 11 empfohlen, z. B. `W:\developer\jdk-11.0.28`)
- **Apache Maven**: Version 3.9.x oder höher (z. B. `W:\developer\apache-maven-3.9.9`)

### Einrichtung der Windows-Umgebung
Laden Sie unter Windows in der Eingabeaufforderung (CMD) vor dem Ausführen von Maven das Umgebungsskript:
```cmd
call W:\developer\maven.bat
```
Dieses Skript konfiguriert `PATH` und `JAVA_HOME` für die aktuelle Terminalsitzung.

Umgebung überprüfen:
```cmd
mvn -version
```
Die Ausgabe sollte die aktive Maven- und Java-Version bestätigen.

---

## 2. Häufige Build-Befehle

Führen Sie folgende Befehle im JTrac-Projektstammverzeichnis aus (wo sich die `pom.xml` befindet):

| Befehl | Beschreibung |
|---|---|
| `mvn compile` | Kompiliert 137 Java-Quelldateien unter `src/main/java` und verarbeitet Ressourcen |
| `mvn test-compile` | Kompiliert alle Unit-Test-Klassen unter `src/test/java` |
| `mvn test` | Führt Unit-Tests aus (verwendet eingebettetes In-Memory-HSQLDB; keine externe DB nötig) |
| `mvn package` | Führt Tests aus und bündelt die vollständige Webanwendung in `target/jtrac.war` |
| `mvn package -DskipTests` | Schnelles Erstellen und Paketieren von `target/jtrac.war` ohne Unit-Tests |
| `mvn clean` | Bereinigt das `target/`-Verzeichnis und Build-Artefakte |
| `mvn clean compile` | Bereinigt vorherige Artefakte und kompiliert den gesamten Quellcode neu |

---

## 3. Automatisches Abhängigkeitsmanagement über Maven (`~/.m2/repository`)

JTrac ist mit dem Standard-Maven-Abhängigkeitsmanagement konfiguriert. Alle erforderlichen Drittanbieter-Bibliotheken (einschließlich Spring Framework, Apache Wicket, Hibernate, Acegi Security, Lucene usw.) sind in der [`pom.xml`](../../pom.xml) deklariert.

### Automatischer Download- & Cache-Ablauf:
1. Beim ersten Ausführen von `mvn compile` oder `mvn package` verbindet sich Maven mit dem zentralen Repository (Maven Central).
2. Alle deklarierten Abhängigkeiten und transitiven Bibliotheken werden automatisch in den lokalen Cache heruntergeladen:
   - **Windows**: `%USERPROFILE%\.m2\repository\`
   - **Linux / macOS**: `~/.m2/repository/`
3. Nachfolgende Builds lesen direkt aus dem lokalen `.m2`-Cache. **Sie müssen niemals JAR-Dateien manuell suchen, herunterladen oder konfigurieren.**

---

## 4. Bündelung von Drittanbieter-Bibliotheken im WAR (`WEB-INF/lib/`)

Eine häufige Frage lautet: "Muss ich beim Bereitstellen von JTrac auf einem Servlet-Container wie Jetty oder Tomcat Drittanbieter-JARs manuell in das `lib/`-Verzeichnis des Servers kopieren?"

**Antwort: Keinesfalls!**

### WAR-Paketarchitektur:
Beim Ausführen von `mvn package` erstellt Maven automatisch ein in sich geschlossenes Web Application Archive: [`target/jtrac.war`](../../target/jtrac.war):

```text
jtrac.war
├── META-INF/
│   └── MANIFEST.MF
├── WEB-INF/
│   ├── classes/                 <-- Kompilierte Klassen und UTF-8-Ressourcendateien
│   │   ├── info/jtrac/...
│   │   └── messages*.properties
│   ├── lib/                     <-- [ALLE 53 Drittanbieter-JARs sind hier enthalten!]
│   │   ├── spring-2.5.6.jar
│   │   ├── wicket-1.3.7.jar
│   │   ├── hibernate-3.2.7.ga.jar
│   │   └── ...
│   └── web.xml                  <-- Servlet-Konfiguration
└── resources/
```

### Wichtige Bereitstellungshinweise:
- **Klassenlader-Isolation**: Servlet-Container (Jetty, Tomcat) isolieren die Bibliotheken in `WEB-INF/lib/` für jede Webanwendung automatisch.
- **Sauberes Serververzeichnis**: Das `lib/`-Verzeichnis des Servers bleibt sauber; kopieren Sie keine Anwendungs-JARs dorthin.
- **Einfache Bereitstellung**: Platzieren Sie einfach `target/jtrac.war` (oder umbenannt in `ROOT.war`) im `webapps/`-Verzeichnis des Servers und starten Sie den Dienst.

---

## 5. Lokales Testen & Ausführen

Um JTrac nach der Paketierung lokal interaktiv auszuführen:

### Mit Jetty:
1. Kopieren Sie `target/jtrac.war` nach `W:\developer\jtrac-2.3.3\webapps\ROOT.war`.
2. Führen Sie `W:\developer\jtrac-2.3.3\start.bat` aus.
3. Öffnen Sie Ihren Browser unter: `http://localhost:8888` (Standard-Administratorzugang: `admin` / `admin`).

---

## 6. Eigenständigen CLI-HTML-Exporter erstellen und ausführen (jtrac-exporter)

Das Projekt enthält ein eigenständiges CLI-Werkzeug `jtrac-exporter`, das JTrac-Datenbankeinträge direkt über Standard-JDBC in statische, responsive HTML-Diskussionsverläufe mit Anhängen exportiert.

### 6.1 Werkzeug bauen (Fat JAR)
Führen Sie diesen Befehl im Projektstammverzeichnis aus:
```cmd
mvn clean package -f tools/jtrac-exporter/pom.xml -DskipTests
```
Nach erfolgreichem Build liegt die ausführbare JAR-Datei direkt unter:
`tools/jtrac-exporter.jar`

### 6.2 Export ausführen (Kommandozeilenmodus)
Vom Projektstammverzeichnis aus einer lokalen HSQLDB exportieren:
```cmd
java -jar tools/jtrac-exporter.jar ^
  --db-url="jdbc:hsqldb:file:./data/db/jtrac;shutdown=true;readonly=true" ^
  --attachments-dir="./data/attachments" ^
  --out="./export-hsqldb" ^
  --lang=de
```

Für entfernte MySQL-, PostgreSQL- oder SQL-Server-Datenbanken:
```cmd
java -jar tools/jtrac-exporter.jar ^
  --db-url="jdbc:mysql://192.168.1.100:3306/jtrac?useUnicode=true&characterEncoding=UTF-8" ^
  --db-user="jtrac" ^
  --db-password="your_password" ^
  --attachments-dir="/path/to/attachments" ^
  --out="./export-mysql" ^
  --lang=de
```
Alle Optionen anzeigen mit: `java -jar tools/jtrac-exporter.jar --help`.
