package com.acme.apigateway.admin;

import com.acme.apigateway.admin.dto.ApiClientRequest;
import com.acme.apigateway.admin.dto.AssignmentRequest;
import com.acme.apigateway.admin.dto.IpRuleRequest;
import com.acme.apigateway.admin.dto.PolicyRequest;
import com.acme.apigateway.admin.dto.RouteRequest;
import com.acme.apigateway.infra.db.entity.ApiClientEntity;
import com.acme.apigateway.infra.db.entity.IpRuleEntity;
import com.acme.apigateway.infra.db.entity.PolicyAssignmentEntity;
import com.acme.apigateway.infra.db.entity.PolicyEntity;
import com.acme.apigateway.infra.db.entity.RouteEntity;
import com.acme.apigateway.infra.db.repo.ApiClientRepository;
import com.acme.apigateway.infra.db.repo.IpRuleRepository;
import com.acme.apigateway.infra.db.repo.PolicyAssignmentRepository;
import com.acme.apigateway.infra.db.repo.PolicyRepository;
import com.acme.apigateway.infra.db.repo.RouteRepository;
import com.acme.apigateway.security.ApiKeyHasher;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AdminService {
  private final RouteRepository routeRepository;
  private final PolicyRepository policyRepository;
  private final PolicyAssignmentRepository assignmentRepository;
  private final IpRuleRepository ipRuleRepository;
  private final ApiClientRepository apiClientRepository;
  private final ApiKeyHasher apiKeyHasher;

  public AdminService(RouteRepository routeRepository,
      PolicyRepository policyRepository,
      PolicyAssignmentRepository assignmentRepository,
      IpRuleRepository ipRuleRepository,
      ApiClientRepository apiClientRepository,
      ApiKeyHasher apiKeyHasher) {
    this.routeRepository = routeRepository;
    this.policyRepository = policyRepository;
    this.assignmentRepository = assignmentRepository;
    this.ipRuleRepository = ipRuleRepository;
    this.apiClientRepository = apiClientRepository;
    this.apiKeyHasher = apiKeyHasher;
  }

  public Flux<RouteEntity> listRoutes() {
    return routeRepository.findAll();
  }

  public Mono<RouteEntity> createRoute(RouteRequest request) {
    RouteEntity entity = new RouteEntity(UUID.randomUUID(), request.name(), request.pathPattern(),
        request.upstreamUrl(), request.methods(), request.enabled(), Instant.now());
    return routeRepository.save(entity);
  }

  public Mono<RouteEntity> updateRoute(UUID id, RouteRequest request) {
    return routeRepository.findById(id)
        .flatMap(existing -> routeRepository.save(new RouteEntity(id, request.name(), request.pathPattern(),
            request.upstreamUrl(), request.methods(), request.enabled(), existing.createdAt())));
  }

  public Mono<Void> deleteRoute(UUID id) {
    return routeRepository.deleteById(id);
  }

  public Flux<PolicyEntity> listPolicies() {
    return policyRepository.findAll();
  }

  public Mono<PolicyEntity> createPolicy(PolicyRequest request) {
    PolicyEntity entity = new PolicyEntity(UUID.randomUUID(), request.name(), request.type(),
        request.configJson(), request.enabled(), Instant.now());
    return policyRepository.save(entity);
  }

  public Flux<PolicyAssignmentEntity> listAssignments(UUID routeId) {
    return assignmentRepository.findAllByRouteIdAndEnabledTrueOrderByOrderIndex(routeId);
  }

  public Mono<Void> assignPolicies(UUID routeId, AssignmentRequest request) {
    return assignmentRepository.findAllByRouteIdAndEnabledTrueOrderByOrderIndex(routeId)
        .flatMap(existing -> assignmentRepository.deleteById(existing.id()))
        .thenMany(Flux.fromIterable(request.assignments()))
        .flatMap(item -> assignmentRepository.save(new PolicyAssignmentEntity(
            UUID.randomUUID(), routeId, item.policyId(), item.orderIndex(), item.enabled(), Instant.now())))
        .then();
  }

  public Flux<IpRuleEntity> listIpRules() {
    return ipRuleRepository.findAll();
  }

  public Mono<IpRuleEntity> createIpRule(IpRuleRequest request) {
    IpRuleEntity entity = new IpRuleEntity(UUID.randomUUID(), request.scope(), request.cidr(),
        request.action(), request.routeId(), request.enabled(), Instant.now());
    return ipRuleRepository.save(entity);
  }

  public Flux<ApiClientEntity> listApiClients() {
    return apiClientRepository.findAll();
  }

  public Mono<ApiClientEntity> createApiClient(ApiClientRequest request) {
    String hashed = apiKeyHasher.hash(request.apiKeyPlain());
    ApiClientEntity entity = new ApiClientEntity(UUID.randomUUID(), request.name(), hashed,
        request.active(), Instant.now());
    return apiClientRepository.save(entity);
  }
}
