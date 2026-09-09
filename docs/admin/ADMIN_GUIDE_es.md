# Guía del Administrador del Sistema y Configuración (Español)

[English](ADMIN_GUIDE_en.md) | [繁體中文](ADMIN_GUIDE_zh-TW.md) | [简体中文](ADMIN_GUIDE_zh-CN.md) | [日本語](ADMIN_GUIDE_ja.md) | [Tiếng Việt](ADMIN_GUIDE_vi.md) | [Deutsch](ADMIN_GUIDE_de.md) | [Español](ADMIN_GUIDE_es.md) | [Français](ADMIN_GUIDE_fr.md)

---

## Índice
1. [Inicio de Sesión y Credenciales Predeterminadas](#1-inicio-de-sesión-y-credenciales-predeterminadas)
2. [Configuración Inicial Obligatoria](#2-configuración-inicial-obligatoria)
3. [Flujograma del Sistema y Correo (Mermaid)](#3-flujograma-del-sistema-y-correo-mermaid)
4. [Funciones Principales de Administración](#4-funciones-principales-de-administración)
5. [Copia de Seguridad y Restauración Completa del Sistema (Protección Antibloqueo)](#5-copia-de-seguridad-y-restauración-completa-del-sistema-protección-antibloqueo)
6. [Seguridad, Actualización de Base de Datos y Mantenimiento](#6-seguridad-actualización-de-base-de-datos-y-mantenimiento)

---

## 1. Inicio de Sesión y Credenciales Predeterminadas

- **URL de Acceso**: `http://<IP-Servidor>:<Puerto>/` (p. ej. `http://localhost:8888/`)
- **Usuario Predeterminado**: `admin`
- **Contraseña Predeterminada**: `admin`

> [!WARNING]
> Cambie la contraseña inmediatamente tras el primer inicio de sesión en **OPTIONS** ➜ **Edit User Profile**.

---

## 2. Configuración Inicial Obligatoria

En **OPTIONS** ➜ **Manage Settings**:

### 1. `jtrac.url.base` (URL Base del Sistema - CRÍTICO)
- **Predeterminado**: `http://localhost/jtrac/`
- **Recomendado**: URL accesible por los usuarios, **terminada en barra inclinada `/`** (p. ej. `http://192.168.1.100:8888/` o `https://issues.yourcompany.com/`).
- **Importancia**: Las notificaciones por correo usan este prefijo. Si se deja en `localhost`, los usuarios remotos no podrán abrir los enlaces.

---

### 2. `locale.default` (Idioma Predeterminado)
- Recomendado: `es` o `en`.

---

### 3. Configuración del Servidor SMTP
- `mail.server.host`, `mail.server.port`, `mail.server.username`, `mail.server.password`, `mail.server.starttls.enable`, `mail.from`.

---

### 4. Ajustes Avanzados y Paginación
- `users.list.pageSize`: Tamaño de página en lista de usuarios (predeterminado: `25`; opciones: 10, 25, 50, 100, Todos).
- `spaces.list.pageSize`: Tamaño de página en lista de proyectos (predeterminado: `25`; opciones: 10, 25, 50, 100, Todos).
- `attachment.maxsize`: Tamaño máximo de archivos en MB (predeterminado `10`).

---

## 3. Flujograma del Sistema y Correo (Mermaid)

```mermaid
flowchart TD
    Start([Iniciar JTrac]) --> Login[Primer Inicio de Sesión<br/>admin / admin]
    Login --> ChangePwd[Cambiar Contraseña Admin<br/>OPTIONS ➜ Edit User Profile]
    ChangePwd --> ConfigSettings[Configurar Parámetros del Sistema<br/>OPTIONS ➜ Manage Settings]
    
    subgraph CriticalSettings [Configuración Esencial]
        ConfigSettings --> SetUrlBase["Definir jtrac.url.base<br/>(p. ej. http://192.168.1.100:8888/)"]
        ConfigSettings --> SetLocale["Definir locale.default<br/>(p. ej. es)"]
        ConfigSettings --> SetSMTP["Configurar Servidor SMTP<br/>(host / port / from)"]
        ConfigSettings --> SetPaging["Configurar Paginación<br/>(users/spaces.list.pageSize)"]
    end
    
    CriticalSettings --> CreateSpaces[Crear Espacios de Seguimiento<br/>OPTIONS ➜ Manage Spaces]
    CreateSpaces --> CreateUsers[Crear Usuarios y Asignar Roles<br/>OPTIONS ➜ Manage Users]
    CreateUsers --> Finish([Sistema Listo para Producción])
```

---

## 4. Funciones Principales de Administración

| Función | Propósito |
|---|---|
| **Edit User Profile** | Actualizar correo, nombre y contraseña del administrador. |
| **Manage Users** | Gestión de usuarios, paginación, restablecer contraseñas, rol Admin. |
| **Manage Spaces** | Gestión de proyectos, paginación, campos personalizados, roles. |
| **Configure Links** | Configurar enlaces externos en la barra de navegación. |
| **Manage Settings** | Parámetros globales (URL base, SMTP, paginación). |
| **Rebuild Indexes** | Reconstruir el índice de búsqueda Lucene. |
| **Export HTML** | Exportación de incidencias a HTML y ZIP desde la web. |
| **Backup & Restore** | Copia de seguridad y restauración completa del sistema: Exclusivo para SuperUsers. Permite la descarga en 1 clic de un archivo ZIP con base de datos JSON, volcado SQL completo (`jtrac-dump.sql`) y adjuntos, y restauración segura con instantánea de emergencia y protección antibloqueo de credenciales. |

---

## 5. Copia de Seguridad y Restauración Completa del Sistema (Protección Antibloqueo)

JTrac proporciona capacidades nativas de recuperación ante desastres y migración de todo el sistema, disponibles exclusivamente para administradores con privilegios de SuperUser:

1. **Exportación de Paquete de Respaldo Completo en un Clic**:
   - Navegue a **OPTIONS** ➜ **Backup & Restore**.
   - Haga clic en **Descargar respaldo (.zip)**. Todas las entidades de la base de datos se serializan en formato JSON estándar (`manifest.json` y `data/system_data.json`), se genera un volcado SQL independiente y completo `jtrac-dump.sql` (que incluye DDL ANSI, notas de dialectos MySQL/PostgreSQL/HSQLDB, sentencias INSERT ANSI ordenadas por clave externa y sugerencias para reiniciar secuencias), y se comprimen junto con el directorio físico `${jtrac.home}/attachments/` en un único archivo `.zip` con marca de tiempo listo para su descarga inmediata.
2. **Motor de Restauración Segura (Safe Restore Engine)**:
   - Seleccione un archivo `.zip` de respaldo válido de JTrac, marque la casilla de verificación de confirmación de sobrescritura y haga clic en **Ejecutar restauración**.
   - **Instantánea de Emergencia Automática en el Servidor (Safety Snapshot)**: Antes de sobrescribir cualquier dato existente, el sistema crea automáticamente una instantánea completa en `${jtrac.home}/backups/` en el servidor, garantizando que el estado actual siempre se pueda revertir ante imprevistos.
   - **Escudo de Protección Antibloqueo de Credenciales (Anti-Lockout Credential Shield)**: El motor identifica al administrador que ejecuta la restauración. Incluso si el respaldo contiene contraseñas desactualizadas u olvidadas, el sistema **preserva obligatoriamente el hash de contraseña activo y el estatus de `ROLE_ADMIN` del operador actual** (o lo inyecta si no existía), eliminando por completo el riesgo de que el administrador quede bloqueado fuera del sistema.
   - **Reconstrucción Asíncrona de Índices de Búsqueda**: Una vez finalizada la restauración, los índices de búsqueda de texto completo Lucene se reconstruyen automáticamente en segundo plano. La sesión activa del administrador se mantiene sin interrupción alguna.

---

## 6. Seguridad, Actualización de Base de Datos y Mantenimiento

1. **Seguridad de Contraseñas (Migración a BCrypt)**:
   - Modernizado con Spring Security 5.8 (BCrypt). Los hashes MD5 existentes se actualizan automáticamente tras el inicio de sesión.
2. **Actualización de Base de Datos (Desde 2.3.3-1.0.0)**:
   - Bases externas: Ejecute [`etc/sql/upgrade-to-2.0.0.sql`](../../etc/sql/upgrade-to-2.0.0.sql).
   - HSQLDB embebida: Se actualiza automáticamente a 2.x con respaldo al iniciar.
3. **Directorio de Datos (`jtrac.home`): Prioridad de Resolución y Copias de Seguridad**:
   - **Prioridad de Resolución de 4 Niveles de `jtrac.home`**:
     1. Parámetro `jtrac.home` en `WEB-INF/classes/jtrac-init.properties`.
     2. Propiedad del sistema JVM `-Djtrac.home=...` (**Recomendado en Producción y Contenedores**).
     3. Parámetro de inicialización del Servlet Context (`web.xml` o XML de contexto de Tomcat).
     4. **Respaldo por Defecto (Default Fallback)**: `System.getProperty("user.home") + "/.jtrac"`.
   - **Pregunta Frecuente: ¿Por qué en Linux con Tomcat se guardaba por defecto en `/root/.jtrac`?**
     Si Tomcat se ejecuta bajo Linux con el usuario `root` sin definir las prioridades 1–3, Java evalúa `user.home` como `/root`. Por lo tanto, JTrac crea automáticamente el directorio oculto `/root/.jtrac` para almacenar todos sus datos. Si se ejecuta con un usuario de servicio estándar `jtrac`, se ubicará en `/home/jtrac/.jtrac`.
   - **Ejemplos de Configuración en Contenedores**:
     - Linux Tomcat (`bin/setenv.sh`): Agregar `export CATALINA_OPTS="$CATALINA_OPTS -Djtrac.home=/var/jtrac-data"`
     - Windows Tomcat (`bin/setenv.bat`): Agregar `set "CATALINA_OPTS=%CATALINA_OPTS% -Djtrac.home=D:/jtrac-data"`
     - Jetty: Pasar `-Djtrac.home=data` en el comando de arranque (como en `start-jtrac.bat`).
   - **Estructura y Estrategia de Copias de Seguridad**:
     - `jtrac.properties`: Conexión de base de datos, credenciales y dialecto.
     - `db/`: Archivos de base de datos HSQLDB integrada (respaldar según programación habitual si usa DB externa).
     - `attachments/`: Directorio físico de archivos adjuntos (`${jtrac.home}/attachments/`), incluir siempre en copias periódicas.
     - `indexes/`: Índices Lucene (pueden reconstruirse desde el panel de administración).
     - `backups/`: Copias instantáneas de seguridad creadas antes de cada restauración.
     - Se recomienda descargar periódicamente una copia de seguridad completa (base de datos y adjuntos) desde **OPTIONS** ➜ **Backup & Restore**.
4. **Partición de Archivos Adjuntos y Búsqueda de Texto Completo**:
   - **Estructura Particionada por ID de Proyecto (Opción C)**: Los adjuntos se almacenan en `${jtrac.home}/attachments/{spaceId}/{prefix}_{filename}`, inmunes a cambios de nombre del proyecto.
   - **Mecanismo de Doble Lectura de Respaldo (Dual-Read Fallback)**: Fallback automático al directorio raíz y a la carpeta de huérfanos (`attachments/0_ORPHAN/`), asegurando 0% de enlaces 404 rotos.
   - **Búsqueda de Texto Completo y Parámetros de Seguridad**: Indexación de texto para `.xlsx`, `.docx`, `.pdf`, `.txt`, `.csv`, `.md`, `.log` con límite de 10MB por archivo y 50,000 caracteres configurables en `config`.
   - **Reconstrucción de Índices y Optimización de Búsqueda**:
     - Con el analizador mejorado `JtracAnalyzer` (tokenización estándar, minúsculas y lematizador Porter), las consultas coinciden automáticamente entre singular/plural y tiempos verbales (por ejemplo, buscar `window` coincide exactamente con documentos que contienen `Windows`; buscar `test` coincide con `tests`/`testing`).
     - Incorpora respaldo inteligente de prefijo: palabras simples (longitud >= 2) sin coincidencias exactas se expanden automáticamente a prefijo comodín (`win` a `win*`). Los caracteres CJK conservan su tokenización unigrama exacta, y los caracteres acentuados mantienen su precisión original.
     - **Requisito Tras la Actualización**: Tras actualizar la versión, los administradores deben ir a **OPTIONS ➜ Rebuild Indexes** y ejecutar una reconstrucción completa de índices para reprocesar registros y adjuntos históricos con las nuevas reglas de lematización.


