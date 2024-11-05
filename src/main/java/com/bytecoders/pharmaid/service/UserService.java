package com.bytecoders.pharmaid.service;

import com.bytecoders.pharmaid.openapi.model.LoginUserRequest;
import com.bytecoders.pharmaid.openapi.model.RegisterUserRequest;
import com.bytecoders.pharmaid.repository.UserRepository;
import com.bytecoders.pharmaid.repository.model.User;
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

  @Autowired
  private PasswordUtils passwordUtils;

  /**
   * Register new user service.
   *
   * @param registerUserRequest request
   * @return User the newly created user
   */
  public User registerUser(RegisterUserRequest registerUserRequest) {
    final User newUser = new User();
    newUser.setEmail(registerUserRequest.getEmail());
    newUser.setHashedPassword(passwordUtils.hashPassword(registerUserRequest.getPassword()));
    return userRepository.save(newUser);
  }

  /**
   * Login user service.
   *
   * @param loginUserRequest request
   * @return the authenticated user
   */
  public Optional<User> loginUser(LoginUserRequest loginUserRequest) {
    Optional<User> userWithEmail = userRepository.findByEmail(loginUserRequest.getEmail());

    if (userWithEmail.isEmpty()) {
      return Optional.empty();
    }

    final boolean
        isCorrectPassword =
        passwordUtils.verifyPassword(loginUserRequest.getPassword(),
            userWithEmail.get().getHashedPassword());

    return isCorrectPassword ? userWithEmail : Optional.empty();
  }

  public Optional<User> getUser(String userId) {
    return userRepository.findById(userId);
  }
}
