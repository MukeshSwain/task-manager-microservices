# Task Manager - Microservice Architecture

## Overview
This project demonstrates a microservice architecture combining:
- **Auth Service** (Node.js + Prisma + MySQL)
- **User Service** (Spring Boot + Java)

Each service runs independently and manages its own database.

## Run Locally
### Auth Service
```bash
  cd auth_service
  npm install
  npm run dev
```

### User Service
```bash
  cd user_service
  ./mvnw spring-boot:run
```

# Tech Stack
- **Auth Service**
  - Node.js
  - Prisma
  - MySQL
- **User Service**
  - Spring Boot
  - Java
  - MongoDB
