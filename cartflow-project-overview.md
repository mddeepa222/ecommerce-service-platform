# CartFlow — E-Commerce Microservices Project

> Java 17 + Spring Boot + Spring Security + JWT + OAuth2 + RabbitMQ + Stripe + PostgreSQL

---

## Tech Stack

| Category | Technology |
|----------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 4.x |
| Security | Spring Security + JWT + OAuth2 (Google) |
| Database | PostgreSQL (har service ka alag DB) |
| Messaging | RabbitMQ (async communication) |
| Service Discovery | Spring Cloud Netflix Eureka |
| API Gateway | Spring Cloud Gateway |
| Config | Spring Cloud Config Server |
| Inter-service REST | OpenFeign |
| Payment | Stripe (test mode) |
| Build Tool | Maven |
| Version Control | GitHub |
| Testing | Postman |

---

## Services Architecture

```
Client (Postman)
       ↓
[API Gateway :8080]
       ↓
┌──────┬──────┬──────┬──────┐
↓      ↓      ↓      ↓      ↓
auth  user  product cart  order
8081  8082   8083   8085   8086
                      ↓      ↓
                  inventory payment
                    8084    8087
                           ↓
                   coupon  notification
                    8088      8089

[Eureka Server :8761] ← sab services register karti hain
[Config Server :8888] ← centralized configuration
```

---

## Services Detail

### Infrastructure Services

#### 1. eureka-server `:8761`
- Service discovery
- Sab microservices yahan register hoti hain
- Health check dashboard

**Dependencies:**
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

---

#### 2. api-gateway `:8080`
- Single entry point
- JWT validate karna
- Routes forward karna
- Load balancing

**Dependencies:**
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

---

#### 3. config-server `:8888`
- Centralized configuration
- Sab services ke application.properties yahan se aayenge
- GitHub backed config

**Dependencies:**
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-server</artifactId>
</dependency>
```

---

### Core Services

#### 4. auth-service `:8081`
**Database:** `auth_db`

**Tables:**
```
users          → id, name, email, password, role, provider
refresh_tokens → id, token, user_id, expiry
```

**Endpoints:**
```
POST /api/auth/register       → naya user register
POST /api/auth/login          → JWT token lo
POST /api/auth/refresh        → access token refresh karo
POST /api/auth/logout         → token invalidate karo
GET  /api/auth/oauth2/google  → Google OAuth2 login
```

**Features:**
- JWT Access Token (15 min expiry)
- Refresh Token (7 days expiry)
- Google OAuth2
- BCrypt password encoding
- Role: USER, ADMIN

**Dependencies:**
```xml
spring-boot-starter-security
spring-boot-starter-web
spring-boot-starter-data-jpa
postgresql
spring-cloud-starter-netflix-eureka-client
spring-security-oauth2-client
jjwt-api (0.11.5)
jjwt-impl (0.11.5)
jjwt-jackson (0.11.5)
lombok
```

---

#### 5. user-service `:8082`
**Database:** `user_db`

**Tables:**
```
profiles   → id, user_id, phone, avatar_url, created_at
addresses  → id, user_id, name, street, city, state, 
             pincode, country, is_default
```

**Endpoints:**
```
GET    /api/users/profile          → profile dekho
PUT    /api/users/profile          → profile update karo
GET    /api/users/addresses        → sab addresses
POST   /api/users/addresses        → naya address add
PUT    /api/users/addresses/{id}   → address update
DELETE /api/users/addresses/{id}   → address delete
PUT    /api/users/addresses/{id}/default → default set karo
```

---

#### 6. product-service `:8083`
**Database:** `product_db`

**Tables:**
```
categories → id, name, description, parent_id
products   → id, name, description, price, 
             category_id, image_url, is_active
```

**Endpoints:**
```
GET    /api/products                → sab products (pagination + sorting)
GET    /api/products/{id}           → ek product
POST   /api/products                → product create (ADMIN)
PUT    /api/products/{id}           → product update (ADMIN)
DELETE /api/products/{id}           → product delete (ADMIN)
GET    /api/products/search?q=      → search karo
GET    /api/products/category/{id}  → category wise
GET    /api/categories              → sab categories
POST   /api/categories              → category create (ADMIN)
```

**Features:**
- Pagination + Sorting
- Search by name/description
- Category filter
- ADMIN only create/update/delete

---

#### 7. inventory-service `:8084`
**Database:** `inventory_db`

**Tables:**
```
inventory   → id, product_id, quantity, reserved_quantity
stock_logs  → id, product_id, change_type, quantity, reason, created_at
```

**Endpoints:**
```
GET  /api/inventory/{productId}     → stock check
POST /api/inventory                 → stock add (ADMIN)
PUT  /api/inventory/{productId}     → stock update (ADMIN)
```

**RabbitMQ Consumer:**
```
order.placed    → stock reduce karo
order.cancelled → stock wapas karo
```

---

#### 8. cart-service `:8085`
**Database:** `cart_db`

**Tables:**
```
carts      → id, user_id, coupon_code, created_at, updated_at
cart_items → id, cart_id, product_id, quantity, price
```

**Endpoints:**
```
GET    /api/cart              → cart dekho
POST   /api/cart/items        → item add karo
PUT    /api/cart/items/{id}   → quantity update
DELETE /api/cart/items/{id}   → item remove
DELETE /api/cart              → cart clear
POST   /api/cart/coupon       → coupon apply
DELETE /api/cart/coupon       → coupon remove
```

**OpenFeign Calls:**
```
→ product-service  (price + product details lena)
→ inventory-service (stock available hai?)
→ coupon-service   (coupon valid hai?)
```

---

#### 9. order-service `:8086`
**Database:** `order_db`

**Tables:**
```
orders      → id, user_id, status, total_amount, 
              address_id, payment_id, created_at
