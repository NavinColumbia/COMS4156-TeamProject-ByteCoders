package com.bytecoders.pharmaid.security;

import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  @Autowired
  private UserRepository userRepository;

  //Load user by email (used during authentication)
  @Override
  public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
    User user = userRepository.findByEmail(id)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with email : " + id));
    return new CustomUserDetails(user);
  }

  // Load user by ID (used in JwtAuthenticationFilter)
  public UserDetails loadUserById(String userId) throws UsernameNotFoundException {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
    return new CustomUserDetails(user);
  }

}
