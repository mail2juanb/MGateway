[🇫🇷 Lire en français](README.fr.md) | [🇬🇧 Read in English](README.md)

# mgateway – API Gateway & Sécurité de l’application MicroDiab

---
**Projet** : MicroDiab  
**Organisation** : MediLabo Solutions  
**Auteur** : michaudj  
**Date** : Janvier 2026  
**Version** : 1.0.0
---

## 📌 Rôle
Le microservice **mgateway** est la **passerelle API centrale** de l’application **MicroDiab**.  
Il constitue le **point d’entrée unique** de l’écosystème microservices et assure :

- le **routage dynamique** des requêtes HTTP vers les microservices back ;
- l’**authentification centralisée** des utilisateurs ;
- la **propagation du contexte de sécurité** vers les microservices internes ;
- le **traçage distribué** des appels inter-services.

`mgateway` est basé sur **Spring Cloud Gateway (WebFlux)** et adopte une approche **réactive de bout en bout**.
L’ensemble de la chaîne de traitement - serveur HTTP, sécurité, accès à la base utilisateurs via **R2DBC**, routage
et traçage - est entièrement **non bloquant**.  
Cette architecture garantit une **forte scalabilité**, une **excellente gestion
de la concurrence** et une **latence maîtrisée**, ce qui est essentiel pour une
API Gateway exposée à un volume élevé de requêtes.

**Fonctionnalités clés** :
- Point d’entrée unique (**API Gateway**).
- Routage dynamique via **Eureka Service Discovery**.
- Authentification et contrôle d’accès via **Spring Security**.
- Gestion des identités utilisateurs stockées en **base MySQL**.
- Accès base de données **réactif** via **R2DBC**.
- Propagation des headers de sécurité et de traçage.
- Documentation interactive via **Swagger UI** et **Javadoc**.
- Traçage distribué avec **Zipkin** et **Micrometer Tracing**.
- Containerisé avec **Docker** (compatible avec `docker-compose`).

---

## 📦 Dépendances
**Microservices Internes**

| Microservice      | Rôle                              | Port |
|-------------------|-----------------------------------|------|
| **eureka-server** | Découverte de services            | 9102 |
| **zipkin**        | Traçage distribué                 | 9411 |
| **mpatient**      | Données patients                  | 9001 |
| **mnotes**        | Notes médicales                   | 9002 |
| **mrisk**         | Analyse du risque de diabète      | 9003 |
| **clientui**      | Interface utilisateur             | 8090 |

**Bases de Données**

| Base de données | Type | Port | Volume Docker         |
|-----------------|------|------|-----------------------|
| MySQL           | SQL  | 3306 | `mmysql-mgateway-data` |

Dans le fichier `docker-compose.yml`, la base de données MySQL via R2DBC pour le microservice mgateway est configurée comme un service indépendant.
- **Image utilisée** : `mysql:8.0`
- **Nom du conteneur** : `mysql-mgateway`
- **Nom de la base de données** : `userservice`
- **Volume pour les données** : `mysql-mgateway-data`

**Tests** : Rapport JaCoCo disponible dans `target/`  
**Réseau** : microdiab-network (partagé avec les autres microservices).

**Sécurité** : La gateway applique une authentification centralisée,
mais chaque microservice reste sécurisé indépendamment afin d’éviter
toute exposition accidentelle. La sécurité repose sur Spring Security (authentification centralisée via `mgateway`). 
Le contexte de sécurité est **propagé via des headers HTTP** (X-Auth-Username, X-Auth-Roles).

---

## 📊 Monitoring et Documentation
| Outil    | URL                                                              | Description        |
|----------|------------------------------------------------------------------|--------------------|
| Actuator | [http://localhost:9010/actuator](http://localhost:9010/actuator) | Métriques et santé |
| Zipkin   | [http://localhost:9411/zipkin](http://localhost:9411/zipkin)     | Traces distribuées |
| Javadoc  | [http://localhost:9010/apidocs](http://localhost:9010/apidocs)   | Documentation Java |

---

## 🛠 Prérequis
- Java 21
- Spring Boot 3.5.8
- Spring Cloud 2025.0.0
- Spring Cloud Gateway (WebFlux)
- Maven
- MySQL (version 8+)
- Docker
- Variables d'environnement :
  - `EUREKA_SERVER_HOST=eureka-server`
  - `EUREKA_SERVER_PORT=9102`  
  - `MYSQL_GATEWAY_HOST=mysql-mgateway`
  - `MYSQL_GATEWAY_PORT=3306`
  - `MYSQL_GATEWAY_DATABASE=userservice`
  - `HOSTNAME=mgateway`

---

## 🛣️ Routage (Spring Cloud Gateway)
`mgateway` route dynamiquement les requêtes vers les services enregistrés dans **Eureka**.

| Préfixe                              | Destination                   |
|--------------------------------------|-------------------------------|
| `/clientui/**`                       | clientui                      |
| `/patients`, `/update/**`, `/add/**` | clientui                      |
| `/mpatient/**`                       | mpatient                      |
| `/mnotes/**`                         | mnotes                        |
| `/mrisk/**`                          | mrisk                         |
| `/webjars/**`                        | ressources statiques clientui |

Les routes utilisent :
- `StripPrefix`
- `PreserveHostHeader`
- Load-balancing via `lb://`

---

