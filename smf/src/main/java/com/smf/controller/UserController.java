package com.smf.controller;

import com.smf.dto.api.ApiResponse;
import com.smf.dto.user.UserRequest;
import com.smf.dto.user.UserResponse;
import com.smf.service.user.IUserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/users")
public class UserController {

  private final IUserService userService;

  @GetMapping("/me")
  @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
  public ResponseEntity<ApiResponse> getCurrentUser() {
    UserResponse response = userService.getCurrentUser();
    return ResponseEntity.ok(new ApiResponse(true, "User fetched successfully", response));
  }

  @PostMapping("/")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<ApiResponse> createUser(@Valid @RequestBody UserRequest request) {
    UserResponse response = userService.createUser(request);
    return ResponseEntity.ok(new ApiResponse(true, "User created successfully", response));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<ApiResponse> getUser(@PathVariable UUID id) {
    UserResponse response = userService.getUserById(id);
    return ResponseEntity.ok(new ApiResponse(true, "User fetched successfully", response));
  }

  @GetMapping("/")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<ApiResponse> getAllUsers() {
    List<UserResponse> users = userService.getAllUsers();
    return ResponseEntity.ok(new ApiResponse(true, "Users fetched successfully", users));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<ApiResponse> updateUser(
      @PathVariable UUID id, @Valid @RequestBody UserRequest request) {
    UserResponse response = userService.updateUser(id, request);
    return ResponseEntity.ok(new ApiResponse(true, "User updated successfully", response));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<ApiResponse> deleteUser(@PathVariable UUID id) {
    userService.deleteUser(id);
    return ResponseEntity.ok(new ApiResponse(true, "User deleted successfully", null));
  }
}
