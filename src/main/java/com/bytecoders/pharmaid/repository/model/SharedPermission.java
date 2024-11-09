package com.bytecoders.pharmaid.repository.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

  @Id @UuidGenerator private String id;

  @ManyToOne
  @JoinColumn(name = "owner", nullable = false)
  private User owner;

  @ManyToOne
  @JoinColumn(name = "requester", nullable = false)
  private User requester;

  @Column(name = "status", nullable = false)
  private Integer status;

  @Column(name = "permission_type", nullable = false)
  private Integer permissionType;
}
