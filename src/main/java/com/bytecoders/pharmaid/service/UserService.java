package com.bytecoders.pharmaid.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bytecoders.pharmaid.repository.PrescriptionRepository;
import com.bytecoders.pharmaid.repository.UserRepository;
import com.bytecoders.pharmaid.repository.model.User;

import jakarta.transaction.Transactional;

/**
 * Service operations around {@link com.bytecoders.pharmaid.repository.model.User}.
 */
@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PrescriptionRepository prescriptionRepository;


  
  /** 
   * @param userId
   * @param updatedUser
   * @return User
   */
  public User updateUser(String userId, User updatedUser) {
    return userRepository.findById(userId)
        .map(user -> {
          user.setEmail(updatedUser.getEmail());
          return userRepository.save(user);
        })
        .orElseThrow(() -> new RuntimeException("User not found"));
  }

  @Transactional
  public void deleteUser(String userId) {
    // First, delete associated prescriptions (or other related entities)
    prescriptionRepository.deleteByUserId(userId);

    // Now delete the user
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