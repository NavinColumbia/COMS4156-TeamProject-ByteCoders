package com.bytecoders.pharmaid.service;

import com.bytecoders.pharmaid.repository.UserRepository;
import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.request.LoginUserRequest;
import com.bytecoders.pharmaid.request.RegisterUserRequest;
import com.bytecoders.pharmaid.util.PasswordUtils;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service operations around {@link com.bytecoders.pharmaid.repository.model.User}.
 */
@Service
public class UserService {
  @Autowired
  private UserRepository userRepository;



  public User updateUser(String userId, User updatedUser) {
    return userRepository.findById(userId)
        .map(user -> {
          user.setEmail(updatedUser.getEmail());
          user.setOrganization(updatedUser.getOrganization());
          user.setUserType(updatedUser.getUserType());
          return userRepository.save(user);
        })
        .orElseThrow(() -> new RuntimeException("User not found"));
  }

  public void deleteUser(String userId) {
    userRepository.deleteById(userId);
  }
  public boolean existsByEmail(String email) {
    return userRepository.findByEmail(email).isPresent();
  }

  public User createUser(User user) {
    return userRepository.save(user);
  }

  public Optional<User> getUserById(String id) {
    return userRepository.findById(id);
  }

  public Optional<User> getUserByEmail(String email) {
    return userRepository.findByEmail(email);
  }


}