package com.bytecoders.pharmaid;

import com.bytecoders.pharmaid.openapi.model.CreatePrescriptionRequest;
import com.bytecoders.pharmaid.openapi.model.LoginUserRequest;
import com.bytecoders.pharmaid.openapi.model.LoginUserResponse;
import com.bytecoders.pharmaid.openapi.model.RegisterUserRequest;
import com.bytecoders.pharmaid.openapi.model.UpdatePrescriptionRequest;
import com.bytecoders.pharmaid.repository.model.Medication;
import com.bytecoders.pharmaid.repository.model.Prescription;
import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.service.MedicationService;
import com.bytecoders.pharmaid.service.PrescriptionService;
import com.bytecoders.pharmaid.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * This class contains all the API routes for the system.
 */
@RestController
public class PharmaidController {

  @Autowired
  private UserService userService;

  @Autowired
  private MedicationService medicationService;

  @Autowired
  private PrescriptionService prescriptionService;

  private static final String USER_ID = "userId";
  private static final String PRESCRIPTION_ID = "prescriptionId";

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
   * Register user endpoint.
   *
   * @param request RegisterUserRequest
   * @return a ResponseEntity with a success message if the operation is successful, or an error
   *     message if the registration is unsuccessful
   */
  @PostMapping({"/register"})
  public ResponseEntity<?> register(@RequestBody @Valid RegisterUserRequest request) {
    try {
      final User user = userService.registerUser(request);
      return new ResponseEntity<>(user, HttpStatus.CREATED);
    } catch (DataIntegrityViolationException e) {
      return new ResponseEntity<>("User already exists for this email", HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
 
  /**
   * Delete a user endpoint.
   *
   * @param userId  user to delete
   * @return a ResponseEntity with a success message if user is successfully deleted, or an error
   *     message if the deletion is unsuccessful
   */
  @DeleteMapping(path = "/users/{userId}")
  public ResponseEntity<?> deleteUser(@PathVariable(USER_ID) String userId) {
    try {
      // check if user exists
      userService.getUser(userId);

      for(Prescription p : prescriptionService.getPrescriptionsForUser(userId)) {
        prescriptionService.deletePrescription(p.getId());
      }

      userService.deleteUser(userId);
      return new ResponseEntity<>("User deleted successfully", HttpStatus.OK);
    } catch (ResponseStatusException e) {
      throw e; // propagates to globalExceptionHandler
    } catch (Exception e) {
      return new ResponseEntity<>("Unexpected error encountered while deleting user",
              HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Login user endpoint.
   *
   * @param request LoginUserRequest
   * @return a ResponseEntity with a success message if the operation is successful, or an error
   *     message if the login is unsuccessful
   */
  @PostMapping(path = "/login")
  public ResponseEntity<?> login(@RequestBody @Valid LoginUserRequest request) {
    try {
      Optional<LoginUserResponse> jwt = userService.loginUser(request);

      if (jwt.isEmpty()) {
        return new ResponseEntity<>("Invalid email or password", HttpStatus.UNAUTHORIZED);
      }
      return new ResponseEntity<>(jwt.get(), HttpStatus.OK);

    } catch (Exception e) {
      return new ResponseEntity<>("Unexpected error encountered during login",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Get all available medications endpoint.
   *
   * @return a ResponseEntity with a list of medications if the operation is successful, or an
   *     error message if an error occurred
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

  /**
   * Add prescription endpoint.
   *
   * @param userId  user to add a prescription for
   * @param request request containing prescription-related data
   * @return a ResponseEntity with the newly created prescription if the operation is successful,
   *     or an error message if an error occurred
   */
  @PostMapping(path = "/users/{userId}/prescriptions")
  public ResponseEntity<?> addPrescription(@PathVariable(USER_ID) String userId,
      @RequestBody @Valid CreatePrescriptionRequest request) {
    try {
      final User user = userService.getUser(userId);
      final Medication medication = medicationService.getMedication(request.getMedicationId());

      final Prescription prescription = new Prescription();
      prescription.setUser(user);
      prescription.setMedication(medication);
      prescription.setDosage(request.getDosage());
      prescription.setNumOfDoses(request.getNumOfDoses());
      prescription.setStartDate(request.getStartDate());
      prescription.setEndDate(request.getEndDate());
      prescription.setIsActive(request.getIsActive());

      return new ResponseEntity<>(prescriptionService.createPrescription(prescription),
          HttpStatus.CREATED);
    } catch (ResponseStatusException e) {
      throw e; // propagates to globalExceptionHandler
    } catch (Exception e) {
      return new ResponseEntity<>("Unexpected error encountered while creating a prescription",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Endpoint to update a user's prescriptions.
   *
   * @param userId user whose prescriptions we're trying to retrieve
   * @param prescriptionId  prescription we are trying to update
   * @param request containing prescription update
   * @return a ResponseEntity with updated prescription if the operation is successful,
   *      or an error message if an error occurred
   */
  @PatchMapping(path = "/users/{userId}/prescriptions/{prescriptionId}")
  public ResponseEntity<?> updateUsersPrescription(
      @PathVariable(USER_ID) String userId,
      @PathVariable(PRESCRIPTION_ID) String prescriptionId,
      @RequestBody @Valid UpdatePrescriptionRequest request) {
    try {
      final User user = userService.getUser(userId);
      final Prescription prescription = prescriptionService.getPrescription(prescriptionId);

      if(Objects.isNull(request.getIsActive()) && Objects.isNull(request.getEndDate())) {
        return new ResponseEntity<>("Invalid update request", HttpStatus.BAD_REQUEST);
      }

      if(user.equals(prescription.getUser())) {
        if(request.getEndDate() != null) {
          if(request.getEndDate().compareTo(prescription.getStartDate()) > 0) {
            prescription.setEndDate(request.getEndDate());
            // ensure end date is past start date
          } else {
            return new ResponseEntity<>("Invalid end date", HttpStatus.BAD_REQUEST);
          }
        }
        if(request.getIsActive() != null) {
          prescription.setIsActive(request.getIsActive());
        }
      } else {
        return new ResponseEntity<>("Provided prescription/user combination doesn't exist", HttpStatus.NOT_FOUND);
      }

      return new ResponseEntity<>(prescriptionService.updatePrescription(prescription), HttpStatus.OK);
    } catch (ResponseStatusException e) {
      throw e; // propagates to globalExceptionHandler
    } catch (Exception e) {
      return new ResponseEntity<>("Unexpected error encountered while updating the prescription",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Endpoint to get user's prescriptions.
   *
   * @param userId user whose prescriptions we're trying to retrieve
   * @return a ResponseEntity with user's prescriptions if the operation is successful, or an
   *     error message if an error occurred
   */
  @GetMapping(path = "/users/{userId}/prescriptions")
  public ResponseEntity<?> getPrescriptionsForUser(@PathVariable(USER_ID) String userId) {
    try {
      // check if user exists
      userService.getUser(userId);

      return new ResponseEntity<>(prescriptionService.getPrescriptionsForUser(userId),
          HttpStatus.OK);
    } catch (ResponseStatusException e) {
      throw e; // propagates to globalExceptionHandler
    } catch (Exception e) {
      return new ResponseEntity<>("Unexpected error encountered while getting user prescriptions",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Endpoint to remove a user's prescription.
   *
   * @param userId user whose prescription we're trying to remove
   * @param prescriptionId prescription we are trying to delete 
   * @return a ResponseEntity with user's prescriptions if the operation is successful,
   *      or an error message if an error occurred
   */
  @DeleteMapping(path = "/users/{userId}/prescriptions/{prescriptionId}")
  public ResponseEntity<?> removePrescription(
      @PathVariable(USER_ID) String userId, 
      @PathVariable(PRESCRIPTION_ID) String prescriptionId) {
    try {
      final User user = userService.getUser(userId);
      final Prescription prescription = prescriptionService.getPrescription(prescriptionId);

      if(user.equals(prescription.getUser())) {
        prescriptionService.deletePrescription(prescriptionId);
      } else {
        return new ResponseEntity<>("Provided prescription/user combination doesn't exist", HttpStatus.NOT_FOUND);
      }

      return new ResponseEntity<>("Prescription removed.", HttpStatus.OK);
    } catch (ResponseStatusException e) {
      throw e; // propagates to globalExceptionHandler
    } catch (Exception e) {
      return new ResponseEntity<>("Unexpected error encountered while getting user prescriptions",
              HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }


}