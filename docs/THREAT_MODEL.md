# Threat Model

## Threats Addressed

- API abuse and scraping with burst traffic
- Credential stuffing and token replay
- Authorization bypass via token tampering
- Injection attempts (SQLi, XSS) in request payloads
- Path traversal and method abuse
- Oversized headers or bodies triggering resource exhaustion
- IP-based reconnaissance and brute-force traffic

## Threats Partially Addressed

- Distributed DoS (rate limiting helps but is not sufficient)
- Bot-driven abuse with rotating IPs or valid tokens
- Application-layer exfiltration via legitimate credentials

## Threats Out of Scope (Documented)

- Full WAF signature coverage (OWASP CRS parity)
- Behavioral anomaly detection
- Advanced fraud detection and device fingerprinting
- Data loss prevention or content classification
- Full mTLS client certificate lifecycle management
- SIEM exporting and correlation

## Trust Boundaries

- Public internet to gateway edge
- Gateway to upstream SaaS/internal API
- Admin control plane (must be isolated and restricted)
