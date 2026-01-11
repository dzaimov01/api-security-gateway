package com.acme.apigateway.proxy;

import com.acme.apigateway.config.GatewayProperties;
import com.acme.apigateway.infra.db.entity.RouteEntity;
import com.acme.apigateway.infra.db.repo.RouteRepository;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class RouteService {
  private final RouteRepository routeRepository;
  private final Map<UUID, RouteDefinition> cache = new ConcurrentHashMap<>();

  public RouteService(final RouteRepository routeRepository, final GatewayProperties properties) {
    this.routeRepository = routeRepository;
    Duration refresh = properties.routeRefreshInterval();
    if (refresh == null || refresh.isZero()) {
      refreshRoutes();
    }
  }

  @Scheduled(fixedDelayString = "${gateway.route-refresh-interval:PT30S}")
  public void refreshRoutes() {
    var source = routeRepository.findAllByEnabledTrue();
    if (source == null) {
      cache.clear();
      return;
    }
    source
        .map(this::toDefinition)
        .collectList()
        .doOnNext(routes -> {
          cache.clear();
          for (RouteDefinition route : routes) {
            cache.put(route.id(), route);
          }
        })
        .subscribe();
  }

  public Flux<RouteDefinition> listRoutes() {
    if (cache.isEmpty()) {
      var source = routeRepository.findAllByEnabledTrue();
      if (source == null) {
        return Flux.empty();
      }
      return source.map(this::toDefinition);
    }
    return Flux.fromIterable(cache.values());
  }

  public Mono<RouteDefinition> getRoute(final UUID id) {
    RouteDefinition cached = cache.get(id);
    if (cached != null) {
      return Mono.just(cached);
    }
    return routeRepository.findById(id).map(this::toDefinition);
  }

  private RouteDefinition toDefinition(final RouteEntity entity) {
    List<String> methods = entity.methods() == null ? List.of()
        : Arrays.stream(entity.methods().split(","))
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .toList();
    return new RouteDefinition(entity.id(), entity.name(), entity.pathPattern(), entity.upstreamUrl(), methods);
  }
}
