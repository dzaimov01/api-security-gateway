package com.acme.apigateway.infra.db.repo;

import com.acme.apigateway.infra.db.entity.ApiClientEntity;
import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ApiClientRepository extends ReactiveCrudRepository<ApiClientEntity, UUID> {
  Mono<ApiClientEntity> findByApiKeyHashAndActiveTrue(String apiKeyHash);
}
