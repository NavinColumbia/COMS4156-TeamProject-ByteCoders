package com.bytecoders.pharmaid.security;

import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter that processes JWT authentication for each request. Extends OncePerRequestFilter to
 * guarantee a single execution per request dispatch.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
  private static final String BEARER_PREFIX = "Bearer ";

  @Autowired private JwtTokenProvider tokenProvider;

  @Autowired private UserService userService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      String jwt = getJwtFromRequest(request);

      if (jwt != null && tokenProvider.validateToken(jwt)) {
        String userId = tokenProvider.getUserIdFromJwt(jwt);

        // Debug print
        System.out.println("JWT Token validated for user: " + userId);

        Optional<User> userOptional = userService.getUser(userId);

        if (userOptional.isPresent()) {
          User user = userOptional.get();
          UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(user, null, null);
          authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authentication);

          // Debug print
          System.out.println("Authentication set for user: " + user.getEmail());
        }
      }
    } catch (Exception ex) {
      System.out.println("Could not set user authentication: " + ex.getMessage());
    }

    filterChain.doFilter(request, response);
  }

  /**
   * Extracts JWT token from the request header.
   *
   * @param request the HTTP request
   * @return the JWT token or null if not present or invalid format
   */
  private String getJwtFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
      return bearerToken.substring(BEARER_PREFIX.length());
    }
    return null;
  }
}
