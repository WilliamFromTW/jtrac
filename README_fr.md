# JTrac (Fork Amélioré)

[English](README.md) | [繁體中文](README_zh-TW.md) | [简体中文](README_zh-CN.md) | [日本語](README_ja.md) | [Tiếng Việt](README_vi.md) | [Deutsch](README_de.md) | [Español](README_es.md) | [Français](README_fr.md)

---

[![Java](https://img.shields.io/badge/Java-8%20%7C%2011-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](license.txt)
[![Fork From](https://img.shields.io/badge/Fork%20From-JTrac%202.3.3-blue)](https://jtrac.info)
[![Version](https://img.shields.io/badge/Version-2.3.3--1.0.0-blue.svg)](pom.xml)
[![OpenSpec](https://img.shields.io/badge/OpenSpec-v1.12.0-brightgreen.svg)](openspec/specs/README.md)

Ce projet est un fork modernisé et amélioré de [JTrac 2.3.3 (https://jtrac.info)](https://jtrac.info). Il vise à fournir un système de suivi des anomalies (Issue Tracking) léger, hautement compatible, doté de capacités d'archivage statique hors ligne et d'une interface utilisateur modernisée. Le développement de ce projet est guidé par les processus de spécifications OpenSpec v1.12.0 et assisté par Antigravity 1.1.27 pour le refactoring architectural, la vérification et le contrôle qualité du code.

---

## Guides de Compilation Multilingues / Multilingual Build Guides

| Langue / Language | Guide de compilation / Build Guide |
|---|---|
| **Français (French)** | [Guide de compilation et d'exécution en français](docs/build/BUILD_fr.md) |
| **English** | [English Build & Compilation Guide](docs/build/BUILD_en.md) |
| **繁體中文 (Traditional Chinese)** | [繁體中文編譯與建置完整指南](docs/build/BUILD_zh-TW.md) |
| **简体中文 (Simplified Chinese)** | [简体中文编译与构建完整指南](docs/build/BUILD_zh-CN.md) |
| **日本語 (Japanese)** | [日本語ビルド・コンパイル詳細ガイド](docs/build/BUILD_ja.md) |
| **Tiếng Việt (Vietnamese)** | [Hướng dẫn biên dịch và đóng gói Tiếng Việt](docs/build/BUILD_vi.md) |
| **Deutsch (German)** | [Deutsche Bau- und Kompilierungsanleitung](docs/build/BUILD_de.md) |
| **Español (Spanish)** | [Guía de construcción y compilación en español](docs/build/BUILD_es.md) |

---

## Guides de l'Administrateur Système Multilingues / Multilingual Administrator Guides

| Langue / Language | Guide de l'administrateur / Admin Guide |
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

## Mises à Jour Majeures dans ce Fork (Changelog & Major Updates)

### 1. Nettoyage de Modules : Suppression Complète du Module Wiki (Removed Wiki Module)
- Suppression totale du code source, des composants d'interface et des icônes de navigation du module Wiki obsolète afin d'alléger le système et de se concentrer sur le cœur du suivi des anomalies.

### 2. Nouvelles Fonctionnalités (New Features)
- **Exportation HTML par lot et téléchargement ZIP depuis le Web** :
  - Intégration d'une action « Exporter HTML » dans l'en-tête de navigation principale. Les utilisateurs peuvent sélectionner plusieurs espaces (avec raccourcis « Tout sélectionner / Tout désélectionner ») et télécharger une archive ZIP contenant les historiques de discussion statiques HTML et leurs pièces jointes.
- **Contrôle strict des autorisations d'espace (Permission Guardrails)** :
  - Double vérification de sécurité côté serveur et côté client : les utilisateurs standard ne peuvent visualiser, sélectionner et exporter que les espaces dont ils sont membres autorisés (les administrateurs conservent un accès global). Un mécanisme de protection pour les sélections vides fournit des retours clairs.
- **Outil en ligne de commande autonome d'exportation HTML (`tools/jtrac-exporter.jar`)** :
  - Connexion directe via JDBC pour exporter les bases de données en rapports HTML responsifs et multilingues sans lancer de serveur web.
  - Mode sombre (Dark Mode) 100 % hors ligne intégré en pur CSS, ne nécessitant aucune connexion Internet ni CDN externe.
- **Couverture d'internationalisation complète (Full i18n Coverage)** :
  - Révision approfondie et finalisation des traductions en chinois traditionnel (`zh_TW`) et chinois simplifié (`zh_CN`).
  - Ajout des libellés manquants dans tous les fichiers de ressources (comme le statut d'activation d'espace `space_form.isActive`).

### 3. Corrections de Bogues (Bug Fixes)
- **Centrage des boutons et badges de l'en-tête de navigation (Navigation Header Centering)** :
  - Correction des problèmes d'alignement vertical et de décalage à droite des éléments de l'en-tête (« Tableau de bord », « Recherche », « Exporter HTML », « Options », « Déconnexion », nom d'utilisateur).
  - Standardisation avec Flexbox inline-flex pour un centrage parfait, bordures arrondies de 3px, survol animé des boutons interactifs et typage dédié des badges d'information.
- **Nettoyage de l'historique des discussions (Thread History Cleanup)** :
  - Suppression de l'entrée initiale redondante « Open » sans commentaire ni modification lors de l'exportation HTML, pour ne conserver que les échanges et transitions pertinents.
- **Résolution de `LazyInitializationException` sous Hibernate** :
  - Correction des exceptions de session fermée lors de l'accès aux métadonnées d'espace grâce à un chargement précoce actif (Eager Fetch / Initialize).
- **Correction du filtrage de ressources Maven UTF-8 et des images binaires corrompues** :
  - Adoption d'un encodage natif UTF-8 sans conversions obsolètes `native2ascii`.
  - Correction du filtrage de ressources Maven qui endommageait les fichiers images binaires (gif, png, jar) lors du packaging.

---

## Technologies et Architecture (Technologies & Architecture)

- **Langage principal** : Java 1.8 / 11
- **Framework Web** : Apache Wicket 1.3
- **Conteneur et IoC** : Spring Framework 2.5
- **ORM et Base de données** : Hibernate 3 / HSQLDB embarqué ; compatible MySQL, PostgreSQL, MS SQL Server, Oracle
- **Outil de build** : Apache Maven 3.9+ (packaging WAR)
- **Outils de développement et spécifications** : OpenSpec v1.12.0, Antigravity 1.1.27
- **Encodage des caractères** : 100% UTF-8

---

## Démarrage Rapide : Compilation et Déploiement (Quick Start)

### 1. Compiler l'application principale (WAR)
```bash
# Compiler le code source
mvn compile

# Empaqueter le fichier WAR (en ignorant les tests)
mvn package -DskipTests
```
L'archive générée est disponible sous : `target/jtrac.war`, prête à être déployée sur Jetty ou Tomcat.

### 2. Construire et exécuter l'exportateur HTML en ligne de commande (CLI)
```bash
# Empaqueter le JAR exécutable autonome
mvn clean package -f tools/jtrac-exporter/pom.xml -DskipTests
# Résultat: tools/jtrac-exporter.jar

# Exemple d'exportation depuis une base locale HSQLDB
java -jar tools/jtrac-exporter.jar \
  --db-url="jdbc:hsqldb:file:./data/db/jtrac;shutdown=true;readonly=true" \
  --attachments-dir="./data/attachments" \
  --out="./export-output" \
  --lang=fr
```

---

## Spécifications et Documentation (Specifications)

Ce projet suit le développement piloté par les spécifications OpenSpec :
- [Répertoire principal des spécifications](openspec/specs/README.md)
- [Spécification de l'exportateur HTML](openspec/specs/html-exporter/spec.md)
- [Spécification des langues et ressources](openspec/specs/i18n-resources/spec.md)
- [Spécification des guides de compilation](openspec/specs/build-documentation/spec.md)
- [Règles de développement du projet](.agents/AGENTS.md)

---

## Licence (License)

JTrac est un logiciel libre publié sous la [Licence logicielle Apache, Version 2.0](license.txt).
