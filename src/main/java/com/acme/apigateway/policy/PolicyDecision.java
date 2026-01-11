package com.acme.apigateway.policy;

import java.util.Map;

public record PolicyDecision(boolean allowed, String policy, String reason, Map<String, Object> metadata) {
  public static PolicyDecision allow(String policy) {
    return new PolicyDecision(true, policy, "allowed", Map.of());
  }

  public static PolicyDecision deny(String policy, String reason, Map<String, Object> metadata) {
    return new PolicyDecision(false, policy, reason, metadata == null ? Map.of() : metadata);
  }
}
