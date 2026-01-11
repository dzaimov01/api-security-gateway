package com.acme.apigateway;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.acme.apigateway.infra.db.entity.IpRuleEntity;
import com.acme.apigateway.infra.db.entity.PolicyAssignmentEntity;
import com.acme.apigateway.infra.db.entity.PolicyEntity;
import com.acme.apigateway.infra.db.entity.RouteEntity;
import com.acme.apigateway.infra.db.repo.AuditLogRepository;
import com.acme.apigateway.infra.db.repo.IpRuleRepository;
import com.acme.apigateway.infra.db.repo.PolicyAssignmentRepository;
import com.acme.apigateway.infra.db.repo.PolicyRepository;
import com.acme.apigateway.infra.db.repo.RouteRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootTest
@AutoConfigureWebTestClient
class GatewayIntegrationTest {

  @Autowired
  private WebTestClient webTestClient;

  @MockBean
  private RouteRepository routeRepository;

  @MockBean
  private PolicyAssignmentRepository assignmentRepository;

  @MockBean
  private PolicyRepository policyRepository;

  @MockBean
  private IpRuleRepository ipRuleRepository;

  @MockBean
  private AuditLogRepository auditLogRepository;

  @BeforeEach
  void setup() {
    UUID routeId = UUID.randomUUID();
    RouteEntity route = new RouteEntity(routeId, "echo", "/echo", "http://localhost:9000", "POST", true, Instant.now());
    PolicyEntity policy = new PolicyEntity(UUID.randomUUID(), "ip", "ip_rule", "{}", true, Instant.now());
    PolicyAssignmentEntity assignment = new PolicyAssignmentEntity(UUID.randomUUID(), routeId, policy.id(), 1, true, Instant.now());
    IpRuleEntity ipRule = new IpRuleEntity(UUID.randomUUID(), "global", "10.0.0.0/24", "deny", null, true, Instant.now());

    when(routeRepository.findAllByEnabledTrue()).thenReturn(Flux.just(route));
    when(assignmentRepository.findAllByRouteIdAndEnabledTrueOrderByOrderIndex(routeId))
        .thenReturn(Flux.just(assignment));
    when(policyRepository.findById(policy.id())).thenReturn(Mono.just(policy));
    when(ipRuleRepository.findAllByEnabledTrue()).thenReturn(Flux.just(ipRule));
    when(auditLogRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
  }

  @Test
  void blocksDeniedIp() {
    webTestClient.post()
        .uri("/echo")
        .header("X-Forwarded-For", "10.0.0.10")
        .exchange()
        .expectStatus().isForbidden();
  }
}
