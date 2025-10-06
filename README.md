# API Gateway Service

Spring Cloud Gateway-based API gateway with JWT authentication, service routing, and circuit breaker patterns.

## 🚀 Features
- **JWT Authentication** - Validates Bearer tokens using RSA public key
- **Service Routing** - Routes to Auth, User, and Order services
- **Circuit Breakers** - Resilience4j integration for fault tolerance
- **OpenAPI Documentation** - Swagger UI with service API aggregation
- **Reactive Architecture** - Built with WebFlux and reactive streams

## 📡 Services Routing
| Service         | Path Patterns | Destination             |
|-----------------|---------------|-------------------------|
| auth-service    | `/auth/**` | Authentication Service  |
| user-service    | `/api/users/**`, `/api/cards/**` | User Management Service |
| order-service   | `/api/items/**`, `/api/orders/**`, `/api/order-items/**` | Order Service           |
| payment-service | `/api/payments/**` | Payment Service         |

## 🔒 Security
- JWT validation with RSA public key (`keys/public.pem`)
- Bypass auth for `/login`, `/register`, and Swagger endpoints
- Internal headers: `X-Internal-Call`, `X-Source-Service`

## 📋 API Endpoints
- `POST /register` - User registration
- `GET /fallback/*` - Circuit breaker fallbacks
- `/swagger-ui.html` - API documentation
- `/actuator/health` - Health monitoring

## 🏗️ Build & Run
mvn clean package
java -jar target/gateway-0.0.1-SNAPSHOT.jar

## 📦 Dependencies
- Spring Cloud Gateway
- Spring Security OAuth2 Resource Server
- Resilience4j Circuit Breaker
- SpringDoc OpenAPI
- Lombok

## 🩺 Health & Monitoring
bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/gateway/routes

