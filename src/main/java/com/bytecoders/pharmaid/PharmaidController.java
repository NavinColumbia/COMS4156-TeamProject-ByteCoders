package com.bytecoders.pharmaid;

import com.bytecoders.pharmaid.exception.AuthenticationException;
import com.bytecoders.pharmaid.repository.model.Medication;
import com.bytecoders.pharmaid.repository.model.Prescription;
import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.request.CreatePrescriptionRequest;
import com.bytecoders.pharmaid.request.LoginUserRequest;
import com.bytecoders.pharmaid.request.RegisterUserRequest;
import com.bytecoders.pharmaid.response.ErrorResponse;
import com.bytecoders.pharmaid.response.LoginResponse;
import com.bytecoders.pharmaid.response.RegistrationResponse;
import com.bytecoders.pharmaid.security.JwtTokenProvider;
import com.bytecoders.pharmaid.service.AuthorizationService;
import com.bytecoders.pharmaid.service.MedicationService;
import com.bytecoders.pharmaid.service.PrescriptionService;
import com.bytecoders.pharmaid.service.SharingPermissionService;
import com.bytecoders.pharmaid.service.UserService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** This class contains all the API routes for the system. */
@Slf4j
@RestController
public class PharmaidController {

  @Autowired private UserService userService;

  @Autowired private MedicationService medicationService;

  @Autowired private PrescriptionService prescriptionService;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private JwtTokenProvider tokenProvider;

  @Autowired private AuthorizationService authorizationService;

  @Autowired private SharingPermissionService sharingPermissionService;

  /**
   * Basic hello endpoint for testing.
   *
   * @return A String
   */
  @GetMapping({"/hello"})
  public String index() {
    return "Hello :)";
  }

  /**
   * Handles user registration.
   *
   * @param request the registration request
   * @return ResponseEntity with registration result
   */

  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody RegisterUserRequest request) {
    try {
      log.debug("Registering user with email: {}", request.getEmail());

      User user = new User();
      user.setEmail(request.getEmail());
      user.setHashedPassword(passwordEncoder.encode(request.getPassword()));
      user.setUserType(request.getUserType());

      User savedUser = userService.createUser(user);

      return ResponseEntity.status(HttpStatus.CREATED)
          .body(RegistrationResponse.builder()
              .userId(savedUser.getId())
              .email(savedUser.getEmail())
              .userType(savedUser.getUserType())
              .message("Registration successful. Please login to continue.")
              .build());

    } catch (DataIntegrityViolationException e) {
      log.debug("Registration failed - user already exists with email: {}", request.getEmail());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ErrorResponse.builder()
              .message("User already exists with this email")
              .timestamp(LocalDateTime.now())
              .build());
    } catch (Exception e) {
      log.error("Error during registration", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ErrorResponse.builder()
              .message("Registration failed")
              .timestamp(LocalDateTime.now())
              .build());
    }
  }

  /**
   * Handles user login.
   *
   * @param request the login request
   * @return ResponseEntity with JWT token if successful
   */
  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody LoginUserRequest request) {
    try {
      User user = userService.getUserByEmail(request.getEmail())
          .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

      if (!passwordEncoder.matches(request.getPassword(), user.getHashedPassword())) {
        throw new AuthenticationException("Invalid credentials");
      }

      String token = tokenProvider.generateToken(user.getId());

      return ResponseEntity.ok(LoginResponse.builder()
          .token("Bearer " + token)
          .userId(user.getId())
          .email(user.getEmail())
          .userType(user.getUserType())
          .build());

    } catch (AuthenticationException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ErrorResponse.builder()
              .message("Invalid email or password")
              .timestamp(LocalDateTime.now())
              .build());
    } catch (Exception e) {
      log.error("Login error", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ErrorResponse.builder()
              .message("An unexpected error occurred")
              .timestamp(LocalDateTime.now())
              .build());
    }
  }

  /**
   * Get all available medications endpoint.
   *
   * @return a ResponseEntity with a list of medications if the operation is successful, or an error
   *     message if an error occurred
   */
  @GetMapping(path = "/medications")
  public ResponseEntity<?> getAllMedications() {
    try {
      final List<Medication> medications = medicationService.getAllMedications();
      return new ResponseEntity<>(medications, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(
          "Unexpected error encountered during getting a list of medications",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

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

  /**
   * Add prescription endpoint.
   *
   * @param userId user to add a prescription for
   * @param request request containing prescription-related data
   * @return a ResponseEntity with the newly creation prescription if the operation is successful,
   *     or an error message if an error occurred
   */
  @PostMapping("/{userId}/records/prescriptions")
  public ResponseEntity<?> addPrescription(
      @PathVariable("userId") String userId,
      @Valid @RequestBody CreatePrescriptionRequest request) {
    try {
      // Get current user ID from security context
      String currentUserId = getCurrentUserId();

      // Check authorization
      if (!authorizationService.canModifyUserRecords(currentUserId, userId)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body("Not authorized to create prescriptions for this user");
      }

      // Get user
      User user =
          userService.getUser(userId).orElseThrow(() -> new RuntimeException("User not found"));

      // Get medication
      Medication medication =
          medicationService
              .getMedication(request.getMedicationId())
              .orElseThrow(() -> new RuntimeException("Medication not found"));

      // Create prescription
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

  /**
   * Endpoint to get user's prescriptions. Only accessible by: - The user themselves - Users with
   * explicit VIEW or EDIT permission - First responders
   *
   * @param userId user whose prescriptions we're trying to retrieve
   * @return a ResponseEntity with user's prescriptions if authorized, or an error message
   */
  @GetMapping(path = "/users/{userId}/prescriptions")
  public ResponseEntity<?> getPrescriptionsForUser(@PathVariable("userId") String userId) {
    try {
      // Get current user's ID
      String currentUserId = getCurrentUserId();

      // Check if user exists
      final Optional<User> userOptional = userService.getUser(userId);
      if (userOptional.isEmpty()) {
        return new ResponseEntity<>("Provided User doesn't exist", HttpStatus.NOT_FOUND);
      }

      // Check if current user is authorized to view these prescriptions
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

  /**
   * Handles user logout. Invalidates the current session and clears security context.
   *
   * @return ResponseEntity indicating logout success or failure
   */
  @PostMapping("/logout")
  public ResponseEntity<?> logout() {
    // The security config handles token validation before reaching here
    return ResponseEntity.ok("Logged out successfully");
  }
}
