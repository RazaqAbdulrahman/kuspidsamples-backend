# Kuspid Samples Backend API

A secure, full-featured REST API built with Spring Boot 3, featuring JWT authentication, file upload with Cloudinary, WebSocket support, and SQLite database.

## 🚀 Features

- ✅ **JWT Authentication** - Secure token-based authentication with refresh tokens
- ✅ **User Management** - Complete CRUD operations for user profiles
- ✅ **File Upload** - Image upload and management with Cloudinary
- ✅ **Sample Management** - CRUD operations for samples with image support
- ✅ **WebSocket Support** - Real-time communication capabilities
- ✅ **Role-Based Access Control** - Admin and User roles
- ✅ **Global Exception Handling** - Consistent error responses
- ✅ **Request Validation** - Input validation with Jakarta Validation
- ✅ **CORS Configuration** - Configurable cross-origin support
- ✅ **SQLite Database** - Lightweight, embedded database
- ✅ **Pagination** - Efficient data retrieval for large datasets

## 🛠️ Technologies

- Java 17
- Spring Boot 3.3.2
- Spring Security
- Spring Data JPA
- JWT (JSON Web Tokens)
- SQLite
- Cloudinary
- WebSocket
- Lombok
- Maven

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- Cloudinary account (for file uploads)

## ⚙️ Setup

### 1. Clone the repository

```bash
git clone <your-repo-url>
cd kuspidsamples-backend
```

### 2. Configure Environment Variables

Create a `.env` file in the root directory:

```bash
# JWT Configuration
JWT_SECRET=your-super-secret-jwt-key-here-at-least-256-bits

# Cloudinary Configuration
CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_API_KEY=your-api-key
CLOUDINARY_API_SECRET=your-api-secret
```

**Important**: Generate a strong JWT secret (minimum 32 characters). You can use:
```bash
openssl rand -base64 32
```

### 3. Build the project

```bash
mvn clean install
```

### 4. Run the application

```bash
mvn spring-boot:run
```

The API will start on `http://localhost:8080`

## 📚 API Documentation

### Base URL
```
http://localhost:8080/api
```

### Authentication Endpoints

#### Register
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "password123",
  "fullName": "John Doe"
}
```

**Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "uuid-refresh-token",
    "tokenType": "Bearer",
    "userId": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "role": "ROLE_USER"
  }
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "usernameOrEmail": "john_doe",
  "password": "password123"
}
```

#### Refresh Token
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "your-refresh-token"
}
```

#### Logout
```http
POST /api/auth/logout
Content-Type: application/json

{
  "refreshToken": "your-refresh-token"
}
```

### User Endpoints

All user endpoints require authentication. Include the JWT token in the Authorization header:
```
Authorization: Bearer your-access-token
```

#### Get Current User Profile
```http
GET /api/users/me
```

#### Get User by ID
```http
GET /api/users/{id}
```

#### Get User by Username
```http
GET /api/users/username/{username}
```

#### Get All Users (Admin only)
```http
GET /api/users
```

#### Update User Profile
```http
PATCH /api/users/{id}
Content-Type: application/json

{
  "fullName": "John Updated Doe"
}
```

#### Delete User (Admin only)
```http
DELETE /api/users/{id}
```

### Sample Endpoints

#### Create Sample (with optional image)
```http
POST /api/samples
Content-Type: multipart/form-data
Authorization: Bearer your-access-token

name=My Sample
description=Sample description
image=<file>
```

#### Get Sample by ID
```http
GET /api/samples/{id}
```

#### Get All Samples (Paginated)
```http
GET /api/samples?page=0&size=20
```

#### Get Current User's Samples
```http
GET /api/samples/my-samples
Authorization: Bearer your-access-token
```

#### Get Samples by User ID (Paginated)
```http
GET /api/samples/user/{userId}?page=0&size=20
```

#### Update Sample
```http
PUT /api/samples/{id}
Content-Type: multipart/form-data
Authorization: Bearer your-access-token

name=Updated Name
description=Updated description
image=<file>
```

#### Delete Sample
```http
DELETE /api/samples/{id}
Authorization: Bearer your-access-token
```

### Health Check
```http
GET /health

Response:
{
  "status": "UP",
  "timestamp": "2024-01-01T12:00:00",
  "service": "Kuspid Samples API",
  "version": "1.0.0"
}
```

## 🔐 Security

- Passwords are hashed using BCrypt
- JWT tokens expire after 24 hours
- Refresh tokens expire after 7 days
- Account locking after 5 failed login attempts
- Role-based access control (ROLE_USER, ROLE_ADMIN)

## 🗄️ Database

The application uses SQLite with the following main tables:
- `users` - User accounts and profiles
- `samples` - Sample data with image references
- `refresh_tokens` - JWT refresh tokens

Database file location: `./data/kuspid_samples.db`

## 🔌 WebSocket

WebSocket endpoint is available at: `ws://localhost:8080/ws`

Use SockJS for fallback support. Topics:
- `/topic/*` - Broadcast to all connected clients
- `/queue/*` - Point-to-point messaging
- `/user/*` - User-specific messages

## 📁 Project Structure

```
src/main/java/com/kuspidsamples/
├── config/              # Configuration classes
│   ├── CloudinaryConfig.java
│   ├── CorsConfig.java
│   ├── JpaConfig.java
│   ├── SecurityConfig.java
│   ├── SQLiteDialect.java
│   └── WebSocketConfig.java
├── controller/          # REST controllers
│   ├── AuthController.java
│   ├── HealthController.java
│   ├── SampleController.java
│   └── UserController.java
├── dto/                 # Data Transfer Objects
│   ├── request/
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   └── SampleRequest.java
│   └── response/
│       ├── AuthResponse.java
│       ├── SampleResponse.java
│       └── UserResponse.java
├── entity/              # JPA entities
│   ├── BaseEntity.java
│   ├── RefreshToken.java
│   ├── Role.java
│   ├── Sample.java
│   └── User.java
├── exception/           # Custom exceptions and handlers
│   ├── BadRequestException.java
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── UnauthorizedException.java
├── repository/          # JPA repositories
│   ├── RefreshTokenRepository.java
│   ├── SampleRepository.java
│   └── UserRepository.java
├── security/            # Security components
│   ├── JwtAuthenticationEntryPoint.java
│   ├── JwtAuthenticationFilter.java
│   ├── JwtTokenProvider.java
│   └── UserDetailsServiceImpl.java
├── service/             # Business logic
│   ├── AuthService.java
│   ├── CloudinaryService.java
│   ├── SampleService.java
│   └── UserService.java
├── util/                # Utility classes
│   └── Constants.java
└── KuspidsamplesApplication.java
```

## 🧪 Testing

Run tests with:
```bash
mvn test
```

## 📦 Building for Production

1. Update `.env` with production credentials
2. Change `spring.jpa.hibernate.ddl-auto` to `validate` in `application.properties`
3. Build the JAR:
```bash
mvn clean package -DskipTests
```
4. Run the JAR:
```bash
java -jar target/kuspidsamples-backend-0.0.1-SNAPSHOT.jar
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License.

## 🐛 Known Issues & Future Improvements

- [ ] Add password reset functionality
- [ ] Implement email verification
- [ ] Add rate limiting
- [ ] Implement caching with Redis
- [ ] Add comprehensive unit and integration tests
- [ ] Add API documentation with Swagger/OpenAPI
- [ ] Migrate to PostgreSQL for production
- [ ] Add Docker support

## 📞 Support

For issues and questions, please open an issue on GitHub.