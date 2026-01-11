package com.acme.apigateway.admin.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;

public record IpRuleRequest(
    @NotBlank String scope,
    @NotBlank String cidr,
    @NotBlank String action,
    UUID routeId,
    boolean enabled) {}
