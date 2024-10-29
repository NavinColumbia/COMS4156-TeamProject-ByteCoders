package com.bytecoders.pharmaid.repository.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Request object for creating sharing permissions between users. */
@Data
public class SharingRequest {

  @NotNull(message = "Permission type is required")
  private PermissionType permissionType;

  /**
   * Gets the permission type.
   *
   * @return the permission type
   */
  public PermissionType getPermissionType() {
    return permissionType;
  }

  /**
   * Sets the permission type.
   *
   * @param permissionType the permission type to set
   */
  public void setPermissionType(PermissionType permissionType) {
    this.permissionType = permissionType;
  }
}
