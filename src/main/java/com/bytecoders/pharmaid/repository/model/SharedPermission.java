package com.bytecoders.pharmaid.repository.model;

import com.bytecoders.pharmaid.openapi.model.SharePermissionType;
import com.bytecoders.pharmaid.openapi.model.ShareRequestStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

/** Shared permissions Table. */
@Data
@Entity
@Table(name = "shared_permissions")
public class SharedPermission {

  @Id
  @UuidGenerator
  @Column(name = "share_request_id")
  private String id;

  @ManyToOne
  @JoinColumn(name = "owner_id", nullable = false)
  private User owner;

  @ManyToOne
  @JoinColumn(name = "requester_id", nullable = false)
  private User requester;

  @Enumerated(EnumType.STRING)
  @Column(name = "share_request_status", nullable = false)
  private ShareRequestStatus status;

  @Enumerated(EnumType.STRING)
  @Column(name = "share_permission_type", nullable = false)
  private SharePermissionType sharePermissionType;
}
