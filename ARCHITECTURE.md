# Architecture PLD-MARS

## Vue d'ensemble

**PLD-MARS** est une application Java Enterprise multi-modules implémentant une architecture orientée services (SOA) à plusieurs niveaux. Le projet démontre une séparation claire des responsabilités à travers 4 couches distinctes communiquant via HTTP/JSON.

**Pattern architectural** : Service-Oriented Architecture (SOA) multi-tiers
**Build Tool** : Gradle (multi-project)
**Base de données** : MySQL 9.0
**Container de servlets** : Tomcat 11

---

## 1. Structure des modules

```
pld-mars/
├── common/           # Utilitaires partagés et code commun
├── ihm/              # Interface Homme-Machine (Couche Présentation)
├── sma/              # Service Métier Applicatif (Couche Logique Applicative)
├── om-account/       # Objet Métier Account (Couche Modèle/Données)
├── compose.yaml      # Configuration Docker MySQL
├── settings.gradle   # Configuration multi-modules Gradle
└── .env              # Variables d'environnement
```

### Hiérarchie des couches

```
┌─────────────────────────────────────────────────┐
│  IHM (Port 8080)                                │
│  Interface utilisateur et présentation          │
└───────────────────┬─────────────────────────────┘
                    │ HTTP/JSON
┌───────────────────▼─────────────────────────────┐
│  SMA (Port 8081)                                │
│  Orchestration de la logique métier             │
└───────────────────┬─────────────────────────────┘
                    │ HTTP/JSON
┌───────────────────▼─────────────────────────────┐
│  OM-Account (Port 8091)                         │
│  Modèle de domaine et accès aux données         │
└───────────────────┬─────────────────────────────┘
                    │ JPA/JDBC
┌───────────────────▼─────────────────────────────┐
│  MySQL Database (Port 3306)                     │
│  Stockage des données                           │
└─────────────────────────────────────────────────┘
```

---

## 2. Description détaillée des modules

### 2.1 Module Common

**Emplacement** : `common/`
**Type** : Bibliothèque Java partagée
**Responsabilités** :
- Communication HTTP inter-services
- Helpers pour sérialisation JSON
- Gestion des connexions base de données
- Filtre d'authentification CAS
- Hiérarchie d'exceptions personnalisées

**Composants clés** :

| Classe | Rôle |
|--------|------|
| `JsonHttpClient` | Client HTTP pour communication JSON entre services |
| `JsonServletHelper` | Utilitaires pour réponses JSON dans les servlets |
| `DBConnection` | Gestion des connexions JDBC |
| `MicroCasFilter` | Filtre d'authentification CAS/SSO |
| `ServiceException` | Exception de base pour la couche service |
| `ServiceIOException` | Exceptions I/O des services |
| `DBException` | Exceptions liées à la base de données |

### 2.2 Module IHM (Interface Homme-Machine)

**Emplacement** : `ihm/`
**Port** : 8080
**Type** : Application Web (WAR)
**Pattern URL** : `/api?action=<actionName>`

**Responsabilités** :
- Servir l'interface utilisateur (HTML/CSS/JavaScript)
- Traiter les requêtes utilisateur
- Déléguer la logique métier au SMA
- Rendre les réponses via les composants Vue

**Architecture** :
```
Browser Request
    ↓
IhmServlet (Front Controller)
    ↓
Action (Command Pattern)
    ↓
IhmService → [HTTP] → SMA Service
    ↓
Vue (Response Renderer)
    ↓
JSON Response
```

**Composants** :

| Composant | Chemin | Rôle |
|-----------|--------|------|
| **IhmServlet** | `controller/IhmServlet.java` | Contrôleur frontal recevant toutes les requêtes |
| **IhmService** | `service/IhmService.java` | Délègue la logique métier au SMA via HTTP |
| **Actions** | `model/*.java` | Pattern Command pour traiter les requêtes |
| - `ListAccountsAction` | `model/ListAccountsAction.java` | Récupère la liste des comptes |
| - `AddAccountAction` | `model/AddAccountAction.java` | Crée un nouveau compte |
| **Vues** | `vue/*.java` | Rend les réponses en JSON |
| - `ListAccountsVue` | `vue/ListAccountsVue.java` | Sérialise la liste de comptes |
| - `AddAccountVue` | `vue/AddAccountVue.java` | Sérialise la réponse de création |
| **UI Web** | `webapp/index.html` | Interface utilisateur JavaScript vanilla |

