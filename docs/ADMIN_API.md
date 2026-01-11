# Admin API Examples

All admin endpoints require JWT with `ROLE_ADMIN`.

## Create Route

```bash
curl -X POST http://localhost:8080/admin/routes \
  -H "Authorization: Bearer $ADMIN_JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "echo",
    "pathPattern": "/echo",
    "upstreamUrl": "http://echo-service:9000",
    "methods": "POST",
    "enabled": true
  }'
```

## Create Policies

```bash
curl -X POST http://localhost:8080/admin/policies \
  -H "Authorization: Bearer $ADMIN_JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "WAF Lite",
    "type": "waf",
    "configJson": "{}",
    "enabled": true
  }'
```

## Assign Policies to a Route

```bash
curl -X POST http://localhost:8080/admin/routes/{routeId}/assignments \
  -H "Authorization: Bearer $ADMIN_JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "assignments": [
      {"policyId": "{wafPolicyId}", "orderIndex": 1, "enabled": true},
      {"policyId": "{jwtPolicyId}", "orderIndex": 2, "enabled": true}
    ]
  }'
```
