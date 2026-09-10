# JTrac 发布说明 (Release Notes) - 2.3.3-1.0.0

[English](release-2.3.3-1.0.0_en.md) | [繁體中文](release-2.3.3-1.0.0_zh-TW.md) | [简体中文](release-2.3.3-1.0.0_zh-CN.md) | [日本語](release-2.3.3-1.0.0_ja.md) | [Tiếng Việt](release-2.3.3-1.0.0_vi.md) | [Deutsch](release-2.3.3-1.0.0_de.md) | [Español](release-2.3.3-1.0.0_es.md) | [Français](release-2.3.3-1.0.0_fr.md)

---

[![Java](https://img.shields.io/badge/Java-8%20%7C%2011-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.8+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](../license.txt)
[![Fork From](https://img.shields.io/badge/Fork%20From-JTrac%202.3.3-blue)](https://jtrac.info)
[![Version](https://img.shields.io/badge/Version-2.3.3--1.0.0-blue.svg)](../pom.xml)

本项目为源自 [JTrac 2.3.3 (https://jtrac.info)](https://jtrac.info) 版本之首个现代化维护与功能增强发布版本。本版本聚焦于现代化标准维护、安全性权限守门、UTF-8 国际化资源补全，以及支持完整的离线静态归档输出。

---

## 📦 版本 2.3.3-1.0.0 重点更新 (Major Features)

1. **模块清理 (Module Cleanup)**：
   - 完全移除过时且无维护的 Wiki 模块，大幅精简系统代码与不必要之依赖。
2. **Web 端批量 HTML 导出与 ZIP 下载 (Batch HTML Export & ZIP Download)**：
   - 于 Web 管理界面实现多项目空间批量离线归档功能，支持将完整 Issue 数据与历程导出为自包含之静态 HTML，并打包为 ZIP 文件一键下载。
3. **严格空间权限守门员 (Strict Space Permission Guardrails)**：
   - 强化空间权限校验机制，确保一般非管理员用户仅能查看、搜索与导出其具备访问授权的项目空间，防止越权访问。
4. **独立命令行导出工具 (`tools/jtrac-exporter.jar`)**：
   - 提供免服务器之独立 CLI 导出工具，可直接通过 JDBC 连接至数据库输出响应式静态 HTML 报表，支持深色主题与离线浏览。
5. **8 种多语言全面覆盖 (Full Multilingual Standard)**：
   - 全项目标准化 UTF-8 编码，补全并校正 8 种语言（英文、繁体中文、简体中文、日语、越南语、德语、西班牙语、法语）之界面语言与消息资源文件。

---

## 📜 历史版本 (Release History)

- **后续版本**：[JTrac 发布说明 - 2.3.3-2.0.0](release-2.3.3-2.0.0_zh-CN.md)

---

## 授权条款 (License)

JTrac 为开源软件，遵循 [Apache Software License, Version 2.0](../license.txt)。