### 2.3 Module SMA (Service Métier Applicatif)

**Emplacement** : `sma/`
**Port** : 8081
**Type** : Application Java avec servlets
**Pattern URL** : `/api?SMA=<smaName>`

**Responsabilités** :
- Implémenter les workflows métier
- Coordonner les appels aux SOMs (Services Objet Métier)
- Valider les règles métier
- Orchestrer les transactions entre services

**Architecture** :
```
IHM Request
    ↓
SmaServlet (Front Controller)
    ↓
Action (Command Pattern)
    ↓
ServiceMetierApplicatif → [HTTP] → OM-Account Service
    ↓
Vue (Response Renderer)
    ↓
JSON Response to IHM
```

**Composants** :

| Composant | Chemin | Rôle |
|-----------|--------|------|
| **SmaServlet** | `controller/SmaServlet.java` | Contrôleur frontal du SMA |
| **ServiceMetierApplicatif** | `service/ServiceMetierApplicatif.java` | Orchestre les appels aux SOMs |
| **Actions** | `model/*.java` | Traitent les requêtes métier |
| **Vues** | `vue/*.java` | Formatent les réponses JSON |

### 2.4 Module OM-Account (Objet Métier Account)

**Emplacement** : `om-account/`
**Port** : 8091
**Type** : Application Java avec servlets et JPA
**Pattern URL** : `/api?SOM=<somName>`

**Responsabilités** :
- Définir les entités de domaine (Account)
- Accès aux données via DAO
- Opérations de persistance JPA
- Gestion des transactions base de données
- Interaction directe avec MySQL

**Architecture** :
```
SMA Request
    ↓
AccountServlet (Front Controller)
    ↓
Action (Command Pattern)
    ↓
AccountService (Transaction Management)
    ↓
AccountDAO (Data Access)
    ↓
JPA/EntityManager
    ↓
MySQL Database
```

**Composants** :

| Composant | Chemin | Rôle |
|-----------|--------|------|
| **AccountServlet** | `controller/AccountServlet.java` | Contrôleur exposant les opérations SOM |
| **AccountService** | `service/AccountService.java` | Gestion des transactions et logique métier |
| **Account** | `domain/Account.java` | Entité JPA représentant un compte |
| **AccountDAO** | `infrastructure/AccountDAO.java` | Accès aux données (pattern DAO) |
| **JpaUtil** | `utils/JpaUtil.java` | Utilitaire de gestion du cycle de vie JPA |
| **persistence.xml** | `resources/META-INF/persistence.xml` | Configuration de l'unité de persistence JPA |
| **Main** | `console/Main.java` | Application console pour tests |

**Modèle de domaine** :

```java
@Entity
@Table(name = "account")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;
}
```

---

## 3. Flux de communication

### 3.1 Dépendances entre modules

```
ihm (port 8080)
  └─ dépend de: common, sma, om-account (compile time)
     └─ communique avec: sma (runtime HTTP/JSON)

sma (port 8081)
  └─ dépend de: common, om-account (compile time)
     └─ communique avec: om-account (runtime HTTP/JSON)

om-account (port 8091)
  └─ dépend de: common
     └─ communique avec: MySQL (JPA/JDBC)

common
  └─ pas de dépendances (bibliothèque de base)
```

### 3.2 Flux de requête "Lister les comptes"

