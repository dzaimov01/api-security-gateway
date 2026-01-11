package com.acme.apigateway.security;

import com.acme.apigateway.config.GatewayProperties;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import reactor.core.publisher.Mono;

@Configuration
@EnableReactiveMethodSecurity
public class SecurityConfig {

  @Bean
  @Order(1)
  public SecurityWebFilterChain adminSecurityFilterChain(final ServerHttpSecurity http,
      final ReactiveJwtDecoder adminJwtDecoder) {
    return http
        .securityMatcher(ServerWebExchangeMatchers.pathMatchers("/admin/**"))
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .authorizeExchange(exchanges -> exchanges
            .anyExchange().hasRole("ADMIN"))
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt.jwtDecoder(adminJwtDecoder)))
        .build();
  }

  @Bean
  @Order(2)
  public SecurityWebFilterChain defaultSecurityFilterChain(final ServerHttpSecurity http) {
    return http
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .authorizeExchange(exchanges -> exchanges
            .pathMatchers("/actuator/health").permitAll()
            .anyExchange().permitAll())
        .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
        .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
        .build();
  }

  @Bean
  public ReactiveJwtDecoder adminJwtDecoder(final GatewayProperties properties) {
    GatewayProperties.JwtProperties jwt = properties.jwt();
    if (jwt.jwksUri() != null && !jwt.jwksUri().isBlank()) {
      NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withJwkSetUri(jwt.jwksUri()).build();
      decoder.setJwtValidator(buildValidator(jwt.issuer(), jwt.audience()));
      return decoder;
    }
    if (jwt.issuer() != null && !jwt.issuer().isBlank()) {
      return ReactiveJwtDecoders.fromIssuerLocation(jwt.issuer());
    }
    return token -> Mono.error(new IllegalStateException("JWT issuer or JWKS URI must be configured"));
  }

  private OAuth2TokenValidator<Jwt> buildValidator(final String issuer, final List<String> audience) {
    OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(issuer);
    OAuth2TokenValidator<Jwt> audienceValidator = token -> {
      if (audience == null || audience.isEmpty()) {
        return OAuth2TokenValidatorResult.success();
      }
      boolean match = token.getAudience().stream().anyMatch(audience::contains);
      if (match) {
        return OAuth2TokenValidatorResult.success();
      }
      return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Invalid audience", null));
    };
    return new DelegatingOAuth2TokenValidator<>(issuerValidator, audienceValidator);
  }
}
