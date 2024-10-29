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

  /**
   * Gets the email address.
   *
   * @return the email address
   */
  public String getEmail() {
    return email;
  }

  /**
   * Sets the email address.
   *
   * @param email the email address to set
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * Gets the password.
   *
   * @return the password
   */
  public String getPassword() {
    return password;
  }

  /**
   * Sets the password.
   *
   * @param password the password to set
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Gets the user type.
   *
   * @return the user type
   */
  public UserType getUserType() {
    return userType;
  }

  /**
   * Sets the user type.
   *
   * @param userType the user type to set
   */
  public void setUserType(UserType userType) {
    this.userType = userType;
  }
}
