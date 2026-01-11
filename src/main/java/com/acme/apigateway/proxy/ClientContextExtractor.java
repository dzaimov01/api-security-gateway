package com.acme.apigateway.proxy;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
public class ClientContextExtractor {
  public String resolveClientIp(final ServerWebExchange exchange) {
    String forwarded = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
    if (StringUtils.isNotBlank(forwarded)) {
      return forwarded.split(",")[0].trim();
    }
    if (exchange.getRequest().getRemoteAddress() != null) {
      return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
    }
    return "unknown";
  }

  public String resolveApiKey(final ServerWebExchange exchange) {
    return Optional.ofNullable(exchange.getRequest().getHeaders().getFirst("X-API-Key"))
        .orElse("");
  }
}
