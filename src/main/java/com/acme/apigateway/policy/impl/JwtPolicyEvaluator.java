package com.acme.apigateway.policy.impl;

import com.acme.apigateway.infra.db.entity.PolicyEntity;
import com.acme.apigateway.policy.PolicyContext;
import com.acme.apigateway.policy.PolicyDecision;
import com.acme.apigateway.policy.PolicyEvaluator;
import com.acme.apigateway.security.JwtTokenValidator;
import java.util.Map;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class JwtPolicyEvaluator implements PolicyEvaluator {
  private final JwtTokenValidator jwtValidatorService;

  public JwtPolicyEvaluator(final JwtTokenValidator jwtValidatorService) {
    this.jwtValidatorService = jwtValidatorService;
  }

  @Override
  public String handlesType() {
    return "jwt";
  }

  @Override
  public Mono<PolicyDecision> evaluate(final PolicyEntity policy, final PolicyContext context) {
    String authHeader = context.request().getHeaders().getFirst("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return Mono.just(PolicyDecision.deny(handlesType(), "missing_bearer_token", Map.of("status", 401)));
    }
    String token = authHeader.substring("Bearer ".length());
    return jwtValidatorService.decode(token)
        .map(jwt -> PolicyDecision.allow(handlesType()))
        .onErrorResume(ex -> Mono.just(PolicyDecision.deny(handlesType(), "invalid_token", Map.of("status", 401))));
  }
}
