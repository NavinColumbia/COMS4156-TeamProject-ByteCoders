package com.bytecoders.pharmaid.repository.model;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 *
 */
@Entity
@Table(name = "users")
public class User {


  @Id
  @UuidGenerator
  private String id;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "hashed_password", nullable = false)
  private String hashedPassword;

  
  /** 
   * @return String
   */
  // Getters and setters
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getHashedPassword() {
    return hashedPassword;
  }

  public void setHashedPassword(String hashedPassword) {
    this.hashedPassword = hashedPassword;
  }


}