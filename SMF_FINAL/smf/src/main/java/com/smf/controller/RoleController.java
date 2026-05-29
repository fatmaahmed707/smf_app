package com.smf.controller;

import com.smf.dto.api.ApiResponse;
import com.smf.dto.role.RoleRequest;
import com.smf.dto.role.RoleResponse;
import com.smf.service.role.IRoleService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/roles")
@PreAuthorize("hasAuthority('ADMIN')")
@AllArgsConstructor
public class RoleController {

  private final IRoleService roleService;

  @PostMapping("/")
  public ResponseEntity<ApiResponse> createRole(@Valid @RequestBody RoleRequest request) {
    RoleResponse response = roleService.createRole(request);
    return ResponseEntity.ok(new ApiResponse(true, "Role created successfully", response));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse> getRole(@PathVariable Long id) {
    RoleResponse response = roleService.getRoleById(id);
    return ResponseEntity.ok(new ApiResponse(true, "Role fetched successfully", response));
  }

  @GetMapping("/")
  public ResponseEntity<ApiResponse> getAllRoles() {
    List<RoleResponse> roles = roleService.getAllRoles();
    return ResponseEntity.ok(new ApiResponse(true, "Roles fetched successfully", roles));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse> updateRole(
      @PathVariable Long id, @Valid @RequestBody RoleRequest request) {

    RoleResponse response = roleService.updateRole(id, request);
    return ResponseEntity.ok(new ApiResponse(true, "Role updated successfully", response));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse> deleteRole(@PathVariable Long id) {
    roleService.deleteRole(id);
    return ResponseEntity.ok(new ApiResponse(true, "Role deleted successfully", null));
  }
}
