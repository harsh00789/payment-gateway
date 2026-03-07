<div align="center">

# ⚡ PayNova

### A Production-Grade Payment Gateway System

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)](https://redis.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![React](https://img.shields.io/badge/React-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://react.dev/)

**A simplified payment gateway system that demonstrates how modern payment platforms process payments securely using idempotency, distributed caching, fraud detection, and event-driven architecture.**

[Getting Started](#-getting-started) · [API Reference](#-api-endpoints) · [Architecture](#-architecture-overview) · [Dashboard](#-frontend-dashboard)

</div>

---

## 📖 About

PayNova simulates real-world payment gateway concepts — safe payment retries, idempotency handling, fraud detection, HMAC-signed webhooks, and a full merchant dashboard UI.

Built with **Java / Spring Boot** on the backend and a **React + Vite** dashboard on the frontend.

---

## ✨ Features

| Category | Feature |
|----------|---------|
| **Payments** | Order creation, payment processing, status tracking |
| **Idempotency** | Redis-backed idempotency keys prevent duplicate charges |
| **Fraud Detection** | Rate-limiting (>5 attempts) + high-amount threshold (>₹1,00,000) |
| **Webhooks** | Async delivery with HMAC-SHA256 signatures, scheduled retries (max 5) |
| **Event System** | Publisher/Consumer pattern for payment lifecycle events |
| **Dashboard** | Full React merchant dashboard — orders, payments, webhooks, fraud logs |
| **Deployment** | Dockerized backend, Render-ready config |

---

## 🏗 Architecture Overview

```mermaid
graph TB
    subgraph Frontend["Frontend — React + Vite"]
        UI["Dashboard UI"]
    end

    subgraph Backend["Backend — Spring Boot"]
        OC["OrderController"]
        PC["PaymentController"]
        WC["WebhookController"]
        FC["FraudController"]

        OS["OrderService"]
        PS["PaymentService"]
        IS["IdempotencyService"]
        FDS["FraudDetectionService"]
        WS["WebhookService"]
        PEP["PaymentEventPublisher"]
        PEC["PaymentEventConsumer"]
    end

    subgraph Data["Data Layer"]
        PG[("PostgreSQL")]
        RD[("Redis")]
    end

    UI -->|REST API| OC & PC & WC & FC

    OC --> OS
    PC --> PS
    WC --> WS
    FC --> FDS

    PS --> IS
    PS --> FDS
    PS --> PEP
    PEP -->|Event| PEC
    PEC --> WS

    OS & PS --> PG
    IS & FDS --> RD
    WS --> PG
```

---

## 🔄 Payment Processing Flow

```mermaid
sequenceDiagram
    participant C as Client/Dashboard
    participant API as Payment API
    participant IDS as Idempotency Service
    participant R as Redis
    participant FD as Fraud Detection
    participant DB as PostgreSQL
    participant WH as Webhook Service

    C->>API: POST /process-payment/{orderId}
    Note over C,API: Header: idempotencyKey

    API->>IDS: Check idempotency key
    IDS->>R: GET payment:idempotency:{key}

    alt Key exists (duplicate request)
        R-->>IDS: Return existing paymentId
        IDS-->>API: Return cached transaction
        API-->>C: 200 OK (same result)
    else Key is new
        R-->>IDS: null

        API->>FD: isFraud(orderId, amount)
        FD->>R: Check attempt count

        alt Fraud detected
            FD->>DB: Save FraudLog
            FD-->>API: true
            API-->>C: 400 Fraudulent transaction
        else Clean transaction
            FD-->>API: false
            API->>DB: Update order → PROCESSING
            API->>DB: Save PaymentTransaction
            API->>IDS: Cache idempotency key
            IDS->>R: SET payment:idempotency:{key}
            API->>WH: Publish payment event
            WH->>DB: Save WebhookEvent
            WH-->>C: Async POST to webhook URL
            API-->>C: 200 OK (transaction result)
        end
    end
```

---

## 🛡 Fraud Detection Logic

```mermaid
flowchart TD
    A["Incoming Payment"] --> B{"Attempt count > 5?"}
    B -->|Yes| C["🚫 BLOCKED — Multiple attempts"]
    B -->|No| D{"Amount > ₹1,00,000?"}
    D -->|Yes| E["🚫 BLOCKED — High amount"]
    D -->|No| F["✅ ALLOWED — Process payment"]

    C --> G["Save to FraudLog"]
    E --> G

    style C fill:#dc2626,color:#fff
    style E fill:#dc2626,color:#fff
    style F fill:#22c55e,color:#fff
```

---

## 🛠 Tech Stack

### Backend
| Technology | Purpose |
|-----------|---------|
| Java 17 | Core language |
| Spring Boot 4.0 | Web framework |
| Spring Data JPA | ORM / data access |
| Spring Data Redis | Distributed caching |
| PostgreSQL | Primary database |
| Redis | Idempotency keys, fraud rate-limiting |
| Gradle | Build tool |
| Lombok | Boilerplate reduction |
| Docker | Containerization |

### Frontend
| Technology | Purpose |
|-----------|---------|
| React 19 | UI library |
| Vite | Dev server & bundler |
| Chart.js | Dashboard charts |
| Vanilla CSS | Custom styling |

---

## 📡 API Endpoints

### Orders

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/orders` | Create a new order |
| `GET` | `/orders` | List all orders |
| `GET` | `/orders/{id}` | Get order by ID |

### Payments

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/process-payment/{orderId}` | Process payment for an order |
| `GET` | `/payments` | List all payments |
| `GET` | `/payments/{id}` | Get payment by ID |
| `GET` | `/payments/stats` | Get dashboard statistics |

### Webhooks

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/webhook/all` | List all webhook events |
| `POST` | `/webhook/verify` | Verify webhook signature |

### Fraud Detection

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/frauds` | List all fraud logs |

### Example — Create Order

```bash
curl -X POST http://localhost:8081/orders \
  -H "Content-Type: application/json" \
  -H "idempotencyKey: $(uuidgen)" \
  -d '{"amount": 5000, "currency": "INR"}'
```

### Example — Process Payment

```bash
curl -X POST http://localhost:8081/process-payment/1 \
  -H "Content-Type: application/json" \
  -H "idempotencyKey: $(uuidgen)"
```

---

## 🎨 Frontend Dashboard

The PayNova dashboard provides a full merchant interface for monitoring and managing payments.

| Page | Features |
|------|----------|
| **Dashboard** | Stat cards, bar chart, doughnut chart |
| **Orders** | Create orders, view status, trigger payments |
| **Payments** | Filter by status (Success / Failed / Processing) |
| **Webhooks** | Delivery log with retry count tracking |
| **Fraud Logs** | View flagged/blocked transactions with reasons |
| **Settings** | API credentials, webhook config, backend connection status |

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Redis (running on localhost:6379)
- PostgreSQL
- Node.js 18+ (for frontend)

### 1️⃣ Clone the Repositories

```bash
# Backend
git clone https://github.com/harsh00789/payment-gateway.git

# Frontend (Dashboard UI)
git clone https://github.com/harsh00789/payment-gateway-ui.git
```

### 2️⃣ Start Redis

```bash
docker run -d -p 6379:6379 redis
```

### 3️⃣ Run the Backend

```bash
cd payment-gateway/payment/payment
./gradlew bootRun
```

The API will be available at `http://localhost:8081`

### 4️⃣ Run the Frontend

```bash
cd payment-gateway-ui
npm install
npm run dev
```

The dashboard will be available at `http://localhost:5173`

### 🐳 Running with Docker

```bash
# Build the image
docker build -t paynova .

# Run the container
docker run -p 8080:8080 paynova
```

---

## 🔑 Key Concepts

### Idempotency
Every payment request requires an `idempotencyKey` header. If the same key is sent twice, the server returns the original response without reprocessing — preventing duplicate charges.

### Fraud Detection
Two-layer fraud check before processing:
1. **Rate limiting** — If an order has >5 payment attempts, it's flagged
2. **Amount threshold** — Transactions over ₹1,00,000 are blocked

Both rules log to the `FraudLog` table for audit.

### Webhook Delivery
After payment processing, PayNova sends webhook events to the registered URL:
- Events are signed with **HMAC-SHA256** for authenticity
- Failed deliveries are automatically **retried every 10 seconds** (up to 5 attempts)
- Recipients verify via the `X-Signature` header

### Event-Driven Architecture
Payment lifecycle uses a publisher/consumer pattern:
- `PaymentEventPublisher` fires events after transaction completion
- `PaymentEventConsumer` handles downstream work (webhook creation, notifications)

---

## 📈 Future Improvements

- [ ] Add JWT-based authentication and authorization
- [ ] Implement rate limiting at API gateway level
- [ ] Add message queue (RabbitMQ/Kafka) for async processing
- [ ] Enhance fraud detection with ML-based risk scoring
- [ ] Add payment status tracking with real-time updates
- [ ] Implement database migration with Flyway
- [ ] Add comprehensive test coverage

---

## 🎯 Learning Goals

This project demonstrates:
- Backend system design for payment platforms
- Idempotent API design patterns
- Distributed caching with Redis
- Event-driven architecture
- Fraud detection systems
- Webhook delivery with retry mechanisms
- HMAC signature verification
- Full-stack development with React + Spring Boot
- Docker containerization

---

## 👤 Author

**Harsh Thaker**
Software Engineer

[![GitHub](https://img.shields.io/badge/GitHub-harsh00789-181717?style=for-the-badge&logo=github)](https://github.com/harsh00789)

---

<div align="center">

**[Backend Repo](https://github.com/harsh00789/payment-gateway)** · **[Frontend Repo](https://github.com/harsh00789/payment-gateway-ui)**

Built with ☕ and ⚡ by [Harsh Thaker](https://github.com/harsh00789)

</div>
