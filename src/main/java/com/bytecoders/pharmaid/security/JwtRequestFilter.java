package com.bytecoders.pharmaid.security;

import com.bytecoders.pharmaid.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Spring security filter to intercept HTTP requests and validate JWT token in Auth header.
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

  @Autowired
  private JwtUtils jwtUtil;

  /**
   * Filter incoming requests to authenticate users based on JWT token Auth header.
   *
   * @param request  the HTTP request to be processed
   * @param response the HTTP response to be generated
   * @param chain    the filter chain to pass control to the next filter
   * @throws ServletException if an error occurs during the filter process
   * @throws IOException      if an input/output error occurs during the filter process
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {

    final String jwt = extractJwtFromHeader(request);
    final String userId = extractUserIdFromJwt(jwt);

    if (userId != null && isAuthenticationNull() && jwtUtil.isTokenValid(jwt, userId)) {
      setUpAuthentication(userId, request);
    }

    chain.doFilter(request, response);
  }

  /**
   * Extract JWT token from Auth header.
   *
   * @param request HTTP request with Auth header
   * @return if present, JWT token as a String prefixed with "Bearer "; else null
   */
  private String extractJwtFromHeader(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7); // remove "Bearer " prefix
    }
    return null; // No valid JWT found
  }

  private String extractUserIdFromJwt(String jwt) {
    if (jwt != null) {
      return jwtUtil.extractUserId(jwt);
    }
    return null;
  }

  /**
   * Checks if the current SecurityContext has no authentication set.
   *
   * @return true if no authentication is present; else false
   */
  private boolean isAuthenticationNull() {
    return SecurityContextHolder.getContext().getAuthentication() == null;
  }

  /**
   * Set up the authentication in SecurityContext using provided userId.
   *
   * @param userId  userId
   * @param request HTTP request to build auth details
   */
  private void setUpAuthentication(String userId, HttpServletRequest request) {
    UsernamePasswordAuthenticationToken
        authToken =
        new UsernamePasswordAuthenticationToken(userId, null, new java.util.ArrayList<>());

    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authToken);
  }
}
