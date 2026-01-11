package com.acme.apigateway.infra.db.repo;

import com.acme.apigateway.infra.db.entity.AuditLogEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface AuditLogRepository extends ReactiveCrudRepository<AuditLogEntity, Long> { }
