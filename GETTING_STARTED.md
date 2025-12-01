# Getting Started - PLD MARS

Guide de démarrage rapide pour les développeurs.

---

## Architecture du projet

**PLD-MARS** est une application Java multi-tiers suivant une architecture SOA (Service-Oriented Architecture) avec 4 modules :

```
Browser (http://localhost:8080)
    ↓
IHM (Interface Homme-Machine) - Port 8080
    ↓ HTTP/JSON
SMA (Service Métier Applicatif) - Port 8081
    ↓ HTTP/JSON
OM-Account (Objet Métier) - Port 8091
    ↓ JPA/JDBC
MySQL Database - Port 3306
```

### Modules

- **common** : Bibliothèque partagée (JsonHttpClient, exceptions, helpers)
- **ihm** : Couche présentation (HTML, JavaScript, servlets)
- **sma** : Couche logique métier (orchestration des services)
- **om-account** : Couche données (entités JPA, DAO, accès base de données)

Chaque couche communique avec la suivante via **HTTP/JSON**. Les modules sont indépendants et tournent sur des ports différents.

---

## Qu'est-ce que Gradle ?

**Gradle** est un outil de build et de gestion de dépendances pour Java (alternative à Maven).

- **Build** : Compile le code, gère les dépendances, package l'application
- **Multi-projet** : Gère plusieurs modules dans un seul projet
- **Gretty** : Plugin Gradle qui lance des serveurs Tomcat embarqués pour le développement

### Fichiers importants

- `settings.gradle` (racine) : Déclare les modules du projet
- `build.gradle` (par module) : Dépendances et configuration de chaque module
- `gradlew` / `gradlew.bat` : Wrapper Gradle (pas besoin d'installer Gradle)

---

## Prérequis

- **Java 17+** installé
- **Docker** (pour MySQL)
- **Git**

---

## Installation et démarrage

### 1. Cloner le projet

```bash
git clone <repo-url>
cd pld-mars
```

### 2. Configurer les variables d'environnement

Le fichier `.env` à la racine contient la configuration de la base de données :

```properties
DATABASE_NAME=pld-mars
DATABASE_USER=pld-mars-user
DATABASE_PASSWORD=pld-mars-password
DATABASE_ROOT_PASSWORD=root-password
DATABASE_URL=jdbc:mysql://localhost:3306/pld-mars
```

### 3. Démarrer MySQL avec Docker

```bash
docker compose up -d
```

Vérifier que MySQL tourne :
```bash
docker ps
```

### 4. Démarrer les services (dans l'ordre)

Ouvrez **3 terminaux différents** :

**Terminal 1 - OM-Account (Port 8091)** :
```bash
cd om-account
./gradlew appRun
```

Attendez le message : `Tomcat 11.0.13 started and listening on port 8091`

**Terminal 2 - SMA (Port 8081)** :
```bash
cd sma
./gradlew appRun
```

Attendez le message : `Tomcat 11.0.13 started and listening on port 8081`

**Terminal 3 - IHM (Port 8080)** :
```bash
cd ihm
./gradlew appRun
```

Attendez le message : `Tomcat 11.0.13 started and listening on port 8080`

### 5. Accéder à l'application

Ouvrez votre navigateur : **http://localhost:8080**

---

## Commandes Gradle utiles

```bash
# Lancer un serveur Tomcat (mode développement)
./gradlew appRun

# Compiler le projet
./gradlew build

# Nettoyer les fichiers compilés
./gradlew clean

# Compiler sans exécuter les tests
./gradlew build -x test

# Arrêter le serveur
# Ctrl+C dans le terminal où tourne appRun
```

---

## Tester l'API

### Via le navigateur

- **Liste des comptes** : http://localhost:8080/api?action=listAccounts
- **Interface Web** : http://localhost:8080

### Via curl

```bash
# Lister les comptes
curl "http://localhost:8080/api?action=listAccounts"

# Ajouter un compte
curl -X POST "http://localhost:8080/api?action=addAccount" \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe"}'
```

---

## Arrêter les services

1. **Arrêter les serveurs Tomcat** : Appuyez sur `Ctrl+C` dans chaque terminal
2. **Arrêter MySQL** :
   ```bash
   docker compose down
   ```

---

## Structure du projet

```
pld-mars/
├── common/                    # Bibliothèque partagée
│   └── src/main/java/
│       └── com/lukamaret/pld_mars_common/
│           ├── JsonHttpClient.java
│           ├── JsonServletHelper.java
│           └── exception/
│
├── ihm/                       # Interface Homme-Machine
│   ├── src/main/java/
│   │   └── com/lukamaret/pld_mars_ihm/
│   │       ├── controller/    # Servlets
│   │       ├── service/       # Appels HTTP vers SMA
│   │       ├── model/         # Actions (Command pattern)
│   │       └── vue/           # Rendu JSON
│   └── src/main/webapp/
│       └── index.html         # Interface Web
│
├── sma/                       # Service Métier Applicatif
│   └── src/main/java/
│       └── com/lukamaret/pld_mars_sma/
│           ├── controller/    # Servlets
│           ├── service/       # Appels HTTP vers OM-Account
│           ├── model/         # Actions
│           └── vue/           # Rendu JSON
│
├── om-account/                # Objet Métier Account
│   └── src/main/java/
│       └── com/lukamaret/pld_mars_account/
│           ├── controller/    # Servlets
│           ├── service/       # Logique métier + transactions
│           ├── domain/        # Entités JPA (Account)
│           ├── infrastructure/# DAO
│           ├── model/         # Actions
│           ├── vue/           # Rendu JSON
│           └── utils/         # JpaUtil
│
├── compose.yaml               # Configuration Docker MySQL
├── settings.gradle            # Configuration multi-modules
└── .env                       # Variables d'environnement
```

---

## Dépannage

### Port déjà utilisé

```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8080 | xargs kill -9
```

### Erreur de connexion MySQL

Vérifiez que Docker tourne et que MySQL est accessible :

```bash
docker ps
docker logs <mysql-container-id>
```

## Ressources

- Lire **ARCHITECTURE.md** pour comprendre en détail le design du système
- **Gradle** : https://docs.gradle.org
- **Gretty** : https://github.com/gretty-gradle-plugin/gretty
- **JPA** : https://jakarta.ee/specifications/persistence/
- **Tomcat** : https://tomcat.apache.org/tomcat-11.0-doc/

