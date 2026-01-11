package com.acme.apigateway.infra.db.repo;

import com.acme.apigateway.infra.db.entity.PolicyAssignmentEntity;
import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface PolicyAssignmentRepository extends ReactiveCrudRepository<PolicyAssignmentEntity, UUID> {
  Flux<PolicyAssignmentEntity> findAllByRouteIdAndEnabledTrueOrderByOrderIndex(UUID routeId);
}