order_items → id, order_id, product_id, quantity, price
```

**Order Status Lifecycle:**
```
PENDING → CONFIRMED → SHIPPED → DELIVERED
                              → CANCELLED
                              → REFUNDED
```

**Endpoints:**
```
POST /api/orders              → order place karo
GET  /api/orders              → order history
GET  /api/orders/{id}         → ek order details
PUT  /api/orders/{id}/cancel  → order cancel
GET  /api/orders/admin/all    → sab orders (ADMIN)
PUT  /api/orders/{id}/status  → status update (ADMIN)
```

**OpenFeign Calls:**
```
→ cart-service    (cart data lena)
→ user-service    (address lena)
→ payment-service (payment initiate)
```

**RabbitMQ Publisher:**
```
order.placed    → inventory-service, notification-service
order.confirmed → notification-service
order.shipped   → notification-service
order.delivered → notification-service
order.cancelled → inventory-service, notification-service
```

---

#### 10. payment-service `:8087`
**Database:** `payment_db`

**Tables:**
```
payments → id, order_id, user_id, amount, status,
           stripe_payment_intent_id, created_at
refunds  → id, payment_id, amount, reason, status, created_at
```

**Endpoints:**
```
POST /api/payments/initiate         → payment intent banao
POST /api/payments/confirm          → payment confirm
POST /api/payments/refund/{orderId} → refund initiate
GET  /api/payments/{orderId}        → payment status
```

**Stripe Flow:**
```
1. order-service → payment-service (initiate)
2. payment-service → Stripe API (create payment intent)
3. Client → Stripe (card details directly)
4. Stripe → payment-service webhook (confirmed)
5. payment-service → RabbitMQ (payment.confirmed)
6. order-service consume → status CONFIRMED
```

**RabbitMQ Publisher:**
```
payment.confirmed → order-service
payment.failed    → order-service, notification-service
```

---

#### 11. coupon-service `:8088`
**Database:** `coupon_db`

**Tables:**
```
coupons       → id, code, discount_type (FLAT/PERCENT),
                discount_value, min_order_amount,
                max_uses, used_count, expiry_date, is_active
coupon_usage  → id, coupon_id, user_id, order_id, used_at
```

**Endpoints:**
```
POST /api/coupons/validate    → coupon valid hai?
GET  /api/coupons             → sab coupons (ADMIN)
POST /api/coupons             → coupon create (ADMIN)
PUT  /api/coupons/{id}        → coupon update (ADMIN)
DELETE /api/coupons/{id}      → coupon delete (ADMIN)
```

---

#### 12. notification-service `:8089`
**Database:** `notification_db`

**Tables:**
```
notifications → id, user_id, type, message, 
                is_read, created_at
email_logs    → id, to_email, subject, status, created_at
```

**RabbitMQ Consumers:**
```
order.placed    → "Order placed successfully" email
order.confirmed → "Order confirmed" email
order.shipped   → "Order shipped, tracking: XXX" email
order.delivered → "Order delivered" email
order.cancelled → "Order cancelled" email
payment.failed  → "Payment failed, try again" email
```

---

## Communication Map

### REST (OpenFeign) — Synchronous
```
cart-service      → product-service    (product details + price)
cart-service      → inventory-service  (stock available check)
cart-service      → coupon-service     (coupon validate)
order-service     → cart-service       (cart data fetch)
order-service     → user-service       (delivery address)
order-service     → payment-service    (payment initiate)
api-gateway       → auth-service       (token validate)
```

### RabbitMQ — Asynchronous
```
Exchange: cartflow.exchange

order-service    →  [order.placed]     →  inventory-service
                                       →  notification-service

order-service    →  [order.confirmed]  →  notification-service
order-service    →  [order.shipped]    →  notification-service
order-service    →  [order.delivered]  →  notification-service
order-service    →  [order.cancelled]  →  inventory-service
                                       →  notification-service

payment-service  →  [payment.confirmed] → order-service
payment-service  →  [payment.failed]   →  order-service
                                        →  notification-service
```

---

## Database Schema

### auth_db
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100),
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255),
    role VARCHAR(20) DEFAULT 'USER',
    provider VARCHAR(20) DEFAULT 'LOCAL',
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(500) UNIQUE NOT NULL,
    user_id BIGINT REFERENCES users(id),
    expiry_date TIMESTAMP NOT NULL
);
```

### product_db
```sql
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    parent_id BIGINT REFERENCES categories(id)
);

CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    category_id BIGINT REFERENCES categories(id),
    image_url VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW()
);
```

