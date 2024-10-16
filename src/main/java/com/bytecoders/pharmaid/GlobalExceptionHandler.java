package com.bytecoders.pharmaid;

import java.util.List;
import java.util.Map;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler.
 */
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
        .map(DefaultMessageSourceResolvable::getDefaultMessage)
        .toList();
    return new ResponseEntity<>(Map.of("errors", errors), HttpStatus.BAD_REQUEST);
  }
}
