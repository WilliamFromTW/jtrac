## Purpose

規範 JTrac Web 表現層以 Apache Wicket 9.16.x 進行現代化架構重構，並整合 Spring 5 核心與排程器，確保系統於現代 Servlet 4.0 容器（如 Jetty 10 / Tomcat 9）中穩定啟動，提供型別安全、高回應性之組件化 Web 介面。

## 表現層架構與請求處理流程圖

```mermaid
flowchart TD
    Client[客戶端瀏覽器] -->|HTTP Request| Jetty[Jetty 10 / Servlet 4.0 容器]
    
    subgraph WebFilters["Web Filter 鏈"]
        OSIV[OpenSessionInViewFilter (Hibernate 5.6)]
        WFilter[WicketFilter (Wicket 9.16)]
    end
    
    Jetty --> OSIV
    OSIV --> WFilter
    
    subgraph WicketApp["Wicket 9 核心 (JtracApplication)"]
        Router{URL 路由匹配}
        WFilter --> Router
        Router -->|/app/item/*| ItemViewPage[ItemViewPage]
        Router -->|/app/login| LoginPage[LoginPage]
        Router -->|/app/dashboard| DashboardPage[DashboardPage]
    end
    
    subgraph SessionMgmt["會話管理"]
        JtracSession[JtracSession (AuthenticatedWebSession)]
        SecurityCtx[Spring SecurityContext]
        JtracSession --> SecurityCtx
    end
    
    subgraph Scheduler["後台現代化排程器 (Spring 5)"]
        ScheduledExecutor[ScheduledExecutorFactoryBean]
        PollTask[executePollingTask]
        HourTask[executeHourlyTask]
        ScheduledExecutor --> PollTask
        ScheduledExecutor --> HourTask
    end
    
    ItemViewPage --> JtracSession
    LoginPage --> JtracSession
    DashboardPage --> JtracSession
```

## ADDED Requirements

### Requirement: Servlet 容器配置現代化與無廢棄類別相容
`web.xml` SHALL 徹底移除已在 Spring 5 絕版之 `Log4jConfigListener` 與過時初始化參數，確保 Servlet 容器於初始化 WebAppContext 時零例外載入。

#### Scenario: 現代容器部署無 ClassNotFoundException
- **WHEN** 部署 WAR 檔至 Jetty 10 容器並啟動 Context
- **THEN** 容器解析 `web.xml` 成功，不觸發 `Log4jConfigListener` 或任何 `ClassNotFoundException`

#### Scenario: 記錄日誌正常運作
- **WHEN** 系統運行並輸出除錯或資訊日誌
- **THEN** 透過 SLF4J 橋接至底層日誌框架正常寫入日誌檔案

### Requirement: 後台排程器現代化
系統排程設定 SHALL 以 Spring 5 原生 `ScheduledExecutorFactoryBean` 取代已移除之 `TimerFactoryBean`，提供可靠之定時背景任務排程。

#### Scenario: 系統啟動正常載入排程 Bean
- **WHEN** Spring 載入 `applicationContext-scheduler.xml`
- **THEN** 系統正確初始化 `ScheduledExecutorFactoryBean`，按既定頻率（每 5 分鐘輪詢、每小時排程）觸發背景作業

### Requirement: Apache Wicket 9 核心應用程式初始化與頁面掛載
`JtracApplication` SHALL 繼承 Wicket 9 之 `AuthenticatedWebApplication`，並採用現代化 `mountPage()` API 掛載所有業務頁面。

#### Scenario: 頁面路由掛載與解析
- **WHEN** 使用者存取 `/app/item/TEST-1`
- **THEN** Wicket 9 路由解析請求並正確導向至 `ItemViewPage`，並保留參數綁定

#### Scenario: 授權會話管理
- **WHEN** 使用者進行登入驗證
- **THEN** `JtracSession` 透過 Spring Security 驗證憑證並持久化使用者身分至 Wicket WebSession

### Requirement: Wicket 組件與模型泛型化重構
所有 Wicket 頁面、表單、面板與自訂元件 SHALL 全面適配 Wicket 9 泛型規範，禁止使用未帶泛型參數之裸型別（Raw Types）。

#### Scenario: 表單與模型資料綁定
- **WHEN** 提交議題建立表單
- **THEN** Wicket 9 `CompoundPropertyModel<Item>` 強型別綁定表單輸入欄位至 `Item` 實體，無型別轉換錯誤
