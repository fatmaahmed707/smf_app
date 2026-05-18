package com.smf.service.user;

import com.smf.dto.user.UserRequest;
import com.smf.dto.user.UserResponse;
import com.smf.model.Role;
import com.smf.model.User;
import com.smf.repo.UserRepository;
import com.smf.service.role.IRoleService;
import com.smf.util.AppError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private IRoleService roleService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_withDefaultRole() {

        UserRequest request = new UserRequest();
        request.setUsername("youssef");
        request.setEmail("test@mail.com");
        request.setPassword("password");
        request.setRoles(Collections.emptySet());

        Role role = new Role("ROLE_USER");

        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(roleService.findRoleByName("ROLE_USER")).thenReturn(role);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserResponse response = userService.createUser(request);

        assertEquals("youssef", response.getFullName());
        assertEquals("test@mail.com", response.getEmail());
    }

    @Test
    void createUser_withCustomRoles() {

        UserRequest request = new UserRequest();
        request.setUsername("admin");
        request.setEmail("admin@mail.com");
        request.setPassword("password");
        request.setRoles(Set.of("ROLE_ADMIN"));

        Role role = new Role("ROLE_ADMIN");

        when(userRepository.findByEmail("admin@mail.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(roleService.findRoleByName("ROLE_ADMIN")).thenReturn(role);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserResponse response = userService.createUser(request);

        assertEquals("admin", response.getFullName());
    }

    @Test
    void createUser_duplicateEmailShouldThrow() {

        UserRequest request = new UserRequest();
        request.setUsername("newuser");
        request.setEmail("existing@mail.com");
        request.setPassword("password");
        request.setRoles(Collections.emptySet());

        User existingUser = new User("existing@mail.com", "existing", "pass");

        when(userRepository.findByEmail("existing@mail.com")).thenReturn(Optional.of(existingUser));

        AppError error = assertThrows(AppError.class, () -> userService.createUser(request));
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
    }

    @Test
    void getUserById_success() {

        UUID id = UUID.randomUUID();
        User user = new User("mail@test.com","user","pass");
        user.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(id);

        assertEquals(id, response.getId());
    }

    @Test
    void getUserById_notFound() {

        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(AppError.class, () -> userService.getUserById(id));
    }

    @Test
    void getAllUsers_success() {

        User u1 = new User("a@mail.com","a","p");
        User u2 = new User("b@mail.com","b","p");

        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        List<UserResponse> users = userService.getAllUsers();

        assertEquals(2, users.size());
    }

    @Test
    void updateUser_success() {

        UUID id = UUID.randomUUID();

        UserRequest request = new UserRequest();
        request.setUsername("new");
        request.setEmail("new@mail.com");
        request.setPassword("password");

        User user = new User("old@mail.com","old","pass");
        user.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.updateUser(id, request);

        assertEquals("new@mail.com", response.getEmail());
    }

    @Test
    void deleteUser_success() {

        UUID id = UUID.randomUUID();

        when(userRepository.existsById(id)).thenReturn(true);

        userService.deleteUser(id);

        verify(userRepository).deleteById(id);
    }

    @Test
    void deleteUser_notFound() {

        UUID id = UUID.randomUUID();
        when(userRepository.existsById(id)).thenReturn(false);

        AppError error = assertThrows(AppError.class, () -> userService.deleteUser(id));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    }

    @Test
    void updateUser_duplicateEmailShouldThrow() {

        UUID id = UUID.randomUUID();
        UUID anotherUserId = UUID.randomUUID();
        User existingUser = new User("old@mail.com","old","pass");
        existingUser.setId(id);

        User anotherUser = new User("existing@mail.com", "other", "pass");
        anotherUser.setId(anotherUserId);

        UserRequest request = new UserRequest();
        request.setUsername("new");
        request.setEmail("existing@mail.com");
        request.setPassword("password");

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail("existing@mail.com")).thenReturn(Optional.of(anotherUser));

        AppError error = assertThrows(AppError.class, () -> userService.updateUser(id, request));
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
    }
}