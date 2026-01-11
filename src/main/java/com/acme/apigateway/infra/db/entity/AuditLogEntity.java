package com.acme.apigateway.infra.db.entity;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("audit_logs")
public record AuditLogEntity(
    @Id Long id,
    UUID requestId,
    String clientIp,
    UUID routeId,
    String routePattern,
    String decision,
    String detailsJson,
    Instant createdAt) { }
