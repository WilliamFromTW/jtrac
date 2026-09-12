# JTrac Release Notes - 2.3.3-2.1.0-beta

[English](release-2.3.3-2.1.0_en.md) | [繁體中文](release-2.3.3-2.1.0_zh-TW.md) | [简体中文](release-2.3.3-2.1.0_zh-CN.md) | [日本語](release-2.3.3-2.1.0_ja.md) | [Tiếng Việt](release-2.3.3-2.1.0_vi.md) | [Deutsch](release-2.3.3-2.1.0_de.md) | [Español](release-2.3.3-2.1.0_es.md) | [Français](release-2.3.3-2.1.0_fr.md)

---

[![Java](https://img.shields.io/badge/Java-11%20%7C%2017-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](../../license.txt)
[![Version](https://img.shields.io/badge/Version-2.3.3--2.1.0--beta-orange.svg)](../../pom.xml)
[![Status](https://img.shields.io/badge/Status-Beta%20Preview-yellow.svg)](release-2.3.3-2.1.0_en.md)
[![Wicket](https://img.shields.io/badge/Wicket-9.16.0-blue.svg)](https://wicket.apache.org/)
[![Spring](https://img.shields.io/badge/Spring-5.3.37-brightgreen.svg)](https://spring.io/)

> [!NOTE]
> **Current Status: Pre-release / Beta Preview - Living Release Notes**  
> This document is a living release log. Throughout the Beta verification phase, any subsequent enhancements, configuration adjustments, and bug fixes will be continuously appended here.

---

## Table of Contents
1. [Core Highlights Overview](#1-core-highlights-overview)
2. [🤖 AI Query Copilot (Ollama Integration)](#2--ai-query-copilot-ollama-integration)
3. [📦 Dependency Upgrade & Java 11 Warning Elimination](#3--dependency-upgrade--java-11-warning-elimination)
4. [🎨 UI/UX Modernization & Accessibility Enhancements](#4--uiux-modernization--accessibility-enhancements)
5. [🛡️ Production Security Hardening & Guardrails](#5--production-security-hardening--guardrails)
6. [⚙️ System Configuration & Stability Fixes](#6--system-configuration--stability-fixes)
7. [Upgrade & Compatibility Guidance](#7-upgrade--compatibility-guidance)

---

## 1. Core Highlights Overview

Building upon the core modernization of 2.0.0, JTrac 2.3.3-2.1.0-beta introduces the revolutionary **AI Query Copilot (powered by Ollama)**, upgrades the foundational XML engine to eliminate Java 11 reflective access warnings, dramatically enhances UI accessibility (4-stage font scaling with A+++ mode and 3-state dark/light themes), and reinforces production security.

---

## 2. 🤖 AI Query Copilot (Ollama Integration)

1. **Two-Phase Query Expansion with Anti-Injection Safeguards**:
   - Integrates local or server-side Ollama LLMs to parse user email subjects and bodies, automatically extracting bilingual entity keywords and technical synonyms.
   - Enforces strict `<untrusted_user_query>` sandboxing to prevent Prompt Injection and unauthorized data leakage.
2. **Hybrid Weighted Retrieval & Scoring**:
   - Scores tickets across Summary (+3), Detail (+1), Comments (+1), and Attachments (+1), with a +5 bonus for cross-lingual English/localized matches.
   - Registers configurable global parameter `llm.retrieval.max_tickets` (default 50).
3. **Map-Reduce Ingestion Pipeline**:
   - **Map Phase**: Analyzes candidate tickets and document attachments (up to 100,000 characters per file; supports PDF, Word, Excel, TXT, LOG, CSV) into intermediate staging Markdown.
   - **Reduce Phase**: Synthesizes the staged summaries into three structured sections:
     1. Executive Summary
     2. Key Findings & Resolution
     3. Next Actions & Recommendations
   - Guaranteed `finally` cleanup of temporary staging files, ensuring zero disk leaks.
4. **Standalone Offline HTML Report Attachment (`JTrac-AI-Report-[yyyyMMdd-HHmm].html`)**:
   - **Minimal Email Body**: Streamlined notification with ticket links, completely eliminating email client rendering breaks.
   - **Full-featured Standalone HTML**: Built in-memory via `ByteArrayResource` (under 3ms, zero disk I/O).
   - **Modern Design**: Crisp table borders (`border-collapse: collapse`), native `<details>` expandable cards, dark/light theme switching (`@media prefers-color-scheme: dark`), and print expansion.
5. **Multilingual Prompt Engineering Guide & 4 Real-world Examples**:
   - Published comprehensive practical guides [`docs/llm/PROMPT_EXAMPLES_*.md`](../llm/PROMPT_EXAMPLES_en.md) across 8 languages.
6. **Ticket Space Grouping, ID DESC Sorting & 100% Air-gapped Offline Mermaid.js Integration**:
   - **Space-Grouped Sub-tables & Newest-First ID DESC Ordering**: Overhauled email summary tables and HTML dossier reports to group candidate tickets into individual sub-tables per authorized Space with ticket count headers. Tickets inside each Space are strictly sorted by ticket ID descending (`ID DESC`); email notifications remain clean and professional without noisy Mermaid disclaimers.
   - **100% Self-Contained Offline Mermaid.js Engine**: Packaged the full UMD bundle of Mermaid.js (v10.9.1) into the Java Classpath and directly embedded it inside generated HTML reports, completely eliminating external CDN network dependencies. Includes automatic dark/light theme switching (`prefers-color-scheme`) and try-catch syntax fallback rendering.
   - **Two-Tier Flowchart Prompts with Double-Quote Guardrails**: Enforced Mermaid `flowchart TD/LR` generation across both the Map phase (per-ticket troubleshooting workflows) and Reduce phase (executive summary & next actions), mandating double-quoted node labels (e.g. `A["Node text"]`) to prevent syntax breaks.

---

## 3. 📦 Dependency Upgrade & Java 11 Warning Elimination

1. **Upgrade `dom4j` to `2.1.4`**:
   - Upgraded legacy `dom4j:1.6.1` (from 2005) to `org.dom4j:dom4j:2.1.4`.
   - Fixed generic node casting in [`Metadata.java`](../../src/main/java/info/jtrac/domain/Metadata.java).
   - Completely resolved `WARNING: An illegal reflective access operation has occurred (org.dom4j.io.SAXContentHandler)` under Tomcat 9 / Java 11.

---

## 4. 🎨 UI/UX Modernization & Accessibility Enhancements

1. **4-Stage Font Scaling Cycle**:
   - Supports 100% (Standard), 115% (Comfortable), 130% (Crisp), and **A+++ Extra Large Mode (145%)**.
   - Includes Anti-FOUC script and table structure protections, persisting preferences via `localStorage`.
2. **3-State Theme Switcher**:
   - One-click seamless toggle between Auto, Light, and Dark modes using a single clean icon.
3. **Unified Search Box & Smart Navigation**:
   - Unified input box with embedded submit button, divider line, and enlarged tap target.
   - Smart RefId jump (typing `PROJ-123` navigates directly to the ticket), with superuser global search support.
4. **Mobile Responsive Experience (RWD)**:
   - Navigation drawer for mobile devices.
   - Explicit ticket IDs in summaries and Bottom-Sheet history detail modal.
   - Centered capsule pagination on mobile; desktop jump to first/last page and total page count.

---

## 5. 🛡️ Production Security Hardening & Guardrails

1. **Global Security Headers Filter**:
   - Injects `X-Frame-Options: SAMEORIGIN`, `X-Content-Type-Options: nosniff`, `Strict-Transport-Security`, `Content-Security-Policy`, and `Referrer-Policy`.
2. **Search Engine Shield (`robots.txt`)**:
   - Deploys default `robots.txt` disallowing search crawlers from indexing sensitive ticket data.
3. **Guardrails & Tamper Resistance**:
   - Space guest role warning.
   - Query parameter whitelist filtering.
   - Form double-click submission prevention.

---

## 6. ⚙️ System Configuration & Stability Fixes

1. **Wicket i18n Debug Warning Suppression**:
   - Added `status.nullValid = ` across all 8 language bundles.
2. **Config Page Boolean Control Modernization**:
   - Refactored brittle checkbox switches to robust `IndicatingDropDownChoice`.
3. **Explicit Database Driver Registration**:
   - Explicitly registers JDBC drivers for single-connection and lightweight test datasources.
4. **Automatic UTF-8 Text Attachment Detection**:
   - Detects text attachment encoding and injects charset headers to prevent garbled text.
5. **Context-Relative Logo Resolution**:
   - Resolves system header logo paths correctly behind reverse proxies.

---

## 7. Upgrade & Compatibility Guidance

- **Database Upgrade**: Fully compatible with 2.3.3-2.0.0; **no schema migration script is required**.
- **WAR Deployment**: Replace the existing `ROOT.war` with `target/jtrac.war`.
- **Related Links**:
  - [JTrac AI Email Query & Prompt Writing Practical Guide](../llm/PROMPT_EXAMPLES_en.md)
  - [Previous Release Notes (2.3.3-2.0.0)](release-2.3.3-2.0.0_en.md)
