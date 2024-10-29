
package com.bytecoders.pharmaid;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.repository.model.UserType;
import com.bytecoders.pharmaid.request.LoginUserRequest;
import com.bytecoders.pharmaid.request.RegisterUserRequest;
import com.bytecoders.pharmaid.security.JwtTokenProvider;
import com.bytecoders.pharmaid.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
class PharmaidControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private UserService userService;

  @MockBean
  private JwtTokenProvider tokenProvider;

  @MockBean
  private PasswordEncoder passwordEncoder;

  private User testUser;
  private LoginUserRequest loginRequest;
  private RegisterUserRequest registerRequest;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId("testId");
    testUser.setEmail("test@example.com");
    testUser.setHashedPassword("hashedPassword");
    testUser.setUserType(UserType.PATIENT);

    loginRequest = new LoginUserRequest();
    loginRequest.setEmail("test@example.com");
    loginRequest.setPassword("password");

    registerRequest = new RegisterUserRequest();
    registerRequest.setEmail("test@example.com");
    registerRequest.setPassword("password");
    registerRequest.setUserType(UserType.PATIENT);
  }

  @Test
  void login_Success() throws Exception {
    // Given
    when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
    when(tokenProvider.generateToken(testUser.getId())).thenReturn("test.jwt.token");

    // When & Then
    mockMvc.perform(post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("Bearer test.jwt.token"))
        .andExpect(jsonPath("$.userId").value(testUser.getId()))
        .andExpect(jsonPath("$.email").value(testUser.getEmail()))
        .andExpect(jsonPath("$.userType").value(testUser.getUserType().toString()));
  }

  @Test
  void login_InvalidCredentials() throws Exception {
    // Given
    when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

    // When & Then
    mockMvc.perform(post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string("Invalid credentials"));
  }

  @Test
  void login_UserNotFound() throws Exception {
    // Given
    when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.empty());

    // When & Then
    mockMvc.perform(post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string("Invalid credentials"));
  }

  // Updated logout tests
  @Test
  void logout_Success() throws Exception {
    // Given
    when(tokenProvider.validateToken("valid.jwt.token")).thenReturn(true);

    // When & Then
    mockMvc.perform(post("/logout")
            .header("Authorization", "Bearer valid.jwt.token"))
        .andExpect(status().isOk())
        .andExpect(content().string("Logged out successfully"));
  }

  @Test
  void logout_InvalidToken() throws Exception {
    // When & Then
    mockMvc.perform(post("/logout")
            .header("Authorization", "InvalidToken"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Invalid token format"));
  }

  @Test
  void logout_NoToken() throws Exception {
    // When & Then
    mockMvc.perform(post("/logout"))
        .andExpect(status().isUnauthorized());
  }
}
