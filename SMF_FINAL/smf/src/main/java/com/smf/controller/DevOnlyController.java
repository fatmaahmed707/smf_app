package com.smf.controller;

import com.smf.dto.api.ApiResponse;
import com.smf.dto.device.SmfDeviceRequest;
import com.smf.dto.device.SmfDeviceResponse;
import com.smf.service.smfdevice.ISmfDeviceService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}")
@ConditionalOnBooleanProperty(name = "app.is-dev-mode", havingValue = true)
@RequiredArgsConstructor
public class DevOnlyController {

  private final ISmfDeviceService smfDeviceService;

  @PostMapping("/smfdevices/")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<ApiResponse> addDevice(@Valid @RequestBody SmfDeviceRequest request) {
    SmfDeviceResponse response = smfDeviceService.addDevice(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new ApiResponse(true, "Device added successfully", response));
  }

  @DeleteMapping("/smfdevices/{id}")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<Void> removeDevice(@PathVariable UUID id) {
    smfDeviceService.removeDevice(id);
    return ResponseEntity.noContent().build();
  }

  @PreAuthorize("hasAuthority('USER')")
  @GetMapping("/auth/isUser")
  public ResponseEntity<ApiResponse> testUser() {
    return ResponseEntity.ok(new ApiResponse(true, "You are an Authenticated user", null));
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @GetMapping("/auth/isAdmin")
  public ResponseEntity<ApiResponse> testAdmin() {
    return ResponseEntity.ok(new ApiResponse(true, "You are an Authenticated user", null));
  }

  @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
  @GetMapping("/auth/isAuthenticated")
  public ResponseEntity<ApiResponse> testAuth() {
    return ResponseEntity.ok(new ApiResponse(true, "You are an Authenticated user or admin", null));
  }
}
