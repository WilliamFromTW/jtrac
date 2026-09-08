# Guía de Compilación y Construcción de JTrac (Español)

[English](BUILD_en.md) | [繁體中文](BUILD_zh-TW.md) | [简体中文](BUILD_zh-CN.md) | [日本語](BUILD_ja.md) | [Tiếng Việt](BUILD_vi.md) | [Deutsch](BUILD_de.md) | [Español](BUILD_es.md) | [Français](BUILD_fr.md)

Esta guía explica detalladamente cómo construir, compilar y empaquetar JTrac 2.3.3-2.0.0, la estructura del archivo WAR y su despliegue en contenedores web modernos (Jetty 10/12, Tomcat 9/10/11).

---

## 1. Requisitos Previos

- **Sistema Operativo**: Windows / Linux / macOS
- **Kit de Desarrollo Java (JDK)**: **JDK 11 o JDK 17** (recomendado JDK 17, p. ej. `W:\developer\jdk-17.0.9` o JDK 11 `W:\developer\jdk-11.0.28`)
  > [!IMPORTANT]
  > Debido a la modernización hacia Spring 5.3, Hibernate 5.6 y Wicket 9, el objetivo de compilación es Java 11. **JDK 8 ya no es compatible**.
- **Apache Maven**: Versión 3.9.x o superior

### Variables de Entorno en Windows
```powershell
$env:JAVA_HOME = "W:\developer\jdk-17.0.9"
$env:PATH = "W:\developer\apache-maven-3.9.9\bin;$env:PATH"
```

Verificación:
```bash
mvn -version
```

---

## 2. Comandos Principales de Construcción

| Comando | Descripción |
|---|---|
| `mvn clean compile` | Limpia la caché y compila `src/main/java` |
| `mvn test-compile` | Compila las pruebas unitarias |
| `mvn test` | Ejecuta todas las pruebas unitarias (JUnit 5 + HSQLDB embebida) |
| `mvn package` | Empaqueta el archivo WAR de producción (`target/jtrac.war`) |
| `mvn package -DskipTests` | Empaquetado rápido (omite pruebas) |
| `mvn clean` | Limpia la carpeta `target/` |

---

## 3. Matriz de Contenedores Web y Despliegue

JTrac 2.3.3-2.0.0 cumple con la especificación Servlet 4.0 (`javax.servlet`):

| Contenedor Web | Versión | Método de Despliegue |
|---|---|---|
| **Jetty 10.x** | 10.0.x (Recomendado) | **Directo**: Copie `target/jtrac.war` a `webapps/ROOT.war`. |
| **Jetty 12.x** | 12.0.x (Actual) | **Nativo**: Habilite el módulo `ee8`:<br/>`java -jar start.jar --add-modules=server,http,ee8-deploy,ee8-webapp` |
| **Tomcat 9.x** | 9.0.x (Recomendado) | **Directo**: Copie `target/jtrac.war` a `webapps/ROOT.war`. |
| **Tomcat 10.x / 11.x** | 10.1.x / 11.0.x | **Migración Automática**: Coloque el WAR en `webapps-javaee/` o conviértalo con `jakartaee-migration`. |

---

## 4. Actualización de Base de Datos

Al actualizar desde 2.3.3-1.0.0:
- Bases de datos externas (MySQL, PostgreSQL, etc.): Ejecute [`etc/sql/upgrade-to-2.0.0.sql`](../../etc/sql/upgrade-to-2.0.0.sql).
- HSQLDB embebida: El proceso se realiza automáticamente con copia de seguridad al iniciar el servidor.

---

## 5. Herramienta Independiente de Exportación HTML (`jtrac-exporter`)

```cmd
mvn clean package -f tools/jtrac-exporter/pom.xml -DskipTests
java -jar tools/jtrac-exporter.jar ^
  --db-url="jdbc:hsqldb:file:./data/db/jtrac;shutdown=true;readonly=true" ^
  --attachments-dir="./data/attachments" ^
  --out="./export-output" ^
  --lang=es
```
