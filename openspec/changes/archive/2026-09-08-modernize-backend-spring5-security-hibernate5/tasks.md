# 實作任務清單：後端核心現代化（Spring Security 5.8 與 Hibernate 5.6 DAO 重構）

## 1. 相依性與建置環境現代化

- [x] 1.1 更新 `pom.xml` 相依性，移除 Acegi 1.0.7、`spring-modules-lucene` 與 JUnit 3.8.1，引進 Spring 5.3.37、Spring Security 5.8.14、Hibernate 5.6.15.Final 與 JUnit 5 (Jupiter)，並透過 `mvn dependency:resolve` 驗證解析成功
- [x] 1.2 清理廢棄 Acegi CAS 類別（`JtracCasProxyTicketValidator.java`）與設定檔（`applicationContext-acegi-cas.xml`），並確認專案內無殘留 CAS 參照

## 2. Spring Security 認證機制重構

- [x] 2.1 實作雙模密碼編碼器 `JtracHybridPasswordEncoder`，支援舊版 MD5 雜湊驗證與 BCrypt 自動升級，並撰寫測試驗證比對正確性
- [x] 2.2 重構 `User.java` 與 `UserSpaceRole.java`，實作標準 Spring Security `UserDetails` 與 `GrantedAuthority` 泛型介面
- [x] 2.3 重構 `Jtrac.java` 與 `JtracImpl.java`，實作 `UserDetailsService` 並整合密碼自動升級邏輯
- [x] 2.4 重構 `ProviderManagerFactoryBean.java` 與 `JtracLdapAuthenticationProvider.java`，遷移至 Spring Security ProviderManager 與 AuthenticationProvider
- [x] 2.5 建立 `applicationContext-security.xml` 取代 `applicationContext-acegi.xml`，並更新 Wicket 呼叫端（`JtracApplication.java`、`HeaderPanel.java`、`HtmlExportPage.java`）之 `SecurityContextHolder` 引用

## 3. Hibernate 5.6 持久層與 DAO 重構

- [x] 3.1 重構 `HibernateJtracDao.java`，徹底移除 `HibernateDaoSupport` 與 `HibernateTemplate`，改為直接注入 `SessionFactory` 並透過 `getCurrentSession()` 進行 CRUD 操作
- [x] 3.2 現代化 `HibernateJtracDao` 中的 HQL 查詢、分頁與批次操作（`bulkUpdate*`），全部採用具名參數（Named Parameters）綁定
- [x] 3.3 更新 `applicationContext.xml`，將 `LocalSessionFactoryBean` 與 `transactionManager` 升級至 `org.springframework.orm.hibernate5`，並配置 `hibernate.hbm2ddl.auto=update`
- [x] 3.4 更新 `web.xml`，將 `OpenSessionInViewFilter` 升級至 `org.springframework.orm.hibernate5.support.OpenSessionInViewFilter`

## 4. 原生 Lucene 全文檢索模組重構

- [x] 4.1 重構 `Indexer.java` 與 `IndexSearcher.java`，移除 `spring-modules-lucene` 繼承，改用原生 Lucene 索引寫入與搜尋 API
- [x] 4.2 更新 `applicationContext-lucene.xml`，配置原生 Lucene 檢索 Bean，並驗證索引建立與搜尋邏輯正常

## 5. 單元測試現代化與整體回歸驗證

- [x] 5.1 重構 `JtracTestBase.java`，採用 JUnit 5 `@ExtendWith(SpringExtension.class)` 與 `@Transactional`，更新 `JtracTest.java` 與 `UserTest.java` 至 JUnit 5 規範
- [x] 5.2 執行 `mvn test` 驗證所有單元測試通過，並執行 `mvn clean package` 驗證 WAR 包封裝成功
