package com.acme.apigateway.config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gateway")
public record GatewayProperties(
    String instanceName,
    Duration routeRefreshInterval,
    RateLimitProperties rateLimit,
    WafProperties waf,
    SchemaValidationProperties schemaValidation,
    ProxyProperties proxy,
    JwtProperties jwt,
    AuditProperties audit) {

  public record RateLimitProperties(
      String storage,
      Duration defaultWindow,
      int defaultCapacity,
      int defaultRefillPerSecond) { }

  public record WafProperties(
      int maxHeaderBytes,
      int maxBodyBytes,
      List<String> blockedMethods,
      List<String> sqlInjectionPatterns,
      List<String> xssPatterns,
      List<String> pathTraversalTokens) { }

  public record SchemaValidationProperties(
      boolean enabled,
      String openApiBasePath) { }

  public record ProxyProperties(
      Duration connectTimeout,
      Duration responseTimeout,
      List<String> hopByHopHeaders) { }

  public record JwtProperties(
      String issuer,
      List<String> audience,
      String jwksUri) { }

  public record AuditProperties(
      boolean enabled) { }
}
