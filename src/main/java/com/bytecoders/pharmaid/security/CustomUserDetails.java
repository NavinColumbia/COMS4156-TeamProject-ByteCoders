package com.bytecoders.pharmaid.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.bytecoders.pharmaid.repository.model.User;

/**
 *
 */
public class CustomUserDetails implements UserDetails {

  private User user;

  public CustomUserDetails(User user) {
    this.user = user;
  }

  
  /** 
   * @return User
   */
  public User getUser() {
    return user;
  }

  
  /** 
   * @return String
   */
  @Override
  public String getUsername() {
    return user.getEmail(); // Return email as username
  }

  @Override
  public String getPassword() {
    return user.getHashedPassword();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singleton(() -> "ROLE_USER");
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
