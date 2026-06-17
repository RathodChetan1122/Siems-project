# SIEMS — All Commands Reference

## Docker Compose (Recommended)

```bash
# First time setup
cp .env.example .env
docker compose up -d --build

# Subsequent starts
docker compose up -d

# Stop (keep data)
docker compose down

# Full reset (delete DB data)
docker compose down -v && docker compose up -d --build

# View logs
docker compose logs -f
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f postgres

# Service status + health
docker compose ps
```

## Backend (Maven)

```bash
cd backend

# Run development server
mvn spring-boot:run

# Run with specific profile
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run

# Build production JAR
mvn clean package -DskipTests

# Run built JAR
java -jar target/siems-backend.jar

# Run all tests
mvn test

# Run tests with coverage
mvn test jacoco:report

# Run specific test class
mvn test -Dtest=ShipmentServiceImplTest

# Skip tests during build
mvn package -DskipTests

# Clean build artifacts
mvn clean
```

## Frontend (npm)

```bash
cd frontend

# Install dependencies
npm install

# Start development server (http://localhost:3000)
npm run dev

# Build for production
npm run build

# Preview production build locally
npm run preview

# Lint
npm run lint
```

## Database (PostgreSQL)

```bash
# Connect via Docker
docker exec -it siems-postgres psql -U siems_user -d siems_db

# Connect locally
psql -U siems_user -d siems_db -h localhost

# Useful queries
\dt                              # List all tables
\d inventory                     # Describe table
SELECT * FROM users;             # View seeded users
SELECT * FROM flyway_schema_history;  # Migration history

# Backup database
docker exec siems-postgres pg_dump -U siems_user siems_db > backup.sql

# Restore database
cat backup.sql | docker exec -i siems-postgres psql -U siems_user -d siems_db
```

## Git & CI/CD

```bash
# Push to trigger CI pipeline
git add .
git commit -m "feat: add new feature"
git push origin main

# Create a release tag
git tag v1.0.0
git push origin v1.0.0

# View GitHub Actions status
# https://github.com//siems/actions
```

## API Testing (curl)

```bash
BASE=http://localhost:8080/api/v1

# Register
curl -s -X POST $BASE/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@siems.com","password":"Test1234","roleName":"ADMIN"}'

# Login — save token
TOKEN=$(curl -s -X POST $BASE/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Password123"}' | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['accessToken'])")

# Use token
curl -s $BASE/suppliers -H "Authorization: Bearer $TOKEN" | python3 -m json.tool

# Create supplier
curl -s -X POST $BASE/suppliers \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Supplier","country":"India","contactEmail":"test@supplier.com","rating":4.2}'

# Create shipment
curl -s -X POST $BASE/shipments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"supplierId":1,"customerId":1,"warehouseId":1,"carrier":"Maersk","eta":"2026-08-01","items":[{"productId":1,"quantity":100,"unitPrice":4.50}]}'

# Update shipment status
curl -s -X PATCH $BASE/shipments/1/status \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"PACKED","location":"Hyderabad Warehouse","remarks":"Ready for dispatch"}'

# Track shipment
curl -s $BASE/shipments/1/tracking -H "Authorization: Bearer $TOKEN"

# Dashboard analytics
curl -s $BASE/analytics/dashboard -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```
