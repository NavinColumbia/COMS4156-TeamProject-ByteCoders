package com.bytecoders.pharmaid.security;

import com.bytecoders.pharmaid.repository.model.User;
import java.util.Collection;
import java.util.Collections;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** Custom implementation of Spring Security's UserDetails. */
public class CustomUserDetails implements UserDetails {

  private static final long serialVersionUID = 1L;

  private final transient User user;

  /**
   * Constructs a new CustomUserDetails.
   *
   * @param user the user entity
   */
  public CustomUserDetails(User user) {
    this.user = user;
  }

  /**
   * Gets the user entity.
   *
   * @return the user entity
   */
  public User getUser() {
    return user;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.emptyList();
  }

  @Override
  public String getPassword() {
    return user.getHashedPassword();
  }

  @Override
  public String getUsername() {
    return user.getEmail();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
