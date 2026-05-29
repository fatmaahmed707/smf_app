package com.smf.service.user;

import com.smf.dto.user.UserRequest;
import com.smf.dto.user.UserResponse;
import com.smf.model.User;
import java.util.List;
import java.util.UUID;

public interface IUserService {

  UserResponse createUser(UserRequest request);

  UserResponse getUserById(UUID userId);

  List<UserResponse> getAllUsers();

  UserResponse updateUser(UUID userId, UserRequest request);

  void deleteUser(UUID userId);

  User findUserById(UUID userId);

  UserResponse getCurrentUser();
}
