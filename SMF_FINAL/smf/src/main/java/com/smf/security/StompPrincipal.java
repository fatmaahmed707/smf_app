package com.smf.security;

import java.security.Principal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;

@Getter
@RequiredArgsConstructor
public class StompPrincipal implements Principal {
  private final String name;
  private final Authentication authentication;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    StompPrincipal that = (StompPrincipal) o;
    return name.equals(that.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}