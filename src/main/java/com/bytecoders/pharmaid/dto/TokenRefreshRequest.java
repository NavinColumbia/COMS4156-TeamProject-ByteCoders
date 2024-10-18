package com.bytecoders.pharmaid.dto;

import jakarta.validation.constraints.NotBlank;

/**
 *
 */
public class TokenRefreshRequest {

  @NotBlank
  private String refreshToken;

  
  /** 
   * @return String
   */
  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }
}
