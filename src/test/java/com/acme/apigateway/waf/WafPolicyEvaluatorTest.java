package com.acme.apigateway.waf;

import static org.assertj.core.api.Assertions.assertThat;

import com.acme.apigateway.config.GatewayProperties;
import com.acme.apigateway.infra.db.entity.PolicyEntity;
import com.acme.apigateway.policy.PolicyContext;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class WafPolicyEvaluatorTest {

  @Test
  void blocksPathTraversal() {
    GatewayProperties properties = new GatewayProperties(
        "test",
        Duration.ofSeconds(30),
        new GatewayProperties.RateLimitProperties("memory", Duration.ofSeconds(60), 100, 10),
        new GatewayProperties.WafProperties(1024, 1024, List.of(), List.of("../"), List.of(), List.of("../")),
        new GatewayProperties.SchemaValidationProperties(false, ""),
        new GatewayProperties.ProxyProperties(Duration.ofSeconds(2), Duration.ofSeconds(5), List.of()),
        new GatewayProperties.JwtProperties("issuer", List.of(), ""),
        new GatewayProperties.AuditProperties(false));

    WafPolicyEvaluator evaluator = new WafPolicyEvaluator(properties);
    PolicyEntity policy = new PolicyEntity(UUID.randomUUID(), "waf", "waf", "{}", true, Instant.now());
    PolicyContext context = new PolicyContext(UUID.randomUUID(), "10.0.0.1", "", null,
        new org.springframework.http.server.reactive.ServerHttpRequestDecorator(
            org.springframework.mock.http.server.reactive.MockServerHttpRequest.get("/../etc/passwd").build()) {},
        new byte[0]);

    var decision = evaluator.evaluate(policy, context).block();
    assertThat(decision).isNotNull();
    assertThat(decision.allowed()).isFalse();
  }
}
