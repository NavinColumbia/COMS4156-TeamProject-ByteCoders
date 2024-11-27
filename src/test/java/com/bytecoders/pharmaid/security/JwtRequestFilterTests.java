package com.bytecoders.pharmaid.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.bytecoders.pharmaid.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/** JwtRequestFiler tests. */
public class JwtRequestFilterTests {

  @InjectMocks
  private JwtRequestFilter jwtRequestFilter;

  @Mock
  private JwtUtils jwtUtils;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain filterChain;

  private static final String JWT_TOKEN = "test.jwt.token";
  private static final String PUBLIC_ENDPOINT = "/public-endpoint";
  private static final String PROTECTED_ENDPOINT = "/protected-endpoint";
  private static final String MOCK_USER_ID = "9101d183-26e6-45b7-a8c4-25f24fdb36fa";

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
    SecurityContextHolder.clearContext();
  }

  @Test
  void doFilterInternal_Success_UsePublicEndpoints() throws ServletException, IOException {
    when(request.getRequestURI()).thenReturn(PUBLIC_ENDPOINT);
    when(jwtUtils.getPublicEndpoints()).thenReturn(new String[]{PUBLIC_ENDPOINT});
    jwtRequestFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain, times(1)).doFilter(request, response);
    verify(jwtUtils, times(1)).getPublicEndpoints();
    verifyNoMoreInteractions(jwtUtils);
  }

  @Test
  void doFilterInternal_Fail_MissingAuthorizationHeader() throws ServletException, IOException {
    when(request.getRequestURI()).thenReturn(PROTECTED_ENDPOINT);
    when(jwtUtils.getPublicEndpoints()).thenReturn(new String[]{PROTECTED_ENDPOINT});
    when(request.getHeader("Authorization")).thenReturn(null); // No header
    jwtRequestFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain, times(1)).doFilter(request, response);
    verify(jwtUtils, times(1)).getPublicEndpoints();
    verifyNoMoreInteractions(jwtUtils);
  }

  @Test
  void doFilterInternal_Fail_InvalidJwt() throws ServletException, IOException {
    when(request.getRequestURI()).thenReturn(PROTECTED_ENDPOINT);
    when(jwtUtils.getPublicEndpoints()).thenReturn(new String[]{PUBLIC_ENDPOINT});
    when(request.getHeader("Authorization")).thenReturn("Bearer " + JWT_TOKEN);

    doThrow(new RuntimeException("Invalid JWT")).when(jwtUtils).extractUserId(JWT_TOKEN);

    assertThrows(RuntimeException.class,
        () -> jwtRequestFilter.doFilterInternal(request, response, filterChain),
        "Expected RuntimeException when JWT is invalid");

    verify(filterChain, never()).doFilter(request, response);
  }

  @Test
  void doFilterInternal_ValidJwtButNullUserId() throws ServletException, IOException {
    when(request.getRequestURI()).thenReturn(PROTECTED_ENDPOINT);
    when(jwtUtils.getPublicEndpoints()).thenReturn(new String[]{PUBLIC_ENDPOINT});
    when(request.getHeader("Authorization")).thenReturn("Bearer " + JWT_TOKEN);
    when(jwtUtils.extractUserId(JWT_TOKEN)).thenReturn(null);

    jwtRequestFilter.doFilterInternal(request, response, filterChain);
    verify(filterChain, times(1)).doFilter(request, response);
    verify(jwtUtils, times(1)).extractUserId(JWT_TOKEN);
    verify(jwtUtils, never()).isTokenValid(anyString(), anyString());
  }

  @Test
  void doFilterInternal_Success_ValidJwtAndUserId() throws ServletException, IOException {
    when(request.getRequestURI()).thenReturn(PROTECTED_ENDPOINT);
    when(jwtUtils.getPublicEndpoints()).thenReturn(new String[]{PUBLIC_ENDPOINT});
    when(request.getHeader("Authorization")).thenReturn("Bearer " + JWT_TOKEN);
    when(jwtUtils.extractUserId(JWT_TOKEN)).thenReturn(MOCK_USER_ID);
    when(jwtUtils.isTokenValid(JWT_TOKEN, MOCK_USER_ID)).thenReturn(true);

    jwtRequestFilter.doFilterInternal(request, response, filterChain);
    verify(filterChain, times(1)).doFilter(request, response);
    verify(jwtUtils, times(1)).extractUserId(JWT_TOKEN);
    verify(jwtUtils, times(1)).isTokenValid(JWT_TOKEN, MOCK_USER_ID);
    assertEquals(MOCK_USER_ID,
        SecurityContextHolder.getContext().getAuthentication().getPrincipal());
  }

  @Test
  void doFilterInternal_Fail_AuthenticationAlreadyExists() throws ServletException, IOException {
    when(request.getRequestURI()).thenReturn(PROTECTED_ENDPOINT);
    when(jwtUtils.getPublicEndpoints()).thenReturn(new String[]{PUBLIC_ENDPOINT});
    when(request.getHeader("Authorization")).thenReturn("Bearer " + JWT_TOKEN);
    when(jwtUtils.extractUserId(JWT_TOKEN)).thenReturn(MOCK_USER_ID);

    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    jwtRequestFilter.doFilterInternal(request, response, filterChain);

    verify(jwtUtils, never()).isTokenValid(anyString(), anyString());
    verify(filterChain, times(1)).doFilter(request, response);
  }

  @Test
  void extractJwtFromHeader_Success() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("Authorization")).thenReturn("Bearer " + JWT_TOKEN);
    String jwt = jwtRequestFilter.extractJwtFromHeader(request);
    assertEquals(JWT_TOKEN, jwt, "JWT should be extracted successfully.");
  }

  @Test
  void extractJwtFromHeader_Fail_NoHeader() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("Authorization")).thenReturn(null);
    String jwt = jwtRequestFilter.extractJwtFromHeader(request);
    assertNull(jwt, "JWT should be null when Authorization header is missing.");
  }

  @Test
  void extractJwtFromHeader_Fail_InvalidHeader() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("Authorization")).thenReturn(JWT_TOKEN);
    String jwt = jwtRequestFilter.extractJwtFromHeader(request);
    assertNull(jwt,
        "JWT should be null when Authorization header does not start with 'Bearer '.");
  }

  @Test
  void extractUserIdFromJwt_Success() {
    when(jwtUtils.extractUserId(JWT_TOKEN)).thenReturn(MOCK_USER_ID);
    String userId = jwtRequestFilter.extractUserIdFromJwt(JWT_TOKEN);
    assertEquals(MOCK_USER_ID, userId, "Extracted userId should match the expected value.");
  }

  @Test
  void extractUserIdFromJwt_Fail_NullJwt() {
    String userId = jwtRequestFilter.extractUserIdFromJwt(null);
    assertNull(userId, "userId should be null when JWT is null.");
  }

  @Test
  void isAuthenticationNull_True() {
    boolean result = jwtRequestFilter.isAuthenticationNull();
    assertTrue(result, "isAuthenticationNull should return true when no authentication is set.");
  }

  @Test
  void isAuthenticationNull_False() {
    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);
    assertFalse(jwtRequestFilter.isAuthenticationNull(),
        "isAuthenticationNull should return false when authentication is set.");
  }
}