```
1. Navigateur
   │ HTTP GET
   └─> GET http://localhost:8080/api?action=listAccounts

2. IHM Layer (IhmServlet:doGet)
   │ Crée ListAccountsAction
   │ Appelle IhmService.getAccountList()
   │ HTTP POST via JsonHttpClient
   └─> POST http://localhost:8081/api?SMA=listAccounts

3. SMA Layer (SmaServlet:doPost)
   │ Crée ListAccountsAction
   │ Appelle ServiceMetierApplicatif.getAccountList()
   │ HTTP POST via JsonHttpClient
   └─> POST http://localhost:8091/api?SOM=listAccounts

4. OM-Account Layer (AccountServlet:doPost)
   │ Crée ListAccountsAction
   │ Appelle AccountService.getAllAccounts()
   │ Ouvre transaction JPA
   │ AccountDAO.getAll() exécute requête JPQL
   └─> SELECT a FROM Account a

5. MySQL Database
   │ Retourne les enregistrements
   └─> ResultSet avec les comptes

6. Flux de réponse (ordre inverse)
   OM-Account → JSON {"accounts": [...]}
   │
   └─> SMA → transmet la réponse JSON
       │
       └─> IHM → formate et retourne au navigateur
           │
           └─> Navigateur → affiche la liste
```

### 3.3 Flux de requête "Ajouter un compte"

```
1. Navigateur
   │ HTTP POST avec body JSON: {"name": "John"}
   └─> POST http://localhost:8080/api?action=addAccount

2. IHM Layer
   │ Extrait le nom du body
   │ HTTP POST avec paramètres
   └─> POST http://localhost:8081/api?SMA=addAccount&name=John

3. SMA Layer
   │ Extrait le paramètre name
   │ HTTP POST avec paramètres
   └─> POST http://localhost:8091/api?SOM=addAccount&name=John

4. OM-Account Layer
   │ Crée nouvelle entité Account
   │ Ouvre transaction JPA
   │ AccountDAO.create(account)
   │ JPA persist
   │ Commit transaction
   └─> INSERT INTO account (name) VALUES ('John')

5. MySQL Database
   │ Insère l'enregistrement
   └─> Auto-generated ID

6. Flux de réponse
   OM-Account → HTTP 201 "Account created"
   │
   └─> SMA → HTTP 201
       │
       └─> IHM → HTTP 201
           │
           └─> Navigateur → message de succès
```

---

## 4. Stack technologique

### Technologies principales

| Composant | Version | Usage |
|-----------|---------|-------|
| **Java** | Jakarta EE 11 | Plateforme de base |
| **Gradle** | 8.x | Build et gestion des dépendances |
| **Tomcat** | 11 | Container de servlets (via Gretty) |
| **MySQL** | 9.0 | Base de données relationnelle |

### Frameworks et bibliothèques

**Commun à tous les modules** :
- **Gson 2.13.2** : Sérialisation/désérialisation JSON
- **Jakarta EE Web API 11.0.0** : Servlet, HTTP, APIs web
- **Apache HttpComponents Client5 5.5.1** : Client HTTP pour communication inter-services

**Module OM-Account** :
- **MySQL Connector/J 9.5.0** : Driver JDBC pour MySQL
- **EclipseLink JPA 5.0.0-B11** : Implémentation JPA
- **Dotenv-Java 3.2.0** : Gestion des variables d'environnement

**Développement** :
- **Gretty 5.0.0** : Plugin Gradle pour serveur Tomcat embarqué

### Configuration base de données

**Docker Compose** (`compose.yaml`) :
```yaml
services:
  mysql:
    image: mysql:9
    ports:
      - "3306:3306"
    environment:
      MYSQL_DATABASE: pld-mars
      MYSQL_USER: pld-mars-user
      MYSQL_PASSWORD: ${DATABASE_PASSWORD}
      MYSQL_ROOT_PASSWORD: ${DATABASE_ROOT_PASSWORD}
    volumes:
      - ./lib/mysql:/var/lib/mysql
```

**JPA Configuration** (`persistence.xml`) :
- Unité de persistence : `com.lukamaret.pld_mars_sma`
- Provider : EclipseLink
- Génération du schéma : drop-and-create (mode développement)
- Configuration dynamique via JpaUtil (credentials depuis .env)

---

## 5. Patterns de conception utilisés

