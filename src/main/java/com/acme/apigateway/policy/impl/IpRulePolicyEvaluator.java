package com.acme.apigateway.policy.impl;

import com.acme.apigateway.infra.db.entity.PolicyEntity;
import com.acme.apigateway.policy.IpRule;
import com.acme.apigateway.policy.IpRuleProvider;
import com.acme.apigateway.policy.PolicyContext;
import com.acme.apigateway.policy.PolicyDecision;
import com.acme.apigateway.policy.PolicyEvaluator;
import com.acme.apigateway.security.IpCidrMatcher;
import java.util.Map;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class IpRulePolicyEvaluator implements PolicyEvaluator {
  private final IpRuleProvider ipRuleService;

  public IpRulePolicyEvaluator(IpRuleProvider ipRuleService) {
    this.ipRuleService = ipRuleService;
  }

  @Override
  public String handlesType() {
    return "ip_rule";
  }

  @Override
  public Mono<PolicyDecision> evaluate(PolicyEntity policy, PolicyContext context) {
    return ipRuleService.rulesForRoute(context.route().id())
        .map(rules -> {
          for (IpRule rule : rules) {
            if (matches(rule, context.clientIp())) {
              if ("deny".equalsIgnoreCase(rule.action())) {
                return PolicyDecision.deny(handlesType(), "ip_denied", Map.of("status", 403, "cidr", rule.cidr()));
              }
              if ("allow".equalsIgnoreCase(rule.action())) {
                return PolicyDecision.allow(handlesType());
              }
            }
          }
          return PolicyDecision.allow(handlesType());
        });
  }

  private boolean matches(IpRule rule, String clientIp) {
    try {
      return new IpCidrMatcher(rule.cidr()).matches(clientIp);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
