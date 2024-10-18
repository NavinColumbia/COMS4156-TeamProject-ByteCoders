package com.bytecoders.pharmaid.security;

import com.bytecoders.pharmaid.repository.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

  private User user;

  public CustomUserDetails(User user) {
    this.user = user;
  }

  public User getUser() {
    return user;
  }

  @Override
  public String getUsername() {
    return user.getId(); // Return user ID as username
  }

  @Override
  public String getPassword() {
    return user.getHashedPassword();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singleton(() -> "ROLE_" + user.getUserType().getName().toUpperCase());
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  // Other methods...
}
