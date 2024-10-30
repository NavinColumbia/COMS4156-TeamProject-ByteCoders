package com.bytecoders.pharmaid;

import com.bytecoders.pharmaid.exception.AuthenticationException;
import com.bytecoders.pharmaid.exception.NotAuthorizedException;
import com.bytecoders.pharmaid.exception.ResourceNotFoundException;
import com.bytecoders.pharmaid.exception.UserNotFoundException;
import com.bytecoders.pharmaid.repository.model.SharingPermission;
import com.bytecoders.pharmaid.repository.model.SharingRequest;
import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.response.ErrorResponse;
import com.bytecoders.pharmaid.response.SharingPermissionResponse;
import com.bytecoders.pharmaid.security.JwtTokenProvider;
import com.bytecoders.pharmaid.service.AuthorizationService;
import com.bytecoders.pharmaid.service.MedicationService;
import com.bytecoders.pharmaid.service.PrescriptionService;
import com.bytecoders.pharmaid.service.SharingPermissionService;
import com.bytecoders.pharmaid.service.UserService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
  @PostMapping("/users/{userId}/request")
  public ResponseEntity<?> requestAccess(
      @PathVariable String userId,
      @Valid @RequestBody SharingRequest request) {
    try {
      SharingPermission permission = sharingPermissionService.createSharingRequest(userId, request);
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(convertToResponse(permission));
    } catch (AuthenticationException e) {
      log.debug("User not Authenticated: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ErrorResponse.builder()
              .message(e.getMessage())
              .timestamp(LocalDateTime.now())
              .build());
    }catch (UserNotFoundException e) {
      log.debug("User not found: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(ErrorResponse.builder()
              .message(e.getMessage())
              .timestamp(LocalDateTime.now())
              .build());
    } catch (IllegalArgumentException e) {
      log.debug("Invalid request: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ErrorResponse.builder()
              .message(e.getMessage())
              .timestamp(LocalDateTime.now())
              .build());
    } catch (Exception e) {
      log.error("Error creating sharing request", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ErrorResponse.builder()
              .message("An error occurred while creating sharing request")
              .timestamp(LocalDateTime.now())
              .build());
    }
  }

  /**
   * Accepts a pending sharing request for user prescriptions.
   *
   * @param userId The ID of the user who owns the records.
   * @param requestId The ID of the sharing request to accept.
   * @return ResponseEntity indicating success.
   * @throws AccessDeniedException if the current user is not authorized.
   */
  @PostMapping("/users/{userId}/permissions/{requestId}/accept")
  public ResponseEntity<?> acceptRequest(
      @PathVariable String userId,
      @PathVariable String requestId) {
    try {
      SharingPermission permission = sharingPermissionService
          .acceptSharingRequest(userId, requestId);
      return ResponseEntity.ok(convertToResponse(permission));
    } catch (AuthenticationException e) {
      log.debug("User not Authenticated: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ErrorResponse.builder()
              .message(e.getMessage())
              .timestamp(LocalDateTime.now())
              .build());
    }catch (ResourceNotFoundException e) {
      log.debug("Resource not found: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(ErrorResponse.builder()
              .message(e.getMessage())
              .timestamp(LocalDateTime.now())
              .build());
    } catch (NotAuthorizedException e) {
      log.debug("Not authorized: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(ErrorResponse.builder()
              .message(e.getMessage())
              .timestamp(LocalDateTime.now())
              .build());
    } catch (Exception e) {
      log.error("Error accepting sharing request", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ErrorResponse.builder()
              .message("An error occurred while accepting sharing request")
              .timestamp(LocalDateTime.now())
              .build());
    }
  }

  /**
   * Denies a pending sharing request for user prescriptions.
   *
   * @param userId The ID of the user who owns the records.
   * @param permissionId The ID of the sharing request to deny.
   * @return ResponseEntity indicating success.
   * @throws AccessDeniedException if the current user is not authorized.
   */
  @PostMapping("/users/{userId}/permissions/{permissionId}/deny")
  public ResponseEntity<?> denyRequest(
      @PathVariable String userId,
      @PathVariable String permissionId) {
    try {
      SharingPermission permission = sharingPermissionService
          .denySharingRequest(userId, permissionId);
      return ResponseEntity.ok(convertToResponse(permission));
    } catch (AuthenticationException e) {
      log.debug("User not Authenticated: {} ", e.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ErrorResponse.builder()
              .message(e.getMessage())
              .timestamp(LocalDateTime.now())
              .build());
    }catch (ResourceNotFoundException e) {
      log.debug("Resource not found: {} ", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(ErrorResponse.builder()
              .message(e.getMessage())
              .timestamp(LocalDateTime.now())
              .build());
    } catch (NotAuthorizedException e) {
      log.debug("Not authorized: {} ", e.getMessage());
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(ErrorResponse.builder()
              .message(e.getMessage())
              .timestamp(LocalDateTime.now())
              .build());
    } catch (Exception e) {
      log.error("Error denying sharing request ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ErrorResponse.builder()
              .message("An error occurred while denying sharing request")
              .timestamp(LocalDateTime.now())
              .build());
    }
  }

  /**
   * Revokes an existing sharing permission for user prescriptions.
   *
   * @param userId The ID of the user who owns the records.
   * @param permissionId The ID of the sharing permission to revoke.
   * @return ResponseEntity indicating success.
   * @throws AccessDeniedException if the current user is not authorized.
   */
  @PostMapping("/users/{userId}/permissions/{permissionId}/revoke")
  public ResponseEntity<?> revokeAccess(
      @PathVariable String userId,
      @PathVariable String permissionId) {
    try {
      sharingPermissionService.revokeSharingPermission(userId, permissionId);
      return ResponseEntity.noContent().build();
    } catch (AuthenticationException e) {
      log.debug("User not Authenticated : {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ErrorResponse.builder()
              .message(e.getMessage())
              .timestamp(LocalDateTime.now())
              .build());
    }catch (ResourceNotFoundException e) {
      log.debug("Resource not found : {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(ErrorResponse.builder()
              .message(e.getMessage())
              .timestamp(LocalDateTime.now())
              .build());
    } catch (NotAuthorizedException e) {
      log.debug("Not authorized : {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(ErrorResponse.builder()
              .message(e.getMessage())
              .timestamp(LocalDateTime.now())
              .build());
    } catch (Exception e) {
      log.error("Error revoking sharing permission ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ErrorResponse.builder()
              .message("An error occurred while revoking sharing permission")
              .timestamp(LocalDateTime.now())
              .build());
    }
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

  /**
   * Retrieves the ID of the currently authenticated user.
   *
   * @return The ID of the current user.
   * @throws AuthenticationException if no user is authenticated.
   */
  public static String getCurrentUserId() {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication != null && authentication.isAuthenticated()) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
          return ((User) principal).getId();
        } else if (principal instanceof org.springframework.security.core.userdetails.User) {
          return ((org.springframework.security.core.userdetails.User) principal).getUsername();
        } else if (principal instanceof String) {
          return (String) principal;
        }
      }
      throw new AuthenticationException("User not authenticated");
    }


}
