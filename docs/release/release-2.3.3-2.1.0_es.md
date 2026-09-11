# Notas de la Versión de JTrac (Release Notes) - 2.3.3-2.1.0-beta

[English](release-2.3.3-2.1.0_en.md) | [繁體中文](release-2.3.3-2.1.0_zh-TW.md) | [简体中文](release-2.3.3-2.1.0_zh-CN.md) | [日本語](release-2.3.3-2.1.0_ja.md) | [Tiếng Việt](release-2.3.3-2.1.0_vi.md) | [Deutsch](release-2.3.3-2.1.0_de.md) | [Español](release-2.3.3-2.1.0_es.md) | [Français](release-2.3.3-2.1.0_fr.md)

---

[![Java](https://img.shields.io/badge/Java-11%20%7C%2017-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](../../license.txt)
[![Version](https://img.shields.io/badge/Version-2.3.3--2.1.0--beta-orange.svg)](../../pom.xml)
[![Status](https://img.shields.io/badge/Status-Beta%20Preview-yellow.svg)](release-2.3.3-2.1.0_es.md)
[![Wicket](https://img.shields.io/badge/Wicket-9.16.0-blue.svg)](https://wicket.apache.org/)
[![Spring](https://img.shields.io/badge/Spring-5.3.37-brightgreen.svg)](https://spring.io/)

> [!NOTE]
> **Estado actual: Versión previa de pruebas Beta (Pre-release / Beta Preview)**  
> Este documento es un registro de cambios dinámico (Living Release Notes). Durante la fase de pruebas Beta, cualquier mejora, ajuste de parámetros y corrección de errores posterior se agregará automáticamente aquí.

---

## Índice
1. [Resumen de características principales](#1-resumen-de-características-principales)
2. [🤖 Asistente de consultas por correo con IA (AI Query Copilot con Ollama)](#2--asistente-de-consultas-por-correo-con-ia-ai-query-copilot-con-ollama)
3. [📦 Actualización de dependencias y eliminación de advertencias en Java 11](#3--actualización-de-dependencias-y-eliminación-de-advertencias-en-java-11)
4. [🎨 Modernización de interfaz, Accesibilidad de fuentes y Temas](#4--modernización-de-interfaz-accesibilidad-de-fuentes-y-temas)
5. [🛡️ Fortalecimiento de seguridad en producción y protecciones](#5--fortalecimiento-de-seguridad-en-producción-y-protecciones)
6. [⚙️ Configuración del sistema y mejoras de estabilidad](#6--configuración-del-sistema-y-mejoras-de-estabilidad)
7. [Guía de actualización y compatibilidad](#7-guía-de-actualización-y-compatibilidad)

---

## 1. Resumen de características principales

Sobre la base de la modernización estructural de la versión 2.0.0, JTrac 2.3.3-2.1.0-beta incorpora el innovador **Asistente de consultas por correo con IA (AI Query Copilot con Ollama)**, actualiza el motor XML para eliminar por completo las advertencias de acceso reflectivo en Java 11, refuerza la accesibilidad de la interfaz (escala de 4 niveles con modo A+++ y selector de 3 temas) y robustece la seguridad.

---

## 2. 🤖 Asistente de consultas por correo con IA (AI Query Copilot con Ollama)

1. **Expansión de consultas en dos fases y protección contra inyección de Prompts**:
   - Analiza el asunto y cuerpo de los correos mediante Ollama LLM para extraer términos técnicos bilingües y sinónimos.
   - Aplica el aislamiento seguro `<untrusted_user_query>` para evitar secuestro de instrucciones o fugas de datos.
2. **Búsqueda ponderada híbrida y puntos de coincidencia bilingüe**:
   - Puntúa según Resumen (+3), Detalle (+1), Comentarios (+1) y Adjuntos (+1), con una bonificación de +5 puntos por coincidencias bilingües.
   - Registra el parámetro `llm.retrieval.max_tickets` en la tabla `config` (por defecto 50).
3. **Procesamiento distribuido Map-Reduce**:
   - **Fase Map**: Analiza tickets individuales y documentos adjuntos (hasta 100.000 caracteres por archivo; PDF, Word, Excel, TXT, LOG, CSV) generando resúmenes intermedios.
   - **Fase Reduce**: Elabora un informe estructurado final (Resumen ejecutivo, Causas y soluciones, Recomendaciones).
   - Limpieza garantizada mediante bloques `finally` sin archivos temporales residuales.
4. **Informe HTML independiente adjunto en el correo (`JTrac-AI-Report-[yyyyMMdd-HHmm].html`)**:
   - **Cuerpo de correo minimalista**: Contiene únicamente la tabla resumen y enlaces directos, evitando distorsiones visuales en clientes de correo.
   - **Adjunto HTML completo**: Construido directamente en memoria mediante `ByteArrayResource` (< 3ms, sin I/O de disco).
   - **Diseño moderno**: Bordes de tabla definidos, tarjetas desplegables `<details>`, adaptación automática al modo oscuro y formato de impresión optimizado.
5. **Guía de Prompts en 8 idiomas**: Documento práctico disponible en [`docs/llm/PROMPT_EXAMPLES_*.md`](../llm/PROMPT_EXAMPLES_es.md).

---

## 3. 📦 Actualización de dependencias y eliminación de advertencias en Java 11

1. **Actualización de `dom4j` a `2.1.4`**:
   - Reemplaza la biblioteca histórica `dom4j:1.6.1` por `org.dom4j:dom4j:2.1.4`.
   - Elimina la advertencia `WARNING: An illegal reflective access operation has occurred` en Tomcat 9 y Java 11.

---

## 4. 🎨 Modernización de interfaz, Accesibilidad de fuentes y Temas

1. **Escalado de fuentes en 4 fases**:
   - Soporta 100% (Estándar), 115% (Cómodo), 130% (Claro) y **Modo Gigante A+++ (145%)** con protección contra parpadeo (Anti-FOUC).
2. **Selector de temas de 3 estados**:
   - Alternancia ágil entre Auto (Sistema), Claro y Oscuro con un solo clic.
3. **Barra de búsqueda unificada y navegación inteligente**:
   - Botón de búsqueda integrado, salto directo por RefId (ej. `PROJ-123`) y búsqueda global para administradores.
4. **Optimización móvil (RWD)**:
   - Menú lateral deslizable (Drawer), panel Bottom-Sheet para historial y paginación en cápsula centrada.

---

## 5. 🛡️ Fortalecimiento de seguridad en producción y protecciones

1. **Filtro global de cabeceras de seguridad**: Inyección de `X-Frame-Options`, `X-Content-Type-Options`, `Strict-Transport-Security` y `Content-Security-Policy`.
2. **Bloqueo para motores de búsqueda (`robots.txt`)**: Protección de tickets internos frente a indexación web.
3. **Protección contra manipulaciones**: Advertencia de rol invitado, lista blanca de parámetros y prevención de doble envío de formularios.

---

## 6. ⚙️ Configuración del sistema y mejoras de estabilidad

1. Supresión de registros debug de Wicket mediante `status.nullValid = ` en todos los idiomas.
2. Modernización de controles booleanos a `IndicatingDropDownChoice`.
3. Registro explícito de controladores JDBC para conexiones de base de datos.
4. Detección automática de codificación UTF-8 para adjuntos de texto y rutas de logotipo relativas.

---

## 7. Guía de actualización y compatibilidad

- **Base de datos**: Totalmente compatible con la versión 2.3.3-2.0.0; **no requiere scripts de migración**.
- **Despliegue**: Sustituya el archivo `ROOT.war` existente en el servidor por `target/jtrac.war`.
- **Documentación relacionada**:
  - [Guía práctica de consultas por correo y Prompts](../llm/PROMPT_EXAMPLES_es.md)
  - [Notas de la versión anterior (2.3.3-2.0.0)](release-2.3.3-2.0.0_es.md)
