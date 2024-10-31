package com.bytecoders.pharmaid.response;

import com.bytecoders.pharmaid.repository.model.UserType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response class for registration operations. Contains user details after successful registration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegistrationResponse {
  private String userId;
  private String email;
  private UserType userType;
  private String message;
}
