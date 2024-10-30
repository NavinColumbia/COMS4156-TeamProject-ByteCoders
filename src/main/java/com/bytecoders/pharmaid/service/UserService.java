package com.bytecoders.pharmaid.service;

import com.bytecoders.pharmaid.repository.UserRepository;
import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.request.LoginUserRequest;
import com.bytecoders.pharmaid.request.RegisterUserRequest;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/** Service operations around {@link com.bytecoders.pharmaid.repository.model.User}. */
@Service
public class UserService {
  @Autowired private UserRepository userRepository;


  /**
   * Creates a new user.
   *
   * @param user the user to create
   * @return the created user
   */
  public User createUser(User user) {
    // Validate email doesn't exist
    if (userRepository.findByEmail(user.getEmail()).isPresent()) {
      throw new DataIntegrityViolationException("Email already exists");
    }
    return userRepository.save(user);
  }

  public Optional<User> getUserByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  public Optional<User> getUser(String userId) {
    return userRepository.findById(userId);
  }
}
