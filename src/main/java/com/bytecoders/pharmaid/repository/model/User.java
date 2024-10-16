package com.bytecoders.pharmaid.repository.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.lang.NonNull;

/**
 * Model class to describe the "users" table.
 */
@Entity
@Table(name = "users")
public class User {
  @Id
  @UuidGenerator
  @JsonProperty
  private String id;

  @Column(name = "email", nullable = false, unique = true)
  @JsonProperty
  private String email;

  @Column(name = "hashed_password", nullable = false)
  @JsonIgnore
  private String hashedPassword;

  public void setEmail(@NonNull String email) {
    this.email = email;
  }

  public void setHashedPassword(@NonNull String hashedPassword) {
    this.hashedPassword = hashedPassword;
  }

  public String getHashedPassword() {
    return this.hashedPassword;
  }
}