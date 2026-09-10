# JTrac Versionshinweise (Release Notes) - 2.3.3-1.0.0

[English](release-2.3.3-1.0.0_en.md) | [繁體中文](release-2.3.3-1.0.0_zh-TW.md) | [简体中文](release-2.3.3-1.0.0_zh-CN.md) | [日本語](release-2.3.3-1.0.0_ja.md) | [Tiếng Việt](release-2.3.3-1.0.0_vi.md) | [Deutsch](release-2.3.3-1.0.0_de.md) | [Español](release-2.3.3-1.0.0_es.md) | [Français](release-2.3.3-1.0.0_fr.md)

---

[![Java](https://img.shields.io/badge/Java-8%20%7C%2011-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.8+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](../license.txt)
[![Fork From](https://img.shields.io/badge/Fork%20From-JTrac%202.3.3-blue)](https://jtrac.info)
[![Version](https://img.shields.io/badge/Version-2.3.3--1.0.0-blue.svg)](../pom.xml)

Diese Version markiert die erste modernisierte und erweiterte Fork-Veröffentlichung von [JTrac 2.3.3 (https://jtrac.info)](https://jtrac.info). Sie konzentriert sich auf Code-Bereinigung, Zugriffsbeschränkungen, vollständige UTF-8-Unterstützung für 8 Sprachen sowie Offline-HTML-Archivierung.

---

## 📦 Hauptfunktionen von Version 2.3.3-1.0.0 (Major Features)

1. **Modul-Bereinigung (Module Cleanup)**:
   - Veraltetes und nicht mehr gepflegtes Wiki-Modul vollständig entfernt, wodurch der Codebase schlanker und Abhängigkeiten reduziert wurden.
2. **Webbasierter Batch-HTML-Export & ZIP-Download (Batch HTML Export & ZIP Download)**:
   - Multi-Space Offline-Archivierung direkt in der Web-Administrationsoberfläche implementiert. Vollständige Ticket-Historien werden als eigenständige, responsive HTML-Dateien exportiert und als ZIP-Paket heruntergeladen.
3. **Strikte Projektberechtigungen (Strict Space Permission Guardrails)**:
   - Strenge Überprüfung der Zugriffsberechtigungen, sodass reguläre Benutzer nur Projekte einsehen, durchsuchen und exportieren können, für die sie ausdrücklich autorisiert sind.
4. **Eigenständiges CLI-Export-Tool (`tools/jtrac-exporter.jar`)**:
   - Serverloses CLI-Tool zur direkten Verbindung per JDBC mit der Datenbank, um responsive statische HTML-Berichte mit Dark-Mode-Unterstützung zu erzeugen.
5. **8-Sprachen-Standard (Full Multilingual Standard)**:
   - Vollständige Umstellung auf UTF-8-Codierung und Bereitstellung konsistenter Übersetzungsprofile für 8 Sprachen (Englisch, traditionelles Chinesisch, vereinfachtes Chinesisch, Japanisch, Vietnamesisch, Deutsch, Spanisch, Französisch).

---

## 📜 Versionsverlauf (Release History)

- **Nächste Version**: [JTrac Versionshinweise - 2.3.3-2.0.0](release-2.3.3-2.0.0_de.md)

---

## Lizenz

JTrac ist Open-Source-Software unter der [Apache Software License, Version 2.0](../license.txt).
