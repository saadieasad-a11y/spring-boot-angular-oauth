# QUICKSTART_WITH_LOCATION.md
# Quick Start Guide - With Location & Expiry Features

## 🚀 Features Added

✅ **Location Management**: Users belong to specific hospitals/clinics
✅ **Account Expiry**: Set expiry dates for user accounts (licenses, trials)
✅ **Location Service**: Full CRUD operations for locations
✅ **Multi-tenant Support**: One system, multiple locations

---

## 📋 Database Schema Changes

### New Locations Table
```sql
CREATE TABLE locations (
    id BIGSERIAL PRIMARY KEY,
    location_name VARCHAR(255) UNIQUE NOT NULL,
    address VARCHAR(500),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    phone_number VARCHAR(20),
    email VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Updated Users Table
```sql
ALTER TABLE users ADD COLUMN location_id BIGINT REFERENCES locations(id);
ALTER TABLE users ADD COLUMN expiry_date TIMESTAMP;
```

---

## 🔧 Setup Steps

### Step 1: Database Setup

```bash
# Login to PostgreSQL
sudo -u postgres psql

# Create database and user
CREATE DATABASE oauth_db ENCODING 'UTF8';
CREATE USER oauth_user WITH PASSWORD 'secure_password';
ALTER ROLE oauth_user CREATEDB;
GRANT ALL PRIVILEGES ON DATABASE oauth_db TO oauth_user;

\c oauth_db
GRANT ALL PRIVILEGES ON SCHEMA public TO oauth_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO oauth_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO oauth_user;

