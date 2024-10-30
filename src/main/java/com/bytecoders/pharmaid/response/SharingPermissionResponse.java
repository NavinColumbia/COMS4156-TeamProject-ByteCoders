package com.bytecoders.pharmaid.response;

import com.bytecoders.pharmaid.repository.model.PermissionType;
import com.bytecoders.pharmaid.repository.model.SharingPermissionStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Date;
import lombok.Builder;
import lombok.Data;

/**
 * Response class for sharing permission operations.
 * Contains details about a sharing permission including the users involved and permission details.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SharingPermissionResponse {
  private String permissionId;
  private String ownerId;
  private String sharedWithUserId;
  private PermissionType permissionType;
  private SharingPermissionStatus status;
  private Date createdAt;
}
