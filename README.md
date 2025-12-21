# Java Spring Cloud – Event Management Microservices

This project is a **Java Spring Cloud microservices application** for creating, organizing, and managing events.  
Users can create events, request participation, leave comments, and view event statistics such as the number of times an event was shown.

The system follows a **distributed microservices architecture** with centralized configuration, service discovery, API gateway routing, and inter-service communication via Feign clients.

---

## 🧩 Architecture Overview

The application is composed of multiple independent services:

- **API Gateway** – single entry point for all client requests
- **Service Discovery** – dynamic registration and lookup of services
- **Configuration Service** – centralized external configuration
- **Business Microservices** – user, event, request, comment management
- **Infrastructure Modules** – shared logging and interaction APIs
- **Databases** – each service has its own database running in Docker

All external requests go through the **gateway-service**, and all services communicate internally using **Feign Clients**.

---

## 🔧 Microservices

### 1. User Service (`user-service`)
Manages application users.

**Responsibilities:**
- User registration and management
- User data retrieval
- User validation for other services

---

### 2. Event Service (`event-service`)
Handles event creation and management.

**Responsibilities:**
- Create, update, and retrieve events
- Display event details
- Integrate with statistics service
- Validate event ownership and access

#### 📊 Stats Service (inside Event Service)
A dedicated internal service responsible for:
- Counting how many times an event page was viewed
- Storing and updating event view statistics
- Displaying view count in the event description

---

### 3. Request Service (`request-service`)
Manages participation requests for events.

**Responsibilities:**
- Create participation requests
- Approve or reject requests
- Track request status per event and user

---

### 4. Comment Service (`comment-service`)
Handles comments related to events.

**Responsibilities:**
- Add comments to events
- Retrieve comments by event
- Manage comment lifecycle

---

## ⚙️ Infrastructure Services

### Discovery Service (`discovery-service`)
- Based on **Spring Cloud Discovery (Eureka)**
- Registers all microservices
- Enables service-to-service communication without hardcoded URLs

---

### Config Service (`config-service`)
- Centralized configuration management
- Externalizes application properties
- Supports environment-specific configurations

---

### Gateway Service (`gateway-service`)
- Single entry point for all HTTP requests
- Routes requests to appropriate microservices
- Simplifies security, logging, and request validation

---

## 🔄 Inter-Service Communication

- **Feign Clients** are used for synchronous communication
- Services call each other using service names registered in Discovery
- Shared DTOs and contracts are provided via the `interaction-api` module

## 🛡️ Fault Tolerance & Resilience

- **Resilience4j Circuit Breaker** is used to protect inter-service communication
- Prevents cascading failures when dependent services are unavailable or slow
- Supports fallback mechanisms for Feign client calls
- Improves overall system stability and availability


---

## 📦 Shared Modules

### Logging Module (`logging`)
Provides:
- Centralized logging configuration
- Unified log format across all services
- Reusable logging utilities

---

### Interaction API (`interaction-api`)
A shared module containing:
- DTOs (Data Transfer Objects)
- Common exceptions
- Feign client interfaces

This module ensures **type safety** and **consistent communication** between microservices.

---

## 🐳 Databases & Docker

- Each microservice has its **own database**
- Databases are run using **Docker containers**
- Ensures service isolation and scalability
- Simplifies local development and testing

---

## 🚀 Technology Stack

- **Java**
- **Spring Boot**
- **Spring Cloud**
- **Spring Cloud Gateway**
- **Spring Cloud Config**
- **Spring Cloud Eureka**
- **OpenFeign**
- **Docker**
- **REST APIs**

---

## 📄 License

This project is for educational and demonstration purposes.
