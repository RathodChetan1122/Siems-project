# SIEMS — Local Development Setup Guide

## Prerequisites Checklist

| Tool | Version | Check |
|---|---|---|
| Java | 17+ | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| Node.js | 20+ | `node -version` |
| npm | 9+ | `npm -version` |
| Docker Desktop | Latest | `docker -version` |
| Docker Compose | v2+ | `docker compose version` |
| PostgreSQL | 16 (optional) | `psql -version` |
| Git | Any | `git -version` |

---

## Option A — Docker Compose (Fastest — 3 commands)

```bash
# Clone
git clone  && cd siems

# Configure
cp .env.example .env

# Launch everything
docker compose up -d --build
```

Wait ~90 seconds for the backend to start (Flyway runs migrations, DataSeeder seeds users).

Visit http://localhost:3000 and log in with `admin / Password123`.

---

## Option B — Manual Local Setup

### Step 1: PostgreSQL

```bash
# macOS (Homebrew)
brew install postgresql@16
brew services start postgresql@16

# Ubuntu/Debian
sudo apt install postgresql-16 postgresql-client-16
sudo systemctl start postgresql

# Windows — download from https://www.postgresql.org/download/windows/
```

```sql
-- Connect as postgres superuser and run:
CREATE USER siems_user WITH PASSWORD 'siems_pass';
CREATE DATABASE siems_db OWNER siems_user;
GRANT ALL PRIVILEGES ON DATABASE siems_db TO siems_user;
```

### Step 2: Backend

```bash
cd backend

# Set env vars (add to ~/.bashrc or ~/.zshrc for persistence)
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=siems_db
export DB_USER=siems_user
export DB_PASSWORD=siems_pass
export JWT_SECRET=local_dev_secret_key_min_32_characters_long

# Run (Flyway + DataSeeder run automatically)
mvn spring-boot:run

# OR build JAR and run
mvn clean package -DskipTests
java -jar target/siems-backend.jar
```

Backend starts on: http://localhost:8080
Swagger UI: http://localhost:8080/swagger-ui.html

### Step 3: Frontend

```bash
cd frontend

npm install
npm run dev
```

Frontend starts on: http://localhost:3000
Vite proxies `/api/*` → `http://localhost:8080/api/*` (configured in `vite.config.js`).

---

## Environment Variables Reference

| Variable | Default | Description |
|---|---|---|
| DB_HOST | localhost | PostgreSQL host |
| DB_PORT | 5432 | PostgreSQL port |
| DB_NAME | siems_db | Database name |
| DB_USER | siems_user | Database user |
| DB_PASSWORD | siems_pass | Database password |
| JWT_SECRET | (see .env.example) | HMAC-SHA256 signing key (min 32 chars) |
| JWT_EXPIRATION_MS | 900000 | Access token TTL (15 min) |
| JWT_REFRESH_EXPIRATION_MS | 604800000 | Refresh token TTL (7 days) |
| MAIL_ENABLED | false | Enable email notifications |
| FRONTEND_URL | http://localhost:3000 | Used in email links |
| VITE_API_BASE_URL | /api/v1 | Frontend API base URL |

---

## IDE Setup (IntelliJ IDEA)

1. Open → Select `backend/pom.xml` → Open as Project
2. Set SDK: File → Project Structure → SDK → Java 17
3. Enable annotation processing: Settings → Build → Compiler → Annotation Processors → Enable
4. Run `SiemsApplication.java` with env vars set in Run Configuration

## IDE Setup (VS Code — Frontend)

1. Open `frontend/` folder
2. Install extensions: ESLint, Tailwind CSS IntelliSense, Prettier
3. Run: `npm run dev` in integrated terminal

---

## Verify Everything Works

```bash
# 1. Backend health
curl http://localhost:8080/actuator/health
# Expected: {"status":"UP"}

# 2. Login
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Password123"}' | python3 -m json.tool

# 3. Get suppliers (using token from step 2)
curl http://localhost:8080/api/v1/suppliers \
  -H "Authorization: Bearer "

# 4. Frontend
open http://localhost:3000
```

Final Folder Tree
