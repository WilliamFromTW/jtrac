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

---

## 4. Mise à Niveau de la Base de Données

Lors de la mise à niveau depuis 2.3.3-1.0.0 :
- Bases externes (MySQL, PostgreSQL, etc.) : Exécutez [`etc/sql/upgrade-to-2.0.0.sql`](../../etc/sql/upgrade-to-2.0.0.sql).
- HSQLDB intégrée : La sauvegarde et la migration s'exécutent automatiquement au démarrage du serveur.

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
