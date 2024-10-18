package com.bytecoders.pharmaid.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * Object to hold user login request body.
 */
@Data
public class LoginUserRequest {
  @NotEmpty(message = "The email address is required")
  @Email(message = "The email address is invalid")
  private String email;

  @NotEmpty(message = "The password is required")
  private String password;
}