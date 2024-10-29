package com.bytecoders.pharmaid.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

  private JwtTokenProvider tokenProvider;
  private static final String TEST_SECRET = "testSecretKeyThatIsLongEnoughForHS512Algorithm";
  private static final int TEST_EXPIRATION = 3600000; // 1 hour
  private static final String TEST_USER_ID = "test-user-123";

  @BeforeEach
  void setUp() {
    tokenProvider = new JwtTokenProvider();
    ReflectionTestUtils.setField(tokenProvider, "jwtSecret", TEST_SECRET);
    ReflectionTestUtils.setField(tokenProvider, "jwtExpirationMs", TEST_EXPIRATION);
    tokenProvider.init();
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
    String expiredToken = Jwts.builder()
        .setSubject(TEST_USER_ID)
        .setIssuedAt(new Date(System.currentTimeMillis() - 3600000))
        .setExpiration(new Date(System.currentTimeMillis() - 1000))
        .signWith(ReflectionTestUtils.getField(tokenProvider, "key"))
        .compact();

    // When & Then
    assertFalse(tokenProvider.validateToken(expiredToken));
  }

  @Test
  void validateToken_ShouldReturnFalseForInvalidSignature() {
    // Given
    String invalidToken = Jwts.builder()
        .setSubject(TEST_USER_ID)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 3600000))
        .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor("differentSecret".repeat(4).getBytes(StandardCharsets.UTF_8)))
        .compact();

    // When & Then
    assertFalse(tokenProvider.validateToken(invalidToken));
  }
}