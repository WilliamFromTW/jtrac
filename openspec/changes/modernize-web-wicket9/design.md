# 技術設計文件：Web 表現層現代化（Wicket 9 與 Servlet 4.0 容器適配）

## Context

見 `proposal.md`。第一階段現代化已成功將後端核心升級至 Spring 5.3.37、Spring Security 5.8.14 與 Hibernate 5.6.15。然而，Web 表現層依然是基於已發表超過 15 年的 Apache Wicket 1.3.7，且 `web.xml` 與 `applicationContext-scheduler.xml` 中殘留了已在 Spring 5 絕版的類別（如 `Log4jConfigListener` 與 `TimerFactoryBean`）。為了能在現代 Servlet 4.0 容器（如已配置於 `W:\developer\jetty-10.0.26` 的 Jetty 10）與 Java 11 環境中穩定發布並提供現代化使用者體驗，必須升級 Web 表現層。

## Goals / Non-Goals

**Goals:**
- 移除 `web.xml` 與 `applicationContext-scheduler.xml` 中的廢棄類別，解決容器啟動即拋例外問題。
- 將 Apache Wicket 系列依賴全面由 1.3.7 升級至 **Wicket 9.16.x**。
- 重構 `JtracApplication` 頁面掛載（使用現代 `mountPage()` API）與 Session 機制。
- 全面泛型化改造 Wicket 頁面、表單與模型（`IModel<T>`）。
- 整合自動部署流程：編譯後自動發布至 `W:\developer\jetty-10.0.26\webapps\ROOT.war`，並於 Jetty 10 驗證通過。

**Non-Goals:**
- 不在此階段升級至 Wicket 10（Wicket 10 強制要求 Jakarta EE 9 / `jakarta.servlet`，會與目前穩定的 Spring 5.3 衝突）。
- 不在此階段進行大規模 CSS 框架改版（外觀重塑保留至後續獨立階段）。

## Decisions

### 1. 選擇 Wicket 9.16.x 作為目標版本
- **理由**：Wicket 9.x 原生支援 Java 11 與 Servlet 4.0，且完全沿用 `javax.servlet.*` 命名空間，與目前的 Spring 5.3.37 和 Jetty 10.0.26 是 100% 原生相容。
- **替代方案**：升級至 Wicket 10.x（缺點：強依賴 `jakarta.servlet`，會迫使整套 Spring 5 與 Hibernate 5 必須升級至 Spring 6，牽涉範圍過大）。

### 2. 排程器現代化：採用 `ScheduledExecutorFactoryBean`
- **理由**：Spring 5 已移除基於 `java.util.Timer` 的 `TimerFactoryBean`，官方正式替代方案為基於 JUC `ScheduledExecutorService` 的 `ScheduledExecutorFactoryBean` 與 `MethodInvokingRunnable`。
- **替代方案**：使用 Quartz 或 Spring `@Scheduled` 註解（後者需要開啟 annotation-driven scheduling，修改範圍大於 Bean 定義改寫）。

### 3. 頁面掛載採用標準 `mountPage` API
- **理由**：Wicket 1.3 的 `mount(new IndexedParamUrlCodingStrategy(...))` 與 `QueryStringUrlCodingStrategy` 已完全廢棄。Wicket 9 提供簡潔強大的 `mountPage("/item/#{refId}", ItemViewPage.class)` 語法。

## Risks / Trade-offs

- **[風險 1] Wicket 1.3 到 9.x 的組件 API 斷代重大變更**
  → *因應策略*：分層重構：先重構核心 `JtracApplication` 與基礎頁面（`BasePage`、`LoginPage`），再循序推進至業務頁面與面板。
- **[風險 2] 泛型化引發大量編譯警告或錯誤**
  → *因應策略*：逐一標註明確的實體泛型型別（如 `CompoundPropertyModel<Item>`、`ListItem<Item>`），消除型別轉換隱患。
- **[風險 3] 本機 Jetty 10 啟動連接埠衝突**
  → *因應策略*：將連接埠預設配置為 `8888`，避免與系統常見之 8080 衝突，並提供一鍵停機腳本 `stop-jtrac.bat`。

## Migration Plan

1. 修復 `web.xml` 與 `applicationContext-scheduler.xml`。
2. 升級 `pom.xml` 依賴至 Wicket 9.16.x。
3. 遷移 `JtracApplication`、`JtracSession` 與核心 Page/Panel。
4. 執行 `mvn clean package` 驗證編譯與產包。
5. 複製產物至 `W:\developer\jetty-10.0.26\webapps\ROOT.war`。
6. 透過 `start-jtrac.bat` 實機啟動並驗證瀏覽器存取。
