package com.bytecoders.pharmaid.repository.model;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.Date;

@Entity
@Table(name = "prescriptions")
public class Prescription {
  @Id
  @UuidGenerator
  private String id;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "medication_name", nullable = false)
  private String medicationName;

  @Column(name = "dosage", nullable = false)
  private String dosage;

  @Column(name = "frequency", nullable = false)
  private String frequency;

  @Column(name = "start_date", nullable = false)
  private Date startDate;

  @Column(name = "end_date")
  private Date endDate;

  // Getters and setters
  public String getId() { return id; }
  public void setId(String id) { this.id = id; }

  public User getUser() { return user; }
  public void setUser(User user) { this.user = user; }

  public String getMedicationName() { return medicationName; }
  public void setMedicationName(String medicationName) { this.medicationName = medicationName; }

  public String getDosage() { return dosage; }
  public void setDosage(String dosage) { this.dosage = dosage; }

  public String getFrequency() { return frequency; }
  public void setFrequency(String frequency) { this.frequency = frequency; }

  public Date getStartDate() { return startDate; }
  public void setStartDate(Date startDate) { this.startDate = startDate; }

  public Date getEndDate() { return endDate; }
  public void setEndDate(Date endDate) { this.endDate = endDate; }

  // Convenience method to get user ID
  public String getUserId() {
    return (user != null) ? user.getId() : null;
  }
}