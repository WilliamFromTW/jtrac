# Guide de Compilation et de Construction JTrac (Français)

[English](BUILD_en.md) | [繁體中文](BUILD_zh-TW.md) | [简体中文](BUILD_zh-CN.md) | [日本語](BUILD_ja.md) | [Tiếng Việt](BUILD_vi.md) | [Deutsch](BUILD_de.md) | [Español](BUILD_es.md) | [Français](BUILD_fr.md)

Ce guide fournit des instructions complètes sur la compilation, l'empaquetage de JTrac 2.3.3-2.0.0, la structure du fichier WAR et le déploiement sur les conteneurs web modernes (Jetty 10/12, Tomcat 9/10/11).

---

## 1. Prérequis

- **Système d'exploitation** : Windows / Linux / macOS
- **Kit de Développement Java (JDK)** : **JDK 11 ou JDK 17** (recommandé JDK 17, ex: `W:\developer\jdk-17.0.9` ou JDK 11 `W:\developer\jdk-11.0.28`)
  > [!IMPORTANT]
  > Suite à la modernisation vers Spring 5.3, Hibernate 5.6 et Wicket 9, la cible de compilation est Java 11. **JDK 8 n'est plus pris en charge**.
- **Apache Maven** : Version 3.9.x ou supérieure

### Variables d'Environnement Windows
```powershell
$env:JAVA_HOME = "W:\developer\jdk-17.0.9"
$env:PATH = "W:\developer\apache-maven-3.9.9\bin;$env:PATH"
```

Vérification :
```bash
mvn -version
```

---

## 2. Commandes de Construction Courantes

| Commande | Description |
|---|---|
| `mvn clean compile` | Nettoie le cache et recompile `src/main/java` |
| `mvn test-compile` | Compile les tests unitaires |
| `mvn test` | Exécute les tests unitaires (JUnit 5 + HSQLDB intégrée) |
| `mvn package` | Empaquette l'archive WAR de production (`target/jtrac.war`) |
| `mvn package -DskipTests` | Empaquetage rapide (ignore les tests) |
| `mvn clean` | Supprime le répertoire `target/` |

---

## 3. Matrice des Conteneurs Web et Déploiement

JTrac 2.3.3-2.0.0 respecte la spécification Servlet 4.0 (`javax.servlet`) :

| Conteneur Web | Version | Méthode de Déploiement |
|---|---|---|
| **Jetty 10.x** | 10.0.x (Recommandé) | **Direct** : Copiez `target/jtrac.war` vers `webapps/ROOT.war`. |
| **Jetty 12.x** | 12.0.x (Actuel) | **Natif** : Activez le module `ee8` :<br/>`java -jar start.jar --add-modules=server,http,ee8-deploy,ee8-webapp` |
| **Tomcat 9.x** | 9.0.x (Recommandé) | **Direct** : Copiez `target/jtrac.war` vers `webapps/ROOT.war`. |
| **Tomcat 10.x / 11.x** | 10.1.x / 11.0.x | **Migration Automatique** : Placez le WAR dans `webapps-javaee/` ou convertissez avec `jakartaee-migration`. |

### 3.1 Répertoire de Données (`jtrac.home`) : Priorité de Résolution et Configuration

Le répertoire racine de stockage des données et pièces jointes est régi par la variable `jtrac.home` ([`JtracConfigurer`](../../src/main/java/info/jtrac/config/JtracConfigurer.java)), selon un ordre strict de 4 niveaux de priorité :

