package com.bytecoders.pharmaid.security;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtTokenProviderTest {

  private JwtTokenProvider tokenProvider;
  private static final Key TEST_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512);
  private static final int TEST_EXPIRATION = 3600000; // 1 hour
  private static final String TEST_USER_ID = "test-user-123";

  @BeforeEach
  void setUp() {
    tokenProvider = new JwtTokenProvider();
    ReflectionTestUtils.setField(tokenProvider, "jwtExpirationMs", TEST_EXPIRATION);
    ReflectionTestUtils.setField(tokenProvider, "key", TEST_KEY);
  }

  @Test
  void generateToken_ShouldCreateValidToken() {
    // When
    String token = tokenProvider.generateToken(TEST_USER_ID);

    // Then
    assertNotNull(token);
    assertTrue(tokenProvider.validateToken(token));
    assertEquals(TEST_USER_ID, tokenProvider.getUserIdFromJwt(token));
  }

  @Test
  void validateToken_ShouldReturnFalseForExpiredToken() {
    // Given
    String expiredToken =
        Jwts.builder()
            .setSubject(TEST_USER_ID)
            .setIssuedAt(new Date(System.currentTimeMillis() - 3600000))
            .setExpiration(new Date(System.currentTimeMillis() - 1000))
            .signWith(TEST_KEY, SignatureAlgorithm.HS512)
            .compact();

    // When & Then
    assertFalse(tokenProvider.validateToken(expiredToken));
  }

  @Test
  void validateToken_ShouldReturnFalseForInvalidSignature() {
    // Given
    Key differentKey =
        Keys.secretKeyFor(SignatureAlgorithm.HS512); // Generate a different secure key
    String invalidToken =
        Jwts.builder()
            .setSubject(TEST_USER_ID)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 3600000))
            .signWith(differentKey, SignatureAlgorithm.HS512)
            .compact();

    // When & Then
    assertFalse(tokenProvider.validateToken(invalidToken));
  }

  @Test
  void getUserIdFromJwt_ShouldExtractCorrectUserId() {
    // Given
    String token = tokenProvider.generateToken(TEST_USER_ID);

    // When
    String extractedUserId = tokenProvider.getUserIdFromJwt(token);

    // Then
    assertEquals(TEST_USER_ID, extractedUserId);
  }

  @Test
  void validateToken_ShouldReturnTrueForValidToken() {
    // Given
    String token = tokenProvider.generateToken(TEST_USER_ID);

    // When & Then
    assertTrue(tokenProvider.validateToken(token));
  }

  @Test
  void validateToken_ShouldReturnFalseForMalformedToken() {
    // Given
    String malformedToken = "malformed.jwt.token";

    // When & Then
    assertFalse(tokenProvider.validateToken(malformedToken));
  }

  @Test
  void validateToken_ShouldReturnFalseForEmptyToken() {
    // When & Then
    assertFalse(tokenProvider.validateToken(""));
    assertFalse(tokenProvider.validateToken(null));
  }
}
