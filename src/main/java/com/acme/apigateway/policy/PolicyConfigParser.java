package com.acme.apigateway.policy;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class PolicyConfigParser {
  private final ObjectMapper objectMapper;

  public PolicyConfigParser(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public <T> T parse(String json, Class<T> type) {
    try {
      return objectMapper.readValue(json, type);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid policy config", e);
    }
  }
}
