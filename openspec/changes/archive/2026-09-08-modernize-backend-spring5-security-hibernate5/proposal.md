# 提案：後端核心現代化（Spring Security 5.8 替換 Acegi 與 Hibernate 5.6 DAO 重構）

## Why

JTrac 原始架構建構於超過 15 年前的過時技術堆疊上，其中 Acegi Security 1.0.7 已終止維護多年且存在已知安全性風險，使用者密碼採用未加鹽的純 MD5 雜湊，且安全架構無法相容現代 Java 環境；此外，`HibernateJtracDao` 繼承自早期的 `HibernateDaoSupport` 並高度依賴 `HibernateTemplate`，該模板在 Spring 5 中已被徹底移除。

為推動整體 JTrac 現代化升級（相容 Java 11、Spring 5.3、Hibernate 5.6 與後續 Wicket 9），必須先將後端核心安全層與資料持久層徹底現代化，消除歷史技術債，並建立現代化單元測試機制。

## What Changes

- **以 Spring Security 5.8 取代 Acegi 1.0.7**：
  - 移除 `org.acegisecurity:acegi-security:1.0.7`。
  - 引進 `spring-security-core`、`spring-security-web`、`spring-security-config`、`spring-security-crypto` (5.8.14)。
  - 重構 `User.java` 與 `UserSpaceRole.java` 實作標準 `UserDetails` 與 `GrantedAuthority`。
  - 實作雙模相容 `JtracHybridPasswordEncoder`：驗證舊有 32 碼 MD5 密碼，並於用戶登入成功時無感自動重新雜湊升級為強安全的 BCrypt。
  - 重構 `ProviderManagerFactoryBean` 與 `JtracLdapAuthenticationProvider` 為 Spring Security 標準 Provider。
  - 清理廢棄的 Acegi CAS 單一登入相關類別與設定。
- **重構 `HibernateJtracDao` 並升級至 Hibernate 5.6 / Spring ORM 5.3**：
  - 移除 `extends HibernateDaoSupport` 與 `HibernateTemplate`，改為直接注入 `SessionFactory` 並以 `sessionFactory.getCurrentSession()` 進行類型安全的查詢。
  - 透過 `LocalSessionFactoryBean` 的 `hibernate.hbm2ddl.auto=update` 統一由 Hibernate 5 管理結構自動更新，並在 `createSchema()` 保留初始預設 admin 帳號與序列號校驗。
  - 升級 `OpenSessionInViewFilter` 至 Hibernate 5 規範。
- **原生 Lucene 全文檢索脫離 `spring-modules-lucene`**：
  - 移除已停止維護的 `spring-modules-lucene:0.8` 依賴。
  - 將 `Indexer.java` 與 `IndexSearcher.java` 改寫為輕量原生 Lucene API 實作。
- **測試架構升級至 JUnit 5 (Jupiter)**：
  - 移除 JUnit 3.8.1，導入 JUnit 5 與 `spring-test:5.3.37`。
  - 重構 `JtracTestBase.java` 適配 `@ExtendWith(SpringExtension.class)`。

## Capabilities

### New Capabilities
- `backend-security`: 現代化 Spring Security 認證機制、相容 MD5 與自動升級 BCrypt 密碼安全、LDAP 整合與清理廢棄 Acegi CAS 模組。
- `backend-persistence`: 現代化 Hibernate 5.6 持久層、原生 SessionFactory 管理、Schema 自動同步、以及脫離 spring-modules 的原生 Lucene 檢索實作。

### Modified Capabilities
（無現有 Spec 需求異動，本變更為全新後端現代化規格）

## Impact

- **主要相依性變更 (`pom.xml`)**：
  - 移除：`acegi-security:1.0.7`、`spring-modules-lucene:0.8`、`junit:3.8.1`。
  - 升級：Spring Framework 升級至 5.3.37、Hibernate ORM 升級至 5.6.15.Final、Spring Security 引進 5.8.14、JUnit 升級至 JUnit 5。
- **領域與服務層**：
  - `info.jtrac.domain.User`、`info.jtrac.domain.UserSpaceRole`。
  - `info.jtrac.Jtrac`、`info.jtrac.JtracImpl`。
  - `info.jtrac.config.ProviderManagerFactoryBean`。
- **持久層**：
  - `info.jtrac.hibernate.HibernateJtracDao`、`info.jtrac.hibernate.SchemaHelper`。
  - `src/main/webapp/WEB-INF/applicationContext.xml`、`src/main/webapp/WEB-INF/web.xml`。
- **檢索層**：
  - `info.jtrac.lucene.Indexer`、`info.jtrac.lucene.IndexSearcher`。
- **測試層**：
  - `info.jtrac.JtracTestBase` 及各單元測試案例。
