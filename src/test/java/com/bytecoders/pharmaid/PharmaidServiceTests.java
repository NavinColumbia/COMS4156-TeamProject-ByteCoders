package com.bytecoders.pharmaid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.bytecoders.pharmaid.repository.model.Medication;
import com.bytecoders.pharmaid.repository.model.Prescription;
import com.bytecoders.pharmaid.repository.model.SharingRequest;
import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.request.CreatePrescriptionRequest;
import com.bytecoders.pharmaid.service.AuthorizationService;
import com.bytecoders.pharmaid.service.MedicationService;
import com.bytecoders.pharmaid.service.PrescriptionService;
import com.bytecoders.pharmaid.service.SharingPermissionService;
import com.bytecoders.pharmaid.service.UserService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Tests for prescription-related endpoints and services.
 * Validates prescription management and access control.
 */
public class PharmaidServiceTests {

  @Mock private SecurityContext securityContext;

  @Mock private Authentication authentication;

  @Mock private UserService userService;

  @Mock private SharingPermissionService sharingPermissionService;

  @Mock private AuthorizationService authorizationService;

  @Mock private PrescriptionService prescriptionService;

  @Mock private MedicationService medicationService;

  @Mock private User user;

  @Mock private Medication medication;

  @Mock private Prescription prescription;

  /**
   * Set up prior to tests.
   */
  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
  }

  /**
   * Tests the getCurrentUserId method when user is properly authenticated.
   * Verifies that the correct user ID is returned.
   */
  @Test
  public void testGetCurrentUserId_UserAuthenticated() {
    // Setup
    when(authentication.getPrincipal()).thenReturn(user);
    when(user.getId()).thenReturn("user123");

    // Execute
    String currentUserId = getCurrentUserId();

    // Assert
    assertEquals("user123", currentUserId);
  }

  @Test
  public void testGetCurrentUserId_UserNotAuthenticated() {
    // Setup
    when(authentication.getPrincipal()).thenReturn(null);

    // Execute and Assert
    assertThrows(RuntimeException.class, this::getCurrentUserId);
  }

  @Test
  public void testAddPrescription_Unauthorized() {
    // Setup
    String userId = "user123";
    String currentUserId = "user456";
    when(authentication.getPrincipal()).thenReturn(user);
    when(user.getId()).thenReturn(currentUserId);
    when(authorizationService.canModifyUserRecords(currentUserId, userId)).thenReturn(false);

    // Execute
    ResponseEntity<?> response = addPrescription(userId, new CreatePrescriptionRequest());

    // Assert
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertEquals("Not authorized to create prescriptions for this user", response.getBody());
  }

  @Test
  public void testAddPrescription_Authorized() {
    // Setup
    String userId = "user123";
    String currentUserId = "user123";
    when(authentication.getPrincipal()).thenReturn(user);
    when(user.getId()).thenReturn(currentUserId);
    when(authorizationService.canModifyUserRecords(currentUserId, userId)).thenReturn(true);
    when(userService.getUser(userId)).thenReturn(Optional.of(user));
    when(medicationService.getMedication("medication123")).thenReturn(Optional.of(medication));
    when(prescriptionService.createPrescription(any(Prescription.class))).thenReturn(prescription);

    // Execute
    CreatePrescriptionRequest request = new CreatePrescriptionRequest();
    request.setMedicationId("medication123");
    ResponseEntity<?> response = addPrescription(userId, request);

    // Assert
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals(prescription, response.getBody());
  }

  @Test
  public void testGetPrescriptionsForUser_Authorized() {
    // Setup
    String userId = "user123";
    String currentUserId = "user456";
    when(authentication.getPrincipal()).thenReturn(user);
    when(user.getId()).thenReturn(currentUserId);
    when(userService.getUser(userId)).thenReturn(Optional.of(user));
    when(authorizationService.canAccessUserRecords(currentUserId, userId)).thenReturn(true);
    List<Prescription> prescriptions = new ArrayList<>();
    prescriptions.add(prescription);
    when(prescriptionService.getPrescriptionsForUser(userId)).thenReturn(prescriptions);

    // Execute
    ResponseEntity<?> response = getPrescriptionsForUser(userId);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(prescriptions, response.getBody());
  }

  @Test
  public void testGetPrescriptionsForUser_Unauthorized() {
    // Setup
    String userId = "user123";
    String currentUserId = "user456";
    when(authentication.getPrincipal()).thenReturn(user);
    when(user.getId()).thenReturn(currentUserId);
    when(userService.getUser(userId)).thenReturn(Optional.of(user));
    when(authorizationService.canAccessUserRecords(currentUserId, userId)).thenReturn(false);

    // Execute
    ResponseEntity<?> response = getPrescriptionsForUser(userId);

    // Assert
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertEquals("You are not authorized to view these prescriptions", response.getBody());
  }

  @Test
  public void testRequestUserPrescriptions() {
    // Setup
    String ownerId = "user123";
    String requestId = "request123";
    SharingRequest request = new SharingRequest();
    when(sharingPermissionService.createSharingRequest(ownerId, request)).thenReturn(requestId);

    // Execute
    ResponseEntity<String> response = requestUserPrescriptions(ownerId, request);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(requestId, response.getBody());
  }

  private String getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof User) {
      User user = (User) authentication.getPrincipal();
      return user.getId();
    }
    throw new RuntimeException("User not authenticated");
  }

  private ResponseEntity<?> addPrescription(String userId, CreatePrescriptionRequest request) {
    try {
      String currentUserId = getCurrentUserId();
      if (!authorizationService.canModifyUserRecords(currentUserId, userId)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body("Not authorized to create prescriptions for this user");
      }

      User user =
          userService.getUser(userId).orElseThrow(() -> new RuntimeException("User not found"));

      Medication medication =
          medicationService
              .getMedication(request.getMedicationId())
              .orElseThrow(() -> new RuntimeException("Medication not found"));

      Prescription prescription = new Prescription();
      prescription.setUser(user);
      prescription.setMedication(medication);
      prescription.setDosage(request.getDosage());
      prescription.setNumOfDoses(request.getNumOfDoses());
      prescription.setStartDate(request.getStartDate());
      prescription.setEndDate(request.getEndDate());
      prescription.setIsActive(request.getIsActive());

      Prescription savedPrescription = prescriptionService.createPrescription(prescription);
      return ResponseEntity.status(HttpStatus.CREATED).body(savedPrescription);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error creating prescription: " + e.getMessage());
    }
  }

  private ResponseEntity<?> getPrescriptionsForUser(String userId) {
    try {
      String currentUserId = getCurrentUserId();
      Optional<User> userOptional = userService.getUser(userId);
      if (userOptional.isEmpty()) {
        return new ResponseEntity<>("Provided User doesn't exist", HttpStatus.NOT_FOUND);
      }

      if (!authorizationService.canAccessUserRecords(currentUserId, userId)) {
        return new ResponseEntity<>(
            "You are not authorized to view these prescriptions", HttpStatus.FORBIDDEN);
      }

      List<Prescription> prescriptions = prescriptionService.getPrescriptionsForUser(userId);
      return new ResponseEntity<>(prescriptions, HttpStatus.OK);

    } catch (AccessDeniedException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
    } catch (Exception e) {
      return new ResponseEntity<>(
          "Unexpected error encountered while getting user prescriptions",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private ResponseEntity<String> requestUserPrescriptions(String userId, SharingRequest request) {
    String requestId = sharingPermissionService.createSharingRequest(userId, request);
    return ResponseEntity.ok(requestId);
  }
}
