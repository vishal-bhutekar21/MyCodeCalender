# Cloudflare Security & Worker Audit

## 1. Live Worker Script Security Audit

Inspection of deployed Cloudflare Workers under the authorized account revealed critical security exposures in the existing edge environment.

### 1.1 SEC-03: Plaintext Razorpay Secret in Worker Bundle
In the deployed script `razorpay-backend-worker`:
```javascript
// Line 44 in razorpay-backend-worker
const keyId = env.RAZORPAY_KEY_ID || "rzp_test_TTHT9QmOAdtrb5";
const keySecret = env.RAZORPAY_KEY_SECRET || "HEPFlUd1FozZEDNXnDa1FITL";
```

#### Security Risk
* The secret key `HEPFlUd1FozZEDNXnDa1FITL` is embedded directly into the JavaScript source code of the deployed worker.
* If the environment variable `RAZORPAY_KEY_SECRET` is unset or fails to load, the worker falls back to this hardcoded secret.
* Any unauthorized access to the worker script or bundle exposes the Razorpay credentials, allowing attackers to forge payment signatures (`/api/verify-payment`) or interact with the Razorpay Merchant API.

#### Remediation
1. Rotate the Razorpay key pair immediately in the Razorpay Dashboard.
2. Remove all plaintext string fallbacks from worker source code:
   ```javascript
   if (!env.RAZORPAY_KEY_SECRET) {
     return jsonResponse({ error: "Configuration error" }, 500);
   }
   ```
3. Store secrets exclusively via encrypted Cloudflare Worker Secrets (`wrangler secret put RAZORPAY_KEY_SECRET`).

---

### 1.2 Overly Permissive CORS Headers
In both deployed workers (`justu-tip-worker` and `razorpay-backend-worker`):
```javascript
var CORS_HEADERS = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type, Authorization",
  "Access-Control-Max-Age": "86400"
};
```
* **Risk**: Wildcard origin `*` allows any third-party malicious website to execute cross-origin POST requests to `/api/record-tip` and `/api/create-order` using a victim's browser session.
* **Remediation**: Restrict `Access-Control-Allow-Origin` strictly to authorized origins (`https://vishalbhutekar.me`, `https://mycodecalendar.com`, or mobile app origins).

---

## 2. Cloudflare Zone Security (`vishalbhutekar.me`)

* **Zone ID**: `84d04451d623e1d6885d01c55a89ce3a`
* **Plan**: Free Website.
* **SSL/TLS Mode**: Full.
* **WAF Status**: Default Cloudflare Managed Rules active. No custom Rate Limiting Rules or IP Access Rules are configured.
* **Page Shield**: Inactive.
* **Security Recommendation**: When deploying the new CodeCalendar API worker on this zone (e.g. `api.vishalbhutekar.me` or a dedicated domain), configure a Cloudflare Rate Limiting Rule (e.g. max 60 requests/minute per IP) to prevent denial-of-service abuse against the edge endpoints.
