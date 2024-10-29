package com.bytecoders.pharmaid;

import com.bytecoders.pharmaid.repository.model.SharingRequest;
import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.security.JwtTokenProvider;
import com.bytecoders.pharmaid.service.AuthorizationService;
import com.bytecoders.pharmaid.service.MedicationService;
import com.bytecoders.pharmaid.service.PrescriptionService;
import com.bytecoders.pharmaid.service.SharingPermissionService;
import com.bytecoders.pharmaid.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contains all the API routes for managing sharing permissions in the system.
 * Handles requests for sharing, accepting, denying, and revoking access to user records.
 */
@Slf4j
@RestController
public class SharingController {

  @Autowired
  private UserService userService;

  @Autowired
  private MedicationService medicationService;

  @Autowired
  private PrescriptionService prescriptionService;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private JwtTokenProvider tokenProvider;

  @Autowired
  private AuthorizationService authorizationService;

  @Autowired
  private SharingPermissionService sharingPermissionService;

  /**
   * Creates a new sharing request for user prescriptions.
   *
   * @param userId The ID of the user whose records are being requested.
   * @param request The sharing request details including permission type.
   * @return ResponseEntity containing the created request ID.
   */
  @PostMapping("/{user_Id}/records/request")
  public ResponseEntity<String> requestUserPrescriptions(
      @PathVariable("user_Id") String userId, @Valid @RequestBody SharingRequest request) {
    String requestId = sharingPermissionService.createSharingRequest(userId, request);
    return ResponseEntity.ok(requestId);
  }

  /**
   * Accepts a pending sharing request for user prescriptions.
   *
   * @param userId The ID of the user who owns the records.
   * @param requestId The ID of the sharing request to accept.
   * @return ResponseEntity indicating success.
   * @throws AccessDeniedException if the current user is not authorized.
   */
  @PostMapping("/{userid}/records/{requestid}/accept")
  public ResponseEntity<?> acceptUserPrescriptionsRequest(
      @PathVariable("userid") String userId, @PathVariable("requestid") String requestId) {
    String currentUserId = getCurrentUserId();

    if (!currentUserId.equals(userId)) {
      throw new AccessDeniedException("You are not authorized to accept this request.");
    }

    sharingPermissionService.acceptSharingRequest(userId, requestId);
    return ResponseEntity.ok().build();
  }

  /**
   * Denies a pending sharing request for user prescriptions.
   *
   * @param userId The ID of the user who owns the records.
   * @param requestId The ID of the sharing request to deny.
   * @return ResponseEntity indicating success.
   * @throws AccessDeniedException if the current user is not authorized.
   */
  @PostMapping("/{user_Id}/records/{request_id}/deny")
  public ResponseEntity<?> denyUserPrescriptionsRequest(
      @PathVariable("user_Id") String userId, @PathVariable("request_id") String requestId) {
    String currentUserId = getCurrentUserId();

    if (!currentUserId.equals(userId)) {
      throw new AccessDeniedException("You are not authorized to deny this request.");
    }

    sharingPermissionService.denySharingRequest(userId, requestId);
    return ResponseEntity.ok().build();
  }

  /**
   * Revokes an existing sharing permission for user prescriptions.
   *
   * @param userId The ID of the user who owns the records.
   * @param requestId The ID of the sharing permission to revoke.
   * @return ResponseEntity indicating success.
   * @throws AccessDeniedException if the current user is not authorized.
   */
  @PostMapping("/{user_Id}/records/{request_Id}/revoke")
  public ResponseEntity<?> revokeUserPrescriptionAccess(
      @PathVariable("user_Id") String userId, @PathVariable("request_Id") String requestId) {
    String currentUserId = getCurrentUserId();

    if (!currentUserId.equals(userId)) {
      throw new AccessDeniedException("You are not authorized to revoke this access.");
    }

    sharingPermissionService.revokeSharingPermission(userId, requestId);
    return ResponseEntity.ok().build();
  }

  /**
   * Retrieves the ID of the currently authenticated user.
   *
   * @return The ID of the current user.
   * @throws RuntimeException if no user is authenticated.
   */
  private String getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    log.debug(
        "Authentication principal type: {}",
        authentication != null ? authentication.getPrincipal().getClass() : "null");

    if (authentication != null && authentication.getPrincipal() instanceof User) {
      User user = (User) authentication.getPrincipal();
      return user.getId();
    }
    throw new RuntimeException("User not authenticated");
  }
}
