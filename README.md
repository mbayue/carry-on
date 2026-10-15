# 📋 Task Manager API (Spring Boot Edition)

A production-ready task management REST API built with **Spring Boot**, **Java 25 LTS**, **PostgreSQL**, **Redis**, **Spring Security (JWT)**, **Flyway**, and **OpenAPI/Swagger**.

Converted from the original Node.js/Express architecture to an enterprise-grade Spring Boot service with full behavioral parity, unified response structures, and relational data integrity.

---

## 🚀 Features

- **Authentication & JWT Security**: Stateless authentication with JJWT (`HMAC-SHA512`).
- **Token Invalidation on Logout**: Active tokens are tracked in Redis (`auth:token:<token>`) with automatic TTL expiration; logout immediately blacklists the token.
- **Role-Based Access Control (RBAC)**: Supports `ADMIN` and `USER` roles. Task deletion (`DELETE /api/tasks/{id}`) is strictly restricted to `ADMIN`.
- **Task Management (CRUD)**: Create, read, update, and delete tasks with automatic assignee defaulting to the currently authenticated user when omitted.
- **Dynamic Filtering**: Filter tasks by `status` (`pending`, `in-progress`, `done`), exact calendar `dueDate`, and `user` using JPA Criteria Specifications.
- **High-Performance Caching**: Redis query caching (`@Cacheable`) with automated invalidation (`@CacheEvict`) and connection pooling (`commons-pool2`).
- **Database Migrations & Seed Data**: Automated Flyway schema migrations (`V1__init_schema.sql`) and initial seed data (`V2__seed_data.sql`).
- **Interactive Documentation**: Swagger UI / OpenAPI 3 with live Bearer token authentication testing.
- **Unified API Response Schema**: Consistent JSON structure (`ApiResponse<T>`) across all endpoints with semantic error statuses (`conflict`, `bad_request`, `unauthorized`, `not_found`).
- **Comprehensive Test Suite**: 31 unit and WebMvc tests with JUnit 5 and Mockito.

---

## 🛠️ Tech Stack

- **Language:** Java 25 LTS (compatible with Java 21 LTS)
- **Framework:** Spring Boot 3+ (Spring Web, Spring Security, Spring Data JPA, Spring Data Redis, Validation)
- **Database:** PostgreSQL (with UUID primary keys and foreign key constraints)
- **Cache & Token Store:** Redis (Lettuce client with Commons Pool 2)
- **Database Migrations:** Flyway
- **Security:** Spring Security + JJWT 0.12.x + BCrypt
- **API Documentation:** SpringDoc OpenAPI 3 (Swagger UI)
- **Testing:** JUnit 5, Mockito, AssertJ, Spring Test (MockMvc)
- **Build Tool:** Maven (Maven Wrapper included)
- **Containerization:** Docker Compose / Rancher Desktop

---

## ⚙️ Setup & Installation

### Prerequisites

- **Java 25** (or **Java 21 LTS**)
- **Docker** / **Rancher Desktop** (with Docker Compose enabled)

### 1. Clone Repository

```bash
git clone https://github.com/mbayue/task-manager.git
cd task-manager
```

### 2. Start PostgreSQL and Redis

Start the containerized databases using Docker Compose:

```bash
docker compose up -d
```

This starts:
- **PostgreSQL 18** on port `5432` (`mydatabase` / user: `myuser` / password: `secret`)
- **Redis** on port `6379`

### 3. Run the Application

#### Windows (PowerShell / CMD):
```powershell
.\mvnw.cmd clean spring-boot:run
```

#### macOS / Linux:
```bash
./mvnw clean spring-boot:run
```

The server starts on port **`3000`**. On initial startup, Flyway automatically builds the tables and populates seed data.

---

## 🧪 Pre-configured Seed Accounts

For immediate testing, Flyway seeds the following credentials:

| Role | Email | Password | Permissions |
|---|---|---|---|
| **ADMIN** | `admin@example.com` | `admin123` | Full access: CRUD tasks, delete tasks, manage all resources. |
| **USER** | `user@example.com` | `user123` | Standard access: Create, read, and update tasks. Deleting tasks returns `401 Unauthorized`. |

