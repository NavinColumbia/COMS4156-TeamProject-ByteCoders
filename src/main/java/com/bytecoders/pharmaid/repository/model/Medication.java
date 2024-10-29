package com.bytecoders.pharmaid.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

/** Model class to describe the "medications" table. */
@Data
@Entity
@Table(name = "medications")
public class Medication {
  @Id @UuidGenerator @JsonProperty private String medicationId;

  @Column(name = "medication_name", nullable = false)
  @JsonProperty
  private String medicationName;
}
