package com.bytecoders.pharmaid.repository.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

/**
 * Represents the join relationship between users and their permission types.
 * This entity manages sharing permissions between users in the system.
 */
@Data
@Entity
@Table(name = "sharing_permissions")
public class SharingPermission {

  @Id @UuidGenerator private String id;

  @ManyToOne
  @JoinColumn(name = "owner_id", nullable = false)
  private User owner;

  @ManyToOne
  @JoinColumn(name = "shared_with_user_id")
  private User sharedWithUser;

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
}
