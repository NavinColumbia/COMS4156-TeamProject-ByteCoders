package com.bytecoders.pharmaid.repository.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "sharing_permissions")
public class SharingPermission {
  @Id
  @UuidGenerator
  private String id;

  @ManyToOne
  @JoinColumn(name = "owner_id", nullable = false)
  private User owner;

  @ManyToOne
  @JoinColumn(name = "shared_with_user_id")
  private User sharedWithUser;

  @ManyToOne
  @JoinColumn(name = "shared_with_org_id")
  private Organization sharedWithOrganization;

  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  private SharingPermissionStatus status;

  @Column(name = "permission_type", nullable = false)
  @Enumerated(EnumType.STRING)
  private PermissionType permissionType;

  @Column(name = "created_at", nullable = false)
  private Date createdAt;

  @Column(name = "expires_at")
  private Date expiresAt;

  // Getters and setters
  public String getId() { return id; }
  public void setId(String id) { this.id = id; }

  public User getOwner() { return owner; }
  public void setOwner(User owner) { this.owner = owner; }

  public User getSharedWithUser() { return sharedWithUser; }
  public void setSharedWithUser(User sharedWithUser) { this.sharedWithUser = sharedWithUser; }

  public Organization getSharedWithOrganization() { return sharedWithOrganization; }
  public void setSharedWithOrganization(Organization sharedWithOrganization) { this.sharedWithOrganization = sharedWithOrganization; }

  public PermissionType getPermissionType() { return permissionType; }
  public void setPermissionType(PermissionType permissionType) { this.permissionType = permissionType; }

  public Date getCreatedAt() { return createdAt; }
  public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

  public Date getExpiresAt() { return expiresAt; }
  public void setExpiresAt(Date expiresAt) { this.expiresAt = expiresAt; }


  public SharingPermissionStatus getStatus() {
    return status;
  }

  public void setStatus(SharingPermissionStatus status) {
    this.status = status;
  }
}