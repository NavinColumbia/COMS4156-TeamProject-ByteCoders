package com.bytecoders.pharmaid.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

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
}