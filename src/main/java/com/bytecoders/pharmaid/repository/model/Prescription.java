package com.bytecoders.pharmaid.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;
import org.hibernate.annotations.UuidGenerator;


/**
 * Model class to describe the "prescriptions" table.
 */
@Entity
@Table(name = "prescriptions")
public class Prescription {

  @Id
  @UuidGenerator
  @JsonProperty
  private String prescriptionId;

  @Column(name = "user_id", nullable = false)
  @JsonProperty
  private String userId;

  @Column(name = "medication_id", nullable = false)
  @JsonProperty
  private String medicationId;

  @Column(name = "dosage", nullable = false)
  @JsonProperty
  private int dosage;

  @Column(name = "start_date", nullable = false)
  @JsonProperty
  private Date startDate;

  @Column(name = "end_date")
  @JsonProperty
  private Date endDate;

  // Getters and Setters
  public String getPrescriptionId() {
    return prescriptionId;
  }

  public void setPrescriptionId(String prescriptionId) {
    this.prescriptionId = prescriptionId;
  }


  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getMedicationId() {
    return medicationId;
  }

  public void setMedicationId(String medicationId) {
    this.medicationId = medicationId;
  }

  public int getDosage() {
    return dosage;
  }

  public void setDosage(int dosage) {
    this.dosage = dosage;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }
}