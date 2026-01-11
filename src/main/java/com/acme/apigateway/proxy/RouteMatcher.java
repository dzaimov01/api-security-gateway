package com.acme.apigateway.proxy;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

@Component
public class RouteMatcher {
  private final PathPatternParser parser = new PathPatternParser();

  public Optional<RouteDefinition> match(final List<RouteDefinition> routes, final String path,
      final HttpMethod method) {
    return routes.stream()
        .filter(route -> method == null || route.methods().isEmpty() || route.methods().contains(method.name()))
        .map(route -> new MatchCandidate(route, parser.parse(route.pathPattern())))
        .filter(candidate -> candidate.pattern().matches(org.springframework.http.server.PathContainer.parsePath(path)))
        .sorted(Comparator.comparingInt((MatchCandidate c) -> c.pattern().getPatternString().length()).reversed())
        .map(MatchCandidate::route)
        .findFirst();
  }

  private record MatchCandidate(RouteDefinition route, PathPattern pattern) { }
}
