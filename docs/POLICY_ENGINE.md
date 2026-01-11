# Policy Engine

Policies are stored in the database and assigned to routes in an explicit order. Each request is evaluated sequentially. The first policy to deny short-circuits the request.

## Policy Types

- `rate_limit`: Token bucket rate limiting (memory or Redis)
- `ip_rule`: CIDR allow/deny rules (global or per-route)
- `jwt`: Signature validation, issuer, audience, expiration
- `schema_validation`: OpenAPI request schema enforcement
- `waf`: Basic WAF-lite payload checks

## Ordering

`policy_assignments.order_index` controls the order. Recommended order:

1. WAF
2. IP rules
3. JWT
4. Rate limiting
5. Schema validation

## Extending

Add a new `PolicyEvaluator` implementation, register it as a Spring bean, and store a policy with a matching `type`.
