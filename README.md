# README.md
# Spring Boot + Angular OAuth2 Login Application

A complete full-stack application with Google and Microsoft OAuth2 authentication using PostgreSQL.

## 📋 Project Structure

```
spring-boot-angular-oauth/
├── backend/                          # Spring Boot Backend
│   ├── src/main/java/com/oauth/
│   │   ├── config/
│   │   │   └── SecurityConfig.java
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   └── HealthController.java
│   │   ├── dto/
│   │   │   ├── UserDTO.java
│   │   │   └── AuthResponse.java
│   │   ├── entity/
│   │   │   ├── User.java
│   │   │   └── OAuthToken.java
│   │   ├── repository/
│   │   │   ├── UserRepository.java
│   │   │   └── OAuthTokenRepository.java
│   │   ├── service/
│   │   │   ├── UserService.java
│   │   │   └── JwtTokenProvider.java
│   │   └── OAuthApplication.java
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
│
├── frontend/                         # Angular Frontend
│   ├── src/
│   │   ├── app/
│   │   │   ├── components/
│   │   │   │   ├── login/
│   │   │   │   │   ├── login.component.ts
│   │   │   │   │   ├── login.component.html
│   │   │   │   │   └── login.component.scss
│   │   │   │   └── dashboard/
│   │   │   │       ├── dashboard.component.ts
│   │   │   │       ├── dashboard.component.html
│   │   │   │       └── dashboard.component.scss
│   │   │   ├── services/
│   │   │   │   └── auth.service.ts
│   │   │   ├── app.component.ts
│   │   │   └── app.routes.ts
│   │   ├── index.html
│   │   ├── main.ts
│   │   └── styles.scss
│   ├── package.json
│   ├── angular.json
│   └── tsconfig.json
│
└── README.md
```

## 🗄️ PostgreSQL Database Schema

### PostgreSQL Setup on Ubuntu

```bash
# Install PostgreSQL
sudo apt-get update
sudo apt-get install postgresql postgresql-contrib

# Start PostgreSQL service
sudo systemctl start postgresql
sudo systemctl enable postgresql

# Access PostgreSQL
sudo -u postgres psql
```

### Create Database and Tables

```sql
-- Create Database
CREATE DATABASE oauth_db ENCODING 'UTF8';

-- Connect to the database
\c oauth_db

-- Create Users Table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    provider VARCHAR(50),
    provider_id VARCHAR(255) UNIQUE,
    profile_picture VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT email_unique UNIQUE (email)
);

-- Create Indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_provider_id ON users(provider_id);

-- Create OAuth Tokens Table
CREATE TABLE oauth_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider VARCHAR(50),
    access_token TEXT,
    refresh_token TEXT,
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Index
CREATE INDEX idx_oauth_tokens_user_id ON oauth_tokens(user_id);

-- Verify tables
\dt
\d users
\d oauth_tokens
```

### Create PostgreSQL User (Recommended)

```bash
# Access PostgreSQL as root
sudo -u postgres psql

# Create a new user
CREATE USER oauth_user WITH PASSWORD 'your_secure_password';

# Grant privileges
ALTER ROLE oauth_user CREATEDB;
GRANT ALL PRIVILEGES ON DATABASE oauth_db TO oauth_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO oauth_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO oauth_user;

# Exit
\q
```

## 🔧 Prerequisites

- **Java 17+**
- **Node.js 18+**
- **npm 9+**
- **Maven 3.8+**
- **PostgreSQL 12+**

## 📝 Setup Instructions

### Step 1: PostgreSQL Database Setup

```bash
# Login to PostgreSQL
sudo -u postgres psql

# Execute the SQL commands from the schema above
# Or use a SQL file:
psql -U postgres -d oauth_db -f database-setup.sql

# Verify connection
psql -U oauth_user -d oauth_db -h localhost
```

### Step 2: Update Backend Configuration

Edit `backend/src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/api

# PostgreSQL Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/oauth_db
spring.datasource.username=oauth_user
spring.datasource.password=your_secure_password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true

# OAuth2 - Google
spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET
spring.security.oauth2.client.registration.google.scope=profile,email
spring.security.oauth2.client.registration.google.redirect-uri=http://localhost:8080/api/login/oauth2/code/google

# OAuth2 - Microsoft
spring.security.oauth2.client.registration.azure.client-id=YOUR_MICROSOFT_CLIENT_ID
spring.security.oauth2.client.registration.azure.client-secret=YOUR_MICROSOFT_CLIENT_SECRET
spring.security.oauth2.client.registration.azure.scope=openid,profile,email
spring.security.oauth2.client.registration.azure.redirect-uri=http://localhost:8080/api/login/oauth2/code/azure
spring.security.oauth2.client.provider.azure.authorization-uri=https://login.microsoftonline.com/common/oauth2/v2.0/authorize
spring.security.oauth2.client.provider.azure.token-uri=https://login.microsoftonline.com/common/oauth2/v2.0/token
spring.security.oauth2.client.provider.azure.user-info-uri=https://graph.microsoft.com/oidc/userinfo
spring.security.oauth2.client.provider.azure.user-name-attribute=preferred_username

# JWT Configuration
jwt.secret=your_super_secret_jwt_key_minimum_32_characters_long
jwt.expiration=86400000

# Logging
logging.level.root=INFO
logging.level.com.oauth=DEBUG
```

