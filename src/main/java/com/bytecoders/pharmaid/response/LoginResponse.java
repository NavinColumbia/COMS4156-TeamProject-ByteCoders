package com.bytecoders.pharmaid.response;

import com.bytecoders.pharmaid.repository.model.UserType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

/**
 * Response class for login operations.
 * Contains authentication details and user information after successful login.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {
  private String token;
  private String userId;
  private String email;
  private UserType userType;
}