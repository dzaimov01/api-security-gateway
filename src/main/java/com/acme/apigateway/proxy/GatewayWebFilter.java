package com.acme.apigateway.proxy;

import com.acme.apigateway.audit.AuditService;
import com.acme.apigateway.policy.PolicyContext;
import com.acme.apigateway.policy.PolicyDecision;
import com.acme.apigateway.policy.PolicyEngine;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class GatewayWebFilter implements WebFilter {
  private static final Logger logger = LoggerFactory.getLogger(GatewayWebFilter.class);

  private final RouteService routeService;
  private final RouteMatcher routeMatcher;
  private final ProxyClient proxyClient;
  private final PolicyEngine policyEngine;
  private final AuditService auditService;
  private final ClientContextExtractor clientContextExtractor;

  public GatewayWebFilter(final RouteService routeService,
      final RouteMatcher routeMatcher,
      final ProxyClient proxyClient,
      final PolicyEngine policyEngine,
      final AuditService auditService,
      final ClientContextExtractor clientContextExtractor) {
    this.routeService = routeService;
    this.routeMatcher = routeMatcher;
    this.proxyClient = proxyClient;
    this.policyEngine = policyEngine;
    this.auditService = auditService;
    this.clientContextExtractor = clientContextExtractor;
  }

  @Override
  public Mono<Void> filter(final ServerWebExchange exchange, final WebFilterChain chain) {
    String path = exchange.getRequest().getPath().value();
    if (path.startsWith("/admin") || path.startsWith("/actuator")) {
      return chain.filter(exchange);
    }

    return routeService.listRoutes().collectList().flatMap(routes -> {
      Optional<RouteDefinition> match = routeMatcher.match(routes, path, exchange.getRequest().getMethod());
      if (match.isEmpty()) {
        return chain.filter(exchange);
      }
      RouteDefinition route = match.get();
      String clientIp = clientContextExtractor.resolveClientIp(exchange);
      String apiKey = clientContextExtractor.resolveApiKey(exchange);
      UUID requestId = UUID.randomUUID();

      return cacheBody(exchange)
          .defaultIfEmpty(new byte[0])
          .flatMap(body -> evaluatePolicies(exchange, route, clientIp, apiKey, requestId, body)
              .flatMap(decision -> handleDecision(exchange, route, clientIp, requestId, decision, body)));
    });
  }

  private Mono<PolicyDecision> evaluatePolicies(final ServerWebExchange exchange,
      final RouteDefinition route,
      final String clientIp,
      final String apiKey,
      final UUID requestId,
      final byte[] body) {
    PolicyContext context = new PolicyContext(requestId, clientIp, apiKey, route, exchange.getRequest(), body);
    return policyEngine.evaluate(route.id(), context);
  }

  private Mono<Void> handleDecision(final ServerWebExchange exchange,
      final RouteDefinition route,
      final String clientIp,
      final UUID requestId,
      final PolicyDecision decision,
      final byte[] body) {
    return auditService.recordDecision(requestId, clientIp, route, decision)
        .then(Mono.defer(() -> {
          if (!decision.allowed()) {
            return deny(exchange, decision);
          }
          return proxyClient.forward(exchange, route, body);
        }));
  }

  private Mono<Void> deny(final ServerWebExchange exchange, final PolicyDecision decision) {
    int status = decision.metadata().getOrDefault("status", HttpStatus.FORBIDDEN.value()) instanceof Integer
        ? (Integer) decision.metadata().getOrDefault("status", HttpStatus.FORBIDDEN.value())
        : HttpStatus.FORBIDDEN.value();
    exchange.getResponse().setStatusCode(HttpStatus.valueOf(status));
    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
    String payload = "{\"allowed\":false,\"policy\":\"" + decision.policy() + "\",\"reason\":\"" + decision.reason() + "\"}";
    byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
    DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
    return exchange.getResponse().writeWith(Mono.just(buffer));
  }

  private Mono<byte[]> cacheBody(final ServerWebExchange exchange) {
    return DataBufferUtils.join(exchange.getRequest().getBody())
        .map(dataBuffer -> {
          byte[] bytes = new byte[dataBuffer.readableByteCount()];
          dataBuffer.read(bytes);
          DataBufferUtils.release(dataBuffer);
          return bytes;
        });
  }
}
