package com.bytecoders.pharmaid;

import com.bytecoders.pharmaid.repository.model.OrganizationPermissionRequest;
import com.bytecoders.pharmaid.repository.model.SharingRequest;
import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.repository.model.Prescription;
import com.bytecoders.pharmaid.security.CustomUserDetails;
import com.bytecoders.pharmaid.service.UserService;
import com.bytecoders.pharmaid.service.PrescriptionService;
import com.bytecoders.pharmaid.service.SharingPermissionService;
import com.bytecoders.pharmaid.service.AuthorizationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

  @Autowired
  private UserService userService;

  @Autowired
  private PrescriptionService prescriptionService;

  @Autowired
  private SharingPermissionService sharingPermissionService;

  @Autowired
  private AuthorizationService authorizationService;

  @GetMapping("/{user_id}")
  public ResponseEntity<User> getUserAccount(@PathVariable("user_id") String userId) {
    String currentUserId = getCurrentUserId();

    // Only allow access if the current user is the requested user
    if (!currentUserId.equals(userId)) {
      throw new AccessDeniedException("You are not authorized to access this account.");
    }

    return userService.getUserById(userId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PatchMapping("/{user_id}")
  public ResponseEntity<User> updateUserAccount(@PathVariable("user_id") String userId, @RequestBody User user) {
    String currentUserId = getCurrentUserId();

    if (!currentUserId.equals(userId)) {
      throw new AccessDeniedException("You are not authorized to update this account.");
    }

    User updatedUser = userService.updateUser(userId, user);
    return ResponseEntity.ok(updatedUser);
  }

  @DeleteMapping("/{user_id}")
  public ResponseEntity<?> deleteUserAccount(@PathVariable("user_id") String userId) {
    String currentUserId = getCurrentUserId();

    if (!currentUserId.equals(userId)) {
      throw new AccessDeniedException("You are not authorized to delete this account.");
    }

    userService.deleteUser(userId);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/{user_id}/records")
  public ResponseEntity<List<Prescription>> getUserHealthRecords(@PathVariable("user_id") String userId) {
    String currentUserId = getCurrentUserId();

    if (!authorizationService.canAccessUserRecords(currentUserId, userId)) {
      throw new AccessDeniedException("You are not authorized to access these records.");
    }

    List<Prescription> records = prescriptionService.getPrescriptionsByUserId(userId);
    return ResponseEntity.ok(records);
  }

  @PostMapping("/{user_id}/records/prescriptions")
  public ResponseEntity<Prescription> addUserPrescription(
      @PathVariable("user_id") String userId,
      @Valid @RequestBody Prescription prescription) {
    String currentUserId = getCurrentUserId();

    if (!authorizationService.canModifyUserRecords(currentUserId, userId)) {
      throw new AccessDeniedException("You are not authorized to add prescriptions for this user.");
    }

    Prescription newPrescription = prescriptionService.createPrescription(userId, prescription);
    return ResponseEntity.ok(newPrescription);
  }

  @PatchMapping("/{user_id}/records/prescriptions/{prescription_id}")
  public ResponseEntity<Prescription> updateUserPrescription(
      @PathVariable("user_id") String userId,
      @PathVariable("prescription_id") String prescriptionId,
      @Valid @RequestBody Prescription prescription) {
    Prescription existingPrescription = prescriptionService.getPrescriptionById(prescriptionId);

    if (!authorizationService.canModifyPrescription(existingPrescription)) {
      throw new AccessDeniedException("You are not authorized to modify this prescription.");
    }

    Prescription updatedPrescription = prescriptionService.updatePrescription(userId, prescriptionId, prescription);
    return ResponseEntity.ok(updatedPrescription);
  }

  @DeleteMapping("/{user_id}/records/prescriptions/{prescription_id}")
  public ResponseEntity<?> removeUserPrescription(
      @PathVariable("user_id") String userId,
      @PathVariable("prescription_id") String prescriptionId) {
    Prescription prescription = prescriptionService.getPrescriptionById(prescriptionId);

    if (!authorizationService.canModifyPrescription(prescription)) {
      throw new AccessDeniedException("You are not authorized to delete this prescription.");
    }

    prescriptionService.deletePrescription(userId, prescriptionId);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{user_id}/records/request")
  public ResponseEntity<String> requestUserPrescriptions(
      @PathVariable("user_id") String userId,
      @Valid @RequestBody SharingRequest request) {
    String requestId = sharingPermissionService.createSharingRequest(userId, request);
    return ResponseEntity.ok(requestId);
  }

  @PostMapping("/{user_id}/records/{request_id}/accept")
  public ResponseEntity<?> acceptUserPrescriptionsRequest(
      @PathVariable("user_id") String userId,
      @PathVariable("request_id") String requestId) {
    String currentUserId = getCurrentUserId();

    if (!currentUserId.equals(userId)) {
      throw new AccessDeniedException("You are not authorized to accept this request.");
    }

    sharingPermissionService.acceptSharingRequest(userId, requestId);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{user_id}/records/{request_id}/deny")
  public ResponseEntity<?> denyUserPrescriptionsRequest(
      @PathVariable("user_id") String userId,
      @PathVariable("request_id") String requestId) {
    String currentUserId = getCurrentUserId();

    if (!currentUserId.equals(userId)) {
      throw new AccessDeniedException("You are not authorized to deny this request.");
    }

    sharingPermissionService.denySharingRequest(userId, requestId);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{user_id}/records/{request_id}/revoke")
  public ResponseEntity<?> revokeUserPrescriptionAccess(
      @PathVariable("user_id") String userId,
      @PathVariable("request_id") String requestId) {
    String currentUserId = getCurrentUserId();

    if (!currentUserId.equals(userId)) {
      throw new AccessDeniedException("You are not authorized to revoke this access.");
    }

    sharingPermissionService.revokeSharingPermission(userId, requestId);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{user_id}/permissions/organizations")
  public ResponseEntity<?> grantPermissionToOrganization(
      @PathVariable("user_id") String userId,
      @Valid @RequestBody OrganizationPermissionRequest request) {
    String currentUserId = getCurrentUserId();

    if (!currentUserId.equals(userId)) {
      throw new AccessDeniedException("You are not authorized to grant permissions for this user.");
    }

    sharingPermissionService.grantPermissionToOrganization(userId, request);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{user_id}/permissions/organizations")
  public ResponseEntity<?> revokePermissionFromOrganization(
      @PathVariable("user_id") String userId,
      @Valid @RequestBody OrganizationPermissionRequest request) {
    String currentUserId = getCurrentUserId();

    if (!currentUserId.equals(userId)) {
      throw new AccessDeniedException("You are not authorized to revoke permissions for this user.");
    }

    sharingPermissionService.revokePermissionFromOrganization(userId, request);
    return ResponseEntity.ok().build();
  }

  private String getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      return userDetails.getUser().getId(); // Return the user ID
    }
    return null; // Or throw an exception if appropriate
  }

}
