package com.acme.apigateway.proxy;

import java.util.List;
import java.util.UUID;

public record RouteDefinition(
    UUID id,
    String name,
    String pathPattern,
    String upstreamUrl,
    List<String> methods) { }
