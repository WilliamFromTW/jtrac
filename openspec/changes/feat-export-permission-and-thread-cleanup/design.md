# 技術設計說明書：專案空間匯出權限控管、複選打包與討論串首筆 OPEN 快照清理

## Context

請參閱 [`proposal.md`](file:///W:/developer/project/github/jtrac/openspec/changes/feat-export-permission-and-thread-cleanup/proposal.md)。在現行架構中，網頁 HTML 匯出採用連線池行內資料庫萃取（`DatabaseReader`）與記憶體即時串流壓縮（`ZipStreamExporter`）。目前匯出範圍僅提供全庫與當前單一空間，且全庫查詢未綁定當前登入使用者的空間存取權限；無任何空間權限之使用者仍可見導覽按鈕。此外，JTrac 原生在建立議題時會強制於 `history` 資料表寫入一筆無留言的 OPEN 狀態快照，導致討論串歷程輸出時必然存在資訊重複與計數偏差。

## Goals / Non-Goals

**Goals:**
- **安全防護 (Defense-in-Depth)**：
  - 若登入使用者無任何空間存取權限（且非 SuperUser），全站導覽列隱藏匯出入口；直接以 URL 訪問予以阻斷。
  - 確保匯出嚴格受限於使用者具備存取權限的專案空間清單（`user.getSpaces()`，SuperUser 例外具備全域權限），任何未授權空間絕不被讀取或寫入 ZIP 檔。
- **直覺易用的複選打包 (Multi-Selection Checkboxes)**：提供核取方塊清單，允許使用者自由勾選一個至多個空間進行自訂組合打包下載（並具備未勾選阻擋防呆）。
- **乾淨的討論串呈現 (Clean Discussion Thread & Zero-Reply Collapse)**：
  - 在網頁匯出與獨立 CLI 工具中，自動移除 Issue 建立時自動產生的首筆無留言 OPEN 快照。
  - 若移除後該議題完全無任何後續留言或狀態變更，**完全隱藏**「💬 討論串歷程與留言記錄」區塊，僅呈現上方的主議題卡片。
- **CLI 工具多空間支援 (CLI Multi-Space Support)**：`jtrac-exporter.jar` 支援以逗號分隔傳入多個空間代碼（例如 `--space=PROJ1,PROJ2`）。

**Non-Goals:**
- 不變更 JTrac 既有之資料庫 Schema 或實體資料。
- 不修改一般使用者的系統角色模型或既有專案空間指派規則。

## Decisions

### 1. 導覽列顯示控管 (Header Navigation Guardrail)
- **決策**：在 `HeaderPanel.java` 中，為 `exportHtml` 導覽連結設置可見性：
  ```java
  @Override
  public boolean isVisible() {
      return user.isSuperUser() || spaces.size() > 0;
  }
  ```
  若使用者為一般帳號且未分配任何空間，導覽列不呈現「📦 匯出 HTML」按鈕。
- 在 `HtmlExportPage.java` 建構子中加入防呆檢查：若 `!user.isSuperUser() && permittedSpaces.isEmpty()`，拋出攔截例外或重定向回首頁。

### 2. 動態授權空間複選 (Checkboxes & UI Scope Options)
- **決策**：在 `HtmlExportPage` 中改用 `List<String> selectedSpaces` 作為表單綁定模型。
  - 介面提供 `CheckBoxMultipleChoice` 或客製之 CheckBox 清單，選項鍵值為空間的 `prefixCode`，顯示名稱為 `SpaceName [PREFIX]`。
  - 預設選取：若當前已處於某一專案空間且在授權清單內，預設勾選該空間；否則預設全選所有授權空間。
  - 前端/表單校驗：若 `selectedSpaces.isEmpty()`，阻擋提交並提示「請至少勾選一個專案空間進行匯出」。

### 3. 後端雙重防禦安全驗證 (Defense-in-Depth)
- **決策**：
  1. 在 `HtmlExportPage.onSubmit` 驗證表單送出之 `selectedSpaces`：每一項空間代碼必須存在於當前使用者的 `permittedSpaces`（或使用者具備 SuperUser 身分），否則拋出 `AccessDeniedException`。
  2. 在 `ExportConfig` 中新增 `Set<String> targetSpacePrefixCodes`（取代原本單一的 `spaceFilter`，支援多空間過濾）。
  3. 在 `DatabaseReader.readAllData()` 中：
     ```java
     if (config.getTargetSpacePrefixCodes() != null && !config.getTargetSpacePrefixCodes().isEmpty()) {
         spaces.removeIf(s -> !config.getTargetSpacePrefixCodes().contains(s.getPrefixCode().toUpperCase()));
     }
     ```

### 4. 討論串快照排除與零留言區塊完全隱藏
- **決策**：
  1. 在 `DatabaseReader` 讀取並關聯議題歷史記錄（`readHistory`）完成後，針對每個議題檢查 `item.getHistoryList()`：
     - 若首筆符合：`status == 1 && (comment == null || comment.trim().isEmpty())`，直接自清單中移除（`histories.remove(0)`）。
  2. 在 `HtmlGenerator.java` 渲染討論串時：
     ```java
     if (item.getHistoryList() != null && !item.getHistoryList().isEmpty()) {
         // 渲染 discussion-thread-container
     }
     // 若為空，完全不輸出任何 discussion-thread-container HTML
     ```

### 5. CLI 工具參數升級
- **決策**：在 `ExportConfig.java` 解析 `--space` 參數時，以逗號 `,` 分割並去除空白，填入 `targetSpacePrefixCodes` 集合，使獨立 CLI 工具與網頁匯出邏輯完全一致。

## Risks / Trade-offs

- **[Risk] 使用者若沒有任何空間權限時的操作體驗** → **Mitigation**: 導覽列不出現該按鈕；即便手動鍵入網址也會安全重定向。
- **[Risk] 建立 Issue 時附帶附件的狀況** → **Mitigation**: 建立時上傳的附件會同時關聯至 `item.attachments`。即便移除首筆 history 快照，該附件依然完整呈現在主議題卡片的「附件清單」中，絕不遺失。

## Migration Plan

本變更純粹修改 Wicket 頁面控制項、資料讀取器過濾條件與多語系資源檔，無需執行任何資料庫遷移指令，重新編譯部署 WAR 檔即可。
