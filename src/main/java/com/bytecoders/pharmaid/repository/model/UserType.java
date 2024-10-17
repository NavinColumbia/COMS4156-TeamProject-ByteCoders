package com.bytecoders.pharmaid.repository.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "user_types")
public class UserType {
  @Id
  @UuidGenerator
  private String id;

  @Column(name = "name", nullable = false, unique = true)
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "can_access_all_records")
  private boolean canAccessAllRecords;

  // Getters and setters
  public String getId() { return id; }
  public void setId(String id) { this.id = id; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  public boolean isCanAccessAllRecords() { return canAccessAllRecords; }
  public void setCanAccessAllRecords(boolean canAccessAllRecords) { this.canAccessAllRecords = canAccessAllRecords; }
}
