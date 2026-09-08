# 技術設計說明書：專案空間匯出權限控管與討論串首筆 OPEN 快照清理

## Context

請參閱 [`proposal.md`](file:///W:/developer/project/github/jtrac/openspec/changes/feat-export-permission-and-thread-cleanup/proposal.md)。在現行架構中，網頁 HTML 匯出採用連線池行內資料庫萃取（`DatabaseReader`）與記憶體即時串流壓縮（`ZipStreamExporter`）。目前匯出範圍僅提供全庫與當前單一空間，且全庫查詢未綁定當前登入使用者的空間存取權限。此外，JTrac 原生在建立議題時會強制於 `history` 資料表寫入一筆無留言的 OPEN 狀態快照，導致討論串歷程輸出時必然存在資訊重複與計數偏差。

## Goals / Non-Goals

**Goals:**
- **安全防護 (Defense-in-Depth)**：確保網頁匯出嚴格受限於使用者具備存取權限的專案空間清單（`user.getSpaces()`，SuperUser 例外具備全域權限），任何未授權空間絕不被讀取或寫入 ZIP 檔。
- **直覺易用的範圍選擇 (Flexible Scope Selection)**：提供使用者友善的空間單選項，包含「全部我有權限的專案空間 (共 N 個)」以及所有可存取的單一空間清單。
- **乾淨的討論串呈現 (Clean Discussion Thread)**：在網頁匯出與獨立 CLI 工具中，自動移除 Issue 建立時自動產生的首筆無留言 OPEN 快照，使討論串更新數與序號正確反映實質回覆。

**Non-Goals:**
- 不變更 JTrac 既有之資料庫 Schema 或實體資料（`history` 資料表依然保留 JTrac 原始的稽核快照，純於資料萃取層進行安全與呈現清理）。
- 不修改一般使用者的系統角色模型或既有專案空間指派規則。

## Decisions

### 1. 動態授權空間解析與 UI 呈現
- **決策**：在 `HtmlExportPage` 初始化時，依使用者身分動態提取允許的空間清單：
  ```java
  List<Space> permittedSpaces;
  if (user.isSuperUser()) {
      permittedSpaces = getJtrac().findSpaces();
  } else {
      permittedSpaces = new ArrayList<>(user.getSpaces());
  }
  ```
- **單選項結構**：
  - 首項固定為 `"ALL"`，顯示文字格式化為：`全部我有權限的專案空間 (共 N 個專案)`。
  - 後續各項為個別空間的 PrefixCode，顯示文字為：`SpaceName [PREFIX]`。
  - 預設選取：若當前已處於某一空間且使用者具備該空間權限，預設選取該空間；否則預設選取 `"ALL"`。

### 2. 後端雙重防禦安全驗證 (Defense-in-Depth)
- **決策**：
  1. 在 `HtmlExportPage.onSubmit` 驗證表單送出之 `selectedScope`：若非 `"ALL"` 且不屬於 `permittedSpaces`（且非 SuperUser），直接拋出安全性拒絕存取例外，中斷請求。
  2. 在 `ExportConfig` 中擴充 `Set<String> allowedSpacePrefixCodes`。
  3. 在 `DatabaseReader.readAllData()` 中，若 `allowedSpacePrefixCodes` 不為空，執行：
     ```java
     spaces.removeIf(s -> !config.getAllowedSpacePrefixCodes().contains(s.getPrefixCode().toUpperCase()));
     ```
     杜絕任何未授權空間資料進入後續議題讀取與附件打包流程。

### 3. 首筆無留言 OPEN 快照排除邏輯
- **決策**：在 `DatabaseReader` 讀取並關聯議題歷史記錄（`readHistory`）完成後，針對每個議題進行快照清理：
  ```java
  List<HistoryDto> histories = item.getHistoryList();
  if (!histories.isEmpty()) {
      HistoryDto first = histories.get(0);
      boolean isInitialOpenSnapshot = (first.getStatus() != null && first.getStatus() == 1)
              && (first.getComment() == null || first.getComment().trim().isEmpty());
      if (isInitialOpenSnapshot) {
          histories.remove(0);
      }
  }
  ```
- **效益**：
  - 集中在 `DatabaseReader` 處理，`HtmlGenerator` 不需要散落過濾邏輯。
  - `item.getHistoryList().size()` 天然反映實際留言數量。
  - 獨立 CLI 工具（`tools/jtrac-exporter`）與網頁內置匯出引擎完全共享此邏輯，行為 100% 一致。

## Risks / Trade-offs

- **[Risk] 使用者若沒有任何空間權限時的操作體驗** → **Mitigation**: 若 `permittedSpaces` 為空，介面呈現醒目提示「您目前尚未被指派任何專案空間」，並停用下載按鈕。
- **[Risk] 建立 Issue 時附帶附件的狀況** → **Mitigation**: 建立時上傳的附件會同時關聯至 `item.attachments` 與 `history.attachment`。即便移除首筆 history 快照，該附件依然完整呈現在主議題卡片的「附件清單」中，絕不遺失。

## Migration Plan

本變更純粹修改 Wicket 匯出頁面控制項、資料讀取器過濾條件與多語系資源檔，無需執行任何資料庫遷移指令，重新編譯部屬 WAR 檔即可熱更新。
