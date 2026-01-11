package com.acme.apigateway.policy;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class PolicyConfigParser {
  private final ObjectMapper objectMapper;

  public PolicyConfigParser(final ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public <T> T parse(final String json, final Class<T> type) {
    try {
      return objectMapper.readValue(json, type);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid policy config", e);
    }
  }
}
