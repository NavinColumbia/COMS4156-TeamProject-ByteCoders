package com.bytecoders.pharmaid.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

/**
 * Model class to describe the "medications" table.
 */
@Entity
@Table(name = "medications")
public class Medication {

  @Id
  @UuidGenerator
  @JsonProperty
  @Column(name = "medication_id", nullable = false, unique = true)
  private String id;

  @Column(name = "medication_name", nullable = false, unique = true)
  @JsonProperty
  private String name;

}