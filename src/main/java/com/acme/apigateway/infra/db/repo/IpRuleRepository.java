package com.acme.apigateway.infra.db.repo;

import com.acme.apigateway.infra.db.entity.IpRuleEntity;
import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface IpRuleRepository extends ReactiveCrudRepository<IpRuleEntity, UUID> {
  Flux<IpRuleEntity> findAllByEnabledTrue();
  Flux<IpRuleEntity> findAllByEnabledTrueAndRouteId(UUID routeId);
}
