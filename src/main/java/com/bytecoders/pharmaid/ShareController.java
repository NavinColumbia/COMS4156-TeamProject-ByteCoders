package com.bytecoders.pharmaid;

import com.bytecoders.pharmaid.openapi.model.ShareRequest;
import com.bytecoders.pharmaid.repository.model.SharedPermission;
import com.bytecoders.pharmaid.service.SharedPermissionService;
import com.bytecoders.pharmaid.util.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** This class contains all the API routes for Shared Permission. */
@RestController
@RequestMapping("/share")
public class ShareController {

  private static final int ACCEPT = 1;
  private static final int DENY = 2;

  @Autowired private SharedPermissionService sharedPermissionService;


  /** Request access to view/edit another user's prescriptions.Permission Type in body. */
  @PostMapping("/request/{ownerId}")
  public ResponseEntity<?> requestAccess(
      @PathVariable String ownerId, @Valid @RequestBody ShareRequest request) {
    try {
      String requesterId = JwtUtils.getLoggedInUserId();

      SharedPermission permission =
          sharedPermissionService.createSharingRequest(
              requesterId, ownerId, request.getPermissionType());
      return new ResponseEntity<>(permission, HttpStatus.CREATED);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (ResponseStatusException e) {
      return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body("Internal Server  Error");
    }
  }

  /** Accept a request. */
  @PostMapping("/{requestId}/accept")
  public ResponseEntity<?> acceptRequest(@PathVariable String requestId) {
    try {
      String userId = JwtUtils.getLoggedInUserId();

      SharedPermission permission =
          sharedPermissionService.acceptDenySharingRequest(userId, requestId, ACCEPT);
      return ResponseEntity.ok(permission);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (ResponseStatusException e) {
      return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body("Internal  Server  Error");
    }
  }

  /** Denies a Request. */
  @PostMapping("/{requestId}/deny")
  public ResponseEntity<?> denyRequest(@PathVariable String requestId) {
    try {
      String userId = JwtUtils.getLoggedInUserId();

      SharedPermission permission =
          sharedPermissionService.acceptDenySharingRequest(userId, requestId, DENY);
      return ResponseEntity.ok(permission);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (ResponseStatusException e) {
      return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body("Internal Server Error");
    }
  }

  /** Revoke an accepted prescription. */
  @PostMapping("/{requestId}/revoke")
  public ResponseEntity<?> revokeAccess(@PathVariable String requestId) {
    try {
      String userId = JwtUtils.getLoggedInUserId();

      sharedPermissionService.revokeSharingPermission(userId, requestId);
      return ResponseEntity.ok("Access revoked successfully");
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (ResponseStatusException e) {
      return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body("Internal Server Error");
    }
  }
}
