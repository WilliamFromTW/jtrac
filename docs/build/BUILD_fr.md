# Guide de Compilation et d'Exécution de JTrac (Français)

Ce guide fournit des instructions étape par étape pour compiler, construire et empaqueter le projet JTrac, ainsi qu'une explication détaillée de la gestion des dépendances Maven et de l'architecture d'empaquetage WAR.

---

## 1. Prérequis

Avant de construire le projet, assurez-vous que votre environnement respecte les exigences suivantes :

- **Système d'exploitation** : Windows / Linux / macOS
- **Kit de développement Java (JDK)** : JDK 8 ou JDK 11 (JDK 11 recommandé, ex. `W:\developer\jdk-11.0.28`)
- **Apache Maven** : Version 3.9.x ou supérieure (ex. `W:\developer\apache-maven-3.9.9`)

### Configuration de l'environnement sous Windows
Sous Windows, chargez le script d'environnement dans l'invite de commandes (CMD) avant d'exécuter Maven :
```cmd
call W:\developer\maven.bat
```
Ce script configure `PATH` et `JAVA_HOME` pour la session active du terminal.

Vérifiez votre configuration :
```cmd
mvn -version
```
La sortie confirmera les versions actives de Maven et de Java.

---

## 2. Commandes de Compilation Fréquentes

Exécutez les commandes suivantes depuis le répertoire racine du projet JTrac (où se situe le fichier `pom.xml`) :

| Commande | Description |
|---|---|
| `mvn compile` | Compile 137 fichiers sources Java sous `src/main/java` et traite les ressources |
| `mvn test-compile` | Compile toutes les classes de tests unitaires sous `src/test/java` |
| `mvn test` | Exécute les tests unitaires (utilise la base HSQLDB en mémoire intégrée) |
| `mvn package` | Exécute les tests et empaquette l'application web complète dans `target/jtrac.war` |
| `mvn package -DskipTests` | Empaquette rapidement `target/jtrac.war` en ignorant les tests unitaires |
| `mvn clean` | Nettoie le répertoire `target/` et les artefacts générés |
| `mvn clean compile` | Supprime les artefacts précédents et recompile l'intégralité du code |

---

## 3. Gestion Automatique des Dépendances via Maven (`~/.m2/repository`)

JTrac est configuré avec la gestion standard des dépendances Maven. Toutes les bibliothèques tierces nécessaires (y compris Spring Framework, Apache Wicket, Hibernate, Acegi Security, Lucene, etc.) sont déclarées dans [`pom.xml`](../../pom.xml).

### Processus Automatique de Téléchargement et de Cache :
1. Lors de votre première exécution de `mvn compile` ou `mvn package`, Maven se connecte au référentiel central (Maven Central).
2. Toutes les dépendances déclarées et transitives sont automatiquement téléchargées dans votre cache local :
   - **Windows** : `%USERPROFILE%\.m2\repository\`
   - **Linux / macOS** : `~/.m2/repository/`
3. Les compilations suivantes lisent directement depuis votre cache local `.m2`. **Vous n'avez jamais besoin de rechercher, télécharger ou configurer des fichiers JAR manuellement.**

---

## 4. Intégration des Bibliothèques Tierces dans le WAR (`WEB-INF/lib/`)

Une question courante est : « Lors du déploiement de JTrac sur un conteneur de servlets comme Jetty ou Tomcat, dois-je copier manuellement les JARs tiers dans le dossier `lib/` du serveur ? »

**Réponse : Absolument PAS !**

### Architecture du Paquet WAR :
Lorsque vous lancez `mvn package`, Maven construit automatiquement une archive Web Application Archive autonome : [`target/jtrac.war`](../../target/jtrac.war) :

```text
jtrac.war
├── META-INF/
│   └── MANIFEST.MF
├── WEB-INF/
│   ├── classes/                 <-- Classes compilées et fichiers de ressources UTF-8
│   │   ├── info/jtrac/...
│   │   └── messages*.properties
│   ├── lib/                     <-- [TOUS les 53 JARs tiers sont inclus ici !]
│   │   ├── spring-2.5.6.jar
│   │   ├── wicket-1.3.7.jar
│   │   ├── hibernate-3.2.7.ga.jar
│   │   └── ...
│   └── web.xml                  <-- Configuration Servlet
└── resources/
```

### Points Clés du Déploiement :
- **Isolation du Classloader** : Les conteneurs de servlets isolent automatiquement les bibliothèques de `WEB-INF/lib/` pour chaque application web.
- **Serveur Propre** : Le dossier `lib/` du serveur doit rester propre ; ne copiez pas les JARs applicatifs dans ce dossier.
- **Déploiement Simplifié** : Déposez simplement `target/jtrac.war` (ou renommé en `ROOT.war`) dans le répertoire `webapps/` de votre serveur et démarrez le service.

---

## 5. Test et Exécution en Local

Pour exécuter JTrac localement après empaquetage :

### Avec Jetty :
1. Copiez `target/jtrac.war` vers `W:\developer\jtrac-2.3.3\webapps\ROOT.war`.
2. Lancez `W:\developer\jtrac-2.3.3\start.bat`.
3. Ouvrez votre navigateur sur : `http://localhost:8888` (Identifiants par défaut : `admin` / `admin`).

---

## 6. Construction et Exécution de l'Outil CLI d'Exportation HTML (jtrac-exporter)

Le projet intègre un outil en ligne de commande autonome `jtrac-exporter` permettant d'exporter les enregistrements de la base de données directement en rapports statiques HTML responsives avec pièces jointes via JDBC standard.

### 6.1 Construire l'outil (Fat JAR)
Exécutez cette commande depuis le répertoire racine du projet :
```cmd
mvn clean package -f tools/jtrac-exporter/pom.xml -DskipTests
```
Une fois la compilation réussie, le JAR exécutable est généré sous :
`tools/jtrac-exporter.jar`

### 6.2 Exécuter l'exportation (Mode Commande)
Depuis la racine du projet, exporter depuis la base locale HSQLDB :
```cmd
java -jar tools/jtrac-exporter.jar ^
  --db-url="jdbc:hsqldb:file:./data/db/jtrac;shutdown=true;readonly=true" ^
  --attachments-dir="./data/attachments" ^
  --out="./export-hsqldb" ^
  --lang=fr
```

Pour les bases de données distantes MySQL, PostgreSQL ou SQL Server :
```cmd
java -jar tools/jtrac-exporter.jar ^
  --db-url="jdbc:mysql://192.168.1.100:3306/jtrac?useUnicode=true&characterEncoding=UTF-8" ^
  --db-user="jtrac" ^
  --db-password="votre_mot_de_passe" ^
  --attachments-dir="/chemin/vers/pieces_jointes" ^
  --out="./export-mysql" ^
  --lang=fr
```
Pour afficher toutes les options disponibles : `java -jar tools/jtrac-exporter.jar --help`.
