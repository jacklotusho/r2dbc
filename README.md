# Spring Boot 4.x R2DBC JWT Authentication

A reactive Spring Boot application with JWT authentication using R2DBC for PostgreSQL database access.

## Tech Stack

- **Spring Boot 4.0.0** (Jakarta EE 11, Java 21+)
- **Spring WebFlux** - Reactive web framework
- **Spring Data R2DBC** - Reactive database access
- **Spring Security** - JWT-based authentication
- **PostgreSQL** - Primary database
- **H2** - Test database (R2DBC dialect)
- **Flyway** - Database migrations
- **Maven** - Build tool
- **jjwt (io.jsonwebtoken)** - JWT library
- **springdoc-openapi 3.0.0** - OpenAPI/Swagger documentation (Spring Boot 4 compatible)

## Database Schema

### Tables

#### users
- `id` BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY (internal use only, never exposed in API)
- `uuid` UUID UNIQUE NOT NULL (business identifier, used in JWT sub claim)
- `username` VARCHAR(50) UNIQUE NOT NULL
- `email` VARCHAR(100) UNIQUE NOT NULL
- `password_hash` VARCHAR(255) NOT NULL
- `enabled` BOOLEAN DEFAULT true
- `created_at` TIMESTAMP NOT NULL

#### roles
- `id` BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY (internal use only)
- `name` VARCHAR(50) UNIQUE NOT NULL

#### user_roles
- `user_id` BIGINT FK → users.id
- `role_id` BIGINT FK → roles.id
- PRIMARY KEY (user_id, role_id)

#### password_reset_tokens
- `id` BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
- `user_id` BIGINT FK → users.id
- `token` VARCHAR(64) UNIQUE NOT NULL
- `expires_at` TIMESTAMP NOT NULL
- `used` BOOLEAN DEFAULT false
- `created_at` TIMESTAMP DEFAULT now()

**Important Design Notes:**
- `id` (Long) is used internally for all FK relationships and DB joins
- `uuid` is the business identifier - JWT `sub` claim contains the user's UUID
- UUIDs are never used as foreign keys
- Internal `id` is never exposed in API responses

## Project Structure

```
src/main/java/com/example/r2dbc/
├── R2dbcJwtAuthApplication.java          # Main application class
├── config/
│   └── SecurityConfig.java               # Security configuration
├── controller/
│   ├── AuthController.java               # Authentication endpoints
│   ├── PasswordResetController.java      # Password reset endpoints
│   └── UserController.java               # User management endpoints
├── dto/
│   ├── AuthResponse.java                 # Authentication response
│   ├── ForgotPasswordRequest.java        # Forgot password request
│   ├── LoginRequest.java                 # Login request
│   ├── RegisterRequest.java              # Registration request
│   ├── ResetPasswordRequest.java         # Reset password request
│   └── UserProfileResponse.java          # User profile profile response
├── entity/
│   ├── Role.java                         # Role entity
│   ├── User.java                         # User entity
│   └── UserRole.java                     # User-Role junction entity
├── repository/
│   ├── RoleRepository.java               # Role repository
│   ├── UserRepository.java               # User repository
│   └── UserRoleRepository.java           # User-Role repository
├── security/
│   ├── JwtAuthenticationWebFilter.java   # JWT authentication filter
│   └── JwtUtil.java                      # JWT utility class
└── service/
    ├── AuthService.java                  # Authentication service
    ├── EmailService.java                 # Reactive email service
    ├── PasswordResetService.java         # Password reset service
    └── UserService.java                  # User management service

src/main/resources/
├── application.yml                       # Application configuration
└── db/migration/
    ├── V1__init.sql                      # Initial database schema
    └── V2__password_reset.sql            # Password reset tokens table

src/test/
├── java/com/example/r2dbc/
│   └── R2dbcJwtAuthApplicationTests.java # Basic test
└── resources/
    └── application-test.yml              # Test configuration
```

## API Documentation

The application provides interactive API documentation via Swagger UI:

| What | URL |
|------|-----|
| **Demo UI** | http://localhost:8080/demo/ |
| **Swagger UI** | http://localhost:8080/demo/swagger-ui.html |
| **OpenAPI JSON** | http://localhost:8080/demo/v3/api-docs |
| **OpenAPI YAML** | http://localhost:8080/demo/v3/api-docs.yaml |

**Note:** The application is configured with a base path of `/demo`. All endpoints are prefixed with `/demo`.

### Using Swagger UI

1. Start the application and open http://localhost:8080/demo/swagger-ui.html
2. Click on `/auth/login` endpoint and try it with default admin credentials:
   ```json
   {
     "usernameOrEmail": "admin",
     "password": "admin123"
   }
   ```
3. Copy the JWT token from the response
4. Click the **"Authorize"** button at the top of the page
5. Enter the token (Swagger UI will automatically add "Bearer " prefix)
6. Now you can test all authenticated endpoints

**Note:** Swagger UI is disabled in production profile (`application-prod.yml`)

## API Endpoints

### Public Endpoints

#### Register User
```http
POST /demo/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "password123"
}

Response: 201 Created
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "username": "john_doe",
  "roles": ["ROLE_USER"]
}
```

