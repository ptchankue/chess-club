# Chess Club Application Makefile

.PHONY: help build run stop clean logs test docker-build docker-run docker-stop docker-clean docker-logs docker-test

# Default target
help:
	@echo "Chess Club Application - Available Commands:"
	@echo ""
	@echo "Development:"
	@echo "  build          - Build the application with Maven"
	@echo "  run            - Run the application locally"
	@echo "  test           - Run tests"
	@echo "  stop           - Stop the local application"
	@echo "  clean          - Clean Maven build artifacts"
	@echo ""
	@echo "Docker:"
	@echo "  docker-build   - Build Docker image"
	@echo "  docker-run     - Run with Docker Compose (development)"
	@echo "  docker-prod    - Run with Docker Compose (production)"
	@echo "  docker-stop    - Stop Docker containers"
	@echo "  docker-clean   - Clean Docker containers and images"
	@echo "  docker-logs    - Show Docker logs"
	@echo "  docker-test    - Run tests in Docker"
	@echo ""
	@echo "Database:"
	@echo "  db-backup      - Backup PostgreSQL database"
	@echo "  db-restore     - Restore PostgreSQL database"

# Development commands
build:
	mvn clean compile

run:
	mvn spring-boot:run

test:
	mvn test

stop:
	@echo "Stopping local application..."
	@pkill -f "spring-boot:run" || true

clean:
	mvn clean

# Docker commands
docker-build:
	docker build -t chessclub:latest .

docker-run:
	docker-compose up -d

docker-prod:
	docker-compose -f docker-compose.prod.yml up -d

docker-stop:
	docker-compose down

docker-stop-prod:
	docker-compose -f docker-compose.prod.yml down

docker-clean:
	docker-compose down -v --rmi all
	docker system prune -f

docker-logs:
	docker-compose logs -f

docker-logs-prod:
	docker-compose -f docker-compose.prod.yml logs -f

docker-test:
	docker run --rm -v $(PWD):/app -w /app maven:3.9.11-openjdk-21 mvn test

# Database commands
db-backup:
	docker exec chessclub-db pg_dump -U chessclub chessclub > backup_$(shell date +%Y%m%d_%H%M%S).sql

db-restore:
	@echo "Usage: make db-restore BACKUP_FILE=backup_20231201_120000.sql"
	@if [ -z "$(BACKUP_FILE)" ]; then echo "Please specify BACKUP_FILE"; exit 1; fi
	docker exec -i chessclub-db psql -U chessclub -d chessclub < $(BACKUP_FILE)

# Utility commands
status:
	@echo "Application Status:"
	@docker-compose ps
	@echo ""
	@echo "Container Resources:"
	@docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}"

shell:
	docker exec -it chessclub-app /bin/bash

db-shell:
	docker exec -it chessclub-db psql -U chessclub -d chessclub
