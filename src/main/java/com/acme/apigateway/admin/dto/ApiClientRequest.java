package com.acme.apigateway.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record ApiClientRequest(
    @NotBlank String name,
    @NotBlank String apiKeyPlain,
    boolean active) { }
