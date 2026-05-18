package com.smf.service.role;

import com.smf.dto.role.RoleRequest;
import com.smf.dto.role.RoleResponse;
import com.smf.model.Role;
import com.smf.repo.RoleRepository;
import com.smf.util.AppError;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleService implements IRoleService {

  private final RoleRepository roleRepository;

  @Override
  @Transactional
  public RoleResponse createRole(RoleRequest request) {
    if (roleRepository.findByRoleName(request.getRoleName()).isPresent()) {
      throw new AppError(HttpStatus.CONFLICT, "Role already exists");
    }

    Role role = new Role(request.getRoleName());
    role = roleRepository.save(role);

    return mapToResponse(role);
  }

  @Override
  public RoleResponse getRoleById(Long id) {
    Role role =
        roleRepository
            .findById(id)
            .orElseThrow(() -> new AppError(HttpStatus.NOT_FOUND, "Role not found"));

    return mapToResponse(role);
  }

  @Override
  public List<RoleResponse> getAllRoles() {
    return roleRepository.findAll().stream().map(this::mapToResponse).toList();
  }

  @Override
  @Transactional
  public RoleResponse updateRole(Long id, RoleRequest request) {
    Role role =
        roleRepository
            .findById(id)
            .orElseThrow(() -> new AppError(HttpStatus.NOT_FOUND, "Role not found"));

    role.setRoleName(request.getRoleName());
    role = roleRepository.save(role);

    return mapToResponse(role);
  }

  @Override
  @Transactional
  public void deleteRole(Long id) {
    if (!roleRepository.existsById(id)) {
      throw new AppError(HttpStatus.NOT_FOUND, "Role not found");
    }
    roleRepository.deleteById(id);
  }

  @Override
  public Role findRoleById(Long id) {
    return roleRepository
        .findById(id)
        .orElseThrow(() -> new AppError(HttpStatus.NOT_FOUND, "Role not found"));
  }

  @Override
  public Role findRoleByName(String name) {
    return roleRepository
        .findByRoleName(name)
        .orElseThrow(() -> new AppError(HttpStatus.NOT_FOUND, "Role not found: " + name));
  }

  private RoleResponse mapToResponse(Role role) {
    return new RoleResponse(role.getId(), role.getRoleName());
  }
}
