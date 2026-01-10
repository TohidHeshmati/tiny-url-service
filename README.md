# TinyURL Service 🔗
A high-performance, production-ready URL shortener built with Kotlin and Spring Boot.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> A minimal production-ready URL shortener designed for low-latency redirections and high scalability. 🚀



## 🚀 Quick start
Simply use `make run` and check if it is up and running from swagger ui.

### Swagger UI
The Swagger UI is available at `http://localhost:8080/swagger-ui/index.html` when the application is running.

## 🏗 Architecture Decisions
#### 1. Short Code Strategy (ID Shuffling)
Instead of random strings (which cause DB collisions) or sequential IDs (which are guessable), this service uses:

1. **DB Sequence:** A global_id_sequence table provides unique sequential IDs.
2. **LCG Shuffling:** A Linear Congruential Generator shuffles the ID to make it non-obvious.
3. **Base62 Encoding:** The shuffled ID is encoded for the final URL (e.g., https://tiny.url/aB3x9L).

#### 2. High-Performance Caching
1. **Read-Through Cache:** Redis stores the Short Code -> Original URL mapping.
2. **Impact:** Reduces database load by ~90% for popular links, enabling sub-100ms redirection.

#### 3. Scalable Cleanup
1. **Scheduled Tasks:** A UrlCleanupJob runs periodically to prune expired records, keeping the primary database lean.

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
🚧 Custom aliases (e.g., /my-promo-link).
🚧 Rate limiting per IP.

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


### 📝 License
MIT License

```