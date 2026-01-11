package com.acme.apigateway.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record PolicyRequest(
    @NotBlank String name,
    @NotBlank String type,
    @NotBlank String configJson,
    boolean enabled) {}
