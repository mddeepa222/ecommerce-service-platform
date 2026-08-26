# CartFlow — E-Commerce Microservices Backend

> A production-ready e-commerce backend built with **Java 21 + Spring Boot** following **microservices architecture**. Features service discovery, centralized configuration, API gateway with JWT authentication, and async messaging via RabbitMQ.

---

## Tech Stack

| Category | Technology |
|----------|------------|
| Language | Java 21 |
| Framework | Spring Boot 4.x |
| Security | Spring Security + JWT + OAuth2 (Google) |
| Database | PostgreSQL (separate DB per service) |
| Messaging | RabbitMQ (async communication) |
| Service Discovery | Spring Cloud Netflix Eureka |
| API Gateway | Spring Cloud Gateway (MVC) |
| Config Management | Spring Cloud Config Server |
| Inter-service REST | OpenFeign |
| Payment | Stripe (test mode) |
| Build Tool | Maven |
| Testing | Postman |

---

## Services Overview

| Service | Port | Status |
|---------|------|--------|
| eureka-server | 8761 | ✅ Complete |
| config-server | 8888 | ✅ Complete |
| api-gateway | 8080 | ✅ Complete |
| auth-service | 8081 | ✅ Complete |
| user-service | 8082 | ✅ Complete |
| product-service | 8083 | ✅ Complete |
| inventory-service | 8084 | 🔧 In Progress |
| cart-service | 8085 | 🔜 Upcoming |
| order-service | 8086 | 🔜 Upcoming |
| payment-service | 8087 | 🔜 Upcoming |
| coupon-service | 8088 | 🔜 Upcoming |
| notification-service | 8089 | 🔜 Upcoming |

---

## Architecture

```
Client (Postman / Frontend)
           │
           ▼
   [API Gateway :8080]
   JWT Validation + Routing
           │
    ┌──────┼──────────┬──────────┐
    ▼      ▼          ▼          ▼
 auth   user      product   inventory
 8081   8082       8083       8084
                              
              [Coming Soon]
           cart  ──► order ──► payment
           8085      8086       8087
                       │
                  coupon  notification
                   8088      8089

[Eureka Server :8761]  ← All services register here
[Config Server :8888]  ← Centralized configuration
```

---

## Available Services (Implemented)

### 🔐 Auth Service `:8081`
Handles user registration, login, JWT token management, and Google OAuth2.

**Endpoints:**
```
POST /api/auth/register         → Register new user
POST /api/auth/login            → Login and get JWT token
POST /api/auth/refresh          → Refresh access token
POST /api/auth/logout           → Invalidate token
GET  /api/auth/oauth2/google    → Google OAuth2 login

GET    /api/auth/account/me         → Get profile
PUT    /api/auth/account/me         → Update profile
POST   /api/auth/account/me/avatar  → Upload avatar
DELETE /api/auth/account/me/avatar  → Remove avatar
DELETE /api/auth/account/me         → Delete account
```

**Features:**
- JWT Access Token (15 min expiry)
- Refresh Token (7 days expiry)
- Google OAuth2 Login
- BCrypt password encoding
- Role-based access: `USER`, `ADMIN`
- Cloudinary avatar upload

---

### 👤 User Service `:8082`
Manages user profiles and delivery addresses.

**Endpoints:**
```
GET  /api/users/profile               → Get profile
PUT  /api/users/profile               → Update profile

GET    /api/users/addresses           → List all addresses
POST   /api/users/addresses           → Add new address
PUT    /api/users/addresses/{id}      → Update address
DELETE /api/users/addresses/{id}      → Delete address
PUT    /api/users/addresses/{id}/default → Set as default
```

---

### 📦 Product Service `:8083`
Manages product catalog with categories, search, and pagination.

**Endpoints:**
```
GET    /api/products                  → All products (pagination + sorting)
GET    /api/products/{id}             → Single product
GET    /api/products/search?q=        → Search by name/description
GET    /api/products/category/{id}    → Filter by category
POST   /api/products                  → Create product (ADMIN)
PUT    /api/products/{id}             → Update product (ADMIN)
DELETE /api/products/{id}             → Delete product (ADMIN)

GET    /api/categories                → All categories
GET    /api/categories/{id}           → Single category
POST   /api/categories                → Create category (ADMIN)
PUT    /api/categories/{id}           → Update category (ADMIN)
DELETE /api/categories/{id}           → Delete category (ADMIN)
```

**Features:**
- Server-side pagination + sorting
- Full-text search by name/description
- Category-based filtering
- ADMIN-only create/update/delete

---

### 🗃️ Inventory Service `:8084` — 🔧 In Progress

Tracks product stock levels and handles reservation logic.

**Planned Endpoints:**
```
GET  /api/inventory/{productId}   → Check stock
POST /api/inventory               → Add stock (ADMIN)
PUT  /api/inventory/{productId}   → Update stock (ADMIN)
```

**Planned RabbitMQ Consumers:**
```
order.placed    → Reduce stock
order.cancelled → Restore stock
```

---

## Upcoming Services

### 🛒 Cart Service `:8085`
Cart management with coupon support. Will use OpenFeign to call product-service, inventory-service, and coupon-service.

### 📋 Order Service `:8086`
Full order lifecycle — `PENDING → CONFIRMED → SHIPPED → DELIVERED`. Will publish events to RabbitMQ for inventory and notification updates.

### 💳 Payment Service `:8087`
Stripe payment integration with webhook support for async payment confirmation.

### 🏷️ Coupon Service `:8088`
Discount coupon management with FLAT and PERCENT discount types.

### 🔔 Notification Service `:8089`
Email notifications via RabbitMQ consumers for all order and payment events.

---

## Communication Pattern

### Synchronous (OpenFeign REST)
```
cart-service    → product-service     (product details + price)
cart-service    → inventory-service   (stock availability)
cart-service    → coupon-service      (coupon validation)
order-service   → cart-service        (fetch cart data)
order-service   → user-service        (delivery address)
order-service   → payment-service     (initiate payment)
api-gateway     → auth-service        (JWT validation)
```

### Asynchronous (RabbitMQ)
```
Exchange: cartflow.exchange

order-service   → [order.placed]      → inventory-service
                                      → notification-service
order-service   → [order.cancelled]   → inventory-service
                                      → notification-service
payment-service → [payment.confirmed] → order-service
payment-service → [payment.failed]    → order-service
                                      → notification-service
```

---

## How to Run

### Prerequisites
- Java 21
- Maven
- PostgreSQL
- RabbitMQ

### Startup Order
```bash
# 1. Start Eureka Server
cd eureka-server && mvn spring-boot:run

# 2. Start Config Server
cd config-server && mvn spring-boot:run

# 3. Start API Gateway
cd api-gateway && mvn spring-boot:run

# 4. Start core services (any order)
cd auth-service && mvn spring-boot:run
cd user-service && mvn spring-boot:run
cd product-service && mvn spring-boot:run
```

### Verify Services
- Eureka Dashboard: `http://localhost:8761`
- API Gateway: `http://localhost:8080`

---

## API Testing

Postman collection is included in the repository root:
```
CartFlow.postman_collection.json
```

Import it in Postman and test all available endpoints.

---

## Project Structure

```
cartflow-microservices/
├── eureka-server/
├── config-server/
├── api-gateway/
├── auth-service/
├── user-service/
├── product-service/
├── inventory-service/          ← In Progress
├── CartFlow.postman_collection.json
└── README.md
```

---

## Author

**Deepa Prajapat** — Java Full Stack Developer  
[GitHub](https://github.com/mddeepa222) • [LinkedIn](https://www.linkedin.com/in/deepa-prajapat-b507a2430)