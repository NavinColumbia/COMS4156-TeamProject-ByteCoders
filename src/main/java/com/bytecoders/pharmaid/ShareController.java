package com.bytecoders.pharmaid;

import com.bytecoders.pharmaid.openapi.model.ShareRequest;
import com.bytecoders.pharmaid.openapi.model.ShareRequestStatus;
import com.bytecoders.pharmaid.repository.model.SharedPermission;
import com.bytecoders.pharmaid.service.SharedPermissionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


/**
 * This class contains all the API routes for Shared Permission.
 */
@Slf4j
@RestController
public class ShareController {

  @Autowired
  private SharedPermissionService sharedPermissionService;


  /**
   * Request access to another user's prescriptions. Permission Type in body.
   */
  @PostMapping("/users/{userId}/requests")
  public ResponseEntity<?> requestAccess(
      @PathVariable String userId, @RequestParam String requesterId,
      @Valid @RequestBody ShareRequest request) {
    try {

      SharedPermission permission =
          sharedPermissionService.createSharingRequest(requesterId, userId,
              request.getSharePermissionType());

      return new ResponseEntity<>(permission, HttpStatus.CREATED);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (ResponseStatusException e) {
      return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body("Unexpected error encountered while creating a share request");
    }
  }

  /**
   * Accept a request.
   */
  @PostMapping("/users/{userId}/requests/{shareRequestId}/accept")
  public ResponseEntity<?> acceptShareRequest(
      @PathVariable String userId, @PathVariable String shareRequestId,
      @RequestParam String requesterId) {
    try {
      SharedPermission permission =
          sharedPermissionService.shareRequestAction(userId, shareRequestId,
              ShareRequestStatus.ACCEPT, requesterId);
      return ResponseEntity.ok(permission);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (ResponseStatusException e) {
      return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body("Unexpected error encountered while accepting a share request");
    }
  }

  /**
   * Denies a Request.
   */
  @PostMapping("/users/{userId}/requests/{shareRequestId}/deny")
  public ResponseEntity<?> denyShareRequest(
      @PathVariable String userId, @PathVariable String shareRequestId,
      @RequestParam String requesterId) {
    try {
      SharedPermission permission =
          sharedPermissionService.shareRequestAction(userId, shareRequestId,
              ShareRequestStatus.DENY, requesterId);
      return ResponseEntity.ok(permission);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (ResponseStatusException e) {
      return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body("Unexpected error encountered while denying a share request");
    }
  }

  /**
   * Revoke an accepted prescription.
   */
  @PostMapping("/users/{userId}/requests/{shareRequestId}/revoke")
  public ResponseEntity<?> revokeShareAccess(
      @PathVariable String userId, @PathVariable String shareRequestId,
      @RequestParam String requesterId) {
    try {
      sharedPermissionService.revokeSharingPermission(userId, shareRequestId, requesterId);
      return ResponseEntity.ok("Access revoked successfully");
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (ResponseStatusException e) {
      return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body("Unexpected error encountered while revoking a share permission");
    }
  }
}
