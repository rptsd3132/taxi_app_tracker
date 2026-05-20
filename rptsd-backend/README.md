# rptsd-backend

Express · TypeScript · Prisma 7 · PostgreSQL REST API for the RPTSD driver app.

## Setup

```bash
npm install
cp .env.example .env      # fill DATABASE_URL, JWT_SECRET, JWT_ADMIN_SECRET
npx prisma migrate dev
npx prisma db seed        # creates admin@rptsd.com / Admin@123
npm run dev               # http://localhost:3001
```

---

## API Reference

Base URL: `http://localhost:3001/api`

All responses follow this envelope:
```json
{ "success": true,  "data": {...},  "message": "..." }
{ "success": false, "error": "...", "code": "..."   }
```

### Auth legend
| Symbol | Meaning |
|--------|---------|
| —      | No authentication required |
| U      | `Authorization: Bearer <user-token>` |
| A      | `Authorization: Bearer <admin-token>` |

---

### Full Endpoint Table

| Method | Endpoint | Auth | Description |
|--------|----------|:----:|-------------|
| POST | `/api/auth/register` | — | Register new driver account |
| POST | `/api/auth/login` | — | Driver login (returns JWT, binds deviceId) |
| POST | `/api/auth/logout` | U | Logout (clears deviceId) |
| GET | `/api/auth/me` | U | Get current user profile |
| POST | `/api/auth/admin/login` | — | Admin login (returns admin JWT) |
| GET | `/api/subscription/status` | U | Get subscription status, endDate, daysRemaining |
| POST | `/api/subscription/create-payment` | U | Create payment record, get mock paymentUrl |
| POST | `/api/subscription/confirm-payment` | U | Confirm payment → set ACTIVE, extend 30 days |
| POST | `/api/stats/sync` | U | Upsert daily trip stats (idempotent by userId+date) |
| GET | `/api/stats/my?days=30` | U | Get my stats for last N days (max 365) |
| POST | `/api/comments` | U | Submit a comment/feedback |
| GET | `/api/comments/my` | U | Get my comments with admin replies |
| GET | `/api/admin/dashboard` | A | KPI summary, recent payments & comments |
| GET | `/api/admin/users` | A | Paginated user list with aggregated stats |
| GET | `/api/admin/users/:id` | A | Full user detail (30d stats, payments, comments) |
| POST | `/api/admin/users/:id/suspend` | A | Suspend or unsuspend a user |
| POST | `/api/admin/users/:id/extend-subscription` | A | Add N days to subscription |
| GET | `/api/admin/payments` | A | Paginated payment list with user info |
| GET | `/api/admin/comments` | A | Paginated comment list with user info |
| POST | `/api/admin/comments/:id/reply` | A | Post admin reply to a comment |

---

## API Examples

### Auth

#### POST /api/auth/register
```bash
curl -X POST http://localhost:3001/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Driver","email":"driver@example.com","phone":"0771234567","password":"Password1","deviceId":"device-uuid"}'
```
**201** — `{ data: { id, name, email, subscriptionStatus, subscriptionEndDate, ... } }`
**409** `EMAIL_TAKEN` | **422** `VALIDATION_ERROR`

#### POST /api/auth/login
```bash
curl -X POST http://localhost:3001/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"driver@example.com","password":"Password1","deviceId":"device-uuid"}'
```
**200** — `{ data: { user: { id, name, email, subscriptionStatus, subscriptionEndDate }, token } }`
**401** `INVALID_CREDENTIALS` | **403** `DEVICE_CONFLICT` | **403** `ACCOUNT_SUSPENDED`

#### POST /api/auth/logout  *(U)*
```bash
curl -X POST http://localhost:3001/api/auth/logout \
  -H "Authorization: Bearer <token>"
```

#### POST /api/auth/admin/login
```bash
curl -X POST http://localhost:3001/api/auth/admin/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@rptsd.com","password":"Admin@123"}'
```
**200** — `{ data: { admin: { id, name, email, role }, token } }`

---

### Subscription

#### GET /api/subscription/status  *(U)*
```bash
curl http://localhost:3001/api/subscription/status \
  -H "Authorization: Bearer <token>"
```
```json
{ "data": { "status": "ACTIVE", "endDate": "2026-06-19T...", "daysRemaining": 30, "isActive": true } }
```

#### POST /api/subscription/create-payment  *(U)*
```bash
curl -X POST http://localhost:3001/api/subscription/create-payment \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"amount":1500,"paymentMethod":"CARD"}'
```
```json
{ "data": { "transactionId": "uuid", "paymentUrl": "https://pay.rptsd.com/checkout/uuid" } }
```

