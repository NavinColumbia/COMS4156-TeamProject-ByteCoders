package com.bytecoders.pharmaid.repository.model;

import jakarta.validation.constraints.NotNull;

public class OrganizationPermissionRequest {

  @NotNull
  private String organizationId;

  @NotNull
  private PermissionType permissionType; // Enum with values like VIEW, EDIT

  // Getters and Setters

  public @NotNull PermissionType getPermissionType() {
    return permissionType;
  }

  public void setPermissionType(
      @NotNull PermissionType permissionType) {
    this.permissionType = permissionType;
  }

  public @NotNull String getOrganizationId() {
    return organizationId;
  }

  public void setOrganizationId(@NotNull String organizationId) {
    this.organizationId = organizationId;
  }
}
