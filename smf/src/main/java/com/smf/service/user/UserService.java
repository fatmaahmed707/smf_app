package com.smf.service.user;

import com.smf.dto.user.UserRequest;
import com.smf.dto.user.UserResponse;
import com.smf.model.Role;
import com.smf.model.User;
import com.smf.repo.UserRepository;
import com.smf.service.role.IRoleService;
import com.smf.util.AppError;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

  private final UserRepository userRepository;
  private final IRoleService roleService;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public UserResponse createUser(UserRequest request) {
    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
      throw new AppError(HttpStatus.CONFLICT, "Email already in use");
    }

    String provider = request.getProvider();
    if (provider == null || "LOCAL".equalsIgnoreCase(provider)) {
      if (request.getPassword() == null || request.getPassword().isBlank()) {
        throw new AppError(HttpStatus.BAD_REQUEST, "Password is required for LOCAL accounts");
      }
    }

    String encodedPassword = passwordEncoder.encode(request.getPassword());

    User user = new User(request.getEmail(), request.getUsername(), encodedPassword);

    if (request.getProvider() != null) {
      user.setProvider(request.getProvider());
    }
    if (request.getGoogleId() != null) {
      user.setGoogleId(request.getGoogleId());
    }
    if (request.getPictureUrl() != null) {
      user.setPictureUrl(request.getPictureUrl());
    }

    Set<Role> roles = new HashSet<>();
    if (request.getRoles() != null && !request.getRoles().isEmpty()) {
      for (String roleName : request.getRoles()) {
        Role role = roleService.findRoleByName(roleName);
        roles.add(role);
      }
    } else {
      Role userRole = roleService.findRoleByName("ROLE_USER");
      roles.add(userRole);
    }
    user.setRoles(roles);

    user = userRepository.save(user);
    return mapToResponse(user);
  }

  @Override
  public UserResponse getUserById(UUID userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new AppError(HttpStatus.NOT_FOUND, "User not found"));
    return mapToResponse(user);
  }

  @Override
  public List<UserResponse> getAllUsers() {
    return userRepository.findAll().stream().map(this::mapToResponse).toList();
  }

  @Override
  @Transactional
  public UserResponse updateUser(UUID userId, UserRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new AppError(HttpStatus.NOT_FOUND, "User not found"));

    Optional<User> existingWithEmail = userRepository.findByEmail(request.getEmail());
    if (existingWithEmail.isPresent() && !existingWithEmail.get().getId().equals(userId)) {
      throw new AppError(HttpStatus.CONFLICT, "Email already in use");
    }

    user.setUsername(request.getUsername());
    user.setEmail(request.getEmail());

    if (request.getPassword() != null && !request.getPassword().isBlank()) {
      user.setPassword(passwordEncoder.encode(request.getPassword()));
    }

    if (request.getPictureUrl() != null) {
      user.setPictureUrl(request.getPictureUrl());
    }

    if (request.getRoles() != null && !request.getRoles().isEmpty()) {
      Set<Role> roles = new HashSet<>();
      for (String roleName : request.getRoles()) {
        Role role = roleService.findRoleByName(roleName);
        roles.add(role);
      }
      user.setRoles(roles);
    }

    user = userRepository.save(user);
    return mapToResponse(user);
  }

  @Override
  @Transactional
  public void deleteUser(UUID userId) {
    if (!userRepository.existsById(userId)) {
      throw new AppError(HttpStatus.NOT_FOUND, "User not found");
    }
    userRepository.deleteById(userId);
  }

  @Override
  public User findUserById(UUID userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new AppError(HttpStatus.NOT_FOUND, "User not found"));
  }

  @Override
  public UserResponse getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String userId = auth.getName();
    User user = userRepository
        .findById(UUID.fromString(userId))
        .orElseThrow(() -> new AppError(HttpStatus.NOT_FOUND, "User not found"));
    return mapToResponse(user);
  }

  private UserResponse mapToResponse(User user) {
    UserResponse response = new UserResponse(user.getId(), user.getUsername(), user.getEmail());
    response.setProvider(user.getProvider());
    response.setPictureUrl(user.getPictureUrl());
    response.setRoles(user.getRoles().stream().map(r -> r.getRoleName()).collect(java.util.stream.Collectors.toSet()));
    return response;
  }
}

