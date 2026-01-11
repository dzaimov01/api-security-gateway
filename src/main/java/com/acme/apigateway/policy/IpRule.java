package com.acme.apigateway.policy;

import java.util.UUID;

public record IpRule(UUID id, String scope, String cidr, String action, UUID routeId) {}
