# JTrac (Fork Modernizado y Mejorado)

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

Este proyecto es un fork modernizado y mejorado de [JTrac 2.3.3 (https://jtrac.info)](https://jtrac.info). Su objetivo es proporcionar un sistema de seguimiento de incidencias ligero y altamente compatible, con capacidades de archivado estático fuera de línea, una interfaz moderna y altos estándares de seguridad empresarial. El desarrollo está guiado por OpenSpec v1.12.0 y cuenta con la asistencia de Antigravity.

---

## Guías de Compilación Multilingües / Multilingual Build Guides

| Idioma / Language | Guía de Compilación / Build Guide |
|---|---|
| **Español (Spanish)** | [Guía de compilación y construcción en español](docs/build/BUILD_es.md) |
| **English** | [English Build & Compilation Guide](docs/build/BUILD_en.md) |
| **繁體中文 (Traditional Chinese)** | [繁體中文編譯與建置完整指南](docs/build/BUILD_zh-TW.md) |
| **简体中文 (Simplified Chinese)** | [简体中文编译与构建完整指南](docs/build/BUILD_zh-CN.md) |
| **日本語 (Japanese)** | [日本語ビルド・コンパイル詳細ガイド](docs/build/BUILD_ja.md) |
| **Tiếng Việt (Vietnamese)** | [Hướng dẫn biên dịch và đóng gói Tiếng Việt](docs/build/BUILD_vi.md) |
| **Deutsch (German)** | [Deutscher Kompilierungs- und Build-Leitfaden](docs/build/BUILD_de.md) |
| **Français (French)** | [Guide complet de compilation et de construction en français](docs/build/BUILD_fr.md) |

---

## Guías del Administrador del Sistema / Multilingual Administrator Guides

| Idioma / Language | Guía del Administrador / Admin Guide |
|---|---|
| **Español (Spanish)** | [Guía del Administrador del Sistema y Configuración en Español](docs/admin/ADMIN_GUIDE_es.md) |
| **English** | [English Administrator & System Configuration Guide](docs/admin/ADMIN_GUIDE_en.md) |
| **繁體中文 (Traditional Chinese)** | [繁體中文系統管理者完整指南](docs/admin/ADMIN_GUIDE_zh-TW.md) |
| **简体中文 (Simplified Chinese)** | [简体中文系统管理员完整指南](docs/admin/ADMIN_GUIDE_zh-CN.md) |
| **日本語 (Japanese)** | [日本語システム管理者総合ガイド](docs/admin/ADMIN_GUIDE_ja.md) |
| **Tiếng Việt (Vietnamese)** | [Hướng dẫn Quản trị viên và Cấu hình Hệ thống](docs/admin/ADMIN_GUIDE_vi.md) |
| **Deutsch (German)** | [Deutscher Systemadministrator- und Konfigurationsleitfaden](docs/admin/ADMIN_GUIDE_de.md) |
| **Français (French)** | [Guide d'administration et de configuration du système en français](docs/admin/ADMIN_GUIDE_fr.md) |

---

## Novedades Principales (Changelog & Major Updates)

### 🚀 Versión 2.3.3-2.0.0 (Gran Modernización Arquitectónica)

1. **Modernización del Núcleo Backend (Spring 5.3 + Hibernate 5.6 + JUnit 5)**:
   - Actualización a Spring Framework 5.3.37; eliminación de `HibernateTemplate` y `TimerFactoryBean`.
   - Migración a Hibernate ORM 5.6.15.Final con gestión nativa de `SessionFactory` y consultas JPA estándar.
   - Búsqueda de texto completo con API Lucene nativa, desacoplada de `spring-modules-lucene`.
   - Pruebas unitarias modernizadas a JUnit 5 (Jupiter).
2. **Reestructuración de Seguridad (Spring Security 5.8 + Migración Transparente a BCrypt)**:
   - Reemplazo completo de Acegi Security 1.0.7 por Spring Security 5.8.14.
   - `JtracHybridPasswordEncoder`: Valida contraseñas MD5 antiguas y las convierte automáticamente a BCrypt tras el inicio de sesión exitoso del usuario sin tiempos de inactividad.
3. **Capa Web Moderna (Apache Wicket 9.16.0)**:
   - Migración de Wicket 1.3.7 a Wicket 9.16.0 con soporte tipado completo (`IModel<T>`).
   - Compatible con contenedores Servlet 4.0 (Jetty 10.0.26, Jetty 12, Tomcat 9, Tomcat 10+).
4. **Paginación en Listas de Usuarios y Proyectos (Pagination & Config)**:
   - Soporte para paginación configurable en `UserListPage` y `SpaceListPage` (10, 25, 50, 100, Todos).
   - Incorporación de parámetros `users.list.pageSize` y `spaces.list.pageSize` en la tabla `config`.
5. **Corrección de Eventos Ajax en Asignación de Roles**:
   - Ajuste de eventos a estándar DOM `"change"` y protección contra deselección.
6. **Filtro Global de Recursos Estáticos (StaticResourceFilter)**:
   - Resuelve el problema de imágenes 404 en `../resources/*` en rutas URL anidadas y restaura iconos faltantes.
7. **Corrección de Enlace de Modelo en Carga de Archivos**:
   - Asignación explícita de `ListModel` independiente en `FileUploadField` en `ItemFormPage` y `ItemViewFormPanel`.
8. **Actualización de Base de Datos y Guía SQL**:
   - Script oficial de actualización [`etc/sql/upgrade-to-2.0.0.sql`](etc/sql/upgrade-to-2.0.0.sql) para MySQL, PostgreSQL, SQL Server y Oracle.
   - Herramienta integrada `HsqldbDatabaseMigrator` para migración automática de HSQLDB 1.8 a 2.x con respaldo preventivo.
9. **Eliminación del Módulo Excel y Reducción del Paquete WAR (Excel Module Removal & POI Deprecation)**:
   - Eliminación total de la importación y exportación de Excel y de la biblioteca Apache POI, reduciendo el tamaño del archivo WAR en más de 3 MB.
10. **Paquete de Copia de Seguridad Mejorado (`jtrac-dump.sql`)**:
    - El archivo ZIP de copia de seguridad completa incluye ahora un volcado SQL ANSI integral (`jtrac-dump.sql`), con DDL ANSI, notas de dialectos MySQL/PostgreSQL/HSQLDB, instrucciones INSERT ordenadas por dependencias de claves foráneas y comandos de restablecimiento de secuencias para recuperación ante desastres y migración de BD por DBA.

---

## Tecnologías y Arquitectura

- **Lenguaje**: Java 11 / 17
- **Framework Web**: Apache Wicket 9.16.0
- **Contenedor IoC**: Spring Framework 5.3.37
- **Seguridad**: Spring Security 5.8.14 (BCrypt)
- **ORM y Persistencia**: Hibernate ORM 5.6.15.Final
- **Bases de Datos Soportadas**: HSQLDB 2.x (embebida), MySQL / MariaDB, PostgreSQL, Microsoft SQL Server, Oracle
- **Contenedores Web Soportados**:
  - **Jetty 10.x** (Nativo, verificado en Jetty 10.0.26)
  - **Jetty 12.x** (Nativo habilitando módulo `ee8`)
  - **Tomcat 9.x** (Nativo)
  - **Tomcat 10.x / 11.x** (Mediante `webapps-javaee/` o herramienta `jakartaee-migration`)
- **Herramienta de Construcción**: Apache Maven 3.9+
- **Especificaciones**: OpenSpec v1.12.0, Antigravity

---

## Inicio Rápido (Quick Start)

### 1. Construir la Aplicación (WAR)
```bash
# Compilar código fuente (requiere JDK 11 o JDK 17)
mvn clean compile

# Ejecutar pruebas y empaquetar WAR
mvn package

# Empaquetado rápido (sin pruebas)
mvn package -DskipTests
```
Archivo generado: `target/jtrac.war`.

### 2. Actualización de Base de Datos (Desde 2.3.3-1.0.0)
- Bases de datos externas (MySQL, PostgreSQL, etc.): Ejecute [`etc/sql/upgrade-to-2.0.0.sql`](etc/sql/upgrade-to-2.0.0.sql).
- HSQLDB embebida: El proceso se realiza automáticamente con copia de seguridad al iniciar el servidor.

### 3. Construir Herramienta de Exportación HTML (CLI)
```bash
mvn clean package -f tools/jtrac-exporter/pom.xml -DskipTests
# Salida: tools/jtrac-exporter.jar
```

---

## Licencia

JTrac es software de código abierto bajo la [Apache Software License, Version 2.0](license.txt).
