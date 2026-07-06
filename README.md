# Afristock — Gestion de stock SaaS multi-boutiques

**Afristock** est une application **SaaS multi-tenant de gestion de stock et de commerce**, développée en Java / Spring Boot, destinée à des boutiques et PME (multi-sites) souhaitant gérer produits, stocks, ventes, achats, fournisseurs, clients, ressources humaines et comptabilité depuis une seule plateforme.

C'est de loin le projet le plus abouti et le plus ambitieux du portfolio à ce stade : architecture multi-tenant, migrations de base de données versionnées, gestion des abonnements (plans tarifaires), et un périmètre fonctionnel couvrant plusieurs métiers de la gestion commerciale.

## 1. Aperçu fonctionnel

| Module | Fonctionnalités |
|---|---|
| **Authentification & comptes** | Connexion, inscription, gestion des profils, rôles (`Role`), permissions fines (`Permission`) |
| **Multi-tenant (SaaS)** | Isolation des données par entreprise (`Company`, `TenantContext`, `TenantFilter`), abonnements et plans tarifaires (`Subscription`, `SubscriptionPlan`, `Feature`) |
| **Super-Admin** | Back-office de pilotage des entreprises clientes, des plans et des fonctionnalités (`SuperAdminController`) |
| **Produits** | Produits, variantes, marques, catégories (`Product`, `ProductVariant`, `Brand`, `Category`) |
| **Multi-sites** | Gestion de plusieurs points de vente/entrepôts (`Site`, `SiteType`) |
| **Stock** | Niveaux de stock, mouvements, transferts inter-sites, pertes (`StockLevel`, `StockMovement`, `StockTransfer`, `StockLoss`) |
| **Inventaires** | Sessions d'inventaire, comptage par site (`InventorySession`, `InventoryLine`) |
| **Ventes** | Ventes, vente au comptant, reçus (`Sale`, `SaleItem`, `SaleType`) |
| **Achats** | Bons de commande fournisseurs (`PurchaseOrder`, `PurchaseOrderItem`, `PurchaseStatus`) |
| **Fournisseurs & Clients** | Répertoires fournisseurs et clients (`Supplier`, `Customer`, `CustomerType`) |
| **Ressources humaines** | Employés, présence, congés (`Employee`, `Attendance`, `Leave`, `LeaveType`, `LeaveStatus`) |
| **Comptabilité** | Écritures comptables, journal (`AccountingEntry`, `EntryType`, `AccountType`) |
| **Rapports** | Rapport journalier (`ReportController`, `reports/daily.html`) |

## 2. Architecture technique

