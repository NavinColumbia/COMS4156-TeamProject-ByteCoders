package com.bytecoders.pharmaid.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

class JwtAuthenticationFilterTest {

  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Mock private JwtTokenProvider tokenProvider;

  @Mock private UserService userService;

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Mock private FilterChain filterChain;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    jwtAuthenticationFilter = new JwtAuthenticationFilter();
    ReflectionTestUtils.setField(jwtAuthenticationFilter, "tokenProvider", tokenProvider);
    ReflectionTestUtils.setField(jwtAuthenticationFilter, "userService", userService);
    SecurityContextHolder.clearContext();
  }

  @Test
  void doFilterInternal_WithValidToken_ShouldAuthenticateUser() throws Exception {
    // Given
    String token = "valid.jwt.token";
    String userId = "test-user-id";
    User user = new User();
    user.setId(userId);
    user.setEmail("test@example.com");

    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(tokenProvider.validateToken(token)).thenReturn(true);
    when(tokenProvider.getUserIdFromJwt(token)).thenReturn(userId);
    when(userService.getUser(userId)).thenReturn(Optional.of(user));

    // When
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain).doFilter(request, response);
    assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    assertEquals(user, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
  }

  @Test
  void doFilterInternal_WithInvalidToken_ShouldNotAuthenticate() throws Exception {
    // Given
    when(request.getHeader("Authorization")).thenReturn("Bearer invalid.token");
    when(tokenProvider.validateToken(anyString())).thenReturn(false);

    // When
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void doFilterInternal_WithNoToken_ShouldNotAuthenticate() throws Exception {
    // Given
    when(request.getHeader("Authorization")).thenReturn(null);

    // When
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void doFilterInternal_WithMalformedToken_ShouldNotAuthenticate() throws Exception {
    // Given
    when(request.getHeader("Authorization")).thenReturn("malformed_token_without_bearer");

    // When
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void doFilterInternal_WithValidTokenButUserNotFound_ShouldNotAuthenticate() throws Exception {
    // Given
    String token = "valid.jwt.token";
    String userId = "test-user-id";

    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(tokenProvider.validateToken(token)).thenReturn(true);
    when(tokenProvider.getUserIdFromJwt(token)).thenReturn(userId);
    when(userService.getUser(userId)).thenReturn(Optional.empty());

    // When
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }
}
