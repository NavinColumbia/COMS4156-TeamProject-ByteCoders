package com.bytecoders.pharmaid;

import com.bytecoders.pharmaid.dto.AuthResponse;
import com.bytecoders.pharmaid.dto.LoginRequest;
import com.bytecoders.pharmaid.dto.RegisterRequest;
import com.bytecoders.pharmaid.dto.TokenRefreshRequest;
import com.bytecoders.pharmaid.repository.PrescriptionRepository;
import com.bytecoders.pharmaid.repository.RefreshTokenRepository;
import com.bytecoders.pharmaid.repository.SharingPermissionRepository;
import com.bytecoders.pharmaid.repository.UserRepository;
import com.bytecoders.pharmaid.repository.model.RefreshToken;
import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.security.JwtTokenProvider;
import com.bytecoders.pharmaid.service.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class LogoutRefreshTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RefreshTokenRepository refreshTokenRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private JwtTokenProvider tokenProvider;

  @Autowired
  private RefreshTokenService refreshTokenService;

  private User testUser;
  private String accessToken;
  private String refreshToken;

  @Autowired
  private SharingPermissionRepository sharingPermissionRepository;

  @Autowired
  private PrescriptionRepository prescriptionRepository;

  @BeforeEach
  public void setup() {
    // Clean up database before each test
    sharingPermissionRepository.deleteAll();
    refreshTokenRepository.deleteAll();
    prescriptionRepository.deleteAll();

    // Create a test user
    testUser = new User();
    testUser.setEmail("testuser3@example.com");
    testUser.setHashedPassword(passwordEncoder.encode("Password123!"));
    userRepository.save(testUser);

    // Generate JWT token for authentication
    accessToken = tokenProvider.generateToken(testUser);

    // Create a refresh token for the user
    RefreshToken refreshTokenEntity = refreshTokenService.createRefreshToken(testUser.getId());
    refreshToken = refreshTokenEntity.getToken();
  }

  // Existing test methods...

  @Test
  public void testRefreshToken_Success() throws Exception {
    TokenRefreshRequest refreshRequest = new TokenRefreshRequest();
    refreshRequest.setRefreshToken(refreshToken);

    mockMvc.perform(post("/api/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(refreshRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").exists())
        .andExpect(jsonPath("$.refreshToken").exists());

    // Optionally, verify that the old refresh token is no longer valid
    Optional<RefreshToken> oldToken = refreshTokenRepository.findByToken(refreshToken);
    assertTrue(oldToken.isEmpty());
  }


  @Test
  public void testRefreshToken_InvalidToken() throws Exception {
    TokenRefreshRequest refreshRequest = new TokenRefreshRequest();
    refreshRequest.setRefreshToken("invalid_refresh_token");

    mockMvc.perform(post("/api/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(refreshRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Refresh token is not in database!"));
  }
  @Test
  public void testLogoutUser_Success() throws Exception {
    mockMvc.perform(post("/api/auth/logout")
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(content().string("Logout successful"));

    // Verify that the refresh token has been deleted
    Optional<RefreshToken> deletedToken = refreshTokenRepository.findByToken(refreshToken);
    assertTrue(deletedToken.isEmpty());
  }
  @Test
  public void testLogoutUser_NoToken() throws Exception {
    mockMvc.perform(post("/api/auth/logout"))
        .andExpect(status().isBadRequest());
  }



}
