# PostgreSQL Database Setup Guide for Ubuntu

## Prerequisites
- Ubuntu 20.04 or higher
- sudo access

## Step 1: Install PostgreSQL

```bash
# Update package list
sudo apt-get update

# Install PostgreSQL and utilities
sudo apt-get install postgresql postgresql-contrib postgresql-client

# Verify installation
psql --version
```

## Step 2: Start PostgreSQL Service

```bash
# Start PostgreSQL
sudo systemctl start postgresql

# Enable it to start on boot
sudo systemctl enable postgresql

# Check status
sudo systemctl status postgresql
```

## Step 3: Create Database User

```bash
# Login as postgres user
sudo -u postgres psql

# Create new user
CREATE USER oauth_user WITH PASSWORD 'secure_password_here';

# Create database
CREATE DATABASE oauth_db ENCODING 'UTF8';

# Grant privileges
ALTER ROLE oauth_user CREATEDB;
GRANT ALL PRIVILEGES ON DATABASE oauth_db TO oauth_user;

# Connect to database
\c oauth_db

# Grant schema privileges
GRANT ALL PRIVILEGES ON SCHEMA public TO oauth_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO oauth_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO oauth_user;

# Exit
\q
```

## Step 4: Create Tables

```bash
# Login as oauth_user
psql -U oauth_user -d oauth_db -h localhost
```

```sql
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
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Indexes for Users
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

-- Create Index for OAuth Tokens
CREATE INDEX idx_oauth_tokens_user_id ON oauth_tokens(user_id);

-- Verify tables
\dt
\d users
\d oauth_tokens
```

## Step 5: Test Connection

```bash
# Test connection from your user (not root)
psql -U oauth_user -d oauth_db -h localhost -p 5432

# If prompt appears for password, enter: secure_password_here

# Test query
SELECT * FROM users;

# Exit
\q
```

## Step 6: Configure Backend Connection

Edit `backend/src/main/resources/application.properties`:

```properties
# PostgreSQL Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/oauth_db
spring.datasource.username=oauth_user
spring.datasource.password=secure_password_here
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration for PostgreSQL
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

## Step 7: Update pom.xml

Add PostgreSQL dependency to `backend/pom.xml`:

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.6.0</version>
    <scope>runtime</scope>
</dependency>
```

## 🔧 Useful PostgreSQL Commands

```bash
# Connect to PostgreSQL
sudo -u postgres psql

# Connect as oauth_user
psql -U oauth_user -d oauth_db

# List all databases
\l

# List all tables
\dt

# Describe a table
\d users

# View table contents
SELECT * FROM users;
SELECT * FROM oauth_tokens;

# Check user permissions
\du

# Drop database
DROP DATABASE oauth_db;

# Drop user
DROP USER oauth_user;

# Change user password
ALTER USER oauth_user WITH PASSWORD 'new_password';
```

## 📊 Backup and Restore

```bash
# Backup entire database
pg_dump -U oauth_user -d oauth_db > oauth_db_backup.sql

# Backup with compression
pg_dump -U oauth_user -d oauth_db -Fc > oauth_db_backup.dump

# Restore database
psql -U oauth_user -d oauth_db < oauth_db_backup.sql

# Restore from compressed backup
pg_restore -U oauth_user -d oauth_db oauth_db_backup.dump
```

## 🐛 Troubleshooting

### Issue: "FATAL: Ident authentication failed"

**Solution:**
```bash
# Edit PostgreSQL authentication configuration
sudo nano /etc/postgresql/14/main/pg_hba.conf

# Change this line (usually around line 90):
# local   all             all                                     ident
# To:
# local   all             all                                     md5

# Save and exit (Ctrl+X, then Y, then Enter)

# Restart PostgreSQL
sudo systemctl restart postgresql
```

### Issue: "Connection refused"

**Solution:**
```bash
# Check if PostgreSQL is running
sudo systemctl status postgresql

# Start if not running
sudo systemctl start postgresql

# Check which port PostgreSQL is listening on
sudo netstat -tulpn | grep postgres
```

### Issue: "Permission denied for schema public"

**Solution:**
```bash
sudo -u postgres psql
GRANT ALL PRIVILEGES ON SCHEMA public TO oauth_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO oauth_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO oauth_user;
\q
```

### Issue: "Password authentication failed"

**Solution:**
```bash
# Change the password
sudo -u postgres psql
ALTER USER oauth_user WITH PASSWORD 'new_password';
\q

# Update application.properties with new password
```

## ✅ Verify Setup

```bash
# Test backend can connect
cd backend
mvn spring-boot:run

# Check logs for successful connection:
# "Initialized JPA EntityManagerFactory for persistence unit 'default'"
```

## 📝 Sample Data (Optional)

```bash
psql -U oauth_user -d oauth_db
```

```sql
INSERT INTO users (email, first_name, last_name, provider, provider_id) VALUES
('test@gmail.com', 'Test', 'User', 'GOOGLE', 'google-123'),
('user@outlook.com', 'John', 'Doe', 'MICROSOFT', 'microsoft-456');

SELECT * FROM users;
```

---

**Setup Complete! 🎉**
