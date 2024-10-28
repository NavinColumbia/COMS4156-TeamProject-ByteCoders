package com.bytecoders.pharmaid.dto;

import jakarta.validation.constraints.NotNull;

/**
 *
 */
public class LoginUserRequest {
  @NotNull
  private String email;

  @NotNull
  private String password;

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
}