### order_db
```sql
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    status VARCHAR(30) DEFAULT 'PENDING',
    total_amount DECIMAL(10,2) NOT NULL,
    address_id BIGINT NOT NULL,
    payment_id BIGINT,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id),
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL
);
```

---

## GitHub Structure

```
cartflow/
├── eureka-server/
│   ├── src/
│   └── pom.xml
├── api-gateway/
│   ├── src/
│   └── pom.xml
├── config-server/
│   ├── src/
│   └── pom.xml
├── auth-service/
│   ├── src/
│   │   └── main/java/com/cartflow/auth/
│   │       ├── config/
│   │       ├── controller/
│   │       ├── service/
│   │       ├── repository/
│   │       ├── entity/
│   │       ├── dto/
│   │       ├── jwt/
│   │       └── exception/
│   └── pom.xml
├── user-service/
├── product-service/
├── inventory-service/
├── cart-service/
├── order-service/
├── payment-service/
├── coupon-service/
├── notification-service/
├── docker-compose.yml
└── README.md
```

---

## Week-wise Implementation Plan

### Week 1 — Foundation + Auth
```
Day 1:
→ GitHub repo setup
→ eureka-server banao + run karo
→ config-server setup

Day 2-3:
→ auth-service — JWT (jo already sikha hai)
→ Register, Login endpoints
→ Eureka pe register karo

Day 4-5:
→ auth-service — Refresh Token add karo
→ Google OAuth2 setup
→ api-gateway basic routing

Day 6-7:
→ api-gateway — JWT validation filter
→ auth-service → api-gateway test karo
```

### Week 2 — User + Product + Inventory
```
Day 1-2: user-service (profile + address)
Day 3-4: product-service (CRUD + Search + Pagination)
Day 5-6: inventory-service
Day 7:   OpenFeign setup — services connect karo
```

### Week 3 — Cart + Coupon + RabbitMQ Setup
```
Day 1-2: RabbitMQ setup + configuration
Day 3-4: coupon-service
Day 5-7: cart-service (OpenFeign calls)
```

### Week 4 — Order + Payment
```
Day 1-3: order-service (lifecycle + RabbitMQ publish)
Day 4-6: payment-service (Stripe integration)
Day 7:   payment → order flow test karo
```

### Week 5 — Notification + Testing
```
Day 1-2: notification-service (RabbitMQ consume + email)
Day 3-5: End to end testing (Postman collection)
Day 6-7: Bug fixes
```

### Week 6 — Polish + Deploy (Mid July)
```
Day 1-2: README + API Documentation
Day 3-4: Postman collection export
Day 5-6: Demo video record karo
Day 7:   Resume + LinkedIn + Portfolio update
```

---

## End-to-End Order Flow

```
Step 1: Login
POST /api/auth/login
→ JWT token milta hai

Step 2: Products browse karo
GET /api/products?page=0&size=10&sort=price,asc
→ Product list milti hai

Step 3: Cart mein add karo
POST /api/cart/items
{ "productId": 1, "quantity": 2 }
→ cart-service:
   → product-service se price lo
   → inventory-service se stock check karo

Step 4: Coupon apply karo
POST /api/cart/coupon
{ "code": "SAVE10" }
→ cart-service → coupon-service (validate)

Step 5: Order place karo
POST /api/orders
{ "addressId": 1 }
→ order-service:
   → cart-service se cart data lo
   → user-service se address lo
   → payment-service ko call karo
   → RabbitMQ → order.placed publish karo
       → inventory-service: stock reduce karo
       → notification-service: email bhejo

Step 6: Payment karo
POST /api/payments/confirm
{ "paymentIntentId": "pi_xxx", "orderId": 1 }
→ Stripe confirm karta hai
→ RabbitMQ → payment.confirmed publish
→ order-service: status CONFIRMED
→ notification-service: confirmation email
```

---

## Day 1 Checklist (Kal ke liye)

```
□ GitHub pe "cartflow" repo banao
□ Spring Initializr se banao:
  □ eureka-server
  □ api-gateway
  □ auth-service
□ eureka-server run karo — dashboard check karo
  http://localhost:8761
□ auth-service eureka pe register karo
□ Basic JWT auth-service mein lagao
  (jo Spring Security mein sikha hai)
□ Register + Login endpoint test karo Postman se
```

---

## Important Concepts Jo Seekhoge Is Project Mein

```
✅ Already pata hai:
→ Spring Boot basics
→ Spring Security + JWT
→ JPA + PostgreSQL
→ REST APIs
→ DTOs + Validation

🔜 Is project mein seekhoge:
→ Microservices architecture
→ Service Discovery (Eureka)
→ API Gateway pattern
→ OpenFeign (inter-service REST)
→ RabbitMQ (async messaging)
→ OAuth2 (Google login)
→ Refresh Token
→ Stripe payment integration
→ Event-driven architecture
→ Distributed systems basics
```

---

*Project start date: Wednesday*
*Target completion: Mid July*
*Total services: 12*
*Tech stack: Java 17 + Spring Boot + PostgreSQL + RabbitMQ + Stripe*
