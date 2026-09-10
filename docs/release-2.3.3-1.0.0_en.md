# JTrac Release Notes - 2.3.3-1.0.0

[English](release-2.3.3-1.0.0_en.md) | [繁體中文](release-2.3.3-1.0.0_zh-TW.md) | [简体中文](release-2.3.3-1.0.0_zh-CN.md) | [日本語](release-2.3.3-1.0.0_ja.md) | [Tiếng Việt](release-2.3.3-1.0.0_vi.md) | [Deutsch](release-2.3.3-1.0.0_de.md) | [Español](release-2.3.3-1.0.0_es.md) | [Français](release-2.3.3-1.0.0_fr.md)

---

[![Java](https://img.shields.io/badge/Java-8%20%7C%2011-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.8+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](../license.txt)
[![Fork From](https://img.shields.io/badge/Fork%20From-JTrac%202.3.3-blue)](https://jtrac.info)
[![Version](https://img.shields.io/badge/Version-2.3.3--1.0.0-blue.svg)](../pom.xml)

This release marks the initial modernization and feature enhancement fork from [JTrac 2.3.3 (https://jtrac.info)](https://jtrac.info). It focuses on code cleanup, permission hardening, complete multilingual UTF-8 support, and offline static HTML archiving capabilities.

---

## 📦 Version 2.3.3-1.0.0 Major Features

1. **Module Cleanup**:
   - Completely removed obsolete and unmaintained Wiki module, streamlining codebase and dependencies.
2. **Web-based Batch HTML Export & ZIP Download**:
   - Implemented multi-space offline archiving directly from the web management interface, exporting full issue histories to self-contained responsive HTML files and downloadable ZIP bundles.
3. **Strict Space Permission Guardrails**:
   - Hardened space access controls to ensure non-admin users can only view, search, and export spaces they have explicit access permissions for.
4. **Standalone CLI Exporter (`tools/jtrac-exporter.jar`)**:
   - Provided a serverless offline HTML reporting CLI tool connecting via JDBC to generate static HTML reports with dark mode support.
5. **Full Multilingual Standard (8 Languages)**:
   - Standardized entire project on UTF-8 encoding, providing comprehensive UI translations across 8 languages (English, Traditional Chinese, Simplified Chinese, Japanese, Vietnamese, German, Spanish, French).

---

## 📜 Release History

- **Next Release**: [JTrac Release Notes - 2.3.3-2.0.0](release-2.3.3-2.0.0_en.md)

---

## License

JTrac is open-source software licensed under the [Apache Software License, Version 2.0](../license.txt).
