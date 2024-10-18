package com.bytecoders.pharmaid.repository.model;

import jakarta.validation.constraints.NotNull;

public class SharingRequest {

  @NotNull
  private PermissionType permissionType;

  public @NotNull PermissionType getPermissionType() {
    return permissionType;
  }

  public void setPermissionType(
      @NotNull PermissionType permissionType) {
    this.permissionType = permissionType;
  }
// Getters and setters
}

