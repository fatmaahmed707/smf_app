package com.smf.controller;

import com.smf.dto.api.ApiResponse;
import com.smf.dto.device.SmfDeviceResponse;
import com.smf.service.smfdevice.ISmfDeviceService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/smfdevices")
@PreAuthorize("hasAuthority('ADMIN')")
@RequiredArgsConstructor
public class SmfDeviceController {

  private final ISmfDeviceService smfDeviceService;

  @GetMapping("/")
  public ResponseEntity<ApiResponse> getAllDevices() {
    List<SmfDeviceResponse> devices = smfDeviceService.getAllDevices();
    return ResponseEntity.ok(new ApiResponse(true, "Devices fetched successfully", devices));
  }

  @GetMapping("/unregistered")
  public ResponseEntity<ApiResponse> getUnregisteredDevices() {
    List<SmfDeviceResponse> devices = smfDeviceService.getUnregisteredDevices();
    return ResponseEntity.ok(
        new ApiResponse(true, "Unregistered devices fetched successfully", devices));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse> getDeviceById(@PathVariable UUID id) {
    SmfDeviceResponse response = smfDeviceService.getDeviceById(id);
    return ResponseEntity.ok(new ApiResponse(true, "Device fetched successfully", response));
  }

  @GetMapping("/label/{label}")
  public ResponseEntity<ApiResponse> getByLabel(@PathVariable String label) {
    SmfDeviceResponse response = smfDeviceService.getByLabel(label);
    return ResponseEntity.ok(new ApiResponse(true, "Device fetched successfully", response));
  }
}
