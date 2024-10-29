//package com.bytecoders.pharmaid.security;
//
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import com.bytecoders.pharmaid.repository.model.User;
//import com.bytecoders.pharmaid.service.UserService;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import java.util.Optional;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.core.context.SecurityContextHolder;
//
//@ExtendWith(MockitoExtension.class)
//class JwtAuthenticationFilterTest {
//
//  @Mock
//  private JwtTokenProvider tokenProvider;
//
//  @Mock
//  private UserService userService;
//
//  @Mock
//  private HttpServletRequest request;
//
//  @Mock
//  private HttpServletResponse response;
//
//  @Mock
//  private FilterChain filterChain;
//
//  @InjectMocks
//  private JwtAuthenticationFilter jwtAuthenticationFilter;
//
//  @BeforeEach
//  void setUp() {
//    SecurityContextHolder.clearContext();
//  }
//
//  @Test
//  void whenValidToken_thenAuthenticateUser() throws Exception {
//    // Given
//    String token = "valid.jwt.token";
//    String userId = "test-user-id";
//    User user = new User();
//    user.setId(userId);
//
//    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
//    when(tokenProvider.validateToken(token)).thenReturn(true);
//    when(tokenProvider.getUserIdFromJwt(token)).thenReturn(userId);
//    when(userService.getUser(userId)).thenReturn(Optional.of(user));
//
//    // When
//    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
//
//    // Then
//    assertNotNull(SecurityContextHolder.getContext().getAuthentication());
//    verify(filterChain).doFilter(request, response);
//  }
//}