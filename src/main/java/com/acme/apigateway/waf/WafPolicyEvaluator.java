package com.acme.apigateway.waf;

import com.acme.apigateway.config.GatewayProperties;
import com.acme.apigateway.infra.db.entity.PolicyEntity;
import com.acme.apigateway.policy.PolicyContext;
import com.acme.apigateway.policy.PolicyDecision;
import com.acme.apigateway.policy.PolicyEvaluator;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class WafPolicyEvaluator implements PolicyEvaluator {
  private final GatewayProperties properties;

  public WafPolicyEvaluator(final GatewayProperties properties) {
    this.properties = properties;
  }

  @Override
  public String handlesType() {
    return "waf";
  }

  @Override
  public Mono<PolicyDecision> evaluate(final PolicyEntity policy, final PolicyContext context) {
    GatewayProperties.WafProperties waf = properties.waf();
    if (waf == null) {
      return Mono.just(PolicyDecision.allow(handlesType()));
    }

    String method = context.request().getMethod() == null ? "UNKNOWN" : context.request().getMethod().name();
    if (waf.blockedMethods().contains(method)) {
      return Mono.just(PolicyDecision.deny(handlesType(), "method_blocked", Map.of("status", 405)));
    }

    int headerSize = estimateHeaderSize(context.request().getHeaders());
    if (headerSize > waf.maxHeaderBytes()) {
      return Mono.just(PolicyDecision.deny(handlesType(), "headers_too_large", Map.of("status", 431)));
    }

    if (context.requestBody() != null && context.requestBody().length > waf.maxBodyBytes()) {
      return Mono.just(PolicyDecision.deny(handlesType(), "body_too_large", Map.of("status", 413)));
    }

    String path = context.request().getPath().value().toLowerCase(Locale.ROOT);
    if (waf.pathTraversalTokens().stream().anyMatch(path::contains)) {
      return Mono.just(PolicyDecision.deny(handlesType(), "path_traversal", Map.of("status", 400)));
    }

    String body = context.requestBody() == null ? "" : new String(context.requestBody(), StandardCharsets.UTF_8);
    String combined = (path + " " + body).toLowerCase(Locale.ROOT);

    if (containsAny(combined, waf.sqlInjectionPatterns())) {
      return Mono.just(PolicyDecision.deny(handlesType(), "sqli_pattern", Map.of("status", 400)));
    }
    if (containsAny(combined, waf.xssPatterns())) {
      return Mono.just(PolicyDecision.deny(handlesType(), "xss_pattern", Map.of("status", 400)));
    }

    return Mono.just(PolicyDecision.allow(handlesType()));
  }

  private int estimateHeaderSize(final HttpHeaders headers) {
    return headers.entrySet().stream()
        .mapToInt(entry -> entry.getKey().length() + entry.getValue().toString().length())
        .sum();
  }

  private boolean containsAny(final String source, final List<String> patterns) {
    return patterns.stream().anyMatch(pattern -> source.contains(pattern.toLowerCase(Locale.ROOT)));
  }
}
