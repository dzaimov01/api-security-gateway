package com.acme.apigateway.policy;

import static org.assertj.core.api.Assertions.assertThat;

import com.acme.apigateway.infra.db.entity.PolicyEntity;
import com.acme.apigateway.policy.impl.IpRulePolicyEvaluator;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

class IpRulePolicyEvaluatorTest {

  @Test
  void deniesMatchingIp() {
    IpRuleProvider provider = routeId -> Mono.just(List.of(
        new IpRule(UUID.randomUUID(), "global", "10.0.0.0/24", "deny", null)));

    IpRulePolicyEvaluator evaluator = new IpRulePolicyEvaluator(provider);

    PolicyEntity policy = new PolicyEntity(UUID.randomUUID(), "ip", "ip_rule", "{}", true, Instant.now());
    com.acme.apigateway.proxy.RouteDefinition route = new com.acme.apigateway.proxy.RouteDefinition(
        UUID.randomUUID(), "test", "/test", "http://localhost", java.util.List.of("GET"));
    PolicyContext context = new PolicyContext(UUID.randomUUID(), "10.0.0.10", "", route,
        new org.springframework.http.server.reactive.ServerHttpRequestDecorator(
            org.springframework.mock.http.server.reactive.MockServerHttpRequest.get("/test").build()) {},
        new byte[0]);

    PolicyDecision decision = evaluator.evaluate(policy, context).block();
    assertThat(decision).isNotNull();
    assertThat(decision.allowed()).isFalse();
  }
}
