package com.acme.apigateway.infra.db.entity;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("ip_rules")
public record IpRuleEntity(
    @Id UUID id,
    String scope,
    String cidr,
    String action,
    UUID routeId,
    boolean enabled,
    Instant createdAt) { }
