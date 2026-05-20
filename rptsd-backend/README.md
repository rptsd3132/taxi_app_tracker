# rptsd-backend

Express + Prisma + PostgreSQL REST API backend for the RPTSD mobile app.

## Setup

```bash
npm install
cp .env.example .env      # edit DATABASE_URL, JWT_SECRET, JWT_ADMIN_SECRET
npx prisma migrate dev
npx prisma db seed
npm run dev               # starts on http://localhost:3001
```

---

## API Examples

Base URL: `http://localhost:3001/api`

All responses follow this format:

```json
{ "success": true,  "data": {...}, "message": "..." }
{ "success": false, "error": "...", "code": "..."  }
```

---

### Auth — User

#### POST /api/auth/register

```bash
curl -X POST http://localhost:3001/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Driver",
    "email": "driver@example.com",
    "phone": "0771234567",
    "password": "Password1",
    "deviceId": "device-uuid-here"
  }'
```

**201 Success**
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "name": "Test Driver",
    "email": "driver@example.com",
    "phone": "0771234567",
    "subscriptionStatus": "TRIAL",
    "subscriptionEndDate": "2026-05-27T00:00:00.000Z",
    "deviceId": "device-uuid-here",
    "isSuspended": false,
    "createdAt": "2026-05-20T07:00:00.000Z",
    "updatedAt": "2026-05-20T07:00:00.000Z"
  },
  "message": "Registration successful"
}
```

**409 Duplicate email**
```json
{ "success": false, "error": "Email already registered", "code": "EMAIL_TAKEN" }
```

**422 Validation error**
```json
{ "success": false, "error": "Invalid email format", "code": "VALIDATION_ERROR" }
```

---

#### POST /api/auth/login

```bash
curl -X POST http://localhost:3001/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "driver@example.com",
    "password": "Password1",
    "deviceId": "device-uuid-here"
  }'
```

**200 Success**
```json
{
  "success": true,
  "data": {
    "user": {
      "id": "uuid",
      "name": "Test Driver",
      "email": "driver@example.com",
      "phone": "0771234567",
      "subscriptionStatus": "TRIAL",
      "subscriptionEndDate": "2026-05-27T00:00:00.000Z"
    },
    "token": "eyJhbGci..."
  },
  "message": "Login successful"
}
```

**401 Wrong credentials**
```json
{ "success": false, "error": "Invalid email or password", "code": "INVALID_CREDENTIALS" }
```

**403 Device conflict**
```json
{ "success": false, "error": "Already logged in on another device", "code": "DEVICE_CONFLICT" }
```

---

#### GET /api/auth/me  *(requires token)*

```bash
curl http://localhost:3001/api/auth/me \
  -H "Authorization: Bearer <token>"
```

**200 Success**
```json
{
  "success": true,
  "data": { "id": "uuid", "name": "Test Driver", "email": "driver@example.com", ... },
  "message": "User fetched"
}
```

**401 No/invalid token**
```json
{ "success": false, "error": "Authorization token required", "code": "UNAUTHORIZED" }
```

---

#### POST /api/auth/logout  *(requires token)*

```bash
curl -X POST http://localhost:3001/api/auth/logout \
  -H "Authorization: Bearer <token>"
```

**200 Success**
```json
{ "success": true, "data": null, "message": "Logged out successfully" }
```

---

### Auth — Admin

#### POST /api/auth/admin/login

```bash
curl -X POST http://localhost:3001/api/auth/admin/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@rptsd.com",
    "password": "Admin@123"
  }'
```

**200 Success**
```json
{
  "success": true,
  "data": {
    "admin": {
      "id": "uuid",
      "email": "admin@rptsd.com",
      "name": "Super Admin",
      "role": "SUPER_ADMIN",
      "createdAt": "2026-05-20T07:05:41.831Z"
    },
    "token": "eyJhbGci..."
  },
  "message": "Admin login successful"
}
```

**401 Wrong credentials**
```json
{ "success": false, "error": "Invalid email or password", "code": "INVALID_CREDENTIALS" }
```

---

## Error Codes

| Code | Meaning |
|------|---------|
| `VALIDATION_ERROR` | Request body failed schema validation |
| `EMAIL_TAKEN` | Email already registered |
| `INVALID_CREDENTIALS` | Wrong email or password |
| `DEVICE_CONFLICT` | Account is logged in on a different device |
| `ACCOUNT_SUSPENDED` | Account has been suspended |
| `UNAUTHORIZED` | Missing or invalid JWT |
| `NOT_FOUND` | Resource not found |
| `SERVER_ERROR` | Unexpected server error |
