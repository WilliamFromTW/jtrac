# Notes de Version JTrac (Release Notes) - 2.3.3-2.1.0-beta

[English](release-2.3.3-2.1.0_en.md) | [繁體中文](release-2.3.3-2.1.0_zh-TW.md) | [简体中文](release-2.3.3-2.1.0_zh-CN.md) | [日本語](release-2.3.3-2.1.0_ja.md) | [Tiếng Việt](release-2.3.3-2.1.0_vi.md) | [Deutsch](release-2.3.3-2.1.0_de.md) | [Español](release-2.3.3-2.1.0_es.md) | [Français](release-2.3.3-2.1.0_fr.md)

---

[![Java](https://img.shields.io/badge/Java-11%20%7C%2017-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](../../license.txt)
[![Version](https://img.shields.io/badge/Version-2.3.3--2.1.0--beta-orange.svg)](../../pom.xml)
[![Status](https://img.shields.io/badge/Status-Beta%20Preview-yellow.svg)](release-2.3.3-2.1.0_fr.md)
[![Wicket](https://img.shields.io/badge/Wicket-9.16.0-blue.svg)](https://wicket.apache.org/)
[![Spring](https://img.shields.io/badge/Spring-5.3.37-brightgreen.svg)](https://spring.io/)

> [!NOTE]
> **État actuel : Version préliminaire de test Beta (Pre-release / Beta Preview)**  
> Ce document constitue un journal des modifications dynamique (Living Release Notes). Tout au long de la période de test Beta, chaque ajout de fonctionnalité, ajustement et correctif sera consigné ici de manière continue.

---

## Sommaire
1. [Vue d'ensemble des points clés](#1-vue-densemble-des-points-clés)
2. [🤖 Assistant de requête par e-mail IA (AI Query Copilot avec Ollama)](#2--assistant-de-requête-par-e-mail-ia-ai-query-copilot-avec-ollama)
3. [📦 Mise à niveau des dépendances et suppression des alertes Java 11](#3--mise-à-niveau-des-dépendances-et-suppression-des-alertes-java-11)
4. [🎨 Modernisation de l'interface, Accessibilité et Thèmes](#4--modernisation-de-linterface-accessibilité-et-thèmes)
5. [🛡️ Renforcement de la sécurité en production et garde-fous](#5--renforcement-de-la-sécurité-en-production-et-garde-fous)
6. [⚙️ Paramètres système et améliorations de stabilité](#6--paramètres-système-et-améliorations-de-stabilité)
7. [Guide de mise à niveau et de compatibilité](#7-guide-de-mise-à-niveau-et-de-compatibilité)

---

## 1. Vue d'ensemble des points clés

Dans la continuité de la modernisation architecturale 2.0.0, JTrac 2.3.3-2.1.0-beta introduit le nouvel **Assistant de requête par e-mail IA (AI Query Copilot via Ollama)**, met à niveau le moteur XML pour éradiquer les avertissements de réflexion illégale sous Java 11, optimise l'accessibilité de l'interface (zoom du texte à 4 niveaux avec mode A+++ et sélecteur de 3 thèmes) et consolide la sécurité en production.

---

## 2. 🤖 Assistant de requête par e-mail IA (AI Query Copilot avec Ollama)

1. **Expansion de requête en 2 phases & Protection anti-injection** :
   - Analyse l'objet et le corps des messages via Ollama LLM pour extraire les termes techniques bilingues et synonymes.
   - Isole les entrées utilisateur dans la balise sécurisée `<untrusted_user_query>` pour contrer les attaques de type Prompt Injection.
2. **Recherche pondérée hybride & Bonus de correspondance bilingue** :
   - Évalue la pertinence selon Résumé (+3), Détail (+1), Commentaires (+1) et Pièces jointes (+1), avec un bonus de +5 points pour les correspondances bilingues.
   - Paramètre configurable `llm.retrieval.max_tickets` dans la table `config` (50 par défaut).
3. **Pipeline distribué Map-Reduce** :
   - **Phase Map** : Analyse individuelle de chaque ticket et pièce jointe (jusqu'à 100 000 caractères par fichier ; PDF, Word, Excel, TXT, LOG, CSV) dans des synthèses temporaires.
   - **Phase Reduce** : Consolidation finale en 3 volets clairs (Synthèse de direction, Causes racines & solutions, Actions recommandées).
   - Nettoyage rigoureux garanti via des blocs `finally` sans encombrement disque.
4. **Rapport HTML hors ligne complet en pièce jointe (`JTrac-AI-Report-[yyyyMMdd-HHmm].html`)** :
   - **Corps d'e-mail allégé** : Tableau synthétique et liens directs uniquement, éliminant les problèmes de mise en page tronquée.
   - **Rapport HTML autonome** : Généré en mémoire vive via `ByteArrayResource` (< 3ms, zéro I/O disque).
   - **Design contemporain** : Bordures de tableau précises, cartes dépliantes `<details>`, mode sombre automatique et mise en page d'impression complète.
5. **Guide de Prompts en 8 langues** : Consulter [`docs/llm/PROMPT_EXAMPLES_*.md`](../llm/PROMPT_EXAMPLES_fr.md).

---

## 3. 📦 Mise à niveau des dépendances et suppression des alertes Java 11

1. **Mise à niveau de `dom4j` vers `2.1.4`** :
   - Remplacement de la bibliothèque historique `dom4j:1.6.1` par `org.dom4j:dom4j:2.1.4`.
   - Éradication totale de l'alerte `WARNING: An illegal reflective access operation has occurred` sous Tomcat 9 et Java 11.

---

## 4. 🎨 Modernisation de l'interface, Accessibilité et Thèmes

1. **Cycle d'agrandissement de police à 4 niveaux** :
   - Supporte 100% (Standard), 115% (Confort), 130% (Clair) et **Mode Géant A+++ (145%)** avec protection anti-scintillement (Anti-FOUC).
2. **Sélecteur de thème à 3 états** :
   - Bascule immédiate entre Auto (Système), Clair et Sombre en un seul clic.
3. **Barre de recherche unifiée et navigation intuitive** :
   - Bouton de recherche intégré, accès direct par identifiant RefId (ex. `PROJ-123`) et recherche globale pour les administrateurs.
4. **Optimisation mobile (RWD)** :
   - Menu tiroir latéral (Drawer), panneau Bottom-Sheet pour les historiques et pagination en capsule centrée.

---

## 5. 🛡️ Renforcement de la sécurité en production et garde-fous

1. **Filtre global des en-têtes de sécurité** : Intégration de `X-Frame-Options`, `X-Content-Type-Options`, `Strict-Transport-Security` et `Content-Security-Policy`.
2. **Protection contre les moteurs de recherche (`robots.txt`)** : Blocage de l'indexation web des tickets confidentiels.
3. **Prévention des abus** : Alerte pour les rôles invités, filtrage par liste blanche des paramètres et protection contre le double envoi de formulaires.

---

## 6. ⚙️ Paramètres système et améliorations de stabilité

1. Suppression des logs de debug Wicket via l'ajout de `status.nullValid = ` sur l'ensemble des 8 langues.
2. Refactorisation des bascules booléennes vers `IndicatingDropDownChoice`.
3. Enregistrement explicite des pilotes JDBC pour les sources de données mono-connexion.
4. Détection automatique du jeu de caractères UTF-8 pour les fichiers textes joints et chemins de logo relatifs.

---

## 7. Guide de mise à niveau et de compatibilité

- **Base de données** : Entièrement compatible avec la version 2.3.3-2.0.0 ; **aucun script de migration requis**.
- **Déploiement** : Remplacez simplement le fichier `ROOT.war` du serveur par `target/jtrac.war`.
- **Liens utiles** :
  - [Guide pratique des requêtes par e-mail et Prompts](../llm/PROMPT_EXAMPLES_fr.md)
  - [Notes de version de la version précédente (2.3.3-2.0.0)](release-2.3.3-2.0.0_fr.md)
