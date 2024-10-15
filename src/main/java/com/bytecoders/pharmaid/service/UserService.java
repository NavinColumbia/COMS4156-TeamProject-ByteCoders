package com.bytecoders.pharmaid.service;

import com.bytecoders.pharmaid.repository.UserRepository;
import com.bytecoders.pharmaid.repository.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service operations around {@link com.bytecoders.pharmaid.repository.model.User}.
 */
@Service
public class UserService {
  @Autowired
  private UserRepository userRepository;

  public User createUser() {
    return userRepository.save(new User());
  }
}
