package com.acme.apigateway.admin.dto;

import java.util.List;
import java.util.UUID;

public record AssignmentRequest(List<AssignmentItem> assignments) {
  public record AssignmentItem(UUID policyId, int orderIndex, boolean enabled) { }
}
