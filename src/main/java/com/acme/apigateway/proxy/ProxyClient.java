package com.acme.apigateway.proxy;

import com.acme.apigateway.config.GatewayProperties;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Component
public class ProxyClient {
  private final WebClient client;
  private final Set<String> hopByHopHeaders;

  public ProxyClient(final GatewayProperties properties) {
    GatewayProperties.ProxyProperties proxy = properties.proxy();
    ConnectionProvider provider = ConnectionProvider.builder("gateway-proxy")
        .maxConnections(500)
        .pendingAcquireMaxCount(1000)
        .build();
    HttpClient httpClient = HttpClient.create(provider)
        .responseTimeout(proxy.responseTimeout())
        .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) proxy.connectTimeout().toMillis());
    this.client = WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .build();
    this.hopByHopHeaders = new HashSet<>(proxy.hopByHopHeaders());
  }

  public Mono<Void> forward(final ServerWebExchange exchange, final RouteDefinition route,
      final byte[] cachedBody) {
    URI upstream = buildUpstreamUri(exchange, route);
    HttpMethod method = exchange.getRequest().getMethod();
    WebClient.RequestBodySpec requestSpec = client.method(method).uri(upstream);

    HttpHeaders filteredHeaders = new HttpHeaders();
    exchange.getRequest().getHeaders().forEach((key, value) -> {
      if (!hopByHopHeaders.contains(key.toLowerCase())) {
        filteredHeaders.put(key, value);
      }
    });
    requestSpec.headers(headers -> headers.addAll(filteredHeaders));

    WebClient.RequestHeadersSpec<?> headersSpec;
    if (cachedBody != null && cachedBody.length > 0) {
      headersSpec = requestSpec.contentType(exchange.getRequest().getHeaders().getContentType())
          .body(BodyInserters.fromValue(cachedBody));
    } else {
      headersSpec = requestSpec;
    }

    return headersSpec.exchangeToMono(clientResponse -> {
      exchange.getResponse().setStatusCode(clientResponse.statusCode());
      exchange.getResponse().getHeaders().putAll(clientResponse.headers().asHttpHeaders());
      return exchange.getResponse().writeWith(clientResponse.bodyToFlux(org.springframework.core.io.buffer.DataBuffer.class));
    });
  }

  private URI buildUpstreamUri(final ServerWebExchange exchange, final RouteDefinition route) {
    String base = route.upstreamUrl();
    String path = exchange.getRequest().getURI().getRawPath();
    String query = exchange.getRequest().getURI().getRawQuery();
    StringBuilder target = new StringBuilder();
    target.append(base);
    if (base.endsWith("/") && path.startsWith("/")) {
      target.setLength(target.length() - 1);
    }
    target.append(path);
    if (query != null && !query.isBlank()) {
      target.append('?').append(query);
    }
    return URI.create(target.toString());
  }
}
