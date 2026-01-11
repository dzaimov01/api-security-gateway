package com.acme.apigateway.policy;

import com.acme.apigateway.proxy.RouteDefinition;
import java.util.UUID;
import org.springframework.http.server.reactive.ServerHttpRequest;

public record PolicyContext(
    UUID requestId,
    String clientIp,
    String apiKey,
    RouteDefinition route,
    ServerHttpRequest request,
    byte[] requestBody) { }
