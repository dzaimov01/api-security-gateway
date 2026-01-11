package com.acme.apigateway.policy;

import java.util.List;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface IpRuleProvider {
  Mono<List<IpRule>> rulesForRoute(UUID routeId);
}
