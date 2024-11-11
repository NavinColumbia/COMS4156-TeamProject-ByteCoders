package com.bytecoders.pharmaid.util;

import com.bytecoders.pharmaid.repository.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

/** For getting loggedin user */
public class SecurityUtils {

  /**
   * Gets id of logged in user.
   *
   * @return the ID of the logged in user.
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
    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated.");
  }
}
