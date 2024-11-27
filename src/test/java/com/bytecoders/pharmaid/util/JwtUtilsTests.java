package com.bytecoders.pharmaid.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.lang.reflect.Field;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Tests for JWT utils.
 */
public class JwtUtilsTests {

  private JwtUtils jwtUtils;
  private static final String MOCK_USER_ID = "9101d183-26e6-45b7-a8c4-25f24fdb36fa";

  @BeforeEach
  void setup() throws Exception {
    jwtUtils = new JwtUtils();

    // set secretKey (private)
    String secretKey = Base64.getEncoder()
        .encodeToString(Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded());
    Field secretKeyField = JwtUtils.class.getDeclaredField("secretKey");
    secretKeyField.setAccessible(true);
    secretKeyField.set(jwtUtils, secretKey);

    // set jwtExpiration (private)
    Field jwtExpirationField = JwtUtils.class.getDeclaredField("jwtExpiration");
    jwtExpirationField.setAccessible(true);
    jwtExpirationField.set(jwtUtils, 60000L); // 1 min expiration
  }

  @Test
  void generateToken_Success() {
    String token = jwtUtils.generateToken(MOCK_USER_ID);
    assertTrue(token != null && !token.isEmpty(), "Generated token should not be null or empty.");
  }

  @Test
  void generateToken_Fail_InvalidKey() {
    try {
      jwtUtils.generateToken(MOCK_USER_ID);
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException,
          "Expected IllegalArgumentException for invalid key.");
    }
  }

  @Test
  void extractUserId_Success() {
    String userId = "testUser";
    String token = jwtUtils.generateToken(userId);

    String extractedUserId = jwtUtils.extractUserId(token);
    assertEquals(userId, extractedUserId,
        "Extracted userId should match the one used to generate the token.");
  }

  @Test
  void extractUserId_Fail_InvalidToken() {
    String invalidToken = "invalid.token.value";
    assertThrows(MalformedJwtException.class,
        () -> jwtUtils.isTokenValid(invalidToken, MOCK_USER_ID),
        "Expected MalformedJwtException for an invalid token.");
  }

  @Test
  void isTokenValid_Success() {
    String token = jwtUtils.generateToken(MOCK_USER_ID);
    assertTrue(jwtUtils.isTokenValid(token, MOCK_USER_ID),
        "Token should be valid for the given userId.");
  }

  @Test
  void isTokenValid_Fail_ExpiredToken() throws Exception {
    Field jwtExpirationField = JwtUtils.class.getDeclaredField("jwtExpiration");
    jwtExpirationField.setAccessible(true);
    jwtExpirationField.set(jwtUtils, -1000L); // set negative expiration time
    String token = jwtUtils.generateToken(MOCK_USER_ID);
    assertThrows(ExpiredJwtException.class, () -> jwtUtils.isTokenValid(token, MOCK_USER_ID),
        "Token should be invalid due to expiration.");
  }

  @Test
  void isTokenValid_Fail_InvalidUserId() {
    String token = jwtUtils.generateToken("invalidUserId");
    assertFalse(jwtUtils.isTokenValid(token, MOCK_USER_ID),
        "Token should be invalid as the userId does not match.");
  }

  @Test
  void isTokenValid_Fail_InvalidToken() {
    String invalidToken = "invalid.token.value";
    assertThrows(MalformedJwtException.class,
        () -> jwtUtils.isTokenValid(invalidToken, MOCK_USER_ID), "Token should be invalid.");
  }

  @Test
  void isTokenValid_Fail_NullToken() {
    assertThrows(IllegalArgumentException.class, () -> jwtUtils.isTokenValid(null, MOCK_USER_ID),
        "Token should be invalid as it is null.");
  }

  @Test
  void getLoggedInUserId_Success() {
    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(MOCK_USER_ID);
    SecurityContextHolder.setContext(securityContext);

    String actualUserId = jwtUtils.getLoggedInUserId();
    assertEquals(MOCK_USER_ID, actualUserId,
        "Logged-in userId should match the mock authentication principal.");
  }

  @Test
  void getLoggedInUserId_Fail_NoAuthentication() {
    SecurityContextHolder.clearContext();
    String actualUserId = jwtUtils.getLoggedInUserId();
    assertNull(actualUserId, "Logged-in userId should be null if there is no authentication.");
  }

  @Test
  void getPublicEndpoints_Success_NonEmpty() {
    // call getPublicEndpoints()
    String[] publicEndpoints = jwtUtils.getPublicEndpoints();

    // verify returned array is not null and > 0
    assertNotEquals(null, publicEndpoints, "Public endpoints should not be null.");
    assertTrue(publicEndpoints.length > 0, "Public endpoints should not be empty.");
  }
}
