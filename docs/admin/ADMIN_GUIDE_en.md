# JTrac Administrator & System Configuration Guide (English)

[English](ADMIN_GUIDE_en.md) | [繁體中文](ADMIN_GUIDE_zh-TW.md) | [简体中文](ADMIN_GUIDE_zh-CN.md) | [日本語](ADMIN_GUIDE_ja.md) | [Tiếng Việt](ADMIN_GUIDE_vi.md) | [Deutsch](ADMIN_GUIDE_de.md) | [Español](ADMIN_GUIDE_es.md) | [Français](ADMIN_GUIDE_fr.md)

---

## Table of Contents
1. [Initial Login & Default Credentials](#1-initial-login--default-credentials)
2. [Mandatory System Initialization Settings](#2-mandatory-system-initialization-settings)
3. [Architecture & Mail Flowchart (Mermaid)](#3-architecture--mail-flowchart-mermaid)
4. [Administrative Functions Overview](#4-administrative-functions-overview)
5. [Full System Backup & Restore (Anti-Lockout Shield)](#5-full-system-backup--restore-anti-lockout-shield)
6. [Security, Database Upgrade & Maintenance Recommendations](#6-security-database-upgrade--maintenance-recommendations)

---

## 1. Initial Login & Default Credentials

Upon first startup and schema initialization, JTrac creates a default global administrator account:

- **System URL**: `http://<server-ip>:<port>/` (e.g. `http://localhost:8888/`)
- **Default Username**: `admin`
- **Default Password**: `admin`

> [!WARNING]
> Immediately upon your initial login, navigate to **OPTIONS** ➜ **Edit User Profile** to change the default `admin` password. Never expose default credentials to production environments.

---

## 2. Mandatory System Initialization Settings

Navigate to **OPTIONS** ➜ **Manage Settings** to configure these vital parameters:

### 1. `jtrac.url.base` (Base System URL - CRITICAL)
- **Default**: `http://localhost/jtrac/`
- **Recommended**: The public or intranet URL accessible to end users, **ending with a trailing slash `/`** (e.g., `http://192.168.1.100:8888/` or `https://issues.yourcompany.com/`).
- **Why this is critical**: All hyperlinks in notification emails (account credentials, password resets, issue updates) are built using this prefix. Leaving it as `localhost` prevents recipients from opening links from their remote machines.

---

### 2. `locale.default` (Default System Language)
- **Default**: `en`
- Options: `zh_TW`, `zh_CN`, `ja`, `en`, `vi`, `de`, `es`, `fr`.

---

### 3. SMTP Mail Server Settings
- `mail.server.host`: SMTP server host or IP.
- `mail.server.port`: SMTP port (25, 587 for TLS, 465 for SSL).
- `mail.server.username` & `mail.server.password`: Authentication credentials.
- `mail.server.starttls.enable`: Set to `true` for TLS.
- `mail.from`: Sender email address.

---

### 4. Advanced Settings & Pagination
- `users.list.pageSize`: Default page size for User Management List (default: `25`; selectable: 10, 25, 50, 100, All).
- `spaces.list.pageSize`: Default page size for Space Management List (default: `25`; selectable: 10, 25, 50, 100, All).
- `attachment.maxsize`: Maximum upload size in MB (default `10`).
- `session.timeout`: Session timeout in seconds (default `1800` / 30 mins).

---

## 3. Architecture & Mail Flowchart (Mermaid)

### Administrator Setup Flow
```mermaid
flowchart TD
    Start([Start JTrac Service]) --> Login[Initial Login<br/>admin / admin]
    Login --> ChangePwd[Change Admin Password<br/>OPTIONS ➜ Edit User Profile]
    ChangePwd --> ConfigSettings[Configure System Settings<br/>OPTIONS ➜ Manage Settings]
    
    subgraph CriticalSettings [Core Configuration]
        ConfigSettings --> SetUrlBase["Set jtrac.url.base<br/>(e.g., http://192.168.1.100:8888/)<br/>★ Prevents email links pointing to localhost"]
        ConfigSettings --> SetLocale["Set locale.default<br/>(e.g., en)<br/>★ Sets default interface language"]
        ConfigSettings --> SetSMTP["Configure SMTP Server<br/>(host / port / from)<br/>★ Enables issue & reset notifications"]
        ConfigSettings --> SetPaging["Configure Page Sizes<br/>(users/spaces.list.pageSize)"]
    end
    
    CriticalSettings --> CreateSpaces[Create Tracking Spaces<br/>OPTIONS ➜ Manage Spaces]
    CreateSpaces --> CreateUsers[Create Users & Assign Roles<br/>OPTIONS ➜ Manage Users]
    CreateUsers --> Finish([System Ready for Production])
```

---

## 4. Administrative Functions Overview

| Menu Item | Purpose |
|---|---|
| **Edit User Profile** | Update current administrator email, display name, and password. |
| **Manage Users** | User management: create users, paginated browsing, reset passwords, lock accounts, assign global Admin role. |
| **Manage Spaces** | Space management: create spaces, paginated browsing, custom fields, statuses, severities, member roles. |
| **Configure Links** | Configure navigation bar external links. |
| **Manage Settings** | Configure global system parameters (base URL, SMTP, pagination). |
| **Rebuild Indexes** | Rebuild Lucene full-text search indexes. |
| **Import From Excel** | Batch import issues via Excel spreadsheet templates. |
| **Export HTML** | Web-based batch HTML export and ZIP download. |
| **Backup & Restore** | Full system backup and restore: SuperUser exclusive feature supporting one-click ZIP bundle download of database and attachments, with safe restore, automatic safety snapshot, and anti-lockout credential protection. |

---

## 5. Full System Backup & Restore (Anti-Lockout Shield)

JTrac provides built-in, native full-system disaster recovery and migration capabilities, accessible exclusively to SuperUsers:

1. **One-Click Full System Backup Bundle Export**:
   - Navigate to **OPTIONS** ➜ **Backup & Restore**.
   - Click **Download Backup (.zip)**. The system serializes all database entities (Configs, Users, Spaces, Items, Histories, Attachments, etc.) into cross-database standard JSON format, and compresses them along with the physical `${jtrac.home}/attachments/` directory into a single timestamped `.zip` bundle for instant browser download.
2. **Safe System Restore Engine**:
   - Choose a valid JTrac backup `.zip` file, check the confirmation checkbox, and click **Execute Restore**.
   - **Automatic Server-Side Safety Snapshot**: Prior to wiping any existing data, the system automatically creates an emergency snapshot backup in `${jtrac.home}/backups/` on the server, guaranteeing that current data can be recovered in case of an unexpected anomaly.
   - **Anti-Lockout Credential Shield**: The restore engine identifies the administrator currently performing the restore operation. Even if the backup bundle contains outdated or forgotten administrative passwords, the system **strictly preserves the current operator's active password hash and global `ROLE_ADMIN` status** (or injects the operator if absent from the backup), completely eliminating administrative lockout risks.
   - **Asynchronous Background Search Index Rebuild**: Once restore finishes, Lucene full-text indexes are automatically rebuilt in the background. The administrator's active session remains valid with zero interruption.

---

## 6. Security, Database Upgrade & Maintenance Recommendations

1. **Password Security (BCrypt & Hybrid Migration)**:
   - Upgraded to Spring Security 5.8 with BCrypt password hashing.
   - Transparent hybrid migration: legacy MD5 hashes are automatically upgraded to BCrypt upon successful user login without database disruption.
2. **Database Upgrade (Upgrading from 2.3.3-1.0.0)**:
   - External DBs (MySQL, PostgreSQL, SQL Server, Oracle): Execute [`etc/sql/upgrade-to-2.0.0.sql`](../../etc/sql/upgrade-to-2.0.0.sql).
   - Embedded HSQLDB: Automatic backup and migration to HSQLDB 2.x is handled on server startup.
3. **Backup Schedule**:
   - Regularly use **OPTIONS** ➜ **Backup & Restore** to download complete backup bundles containing both structured database records and physical attachment files.
   - Database files: Backup `data/db/` or external database on a regular schedule.
   - Attachments: Regularly backup `data/attachments/`.
4. **Reverse Proxy & HTTPS**:
   - When placing behind Nginx/Apache with HTTPS, set `jtrac.url.base` to `https://...` and preserve `Host` and `X-Forwarded-Proto` headers.

