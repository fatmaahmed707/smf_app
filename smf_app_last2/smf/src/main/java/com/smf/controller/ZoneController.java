package com.smf.controller;

import com.smf.dto.api.ApiResponse;
import com.smf.dto.zone.ZoneRequest;
import com.smf.dto.zone.ZoneResponse;
import com.smf.service.zone.IZoneService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/zones")
@PreAuthorize("hasAuthority('ADMIN')")
@RequiredArgsConstructor
public class ZoneController {

  private final IZoneService zoneService;

  @PostMapping("/")
  public ResponseEntity<ApiResponse> createZone(@Valid @RequestBody ZoneRequest request) {
    ZoneResponse response = zoneService.createZone(request);
    return ResponseEntity.ok(new ApiResponse(true, "Zone created successfully", response));
  }

  @GetMapping("/")
  public ResponseEntity<ApiResponse> getZones(@RequestParam(required = false) String name) {
    List<ZoneResponse> zones;
    if (name != null) {
      zones = zoneService.searchByName(name);
    } else {
      zones = zoneService.getAllZones();
    }

    return ResponseEntity.ok(new ApiResponse(true, "Zones fetched successfully", zones));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse> getZoneById(@PathVariable UUID id) {
    ZoneResponse zone = zoneService.getZoneById(id);
    return ResponseEntity.ok(new ApiResponse(true, "Zone fetched successfully", zone));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse> updateZone(
      @PathVariable UUID id, @Valid @RequestBody ZoneRequest request) {
    ZoneResponse updated = zoneService.updateZone(id, request);
    return ResponseEntity.ok(new ApiResponse(true, "Zone updated successfully", updated));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse> deleteZone(@PathVariable UUID id) {
    zoneService.deleteZone(id);
    return ResponseEntity.ok(new ApiResponse(true, "Zone deleted successfully", null));
  }

  @PostMapping("/{id}/roles/{roleId}")
  public ResponseEntity<ApiResponse> assignRoleToZone(
      @PathVariable UUID id, @PathVariable Long roleId) {
    zoneService.assignRoleToZone(id, roleId);
    return ResponseEntity.ok(new ApiResponse(true, "Role assigned to zone successfully", null));
  }

  @DeleteMapping("/{id}/roles/{roleId}")
  public ResponseEntity<ApiResponse> removeRoleFromZone(
      @PathVariable UUID id, @PathVariable Long roleId) {
    zoneService.removeRoleFromZone(id, roleId);
    return ResponseEntity.ok(new ApiResponse(true, "Role removed from zone successfully", null));
  }
}