| Pattern | Implémentation | Bénéfice |
|---------|----------------|----------|
| **Layered Architecture** | 4 couches (Common, IHM, SMA, OM) | Séparation des responsabilités |
| **Service-Oriented Architecture** | Services indépendants communiquant via HTTP/JSON | Scalabilité et déploiement distribué |
| **Front Controller** | Un servlet par module (`IhmServlet`, `SmaServlet`, `AccountServlet`) | Point d'entrée unique par couche |
| **Command Pattern** | Classes `Action` encapsulant la logique de traitement | Découplage et extensibilité |
| **Data Access Object (DAO)** | `AccountDAO` sépare la logique de persistence | Abstraction de l'accès aux données |
| **Repository Pattern** | DAO fournit une interface collection-like | Simplification de l'accès aux données |
| **Dependency Injection** | Services injectés dans Actions via constructeur | Testabilité et découplage |
| **Model-View-Controller** | Action (Controller), Domain (Model), Vue (View) | Séparation présentation/logique |
| **Transaction Script** | Méthodes de service gèrent les transactions | Cohérence des données |
| **Thread-Local Storage** | JpaUtil gère EntityManager par thread | Isolation des transactions |

---

## 6. Configuration et déploiement

### Variables d'environnement (`.env`)

```properties
DATABASE_NAME=pld-mars
DATABASE_USER=pld-mars-user
DATABASE_PASSWORD=pld-mars-password
DATABASE_ROOT_PASSWORD=root-password
DATABASE_URL=jdbc:mysql://localhost:3306/pld-mars

SOM_ACCOUNT_URL=http://localhost:8091/om-account/api
SMA_URL=http://localhost:8081/sma/api
```

### Architecture de déploiement (mode développement)

```
┌─────────────────────────────────────────┐
│  3 instances Tomcat (via Gretty)       │
│  ┌─────────────────────────────────┐   │
│  │ IHM        : localhost:8080     │   │
│  │ SMA        : localhost:8081     │   │
│  │ OM-Account : localhost:8091     │   │
│  └─────────────────────────────────┘   │
└─────────────────────────────────────────┘
                   ↓ JDBC/JPA
┌─────────────────────────────────────────┐
│  MySQL (Docker) : localhost:3306        │
└─────────────────────────────────────────┘
```

### Séquence de démarrage

```bash
# 1. Démarrer MySQL
docker compose up -d

# 2. Démarrer OM-Account (couche données)
cd om-account
./gradlew appRun

# 3. Démarrer SMA (couche métier)
cd ../sma
./gradlew appRun

# 4. Démarrer IHM (couche présentation)
cd ../ihm
./gradlew appRun

# 5. Accéder à l'application
# http://localhost:8080
```

### Configuration Gradle multi-modules

**`settings.gradle`** (racine) :
```gradle
rootProject.name = 'pld-mars'
include 'ihm'
include 'sma'
include 'om-account'
include 'common'
```

**Dépendances par module** :
- `common/build.gradle` : Bibliothèque Java (pas de servlet)
- `ihm/build.gradle` : WAR, dépend de common/sma/om-account, port 8080
- `sma/build.gradle` : Java app, dépend de common/om-account, port 8081
- `om-account/build.gradle` : Java app, dépend de common, port 8091

---

## 7. Sécurité

| Aspect | Implémentation actuelle | Recommandations |
|--------|------------------------|-----------------|
| **Authentification** | MicroCasFilter disponible (non actif) | Activer CAS ou implémenter JWT |
| **Autorisation** | Non implémentée | Ajouter RBAC (Role-Based Access Control) |
| **Validation des entrées** | Minimale | Valider tous les inputs utilisateur |
| **Credentials DB** | Stockées dans `.env` (non commité) | Utiliser un gestionnaire de secrets |
| **HTTPS** | Non configuré | Activer TLS en production |
| **Injections SQL** | Protection via JPA/JPQL | Maintenir l'usage de requêtes préparées |
| **XSS** | Non traité | Échapper les sorties HTML |
| **CSRF** | Non protégé | Implémenter tokens CSRF |

---

## 8. Points forts de l'architecture

1. **Séparation claire des responsabilités** : Chaque couche a un rôle distinct et bien défini
2. **Modularité** : Les modules peuvent être développés et testés indépendamment
3. **Scalabilité horizontale** : Les services peuvent être déployés sur des serveurs différents
4. **Indépendance technologique** : Chaque couche pourrait utiliser des technologies différentes
5. **Réutilisabilité** : Module common partagé entre toutes les couches
6. **Testabilité** : Chaque couche peut être testée en isolation
7. **Maintenabilité** : Les changements dans une couche minimisent l'impact sur les autres
8. **Apprentissage** : Architecture idéale pour comprendre les systèmes distribués

