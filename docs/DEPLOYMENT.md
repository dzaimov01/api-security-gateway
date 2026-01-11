# Deployment

## Local (Docker Compose)

```bash
docker compose up --build
```

- Gateway: `http://localhost:8080`
- Echo service: `http://localhost:9000`
- Postgres: `localhost:5432`
- Redis: `localhost:6379`

## Production Notes

- Place the gateway behind a load balancer with TLS termination.
- Lock down the `/admin` API to private networks or VPN.
- Use Redis for shared rate limiting across instances.
- Enable structured logging to ship audit trails.
- Use separate DB credentials for read/write access if needed.
- Configure health checks against `/actuator/health`.

## Environment Configuration

See `config/example.env` for required variables.
