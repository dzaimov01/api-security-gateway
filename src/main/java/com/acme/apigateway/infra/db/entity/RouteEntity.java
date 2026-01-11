package com.acme.apigateway.infra.db.entity;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("routes")
public record RouteEntity(
    @Id UUID id,
    String name,
    String pathPattern,
    String upstreamUrl,
    String methods,
    boolean enabled,
    Instant createdAt) { }