---

## 9. Axes d'amélioration

### 9.1 Infrastructure

| Amélioration | Bénéfice |
|--------------|----------|
| **Service Discovery** | Remplacer les URLs hardcodées par un registre de services (Consul, Eureka) |
| **API Gateway** | Point d'entrée unique avec routing, rate limiting, authentification |
| **Load Balancer** | Distribution de charge pour haute disponibilité |
| **Circuit Breaker** | Gestion gracieuse des pannes de services (Resilience4j, Hystrix) |

### 9.2 Développement

| Amélioration | Bénéfice |
|--------------|----------|
| **Tests** | Unit tests, tests d'intégration, tests de contrat (Spring Boot Test, JUnit) |
| **Logging structuré** | SLF4J + Logback avec corrélation de requêtes |
| **Validation** | Bean Validation (JSR 303/380) pour valider les inputs |
| **Documentation API** | OpenAPI/Swagger pour documentation interactive |
| **DTO Pattern** | Séparer les objets de transfert des entités de domaine |

### 9.3 Performance

| Amélioration | Bénéfice |
|--------------|----------|
| **Caching** | Redis ou Hazelcast pour réduire les appels base de données |
| **Communication asynchrone** | Message queues (RabbitMQ, Kafka) pour découplage |
| **Connection pooling** | HikariCP pour optimiser les connexions JDBC |
| **Pagination** | Limiter les résultats des requêtes volumineuses |

### 9.4 Monitoring

| Amélioration | Bénéfice |
|--------------|----------|
| **APM** | Application Performance Monitoring (New Relic, DataDog, Prometheus) |
| **Tracing distribué** | Suivi des requêtes à travers les services (Jaeger, Zipkin) |
| **Health checks** | Endpoints de santé pour chaque service |
| **Metrics** | Collecte de métriques métier et techniques |

---

## 10. Diagrammes

### 10.1 Diagramme de composants

```
┌─────────────────────────────────────────────────────────────┐
│                        Browser                              │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP/JSON
┌──────────────────────────▼──────────────────────────────────┐
│  IHM Module (Port 8080)                                     │
│  ┌────────────┐  ┌─────────────┐  ┌──────────┐             │
│  │ IhmServlet │→ │ IhmService  │→ │ Vue      │             │
│  └────────────┘  └─────────────┘  └──────────┘             │
│         ↓                ↓                                   │
│    ┌─────────────────────┐                                  │
│    │ Action (Command)    │                                  │
│    └─────────────────────┘                                  │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP/JSON (JsonHttpClient)
┌──────────────────────────▼──────────────────────────────────┐
│  SMA Module (Port 8081)                                     │
│  ┌────────────┐  ┌────────────────────────┐  ┌──────────┐  │
│  │ SmaServlet │→ │ ServiceMetierApplicatif│→ │ Vue      │  │
│  └────────────┘  └────────────────────────┘  └──────────┘  │
│         ↓                ↓                                   │
│    ┌─────────────────────┐                                  │
│    │ Action (Command)    │                                  │
│    └─────────────────────┘                                  │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP/JSON (JsonHttpClient)
┌──────────────────────────▼──────────────────────────────────┐
│  OM-Account Module (Port 8091)                              │
│  ┌────────────────┐  ┌────────────────┐  ┌──────────┐      │
│  │ AccountServlet │→ │ AccountService │→ │ Vue      │      │
│  └────────────────┘  └────────────────┘  └──────────┘      │
│         ↓                    ↓                               │
│    ┌─────────────────────┐  ↓                               │
│    │ Action (Command)    │  ↓                               │
│    └─────────────────────┘  ↓                               │
│                        ┌────▼──────┐                        │
│                        │ AccountDAO│                        │
│                        └────┬──────┘                        │
│                             ↓                                │
│                        ┌────────────┐                       │
│                        │  JpaUtil   │                       │
│                        └────┬───────┘                       │
└─────────────────────────────┼───────────────────────────────┘
                              │ JPA/JDBC
┌─────────────────────────────▼───────────────────────────────┐
│                   MySQL Database (Port 3306)                │
│                   Table: account (id, name)                 │
└─────────────────────────────────────────────────────────────┘

        ┌──────────────────────────────────────┐
        │   Common Module (Shared Library)     │
        │  - JsonHttpClient                    │
        │  - JsonServletHelper                 │
        │  - DBConnection                      │
        │  - MicroCasFilter                    │
        │  - ServiceException                  │
        └──────────────────────────────────────┘
                (Used by all modules)
```

