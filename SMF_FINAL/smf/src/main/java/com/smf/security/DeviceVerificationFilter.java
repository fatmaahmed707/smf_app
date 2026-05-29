package com.smf.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smf.dto.api.ApiResponse;
import com.smf.service.deviceauth.IDeviceAuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@AllArgsConstructor
public class DeviceVerificationFilter extends OncePerRequestFilter {

  private static final String HEADER_MAC = "X-Device-Mac";
  private static final String HEADER_TIMESTAMP = "X-Device-Timestamp";
  private static final String HEADER_SIGNATURE = "X-Device-Signature";

  private final IDeviceAuthService deviceAuthService;
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String macAddress = request.getHeader(HEADER_MAC);
    String timestampStr = request.getHeader(HEADER_TIMESTAMP);
    String signature = request.getHeader(HEADER_SIGNATURE);

    if (!StringUtils.hasText(macAddress)
        || !StringUtils.hasText(timestampStr)
        || !StringUtils.hasText(signature)) {
      filterChain.doFilter(request, response);
      return;
    }

    long timestamp;
    try {
      timestamp = Long.parseLong(timestampStr);
    } catch (NumberFormatException e) {
      sendUnauthorized(response, "Invalid timestamp format");
      return;
    }

    try {
      boolean verified = deviceAuthService.verifyDevice(macAddress, timestamp, signature);
      if (!verified) {
        sendUnauthorized(response, "Invalid device signature");
        return;
      }

      var auth =
          new UsernamePasswordAuthenticationToken(
              macAddress, null, java.util.List.of(new SimpleGrantedAuthority("DEVICE")));
      SecurityContextHolder.getContext().setAuthentication(auth);

    } catch (InvalidKeyException | NoSuchAlgorithmException e) {
      sendUnauthorized(response, "Signature verification failed");
      return;
    } catch (Exception e) {
      sendUnauthorized(response, "Unauthorized");
      return;
    }

    filterChain.doFilter(request, response);
  }

  private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");
    response
        .getWriter()
        .write(objectMapper.writeValueAsString(new ApiResponse(false, message, null)));
  }
}
