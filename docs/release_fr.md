# JTrac (Fork Amélioré)

[English](release_en.md) | [繁體中文](release_zh-TW.md) | [简体中文](release_zh-CN.md) | [日本語](release_ja.md) | [Tiếng Việt](release_vi.md) | [Deutsch](release_de.md) | [Español](release_es.md) | [Français](release_fr.md)

---

[![Java](https://img.shields.io/badge/Java-11%20%7C%2017-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](../license.txt)
[![Fork From](https://img.shields.io/badge/Fork%20From-JTrac%202.3.3-blue)](https://jtrac.info)
[![Version](https://img.shields.io/badge/Version-2.3.3--2.0.0-blue.svg)](../pom.xml)
[![Wicket](https://img.shields.io/badge/Wicket-9.16.0-blue.svg)](https://wicket.apache.org/)
[![Spring](https://img.shields.io/badge/Spring-5.3.37-brightgreen.svg)](https://spring.io/)
[![OpenSpec](https://img.shields.io/badge/OpenSpec-v1.12.0-brightgreen.svg)](../openspec/specs/README.md)

Ce projet est dérivé de la version [JTrac 2.3.3 (https://jtrac.info)](https://jtrac.info). Il est dédié à fournir un système de suivi et d'enregistrement textuel de Q&R (Questions/Réponses) léger, hautement compatible, doté de fonctionnalités d'archivage statique hors ligne et d'une interface utilisateur soignée, parfaitement adapté à la gestion des connaissances (Knowledge Management), avec support de pièces jointes pour les flux de travail complexes. Le développement de ce projet s'appuie sur le processus piloté par spécifications OpenSpec v1.12.0 et bénéficie de l'assistance d'Antigravity pour la refactorisation de l'architecture et le contrôle qualité.

---

## Guides de Compilation Multilingues / Multilingual Build Guides

| Langue / Language | Guide de Compilation / Build Guide |
|---|---|
| **Français (French)** | [Guide complet de compilation et de construction en français](build/BUILD_fr.md) |
| **English** | [English Build & Compilation Guide](build/BUILD_en.md) |
| **繁體中文 (Traditional Chinese)** | [繁體中文編譯與建置完整指南](build/BUILD_zh-TW.md) |
| **简体中文 (Simplified Chinese)** | [简体中文编译与构建完整指南](build/BUILD_zh-CN.md) |
| **日本語 (Japanese)** | [日本語ビルド・コンパイル詳細ガイド](build/BUILD_ja.md) |
| **Tiếng Việt (Vietnamese)** | [Hướng dẫn biên dịch và đóng gói Tiếng Việt](build/BUILD_vi.md) |
| **Deutsch (German)** | [Deutscher Kompilierungs- und Build-Leitfaden](build/BUILD_de.md) |
| **Español (Spanish)** | [Guía de compilación y construcción en español](build/BUILD_es.md) |

---

## Guides de l'Administrateur Système / Multilingual Administrator Guides

| Langue / Language | Guide de l'Administrateur / Admin Guide |
|---|---|
| **Français (French)** | [Guide d'administration et de configuration du système en français](admin/ADMIN_GUIDE_fr.md) |
| **English** | [English Administrator & System Configuration Guide](admin/ADMIN_GUIDE_en.md) |
| **繁體中文 (Traditional Chinese)** | [繁體中文系統管理者完整指南](admin/ADMIN_GUIDE_zh-TW.md) |
| **简体中文 (Simplified Chinese)** | [简体中文系统管理员完整指南](admin/ADMIN_GUIDE_zh-CN.md) |
| **日本語 (Japanese)** | [日本語システム管理者総合ガイド](admin/ADMIN_GUIDE_ja.md) |
| **Tiếng Việt (Vietnamese)** | [Hướng dẫn Quản trị viên và Cấu hình Hệ thống](admin/ADMIN_GUIDE_vi.md) |
| **Deutsch (German)** | [Deutscher Systemadministrator- und Konfigurationsleitfaden](admin/ADMIN_GUIDE_de.md) |
| **Español (Spanish)** | [Guía del Administrador del Sistema y Configuración en Español](admin/ADMIN_GUIDE_es.md) |

---

## Principales Mises à Jour (Changelog & Major Updates)

### 🚀 Version 2.3.3-2.0.0 (Mise à Niveau Architecturale Majeure)

1. **Mise à Niveau du Cœur Backend (Spring 5.3 + Hibernate 5.6 + JUnit 5)** :
   - Mise à niveau vers Spring Framework 5.3.37 ; suppression de `HibernateTemplate` et `TimerFactoryBean`.
   - Migration vers Hibernate ORM 5.6.15.Final avec gestion native de `SessionFactory` et requêtes standard JPA.
   - Indexation et recherche plein texte migrées vers l'API Lucene native (découplage de `spring-modules-lucene`).
   - Tests unitaires mis à niveau sous JUnit 5 (Jupiter).
2. **Refonte Complète de la Sécurité (Spring Security 5.8 + Migration BCrypt)** :
   - Remplacement complet d'Acegi Security 1.0.7 obsolète par Spring Security 5.8.14.
   - Encodeur hybride `JtracHybridPasswordEncoder` : valide les anciens hachages MD5 et les convertit automatiquement et de manière transparente en BCrypt lors de la connexion réussie de l'utilisateur.
3. **Couche de Présentation Web (Apache Wicket 9.16.0)** :
   - Remplacement de Wicket 1.3.7 par Wicket 9.16.0 avec support complet des types génériques (`IModel<T>`).
   - Compatible avec les conteneurs de servlets Servlet 4.0 (Jetty 10.0.26, Jetty 12, Tomcat 9, Tomcat 10+).
4. **Pagination des Utilisateurs et Espaces (Pagination & Config)** :
   - Prise en charge de la pagination personnalisable dans `UserListPage` et `SpaceListPage` (10, 25, 50, 100, Tout).
   - Enregistrement des clés `users.list.pageSize` et `spaces.list.pageSize` dans la table `config`.
5. **Correction des Événements Ajax d'Attribution de Rôles** :
   - Passage des événements Ajax à la norme DOM `"change"` avec garde-fous pour les désélections.
6. **Filtre Global des Ressources Statiques (StaticResourceFilter)** :
   - Résolution définitive des erreurs 404 sur les images `../resources/*` dans les URL imbriquées et restauration des icônes manquantes.
7. **Correction de la Liaison de Modèle pour les Téléversements** :
   - Attribution explicite d'un `ListModel` dédié pour `FileUploadField` dans `ItemFormPage` et `ItemViewFormPanel`.
8. **Mise à Niveau de la Base de Données et Script SQL** :
   - Script SQL dédié [`etc/sql/upgrade-to-2.0.0.sql`](etc/sql/upgrade-to-2.0.0.sql) pour MySQL, PostgreSQL, SQL Server et Oracle.
   - Outil intégré `HsqldbDatabaseMigrator` pour la migration automatique de HSQLDB 1.8 vers 2.x au démarrage.
9. **Suppression du Module Excel et Allègement de l'Archive WAR (Excel Module Removal & POI Deprecation)** :
   - Suppression complète des fonctionnalités d'import/export Excel et de la bibliothèque Apache POI, réduisant la taille du fichier WAR de plus de 3 Mo.
10. **Amélioration du Paquet de Sauvegarde Complète (`jtrac-dump.sql`)** :
    - L'archive ZIP de sauvegarde complète inclut désormais un script SQL ANSI autonome complet (`jtrac-dump.sql`), avec DDL ANSI, annotations pour dialectes MySQL/PostgreSQL/HSQLDB, instructions INSERT ordonnées selon les clés étrangères et commandes de réinitialisation des séquences pour la migration ou la reprise après sinistre.
11. **Partitionnement des Pièces Jointes par Projet & Indexation Plein Texte Lucene** :
    - **Structure Partitionnée par ID Numérique de Projet (Option C)** : Stockage dans `${jtrac.home}/attachments/{spaceId}/{prefix}_{filename}`, insensible aux renommages de projet.
    - **Mécanisme de Secours à Double Lecture (Dual-Read Fallback)** : Repli automatique sur le répertoire racine et le dossier d'isolement (`attachments/0_ORPHAN/`), garantissant 0% d'erreurs 404.
    - **Migration Automatique au Démarrage** : Détection et déplacement automatique des anciennes pièces jointes vers les sous-dossiers projets, avec indicateur de complétion (`.attachment_migrated`).
    - **Extraction Multi-format** : Prise en charge de `.xlsx`, `.docx` (parseur streaming OpenXML JDK natif), `.pdf` (Apache PDFBox 2.0.31), `.txt`, `.csv`, `.md`, `.log`, avec `SmartCharsetDetector` pour éliminer le mojibake.
    - **Gardes-fous et File d'Attente Asynchrone** : Limites configurables de 10 Mo par fichier et 50 000 caractères ; indexation en tâche de fond (`ExecutorService`) pour des temps de réponse d'upload instantanés.

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

JTrac est un logiciel libre distribué sous la [Licence Apache, Version 2.0](../license.txt).
