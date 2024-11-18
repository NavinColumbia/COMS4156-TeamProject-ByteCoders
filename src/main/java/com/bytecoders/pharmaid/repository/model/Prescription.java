package com.bytecoders.pharmaid.repository.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;


/**
 * Model class to describe the "prescriptions" table.
 */
@Data
@Entity
@Table(name = "prescriptions")
public class Prescription {

  @Id
  @UuidGenerator
  @JsonProperty
  @Column(name = "prescription_id")
  private String id;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  @JsonIgnore
  private User user;

  @ManyToOne
  @JoinColumn(name = "medication_id", nullable = false)
  @JsonProperty
  private Medication medication;

  @Column(name = "dosage", nullable = false)
  @JsonProperty
  private int dosage;

  @Column(name = "num_doses")
  @JsonProperty
  private int numOfDoses;

  @Column(name = "start_date", nullable = false)
  @JsonProperty
  private Date startDate;

  @Column(name = "end_date")
  @JsonProperty
  private Date endDate;

  @Column(name = "is_active")
  @JsonProperty
  private Boolean isActive;
}