1. **Priorité 1** : `jtrac.home` défini dans `WEB-INF/classes/jtrac-init.properties`.
2. **Priorité 2 (Recommandé en production)** : Propriété système JVM `-Djtrac.home=...`.
3. **Priorité 3** : Paramètre d'initialisation de Servlet Context (`web.xml` ou contexte Tomcat).
4. **Priorité 4 (Repli par Défaut Default Fallback)** : `System.getProperty("user.home") + "/.jtrac"`.
   - **Remarque Tomcat** : Si Tomcat s'exécute sous Linux en tant qu'utilisateur `root` sans définir les priorités 1 à 3, JTrac enregistrera automatiquement ses données dans `/root/.jtrac`.
   - **Jetty Local** : `start-jtrac.bat` configure `-Djtrac.home=data`, stockant les données dans `W:\developer\jetty-10.0.26\data\`.

#### Structure Standard de `jtrac.home` :
- `jtrac.properties` : Configuration de connexion à la base, URL, identifiants et dialecte Hibernate.
- `db/` : Fichiers de la base HSQLDB intégrée (`jtrac.script`, `jtrac.data`, etc.).
- `attachments/` : Pièces jointes partitionnées par ID de projet (`attachments/{spaceId}/`).
- `indexes/` : Index de recherche plein texte Lucene.
- `backups/` : Instantanés de sécurité d'urgence créés avant chaque restauration.
- `logs/` : Journaux d'exécution de l'application (`jtrac.log`).

#### Configuration dans les Conteneurs :
- **Linux Tomcat (`bin/setenv.sh`)** :
  ```bash
  export CATALINA_OPTS="$CATALINA_OPTS -Djtrac.home=/var/jtrac-data"
  ```
- **Windows Tomcat (`bin/setenv.bat`)** :
  ```cmd
  set "CATALINA_OPTS=%CATALINA_OPTS% -Djtrac.home=D:/jtrac-data"
  ```
- **Jetty / Ligne de Commande** :
  ```bash
  java -Djtrac.home=/var/jtrac-data -jar start.jar
  ```

---

## 4. Mise à Niveau de la Base de Données et du Stockage
 
Lors de la mise à niveau depuis 2.3.3-1.0.0 :
- Bases externes (MySQL, PostgreSQL, etc.) : Exécutez [`etc/sql/upgrade-to-2.0.0.sql`](../../etc/sql/upgrade-to-2.0.0.sql).
- HSQLDB intégrée : La sauvegarde et la migration s'exécutent automatiquement au démarrage du serveur.
- Migration des pièces jointes : `AttachmentStorageMigrator` organise automatiquement les anciens fichiers dans des dossiers par ID de projet (`attachments/{spaceId}/`), isolant les orphelins dans `attachments/0_ORPHAN/`.
- Recherche plein texte : Extraction intégrée pour `.xlsx`, `.docx` (parseur streaming OpenXML JDK natif), `.pdf` (Apache PDFBox 2.0.31), `.txt`, `.csv`, `.md`, `.log` avec `SmartCharsetDetector`.

---

## 5. Outil Indépendant d'Exportation HTML (`jtrac-exporter`)

```cmd
mvn clean package -f tools/jtrac-exporter/pom.xml -DskipTests
java -jar tools/jtrac-exporter.jar ^
  --db-url="jdbc:hsqldb:file:./data/db/jtrac;shutdown=true;readonly=true" ^
  --attachments-dir="./data/attachments" ^
  --out="./export-output" ^
  --lang=fr
```

---

## 6. Construction et Déploiement avec Conteneur Docker Natif (Eclipse Temurin 17 + Jetty 12)

Le projet propose un environnement de compilation multi-étapes (multi-stage) Docker ne nécessitant aucune installation préalable de JDK ou Maven en local :

### 6.1 Commandes Docker Natives (Recommandé)
Accédez au répertoire `docker/` et lancez la construction en utilisant la racine du projet (`..`) comme contexte :
```bash
cd docker
docker build -f Dockerfile -t jtrac:latest ..
docker run -d -p 8888:8080 -v jtrac_data:/jtrac-data --name jtrac jtrac:latest
```

### 6.2 Scripts Auxiliaires Multiplateformes et Docker Compose
- **Windows** : Exécutez `build.bat` et `run.bat` dans `docker/`
- **Linux / macOS** : Exécutez `./build.sh` et `./run.sh` dans `docker/`
- **Docker Compose** : Exécutez `docker compose up -d` dans `docker/`

Une fois le conteneur démarré, ouvrez votre navigateur à l'adresse : `http://localhost:8888/` (Identifiants par défaut : `admin` / `admin`). Pour la configuration des bases de données externes et des options JVM, consultez [`docker/README.md`](../../docker/README.md).

