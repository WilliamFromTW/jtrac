# JTrac 發布說明 (Release Notes) - 2.3.3-1.0.0

[English](release-2.3.3-1.0.0_en.md) | [繁體中文](release-2.3.3-1.0.0_zh-TW.md) | [简体中文](release-2.3.3-1.0.0_zh-CN.md) | [日本語](release-2.3.3-1.0.0_ja.md) | [Tiếng Việt](release-2.3.3-1.0.0_vi.md) | [Deutsch](release-2.3.3-1.0.0_de.md) | [Español](release-2.3.3-1.0.0_es.md) | [Français](release-2.3.3-1.0.0_fr.md)

---

[![Java](https://img.shields.io/badge/Java-8%20%7C%2011-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.8+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](../license.txt)
[![Fork From](https://img.shields.io/badge/Fork%20From-JTrac%202.3.3-blue)](https://jtrac.info)
[![Version](https://img.shields.io/badge/Version-2.3.3--1.0.0-blue.svg)](../pom.xml)

本專案為源自 [JTrac 2.3.3 (https://jtrac.info)](https://jtrac.info) 版本之首個現代化維護與功能增強發布版本。本版本聚焦於現代化標準維護、安全性權限守門、UTF-8 國際化資源補全，以及支援完整的離線靜態歸檔輸出。

---

## 📦 版本 2.3.3-1.0.0 重點更新 (Major Features)

1. **模組清理 (Module Cleanup)**：
   - 完全移除過時且無維護的 Wiki 模組，大幅精簡系統程式碼與不必要之依賴。
2. **Web 端批次 HTML 匯出與 ZIP 下載 (Batch HTML Export & ZIP Download)**：
   - 於 Web 管理介面實作多專案空間批次離線封存功能，支援將完整 Issue 資料與歷程匯出為自包含之靜態 HTML，並打包為 ZIP 檔一鍵下載。
3. **嚴格空間權限守門員 (Strict Space Permission Guardrails)**：
   - 強化空間權限校驗機制，確保一般非管理者使用者僅能檢視、搜尋與匯出其具備存取授權之專案空間，防止越權存取。
4. **獨立命令列匯出工具 (`tools/jtrac-exporter.jar`)**：
   - 提供免伺服器之獨立 CLI 匯出工具，可直接透過 JDBC 連線至資料庫輸出響應式靜態 HTML 報表，支援深色主題與離線瀏覽。
5. **8 國多語系全面覆蓋 (Full Multilingual Standard)**：
   - 全專案標準化 UTF-8 編碼，補全並校正 8 種語言（英文、繁體中文、簡體中文、日語、越南語、德語、西班牙語、法語）之介面語言與訊息資源檔。

---

## 📜 歷史版本 (Release History)

- **後續版本**：[JTrac 發布說明 - 2.3.3-2.0.0](release-2.3.3-2.0.0_zh-TW.md)

---

## 授權條款 (License)

JTrac 為開源軟體，遵循 [Apache Software License, Version 2.0](../license.txt)。
