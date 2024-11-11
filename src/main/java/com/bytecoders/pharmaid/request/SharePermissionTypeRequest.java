package com.bytecoders.pharmaid.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SharePermissionTypeRequest {
  @NotNull private Integer permissionType; // 0=view, 1=edit
}
