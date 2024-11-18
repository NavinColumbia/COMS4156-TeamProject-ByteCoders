package com.bytecoders.pharmaid;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

/**
 * Global exception handler.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Handler for request body validation errors.
   *
   * @param ex MethodArgumentNotValidException
   * @return a ResponseEntity indicating BAD_REQUEST and what went wrong
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, List<String>>> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex) {
    final List<String> errors = ex.getBindingResult().getFieldErrors().stream()
        .map(DefaultMessageSourceResolvable::getDefaultMessage).toList();
    return new ResponseEntity<>(Map.of("errors", errors), HttpStatus.BAD_REQUEST);
  }

  /**
   * Handler for invalid enum values or other illegal arguments.
   *
   * @param ex IllegalArgumentException
   * @return a ResponseEntity indicating BAD_REQUEST with a detailed message
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> handleIllegalArgumentException(
      IllegalArgumentException ex) {
    log.error("Caught IllegalArgumentException: {}", ex.getMessage());
    return new ResponseEntity<>(Map.of("error", "Invalid input", "details", ex.getMessage()),
        HttpStatus.BAD_REQUEST);
  }

  /**
   * Handler for ResponseStatusException to handle HTTP-related errors.
   *
   * @param ex ResponseStatusException
   * @return ResponseEntity with the appropriate HTTP status and error message
   */
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String, String>> handleResponseStatusException(
      ResponseStatusException ex) {
    return new ResponseEntity<>(
        Map.of("error", ex.getReason(), "status", ex.getStatusCode().toString()),
        ex.getStatusCode());
  }

  /**
   * Handler for HttpMessageNotReadableException to requests with missing body.
   *
   * @param ex ResponseStatusException
   * @return ResponseEntity with the appropriate HTTP status and error message
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<String> handleMissingRequestBody(HttpMessageNotReadableException ex) {
    log.warn("Request body missing or invalid: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
        "The request body is missing or improperly formatted. Please provide the required data.");
  }
}
