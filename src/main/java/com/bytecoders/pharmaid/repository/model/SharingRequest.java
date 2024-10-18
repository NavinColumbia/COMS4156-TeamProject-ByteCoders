package com.bytecoders.pharmaid.repository.model;

import jakarta.validation.constraints.NotNull;

public class SharingRequest {

  @NotNull
  private PermissionType permissionType;

  
  /** 
   * @return PermissionType
   */
  public @NotNull PermissionType getPermissionType() {
    return permissionType;
  }

  public void setPermissionType(
      @NotNull PermissionType permissionType) {
    this.permissionType = permissionType;
  }

}

