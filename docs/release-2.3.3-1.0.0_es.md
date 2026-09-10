# Notas de la Versión de JTrac (Release Notes) - 2.3.3-1.0.0

[English](release-2.3.3-1.0.0_en.md) | [繁體中文](release-2.3.3-1.0.0_zh-TW.md) | [简体中文](release-2.3.3-1.0.0_zh-CN.md) | [日本語](release-2.3.3-1.0.0_ja.md) | [Tiếng Việt](release-2.3.3-1.0.0_vi.md) | [Deutsch](release-2.3.3-1.0.0_de.md) | [Español](release-2.3.3-1.0.0_es.md) | [Français](release-2.3.3-1.0.0_fr.md)

---

[![Java](https://img.shields.io/badge/Java-8%20%7C%2011-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.8+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](../license.txt)
[![Fork From](https://img.shields.io/badge/Fork%20From-JTrac%202.3.3-blue)](https://jtrac.info)
[![Version](https://img.shields.io/badge/Version-2.3.3--1.0.0-blue.svg)](../pom.xml)

Esta versión marca la primera publicación fork modernizada y mejorada a partir de [JTrac 2.3.3 (https://jtrac.info)](https://jtrac.info). Se centra en la depuración de código, refuerzo de la seguridad de accesos, soporte multilingüe completo en UTF-8 y capacidad de archivado estático en HTML fuera de línea.

---

## 📦 Características Principales de la Versión 2.3.3-1.0.0 (Major Features)

1. **Limpieza de Módulos (Module Cleanup)**:
   - Eliminación total del módulo Wiki obsoleto y sin mantenimiento, simplificando el código y reduciendo dependencias innecesarias.
2. **Exportación HTML por Lotes y Descarga ZIP Web (Batch HTML Export & ZIP Download)**:
   - Implementación en la consola de administración web de archivado fuera de línea multiespacio, exportando el historial de incidencias en HTML estático responsive autónomo y descarga en un solo archivo ZIP.
3. **Estricto Control de Permisos por Espacio (Strict Space Permission Guardrails)**:
   - Refuerzo en la comprobación de permisos para asegurar que los usuarios no administradores solo puedan ver, buscar y exportar los espacios a los que tienen acceso autorizado.
4. **Herramienta CLI Independiente (`tools/jtrac-exporter.jar`)**:
   - Herramienta de línea de comandos sin necesidad de servidor web, que se conecta directamente mediante JDBC para generar informes HTML estáticos con soporte de modo oscuro.
5. **Estándar Multilingüe Completo (8 Idiomas)**:
   - Estandarización de todo el proyecto en codificación UTF-8, completando y calibrando los recursos de interfaz para 8 idiomas (inglés, chino tradicional, chino simplificado, japonés, vietnamita, alemán, español y francés).

---

## 📜 Historial de Versiones (Release History)

- **Versión Siguiente**: [Notas de la Versión de JTrac - 2.3.3-2.0.0](release-2.3.3-2.0.0_es.md)

---

## Licencia

JTrac es software de código abierto bajo la [Apache Software License, Version 2.0](../license.txt).
