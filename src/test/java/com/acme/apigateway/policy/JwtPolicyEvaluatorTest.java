package com.acme.apigateway.policy;

import static org.assertj.core.api.Assertions.assertThat;

import com.acme.apigateway.infra.db.entity.PolicyEntity;
import com.acme.apigateway.policy.impl.JwtPolicyEvaluator;
import com.acme.apigateway.security.JwtTokenValidator;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import reactor.core.publisher.Mono;

class JwtPolicyEvaluatorTest {

  @Test
  void rejectsMissingToken() {
    JwtTokenValidator validator = token -> Mono.error(new IllegalStateException("invalid"));
    JwtPolicyEvaluator evaluator = new JwtPolicyEvaluator(validator);

    PolicyEntity policy = new PolicyEntity(UUID.randomUUID(), "jwt", "jwt", "{}", true, Instant.now());
    PolicyContext context = new PolicyContext(UUID.randomUUID(), "10.0.0.1", "", null,
        new ServerHttpRequestDecorator(MockServerHttpRequest.get("/test").build()) {},
        new byte[0]);

    PolicyDecision decision = evaluator.evaluate(policy, context).block();
    assertThat(decision).isNotNull();
    assertThat(decision.allowed()).isFalse();
  }

  @Test
  void acceptsValidToken() {
    JwtTokenValidator validator = token -> Mono.just(new org.springframework.security.oauth2.jwt.Jwt(
        "token",
        java.time.Instant.now(),
        java.time.Instant.now().plusSeconds(60),
        java.util.Map.of("alg", "none"),
        java.util.Map.of("sub", "test")));
    JwtPolicyEvaluator evaluator = new JwtPolicyEvaluator(validator);

    PolicyEntity policy = new PolicyEntity(UUID.randomUUID(), "jwt", "jwt", "{}", true, Instant.now());
    PolicyContext context = new PolicyContext(UUID.randomUUID(), "10.0.0.1", "", null,
        new ServerHttpRequestDecorator(MockServerHttpRequest.get("/test")
            .header("Authorization", "Bearer token")
            .build()) {},
        new byte[0]);

    PolicyDecision decision = evaluator.evaluate(policy, context).block();
    assertThat(decision).isNotNull();
    assertThat(decision.allowed()).isTrue();
  }
}