\q
```

### Step 2: Create Tables

```bash
psql -U oauth_user -d oauth_db -h localhost
```

```sql
-- Locations Table
CREATE TABLE locations (
    id BIGSERIAL PRIMARY KEY,
    location_name VARCHAR(255) UNIQUE NOT NULL,
    address VARCHAR(500),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    phone_number VARCHAR(20),
    email VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_locations_name ON locations(location_name);

-- Users Table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    provider VARCHAR(50),
    provider_id VARCHAR(255) UNIQUE,
    profile_picture VARCHAR(500),
    location_id BIGINT REFERENCES locations(id) ON DELETE SET NULL,
    expiry_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_provider_id ON users(provider_id);
CREATE INDEX idx_users_location_id ON users(location_id);
CREATE INDEX idx_users_expiry_date ON users(expiry_date);

-- OAuth Tokens Table
CREATE TABLE oauth_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider VARCHAR(50),
    access_token TEXT,
    refresh_token TEXT,
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_oauth_tokens_user_id ON oauth_tokens(user_id);
```

### Step 3: Insert Sample Data

```sql
-- Insert Locations
INSERT INTO locations (location_name, address, city, state, country, postal_code, phone_number, email) VALUES
('Apollo Hospital', '123 Medical Plaza', 'New York', 'NY', 'USA', '10001', '+1-555-0100', 'contact@apollo.com'),
('City Clinic', '456 Care Center', 'Boston', 'MA', 'USA', '02101', '+1-555-0101', 'contact@cityclinic.com'),
('HealthFirst Medical', '789 Wellness Ave', 'Los Angeles', 'CA', 'USA', '90001', '+1-555-0102', 'contact@healthfirst.com');

-- Insert Sample Users
INSERT INTO users (email, first_name, last_name, provider, provider_id, location_id, expiry_date) VALUES
('dr.smith@hospital.com', 'John', 'Smith', 'GOOGLE', 'google-123', 1, '2025-12-31'),
('nurse.jane@hospital.com', 'Jane', 'Doe', 'MICROSOFT', 'microsoft-456', 1, '2025-06-30'),
('admin@clinic.com', 'Bob', 'Johnson', 'GOOGLE', 'google-789', 2, NULL);

\q
```

### Step 4: Backend Configuration

Edit `backend/src/main/resources/application.properties`:

```properties
server.port=8080
server.servlet.context-path=/api

# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/oauth_db
spring.datasource.username=oauth_user
spring.datasource.password=secure_password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# OAuth2
spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET
spring.security.oauth2.client.registration.google.scope=profile,email
spring.security.oauth2.client.registration.google.redirect-uri=http://localhost:8080/api/login/oauth2/code/google

spring.security.oauth2.client.registration.azure.client-id=YOUR_MICROSOFT_CLIENT_ID
spring.security.oauth2.client.registration.azure.client-secret=YOUR_MICROSOFT_CLIENT_SECRET
spring.security.oauth2.client.registration.azure.scope=openid,profile,email
spring.security.oauth2.client.registration.azure.redirect-uri=http://localhost:8080/api/login/oauth2/code/azure
spring.security.oauth2.client.provider.azure.authorization-uri=https://login.microsoftonline.com/common/oauth2/v2.0/authorize
spring.security.oauth2.client.provider.azure.token-uri=https://login.microsoftonline.com/common/oauth2/v2.0/token
spring.security.oauth2.client.provider.azure.user-info-uri=https://graph.microsoft.com/oidc/userinfo
spring.security.oauth2.client.provider.azure.user-name-attribute=preferred_username

# JWT
jwt.secret=your_super_secret_jwt_key_minimum_32_characters_long
jwt.expiration=86400000

# Logging
logging.level.root=INFO
logging.level.com.oauth=DEBUG
```

### Step 5: Add PostgreSQL Driver to pom.xml

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.6.0</version>
    <scope>runtime</scope>
</dependency>
```

### Step 6: Start Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

✅ Backend running on: `http://localhost:8080/api`

### Step 7: Start Frontend

```bash
cd frontend
npm install
npm start
```

✅ Frontend running on: `http://localhost:4200`

---

## 📍 API Endpoints - Location Management

### Get All Locations
```bash
curl http://localhost:8080/api/locations
```

### Create Location
```bash
curl -X POST http://localhost:8080/api/locations \
  -H "Content-Type: application/json" \
  -d '{
    "locationName": "New Hospital",
    "address": "999 Medical St",
    "city": "Chicago",
    "state": "IL",
    "country": "USA",
    "postalCode": "60601",
    "phoneNumber": "+1-555-0103",
    "email": "contact@newhospital.com"
  }'
```

### Get Location by ID
```bash
curl http://localhost:8080/api/locations/1
```

### Update Location
```bash
curl -X PUT http://localhost:8080/api/locations/1 \
  -H "Content-Type: application/json" \
  -d '{
    "locationName": "Apollo Hospital Updated",
    "address": "123 Medical Plaza, Suite 100",
    "city": "New York",
    "state": "NY",
    "country": "USA",
    "postalCode": "10001",
    "phoneNumber": "+1-555-0100",
    "email": "contact@apollo.com"
  }'
```

### Delete Location
```bash
curl -X DELETE http://localhost:8080/api/locations/1
```

---

## 🎯 User Assignment & Expiry Flow

### Step 1: User Logins with Location

Frontend passes locationId during OAuth:
```javascript
// Login with location
window.location.href = `http://localhost:8080/api/oauth2/authorization/google?locationId=1`;
```

### Step 2: Get User Info (with Location & Expiry)

```bash
curl http://localhost:8080/api/auth/me
```

Response:
```json
{
  "success": true,
  "message": "User retrieved successfully",
  "user": {
    "id": 1,
    "email": "user@hospital.com",
    "firstName": "John",
    "lastName": "Smith",
    "provider": "GOOGLE",
    "profilePicture": "https://...",
    "locationId": 1,
    "locationName": "Apollo Hospital",
    "expiryDate": "2025-12-31T23:59:59"
  }
}
```

### Step 3: Handle Expiry

If account is expired:
```json
{
  "success": false,
  "message": "User account has expired"
}
```

---

## 📊 Database Queries

### Get All Users by Location
```sql
SELECT u.*, l.location_name 
FROM users u
JOIN locations l ON u.location_id = l.id
WHERE l.id = 1;
```

### Get Active Users (Not Expired)
```sql
SELECT * FROM users
WHERE expiry_date IS NULL OR expiry_date > CURRENT_TIMESTAMP;
```

### Get Users Expiring Soon (Next 30 Days)
```sql
SELECT * FROM users
WHERE expiry_date BETWEEN CURRENT_TIMESTAMP AND CURRENT_TIMESTAMP + INTERVAL '30 days';
```

---

## 🧪 Testing

### Test Location API

1. **Get Locations**: `GET /api/locations`
2. **Create Location**: `POST /api/locations` with JSON body
3. **Update Location**: `PUT /api/locations/1` with JSON body
4. **Delete Location**: `DELETE /api/locations/1`

### Test User Expiry

1. Login with Google/Microsoft
2. Check user info: `GET /api/auth/me`
3. Verify `expiryDate` in response
4. If expired: Access denied

---

## 📚 Complete Documentation

For complete API documentation, see: `API_DOCUMENTATION.md`
For database schema details, see: `DATABASE_SCHEMA.md`

---

✨ **Setup Complete!** Your HMIS OAuth system with location and expiry management is ready! 🎉
