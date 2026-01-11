package com.acme.apigateway.infra.db.entity;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("api_clients")
public record ApiClientEntity(
    @Id UUID id,
    String name,
    String apiKeyHash,
    boolean active,
    Instant createdAt) { }
