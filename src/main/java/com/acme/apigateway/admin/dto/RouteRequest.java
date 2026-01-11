package com.acme.apigateway.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record RouteRequest(
    @NotBlank String name,
    @NotBlank String pathPattern,
    @NotBlank String upstreamUrl,
    String methods,
    boolean enabled) {}