- **Langage / Framework** : Java 17, Spring Boot 3.5.11
- **Persistance** : Spring Data JPA + Hibernate (mode `validate` — le schéma est piloté par les migrations, pas par l'auto-génération Hibernate)
- **Base de données** : PostgreSQL
- **Migrations** : Flyway (16 scripts versionnés dans `src/main/resources/db/migration`, de `V1__initial_schema` à `V16__fix_users_role_check`)
- **Sécurité** : Spring Security (authentification par formulaire, sessions, `@PreAuthorize` via `@EnableMethodSecurity`), mots de passe hachés en BCrypt
- **Multi-tenancy** : filtre applicatif (`TenantFilter`) + contexte de tenant (`TenantContext`) + interception des requêtes JPA (`TenantStatementInspector`) pour isoler les données par entreprise
- **Vues** : Thymeleaf (rendu côté serveur, `thymeleaf-extras-springsecurity6` pour les contrôles de droits dans les templates)
- **Autres** : Apache POI (export Excel), Spring Boot Actuator (supervision), Lombok, tests avec JUnit + H2 (base en mémoire pour les tests)

### Structure du projet

```
afristock/
├── pom.xml
├── src/main/java/com/afristock/
│   ├── controller/          # Contrôleurs Web (Thymeleaf) et REST (Auth)
│   ├── model/entity/        # Entités JPA (Product, Sale, Stock*, Company, User...)
│   ├── model/enums/         # Enumérations métier (Role, MovementType, LeaveStatus...)
│   ├── repository/          # Interfaces Spring Data JPA
│   ├── service/             # Logique métier
│   ├── security/            # Spring Security + isolation multi-tenant
│   ├── dto/                 # Objets de transfert (login, inscription)
│   ├── config/DataInitializer.java  # Données d'amorçage (super-admin, plans, features)
│   └── web/                 # Configuration Web additionnelle (navigation SPA-like)
├── src/main/resources/
│   ├── db/migration/        # Scripts Flyway (schéma versionné)
│   ├── templates/           # Vues Thymeleaf, organisées par module
│   ├── static/              # Assets (images, JS)
│   └── application.properties
└── src/test/java/com/afristock/    # Tests (dont un test dédié à l'isolation multi-tenant)
```

> ⚠️ **Point d'attention** : le dossier `afristock/bin/` contient une **copie compilée et une ancienne version des sources** (fichiers `.class` et fragments `.java`/templates plus anciens). Ce dossier n'est pas exclu par le `.gitignore` (qui ne couvre que `target/`) et alourdit inutilement le dépôt tout en risquant de semer la confusion avec le code source réel dans `src/`. **Recommandation** : ajouter `bin/` au `.gitignore` et le retirer du suivi Git (`git rm -r --cached afristock/bin`).

## 3. Prérequis

- **Java 17**
- **Maven** (le wrapper `mvnw` / `mvnw.cmd` est fourni, pas besoin d'installer Maven séparément)
- **PostgreSQL** (une instance locale ou distante)

## 4. Installation et lancement

### 1. Cloner le dépôt

```bash
git clone https://github.com/Tchokouaga25/Gestion-de-stock.git
cd Gestion-de-stock/afristock
```

### 2. Créer la base de données PostgreSQL

```sql
CREATE DATABASE afristock;
```

### 3. Configurer les variables d'environnement (recommandé)

L'application lit ces variables avec des valeurs par défaut définies dans `application.properties` :

| Variable | Défaut | Description |
|---|---|---|
| `DB_USER` | `postgres` | Utilisateur PostgreSQL |
| `DB_PASSWORD` | `1234` | Mot de passe PostgreSQL |
| `SUPER_ADMIN_EMAIL` | `admin@afristock.com` | Email du compte Super-Admin créé au premier démarrage |
| `SUPER_ADMIN_PASSWORD` | `ChangeMe123!` | Mot de passe du compte Super-Admin créé au premier démarrage |

```bash
export DB_USER=postgres
export DB_PASSWORD=votre_mot_de_passe
export SUPER_ADMIN_EMAIL=admin@votredomaine.com
export SUPER_ADMIN_PASSWORD=UnMotDePasseFort!
```

> 🔴 **Sécurité — à ne jamais négliger** : les valeurs par défaut (`postgres`/`1234`, `ChangeMe123!`) sont des identifiants de démonstration codés en dur dans `application.properties`. Il est impératif de définir ces variables d'environnement avant tout déploiement au-delà d'un poste de développement local, sous peine d'exposer un compte Super-Admin avec un mot de passe public et connu.

### 4. Lancer l'application

```bash
./mvnw spring-boot:run
```

Flyway exécutera automatiquement les migrations au démarrage, puis `DataInitializer` créera le compte Super-Admin et les données de référence (plans d'abonnement, fonctionnalités) si elles n'existent pas encore.

L'application est accessible sur `http://localhost:8080`.

### 5. Lancer les tests

```bash
./mvnw test
```

Les tests s'exécutent sur une base **H2 en mémoire** (voir `src/test/resources/application.properties`), sans impacter la base PostgreSQL de développement. Le projet inclut notamment un test dédié à la vérification de l'**isolation multi-tenant** (`TenantIsolationTest`), un point critique dans une architecture SaaS.

## 5. Points d'attention identifiés (revue technique)

| Sévérité | Constat |
|---|---|
| 🔴 | Identifiants Super-Admin et base de données par défaut codés en dur dans `application.properties` (`ChangeMe123!`, `postgres`/`1234`) — à surcharger impérativement via variables d'environnement en dehors du poste de développement. |
| 🟠 | Le dossier `afristock/bin/` (build compilé + anciennes sources) est versionné dans Git alors qu'il devrait être ignoré — source de confusion et de gonflement inutile du dépôt. |
| 🟡 | Le fichier `Gestion des stocks 2.docx` à la racine du dépôt (probablement le cahier des charges/rapport) mélange documentation et code source ; un dossier `docs/` dédié serait plus propre. |
| 🟡 | Absence de fichier `README.md` détaillé à l'origine, malgré la richesse fonctionnelle du projet — corrigé par ce document. |

## 6. Pistes d'évolution suggérées

1. Nettoyer `afristock/bin/` du suivi Git et l'ajouter au `.gitignore`.
2. Externaliser totalement les secrets (mot de passe DB, identifiants Super-Admin) via un fichier `.env` non versionné ou un gestionnaire de secrets, sans valeur par défaut sensible dans le code.
3. Ajouter une documentation d'architecture (diagramme des modules, modèle de données) dans un dossier `docs/`.
4. Étendre la couverture de tests au-delà des services critiques déjà couverts (`SaleService`, `StockTransferService`, isolation tenant).
5. Envisager une API REST documentée (OpenAPI/Swagger) si une consommation par un futur client mobile ou SPA est prévue.

## 7. Auteur

Projet réalisé par **Tchokouaga25** dans le cadre de sa formation en développement web / Java.
