# JTrac (Erweiterter moderner Fork)

[English](README.md) | [繁體中文](README_zh-TW.md) | [简体中文](README_zh-CN.md) | [日本語](README_ja.md) | [Tiếng Việt](README_vi.md) | [Deutsch](README_de.md) | [Español](README_es.md) | [Français](README_fr.md)

---

[![Java](https://img.shields.io/badge/Java-11%20%7C%2017-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](license.txt)
[![Fork From](https://img.shields.io/badge/Fork%20From-JTrac%202.3.3-blue)](https://jtrac.info)
[![Version](https://img.shields.io/badge/Version-2.3.3--2.0.0-blue.svg)](pom.xml)
[![Wicket](https://img.shields.io/badge/Wicket-9.16.0-blue.svg)](https://wicket.apache.org/)
[![Spring](https://img.shields.io/badge/Spring-5.3.37-brightgreen.svg)](https://spring.io/)
[![OpenSpec](https://img.shields.io/badge/OpenSpec-v1.12.0-brightgreen.svg)](openspec/specs/README.md)

Dieses Projekt ist ein modernisierter, erweiterter Fork von [JTrac 2.3.3 (https://jtrac.info)](https://jtrac.info). Ziel ist es, ein schlankes, hochkompatibles Issue-Tracking-System bereitzustellen, das über Offline-Archivierungsfunktionen, eine überarbeitete Benutzeroberfläche und moderne Sicherheitsstandards verfügt. Die Entwicklung wird über OpenSpec v1.12.0 geführt und durch Antigravity unterstützt.

---

## Mehrsprachige Build-Anleitungen / Multilingual Build Guides

| Sprache / Language | Build-Anleitung / Build Guide |
|---|---|
| **Deutsch (German)** | [Deutscher Kompilierungs- und Build-Leitfaden](docs/build/BUILD_de.md) |
| **English** | [English Build & Compilation Guide](docs/build/BUILD_en.md) |
| **繁體中文 (Traditional Chinese)** | [繁體中文編譯與建置完整指南](docs/build/BUILD_zh-TW.md) |
| **简体中文 (Simplified Chinese)** | [简体中文编译与构建完整指南](docs/build/BUILD_zh-CN.md) |
| **日本語 (Japanese)** | [日本語ビルド・コンパイル詳細ガイド](docs/build/BUILD_ja.md) |
| **Tiếng Việt (Vietnamese)** | [Hướng dẫn biên dịch và đóng gói Tiếng Việt](docs/build/BUILD_vi.md) |
| **Español (Spanish)** | [Guía de compilación y construcción en español](docs/build/BUILD_es.md) |
| **Français (French)** | [Guide complet de compilation et de construction en français](docs/build/BUILD_fr.md) |

---

## Mehrsprachige Administrator-Handbücher / Multilingual Administrator Guides

| Sprache / Language | Administrator-Handbuch / Admin Guide |
|---|---|
| **Deutsch (German)** | [Deutscher Systemadministrator- und Konfigurationsleitfaden](docs/admin/ADMIN_GUIDE_de.md) |
| **English** | [English Administrator & System Configuration Guide](docs/admin/ADMIN_GUIDE_en.md) |
| **繁體中文 (Traditional Chinese)** | [繁體中文系統管理者完整指南](docs/admin/ADMIN_GUIDE_zh-TW.md) |
| **简体中文 (Simplified Chinese)** | [简体中文系统管理员完整指南](docs/admin/ADMIN_GUIDE_zh-CN.md) |
| **日本語 (Japanese)** | [日本語システム管理者総合ガイド](docs/admin/ADMIN_GUIDE_ja.md) |
| **Tiếng Việt (Vietnamese)** | [Hướng dẫn Quản trị viên và Cấu hình Hệ thống](docs/admin/ADMIN_GUIDE_vi.md) |
| **Español (Spanish)** | [Guía del Administrador del Sistema y Configuración en Español](docs/admin/ADMIN_GUIDE_es.md) |
| **Français (French)** | [Guide d'administration et de configuration du système en français](docs/admin/ADMIN_GUIDE_fr.md) |

---

## Wichtigste Neuerungen (Changelog & Major Updates)

### 🚀 Version 2.3.3-2.0.0 (Große Architekturmodernisierung)

1. **Modernisierung des Backend-Kerns (Spring 5.3 + Hibernate 5.6 + JUnit 5)**:
   - Upgrade auf Spring Framework 5.3.37; veraltetes `HibernateTemplate` und `TimerFactoryBean` entfernt.
   - Upgrade auf Hibernate ORM 5.6.15.Final mit nativer `SessionFactory`-Verwaltung und JPA-konformen Abfragen.
   - Volltextsuche auf native Lucene-API umgestellt; Abhängigkeit vom veralteten `spring-modules-lucene` entfernt.
   - Komplette Umstellung der Unit-Tests auf JUnit 5 (Jupiter).
2. **Sicherheitsarchitektur (Spring Security 5.8 + nahtlose BCrypt-Migration)**:
   - Acegi Security 1.0.7 durch Spring Security 5.8.14 ersetzt.
   - `JtracHybridPasswordEncoder`: Erkennt alte MD5-Passwort-Hashes und konvertiert sie beim erfolgreichen Benutzer-Login automatisch in sichere BCrypt-Hashes.
3. **Web-Präsentationsschicht (Apache Wicket 9.16.0)**:
   - Wicket 1.3.7 durch Wicket 9.16.0 ersetzt; Modelle und Komponenten vollständig typisiert (`IModel<T>`).
   - Kompatibel mit modernen Servlet 4.0-Containern (Jetty 10.0.26, Jetty 12, Tomcat 9, Tomcat 10+).
4. **Paginierung für Benutzer und Projekte (Pagination & Config)**:
   - Dynamische Paginierung für `UserListPage` und `SpaceListPage` (10, 25, 50, 100, Alle).
   - Parameter `users.list.pageSize` und `spaces.list.pageSize` in der `config`-Tabelle registriert.
5. **Ajax-Fehlerbehebung bei Rollenzuweisung (Role Allocation Ajax Fix)**:
   - Event-Handler auf DOM-Standard `"change"` umgestellt und Schutzmechanismen implementiert.
6. **Globaler statischer Ressourcenfilter (StaticResourceFilter)**:
   - Behebt 404-Bildfehler bei `../resources/*` in verschachtelten URLs und ergänzt fehlende Icons.
7. **Modellbindung bei Datei-Uploads korrigiert**:
   - `FileUploadField` in `ItemFormPage`, `ItemViewFormPanel` und `ExcelImportPage` an dedizierte `ListModel` gebunden; Laufzeit-Ausnahmen behoben.
8. **Datenbank-Upgrade & SQL-Leitfaden**:
   - Bereitstellung von [`etc/sql/upgrade-to-2.0.0.sql`](etc/sql/upgrade-to-2.0.0.sql) für MySQL, PostgreSQL, SQL Server, Oracle.
   - Integrierter `HsqldbDatabaseMigrator` für die automatische HSQLDB 1.8 -> 2.x Migration.

---

## Technologien & Architektur

- **Sprache**: Java 11 / 17
- **Web-Framework**: Apache Wicket 9.16.0
- **IoC-Container**: Spring Framework 5.3.37
- **Sicherheit**: Spring Security 5.8.14 (BCrypt)
- **ORM & Persistenz**: Hibernate ORM 5.6.15.Final
- **Unterstützte Datenbanken**: HSQLDB 2.x, MySQL / MariaDB, PostgreSQL, Microsoft SQL Server, Oracle
- **Unterstützte Web-Container**:
  - **Jetty 10.x** (Nativ, verifiziert auf Jetty 10.0.26)
  - **Jetty 12.x** (Nativ über `ee8`-Modul)
  - **Tomcat 9.x** (Nativ)
  - **Tomcat 10.x / 11.x** (Über `webapps-javaee/` oder `jakartaee-migration`)
- **Build-Tool**: Apache Maven 3.9+
- **Spezifikationen**: OpenSpec v1.12.0, Antigravity

---

## Schnellstart: Build & Deployment

### 1. Anwendung erstellen (WAR)
```bash
# Quellcode kompilieren (erfordert JDK 11 oder JDK 17)
mvn clean compile

# Tests ausführen und WAR erstellen
mvn package

# Schneller Build (ohne Tests)
mvn package -DskipTests
```
Ausgabe: `target/jtrac.war`.

### 2. Datenbank-Upgrade (beim Upgrade von 2.3.3-1.0.0)
- Bei externen Datenbanken (MySQL, PostgreSQL etc.): Führen Sie [`etc/sql/upgrade-to-2.0.0.sql`](etc/sql/upgrade-to-2.0.0.sql) aus.
- Bei eingebetteter HSQLDB: Die Migration erfolgt beim Serverstart vollautomatisch mit Backup.

### 3. Eigenständiges HTML-Export-Tool erstellen (CLI)
```bash
mvn clean package -f tools/jtrac-exporter/pom.xml -DskipTests
# Ausgabe: tools/jtrac-exporter.jar
```

---

## Lizenz

JTrac ist Open-Source-Software unter der [Apache Software License, Version 2.0](license.txt).
