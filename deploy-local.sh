#!/bin/bash

# deploy-local.sh
# Script for full local project deployment

set -e  # Exit on error

echo "Starting project deployment..."

# Check for required tools
echo "Checking dependencies..."
command -v docker >/dev/null 2>&1 || { echo "ERROR: Docker is not installed"; exit 1; }
command -v docker-compose >/dev/null 2>&1 || { echo "ERROR: Docker Compose is not installed"; exit 1; }
command -v mvn >/dev/null 2>&1 || { echo "ERROR: Maven is not installed"; exit 1; }

# Check Java version (minimum 23)
echo "Checking Java version..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
    echo "Found Java version: $JAVA_VERSION"

    if [ "$JAVA_VERSION" -lt 23 ]; then
        echo "ERROR: Java version 23 or higher is required. Found version $JAVA_VERSION"
        exit 1
    fi
else
    echo "ERROR: Java is not installed"
    exit 1
fi

# Clean previous builds
echo "Cleaning previous builds..."
mvn clean -q

# Build Maven project
echo "Building Maven project..."
mvn package -DskipTests -q

echo "Building Docker images..."

# Stop and remove previous containers
echo "Stopping previous containers..."
docker-compose down --remove-orphans

# Build and start containers
echo "Starting Docker Compose..."
docker-compose up --build -d

echo "Waiting for services to start..."

# Check container status
echo "Checking container status..."
sleep 10

docker-compose ps

echo ""
echo "SUCCESS: Deployment completed!"
echo ""
echo "Available services:"
echo "   Main application:  http://localhost:727"
echo "   Frontend:          http://localhost:5173"
echo "   Grafana:           http://localhost:3000"
echo "   Prometheus:        http://localhost:9090"
echo "   Jaeger:            http://localhost:16686"
echo "   RabbitMQ Manager:  http://localhost:15627 (admin/password)"
echo ""
echo "To view logs use: docker-compose logs -f"
echo "To stop services: docker-compose down"
