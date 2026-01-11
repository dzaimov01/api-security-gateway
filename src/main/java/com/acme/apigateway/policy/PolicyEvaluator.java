package com.acme.apigateway.policy;

import com.acme.apigateway.infra.db.entity.PolicyEntity;
import reactor.core.publisher.Mono;

public interface PolicyEvaluator {
  String handlesType();
  Mono<PolicyDecision> evaluate(PolicyEntity policy, PolicyContext context);
}
