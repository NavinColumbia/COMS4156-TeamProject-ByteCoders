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

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with email : " + email));

    return org.springframework.security.core.userdetails.User
        .withUsername(user.getEmail())
        .password(user.getHashedPassword())
        .authorities("ROLE_" + user.getUserType().getName().toUpperCase())
        .accountExpired(false)
        .accountLocked(false)
        .credentialsExpired(false)
        .disabled(false)
        .build();
  }

  public UserDetails loadUserById(String id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with id : " + id));

    return org.springframework.security.core.userdetails.User
        .withUsername(user.getEmail())
        .password(user.getHashedPassword())
        .authorities("ROLE_" + user.getUserType().getName().toUpperCase())
        .accountExpired(false)
        .accountLocked(false)
        .credentialsExpired(false)
        .disabled(false)
        .build();
  }
}