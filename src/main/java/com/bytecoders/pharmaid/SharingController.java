package com.bytecoders.pharmaid;

import com.bytecoders.pharmaid.repository.model.SharingPermission;
import com.bytecoders.pharmaid.repository.model.SharingRequest;
import com.bytecoders.pharmaid.response.SharingPermissionResponse;
import com.bytecoders.pharmaid.service.SharingPermissionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing sharing permissions in the PharmaID system. Provides endpoints for
 * creating, accepting, denying, and revoking access requests to user records.
 */
@RestController
public class SharingController {

  /** Service for managing sharing permissions. */
  @Autowired private SharingPermissionService sharingPermissionService;

  /**
   * Creates a new sharing request for user prescriptions.
   *
   * @param userId the ID of the user whose records are being requested
   * @param request the sharing request details, including permission type
   * @return ResponseEntity containing the created request ID
   */
  @PostMapping("/users/{userId}/request")
  public ResponseEntity<SharingPermissionResponse> requestAccess(
      @PathVariable String userId, @Valid @RequestBody SharingRequest request) {
    SharingPermission permission = sharingPermissionService.createSharingRequest(userId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponse(permission));
  }

  /**
   * Accepts a pending sharing request for user prescriptions.
   *
   * @param userId the ID of the user who owns the records
   * @param requestId the ID of the sharing request to accept
   * @return ResponseEntity indicating success
   */
  @PostMapping("/users/{userId}/permissions/{requestId}/accept")
  public ResponseEntity<SharingPermissionResponse> acceptRequest(
      @PathVariable String userId, @PathVariable String requestId) {
    SharingPermission permission = sharingPermissionService.acceptSharingRequest(userId, requestId);
    return ResponseEntity.ok(convertToResponse(permission));
  }

  /**
   * Denies a pending sharing request for user prescriptions.
   *
   * @param userId the ID of the user who owns the records
   * @param permissionId the ID of the sharing request to deny
   * @return ResponseEntity indicating success
   */
  @PostMapping("/users/{userId}/permissions/{permissionId}/deny")
  public ResponseEntity<SharingPermissionResponse> denyRequest(
      @PathVariable String userId, @PathVariable String permissionId) {
    SharingPermission permission =
        sharingPermissionService.denySharingRequest(userId, permissionId);
    return ResponseEntity.ok(convertToResponse(permission));
  }

  /**
   * Revokes an existing sharing permission for user prescriptions.
   *
   * @param userId the ID of the user who owns the records
   * @param permissionId the ID of the sharing permission to revoke
   * @return ResponseEntity indicating success
   */
  @PostMapping("/users/{userId}/permissions/{permissionId}/revoke")
  public ResponseEntity<Void> revokeAccess(
      @PathVariable String userId, @PathVariable String permissionId) {
    sharingPermissionService.revokeSharingPermission(userId, permissionId);
    return ResponseEntity.noContent().build();
  }

  private SharingPermissionResponse convertToResponse(SharingPermission permission) {
    return SharingPermissionResponse.builder()
        .permissionId(permission.getId())
        .ownerId(permission.getOwner().getId())
        .sharedWithUserId(permission.getSharedWithUser().getId())
        .permissionType(permission.getPermissionType())
        .status(permission.getStatus())
        .createdAt(permission.getCreatedAt())
        .build();
  }
}
