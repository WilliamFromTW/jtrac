## 1. 資源檔案 UTF-8 規範化

- [x] 1.1 將 `src/main/resources/messages_de.properties` 轉為標準 UTF-8 編碼，並透過字元檢查確認 Latin-1 (0xDC) 已轉為 UTF-8 (0xC3 0x9C)
- [x] 1.2 將 `src/main/resources/messages_nl.properties` 轉為標準 UTF-8 編碼，並透過字元檢查確認 Latin-1 (0xEF) 已轉為 UTF-8 (0xC3 0xAF)

## 2. Maven 構建設定調整

- [x] 2.1 在 `pom.xml` 中調整 `<resources>` 結構，僅對 `jtrac-version.properties` 設定 `filtering: true`，其餘資源檔設定 `filtering: false`
- [x] 2.2 在 `pom.xml` 的 `maven-compiler-plugin` 顯式聲明 `<encoding>UTF-8</encoding>`

## 3. 建置與編譯驗證

- [x] 3.1 執行 `cmd /c "call W:\developer\maven.bat && mvn compile"` 驗證資源過濾與 137 個 Java 檔編譯成功
- [x] 3.2 執行 `cmd /c "call W:\developer\maven.bat && mvn test-compile"` 驗證 16 個 Test 測試類別編譯成功
