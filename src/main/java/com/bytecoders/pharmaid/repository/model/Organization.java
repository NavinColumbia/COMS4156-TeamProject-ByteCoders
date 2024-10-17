package com.bytecoders.pharmaid.repository.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "organizations")
public class Organization {
  @Id
  @UuidGenerator
  private String id;

  @Column(name = "name", nullable = false)
  private String name;

  @ManyToOne
  @JoinColumn(name = "org_type_id", nullable = false)
  private OrganizationType orgType;

  // Getters and setters
  public String getId() { return id; }
  public void setId(String id) { this.id = id; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public OrganizationType getOrgType() { return orgType; }
  public void setOrgType(OrganizationType orgType) { this.orgType = orgType; }
}
