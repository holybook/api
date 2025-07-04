# Instructions for coding agents

This file provides guidance for agents when working with code in this repository.

## Project Overview

This is a Kotlin-based multi-module project that processes and serves Bahá'í religious texts. The system consists of data ingestion pipeline, API server, and React frontend for browsing and translating texts.

## Architecture

The project follows a modular architecture with these key components:

### Data Flow Pipeline
```
fetch-index → fetch-content → import → postgres db → ktor server → react frontend
```

### Core Modules
- **lib**: Shared library containing models, database access, translation services, and parsing utilities
- **server**: Ktor-based REST API server that serves the frontend and provides endpoints for books, search, and translation
- **fetch-index**: CLI tool to retrieve document indices from web sources
- **fetch-content**: CLI tool to download document content based on indices
- **import**: CLI tool to import processed XML documents into PostgreSQL database
- **index**: CLI tool for indexing content for search functionality
- **eval**: Evaluation tools for translation quality assessment
- **webclient**: React frontend for browsing and translating texts

### Key Technologies
- **Backend**: Kotlin, Ktor server, PostgreSQL, HikariCP connection pooling
- **Translation**: Google Generative API integration for AI-powered translation
- **Frontend**: React, Material-UI, Sass
- **Build**: Gradle with Kotlin DSL
- **Database**: PostgreSQL with raw SQL queries (no ORM)
- **Dependency Injection**: Dagger 2

## Common Development Commands

### Backend (Kotlin/Gradle)
```bash
# Build all modules
./gradlew build

# Run specific module
./gradlew :server:run
./gradlew :import:run --args="-jdbc <JDBC_URL>"
./gradlew :fetch-index:run --args="-o <output_directory>"

# Run tests
./gradlew test

# Build shadow JAR for server
./gradlew :server:shadowJar
```

### Frontend (React)
```bash
# Navigate to webclient directory first
cd webclient

# Install dependencies
npm install

# Start development server
npm start

# Build for production
npm run build

# Run tests
npm test
```

## Database Configuration

The application uses PostgreSQL and requires a JDBC URL for connection. Database initialization is handled automatically through the Database.init() method in the server module.

For Google Cloud SQL instances, ensure you authenticate first:
```bash
gcloud auth application-default login
```

## Translation System

The translation functionality uses Google Generative AI API and includes:
- **IncrementalTranslator**: Handles paragraph-by-paragraph translation
- **MonolithicTranslator**: Handles bulk translation operations
- **TextMatcher**: Matches text segments for translation alignment
- **GeminiTranslator**: Implementation using Google's Gemini models

## Key Configuration Files

- **buildSrc/src/main/kotlin/Deps.kt**: Centralized dependency management
- **buildSrc/src/main/kotlin/Versions.kt**: Version constants
- **server/src/main/resources/application.conf**: Server configuration
- **webclient/package.json**: Frontend dependencies and scripts

## Data Processing Pipeline

1. **fetch-index**: Retrieves document indices from web sources
2. **fetch-content**: Downloads XML content files based on indices
3. **import**: Parses XML and imports into PostgreSQL database
4. **index**: Creates search indices for full-text search
5. **server**: Serves API endpoints for frontend consumption

## Testing

The project uses JUnit for testing. Run tests for all modules:
```bash
./gradlew test
```

## Deployment

The project includes deployment scripts in the `scripts/` directory:
- `deploy_appengine.sh`: Google App Engine deployment
- `deploy_compose.sh`: Docker Compose deployment
- `update_db.sh`: Database update script
- `update_rds.sh`: RDS database update script

## Development Notes

- Use Java 21 as the target language version
- Database operations use raw SQL with custom transaction management
- Translation services are injected using Dagger 2
- Frontend proxies API calls to backend server (configured in package.json)
- All modules use common dependency configurations defined in buildSrc