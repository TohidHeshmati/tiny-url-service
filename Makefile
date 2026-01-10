.PHONY: run stop clean test build check format help logs

# Default target
run:
	@echo "🚀 Starting infrastructure..."
	docker compose up -d
	@echo "⏳ Waiting for MySQL to be ready..."
	@sleep 10
	@echo "🌱 Starting the application..."
	./gradlew bootRun --args='--spring.profiles.active=local'


# Usage: make shorten url=https://google.com
shorten:
	@curl -X POST http://localhost:8080/api/v1/urls \
		-H "Content-Type: application/json" \
		-d '{"original_url": "$(or $(url), https://example.com)", "expiry_date": "2026-12-31T23:59:59Z"}' \
		| jq .

health:
	@curl -s http://localhost:8080/actuator/health | jq .

# Build the JAR and the Docker Image
build:
	@echo "🔨 Building the JAR file..."
	./gradlew bootJar
	@echo "📦 Creating Docker Image..."
	docker build -t tiny-url-service .

# Standardize code style
format:
	./gradlew ktlintFormat

check:
	./gradlew ktlintCheck

# Run tests
test:
	@echo "🧪 Running integration tests..."
	./gradlew clean test

# View logs for the infrastructure (MySQL/Redis)
logs:
	docker compose logs -f

# Stop and remove volumes (clean slate)
stop:
	@echo "🛑 Stopping application and infrastructure..."
	docker compose down -v

clean:
	./gradlew clean