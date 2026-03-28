# API Testing with cURL

This file contains curl commands to test the Customer Support Hub API endpoints.

## Default Configuration

- **Base URL**: `http://localhost:8080`
- **Content-Type**: `application/json`
- **Authentication**: Bearer token (JWT)
- **Token TTL**: 3600 seconds (1 hour)

## Test Users

```
admin       / admin11
developer   / developer11
agent       / agent11
customer    / java11
```

---

## Health Check

Check if the service is running:

```bash
curl -X GET http://localhost:8080/alive
```

Response: `"alive"`

---

## Authentication (Login)

### Get Access Token

```bash
curl -X POST http://localhost:8080/oauth/token \
  -H "Content-Type: application/json" \
  -d '{"username":"agent","password":"agent11"}'
```

**Response:**
```json
{
  "access_token": "eyJ0eXAiOiJKV1QiLCJhbGc...",
  "token_type": "Bearer",
  "expires_in": 3600
}
```

### Extract Token (with jq)

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/oauth/token \
  -H "Content-Type: application/json" \
  -d '{"username":"agent","password":"agent11"}' | jq -r '.access_token')

echo $TOKEN
```

### Get Customer Token

```bash
curl -X POST http://localhost:8080/oauth/token \
  -H "Content-Type: application/json" \
  -d '{"username":"customer","password":"java11"}'
```

Or extract it for use in subsequent requests:

```bash
CUSTOMER_TOKEN=$(curl -s -X POST http://localhost:8080/oauth/token \
  -H "Content-Type: application/json" \
  -d '{"username":"customer","password":"java11"}' | jq -r '.access_token')

echo $CUSTOMER_TOKEN
```

---

## Ticket Management

### Create Ticket

**Endpoint**: `POST /tickets`  
**Auth**: Required (CUSTOMER role only)

```bash
curl -X POST http://localhost:8080/tickets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <access-token>" \
  -d '{
    "title": "Database connection issue",
    "description": "Cannot connect to production database"
  }'
```

**Note**: The ticket will be created by the authenticated customer. The `customerUsername` field is only used by agents creating tickets for customers.

**Request Body:**
```json
{
  "title": "string (required, max 255)",
  "description": "string (optional, max 4000)",
  "customerUsername": "string (optional, max 100)"
}
```

### Get Own Tickets

**Endpoint**: `GET /tickets`  
**Auth**: Required (any authenticated user)

```bash
curl -X GET http://localhost:8080/tickets \
  -H "Authorization: Bearer <access-token>"
```

### Search Agent Tickets

**Endpoint**: `GET /agent/tickets`  
**Auth**: Required (ADMIN or AGENT role)

```bash
curl -X GET "http://localhost:8080/agent/tickets?search=error" \
  -H "Authorization: Bearer <access-token>"
```

---

## User Profile Management

### Get Own Profile

**Endpoint**: `GET /profile/me`  
**Auth**: Required (any authenticated user)

```bash
curl -X GET http://localhost:8080/profile/me \
  -H "Authorization: Bearer <access-token>"
```

### Update Own Profile

**Endpoint**: `PUT /profile/me`  
**Auth**: Required (any authenticated user)

```bash
curl -X PUT http://localhost:8080/profile/me \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <access-token>" \
  -d '{
    "fullName": "Updated Name",
    "email": "newemail@example.com"
  }'
```

### Create Customer (Agent/Admin only)

**Endpoint**: `POST /agent/customers`  
**Auth**: Required (ADMIN or AGENT role)

```bash
curl -X POST http://localhost:8080/agent/customers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <access-token>" \
  -d '{
    "username": "john_doe",
    "fullName": "John Doe",
    "email": "john@example.com",
    "agentUsername": "agent"
  }'
```

### Get Customers List (Agent/Admin only)

**Endpoint**: `GET /agent/customers`  
**Auth**: Required (ADMIN or AGENT role)

```bash
curl -X GET http://localhost:8080/agent/customers \
  -H "Authorization: Bearer <access-token>"
```

---

## Complete Workflow Example

### 1. Login as Customer and Create Ticket (One Command)

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/oauth/token \
  -H "Content-Type: application/json" \
  -d '{"username":"customer","password":"java11"}' | jq -r '.access_token') && \
curl -X POST http://localhost:8080/tickets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "Database connection issue",
    "description": "Cannot connect to production database"
  }'
```

### 2. Login as Customer and Create Ticket

```bash
# 1. Login as customer
CUSTOMER_TOKEN=$(curl -s -X POST http://localhost:8080/oauth/token \
  -H "Content-Type: application/json" \
  -d '{"username":"customer","password":"java11"}' | jq -r '.access_token')

# 2. Create a ticket
curl -X POST http://localhost:8080/tickets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -d '{
    "title": "Unable to access account",
    "description": "I cannot login to my account"
  }'

# 3. Get your tickets
curl -X GET http://localhost:8080/tickets \
  -H "Authorization: Bearer $CUSTOMER_TOKEN"
```

---

## Role-Based Access Control

| Endpoint | ADMIN | AGENT | CUSTOMER | Public |
|----------|-------|-------|----------|--------|
| POST /oauth/token | ✓ | ✓ | ✓ | ✓ |
| GET /alive | ✓ | ✓ | ✓ | ✓ |
| GET /profile/me | ✓ | ✓ | ✓ | ✗ |
| PUT /profile/me | ✓ | ✓ | ✓ | ✗ |
| POST /tickets | ✗ | ✗ | ✓ | ✗ |
| GET /tickets | ✓ | ✓ | ✓ | ✗ |
| POST /agent/customers | ✓ | ✓ | ✗ | ✗ |
| GET /agent/customers | ✓ | ✓ | ✗ | ✗ |
| GET /agent/tickets | ✓ | ✓ | ✗ | ✗ |

---

## Security Configuration

- **JWT Algorithm**: HS256 (HMAC-SHA256)
- **Token TTL**: 3600 seconds (1 hour)
- **Authorization Header**: `Authorization: Bearer <token>`
- **Session Policy**: Stateless
- **CSRF Protection**: Disabled

---

## Troubleshooting

### Missing Token
```bash
# Error: Unauthorized
# Solution: Make sure to include the Authorization header with a valid token
```

### Invalid Token
```bash
# Error: Invalid token
# Solution: Get a new token using the login endpoint
```

### Expired Token
```bash
# Error: Token expired
# Solution: Tokens expire after 3600 seconds (1 hour). Get a new token.
```

### Insufficient Permissions
```bash
# Error: 403 Forbidden / Access Denied
# Solution: Use a user account with the appropriate role (ADMIN/AGENT)
```
