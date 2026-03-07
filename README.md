# 💳 Mini Payment Gateway

> A production-inspired payment gateway built with **Java 17**, **Spring Boot**, **Redis**, and **Docker** — demonstrating idempotency, distributed caching, and scalable microservice architecture.

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen?style=flat-square&logo=spring-boot)
![Redis](https://img.shields.io/badge/Redis-Cache-red?style=flat-square&logo=redis)
![Docker](https://img.shields.io/badge/Docker-Containerized-blue?style=flat-square&logo=docker)
![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)

---

## 📖 Overview

This project simulates how modern payment platforms like Razorpay and Stripe process payments safely at scale. It focuses on three critical backend engineering concepts:

- **Idempotency** — preventing duplicate charges on retried requests
- **Distributed Caching** — Redis-backed state management for fast lookups
- **Retry-Safe Architecture** — ensuring no double-billing under network failures

---

## ✨ Features

| Feature | Description |
|---|---|
| 💰 Payment Processing API | RESTful endpoint to initiate payments |
| 🔑 Idempotency Handling | Duplicate request detection via idempotency keys |
| ⚡ Redis Caching | Distributed cache for payment state |
| 🔁 Retry-Safe Transactions | Safe to retry without double charges |
| 🐳 Docker Support | Fully containerized for easy deployment |
| 📦 Scalable Architecture | Microservice-style, horizontally scalable |

---

## 🏗️ System Architecture

```mermaid
graph TB
    Client(["🖥️ Client\n(Postman / App)"])
    API["⚙️ Payment API\nSpring Boot :8080"]
    Idem["🔑 Idempotency Layer\nKey Validation"]
    Redis[("🗄️ Redis Cache\n:6379")]
    Logic["💳 Payment Processing\nBusiness Logic"]
    Response(["✅ Response\nto Client"])

    Client -->|"POST /payments\n+ Idempotency-Key header"| API
    API --> Idem
    Idem -->|"Check key exists?"| Redis
    Redis -->|"HIT → return cached result"| Response
    Idem -->|"MISS → process payment"| Logic
    Logic -->|"Store result"| Redis
    Logic --> Response
```

---

## 🔄 Payment Flow — Sequence Diagram

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant API as Payment API
    participant IL as Idempotency Layer
    participant R as Redis Cache
    participant PL as Payment Processor

    C->>API: POST /payments (Idempotency-Key: xyz-123)
    API->>IL: Validate idempotency key
    IL->>R: GET idempotency:xyz-123

    alt Key EXISTS in Redis (Duplicate Request)
        R-->>IL: Cached payment result
        IL-->>API: Return cached response
        API-->>C: 200 OK (same result, no charge)
    else Key NOT EXISTS (New Request)
        R-->>IL: null
        IL->>PL: Process new payment
        PL->>PL: Execute payment logic
        PL-->>IL: Payment result
        IL->>R: SET idempotency:xyz-123 (TTL: 24h)
        IL-->>API: New payment result
        API-->>C: 200 OK (paymentId + status)
    end
```

---

## 🛠️ Tech Stack

### Backend
- **Java 17** — Core language
- **Spring Boot** — REST API framework
- **Gradle** — Build tool

### Infrastructure
- **Redis** — Distributed cache & idempotency store
- **Docker** — Containerization
- **Docker Compose** — Multi-container orchestration

### Developer Tools
- **Postman** — API testing
- **GitHub** — Version control

---

## 📦 Project Structure

```
mini-payment-gateway/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/gateway/
│       │       ├── controller/       # REST controllers
│       │       ├── service/          # Business logic
│       │       ├── model/            # Request/Response models
│       │       └── cache/            # Redis idempotency logic
│       └── resources/
│           └── application.yml       # App configuration
├── Dockerfile
├── docker-compose.yml
├── build.gradle
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Docker
- Gradle

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/mini-payment-gateway.git
cd mini-payment-gateway
```

### 2. Start Redis

```bash
docker run -d -p 6379:6379 --name redis redis
```

### 3. Run the Application

```bash
./gradlew bootRun
```

Or build and run the JAR:

```bash
./gradlew build
java -jar build/libs/*.jar
```

The server starts at **http://localhost:8080**

---

## 🐳 Docker Deployment

### Build & Run with Docker

```bash
# Build image
docker build -t mini-payment-gateway .

# Run container
docker run -p 8080:8080 mini-payment-gateway
```

### Run with Docker Compose (Recommended)

```bash
docker-compose up --build
```

---

## 📡 API Reference

### POST `/payments` — Create Payment

**Headers**

```
Content-Type: application/json
Idempotency-Key: <unique-uuid>
```

**Request Body**

```json
{
  "amount": 1000,
  "currency": "INR",
  "userId": "12345"
}
```

**Response — 200 OK**

```json
{
  "paymentId": "pay_123456",
  "status": "SUCCESS"
}
```

**cURL Example**

```bash
curl -X POST http://localhost:8080/payments \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: test-key-001" \
  -d '{"amount": 1000, "currency": "INR", "userId": "12345"}'
```

---

## 🔑 Key Concepts

### Idempotency
When a client retries a request (due to network timeout), the same `Idempotency-Key` is sent. The server checks Redis — if the key already exists, it returns the **cached result without re-processing**, preventing double charges.

### Redis Caching
All idempotency keys and their results are stored in Redis with a TTL (e.g., 24 hours). This provides O(1) lookup performance and works across multiple service instances.

### Retry-Safe Design
```
First Request  → Process → Store in Redis → Return result
Retry Request  → Check Redis (HIT) → Return same result (no re-processing)
```

---

## 🗺️ Idempotency State Machine

```mermaid
stateDiagram-v2
    [*] --> NewRequest : Client sends payment

    NewRequest --> CheckCache : Validate Idempotency-Key
    CheckCache --> CacheHit : Key found in Redis
    CheckCache --> CacheMiss : Key not in Redis

    CacheHit --> ReturnCached : Return existing result
    ReturnCached --> [*] : 200 OK (no charge)

    CacheMiss --> Processing : Execute payment
    Processing --> Success : Payment approved
    Processing --> Failed : Payment rejected

    Success --> StoreCache : Save to Redis
    Failed --> StoreCache : Save to Redis
    StoreCache --> ReturnNew : Return fresh result
    ReturnNew --> [*] : 200 OK
```

---

## 🔮 Future Improvements

- [ ] Database persistence (PostgreSQL)
- [ ] Webhook notifications for payment events
- [ ] Payment status tracking endpoint (`GET /payments/{id}`)
- [ ] Message queue integration (Kafka / RabbitMQ)
- [ ] Authentication & Authorization (JWT)
- [ ] Rate limiting per user/IP
- [ ] Payment analytics dashboard

---

## 🎓 Learning Goals

This project is a practical demonstration of:

- ✅ Backend system design for financial systems
- ✅ Idempotent API design patterns
- ✅ Distributed caching with Redis
- ✅ Payment processing flow fundamentals
- ✅ Docker-based deployment
- ✅ Microservice architecture concepts

---

## 👨‍💻 Author

**Harsh Thaker** — Backend Developer

[![GitHub](https://img.shields.io/badge/GitHub-@harshthaker-black?style=flat-square&logo=github)](https://github.com/your-username)

---

> ⭐ If this project helped you understand payment systems, consider giving it a star!
