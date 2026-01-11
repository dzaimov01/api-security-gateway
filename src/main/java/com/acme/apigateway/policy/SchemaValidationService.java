package com.acme.apigateway.policy;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class SchemaValidationService {
  private final Map<String, OpenApiInteractionValidator> cache = new ConcurrentHashMap<>();

  public OpenApiInteractionValidator load(final String specPath) {
    return cache.computeIfAbsent(specPath, path -> OpenApiInteractionValidator.createFor(path).build());
  }
}
