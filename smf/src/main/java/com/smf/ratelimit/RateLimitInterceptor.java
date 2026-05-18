package com.smf.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smf.dto.api.ApiResponse;
import com.smf.security.AppUserDetails;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.smf.util.HmacUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

  private static final String HEADER_DEVICE_MAC = "X-Device-Mac";
  private static final String ANONYMOUS_USER = "anonymousUser";
  private static final String ANONYMOUS_RATELIMIT_SECRET = "smf-rate-limit-secret-change-in-prod";

  private final com.github.benmanes.caffeine.cache.Cache<String, Bucket> cache = Caffeine.newBuilder()
      .maximumSize(10000)
      .expireAfterAccess(1, TimeUnit.HOURS)
      .build();
  private final ObjectMapper objectMapper;

  public RateLimitInterceptor(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {

    if (!(handler instanceof HandlerMethod handlerMethod)) {
      return true;
    }

    RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
    if (rateLimit == null) {
      return true;
    }

    String key = resolveKey(request);
    Bucket bucket = cache.get(key, ignored -> createBucket(rateLimit));

    var probe = bucket.tryConsumeAndReturnRemaining(1);
    if (!probe.isConsumed()) {
      long retryAfter = Duration.ofNanos(probe.getNanosToWaitForRefill()).getSeconds();

      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setContentType("application/json");

      ApiResponse apiResponse =
          new ApiResponse(false, "Too many requests", Map.of("retryAfter", retryAfter));
      response.getWriter().write(objectMapper.writeValueAsString(apiResponse));

      return false;
    }

    return true;
  }

  private Bucket createBucket(RateLimit rateLimit) {
    return Bucket.builder()
        .addLimit(Bandwidth.simple(rateLimit.capacity(), Duration.ofSeconds(rateLimit.period())))
        .build();
  }

  private String resolveKey(HttpServletRequest request) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null
        && auth.isAuthenticated()
        && !ANONYMOUS_USER.equals(auth.getPrincipal())) {
      Object principal = auth.getPrincipal();
      if (principal instanceof AppUserDetails userDetails) {
        UUID userId = userDetails.getId();
        if (userId != null) {
          return "user:" + userId;
        }
      }
    }

    String macAddress = request.getHeader(HEADER_DEVICE_MAC);
    if (macAddress != null && !macAddress.isBlank()) {
      return "mac:" + macAddress;
    }

    String ipAddress = request.getRemoteAddr();
    if (ipAddress == null || ipAddress.isBlank()) {
      ipAddress = "unknown";
    }
    String userAgent = request.getHeader("User-Agent");
    if (userAgent == null || userAgent.isBlank()) {
      userAgent = "unknown";
    }
    String payload = ipAddress + ":" + userAgent;
    String anonKey;
    try {
      anonKey = HmacUtil.computeSignature(payload, 0L, ANONYMOUS_RATELIMIT_SECRET);
    } catch (Exception e) {
      anonKey = payload; // fallback
    }
    return "anon:" + anonKey;
  }
}

