package com.bytecoders.pharmaid.request;

import com.bytecoders.pharmaid.repository.model.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Request object for user registration. */
@Data
public class RegisterUserRequest {

  @NotEmpty(message = "The email address is required")
  @Email(message = "The email address is invalid")
  private String email;

  @NotEmpty(message = "The password is required")
  private String password;

  @NotNull(message = "User type is required")
  private UserType userType = UserType.PATIENT; // Default to PATIENT if not specified

}
