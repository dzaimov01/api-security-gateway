package com.acme.apigateway.policy;

import com.acme.apigateway.infra.db.entity.IpRuleEntity;
import com.acme.apigateway.infra.db.repo.IpRuleRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class IpRuleService implements IpRuleProvider {
  private final IpRuleRepository ipRuleRepository;
  private volatile List<IpRule> cached = List.of();

  public IpRuleService(IpRuleRepository ipRuleRepository) {
    this.ipRuleRepository = ipRuleRepository;
    refresh();
  }

  @Scheduled(fixedDelayString = "${gateway.route-refresh-interval:PT30S}")
  public void refresh() {
    Flux<IpRuleEntity> source = ipRuleRepository.findAllByEnabledTrue();
    if (source == null) {
      cached = List.of();
      return;
    }
    source
        .map(this::toRule)
        .collectList()
        .doOnNext(list -> cached = list)
        .subscribe();
  }

  @Override
  public Mono<List<IpRule>> rulesForRoute(UUID routeId) {
    return Mono.just(cached.stream()
        .filter(rule -> rule.routeId() == null || rule.routeId().equals(routeId))
        .toList());
  }

  private IpRule toRule(IpRuleEntity entity) {
    return new IpRule(entity.id(), entity.scope(), entity.cidr(), entity.action(), entity.routeId());
  }
}
