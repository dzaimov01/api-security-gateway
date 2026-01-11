package com.acme.apigateway.security;

import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

public interface JwtTokenValidator {
  Mono<Jwt> decode(String token);
}
