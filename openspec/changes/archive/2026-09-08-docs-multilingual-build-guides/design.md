## Context

JTrac 是一套知名的 Java Issue Tracking Web 系統，專案基於 Maven 構建。目前專案缺少現代化的根目錄 `README.md`，亦無針對各國開發者之建置說明。使用者明確要求建立 5 國語言（英文、繁體中文、簡體中文、日語、越南語）之編譯說明文件，並於說明中著重強調 Maven 依賴自動下載至 `~/.m2` 與 WAR 內部包含第三方 lib 之機制。

## Goals / Non-Goals

**Goals:**
- 在 `docs/build/` 目錄建立 5 國語言的建置與編譯指南（`BUILD_zh-TW.md`, `BUILD_en.md`, `BUILD_zh-CN.md`, `BUILD_ja.md`, `BUILD_vi.md`）。
- 在各指南中完整說明 Maven 快取機制（`~/.m2/repository`）及 WAR 包自動包含 53 個第三方 JAR 之運作原理。
- 建立專案根目錄 [`README.md`](file:///W:/developer/project/github/jtrac/README.md)，具備多語系導覽與超連結。

**Non-Goals:**
- 修改 Java 原始程式碼或調整 Maven 構建邏輯（此為文件專項提案）。

## Decisions

### 決策 1：多語系檔案命名與存放策略
- **決定**：統一存放於 `docs/build/`，採用 `BUILD_<lang_code>.md` 命名（如 `BUILD_zh-TW.md`, `BUILD_en.md`, `BUILD_ja.md`, `BUILD_vi.md`）。
- **考量**：避免根目錄過於雜亂，集中於 `docs/build/` 維護，且根目錄 `README.md` 可以整齊排列各語系跳轉連結。

### 決策 2：根目錄 README.md 的多語系呈現方式
- **決定**：在 `README.md` 頂部提供醒目的多語系切換徽章/導覽清單（Language Navigation Bar），隨後提供英文與繁體中文並行之快速入門說明，引導各語系讀者點擊對應之完整建置指南。

## Risks / Trade-offs

- **[風險]**：多語系文件日後若 build 指令更新需同步維護多個檔案。
  - **緩解措施**：文件採用模組化結構，核心指令塊保持一致的 Maven 指令語法，降低維護不同步的風險。
