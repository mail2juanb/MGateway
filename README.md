[🇫🇷 Read in French](README.fr.md) | [🇬🇧 Read in English](README.md)

# mgateway – API Gateway & Security for the MicroDiab Application

---
**Project**: MicroDiab  
**Organization**: MediLabo Solutions  
**Author**: michaudj  
**Date**: January 2026  
**Version**: 1.0.0  
---

## 📌 Role
The **mgateway** microservice is the **central API Gateway** for the **MicroDiab** application.
It serves as the **single entry point** for the microservices ecosystem and ensures:

- **Dynamic routing** of HTTP requests to backend microservices;
- **Centralized user authentication**;
- **Propagation of the security context** to internal microservices;
- **Distributed tracing** of inter-service calls.

`mgateway` is based on **Spring Cloud Gateway (WebFlux)** and adopts an **end-to-end reactive approach**.
The entire processing chain—HTTP server, security, user database access via **R2DBC**, routing,
and tracing—is fully **non-blocking**.
This architecture ensures **high scalability**, **excellent concurrency management**, and **controlled latency**,
which is essential for an API Gateway exposed to a high volume of requests.

**Key Features**:
- Single entry point (**API Gateway**);
- Dynamic routing via **Eureka Service Discovery**;
- Authentication and access control via **Spring Security**;
- User identity management stored in a **MySQL database**;
- Reactive database access via **R2DBC**;
- Propagation of security and tracing headers;
- Interactive documentation via **Swagger UI** and **Javadoc**;
- Distributed tracing with **Zipkin** and **Micrometer Tracing**;
- Containerized with **Docker** (compatible with `docker-compose`).

---

## 📦 Dependencies
**Internal Microservices**

| Microservice      | Role                              | Port |
|-------------------|-----------------------------------|------|
| **eureka-server** | Service discovery                 | 9102 |
| **zipkin**        | Distributed tracing               | 9411 |
| **mpatient**      | Patient data                      | 9001 |
| **mnotes**        | Medical notes                     | 9002 |
| **mrisk**         | Diabetes risk analysis            | 9003 |
| **clientui**      | User interface                    | 8090 |

**Databases**

| Database | Type | Port | Docker Volume          |
|----------|------|------|------------------------|
| MySQL    | SQL  | 3306 | `mmysql-mgateway-data` |

In the `docker-compose.yml` file, the MySQL database via R2DBC for the mgateway microservice is configured as an independent service.
- **Image used**: `mysql:8.0`
- **Container name**: `mysql-mgateway`
- **Database name**: `userservice`
- **Data volume**: `mysql-mgateway-data`

**Tests**: JaCoCo report available in `target/`
**Network**: microdiab-network (shared with other microservices).

**Security**: The gateway applies centralized authentication, but each microservice remains independently secured to prevent accidental exposure. Security is based on Spring Security (centralized authentication via `mgateway`). The security context is **propagated via HTTP headers** (X-Auth-Username, X-Auth-Roles).

---

## 📊 Monitoring and Documentation
| Tool     | URL                                                              | Description        |
|----------|------------------------------------------------------------------|--------------------|
| Actuator | [http://localhost:9010/actuator](http://localhost:9010/actuator) | Metrics and health |
| Zipkin   | [http://localhost:9411/zipkin](http://localhost:9411/zipkin)     | Distributed traces |
| Javadoc  | [http://localhost:9010/apidocs](http://localhost:9010/apidocs)   | Java documentation |

---

## 🛠 Prerequisites
- Java 21
- Spring Boot 3.5.8
- Spring Cloud 2025.0.0
- Spring Cloud Gateway (WebFlux)
- Maven
- MySQL (version 8+)
- Docker
- Environment variables:
    - `EUREKA_SERVER_HOST=eureka-server`
    - `EUREKA_SERVER_PORT=9102`
    - `MYSQL_GATEWAY_HOST=mysql-mgateway`
    - `MYSQL_GATEWAY_PORT=3306`
    - `MYSQL_GATEWAY_DATABASE=userservice`
    - `HOSTNAME=mgateway`

---

## 🛣️ Routing (Spring Cloud Gateway)
`mgateway` dynamically routes requests to services registered in **Eureka**.

| Prefix                               | Destination               |
|--------------------------------------|---------------------------|
| `/clientui/**`                       | clientui                  |
| `/patients`, `/update/**`, `/add/**` | clientui                  |
| `/mpatient/**`                       | mpatient                  |
| `/mnotes/**`                         | mnotes                    |
| `/mrisk/**`                          | mrisk                     |
| `/webjars/**`                        | clientui static resources |

Routes use :
- `StripPrefix`
- `PreserveHostHeader`
- Load-balancing via `lb://`

---
