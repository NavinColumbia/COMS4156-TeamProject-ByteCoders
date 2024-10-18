package com.bytecoders.pharmaid.dto;

/**
 *
 */
public class AuthResponse {

  private String accessToken;
  private String refreshToken;
  private String tokenType = "Bearer";

  /**
   *
   * @param accessToken
   */
  public AuthResponse(String accessToken) {
    this.accessToken = accessToken;
  }

  /**
   *
   * @param accessToken
   * @param refreshToken
   */
  public AuthResponse(String accessToken, String refreshToken) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
  }

  /**
   *
   * @return
   */
  public String getAccessToken() {
    return accessToken;
  }

  /**
   *
   * @param accessToken
   */
  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  /**
   *
   * @return
   */
  public String getRefreshToken() {
    return refreshToken;
  }

  /**
   *
   * @param refreshToken
   */
  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  /**
   *
   * @return
   */
  public String getTokenType() {
    return tokenType;
  }

  /**
   *
   * @param tokenType
   */
  public void setTokenType(String tokenType) {
    this.tokenType = tokenType;
  }
}
