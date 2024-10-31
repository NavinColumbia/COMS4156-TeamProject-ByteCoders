package com.bytecoders.pharmaid.exception.handler;

import com.bytecoders.pharmaid.exception.AuthenticationException;
import com.bytecoders.pharmaid.exception.NotAuthorizedException;
import com.bytecoders.pharmaid.exception.ResourceNotFoundException;
import com.bytecoders.pharmaid.exception.UserNotFoundException;
import com.bytecoders.pharmaid.response.ErrorResponse;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Global exception handler to centralize error responses and reduce duplication. */
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Handles authentication exceptions.
   *
   * @param ex the AuthenticationException
   * @return a ResponseEntity with error details and HTTP UNAUTHORIZED status
   */
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
    return buildErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED);
  }

  /**
   * Handles resource not found exceptions.
   *
   * @param ex the ResourceNotFoundException
   * @return a ResponseEntity with error details and HTTP NOT FOUND status
   */
  @ExceptionHandler({ResourceNotFoundException.class, UserNotFoundException.class})
  public ResponseEntity<ErrorResponse> handleNotFoundException(RuntimeException ex) {
    return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
  }

  /**
   * Handles not authorized exceptions.
   *
   * @param ex the NotAuthorizedException
   * @return a ResponseEntity with error details and HTTP FORBIDDEN status
   */
  @ExceptionHandler(NotAuthorizedException.class)
  public ResponseEntity<ErrorResponse> handleNotAuthorizedException(NotAuthorizedException ex) {
    return buildErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN);
  }

  /**
   * Handles general exceptions.
   *
   * @param ex the Exception
   * @return a ResponseEntity with error details and HTTP INTERNAL SERVER ERROR status
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
    return buildErrorResponse("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private ResponseEntity<ErrorResponse> buildErrorResponse(String message, HttpStatus status) {
    ErrorResponse errorResponse =
        ErrorResponse.builder().message(message).timestamp(LocalDateTime.now()).build();
    return ResponseEntity.status(status).body(errorResponse);
  }
}
