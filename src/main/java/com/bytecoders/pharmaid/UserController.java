package com.bytecoders.pharmaid;

import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.repository.model.Prescription;
import com.bytecoders.pharmaid.service.UserService;
import com.bytecoders.pharmaid.service.PrescriptionService;
import com.bytecoders.pharmaid.service.SharingPermissionService;
import com.bytecoders.pharmaid.service.TypeManagementService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
  private TypeManagementService typeManagementService;

  @GetMapping("/{user_id}")
  public ResponseEntity<User> getUserAccount(@PathVariable("user_id") String userId) {
    return userService.getUserById(userId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PatchMapping("/{user_id}")
  public ResponseEntity<User> updateUserAccount(@PathVariable("user_id") String userId, @RequestBody User user) {
    User updatedUser = userService.updateUser(userId, user);
    return ResponseEntity.ok(updatedUser);
  }

  @DeleteMapping("/{user_id}")
  public ResponseEntity<?> deleteUserAccount(@PathVariable("user_id") String userId) {
    userService.deleteUser(userId);
    return ResponseEntity.ok().build();
  }
  @GetMapping("/{user_id}/records")
  public ResponseEntity<List<Prescription>> getUserHealthRecords(
      @PathVariable("user_id") String userId) {
    List<Prescription> records = prescriptionService.getPrescriptionsByUserId(userId);
    return ResponseEntity.ok(records);
  }

  @PostMapping("/{user_id}/records/prescriptions")
  public ResponseEntity<Prescription> addUserPrescription(
      @PathVariable("user_id") String userId,
      @Valid @RequestBody Prescription prescription) {
    Prescription newPrescription = prescriptionService.createPrescription(userId, prescription);
    return ResponseEntity.ok(newPrescription);
  }

  @PatchMapping("/{user_id}/records/prescriptions/{prescription_id}")
  public ResponseEntity<Prescription> updateUserPrescription(
      @PathVariable("user_id") String userId,
      @PathVariable("prescription_id") String prescriptionId,
      @Valid @RequestBody Prescription prescription) {
    Prescription updatedPrescription = prescriptionService.updatePrescription(userId, prescriptionId, prescription);
    return ResponseEntity.ok(updatedPrescription);
  }

  @DeleteMapping("/{user_id}/records/prescriptions/{prescription_id}")
  public ResponseEntity<?> removeUserPrescription(
      @PathVariable("user_id") String userId,
      @PathVariable("prescription_id") String prescriptionId) {
    prescriptionService.deletePrescription(userId, prescriptionId);
    return ResponseEntity.ok().build();
  }


  @PostMapping("/{user_id}/records/request")
  public ResponseEntity<String> requestUserPrescriptions(
      @PathVariable("user_id") String userId,
      @RequestHeader("X-Requester-ID") String requesterId) {
    String requestId = sharingPermissionService.createSharingRequest(userId, requesterId);
    return ResponseEntity.ok(requestId);
  }

  @PostMapping("/{user_id}/records/{request_id}/accept")
  public ResponseEntity<?> acceptUserPrescriptionsRequest(
      @PathVariable("user_id") String userId,
      @PathVariable("request_id") String requestId) {
    sharingPermissionService.acceptSharingRequest(userId, requestId);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{user_id}/records/{request_id}/deny")
  public ResponseEntity<?> denyUserPrescriptionsRequest(
      @PathVariable("user_id") String userId,
      @PathVariable("request_id") String requestId) {
    sharingPermissionService.denySharingRequest(userId, requestId);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{user_id}/records/{request_id}/revoke")
  public ResponseEntity<?> revokeUserPrescriptionAccess(
      @PathVariable("user_id") String userId,
      @PathVariable("request_id") String requestId) {
    sharingPermissionService.revokeSharingPermission(userId, requestId);
    return ResponseEntity.ok().build();
  }
}