package com.bytecoders.pharmaid.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import com.bytecoders.pharmaid.repository.UserRepository;
import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.request.LoginUserRequest;
import com.bytecoders.pharmaid.request.RegisterUserRequest;
import com.bytecoders.pharmaid.util.PasswordUtils;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

/**
 * Tests for {@link com.bytecoders.pharmaid.service.UserService}.
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceTests {
  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordUtils passwordUtils;

  @InjectMocks
  private UserService userService = new UserService();

  @Test
  public void testRegisterUser() {
    final RegisterUserRequest request = new RegisterUserRequest();
    request.setEmail("email@test.com");
    request.setPassword("password");

    final User expectedUser = new User();
    expectedUser.setId("autogeneratedId");
    expectedUser.setEmail("email@test.com");
    expectedUser.setHashedPassword("hashedPassword");

    doAnswer((Answer<User>) invocation -> {
      User user = invocation.getArgument(0);
      user.setId("autogeneratedId");
      return user;
    }).when(userRepository).save(any(User.class));
    when(passwordUtils.hashPassword("password")).thenReturn("hashedPassword");

    final User actualUser = userService.registerUser(request);
    assertEquals(actualUser, expectedUser);
  }

  @Test
  public void testLoginSuccess() {
    final LoginUserRequest request = new LoginUserRequest();
    request.setEmail("email@test.com");
    request.setPassword("password");

    final User mockUser = new User();
    mockUser.setId("autogeneratedId");
    mockUser.setEmail("email@test.com");
    mockUser.setHashedPassword("hashedPassword");

    when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(mockUser));
    when(passwordUtils.verifyPassword("password", "hashedPassword"))
        .thenReturn(true);

    final Optional<User> userOptional = userService.loginUser(request);
    assertEquals(userOptional, Optional.of(mockUser));
  }

  @Test
  public void testLoginNoSuchUser() {
    final LoginUserRequest request = new LoginUserRequest();
    request.setEmail("email@test.com");
    request.setPassword("password");

    when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

    final Optional<User> userOptional = userService.loginUser(request);
    assertEquals(userOptional, Optional.empty());
  }

  @Test
  public void testLoginInvalidPassword() {
    final LoginUserRequest request = new LoginUserRequest();
    request.setEmail("email@test.com");
    request.setPassword("password");

    final User mockUser = new User();
    mockUser.setId("autogeneratedId");
    mockUser.setEmail("email@test.com");
    mockUser.setHashedPassword("hashedPassword");

    when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(mockUser));
    when(passwordUtils.verifyPassword("password", "hashedPassword"))
        .thenReturn(false);

    final Optional<User> userOptional = userService.loginUser(request);
    assertEquals(userOptional, Optional.empty());
  }
}
