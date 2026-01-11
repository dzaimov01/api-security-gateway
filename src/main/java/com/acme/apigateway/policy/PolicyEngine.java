package com.acme.apigateway.policy;

import com.acme.apigateway.infra.db.entity.PolicyAssignmentEntity;
import com.acme.apigateway.infra.db.entity.PolicyEntity;
import com.acme.apigateway.infra.db.repo.PolicyAssignmentRepository;
import com.acme.apigateway.infra.db.repo.PolicyRepository;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class PolicyEngine {
  private final PolicyAssignmentRepository assignmentRepository;
  private final PolicyRepository policyRepository;
  private final Map<String, PolicyEvaluator> evaluators;

  public PolicyEngine(PolicyAssignmentRepository assignmentRepository,
      PolicyRepository policyRepository,
      java.util.List<PolicyEvaluator> evaluators) {
    this.assignmentRepository = assignmentRepository;
    this.policyRepository = policyRepository;
    this.evaluators = evaluators.stream()
        .collect(Collectors.toMap(PolicyEvaluator::handlesType, Function.identity()));
  }

  public Mono<PolicyDecision> evaluate(UUID routeId, PolicyContext context) {
    return assignmentRepository.findAllByRouteIdAndEnabledTrueOrderByOrderIndex(routeId)
        .concatMap(assignment -> policyRepository.findById(assignment.policyId())
            .filter(PolicyEntity::enabled)
            .flatMap(policy -> evaluatePolicy(policy, context))
            .switchIfEmpty(Mono.just(PolicyDecision.allow("policy_skipped"))))
        .filter(decision -> !decision.allowed())
        .next()
        .defaultIfEmpty(PolicyDecision.allow("all_policies"));
  }

  private Mono<PolicyDecision> evaluatePolicy(PolicyEntity policy, PolicyContext context) {
    PolicyEvaluator evaluator = evaluators.get(policy.type());
    if (evaluator == null) {
      return Mono.just(PolicyDecision.deny(policy.type(), "unsupported_policy", Map.of("policyId", policy.id().toString())));
    }
    return evaluator.evaluate(policy, context);
  }
}
