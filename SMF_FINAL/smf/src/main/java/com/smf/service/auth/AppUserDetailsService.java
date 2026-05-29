package com.smf.service.auth;

import com.smf.model.User;
import com.smf.repo.UserRepository;
import com.smf.security.AppUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AppUserDetailsService implements UserDetailsService {
  private final UserRepository userRepo;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user =
        userRepo
            .findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    return AppUserDetails.buildUserDetails(user);
  }

  public UserDetails loadUserById(String id) throws UsernameNotFoundException {
    User user =
        userRepo
            .findById(java.util.UUID.fromString(id))
            .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
    return AppUserDetails.buildUserDetails(user);
  }
}