### 10.2 Diagramme de séquence - Liste des comptes

```
Browser    IhmServlet    IhmService    SmaServlet    SMA-Service    AccountServlet    AccountService    AccountDAO    MySQL
   │            │            │             │              │                │                 │              │          │
   │ GET /api?  │            │             │              │                │                 │              │          │
   │ action=    │            │             │              │                │                 │              │          │
   │ listAccounts            │             │              │                │                 │              │          │
   ├───────────>│            │             │              │                │                 │              │          │
   │            │ execute()  │             │              │                │                 │              │          │
   │            ├───────────>│             │              │                │                 │              │          │
   │            │            │ POST /api?  │              │                │                 │              │          │
   │            │            │ SMA=list    │              │                │                 │              │          │
   │            │            ├────────────>│              │                │                 │              │          │
   │            │            │             │ execute()    │                │                 │              │          │
   │            │            │             ├─────────────>│                │                 │              │          │
   │            │            │             │              │ POST /api?     │                 │              │          │
   │            │            │             │              │ SOM=list       │                 │              │          │
   │            │            │             │              ├───────────────>│                 │              │          │
   │            │            │             │              │                │ execute()       │              │          │
   │            │            │             │              │                ├────────────────>│              │          │
   │            │            │             │              │                │                 │ getAll()     │          │
   │            │            │             │              │                │                 ├─────────────>│          │
   │            │            │             │              │                │                 │              │ SELECT   │
   │            │            │             │              │                │                 │              ├─────────>│
   │            │            │             │              │                │                 │              │          │
   │            │            │             │              │                │                 │              │<─────────┤
   │            │            │             │              │                │                 │              │ ResultSet│
   │            │            │             │              │                │                 │<─────────────┤          │
   │            │            │             │              │                │                 │ List<Account>│          │
   │            │            │             │              │                │<────────────────┤              │          │
   │            │            │             │              │                │ List<Account>   │              │          │
   │            │            │             │              │<───────────────┤                 │              │          │
   │            │            │             │              │ JSON           │                 │              │          │
   │            │            │             │<─────────────┤                │                 │              │          │
   │            │            │             │ JSON         │                │                 │              │          │
   │            │            │<────────────┤              │                │                 │              │          │
   │            │            │ List<Account>              │                │                 │              │          │
   │            │<───────────┤             │              │                │                 │              │          │
   │            │ serialize()│             │              │                │                 │              │          │
   │<───────────┤            │             │              │                │                 │              │          │
   │ JSON       │            │             │              │                │                 │              │          │
```

---

## 11. Glossaire

| Terme | Signification |
|-------|---------------|
| **IHM** | Interface Homme-Machine (Presentation Layer) |
| **SMA** | Service Métier Applicatif (Application Business Service) |
| **SOM** | Service Objet Métier (Business Object Service) |
| **OM** | Objet Métier (Business Object Model) |
| **DAO** | Data Access Object |
| **JPA** | Jakarta Persistence API |
| **SOA** | Service-Oriented Architecture |
| **CAS** | Central Authentication Service |
| **JPQL** | Jakarta Persistence Query Language |

---

## Conclusion

Cette architecture représente une implémentation classique d'une application Java Enterprise n-tiers avec séparation claire des responsabilités. Elle suit des patterns établis issus des frameworks pédagogiques WASO (Web Architectures for Service-Oriented) et DASI (Data Access and Service Integration).

Le design met l'accent sur :
- L'apprentissage des concepts de systèmes distribués
- L'orchestration de services
- Le développement d'applications multi-couches
- La communication inter-services via HTTP/JSON
- La persistance des données via JPA

Cette architecture modulaire permet une évolution progressive vers des architectures plus modernes (microservices, event-driven) tout en maintenant une base solide et compréhensible.
