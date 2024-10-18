package com.bytecoders.pharmaid.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 *
 */
public class RegisterRequest {

  @NotBlank
  @Email
  private String email;

  @NotBlank
  private String password;

  
  /** 
   * @return String
   */
  // Getters and Setters
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