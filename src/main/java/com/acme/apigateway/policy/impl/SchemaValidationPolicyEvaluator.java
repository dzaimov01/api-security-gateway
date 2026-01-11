package com.acme.apigateway.policy.impl;

import com.acme.apigateway.config.GatewayProperties;
import com.acme.apigateway.infra.db.entity.PolicyEntity;
import com.acme.apigateway.policy.PolicyConfigParser;
import com.acme.apigateway.policy.PolicyContext;
import com.acme.apigateway.policy.PolicyDecision;
import com.acme.apigateway.policy.PolicyEvaluator;
import com.acme.apigateway.policy.SchemaValidationService;
import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.model.SimpleRequest;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class SchemaValidationPolicyEvaluator implements PolicyEvaluator {
  private final SchemaValidationService validationService;
  private final PolicyConfigParser parser;
  private final GatewayProperties properties;

  public SchemaValidationPolicyEvaluator(SchemaValidationService validationService,
      PolicyConfigParser parser,
      GatewayProperties properties) {
    this.validationService = validationService;
    this.parser = parser;
    this.properties = properties;
  }

  @Override
  public String handlesType() {
    return "schema_validation";
  }

  @Override
  public Mono<PolicyDecision> evaluate(PolicyEntity policy, PolicyContext context) {
    if (!properties.schemaValidation().enabled()) {
      return Mono.just(PolicyDecision.allow(handlesType()));
    }
    SchemaValidationConfig config = parser.parse(policy.configJson(), SchemaValidationConfig.class);
    String specPath = properties.schemaValidation().openApiBasePath() + "/" + config.openApiSpec();
    OpenApiInteractionValidator validator = validationService.load(specPath);

    String contentType = context.request().getHeaders().getContentType() != null
        ? context.request().getHeaders().getContentType().toString()
        : MediaType.APPLICATION_JSON_VALUE;
    String body = context.requestBody() == null ? "" : new String(context.requestBody(), StandardCharsets.UTF_8);

    String method = context.request().getMethod() == null ? "GET" : context.request().getMethod().name();
    SimpleRequest.Builder builder = switch (method) {
      case "GET" -> SimpleRequest.Builder.get(context.request().getPath().value());
      case "POST" -> SimpleRequest.Builder.post(context.request().getPath().value());
      case "PUT" -> SimpleRequest.Builder.put(context.request().getPath().value());
      case "DELETE" -> SimpleRequest.Builder.delete(context.request().getPath().value());
      case "PATCH" -> SimpleRequest.Builder.patch(context.request().getPath().value());
      case "HEAD" -> SimpleRequest.Builder.head(context.request().getPath().value());
      case "OPTIONS" -> SimpleRequest.Builder.options(context.request().getPath().value());
      case "TRACE" -> SimpleRequest.Builder.trace(context.request().getPath().value());
      default -> new SimpleRequest.Builder(method, context.request().getPath().value());
    };
    builder.withBody(body).withContentType(contentType);
    context.request().getHeaders().toSingleValueMap().forEach(builder::withHeader);
    Request request = builder.build();

    var report = validator.validateRequest(request);
    if (report.hasErrors()) {
      return Mono.just(PolicyDecision.deny(handlesType(), "schema_validation_failed",
          Map.of("status", 400, "errors", report.getMessages())));
    }
    return Mono.just(PolicyDecision.allow(handlesType()));
  }

  public record SchemaValidationConfig(String openApiSpec) {}
}