#### Login
```http
POST /demo/auth/login
Content-Type: application/json

{
  "usernameOrEmail": "john_doe",  // Can be username or email
  "password": "password123"
}

Response: 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "username": "john_doe",
  "roles": ["ROLE_USER"]
}
```

#### Forgot Password
```http
POST /demo/auth/forgot-password
Content-Type: application/json

{
  "email": "john@example.com"
}

Response: 200 OK (Always returns the same message for security)
{
  "message": "If an account exists for that email, a password reset link has been sent."
}
```

#### Reset Password
```http
POST /demo/auth/reset-password
Content-Type: application/json

{
  "token": "secure-reset-token-here",
  "newPassword": "new_password123",
  "confirmPassword": "new_password123"
}

Response: 200 OK
{
  "message": "Password has been successfully reset."
}
```

### Authenticated Endpoints

#### Get Current User Profile
```http
GET /demo/api/users/me
Authorization: Bearer <token>

Response: 200 OK
{
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "username": "john_doe",
  "email": "john@example.com",
  "roles": ["ROLE_USER"],
  "createdAt": "2024-01-15T10:30:00"
}
```

### Admin Only Endpoints

#### Get All Users
```http
GET /demo/api/users
Authorization: Bearer <token>

Response: 200 OK
[
  {
    "uuid": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe",
    "email": "john@example.com",
    "roles": ["ROLE_USER"],
    "createdAt": "2024-01-15T10:30:00"
  },
  ...
]
```

## JWT Token Structure

The JWT token contains:
- **sub**: User UUID (string representation)
- **roles**: Array of role names (e.g., ["ROLE_USER", "ROLE_ADMIN"])
- **iat**: Issued at timestamp
- **exp**: Expiration timestamp

Example decoded JWT payload:
```json
{
  "sub": "550e8400-e29b-41d4-a716-446655440000",
  "roles": ["ROLE_USER"],
  "iat": 1705315800,
  "exp": 1705402200
}
```

## Configuration

### Database Configuration

Update `src/main/resources/application.yml`:

```yaml
spring:
  webflux:
    base-path: /demo  # All endpoints prefixed with /demo
  
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/r2dbc_auth_db
    username: postgres
    password: postgres
  
  datasource:  # For Flyway
    url: jdbc:postgresql://localhost:5432/r2dbc_auth_db
    username: postgres
    password: postgres

app:
  jwt:
    secret: YourBase64EncodedSecretKeyHereMustBeAtLeast256BitsLongForHS256Algorithm
    expiration-ms: 86400000  # 24 hours
  frontend-base-url: http://localhost:8080/demo
  reset-token-expiry-minutes: 60

spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${GMAIL_USERNAME}
    password: ${GMAIL_APP_PASSWORD}
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true
```

### JWT Secret

Generate a secure JWT secret (512 bits for post-quantum security):
```bash
openssl rand -base64 64
```

## Setup and Running

### Prerequisites

- Java 21+
- PostgreSQL 14+
- Maven 3.8+

### Database Setup

1. Create PostgreSQL database:
```sql
CREATE DATABASE r2dbc_auth_db;
```

2. Flyway will automatically run migrations on application startup

3. Default admin user will be created automatically:
   - **Username:** `admin`
   - **Password:** `admin123`
   - **Roles:** ROLE_USER, ROLE_ADMIN
   - ⚠️ **Change this password in production!**

### Build and Run

```bash
# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run

# Run tests
./mvnw test
```

The application will start on `http://localhost:8080/demo`

### Demo UI and Swagger

Access the application at:
- **Demo UI:** http://localhost:8080/demo/ (interactive authentication demo)
- **Swagger UI:** http://localhost:8080/demo/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/demo/v3/api-docs

**Using Swagger UI with JWT:**
1. Login via `/auth/login` endpoint to get JWT token
2. Click "Authorize" button in Swagger UI
3. Enter: `Bearer <your-jwt-token>`
4. Now you can test authenticated endpoints

## Security Features

1. **Stateless JWT Authentication**: No server-side session storage
2. **Password Hashing**: BCrypt password encoding
3. **Role-Based Access Control**: ROLE_USER and ROLE_ADMIN
4. **UUID-Based Identity**: User UUID in JWT sub claim (not internal ID)
5. **Reactive Security**: WebFlux security with custom JWT filter
6. **Password Reset Flow**: Secure token-based reset with expiration and single-use validation

## Key Design Decisions

### ID vs UUID
- **Internal ID (Long)**: Used for all database relationships and joins
- **UUID**: Business identifier exposed in APIs and JWT tokens
- **Rationale**: Separates internal database concerns from external API contracts

### Reactive Architecture
- **R2DBC**: Non-blocking database access
- **WebFlux**: Reactive web framework
- **Reactor**: Mono and Flux for async operations

### JWT Authentication Flow
1. User registers/logs in → receives JWT token
2. Client includes token in Authorization header: `Bearer <token>`
3. JwtAuthenticationWebFilter validates token
4. Extracts UUID from token's sub claim
5. Loads user details and sets authentication in ReactiveSecurityContext
6. SecurityContext principal is UUID object (not String, not Long)

## Testing

Run tests with H2 in-memory database:
```bash
./mvnw test
```

Test configuration uses H2 R2DBC dialect for fast, isolated tests.

## License

This project is provided as-is for educational purposes.

### Remove mac metadata files

```bash
find . -type f -name ".DS_Store" -delete
```