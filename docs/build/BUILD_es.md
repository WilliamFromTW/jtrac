# Guía de Construcción y Compilación de JTrac (Español)

Esta guía proporciona instrucciones paso a paso para construir, compilar y empaquetar el proyecto JTrac, junto con una explicación profunda de la gestión de dependencias de Maven y la arquitectura de empaquetado WAR.

---

## 1. Requisitos Previos

Antes de construir el proyecto, asegúrese de que su entorno cumpla con los siguientes requisitos:

- **Sistema Operativo**: Windows / Linux / macOS
- **Kit de Desarrollo Java (JDK)**: JDK 8 o JDK 11 (se recomienda JDK 11, ej., `W:\developer\jdk-11.0.28`)
- **Apache Maven**: Versión 3.9.x o superior (ej., `W:\developer\apache-maven-3.9.9`)

### Configuración del Entorno en Windows
En Windows, ejecute el script por lotes en el Símbolo del sistema (CMD) antes de ejecutar Maven:
```cmd
call W:\developer\maven.bat
```
Este script configura `PATH` y `JAVA_HOME` para la sesión actual de la terminal.

Verifique su entorno:
```cmd
mvn -version
```
La salida confirmará las versiones activas de Maven y Java.

---

## 2. Comandos Habituales de Construcción

Ejecute los siguientes comandos desde el directorio raíz del proyecto JTrac (donde se encuentra `pom.xml`):

| Comando | Descripción |
|---|---|
| `mvn compile` | Compila 137 archivos fuente Java en `src/main/java` y procesa los recursos |
| `mvn test-compile` | Compila todas las clases de pruebas unitarias en `src/test/java` |
| `mvn test` | Ejecuta las pruebas unitarias (usa HSQLDB en memoria; no requiere BD externa) |
| `mvn package` | Ejecuta pruebas y empaqueta la aplicación web completa en `target/jtrac.war` |
| `mvn package -DskipTests` | Construye y empaqueta rápidamente `target/jtrac.war` omitiendo pruebas unitarias |
| `mvn clean` | Limpia el directorio `target/` y los artefactos de compilación previos |
| `mvn clean compile` | Limpia artefactos anteriores y recompila todo el código fuente |

---

## 3. Gestión Automática de Dependencias con Maven (`~/.m2/repository`)

JTrac está configurado con la gestión de dependencias estándar de Maven. Todas las librerías de terceros necesarias (incluyendo Spring Framework, Apache Wicket, Hibernate, Acegi Security, Lucene, etc.) están declaradas en [`pom.xml`](../../pom.xml).

### Flujo Automático de Descarga y Caché:
1. Al ejecutar por primera vez `mvn compile` o `mvn package`, Maven se conecta al repositorio remoto (Maven Central).
2. Todas las dependencias declaradas y transitivas se descargan automáticamente a su caché local:
   - **Windows**: `%USERPROFILE%\.m2\repository\`
   - **Linux / macOS**: `~/.m2/repository/`
3. Las compilaciones posteriores leen directamente de la caché local `.m2`. **Nunca necesita buscar, descargar o configurar archivos JAR manualmente.**

---

## 4. Empaquetado de Librerías de Terceros en el WAR (`WEB-INF/lib/`)

Una duda habitual es: "¿Al desplegar JTrac en un contenedor de servlets como Jetty o Tomcat, es necesario copiar JARs de terceros en la carpeta `lib/` del servidor?"

**Respuesta: ¡En absoluto!**

### Arquitectura del Paquete WAR:
Al ejecutar `mvn package`, Maven construye automáticamente un Web Application Archive autónomo: [`target/jtrac.war`](../../target/jtrac.war):

```text
jtrac.war
├── META-INF/
│   └── MANIFEST.MF
├── WEB-INF/
│   ├── classes/                 <-- Clases compiladas y archivos de recursos en UTF-8
│   │   ├── info/jtrac/...
│   │   └── messages*.properties
│   ├── lib/                     <-- [¡TODOS los 53 JARs de terceros están aquí!]
│   │   ├── spring-2.5.6.jar
│   │   ├── wicket-1.3.7.jar
│   │   ├── hibernate-3.2.7.ga.jar
│   │   └── ...
│   └── web.xml                  <-- Configuración del servlet
└── resources/
```

### Puntos Clave de Despliegue:
- **Aislamiento de Classloader**: Los contenedores de servlets aíslan automáticamente las librerías en `WEB-INF/lib/` para cada aplicación.
- **Servidor Limpio**: La carpeta `lib/` del propio servidor debe permanecer limpia; no copie JARs de la aplicación allí.
- **Despliegue Sencillo**: Simplemente coloque `target/jtrac.war` (o cámbielo a `ROOT.war`) en la carpeta `webapps/` del servidor y arranque el contenedor.

---

## 5. Pruebas y Ejecución Local

Para probar JTrac localmente después del empaquetado:

### Usando Jetty:
1. Copie `target/jtrac.war` a `W:\developer\jtrac-2.3.3\webapps\ROOT.war`.
2. Ejecute `W:\developer\jtrac-2.3.3\start.bat`.
3. Abra su navegador en: `http://localhost:8888` (Credenciales por defecto: `admin` / `admin`).

---

## 6. Construcción y Ejecución del Exportador HTML CLI (jtrac-exporter)

El proyecto incluye la herramienta CLI independiente `jtrac-exporter`, que exporta registros de base de datos directamente a hilos de discusión estáticos en HTML con archivos adjuntos mediante JDBC estándar.

### 6.1 Construir la Herramienta (Fat JAR)
Ejecute este comando desde el directorio raíz del proyecto:
```cmd
mvn clean package -f tools/jtrac-exporter/pom.xml -DskipTests
```
Tras una compilación exitosa, el JAR ejecutable se generará directamente en:
`tools/jtrac-exporter.jar`

### 6.2 Ejecutar la Exportación (Modo Comando)
Exportar desde la base de datos local HSQLDB:
```cmd
java -jar tools/jtrac-exporter.jar ^
  --db-url="jdbc:hsqldb:file:./data/db/jtrac;shutdown=true;readonly=true" ^
  --attachments-dir="./data/attachments" ^
  --out="./export-hsqldb" ^
  --lang=es
```

Para bases de datos remotas MySQL, PostgreSQL o SQL Server:
```cmd
java -jar tools/jtrac-exporter.jar ^
  --db-url="jdbc:mysql://192.168.1.100:3306/jtrac?useUnicode=true&characterEncoding=UTF-8" ^
  --db-user="jtrac" ^
  --db-password="su_contraseña" ^
  --attachments-dir="/ruta/a/adjuntos" ^
  --out="./export-mysql" ^
  --lang=es
```
Para ver todas las opciones disponibles, ejecute: `java -jar tools/jtrac-exporter.jar --help`.