---

## 📖 API Documentation & Live Testing

Once the application is running, open the interactive Swagger UI in your browser:

👉 **[http://localhost:3000/swagger-ui.html](http://localhost:3000/swagger-ui.html)**

### Testing with Bearer Token in Swagger:
1. Execute `POST /api/auth/login` using `admin@example.com` / `admin123`.
2. Copy the `token` string from the response.
3. Click the green **Authorize** button at the top of Swagger UI.
4. Paste the token and click **Authorize**.
5. All protected `/api/tasks` endpoints are now unlocked for testing.

---

## 📬 API Endpoints

### Authentication (`/api/auth`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Register a new user (returns JWT token). |
| `POST` | `/api/auth/login` | Public | Login with email and password (returns JWT token). |
| `POST` | `/api/auth/logout` | Authenticated | Revoke active token in Redis and log out. |

### Tasks (`/api/tasks`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/tasks` | Authenticated | Create a new task (defaults assignee to current user if omitted). |
| `GET` | `/api/tasks` | Authenticated | List all tasks. Supports filters: `?status=...&dueDate=...&user=...` |
| `GET` | `/api/tasks/{id}` | Authenticated | Get a specific task by UUID (cached in Redis). |
| `PUT` | `/api/tasks/{id}` | Authenticated | Update task fields (invalidates Redis cache). |
| `DELETE` | `/api/tasks/{id}` | **ADMIN only** | Delete a task (invalidates Redis cache). |

---

## 📦 Unified Response Schema

Every response adheres to the standardized `ApiResponse<T>` contract. Empty or null fields (`data` or `errors`) are automatically omitted.

### Success Format (`200 OK` / `201 Created`)
```json
{
  "status": "success",
  "code": 200,
  "message": "Task retrieved successfully",
  "timestamp": "2026-10-15T13:25:00",
  "data": {
    "id": "e4197192-697f-49bf-9d2e-9fea304fa663",
    "title": "Setup Spring Boot Architecture",
    "description": "Convert Express architecture to Spring Boot 3 with JPA and Redis",
    "status": "done",
    "dueDate": "2026-10-18T11:06:51",
    "assignedTo": "781edad2-bd22-4685-a400-1500faf94a32",
    "assignedToName": "Admin Manager",
    "createdAt": "2026-10-15T11:06:51",
    "updatedAt": "2026-10-15T11:06:51"
  }
}
```

### Semantic Error Formats

#### Duplicate Registration (`409 Conflict`):
```json
{
  "status": "conflict",
  "code": 409,
  "message": "User already exists",
  "timestamp": "2026-10-15T13:24:05"
}
```

#### Validation Error (`400 Bad Request`):
```json
{
  "status": "bad_request",
  "code": 400,
  "message": "Validation failed",
  "timestamp": "2026-10-15T13:24:13",
  "errors": [
    "Email is required",
    "Password is required"
  ]
}
```

#### Unauthorized Access (`401 Unauthorized`):
```json
{
  "status": "unauthorized",
  "code": 401,
  "message": "Not authorized",
  "timestamp": "2026-10-15T13:24:19"
}
```

#### Resource Not Found (`404 Not Found`):
```json
{
  "status": "not_found",
  "code": 404,
  "message": "Task not found with id: e4197192-...",
  "timestamp": "2026-10-15T13:24:25"
}
```

---

## 🧪 Running Tests

Execute the full suite of 31 unit and WebMvc tests:

```powershell
.\mvnw.cmd clean test
```

---

## 🔒 Security & Environment Variables

Production deployments can override defaults using environment variables (see `.env.example`):

```bash
PORT=3000
DATABASE_URL=jdbc:postgresql://your-db-host:5432/your-db-name
DB_USER=your-db-user
DB_PASSWORD=your-db-password
REDIS_HOST=your-redis-host
REDIS_PORT=6379
JWT_SECRET=your_base64_or_hex_encoded_secret_at_least_256_bits_long
```

---

## 📄 License

This project is licensed under the MIT License.
