package com.bytecoders.pharmaid.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Object to hold register user request body.
 */
public class RegisterUserRequest {
  @NotBlank(message = "The email address is required")
  @Email(message = "The email address is invalid")
  private String email;

  @NotBlank(message = "The password is required")
  @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
  private String password;

  // Constructor
  public RegisterUserRequest() {}

  // Constructor with parameters
  public RegisterUserRequest(String email, String password) {
    this.email = email;
    this.password = password;
  }

  // Getter for email
  public String getEmail() {
    return this.email;
  }

  // Setter for email
  public void setEmail(String email) {
    this.email = email;
  }

  // Getter for password
  public String getPassword() {
    return this.password;
  }

  // Setter for password
  public void setPassword(String password) {
    this.password = password;
  }

  // toString method for logging (be careful not to expose the password)
  @Override
  public String toString() {
    return "RegisterUserRequest{" +
        "email='" + email + '\'' +
        ", password='[PROTECTED]'" +
        '}';
  }
}