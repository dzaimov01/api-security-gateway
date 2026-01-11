package com.acme.apigateway.security;

import com.acme.apigateway.config.GatewayProperties;
import java.util.List;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class JwtValidatorService implements JwtTokenValidator {
  private final ReactiveJwtDecoder decoder;
  private final List<String> audience;

  public JwtValidatorService(final GatewayProperties properties) {
    GatewayProperties.JwtProperties jwt = properties.jwt();
    this.audience = jwt.audience();
    if (jwt.jwksUri() != null && !jwt.jwksUri().isBlank()) {
      NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withJwkSetUri(jwt.jwksUri()).build();
      decoder.setJwtValidator(buildValidator(jwt.issuer(), jwt.audience()));
      this.decoder = decoder;
    } else if (jwt.issuer() != null && !jwt.issuer().isBlank()) {
      this.decoder = ReactiveJwtDecoders.fromIssuerLocation(jwt.issuer());
    } else {
      this.decoder = token -> Mono.error(new IllegalStateException("JWT issuer or JWKS URI must be configured"));
    }
  }

  @Override
  public Mono<Jwt> decode(final String token) {
    return decoder.decode(token);
  }

  private OAuth2TokenValidator<Jwt> buildValidator(final String issuer, final List<String> audience) {
    OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(issuer);
    OAuth2TokenValidator<Jwt> audienceValidator = jwt -> {
      if (audience == null || audience.isEmpty()) {
        return OAuth2TokenValidatorResult.success();
      }
      boolean match = jwt.getAudience().stream().anyMatch(audience::contains);
      if (match) {
        return OAuth2TokenValidatorResult.success();
      }
      return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Invalid audience", null));
    };
    return new DelegatingOAuth2TokenValidator<>(issuerValidator, audienceValidator);
  }
}
