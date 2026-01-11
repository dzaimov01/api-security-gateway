package com.acme.apigateway.policy.impl;

import com.acme.apigateway.policy.RateLimitConfig;
import com.acme.apigateway.policy.RateLimitResult;
import com.acme.apigateway.policy.RateLimiter;
import java.time.Duration;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RedisRateLimiter implements RateLimiter {
  private final ReactiveStringRedisTemplate redisTemplate;

  public RedisRateLimiter(final ReactiveStringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public Mono<RateLimitResult> allow(final String key, final RateLimitConfig config) {
    String redisKey = "rate:" + key;
    Duration window = config.window();
    return redisTemplate.opsForValue().increment(redisKey)
        .flatMap(count -> redisTemplate.expire(redisKey, window).thenReturn(count))
        .map(count -> {
          if (count <= config.capacity()) {
            return RateLimitResult.allowed((int) (config.capacity() - count));
          }
          return RateLimitResult.denied();
        });
  }

  @Override
  public String storageType() {
    return "redis";
  }
}
