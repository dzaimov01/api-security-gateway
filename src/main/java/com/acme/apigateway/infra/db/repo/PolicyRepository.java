package com.acme.apigateway.infra.db.repo;

import com.acme.apigateway.infra.db.entity.PolicyEntity;
import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface PolicyRepository extends ReactiveCrudRepository<PolicyEntity, UUID> {
  Flux<PolicyEntity> findAllByEnabledTrue();
}
