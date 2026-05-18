package com.smf.security;

import com.smf.model.User;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@AllArgsConstructor
public class AppUserDetails implements UserDetails {
  private UUID id;
  private String email;
  private String password;
  private Collection<GrantedAuthority> authorities;

  public static AppUserDetails buildUserDetails(User user) {
    List<GrantedAuthority> authorities =
        user.getRoles().stream()
            .flatMap(
                role -> {
                  List<GrantedAuthority> roleAuths = new java.util.ArrayList<>();
                  roleAuths.add(new SimpleGrantedAuthority(role.getRoleName()));
                  if (role.isAdmin()) {
                    roleAuths.add(new SimpleGrantedAuthority("ADMIN"));
                  }
                  return roleAuths.stream();
                })
            .distinct()
            .collect(Collectors.toList());

    return new AppUserDetails(user.getId(), user.getEmail(), user.getPassword(), authorities);
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return email;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public void setEmail(String username) {
    this.email = username;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setAuthorities(Collection<GrantedAuthority> authorities) {
    this.authorities = authorities;
  }
}
