# Notes de Version JTrac (Release Notes) - 2.3.3-1.0.0

[English](release-2.3.3-1.0.0_en.md) | [繁體中文](release-2.3.3-1.0.0_zh-TW.md) | [简体中文](release-2.3.3-1.0.0_zh-CN.md) | [日本語](release-2.3.3-1.0.0_ja.md) | [Tiếng Việt](release-2.3.3-1.0.0_vi.md) | [Deutsch](release-2.3.3-1.0.0_de.md) | [Español](release-2.3.3-1.0.0_es.md) | [Français](release-2.3.3-1.0.0_fr.md)

---

[![Java](https://img.shields.io/badge/Java-8%20%7C%2011-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.8+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](../../license.txt)
[![Fork From](https://img.shields.io/badge/Fork%20From-JTrac%202.3.3-blue)](https://jtrac.info)
[![Version](https://img.shields.io/badge/Version-2.3.3--1.0.0-blue.svg)](../../pom.xml)

Cette version constitue la première publication fork modernisée et enrichie dérivée de [JTrac 2.3.3 (https://jtrac.info)](https://jtrac.info). Elle met l'accent sur le nettoyage du code, le renforcement des droits d'accès, le support multilingue complet en UTF-8 et la capacité d'archivage hors ligne au format HTML statique.

---

## 📦 Principales Fonctionnalités de la Version 2.3.3-1.0.0 (Major Features)

1. **Nettoyage des Modules (Module Cleanup)** :
   - Suppression totale du module Wiki obsolète et non maintenu, allégeant la base de code et éliminant les dépendances superflues.
2. **Export HTML par Lot et Téléchargement ZIP Web (Batch HTML Export & ZIP Download)** :
   - Implémentation d'une fonction d'archivage hors ligne multi-espaces dans l'interface d'administration web, exportant l'historique complet des tickets en HTML statique responsive autonome et téléchargeable en fichier ZIP.
3. **Contrôle Strict des Permissions d'Espace (Strict Space Permission Guardrails)** :
   - Renforcement du contrôle des accès pour garantir que les utilisateurs standard ne peuvent consulter, rechercher et exporter que les espaces projets pour lesquels ils disposent d'une autorisation explicite.
4. **Outil d'Exportation CLI Autonome (`tools/jtrac-exporter.jar`)** :
   - Fourniture d'un outil en ligne de commande autonome sans serveur web requis, se connectant directement via JDBC pour générer des rapports HTML statiques avec support du thème sombre.
5. **Norme Multilingue Complète sur 8 Langues (Full Multilingual Standard)** :
   - Standardisation de l'ensemble du projet en encodage UTF-8, avec révision et complétion des ressources d'interface pour 8 langues (anglais, chinois traditionnel, chinois simplifié, japonais, vietnamien, allemand, espagnol et français).

---

## 📜 Historique des Versions (Release History)

- **Version Suivante** : [Notes de Version JTrac - 2.3.3-2.0.0](release-2.3.3-2.0.0_fr.md)

---

## Licence

JTrac est un logiciel libre distribué sous la [Licence Apache, Version 2.0](../../license.txt).
