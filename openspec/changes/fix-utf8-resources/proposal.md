## Why

在 Windows 繁體中文環境（預設平台編碼為 MS950）下使用 Maven 3.9.9 與 JDK 11 建置 JTrac 專案時，`maven-resources-plugin` 會因全域資源過濾開啟（`<filtering>true</filtering>`）及語系檔編碼衝突（`messages_de.properties` 內含 Latin-1 字元 `Ü`），拋出 `MalformedInputException: Input length = 1` 導致建置中斷。

此外，多國語系檔中存在 Apache Wicket 框架前端動態標籤變數（如 `${label}`），不應納入 Maven 建置期的變數替換範圍。為徹底解決建置失敗並杜絕未來的字元編碼異常，本提案將精準限定資源過濾範圍，並全面將資源檔規範化為標準 UTF-8 編碼。

## What Changes

- **精準限定 Maven 資源過濾範圍**：在 `pom.xml` 中將資源過濾調整為僅對 `jtrac-version.properties` 啟用 `filtering: true`，其餘資源檔（包含所有語系檔）設為 `filtering: false`。
- **統一 Maven 編譯編碼**：在 `pom.xml` 的 `maven-compiler-plugin` 顯式聲明 `<encoding>UTF-8</encoding>`。
- **多國語系資源檔全面 UTF-8 規範化**：
  - 將 [`messages_de.properties`](file:///W:/developer/project/github/jtrac/src/main/resources/messages_de.properties) 轉為標準 UTF-8 編碼（修正 Latin-1 字元 `Ü`）。
  - 將 [`messages_nl.properties`](file:///W:/developer/project/github/jtrac/src/main/resources/messages_nl.properties) 轉為標準 UTF-8 編碼（修正 Latin-1 字元 `ï`）。
  - 確保專案中 100% 的屬性檔皆為合法 UTF-8 格式。

## Capabilities

### New Capabilities
- `i18n-resources`: 定義多國語系資源檔案之 UTF-8 編碼規範與 Maven 資源處理隔離規則。

### Modified Capabilities
<!-- 無既有規格需修改 -->

## Impact

- 受影響檔案：
  - [`pom.xml`](file:///W:/developer/project/github/jtrac/pom.xml)
  - [`src/main/resources/messages_de.properties`](file:///W:/developer/project/github/jtrac/src/main/resources/messages_de.properties)
  - [`src/main/resources/messages_nl.properties`](file:///W:/developer/project/github/jtrac/src/main/resources/messages_nl.properties)
- 外部系統與 API：無相容性影響，不破壞既有 Wicket 標籤功能與 Java 執行期相容性。
