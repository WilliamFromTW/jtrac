# JTrac (Fork Mejorado)

[English](README.md) | [繁體中文](README_zh-TW.md) | [简体中文](README_zh-CN.md) | [日本語](README_ja.md) | [Tiếng Việt](README_vi.md) | [Deutsch](README_de.md) | [Español](README_es.md)

---

[![Java](https://img.shields.io/badge/Java-8%20%7C%2011-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](license.txt)
[![Fork From](https://img.shields.io/badge/Fork%20From-JTrac%202.3.3-blue)](https://jtrac.info)
[![OpenSpec](https://img.shields.io/badge/OpenSpec-v1.12.0-brightgreen.svg)](openspec/specs/README.md)

Este proyecto es un fork modernizado y mejorado de [JTrac 2.3.3 (https://jtrac.info)](https://jtrac.info). Su objetivo es proporcionar un sistema de seguimiento de incidencias ligero y altamente compatible, con capacidades de archivado estático fuera de línea y una interfaz de usuario renovada. El desarrollo está guiado por la metodología de especificaciones OpenSpec v1.12.0 y cuenta con la asistencia de Antigravity 1.1.27 en la refactorización arquitectónica, verificación y control de calidad del código.

---

## Guías de Compilación Multilingüe / Multilingual Build Guides

| Idioma / Language | Guía de compilación / Build Guide |
|---|---|
| **English** | [English Build & Compilation Guide](docs/build/BUILD_en.md) |
| **繁體中文 (Traditional Chinese)** | [繁體中文編譯與建置完整指南](docs/build/BUILD_zh-TW.md) |
| **简体中文 (Simplified Chinese)** | [简体中文编译与构建完整指南](docs/build/BUILD_zh-CN.md) |
| **日本語 (Japanese)** | [日本語ビルド・コンパイル詳細ガイド](docs/build/BUILD_ja.md) |
| **Tiếng Việt (Vietnamese)** | [Hướng dẫn biên dịch và đóng gói Tiếng Việt](docs/build/BUILD_vi.md) |

---

## Actualizaciones Principales en este Fork (Changelog & Major Updates)

### 1. Limpieza de Módulos: Eliminación Completa del Módulo Wiki (Removed Wiki Module)
- Eliminación total del código fuente, componentes de página e iconos de navegación del módulo Wiki heredado, simplificando la arquitectura para enfocarse en el núcleo de seguimiento de incidencias.

### 2. Nuevas Funcionalidades (New Features)
- **Exportación masiva a HTML y descarga en ZIP desde la Web**:
  - Se añadió la acción "Exportar HTML" en la barra de navegación superior. Los usuarios pueden seleccionar múltiples espacios de proyecto (con atajos "Seleccionar todo / Deseleccionar todo") y descargar un archivo comprimido ZIP con todo el historial de discusión en HTML y sus archivos adjuntos.
- **Control estricto de permisos de espacio (Permission Guardrails)**:
  - Doble verificación de seguridad en servidor y cliente: los usuarios estándar solo pueden visualizar, seleccionar y descargar espacios en los que tienen membresía autorizada (los administradores conservan acceso global). Se incluye bloqueo ante listas vacías y avisos amigables.
- **Herramienta independiente de línea de comandos para exportar a HTML (`tools/jtrac-exporter.jar`)**:
  - Permite conectarse directamente mediante JDBC para generar informes HTML responsivos y multilingües sin necesidad de iniciar el servidor web.
  - Modo oscuro (Dark Mode) 100% autónomo y fuera de línea implementado en CSS puro, sin dependencias externas de CDN o internet.
- **Localización multilingüe completa (Full i18n Coverage)**:
  - Revisión y completado de las traducciones en chino tradicional (`zh_TW`) y chino simplificado (`zh_CN`).
  - Incorporación de etiquetas faltantes en todos los paquetes de idiomas (por ejemplo, el estado de activación de espacio `space_form.isActive`).

### 3. Corrección de Errores (Bug Fixes)
- **Centrado de botones y etiquetas de navegación (Navigation Header Centering)**:
  - Corrección de problemas de alineación donde los elementos de cabecera ("Panel", "Búsqueda", "Exportar HTML", "Opciones", "Cerrar sesión", usuario) aparecían desplazados a la derecha y desalineados verticalmente.
  - Adopción de Flexbox inline-flex para centrado horizontal y vertical perfecto, bordes redondeados de 3px, animaciones hover para botones interactivos y estilos específicos para distintivos informativos.
- **Depuración del historial de discusiones (Thread History Cleanup)**:
  - Se eliminó el registro de estado inicial "Open" redundante que no contenía notas ni cambios de campo al exportar a HTML, conservando solo el historial con valor real.
- **Resolución de `LazyInitializationException` de Hibernate**:
  - Solución a excepciones por cierre de sesión al acceder a los metadatos de configuración de espacios mediante carga anticipada activa (Eager Fetch / Initialize).
- **Corrección del filtrado de recursos Maven UTF-8 y prevención de archivos corruptos**:
  - Adopción de codificación nativa UTF-8 eliminando conversiones `native2ascii`.
  - Corrección en el filtrado de recursos de Maven que corrompía imágenes binarias (gif, png, jar) al empaquetar.

---

## Tecnologías y Arquitectura (Technologies & Architecture)

- **Lenguaje principal**: Java 1.8 / 11
- **Framework Web**: Apache Wicket 1.3
- **Contenedor e IoC**: Spring Framework 2.5
- **ORM y Base de Datos**: Hibernate 3 / HSQLDB integrado; compatible con MySQL, PostgreSQL, MS SQL Server, Oracle
- **Herramienta de construcción**: Apache Maven 3.9+ (paquete WAR)
- **Herramientas de apoyo y especificación**: OpenSpec v1.12.0, Antigravity 1.1.27
- **Codificación de caracteres**: 100% UTF-8

---

## Inicio Rápido: Compilación y Despliegue (Quick Start)

### 1. Compilar aplicación principal (WAR)
```bash
# Compilar código fuente
mvn compile

# Empaquetar archivo WAR (omitiendo pruebas)
mvn package -DskipTests
```
El archivo generado se ubica en: `target/jtrac.war`, listo para desplegarse en Jetty o Tomcat.

### 2. Compilar y ejecutar la herramienta de exportación HTML (CLI)
```bash
# Empaquetar JAR ejecutable independiente
mvn clean package -f tools/jtrac-exporter/pom.xml -DskipTests
# Salida: tools/jtrac-exporter.jar

# Ejemplo de exportación desde HSQLDB local
java -jar tools/jtrac-exporter.jar \
  --db-url="jdbc:hsqldb:file:./data/db/jtrac;shutdown=true;readonly=true" \
  --attachments-dir="./data/attachments" \
  --out="./export-output" \
  --lang=es
```

---

## Especificaciones y Documentación (Specifications)

Este proyecto se desarrolla bajo la metodología de especificaciones OpenSpec:
- [Directorio principal de especificaciones](openspec/specs/README.md)
- [Especificación del exportador HTML](openspec/specs/html-exporter/spec.md)
- [Especificación de idiomas y recursos](openspec/specs/i18n-resources/spec.md)
- [Especificación de guías de compilación](openspec/specs/build-documentation/spec.md)
- [Reglas de desarrollo](.agents/AGENTS.md)

---

## Licencia (License)

JTrac es software de código abierto publicado bajo la [Licencia de Software Apache, Versión 2.0](license.txt).
