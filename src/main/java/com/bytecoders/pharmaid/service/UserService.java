package com.bytecoders.pharmaid.service;

import com.bytecoders.pharmaid.openapi.model.LoginUserRequest;
import com.bytecoders.pharmaid.openapi.model.LoginUserResponse;
import com.bytecoders.pharmaid.openapi.model.RegisterUserRequest;
import com.bytecoders.pharmaid.repository.UserRepository;
import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.util.JwtUtils;
import com.bytecoders.pharmaid.util.PasswordUtils;
import com.bytecoders.pharmaid.util.ServiceUtils;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service operations around {@link User}.
 */
@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordUtils passwordUtils;

  @Autowired
  private JwtUtils jwtUtils;

  @Autowired
  private ServiceUtils serviceUtils;

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
    newUser.setUserType(registerUserRequest.getUserType());
    return userRepository.save(newUser);
  }

  /**
   * Delete a user service.
   *
   * @param userId String of user id
   */
  public void deleteUser(String userId) {
    if (getUser(userId) != null) {
      userRepository.deleteById(userId);
    }
  }

  /**
   * Login user service.
   *
   * @param loginUserRequest request
   * @return the authenticated user
   */
  public Optional<LoginUserResponse> loginUser(LoginUserRequest loginUserRequest) {
    Optional<User> userWithEmail = userRepository.findByEmail(loginUserRequest.getEmail());

    if (userWithEmail.isEmpty()) {
      return Optional.empty();
    }

    final boolean isCorrectPassword = passwordUtils.verifyPassword(loginUserRequest.getPassword(),
        userWithEmail.get().getHashedPassword());

    if (isCorrectPassword) {
      String token = jwtUtils.generateToken(userWithEmail.get().getId());
      User user = userWithEmail.get();

      LoginUserResponse loginResponse = new LoginUserResponse();
      loginResponse.setUserId(user.getId());
      loginResponse.setEmail(user.getEmail());
      loginResponse.setToken(token);

      return Optional.of(loginResponse);
    }
    return Optional.empty();
  }

  /**
   * Returns a User or throws a ResponseStatusException.
   *
   * @param userId Id of the User
   */
  public User getUser(String userId) {
    return serviceUtils.findEntityById(userId, "user", userRepository);
  }
}