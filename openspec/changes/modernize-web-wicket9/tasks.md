# 實作任務清單：Web 表現層現代化（Wicket 9 與 Jetty 10 容器適配）

## 1. Servlet 容器相容性修復與排程器現代化

- [x] 1.1 清理 `web.xml`：徹底移除已廢棄之 `Log4jConfigListener` 與 `webAppRootKey` / `log4jConfigLocation`，並確認 XML 格式驗證通過
- [x] 1.2 重構 `applicationContext-scheduler.xml`：以 Spring 5 原生 `ScheduledExecutorFactoryBean` 取代過時 `TimerFactoryBean`，並驗證 Spring 載入無 ClassNotFound 錯誤

## 2. Wicket 9.16.x 核心相依性與 Application 基礎重構

- [x] 2.1 更新 `pom.xml`，將 `wicket`, `wicket-extensions`, `wicket-spring`, `wicket-auth-roles` 升級至 9.16.0，並透過 `mvn dependency:resolve` 驗證解析成功
- [x] 2.2 重構 `JtracApplication.java`：適配 Wicket 9 核心生命週期、頁面掛載（`mountPage()`）、自訂 RequestCycle 與 SpringComponentInjector
- [x] 2.3 重構 `JtracSession.java`：繼承 Wicket 9 `AuthenticatedWebSession`，重構登入授權與使用者憑證狀態管理

## 3. Wicket 頁面與組件模型泛型化重構

- [ ] 3.1 重構基礎頁面 `BasePage.java`、`HeaderPanel.java` 與認證頁面（`LoginPage.java`、`LogoutPage.java`）
- [ ] 3.2 重構核心業務頁面（`DashboardPage.java`、`ItemViewPage.java`、`ItemFormPage.java`）之泛型 Model 與 Form 綁定
- [ ] 3.3 重構管理設定頁面與報表匯出頁面（`UserListPage.java`、`SpaceListPage.java`、`HtmlExportPage.java`）

## 4. 打包部署與 Jetty 10 實機上線驗證

- [ ] 4.1 執行 `mvn clean package` 驗證專案編譯與 WAR 封裝產出
- [ ] 4.2 自動將 `target/jtrac.war` 部署至 `W:\developer\jetty-10.0.26\webapps\ROOT.war`
- [ ] 4.3 透過 `start-jtrac.bat` 在 Jetty 10 (Java 11) 實機啟動，驗證 WebContext 狀態為 STARTED 且首頁存取正常
