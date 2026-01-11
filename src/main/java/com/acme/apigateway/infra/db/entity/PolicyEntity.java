package com.acme.apigateway.infra.db.entity;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("policies")
public record PolicyEntity(
    @Id UUID id,
    String name,
    String type,
    String configJson,
    boolean enabled,
    Instant createdAt) {}