#### POST /api/subscription/confirm-payment  *(U)*
```bash
curl -X POST http://localhost:3001/api/subscription/confirm-payment \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"transactionId":"uuid-from-create"}'
```
```json
{ "data": { "subscriptionStatus": "ACTIVE", "subscriptionEndDate": "2026-07-19T..." } }
```

---

### Stats

#### POST /api/stats/sync  *(U)*  — idempotent by userId + date
```bash
curl -X POST http://localhost:3001/api/stats/sync \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"date":"2026-05-20","ridesAccepted":12,"ridesSkipped":3,"totalEarnings":4500.50}'
```

#### GET /api/stats/my?days=30  *(U)*
```bash
curl "http://localhost:3001/api/stats/my?days=7" \
  -H "Authorization: Bearer <token>"
```
Returns array sorted newest-first.

---

### Comments

#### POST /api/comments  *(U)*
```bash
curl -X POST http://localhost:3001/api/comments \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"message":"The app crashes on startup sometimes."}'
```

#### GET /api/comments/my  *(U)*
```bash
curl http://localhost:3001/api/comments/my \
  -H "Authorization: Bearer <token>"
```
Returns comments newest-first, includes `adminReply`, `repliedAt`, `repliedBy`.

---

### Admin

#### GET /api/admin/dashboard  *(A)*
```bash
curl http://localhost:3001/api/admin/dashboard \
  -H "Authorization: Bearer <admin-token>"
```
```json
{
  "data": {
    "totalUsers": 42,
    "activeSubscriptions": 30,
    "expiredSubscriptions": 5,
    "revenueThisMonth": 45000,
    "newUsersThisWeek": 7,
    "recentPayments": [...],
    "recentComments": [...]
  }
}
```

#### GET /api/admin/users  *(A)*
Query params: `page`, `limit`, `status` (TRIAL|ACTIVE|EXPIRED|SUSPENDED), `search`
```bash
curl "http://localhost:3001/api/admin/users?page=1&limit=20&status=ACTIVE&search=john" \
  -H "Authorization: Bearer <admin-token>"
```
```json
{ "data": { "items": [{ "id","name","email","subscriptionStatus","totalRides","totalEarnings" }], "total","page","totalPages" } }
```

#### GET /api/admin/users/:id  *(A)*
Full user with last 30d `tripStats`, all `payments`, all `comments`.

#### POST /api/admin/users/:id/suspend  *(A)*
```bash
curl -X POST http://localhost:3001/api/admin/users/:id/suspend \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json" \
  -d '{"isSuspended":true}'
```

#### POST /api/admin/users/:id/extend-subscription  *(A)*
```bash
curl -X POST http://localhost:3001/api/admin/users/:id/extend-subscription \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json" \
  -d '{"days":30}'
```

#### GET /api/admin/payments  *(A)*
Query params: `page`, `limit`, `status` (PENDING|SUCCESS|FAILED|REFUNDED), `search` (user email)
```bash
curl "http://localhost:3001/api/admin/payments?status=SUCCESS" \
  -H "Authorization: Bearer <admin-token>"
```

#### GET /api/admin/comments  *(A)*
Query params: `page`, `limit`, `status` (NEW|READ|REPLIED)
```bash
curl "http://localhost:3001/api/admin/comments?status=NEW" \
  -H "Authorization: Bearer <admin-token>"
```

#### POST /api/admin/comments/:id/reply  *(A)*
```bash
curl -X POST http://localhost:3001/api/admin/comments/:id/reply \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json" \
  -d '{"reply":"Fixed in v2.1 — please update the app."}'
```

---

## Error Codes

| Code | HTTP | Meaning |
|------|------|---------|
| `VALIDATION_ERROR` | 422 | Request body failed Zod schema validation |
| `EMAIL_TAKEN` | 409 | Email already registered |
| `INVALID_CREDENTIALS` | 401 | Wrong email or password |
| `DEVICE_CONFLICT` | 403 | Account is active on a different device |
| `ACCOUNT_SUSPENDED` | 403 | Account suspended by admin |
| `UNAUTHORIZED` | 401 | Missing or invalid JWT token |
| `PAYMENT_NOT_FOUND` | 404 | Transaction ID not found |
| `ALREADY_PROCESSED` | 409 | Payment already confirmed |
| `FORBIDDEN` | 403 | Resource belongs to another user |
| `NOT_FOUND` | 404 | Resource not found |
| `SERVER_ERROR` | 500 | Unexpected server error |
