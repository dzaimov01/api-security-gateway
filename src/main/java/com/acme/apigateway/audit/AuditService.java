package com.acme.apigateway.audit;

import com.acme.apigateway.config.GatewayProperties;
import com.acme.apigateway.infra.db.entity.AuditLogEntity;
import com.acme.apigateway.infra.db.repo.AuditLogRepository;
import com.acme.apigateway.policy.PolicyDecision;
import com.acme.apigateway.proxy.RouteDefinition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AuditService {
  private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

  private final AuditLogRepository auditLogRepository;
  private final ObjectMapper objectMapper;
  private final GatewayProperties properties;

  public AuditService(AuditLogRepository auditLogRepository, ObjectMapper objectMapper, GatewayProperties properties) {
    this.auditLogRepository = auditLogRepository;
    this.objectMapper = objectMapper;
    this.properties = properties;
  }

  public Mono<Void> recordDecision(UUID requestId, String clientIp, RouteDefinition route, PolicyDecision decision) {
    if (!properties.audit().enabled()) {
      return Mono.empty();
    }
    String details = toJson(decision.metadata());
    AuditLogEntity entity = new AuditLogEntity(
        null,
        requestId,
        clientIp,
        route.id(),
        route.pathPattern(),
        decision.allowed() ? "ALLOW" : "DENY",
        details,
        Instant.now());

    logger.info("audit_decision requestId={} clientIp={} route={} decision={} policy={} reason={}",
        requestId, clientIp, route.pathPattern(), decision.allowed(), decision.policy(), decision.reason());

    return auditLogRepository.save(entity).then();
  }

  private String toJson(Map<String, Object> metadata) {
    try {
      return objectMapper.writeValueAsString(metadata == null ? Map.of() : metadata);
    } catch (JsonProcessingException e) {
      return "{}";
    }
  }
}
