# 變更提案：Web 表現層現代化（Apache Wicket 9 升級、排程器重構與 Servlet 容器相容性適配）

## Why

在第一階段完成 Spring 5.3、Spring Security 5.8 與 Hibernate 5.6 之核心現代化後，系統之 Web 表現層仍受限於 2008 年的 Apache Wicket 1.3.7，且 Web 配置中包含已在 Spring 5 絕版之過時類別（如 `Log4jConfigListener` 與 `TimerFactoryBean`）。這導致專案無法在現代 Servlet 4.0 容器（如 Jetty 10 / Tomcat 9）中部署啟動，亦無法發揮 Java 11 現代語言特性。

為使 JTrac 具備完整的現代 Web 服務能力，並能於現代容器（如 Jetty 10.0.26、Java 11）穩定運行，本階段將全面重構 Web 表現層至 Apache Wicket 9.16.x，清理 Servlet 容器配置，並現代化系統後台排程器。

## What Changes

- **容器相容性修復與配置現代化**：
  - 清理 `web.xml`：徹底移除已在 Spring 5 廢除的 `Log4jConfigListener` 與 `webAppRootKey` / `log4jConfigLocation` 配置。
  - 升級 `applicationContext-scheduler.xml`：將廢棄的 `TimerFactoryBean` 與 `MethodInvokingTimerTaskFactoryBean` 重構為 Spring 5 的 `ScheduledExecutorFactoryBean` 與 `MethodInvokingRunnable`。
- **Apache Wicket 9.16.x 框架升級**：
  - 更新 `pom.xml` 將 `wicket`, `wicket-extensions`, `wicket-spring`, `wicket-auth-roles` 升級至 9.16.x。
  - 重構 `JtracApplication`：頁面掛載（Page Mounting）改用 Wicket 9 標準 `mountPage()` API，重構 Spring Bean 注入與認證 Session 儲存策略。
  - 泛型化重構：全面升級 `JtracSession`、所有 Web 頁面與組件模型至 Wicket 9 泛型規範（`IModel<T>`、`CompoundPropertyModel<T>`、`LoadableDetachableModel<T>`）。
- **Jetty 10 本地發布與實機驗證**：
  - 建置產出自動部署至 `W:\developer\jetty-10.0.26\webapps\ROOT.war`。
  - 驗證於 Jetty 10（Java 11，Port 8888）環境下正常啟動且頁面無死角運作。

## Capabilities

### New Capabilities
- `web-presentation`: 規範 Apache Wicket 9 表現層組件模型、現代化頁面掛載、Spring 5 排程器整合與 Servlet 4.0 容器部署標準。

### Modified Capabilities
（無，本階段引入現代化 Web 表現層新規範）

## Impact

- **相依套件**：引入 `wicket-*` 9.16.x 系列，移除 1.3.7 系列。
- **原始碼架構**：`info.jtrac.wicket.*` 套件下之頁面、面板與自訂元件。
- **設定檔**：`web.xml`, `applicationContext-scheduler.xml`。
- **部署環境**：完美支援 Jetty 10.0.26 (Java 11) 與相容之 Servlet 4.0 容器。
