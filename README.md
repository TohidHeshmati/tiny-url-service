# TinyURL Service 🔗

> A minimal production-ready URL shortener designed for low-latency redirections and high scalability. 🚀

## 🚀 Quick start
Simply use `make run` and check if it is up and running from swagger ui.

### Swagger UI
The Swagger UI is available at `http://localhost:8080/swagger-ui/index.html` when the application is running.

## 🏗 Architecture Decisions

#### 1. Short Code Strategy (ID Shuffling)
Instead of random strings (which cause DB collisions) or sequential IDs (which are predictable and pose a security risk of enumeration), this service uses a robust approach:
1.  **DB Sequence:** A `global_id_sequence` table provides unique, sequential ID blocks for each instance of the app.
2.  **LCG Shuffling:** A Linear Congruential Generator shuffles the ID to make it non-obvious and non-predictable. This prevents enumeration of short URLs, enhancing privacy and security.
3.  **Base62 Encoding:** The shuffled ID is then efficiently encoded into a compact, URL-safe string for the final short URL (e.g., `https://tiny.url/aB3x9L`).

#### 2. High-Performance Caching (Redis)
To ensure rapid redirection and minimize database load, a read-through cache is implemented:
1.  **Read-Through Cache:** Redis is utilized to store the `Short Code -> Original URL` mapping.
2.  **Impact:** This dramatically reduces database hits for popular links, enabling sub-100ms redirection times and significantly improving overall service responsiveness and scalability.

#### 3. Optimized Database Access
Efficient database interactions are critical for performance:
1.  **Purpose-built Sequences:** Dedicated database sequences (e.g., `global_id_sequence`) ensure high-concurrency, collision-free ID generation.
2.  **Strategic Indexing:** Key columns, especially the `short_code` in the URL table, are indexed (`V2__add_index_on_short_url.sql`). This is crucial for sub-millisecond lookup times during redirection and other API calls, preventing full table scans.

#### 4. Scalable & Robust Cleanup
To prevent indefinite database growth and ensure resource efficiency, automated cleanup is integrated:
1.  **Scheduled Tasks:** A `UrlCleanupJob` runs periodically to prune expired or stale URL records from the database.
2.  **Distributed Locking (ShedLock):** `ShedLock` is used to ensure that this job runs reliably in a clustered environment, preventing multiple instances from executing the cleanup concurrently and ensuring data integrity.

#### 5. Comprehensive Quality Assurance
A strong emphasis is placed on code quality, reliability, and correctness:
1.  **Extensive Testing Suite:** The project includes a comprehensive suite of both unit tests (`...Test.kt`) and integration tests (`...IT.kt`).
2.  **Purpose:** This robust testing infrastructure ensures the correctness of business logic, validates API contracts, and provides confidence for future refactoring and feature development.

### ✨ Evolution from Previous Version

This project is a significant evolution of a [previous URL shortener I built](https://github.com/TohidHeshmati/urlShortener). While the original was a functional proof-of-concept, this version was rewritten with a focus on security, performance, and production-readiness.

Here is a summary of the key improvements:

| Feature                 | Previous Implementation              | **Current Version**                       | Impact                                                           |
|:------------------------|:-------------------------------------|:------------------------------------------|:-----------------------------------------------------------------|
| **Short Code Security** | Sequential, predictable IDs          | **LCG Shuffled IDs**                      | Prevents "ID Scraping" & Business Intelligence leaks.            |
| **Id Generation**       | Redis `INCR` (Centralized)           | **Range-based DB Segments**               | High availability; service can still issue IDs if Redis is down. |
| **Scalability**         | Every instance every call hits Redis | **Each instance has local memory blocks** | Massive reduction in network round-trips to storage.             |
| **Architecture**        | Monolithic Controller                | **Separated API vs. Web layers**          | Better Clean Architecture & independent scaling.                 |
| **Reliability**         | No Error Schema                      | **Standardized Error Handling**           | Predictable contract for Frontend/Client consumers.              |

## 🧪 Development & Testing

| Command                       | Action                                                              |
|-------------------------------|---------------------------------------------------------------------|
| `make run`                    | Starts the application and its dependencies (`mysql` and `redis`).  |
| `make stop`                   | Stops the application and its dependencies.                         |
| `make test`                   | Runs the integration tests.                                         |
| `make build`                  | Builds the application JAR and a Docker image.                      |
| `make clean`                  | Cleans the build artifacts.                                         |
| `make format`                 | Formats the code using Ktlint.                                      |
| `make check`                  | Checks the code style using Ktlint.                                 |
| `make logs`                   | Tails the logs from the `mysql` and `redis` containers.             |
| `make health`                 | Checks the health of the application.                               |
| `make shorten url=<url here>` | Shortens a sample URL. If no URL is provided, it uses `https://example.com`. |

### ✨ Features & Limitations
##### Features
- ✅ Base62 Encoding: Short, URL-friendly codes.
- ✅ Expiration Support: Links automatically expire based on user input.
- ✅ Global Error Handling: Consistent JSON error responses.
- ✅ Flyway Migrations: Versioned database schema.

##### Roadmap
- 🚧 Custom aliases (e.g., /my-promo-link).
- 🚧 Rate limiting per IP.

### 🛠Tech Stack
![Kotlin](https://img.shields.io/badge/Kotlin-JVM%20--%20Backend-blueviolet?logo=kotlin)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?logo=springboot)
![Java](https://img.shields.io/badge/Java-21-orange?logo=java)
![MySQL](https://img.shields.io/badge/MySQL-8-blue?logo=mysql)
![Redis](https://img.shields.io/badge/Redis-7-red?logo=redis)
![Gradle](https://img.shields.io/badge/Gradle-Build%20Tool-02303A?logo=gradle)
![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?logo=docker)
![Swagger](https://img.shields.io/badge/Swagger-API%20Docs-%23ClojureGreen?logo=swagger)
![JUnit](https://img.shields.io/badge/JUnit-5-important?logo=java)
![Ktlint](https://img.shields.io/badge/Ktlint-Code%20Formatter-blueviolet?logo=kotlin)
![Flyway](https://img.shields.io/badge/Flyway-DB%20Migration-orange?logo=flyway)

### Pre-requisites
- Java 21
- Gradle (or Gradle Wrapper)
- Docker (for running MySQL and Redis)
- make

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

