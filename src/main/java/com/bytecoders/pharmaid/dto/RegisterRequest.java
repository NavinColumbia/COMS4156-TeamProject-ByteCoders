package com.bytecoders.pharmaid.dto;

import com.bytecoders.pharmaid.repository.model.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 *
 */
public class RegisterUserRequest {
  @NotNull
  private String email;

  @NotNull
  private String password;

  @NotNull
  private UserType userType = UserType.PATIENT;

  // getters and setters
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public UserType getUserType() {
    return userType;
  }

  public void setUserType(UserType userType) {
    this.userType = userType;
  }
}