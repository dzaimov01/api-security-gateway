package com.acme.apigateway.infra.db.repo;

import com.acme.apigateway.infra.db.entity.RouteEntity;
import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface RouteRepository extends ReactiveCrudRepository<RouteEntity, UUID> {
  Flux<RouteEntity> findAllByEnabledTrue();
}
