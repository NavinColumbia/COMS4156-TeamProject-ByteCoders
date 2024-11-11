package com.bytecoders.pharmaid.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Object to hold permissiontype.
 */
@Data
public class ShareRequest {
  @NotNull private Integer permissionType; // 0=view, 1=edit
}
