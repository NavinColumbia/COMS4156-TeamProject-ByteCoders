package com.bytecoders.pharmaid.util;

import com.bytecoders.pharmaid.exception.AuthenticationException;
import com.bytecoders.pharmaid.repository.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for security-related functions, such as retrieving the current authenticated user
 * ID.
 */
public class SecurityUtils {

  /**
   * Retrieves the ID of the currently authenticated user.
   *
   * @return the ID of the current user.
   * @throws AuthenticationException if no user is authenticated.
   */
  public static String getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.isAuthenticated()) {
      Object principal = authentication.getPrincipal();
      if (principal instanceof User) {
        return ((User) principal).getId();
      } else if (principal instanceof org.springframework.security.core.userdetails.User) {
        return ((org.springframework.security.core.userdetails.User) principal).getUsername();
      } else if (principal instanceof String) {
        return (String) principal;
      }
    }
    throw new AuthenticationException("User not authenticated");
  }
}
