package com.smf.service.role;

import com.smf.dto.role.RoleRequest;
import com.smf.dto.role.RoleResponse;
import com.smf.model.Role;
import com.smf.repo.RoleRepository;
import com.smf.util.AppError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    @Test
    void createRole_success() {

        RoleRequest request = new RoleRequest();
        request.setRoleName("ADMIN");

        Role role = new Role("ADMIN");
        role.setId(1L);

        when(roleRepository.findByRoleName("ADMIN")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        RoleResponse response = roleService.createRole(request);

        assertNotNull(response);
        assertEquals("ADMIN", response.getRoleName());
    }

    @Test
    void createRole_shouldThrowIfRoleExists() {

        RoleRequest request = new RoleRequest();
        request.setRoleName("ADMIN");

        when(roleRepository.findByRoleName("ADMIN"))
                .thenReturn(Optional.of(new Role("ADMIN")));

        AppError error = assertThrows(AppError.class, () -> {
            roleService.createRole(request);
        });

        assertEquals(HttpStatus.CONFLICT, error.getStatus());
    }

    @Test
    void getRoleById_success() {

        Role role = new Role("ADMIN");
        role.setId(1L);

        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

        RoleResponse response = roleService.getRoleById(1L);

        assertEquals(1L, response.getId());
        assertEquals("ADMIN", response.getRoleName());
    }

    @Test
    void getRoleById_notFound() {

        when(roleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AppError.class, () -> {
            roleService.getRoleById(1L);
        });
    }

    @Test
    void getAllRoles_success() {

        Role role1 = new Role("ADMIN");
        role1.setId(1L);

        Role role2 = new Role("USER");
        role2.setId(2L);

        when(roleRepository.findAll()).thenReturn(List.of(role1, role2));

        List<RoleResponse> roles = roleService.getAllRoles();

        assertEquals(2, roles.size());
    }

    @Test
    void updateRole_success() {

        RoleRequest request = new RoleRequest();
        request.setRoleName("SUPER_ADMIN");

        Role role = new Role("ADMIN");
        role.setId(1L);

        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        RoleResponse response = roleService.updateRole(1L, request);

        assertEquals("SUPER_ADMIN", response.getRoleName());
    }

    @Test
    void deleteRole_success() {

        when(roleRepository.existsById(1L)).thenReturn(true);

        roleService.deleteRole(1L);

        verify(roleRepository).deleteById(1L);
    }

    @Test
    void deleteRole_notFound() {

        when(roleRepository.existsById(1L)).thenReturn(false);

        assertThrows(AppError.class, () -> {
            roleService.deleteRole(1L);
        });
    }

    @Test
    void findRoleByName_success() {

        Role role = new Role("ADMIN");
        role.setId(1L);

        when(roleRepository.findByRoleName("ADMIN")).thenReturn(Optional.of(role));

        Role result = roleService.findRoleByName("ADMIN");

        assertEquals("ADMIN", result.getRoleName());
    }

    @Test
    void findRoleByName_notFound() {

        when(roleRepository.findByRoleName("ADMIN")).thenReturn(Optional.empty());

        assertThrows(AppError.class, () -> {
            roleService.findRoleByName("ADMIN");
        });
    }
}