# JTrac (Fork Modernisé et Amélioré)

[English](README.md) | [繁體中文](README_zh-TW.md) | [简体中文](README_zh-CN.md) | [日本語](README_ja.md) | [Tiếng Việt](README_vi.md) | [Deutsch](README_de.md) | [Español](README_es.md) | [Français](README_fr.md)

---

[![Java](https://img.shields.io/badge/Java-11%20%7C%2017-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](license.txt)
[![Fork From](https://img.shields.io/badge/Fork%20From-JTrac%202.3.3-blue)](https://jtrac.info)
[![Version](https://img.shields.io/badge/Version-2.3.3--2.0.0-blue.svg)](pom.xml)
[![Wicket](https://img.shields.io/badge/Wicket-9.16.0-blue.svg)](https://wicket.apache.org/)
[![Spring](https://img.shields.io/badge/Spring-5.3.37-brightgreen.svg)](https://spring.io/)
[![OpenSpec](https://img.shields.io/badge/OpenSpec-v1.12.0-brightgreen.svg)](openspec/specs/README.md)

Ce projet est un fork modernisé et amélioré de [JTrac 2.3.3 (https://jtrac.info)](https://jtrac.info). Il vise à fournir un système de suivi des anomalies (Issue Tracking) léger, hautement compatible, doté de capacités d'archivage statique hors ligne, d'une interface utilisateur moderne et des derniers standards de sécurité d'entreprise. Le développement est guidé par OpenSpec v1.12.0 avec l'assistance d'Antigravity.

---

## Guides de Compilation Multilingues / Multilingual Build Guides

| Langue / Language | Guide de Compilation / Build Guide |
|---|---|
| **Français (French)** | [Guide complet de compilation et de construction en français](docs/build/BUILD_fr.md) |
| **English** | [English Build & Compilation Guide](docs/build/BUILD_en.md) |
| **繁體中文 (Traditional Chinese)** | [繁體中文編譯與建置完整指南](docs/build/BUILD_zh-TW.md) |
| **简体中文 (Simplified Chinese)** | [简体中文编译与构建完整指南](docs/build/BUILD_zh-CN.md) |
| **日本語 (Japanese)** | [日本語ビルド・コンパイル詳細ガイド](docs/build/BUILD_ja.md) |
| **Tiếng Việt (Vietnamese)** | [Hướng dẫn biên dịch và đóng gói Tiếng Việt](docs/build/BUILD_vi.md) |
| **Deutsch (German)** | [Deutscher Kompilierungs- und Build-Leitfaden](docs/build/BUILD_de.md) |
| **Español (Spanish)** | [Guía de compilación y construcción en español](docs/build/BUILD_es.md) |

---

## Guides de l'Administrateur Système / Multilingual Administrator Guides

| Langue / Language | Guide de l'Administrateur / Admin Guide |
|---|---|
| **Français (French)** | [Guide d'administration et de configuration du système en français](docs/admin/ADMIN_GUIDE_fr.md) |
| **English** | [English Administrator & System Configuration Guide](docs/admin/ADMIN_GUIDE_en.md) |
| **繁體中文 (Traditional Chinese)** | [繁體中文系統管理者完整指南](docs/admin/ADMIN_GUIDE_zh-TW.md) |
| **简体中文 (Simplified Chinese)** | [简体中文系统管理员完整指南](docs/admin/ADMIN_GUIDE_zh-CN.md) |
| **日本語 (Japanese)** | [日本語システム管理者総合ガイド](docs/admin/ADMIN_GUIDE_ja.md) |
| **Tiếng Việt (Vietnamese)** | [Hướng dẫn Quản trị viên và Cấu hình Hệ thống](docs/admin/ADMIN_GUIDE_vi.md) |
| **Deutsch (German)** | [Deutscher Systemadministrator- und Konfigurationsleitfaden](docs/admin/ADMIN_GUIDE_de.md) |
| **Español (Spanish)** | [Guía del Administrador del Sistema y Configuración en Español](docs/admin/ADMIN_GUIDE_es.md) |

---

## Principales Mises à Jour (Changelog & Major Updates)

### 🚀 Version 2.3.3-2.0.0 (Modernisation Architecturale Majeure)

1. **Modernisation du Cœur Backend (Spring 5.3 + Hibernate 5.6 + JUnit 5)** :
   - Mise à niveau vers Spring Framework 5.3.37 ; suppression de `HibernateTemplate` et `TimerFactoryBean`.
   - Migration vers Hibernate ORM 5.6.15.Final avec gestion native de `SessionFactory` et requêtes standard JPA.
   - Indexation et recherche plein texte migrées vers l'API Lucene native (découplage de `spring-modules-lucene`).
   - Tests unitaires modernisés sous JUnit 5 (Jupiter).
2. **Refonte Complète de la Sécurité (Spring Security 5.8 + Migration BCrypt)** :
   - Remplacement complet d'Acegi Security 1.0.7 obsolète par Spring Security 5.8.14.
   - Encodeur hybride `JtracHybridPasswordEncoder` : valide les anciens hachages MD5 et les convertit automatiquement et de manière transparente en BCrypt lors de la connexion réussie de l'utilisateur.
3. **Couche de Présentation Web Moderne (Apache Wicket 9.16.0)** :
   - Remplacement de Wicket 1.3.7 par Wicket 9.16.0 avec support complet des types génériques (`IModel<T>`).
   - Compatible avec les conteneurs de servlets modernes (Jetty 10.0.26, Jetty 12, Tomcat 9, Tomcat 10+).
4. **Pagination des Utilisateurs et Espaces (Pagination & Config)** :
   - Prise en charge de la pagination personnalisable dans `UserListPage` et `SpaceListPage` (10, 25, 50, 100, Tout).
   - Enregistrement des clés `users.list.pageSize` et `spaces.list.pageSize` dans la table `config`.
5. **Correction des Événements Ajax d'Attribution de Rôles** :
   - Passage des événements Ajax à la norme DOM `"change"` avec garde-fous pour les désélections.
6. **Filtre Global des Ressources Statiques (StaticResourceFilter)** :
   - Résolution définitive des erreurs 404 sur les images `../resources/*` dans les URL imbriquées et restauration des icônes manquantes.
7. **Correction de la Liaison de Modèle pour les Téléversements** :
   - Attribution explicite d'un `ListModel` dédié pour `FileUploadField` dans `ItemFormPage`, `ItemViewFormPanel` et `ExcelImportPage`.
8. **Mise à Niveau de la Base de Données et Script SQL** :
   - Script SQL dédié [`etc/sql/upgrade-to-2.0.0.sql`](etc/sql/upgrade-to-2.0.0.sql) pour MySQL, PostgreSQL, SQL Server et Oracle.
   - Outil intégré `HsqldbDatabaseMigrator` pour la migration automatique de HSQLDB 1.8 vers 2.x au démarrage.

---

## Technologies et Architecture

- **Langage** : Java 11 / 17
- **Framework Web** : Apache Wicket 9.16.0
- **Conteneur IoC** : Spring Framework 5.3.37
- **Sécurité** : Spring Security 5.8.14 (Chiffrement BCrypt)
- **ORM & Persistance** : Hibernate ORM 5.6.15.Final
- **Bases de Données Prises en Charge** : HSQLDB 2.x (intégrée), MySQL / MariaDB, PostgreSQL, Microsoft SQL Server, Oracle
- **Conteneurs Web Pris en Charge** :
  - **Jetty 10.x** (Prise en charge native, validé sur Jetty 10.0.26)
  - **Jetty 12.x** (Prise en charge native via le module `ee8`)
  - **Tomcat 9.x** (Prise en charge native)
  - **Tomcat 10.x / 11.x** (Prise en charge via le répertoire `webapps-javaee/` ou l'outil `jakartaee-migration`)
- **Outil de Construction** : Apache Maven 3.9+
- **Spécifications** : OpenSpec v1.12.0, Antigravity

---

## Démarrage Rapide (Quick Start)

### 1. Construire l'Application (WAR)
```bash
# Compiler le code source (requiert JDK 11 ou JDK 17)
mvn clean compile

# Exécuter les tests et empaqueter le WAR
mvn package

# Empaquetage rapide (ignorer les tests)
mvn package -DskipTests
```
Fichier produit : `target/jtrac.war`.

### 2. Mise à Niveau de la Base de Données (Depuis 2.3.3-1.0.0)
- Pour MySQL / PostgreSQL / SQL Server / Oracle : Exécutez [`etc/sql/upgrade-to-2.0.0.sql`](etc/sql/upgrade-to-2.0.0.sql).
- Pour HSQLDB intégrée : La sauvegarde et la mise à niveau de structure s'exécutent automatiquement au démarrage.

### 3. Construire l'Outil d'Exportation HTML Indépendant (CLI)
```bash
mvn clean package -f tools/jtrac-exporter/pom.xml -DskipTests
# Fichier produit : tools/jtrac-exporter.jar
```

---

## Licence

JTrac est un logiciel libre distribué sous la [Licence Apache, Version 2.0](license.txt).
