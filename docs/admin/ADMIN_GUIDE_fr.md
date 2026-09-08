# Guide d'Administration et de Configuration Système (Français)

[English](ADMIN_GUIDE_en.md) | [繁體中文](ADMIN_GUIDE_zh-TW.md) | [简体中文](ADMIN_GUIDE_zh-CN.md) | [日本語](ADMIN_GUIDE_ja.md) | [Tiếng Việt](ADMIN_GUIDE_vi.md) | [Deutsch](ADMIN_GUIDE_de.md) | [Español](ADMIN_GUIDE_es.md) | [Français](ADMIN_GUIDE_fr.md)

---

## Sommaire
1. [Première Connexion & Identifiants par Défaut](#1-première-connexion--identifiants-par-défaut)
2. [Paramètres d'Initialisation Obligatoires](#2-paramètres-dinitialisation-obligatoires)
3. [Architecture et Flux E-mail (Mermaid)](#3-architecture-et-flux-e-mail-mermaid)
4. [Fonctions d'Administration Principales](#4-fonctions-dadministration-principales)
5. [Sécurité, Mise à Niveau de la Base de Données et Maintenance](#5-sécurité-mise-à-niveau-de-la-base-de-données-et-maintenance)

---

## 1. Première Connexion & Identifiants par Défaut

- **URL du système** : `http://<IP-Serveur>:<Port>/` (ex: `http://localhost:8888/`)
- **Nom d'utilisateur par défaut** : `admin`
- **Mot de passe par défaut** : `admin`

> [!WARNING]
> Changez immédiatement le mot de passe après votre première connexion via **OPTIONS** ➜ **Edit User Profile**.

---

## 2. Paramètres d'Initialisation Obligatoires

Accédez à **OPTIONS** ➜ **Manage Settings** :

### 1. `jtrac.url.base` (URL de base du système - CRITIQUE)
- **Valeur par défaut** : `http://localhost/jtrac/`
- **Recommandation** : URL accessible aux utilisateurs finaux, **terminée par un slash `/`** (ex: `http://192.168.1.100:8888/` ou `https://issues.yourcompany.com/`).
- **Importance** : Tous les liens des e-mails s'appuient sur ce préfixe. S'il reste sur `localhost`, les destinataires distants ne pourront pas ouvrir les liens.

---

### 2. `locale.default` (Langue par défaut)
- Recommandé : `fr` ou `en`.

---

### 3. Paramètres du Serveur SMTP
- `mail.server.host`, `mail.server.port`, `mail.server.username`, `mail.server.password`, `mail.server.starttls.enable`, `mail.from`.

---

### 4. Paramètres Avancés & Pagination
- `users.list.pageSize` : Nombre d'éléments par page pour la liste des utilisateurs (défaut : `25` ; choix : 10, 25, 50, 100, Tout).
- `spaces.list.pageSize` : Nombre d'éléments par page pour la liste des espaces (défaut : `25` ; choix : 10, 25, 50, 100, Tout).
- `attachment.maxsize` : Taille maximale des pièces jointes en Mo (défaut `10`).

---

## 3. Architecture et Flux E-mail (Mermaid)

```mermaid
flowchart TD
    Start([Démarrer JTrac]) --> Login[Première Connexion<br/>admin / admin]
    Login --> ChangePwd[Changer le mot de passe<br/>OPTIONS ➜ Edit User Profile]
    ChangePwd --> ConfigSettings[Configurer les paramètres<br/>OPTIONS ➜ Manage Settings]
    
    subgraph CriticalSettings [Configuration Essentielle]
        ConfigSettings --> SetUrlBase["Définir jtrac.url.base<br/>(ex: http://192.168.1.100:8888/)"]
        ConfigSettings --> SetLocale["Définir locale.default<br/>(ex: fr)"]
        ConfigSettings --> SetSMTP["Configurer le serveur SMTP<br/>(host / port / from)"]
        ConfigSettings --> SetPaging["Configurer la pagination<br/>(users/spaces.list.pageSize)"]
    end
    
    CriticalSettings --> CreateSpaces[Créer les espaces<br/>OPTIONS ➜ Manage Spaces]
    CreateSpaces --> CreateUsers[Créer utilisateurs & attribuer rôles<br/>OPTIONS ➜ Manage Users]
    CreateUsers --> Finish([Prêt pour la production])
```

---

## 4. Fonctions d'Administration Principales

| Fonction | Description |
|---|---|
| **Edit User Profile** | Modifier e-mail, nom d'affichage et mot de passe de l'administrateur. |
| **Manage Users** | Gestion des utilisateurs, pagination, réinitialisation mot de passe, rôle Admin. |
| **Manage Spaces** | Gestion des espaces, pagination, champs personnalisés, rôles des membres. |
| **Configure Links** | Configurer des liens externes dans la barre de navigation. |
| **Manage Settings** | Paramètres globaux (URL de base, SMTP, pagination). |
| **Rebuild Indexes** | Reconstruire l'index de recherche plein texte Lucene. |
| **Import From Excel** | Importation en masse de tickets via Excel. |
| **Export HTML** | Exportation HTML et téléchargement ZIP directement depuis le Web. |

---

## 5. Sécurité, Mise à Niveau de la Base de Données et Maintenance

1. **Sécurité des Mots de Passe (Migration BCrypt)** :
   - Migration vers Spring Security 5.8 avec hachage BCrypt. Les anciens hachages MD5 sont automatiquement convertis en BCrypt lors de la connexion réussie.
2. **Mise à Niveau de la Base de Données (Depuis 2.3.3-1.0.0)** :
   - Bases externes : Exécutez [`etc/sql/upgrade-to-2.0.0.sql`](../../etc/sql/upgrade-to-2.0.0.sql).
   - HSQLDB intégrée : Sauvegarde et mise à niveau automatiques vers 2.x au démarrage du serveur.
3. **Sauvegardes Régulières** :
   - Sauvegardez régulièrement les dossiers `data/db/` et `data/attachments/`.
