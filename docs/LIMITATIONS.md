# Limitations

This gateway provides baseline protections but is not a full replacement for a dedicated WAF or threat detection platform.

## Not Covered

- Behavioral anomaly detection and adaptive throttling
- Full OWASP CRS parity and deep payload inspection
- mTLS client certificate issuance and rotation
- SIEM export, alerting, and correlation pipelines
- Abuse detection across multiple tenants or identity providers

## Why These Are Risky

These capabilities require deep tuning and threat intelligence. Incorrect implementations can lead to false positives, bypasses, or operational outages.

Use the extension points described in the README to implement them safely with dedicated security expertise.
