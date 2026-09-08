# JTrac (Erweiterter Fork)

[English](README.md) | [繁體中文](README_zh-TW.md) | [简体中文](README_zh-CN.md) | [日本語](README_ja.md) | [Tiếng Việt](README_vi.md) | [Deutsch](README_de.md) | [Español](README_es.md) | [Français](README_fr.md)

---

[![Java](https://img.shields.io/badge/Java-8%20%7C%2011-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](license.txt)
[![Fork From](https://img.shields.io/badge/Fork%20From-JTrac%202.3.3-blue)](https://jtrac.info)
[![Version](https://img.shields.io/badge/Version-2.3.3--1.0.0-blue.svg)](pom.xml)
[![OpenSpec](https://img.shields.io/badge/OpenSpec-v1.12.0-brightgreen.svg)](openspec/specs/README.md)

Dieses Projekt ist ein modernisierter, erweiterter Fork von [JTrac 2.3.3 (https://jtrac.info)](https://jtrac.info). Ziel ist es, ein schlankes, hochkompatibles Issue-Tracking-System bereitzustellen, das über Offline-Archivierungsfunktionen und eine überarbeitete Benutzeroberfläche verfügt. Die Entwicklung wird über OpenSpec v1.12.0 spezifikationsgesteuert geführt und durch Antigravity 1.1.27 bei Architekturrefaktorisierung, Verifikation und Qualitätskontrolle unterstützt.

---

## Mehrsprachige Build-Anleitungen / Multilingual Build Guides

| Sprache / Language | Bauanleitung / Build Guide |
|---|---|
| **Deutsch (German)** | [Deutsche Bau- und Kompilierungsanleitung](docs/build/BUILD_de.md) |
| **English** | [English Build & Compilation Guide](docs/build/BUILD_en.md) |
| **繁體中文 (Traditional Chinese)** | [繁體中文編譯與建置完整指南](docs/build/BUILD_zh-TW.md) |
| **简体中文 (Simplified Chinese)** | [简体中文编译与构建完整指南](docs/build/BUILD_zh-CN.md) |
| **日本語 (Japanese)** | [日本語ビルド・コンパイル詳細ガイド](docs/build/BUILD_ja.md) |
| **Tiếng Việt (Vietnamese)** | [Hướng dẫn biên dịch và đóng gói Tiếng Việt](docs/build/BUILD_vi.md) |
| **Español (Spanish)** | [Guía de construcción y compilación en español](docs/build/BUILD_es.md) |
| **Français (French)** | [Guide de compilation et d'exécution en français](docs/build/BUILD_fr.md) |

---

## Wichtigste Neuerungen dieses Forks (Changelog & Major Updates)

### 1. Bereinigung von Modulen: Vollständige Entfernung des Wiki-Moduls (Removed Wiki Module)
- Vollständiges Entfernen des veralteten Wiki-Quellcodes, der Webseitenkomponenten und der Navigationssymbole, um die Codebasis zu verschlanken und den Fokus auf den Kern des Issue-Trackings zu richten.

### 2. Neue Funktionen (New Features)
- **Webbasierter Batch-HTML-Export & ZIP-Download**:
  - Integration einer "HTML exportieren"-Aktion in der Hauptnavigationsleiste. Benutzer können mehrere Projekträume auswählen (inklusive "Alle auswählen / Alle abwählen") und ein Offline-Archiv mit statischen HTML-Diskussionsverläufen und Anhängen als ZIP-Datei herunterladen.
- **Strikte Projektzugriffskontrolle (Permission Guardrails)**:
  - Zweistufige serverseitige und clientseitige Sicherheitsüberprüfung: Reguläre Benutzer sehen, wählen und exportieren nur Projekträume, für die sie autorisiert sind (Systemadministratoren behalten uneingeschränkten Vollzugriff). Schutzmechanismen für leere Auswahlen bieten klares Feedback.
- **Eigenständiges CLI-HTML-Exportwerkzeug (`tools/jtrac-exporter.jar`)**:
  - Direkte Verbindung über JDBC zum Exportieren von Datenbanken in responsive, mehrsprachige HTML-Berichte ohne laufenden Webserver.
  - Integrierter, 100% netzwerkunabhängiger Dark-Mode-Schalter (reines CSS), der keinerlei externe CDNs oder Internetzugang benötigt.
- **Vollständige Lokalisierung (Full i18n Coverage)**:
  - Gründliche Überprüfung und Vervollständigung der Übersetzungen für traditionelles Chinesisch (`zh_TW`) und vereinfachtes Chinesisch (`zh_CN`).
  - Ergänzung fehlender Schlüssel über alle Sprachdateien hinweg (z. B. Aktivierungsstatus für Räume `space_form.isActive`).

### 3. Fehlerbehebungen (Bug Fixes)
- **Zentrierung der Navigationsleiste (Navigation Header Centering)**:
  - Behebung von Ausrichtungsfehlern bei Navigationsschaltflächen ("Übersicht", "Suche", "HTML exportieren", "Optionen", "Abmelden", Benutzername), die zuvor nach rechts verschoben und vertikal asymmetrisch waren.
  - Standardisierung auf Flexbox inline-flex für horizontale und vertikale Zentrierung, Hinzufügen von 3px abgerundeten Ecken, Hover-Effekten für Schaltflächen und Kennzeichnung von reinen Informationsfeldern.
- **Bereinigung des Diskussionsverlaufs (Thread History Cleanup)**:
  - Unterdrückung des redundanten initialen "Open"-Statusverlaufs beim HTML-Export, falls dieser weder Kommentare noch Feldänderungen enthielt, um nur gehaltvolle Verläufe anzuzeigen.
- **Behebung der Hibernate `LazyInitializationException`**:
  - Beseitigung von Session-Fehlern beim Zugriff auf Raummetadaten durch erzwungenes frühzeitiges Laden (Eager Fetch / Initialize) der Konfigurationsentitäten.
- **Korrektur der Maven UTF-8-Ressourcenfilterung & Binärdateibeschädigung**:
  - Standardisierung auf native UTF-8-Ressourcendateien ohne veraltete `native2ascii`-Codierungen.
  - Behebung von Fehlern bei der Maven-Ressourcenfilterung, durch die Bilddateien (gif, png, jar) beschädigt wurden.

---

## Entwicklungstechnologien & Architektur (Technologies & Architecture)

- **Kernsprache**: Java 1.8 / 11
- **Web-Framework**: Apache Wicket 1.3
- **IoC-Container**: Spring Framework 2.5
- **ORM & Datenbank**: Hibernate 3 / integriertes HSQLDB; unterstützt auch MySQL, PostgreSQL, MS SQL Server, Oracle
- **Build-Werkzeug**: Apache Maven 3.9+ (WAR-Paketierung)
- **Entwicklungswerkzeuge & Spezifikationen**: OpenSpec v1.12.0, Antigravity 1.1.27
- **Zeichenkodierung**: 100% UTF-8

---

## Schnellstart: Bauen & Bereitstellen (Quick Start)

### 1. Hauptanwendung erstellen (WAR)
```bash
# Quellcode kompilieren
mvn compile

# WAR-Paket erstellen (Tests überspringen)
mvn package -DskipTests
```
Das fertige Paket liegt unter: `target/jtrac.war` und kann direkt auf Jetty oder Tomcat bereitgestellt werden.

### 2. Standalone-HTML-Exporter erstellen und ausführen (CLI)
```bash
# Eigenständiges ausführbares JAR paketieren
mvn clean package -f tools/jtrac-exporter/pom.xml -DskipTests
# Ausgabe: tools/jtrac-exporter.jar

# Export einer lokalen HSQLDB-Datenbank ausführen
java -jar tools/jtrac-exporter.jar \
  --db-url="jdbc:hsqldb:file:./data/db/jtrac;shutdown=true;readonly=true" \
  --attachments-dir="./data/attachments" \
  --out="./export-output" \
  --lang=de
```

---

## Spezifikationen & Dokumentation (Specifications)

Dieses Projekt folgt spezifikationsgestützter Entwicklung mit OpenSpec:
- [Hauptspezifikationsverzeichnis](openspec/specs/README.md)
- [HTML-Exporter-Spezifikation](openspec/specs/html-exporter/spec.md)
- [Mehrsprachigkeit & Ressourcen-Spezifikation](openspec/specs/i18n-resources/spec.md)
- [Baudokumentations-Spezifikation](openspec/specs/build-documentation/spec.md)
- [Entwicklungsrichtlinien](.agents/AGENTS.md)

---

## Lizenz (License)

JTrac ist Open-Source-Software unter der [Apache Software License, Version 2.0](license.txt).
