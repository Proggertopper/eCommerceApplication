# eCommerce Microservices

Spring Cloud based eCommerce backend built as a set of independent services. The project is focused on practicing real microservice infrastructure: centralized configuration, service discovery, API gateway routing, authentication, async events, databases per service, observability, Docker Compose, and CI.

## Tech Stack

- Java 21
- Spring Boot 3
- Spring Cloud Config
- Spring Cloud Gateway WebFlux
- Netflix Eureka
- Spring Security OAuth2 Resource Server
- Keycloak
- PostgreSQL, MySQL, MongoDB
- Kafka and Spring Cloud Stream
- RabbitMQ / Spring Cloud Bus
- Redis rate limiting
- Resilience4j circuit breaker
- Micrometer, Prometheus, Zipkin, Grafana/Loki configs
- Docker Compose
- Kubernetes manifests
- Maven multi-module build
- GitHub Actions CI

## Architecture

```text
Client
  |
  v
API Gateway :8080
  |-- /api/products/** -> product-service :8081 -> PostgreSQL
  |-- /api/users/**    -> user-service    :8082 -> MongoDB + Keycloak Admin API
  |-- /api/orders/**   -> order-service   :8083 -> MySQL
  |-- /api/cart/**     -> order-service   :8083 -> MySQL
  |
  |-- Swagger UI aggregation
  |-- OAuth2/JWT validation
  |-- Redis rate limiting
  |-- Resilience4j circuit breaker

Config Server :8888 -> centralized service configuration
Eureka        :8761 -> service discovery
Kafka               -> order created events
notification-service :8084 -> consumes order events
Zipkin        :9411 -> distributed tracing
Prometheus/Grafana  -> metrics experiments under additional/
```

## Services

| Service | Port | Purpose |
| --- | ---: | --- |
| configserver | 8888 | Centralized native Spring Cloud Config server |
| eureka | 8761 | Service discovery registry |
| gateway | 8080 | API Gateway, OAuth2 resource server, routing, rate limiting, Swagger aggregation |
| product-service | 8081 | Product catalog backed by PostgreSQL |
| user-service | 8082 | User API backed by MongoDB and integrated with Keycloak |
| order-service | 8083 | Cart and order API backed by MySQL, publishes order events to Kafka |
| notification | 8084 | Kafka consumer for order events |

## Main API Routes

Gateway routes:

- `GET /api/products`
- `GET /api/products/{id}`
- `GET /api/products/search?keyword=...`
- `POST /api/products`
- `PUT /api/products/{id}`
- `DELETE /api/products/{id}`
- `GET /api/users`
- `GET /api/users/{id}`
- `POST /api/users`
- `PUT /api/users/{id}`
- `GET /api/cart`
- `POST /api/cart`
- `DELETE /api/cart/items/{productId}`
- `POST /api/orders`

The order and cart endpoints expect the user id in the `X-User-ID` header.

## Swagger / OpenAPI

OpenAPI documentation is generated at runtime by `springdoc-openapi`.

Each REST service exposes its own OpenAPI JSON:

- product-service: `http://localhost:8081/v3/api-docs`
- user-service: `http://localhost:8082/v3/api-docs`
- order-service: `http://localhost:8083/v3/api-docs`

The gateway exposes one aggregated Swagger UI:

- `http://localhost:8080/swagger-ui/index.html`

Gateway aggregation works through these routes:

- `/aggregate/product-service/v3/api-docs` -> `product-service /v3/api-docs`
- `/aggregate/user-service/v3/api-docs` -> `user-service /v3/api-docs`
- `/aggregate/order-service/v3/api-docs` -> `order-service /v3/api-docs`

The list of Swagger UI entries is configured in `configserver/src/main/resources/config/gateway-service.yml` under `springdoc.swagger-ui.urls`.

## Local Docker Start

Create a local env file from the example:

```bash
cp deploy/docker/.env.example deploy/docker/.env
```

Build all service jars:

```bash
mvn clean package
```

Start infrastructure and services:

```bash
cd deploy/docker
docker compose up -d --build
```

Useful local URLs:

- Gateway: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Eureka UI: `http://localhost:8761`
- Keycloak: `http://localhost:8181`
- RabbitMQ Management UI: `http://localhost:15672`
- pgAdmin: `http://localhost:5050`
- Zipkin: `http://localhost:9411`

## Maven Build

The root `pom.xml` is a Maven aggregator for all services:

```bash
mvn clean verify
```

Run tests only:

```bash
mvn test
```

## CI

GitHub Actions workflow is located at `.github/workflows/ci.yml`.

It runs on pushes and pull requests, installs Java 21, caches Maven dependencies, and executes:

```bash
mvn -B clean verify
```

This gives the repository a basic quality gate: every commit must compile all modules and pass all tests.

## Kubernetes

Local Kubernetes manifests are located in `deploy/k8s`.

They include:

- namespace
- demo secrets and shared config map
- infrastructure deployments and services
- application deployments and services
- readiness/liveness probes for core services

Quick start:

```bash
mvn clean package
kubectl apply -f deploy/k8s/namespace.yaml
kubectl apply -f deploy/k8s/secrets.yaml
kubectl apply -f deploy/k8s/configmap.yaml
kubectl apply -f deploy/k8s/infra.yaml
kubectl apply -f deploy/k8s/apps.yaml
kubectl get pods -n ecommerce
```

More detailed Minikube/Kind instructions are available in `deploy/k8s/README.md`.

## Project Status

Already implemented:

- Multi-module Maven structure
- Service discovery with Eureka
- Centralized config with Spring Cloud Config
- Gateway routing
- Keycloak-based JWT resource server setup
- Database per service
- Kafka order event publishing/consuming
- Redis-based gateway rate limiting
- Resilience4j circuit breaker for product route
- Actuator, Prometheus metrics, Zipkin tracing
- Docker Compose environment
- Kubernetes manifests for local deployment
- Unit tests for core services
- GitHub Actions CI

Planned improvements:

- More detailed Swagger annotations and JWT auth scheme in OpenAPI
- Testcontainers integration tests
- Database migrations with Flyway or Liquibase
- Docker image publishing pipeline
