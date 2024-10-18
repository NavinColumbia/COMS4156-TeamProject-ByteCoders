
package com.bytecoders.pharmaid;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bytecoders.pharmaid.dto.LoginRequest;
import com.bytecoders.pharmaid.dto.RegisterRequest;
import com.bytecoders.pharmaid.repository.PrescriptionRepository;
import com.bytecoders.pharmaid.repository.RefreshTokenRepository;
import com.bytecoders.pharmaid.repository.SharingPermissionRepository;
import com.bytecoders.pharmaid.repository.UserRepository;
import com.bytecoders.pharmaid.repository.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PrescriptionRepository prescriptionRepository;

  @Autowired
  private RefreshTokenRepository refreshTokenRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private SharingPermissionRepository sharingPermissionRepository;

  @BeforeEach
  public void setup() {
    sharingPermissionRepository.deleteAll();
    refreshTokenRepository.deleteAll();
    prescriptionRepository.deleteAll();
    userRepository.deleteAll();
  }


  @Test
  public void testRegisterUser_Success() throws Exception {
    RegisterRequest registerRequest = new RegisterRequest();
    registerRequest.setEmail("testuser1@example.com");
    registerRequest.setPassword("TestPassword123!");

    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isOk())
        .andExpect(content().string("User registered successfully!"));
  }

  @Test
  public void testRegisterUser_EmailAlreadyExists() throws Exception {
    // Create a user in the repository
    User existingUser = new User();
    existingUser.setEmail("existing1@example.com");
    existingUser.setHashedPassword(passwordEncoder.encode("password"));
    userRepository.save(existingUser);

    RegisterRequest registerRequest = new RegisterRequest();
    registerRequest.setEmail("existing1@example.com");
    registerRequest.setPassword("TestPassword123!");

    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Error: Email is already in use!"));
  }

  @Test
  public void testAuthenticateUser_Success() throws Exception {
    // First, register the user
    User user = new User();
    user.setEmail("loginuser1@example.com");
    user.setHashedPassword(passwordEncoder.encode("Password123!"));
    userRepository.save(user);

    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail("loginuser1@example.com");
    loginRequest.setPassword("Password123!");

    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").exists());
  }

  @Test
  public void testAuthenticateUser_InvalidCredentials() throws Exception {
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail("nonexistent@example.com");
    loginRequest.setPassword("WrongPassword!");

    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isUnauthorized());
  }

}

