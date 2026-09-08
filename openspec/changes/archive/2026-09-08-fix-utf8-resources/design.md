## Context

目前 JTrac 使用 Maven 3.9.9 與 JDK 11 建置，在 Windows 預設 MS950 編碼環境下，`maven-resources-plugin` 會因全域開啟資源過濾而試圖將非 UTF-8 檔案以 UTF-8 解碼，造成建置中斷。動機與背景參見 `proposal.md`。

## Goals / Non-Goals

**Goals:**
- 將 Maven 資源過濾嚴格限制在 `jtrac-version.properties`，避免語系檔與靜態資源被不當處置。
- 將非 UTF-8 資源檔案轉換為純 UTF-8 編碼，達成 100% 資源檔 UTF-8 規範化。
- 明確設定 `maven-compiler-plugin` 編碼為 UTF-8，確保跨平台建置輸出一致。
- 順利執行 `mvn compile` 與 `mvn test-compile` 通過驗證。

**Non-Goals:**
- 重構 Spring MessageSource 或引入新的 i18n 函式庫。
- 修改 Wicket 框架的前端標籤機制或頁面結構。
- 變更其他未受影響的 Java 原始程式碼。

## Decisions

### 決策 1：Maven 資源過濾分離配置 (Resource Filtering Separation)
- **決定**：在 `pom.xml` 中配置兩個 `<resource>` 節點，分別處理 `filtering: true`（僅包含 `jtrac-version.properties`）與 `filtering: false`（排除 `jtrac-version.properties`）。
- **考量方案**：
  - *方案 A（選用）*：標準 Maven `<resource>` 隔離配置。優點是完全遵循 Maven 標準、清晰且不依賴特殊外掛。
  - *方案 B*：在 `maven-resources-plugin` 中指定 `<encoding>` 或 `<escapeWindowsPaths>`。缺點是語系檔中的 `${label}` 仍會暴露在 filtering 處理中，有潛在誤替換風險。

### 決策 2：資源檔案字元轉換方式 (Raw UTF-8 vs Unicode Escape)
- **決定**：將 `messages_de.properties`（Latin-1 `Ü`）與 `messages_nl.properties`（Latin-1 `ï`）直接轉換為標準 UTF-8 字元。
- **考量方案**：
  - *原生 UTF-8（選用）*：符合使用者對專案檔案現代化與全面 UTF-8 的要求，且 Java 9+ 原生支援 UTF-8 properties。
  - *轉為 \uXXXX*：雖然相容舊版 Java 8 ResourceBundle，但檔案中會存在轉義碼，不利於直接閱讀維護。

## Risks / Trade-offs

- **[風險]**：若日後在舊版 Java 8 環境且未使用 Spring ReloadableMessageSource 的情況下載入原生 UTF-8 字元，可能有極小機率在該字元處出現解碼問題。
  - **緩解措施**：目前建置與開發環境皆已使用 JDK 11（支援 JEP 226），且修改範圍僅限德文與荷蘭文各 1 個重音字母，風險極低。
