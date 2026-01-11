package com.acme.apigateway.policy;

import static org.assertj.core.api.Assertions.assertThat;

import com.acme.apigateway.config.GatewayProperties;
import com.acme.apigateway.infra.db.entity.PolicyEntity;
import com.acme.apigateway.policy.impl.InMemoryRateLimiter;
import com.acme.apigateway.policy.impl.RateLimitPolicyEvaluator;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RateLimitPolicyEvaluatorTest {

  @Test
  void rateLimitBlocksAfterCapacity() {
    GatewayProperties properties = new GatewayProperties(
        "test",
        Duration.ofSeconds(30),
        new GatewayProperties.RateLimitProperties("memory", Duration.ofSeconds(60), 1, 0),
        new GatewayProperties.WafProperties(1024, 1024, List.of(), List.of(), List.of(), List.of()),
        new GatewayProperties.SchemaValidationProperties(false, ""),
        new GatewayProperties.ProxyProperties(Duration.ofSeconds(2), Duration.ofSeconds(5), List.of()),
        new GatewayProperties.JwtProperties("issuer", List.of(), ""),
        new GatewayProperties.AuditProperties(false));

    PolicyConfigParser parser = new PolicyConfigParser(new com.fasterxml.jackson.databind.ObjectMapper());
    RateLimitPolicyEvaluator evaluator = new RateLimitPolicyEvaluator(parser, List.of(new InMemoryRateLimiter()), properties);

    String configJson = "{\"scope\":\"ip\",\"capacity\":1,\"refillPerSecond\":0,\"windowSeconds\":60}";
    PolicyEntity policy = new PolicyEntity(UUID.randomUUID(), "rate", "rate_limit", configJson, true, Instant.now());
    PolicyContext context = new PolicyContext(UUID.randomUUID(), "10.0.0.1", "", null,
        new org.springframework.http.server.reactive.ServerHttpRequestDecorator(
            org.springframework.mock.http.server.reactive.MockServerHttpRequest.get("/test").build()) {},
        new byte[0]);

    PolicyDecision first = evaluator.evaluate(policy, context).block();
    PolicyDecision second = evaluator.evaluate(policy, context).block();

    assertThat(first).isNotNull();
    assertThat(second).isNotNull();
    assertThat(first.allowed()).isTrue();
    assertThat(second.allowed()).isFalse();
  }
}
