# API Quirks — Restful Booker

## 1. Rate Limiting (418 I'm a Teapot)
- **Endpoint:** POST /booking
- **Behaviour:** Returns 418 after rapid successive POST requests
- **Impact:** CRUD tests skip gracefully via RetryAnalyzer when 418 is received
- **Workaround:** 3-second delay between POST calls; tests self-heal on retry

## 2. Auth Failure Returns 200
- **Endpoint:** POST /auth
- **Expected:** 401 Unauthorized
- **Actual:** 200 OK with body `{"reason": "Bad credentials"}`
- **Impact:** Auth failure tests assert on response body, not status code

## 3. DELETE Returns 201
- **Endpoint:** DELETE /booking/{id}
- **Expected:** 200 OK or 204 No Content
- **Actual:** 201 Created
- **Impact:** Delete assertions use 201, not 200

## 4. No Auth Returns 403
- **Endpoint:** PUT/PATCH/DELETE /booking/{id} without token
- **Behaviour:** Returns 403 Forbidden
- **Impact:** Negative auth tests assert 403

## 5. Invalid Token Returns 403
- **Endpoint:** PUT/PATCH/DELETE with wrong Cookie token
- **Behaviour:** Returns 403 Forbidden (same as no-auth)
- **Impact:** No distinction between missing vs invalid token