# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

The stock-and-fund project is a Java Spring Boot application designed for monitoring stock and fund prices, enabling users to simulate trading, track investments, and calculate profits/losses. The application supports multiple users and integrates with various Chinese financial data sources.

## Architecture & Structure

The project follows Domain Driven Design (DDD) principles with the following main package structure:
- `apis.controller`: REST controllers for web API endpoints
- `domain.model`: Core business entities (FundEntity, StockEntity, DepositEntity, etc.)
- `domain.service`: Business logic services
- `infrastructure.adapter.rest`: REST clients for external data sources (East Money, Sina, GTimg, etc.)
- `infrastructure.persistent`: Data persistence layer with POJOs and MyBatis mappers
- `infrastructure.general.config`: Configuration classes for security, scheduled tasks, etc.

## Technology Stack

- Java 21 (with virtual threads support)
- Spring Boot 3.3.3
- SQLite database
- MyBatis for ORM
- Maven for build management
- Thymeleaf for templating
- Security with OAuth2 and Keycloak integration
- Scheduled tasks for automatic data collection

## Common Commands

### Build & Run
```bash
# Compile and package the application
mvn clean compile
mvn clean package

# Run locally with SQLite database path
java -jar -Dsqllite.db.file=/path/to/stock-and-fund.db target/stock-and-fund-*.jar

# Or run directly with Maven
mvn spring-boot:run
```

### Database Setup
- The application uses SQLite with the `stock-and-fund.db` file
- Database path must be provided via environment variable: `sqllite.db.file=/path/to/stock-and-fund.db`
- Refer to README.md for complete table schema initialization

### Deployment
- Docker deployment is supported via the provided Dockerfile
- Local deployment script: `./localDeployment.sh`
- Application runs on port 8080 by default

### Key Features
- Stock and fund price monitoring
- Profit/loss calculation with historical data
- Multi-user support with authentication
- Keycloak integration for single sign-on
- Daily statistics and email reporting
- Tang Qian Channel method for stock monitoring
- Chart displays for fund net worth trends

### Development Notes
- The application heavily uses caching to reduce external API calls during non-trading hours
- Virtual threads are implemented for concurrent processing of user data
- Email notifications are sent for daily profit/loss calculations
- Trading date validation ensures calculations only occur on valid trading days
- Multiple UI themes available (Bootstrap and Layui styles)

### Testing
- No explicit test directory seen in structure, likely tests are integrated differently
- When making changes, ensure that profit/loss calculations remain accurate
- Pay attention to timezone handling (China timezone GMT+08 is configured)

### Security Configuration
- Memory-based authentication is now used instead of database-based authentication
- Default admin user with username "admin" and password "admin123"
- Keycloak integration still available via SecurityConfigKeyCloak class
- Passwords are no longer BCrypt hashed (for memory users)
- Static resources are configured to bypass security filters

## Environment Variables & Configuration

### Environment Setup
To improve security, sensitive configurations are stored in environment variables:

1. Copy `.env.example` to `.env` and fill in actual values:
```bash
cp .env.example .env
```

2. Required environment variables:
   - `TENCENT_CLOUD_ENV_ID`: Tencent Cloud environment ID
   - `TENCENT_CLOUD_TOKEN`: Tencent Cloud access token
   - `TENCENT_CLOUD_FUND_FUNCTION_NAME`: Fund function name (default: getAllUserFunds)

### Running with Environment Variables
```bash
# Set environment variables manually
export TENCENT_CLOUD_ENV_ID=your_actual_env_id
export TENCENT_CLOUD_TOKEN=your_actual_token
export TENCENT_CLOUD_FUND_FUNCTION_NAME=getAllUserFunds

# Then run the application
mvn spring-boot:run
```

### Application Properties
- Main configuration file: `src/main/resources/application.properties`
- Database connection: SQLite with configurable path via `spring.datasource.url`
- Cache configuration with simple cache type
- Logging levels configured for different modules
- Email settings with placeholders for SMTP configuration

## Deployment Options

### Local Docker Deployment
1. Package the application with Maven
2. Run `./localDeployment.sh` script
3. Access the application at http://localhost:8080

### Direct JAR Execution
Download release JAR and run with:
```bash
java -jar -Dsqllite.db.file=/path/to/stock-and-fund.db stock-and-fund-x.x.x.jar
```

### IDE Development
Configure environment variable `sqllite.db.file` pointing to the SQLite database file path in your IDE's run configuration.

## Database Schema
The application uses SQLite with the following main tables:
- `DEPOSIT`: Profit/loss summary data
- `FUND` and `FUND_HIS`: Fund data and history
- `STOCK` and `STOCK_HIS`: Stock data and history
- `BUY_OR_SELL`: Transaction records
- `PARAM`: Dictionary data
- `users` and `authorities`: Authentication tables
- `OPEN_PERSISTENT_MONTH`: Trading day data

## User Management
Two authentication methods are supported:
1. **Keycloak SSO**: Default method using OAuth2
2. **Local authentication**: Enable by commenting/uncommenting configuration classes
   - Generate passwords using `PasswordUtils.java`
   - Insert users into `users` and `authorities` tables with BCrypt hashes