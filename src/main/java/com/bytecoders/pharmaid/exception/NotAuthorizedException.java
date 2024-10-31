package com.bytecoders.pharmaid.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


/**
 * Exception thrown when authorization fails.
 * Maps to HTTP 403 Forbidden response.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class NotAuthorizedException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public NotAuthorizedException(String message) {
    super(message);
  }
}