### Step 3: Update pom.xml for PostgreSQL

Replace the MySQL driver with PostgreSQL:

```xml
<!-- Remove MySQL and add PostgreSQL -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.6.0</version>
    <scope>runtime</scope>
</dependency>
```

### Step 4: Build and Run Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend will run on: `http://localhost:8080/api`

### Step 5: Setup and Run Frontend

```bash
cd frontend
npm install
npm start
```

Frontend will run on: `http://localhost:4200`

## ✨ Features

✅ Google OAuth2 Login
✅ Microsoft OAuth2 Login
✅ User Registration (Auto-create on first login)
✅ JWT Token Management
✅ User Profile Display
✅ Logout Functionality
✅ Responsive Design
✅ Error Handling
✅ Loading States
✅ Secure Token Storage

## 📚 API Endpoints

### Authentication
- `GET /api/oauth2/authorization/google` - Initiate Google login
- `GET /api/oauth2/authorization/azure` - Initiate Microsoft login
- `GET /api/auth/google/callback` - Google OAuth callback
- `GET /api/auth/microsoft/callback` - Microsoft OAuth callback
- `GET /api/auth/me` - Get current user
- `POST /api/auth/logout` - Logout user
- `GET /api/health` - Health check

## 🔒 Security Features

- OAuth2 authentication with Google and Microsoft
- JWT token generation and validation
- CORS configuration
- Secure password handling
- Token expiration management
- Database-level foreign key constraints

## 🎨 Frontend Components

### Login Component
- Beautiful gradient background
- Google and Microsoft login buttons
- Error message display
- Loading state management
- Responsive design

### Dashboard Component
- User profile information display
- Profile picture with fallback avatar
- Provider badge
- Logout button
- Loading and error states

## 📦 Dependencies

### Backend
- Spring Boot 3.1.5
- Spring Security OAuth2
- Spring Data JPA
- JWT (io.jsonwebtoken)
- PostgreSQL Driver
- Lombok

### Frontend
- Angular 17
- RxJS 7.8
- Bootstrap 5.3
- TypeScript 5.2

## 🐛 Troubleshooting

### PostgreSQL Connection Issues

**Error: "FATAL: Ident authentication failed"**
```bash
# Edit PostgreSQL authentication file
sudo nano /etc/postgresql/14/main/pg_hba.conf

# Change 'ident' to 'md5' or 'password':
# local   all             all                                     md5

# Restart PostgreSQL
sudo systemctl restart postgresql
```

**Error: "Connection refused"**
```bash
# Check PostgreSQL is running
sudo systemctl status postgresql

# Start if stopped
sudo systemctl start postgresql

# Check port (default 5432)
sudo netstat -tulpn | grep postgres
```

### CORS Error
**Solution:** Ensure backend is running on `http://localhost:8080` and frontend on `http://localhost:4200`

### OAuth Login Not Working
**Solution:** Verify OAuth credentials are correct and redirect URIs are configured properly in Google/Microsoft console

### CSRF Token Error
**Solution:** CSRF is disabled in SecurityConfig for OAuth2. This is normal for OAuth flows.

## 📋 PostgreSQL Useful Commands

```bash
# Connect to PostgreSQL
psql -U oauth_user -d oauth_db -h localhost

# List databases
\l

# List tables
\dt

# Describe table
\d users

# Show all data
SELECT * FROM users;

# Drop database
DROP DATABASE oauth_db;

# Backup database
pg_dump -U oauth_user -d oauth_db > backup.sql

# Restore database
psql -U oauth_user -d oauth_db < backup.sql
```

## 🚀 Deployment

### Backend (Production)
1. Build JAR: `mvn clean package`
2. Run JAR: `java -jar target/oauth-backend-1.0.0.jar`

### Frontend (Production)
1. Build: `npm run build`
2. Deploy `dist/oauth-frontend` to web server

## 📞 Support

For issues or questions, create an issue on GitHub.

## 📄 License

MIT License - Feel free to use this project for learning and development.

## ✨ Version

- **v1.0.0** - Initial release with Google and Microsoft OAuth using PostgreSQL

---

**Happy Coding! 🎉**
