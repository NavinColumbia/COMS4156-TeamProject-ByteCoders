package com.bytecoders.pharmaid.repository.model;

import com.bytecoders.pharmaid.openapi.model.UserType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

/**
 * Model class to describe the "users" table.
 */
@Data
@Entity
@Table(name = "users")
public class User {

  @Id
  @UuidGenerator
  @JsonProperty
  @Column(name = "user_id")
  private String id;

  @Column(name = "email", nullable = false, unique = true)
  @JsonProperty
  private String email;

  @Column(name = "hashed_password", nullable = false)
  @JsonIgnore
  private String hashedPassword;

  @Enumerated(EnumType.STRING)
  @Column(name = "user_type", nullable = false)
  private UserType userType;
}