#!/bin/bash

# build-and-run.sh
# Script for building and running the project locally

set -e  # Exit on error

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOGS_DIR="$PROJECT_ROOT/logs"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check Java version
check_java_version() {
    log_info "Checking Java version..."

    if ! command -v java &> /dev/null; then
        log_error "Java is not installed"
        exit 1
    fi

    # Extract major version (handles both "23" and "23.0.1" formats)
    JAVA_VERSION_STRING=$(java -version 2>&1 | head -1 | cut -d'"' -f2)
    JAVA_MAJOR_VERSION=$(echo "$JAVA_VERSION_STRING" | cut -d'.' -f1)

    # Handle versions like "1.8" or "11"
    if [[ "$JAVA_MAJOR_VERSION" == "1" ]]; then
        JAVA_MAJOR_VERSION=$(echo "$JAVA_VERSION_STRING" | cut -d'.' -f2)
    fi

    log_info "Detected Java version: $JAVA_VERSION_STRING (major: $JAVA_MAJOR_VERSION)"

    if [ "$JAVA_MAJOR_VERSION" -lt 23 ]; then
        log_error "Java version 23 or higher is required. Found version $JAVA_MAJOR_VERSION"
        exit 1
    fi

    log_success "Java version check passed"
}

# Function to check dependencies
check_dependencies() {
    log_info "Checking dependencies..."

    local missing_deps=()

    # Check Java version first
    check_java_version

    # Check Maven
    if ! command -v mvn &> /dev/null; then
        missing_deps+=("Maven")
    else
        MAVEN_VERSION=$(mvn -v | grep "Apache Maven" | cut -d' ' -f3)
        log_info "Maven version: $MAVEN_VERSION"
    fi

    # Check Docker
    if ! command -v docker &> /dev/null; then
        missing_deps+=("Docker")
    else
        DOCKER_VERSION=$(docker --version | cut -d' ' -f3 | sed 's/,//')
        log_info "Docker version: $DOCKER_VERSION"
    fi

    # Check Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        missing_deps+=("Docker Compose")
    else
        DOCKER_COMPOSE_VERSION=$(docker-compose --version | cut -d' ' -f3 | sed 's/,//')
        log_info "Docker Compose version: $DOCKER_COMPOSE_VERSION"
    fi

    if [ ${#missing_deps[@]} -gt 0 ]; then
        log_error "Missing dependencies: ${missing_deps[*]}"
        exit 1
    fi

    log_success "All dependencies are installed"
}

# Function to build Maven project
build_maven() {
    log_info "Building Maven project..."

    # Clean
    log_info "Cleaning project..."
    mvn clean -q

    # Build with tests skipped
    log_info "Compiling and packaging..."
    if mvn package -DskipTests -q; then
        log_success "Maven build completed successfully"
    else
        log_error "Error during Maven build"
        exit 1
    fi
}

# Function to check configuration files
check_config_files() {
    log_info "Checking configuration files..."

    # Create logs directory
    mkdir -p "$LOGS_DIR"
    log_info "Logs directory: $LOGS_DIR"
}

# Function to start Docker containers
start_containers() {
    log_info "Starting Docker containers..."

    # Stop previous containers
    log_info "Stopping previous containers..."
    docker-compose down --remove-orphans 2>/dev/null || true

    # Build and start
    log_info "Building and starting containers..."
    if docker-compose up --build -d; then
        log_success "Containers started successfully"
    else
        log_error "Error starting containers"
        exit 1
    fi
}

# Function to start containers without rebuilding
start_containers_no_build() {
    log_info "Starting Docker containers (no rebuild)..."

    # Stop previous containers
    log_info "Stopping previous containers..."
    docker-compose down --remove-orphans 2>/dev/null || true

    # Start without building
    log_info "Starting containers..."
    if docker-compose up -d; then
        log_success "Containers started successfully"
    else
        log_error "Error starting containers"
        exit 1
    fi
}

# Function to check service status
check_services() {
    log_info "Checking service status..."
    sleep 15

    echo ""
    echo "========================================="
    echo "         SERVICE STATUS"
    echo "========================================="
    docker-compose ps

    echo ""
    echo "========================================="
    echo "         AVAILABLE SERVICES"
    echo "========================================="
    echo -e "${GREEN}Main application:${NC} http://localhost:727"
    echo -e "${GREEN}Frontend:${NC}          http://localhost:5173"
    echo -e "${GREEN}Grafana (monitoring):${NC} http://localhost:3000"
    echo -e "${GREEN}Prometheus:${NC}        http://localhost:9090"
    echo -e "${GREEN}Jaeger (tracing):${NC}  http://localhost:16686"
    echo -e "${GREEN}RabbitMQ Manager:${NC}  http://localhost:15627"
    echo "              Login: admin"
    echo "              Password: password"
    echo "========================================="

    echo ""
    echo "Useful commands:"
    echo "  View logs:        docker-compose logs -f [service_name]"
    echo "  Stop services:    docker-compose down"
    echo "  Restart service:  docker-compose restart [service_name]"
}

# Function to show help
show_help() {
    echo "Usage: $0 [options]"
    echo ""
    echo "Options:"
    echo "  --help, -h       Show this help message"
    echo "  --build-only     Build only, do not run"
    echo "  --run-only       Run only, do not build"
    echo "  --logs           Show logs after starting"
    echo "  --clean          Full clean before building"
    echo "  --status         Show service status after starting"
    echo "  --no-status      Do not show service status after starting"
    echo "  --no-rebuild     Start containers without rebuilding images"
    echo ""
}

# Main function
main() {
    # Parse arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            --build-only)
                echo "========================================="
                echo "    BUILD PROJECT ONLY"
                echo "========================================="

                cd "$PROJECT_ROOT"
                check_dependencies
                check_config_files
                build_maven

                log_success "Build completed. Use './build-and-run.sh --run-only' to start containers."
                exit 0
                ;;
            --run-only)
                echo "========================================="
                echo "    RUN CONTAINERS ONLY"
                echo "========================================="

                cd "$PROJECT_ROOT"
                check_dependencies
                check_config_files

                # Check if --no-rebuild is the next argument
                if [ "$2" = "--no-rebuild" ]; then
                    shift
                    log_info "Starting containers without rebuild..."
                    start_containers_no_build
                else
                    start_containers
                fi

                check_services

                # Check if --logs is provided
                for arg in "$@"; do
                    if [ "$arg" = "--logs" ]; then
                        log_info "Showing container logs..."
                        docker-compose logs -f
                        break
                    fi
                done

                log_success "Containers started successfully"
                exit 0
                ;;
            --clean)
                echo "========================================="
                echo "    CLEAN BUILD AND RUN"
                echo "========================================="

                cd "$PROJECT_ROOT"
                check_dependencies
                check_config_files

                log_info "Performing full clean..."
                mvn clean -q
                docker system prune -f

                build_maven
                start_containers
                check_services
                log_success "Clean build and run completed"
                exit 0
                ;;
            --logs)
                echo "========================================="
                echo "    VIEW CONTAINER LOGS"
                echo "========================================="

                cd "$PROJECT_ROOT"
                log_info "Showing container logs..."
                docker-compose logs -f
                exit 0
                ;;
            --status)
                echo "========================================="
                echo "    CHECK SERVICE STATUS"
                echo "========================================="

                cd "$PROJECT_ROOT"
                check_services
                exit 0
                ;;
            --no-rebuild)
                # This flag is handled in --run-only case
                shift
                continue
                ;;
            *)
                # Default mode: build and run
                echo "========================================="
                echo "    BUILD AND RUN PROJECT"
                echo "========================================="

                cd "$PROJECT_ROOT"
                check_dependencies
                check_config_files

                # Check for --clean flag in default mode
                for arg in "$@"; do
                    if [ "$arg" = "--clean" ]; then
                        log_info "Performing full clean..."
                        mvn clean -q
                        docker system prune -f
                        break
                    fi
                done

                # Check for --no-rebuild flag in default mode
                local rebuild=true
                for arg in "$@"; do
                    if [ "$arg" = "--no-rebuild" ]; then
                        rebuild=false
                        break
                    fi
                done

                build_maven

                if [ "$rebuild" = true ]; then
                    start_containers
                else
                    start_containers_no_build
                fi

                check_services

                # Check for --logs flag
                for arg in "$@"; do
                    if [ "$arg" = "--logs" ]; then
                        log_info "Showing container logs..."
                        docker-compose logs -f
                        break
                    fi
                done

                log_success "Script executed successfully!"
                exit 0
                ;;
        esac
        shift
    done

    # If no arguments provided, run default mode
    echo "========================================="
    echo "    BUILD AND RUN PROJECT"
    echo "========================================="

    cd "$PROJECT_ROOT"
    check_dependencies
    check_config_files

    build_maven
    start_containers
    check_services

    log_success "Script executed successfully!"
}

# Run main function
main "$@"
