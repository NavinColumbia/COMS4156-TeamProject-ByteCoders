package com.bytecoders.pharmaid.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.bytecoders.pharmaid.repository.model.UserType;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for RegisterUserRequest class.
 */
class RegisterUserRequestTest {

  private Validator validator;
  private RegisterUserRequest request;

  @BeforeEach
  void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
    request = new RegisterUserRequest();
  }

  @Test
  void whenDefaultConstructor_thenPatientType() {
    assertNotNull(request.getUserType());
    assertEquals(UserType.PATIENT, request.getUserType());
  }

  @Test
  void whenSetUserType_thenGetReturnsCorrectValue() {
    request.setUserType(UserType.FIRST_RESPONDER);
    assertEquals(UserType.FIRST_RESPONDER, request.getUserType());
  }

  @Test
  void whenValidRequest_thenNoValidationViolations() {
    request.setEmail("test@example.com");
    request.setPassword("password123");
    request.setUserType(UserType.HEALTH_CARE_PROVIDER);

    var violations = validator.validate(request);
    assertEquals(0, violations.size());
  }

  @Test
  void whenInvalidEmail_thenValidationViolations() {
    request.setEmail("invalid-email");
    request.setPassword("password123");

    var violations = validator.validate(request);
    assertEquals(1, violations.size());
  }

  @Test
  void whenEmptyPassword_thenValidationViolations() {
    request.setEmail("test@example.com");
    request.setPassword("");

    var violations = validator.validate(request);
    assertEquals(1, violations.size());
  }
}