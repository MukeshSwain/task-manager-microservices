# Multi-Tenant Task Manager-Microservice Architecture

A production‑grade, enterprise‑level Multi‑Tenant Task Manager built using a Microservices Architecture.
This system is designed for SaaS environments with strict tenant isolation, scalability, observability, 
and modern backend engineering practices.
### This repository currently contains the following services:

- **Auth Service** (Node.js + Prisma + MySQL)
- **User Service** (Spring Boot + MongoDB)
- **Tenant Service** (Spring Boot + PostgresSQL)
- **Notification Service** (Spring Boot + RabbitMQ)
- **Project Service** (Spring Boot + PostgresSQL)
- **Task Service** (Spring Boot + PostgresSQL)

Additional services (Task, Billing, Notification, Gateway, etc.) will be added as the system evolves.
# 🚀 Architecture Overview
## 🏗️ Microservices Design

- **Independently deployable**
- **Scoped to a single responsibility**
- **Backed by its own database to enforce strong tenant isolation**
- **Communicating through REST/Message Broker**

# Project Structure (Update in future)
- **auth-service**
- **user-service**
- **tenant-service**
- **notification-service**
- **project-service**
- **task-service**

# Tech Stack
- **Auth Service**
  - Node.js
  - Prisma
  - MySQL
  - JWT
- **User Service**
  - Spring Boot
  - Java
  - MongoDB
- **Tenant Service**
  - Spring Boot
  - Java
  - PostgresSQL
- **Notification Service**
  - Spring Boot
  - Java
  - RabbitMQ
- **Project Service**
  - Spring Boot
  - Java
  - Postgresql
- **Task Service**
  - Spring Boot
  - Java
  - Postgresql
  - RabbitMQ

# Services Summary
- **Auth Service**
  - User registration & login
  - JWT access/refresh token issuance
- **User Service**
  - User profiles
  - User ↔ Organization relationships
- **Tenant Service**
  - Organization creation
  - Adding and removing members
  - Managing tenant-level metadata
- **Notification Service**
  - Sending emails
- **Project Service**
  - Managing project related operations
- **Task Service**
  - Managing task related operations
  

## Run Locally
### Auth Service
```bash
  cd auth-service
  npm install
  npm run dev
```

### User Service
```bash
  cd user-service
  ./mvnw spring-boot:run
```

### Tenant Service
```bash
  cd tenant-service
  ./mvnw spring-boot:run
```

### Notification Service
```bash
  cd notification-service
  ./mvnw spring-boot:run
```

## Project Service
```bash
  cd project-service
  ./mvnw spring-boot:run
```

## Task Service
```bash
  cd task-service
  ./mvnw spring-boot:run
```

# API Documentation / Endpoints
Below is a high-level overview of the core API endpoints for each service. Full Swagger/OpenAPI specs can be added later.
### 🔐 Auth Service (Node.js + Prisma + MySQL)
**Base URL: `/auth`**

| Method | Endpoint                    | Description                                |
|--------|-----------------------------|--------------------------------------------|
| POST   | `/auth/signup`              | Register a new user                        |
| POST   | `/auth/login`               | Login and receive access + refresh tokens  |
| POST   | `/auth/verify-email/:token` | Verify the email before signup             |
| POST   | `/auth/verify-otp`          | Verify OTP to login                        |
| POST   | `/auth/logout`              | Logout the user and clear cookie and redis |
| POST   | `/auth/refresh`             | Refresh the access token                   |
| POST   | `/auth/signup/invite`       | signup via invitation link                 |


### 📦 User Service (Spring Boot + MongoDB)
**Base URL: `/user`**

| Method | Endpoint                  | Description                   |
|--------|---------------------------|-------------------------------|
| POST   | `/user`                   | Create a new user             |
| POST   | `/user/upload-image`      | Upload profile picture        |
| PUT    | `/user`                   | Update user profile           |
| GET    | `/user/profile`           | Get user profile details      |
| GET    | `/user/all`               | Get all users profile details |
| GET    | `/user/lookup`            | Lookup user by email          |
| GET    | `/user/email/:authId`     | Get email by authId           |
| POST   | `/user/betch-fetch`       | Get users by authIds          |
| GET    | `/user/validate/{authId}` | Validate user by authId       |


### 📦 Tenant Service (Spring Boot + PostgresSQL)
**Base URL: `/tenant`**

| Method | Endpoint             | Description                                               |
|--------|----------------------|-----------------------------------------------------------|
| POST   | `/tenant`            | Create a new tenant/organization                          |
| POST   | `/tenant/:orgId/add` | Add member to organization                                |
| GET    | `/tenant/me`         | Get all organizations of user(authId provided in request) |


**Base URL: `/member`**

| Method | Endpoint                          | Description                           |
|--------|-----------------------------------|---------------------------------------|
| GET    | `/member/invitation/validate`     | Validate invitation token             |
| POST   | `/member/invitation/accept`       | Accept and add member to organization |
| PUT    | `/member/{orgId}/update/role`     | Update member role (authId in body)   |
| GET    | `/member/{orgId}`                 | Get all members of organization       |
| DELETE | `/member/{orgId}/remove/{authId}` | Remove member from organization       |
| GET    | `/member/{orgId}/{authId}/get`    | Get member details                    |


**Base URL: `/invitation`**

| Method | Endpoint                      | Description                                 |
|--------|-------------------------------|---------------------------------------------|
| GET    | `/invitation/{orgId}/pending` | Get all pending invitations of organization |
| PUT    | `/invitation/{orgId}/{email}` | Cancel invitation                           |


### 📦 Project Service (Spring Boot + PostgresSQL)
**Base URL: `/project`**

| Method | Endpoint                             | Description                      |
|--------|--------------------------------------|----------------------------------|
| POST   | `/project`                           | Create a new project             |
| GET    | `/project/{projectId}`               | Get project details              |
| GET    | `/project/org/{orgId}`               | Get all projects of organization |
| GET    | `/project/user/{authId}`             | Get all projects of user         |
| PUT    | `/project/{projectId}/{performedBy}` | Update project details           |
| DELETE | `/project/{projectId}`               | Delete project                   |
| GET    | `/project/validate/{projectId}`      | validate the project by id       |


**Base URL: `projectMember=/api/projects/{projectId}/members`**

| Method | Endpoint                                | Description                |
|--------|-----------------------------------------|----------------------------|
| POST   | `/projectMember/{performedBy}`          | Add member to project      |
| GET    | `/projectMember`                        | Get all members of project |
| DELETE | `/projectMember/{authId}/{performedBy}` | Remove member from project |
| PUT    | `/projectMember/{authId}/{performedBy}` | update role                |


### 📦 Task Service (Spring Boot + PostgresSQL)
**Base URL: `task=/api/{projectId}/tasks`**

| Method | Endpoint                | Description                |
|--------|-------------------------|----------------------------|
| POST   | `/task`                 | Create a new task          |
| GET    | `/task/{taskId}`        | Get task details           |
| GET    | `/task`                 | Get all tasks of a project |
| PUT    | `/task/{taskId}`        | Update task details        |
| DELETE | `/task/{taskId}`        | Delete task                |
| PUT    | `/task/{taskId}/status` | Update task status         |
| PUT    | `/task/{taskId}/assign` | Assign task to user        |


## 🙌 Author
Mukesh Swain-Backend Developer | Spring Boot | MERN | Microservices