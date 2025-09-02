# Chess Club Administration App

The local chess club wants to keep track of their members. The main thing that they want to
keep track of is the ranking of each member.

## Quick Start with Docker (Recommended)

### Prerequisites
- Docker
- Docker Compose

### Run with Docker
```bash
# Build and start the application
make docker-run

# Or manually:
docker-compose up -d

# View logs
make docker-logs

# Stop the application
make docker-stop
```

### Production Deployment
```bash
# Deploy production version
make docker-prod

# View production logs
make docker-logs-prod
```

## Development Setup

### Prerequisites
- Java 21
- Maven 3.9+
- Node.js (for Vaadin frontend)

### Traditional Development
```bash
# Build frontend
mvn vaadin:build-frontend

# Run tests
make test

# Start the application
make run

# Or manually:
mvn spring-boot:run
```


## 📚 Available Commands

### Docker Commands
```bash
make help              # Show all available commands
make docker-build      # Build Docker image
make docker-run        # Run with Docker Compose (development)
make docker-prod       # Run with Docker Compose (production)
make docker-stop       # Stop Docker containers
make docker-clean      # Clean Docker containers and images
make docker-logs       # Show Docker logs
make status            # Show container status and resource usage
```

### Development Commands
```bash
make build             # Build with Maven
make test              # Run tests
make run               # Run locally
make clean             # Clean build artifacts
```

## Using the API

### Add a Member
```bash
curl -X POST http://localhost:8084/api/members \
  -H "Content-Type: application/json" \
  -d '{"name":"John","surname":"Doe","email":"test@example.com","birthday":"1990-01-01"}' \
  -v
```

### Record a Match
```bash
curl -X POST 'http://localhost:8084/api/matches' \
  -H 'Content-Type: application/json' \
  -d '{
    "player1": {"id": 6},
    "player2": {"id": 1},
    "player1Score": 1,
    "player2Score": 0
  }'
```

## Access the Application

- **Main Application**: http://localhost:8084/
- **H2 Console** (development): http://localhost:8084/h2-console
- **Swagger UI**: http://localhost:8084/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8084/api-docs
