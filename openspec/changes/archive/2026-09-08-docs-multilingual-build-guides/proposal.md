## Why

目前 JTrac 專案根目錄缺乏標準的 `README.md` 首頁導覽，且缺乏針對現代化建置流程（包含本機環境設定、Maven 指令、自動相依套件快取下載機制與 WAR 包第三方函式庫封裝邏輯）的完整說明文件。

為了使多國開發者（包含英語、繁體中文、簡體中文、日語、越南語使用者）能迅速理解並成功建置 JTrac，本提案旨在建立全方位的多語系編譯建置說明文件，並於專案首頁提供多語系導覽與架構說明。

## What Changes

- **建立專案首頁主文件**：在根目錄建立 [`README.md`](file:///W:/developer/project/github/jtrac/README.md)，包含專案簡介、技術棧概覽、快速啟動指引與 5 國語言建置文件的超連結導覽。
- **建立 5 國語言編譯建置指南**：在 `docs/build/` 目錄下建立 5 份獨立 Markdown 文件：
  - [`docs/build/BUILD_zh-TW.md`](file:///W:/developer/project/github/jtrac/docs/build/BUILD_zh-TW.md)（繁體中文）
  - [`docs/build/BUILD_en.md`](file:///W:/developer/project/github/jtrac/docs/build/BUILD_en.md)（English）
  - [`docs/build/BUILD_zh-CN.md`](file:///W:/developer/project/github/jtrac/docs/build/BUILD_zh-CN.md)（简体中文）
  - [`docs/build/BUILD_ja.md`](file:///W:/developer/project/github/jtrac/docs/build/BUILD_ja.md)（日本語）
  - [`docs/build/BUILD_vi.md`](file:///W:/developer/project/github/jtrac/docs/build/BUILD_vi.md)（Tiếng Việt）
- **核心建置機制解說收錄**：
  - 詳細記錄 JDK 8/11、Maven 3.9+ 與 `w:\developer\maven.bat` 環境配置。
  - 完整解說 Maven 全自動自遠端下載相依套件至使用者快取目錄 `~/.m2/repository` 的機制。
  - 完整解說 `mvn package` 打包時，所有 53 個第三方 JAR 檔自動封裝進 WAR 檔 `WEB-INF/lib/` 內、無須手動安裝至伺服器的機制。

## Capabilities

### New Capabilities
- `build-documentation`: 定義專案多語系建置編譯說明文件與相依函式庫封裝透明化規範。

### Modified Capabilities
<!-- 無既有規格需修改 -->

## Impact

- 受影響檔案：
  - [`README.md`](file:///W:/developer/project/github/jtrac/README.md)（新增）
  - `docs/build/BUILD_*.md`（新增共 5 個語系檔案）
- 程式碼邏輯與執行期無任何破壞性變更，純文件健全度與開發者體驗提升。
