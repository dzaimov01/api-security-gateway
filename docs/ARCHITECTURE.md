# Architecture

The gateway is a non-blocking reverse proxy that enforces ordered, composable security policies before forwarding traffic.

## Request Flow

```mermaid
sequenceDiagram
  autonumber
  participant Client
  participant Gateway
  participant PolicyEngine
  participant WAF
  participant JWT
  participant RateLimit
  participant IP
  participant Schema
  participant Audit
  participant Upstream

  Client->>Gateway: HTTP request
  Gateway->>WAF: Pre-checks (size, method, patterns)
  WAF-->>Gateway: allow/deny
  Gateway->>PolicyEngine: Build context
  PolicyEngine->>IP: Allow/Deny checks
  PolicyEngine->>JWT: Token validation
  PolicyEngine->>RateLimit: Throttle
  PolicyEngine->>Schema: OpenAPI validation
  PolicyEngine-->>Gateway: decision
  Gateway->>Audit: Record decision
  alt Allowed
    Gateway->>Upstream: Forward request
    Upstream-->>Gateway: Response
    Gateway-->>Client: Response
  else Denied
    Gateway-->>Client: 4xx response
  end
```

## Core Components

- `/proxy`: Route matching, request forwarding, and response bridging
- `/policy`: Ordered policy evaluation and decision aggregation
- `/security`: JWT validation, API key hashing, and IP CIDR matching
- `/waf`: WAF-lite checks and payload inspection
- `/audit`: Structured decision logging and persistent audit trail
- `/admin`: Control plane APIs to manage routes and policies
- `/infra`: Database entities/repositories, caching, and external clients
