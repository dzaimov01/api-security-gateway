package com.acme.apigateway.infra.db.entity;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("policy_assignments")
public record PolicyAssignmentEntity(
    @Id UUID id,
    UUID routeId,
    UUID policyId,
    int orderIndex,
    boolean enabled,
    Instant createdAt) { }
