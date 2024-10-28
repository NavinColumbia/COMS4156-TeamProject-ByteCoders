package com.bytecoders.pharmaid;

import com.bytecoders.pharmaid.repository.model.Medication;
import com.bytecoders.pharmaid.repository.model.Prescription;
import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.request.CreatePrescriptionRequest;
import com.bytecoders.pharmaid.request.LoginUserRequest;
import com.bytecoders.pharmaid.request.RegisterUserRequest;
import com.bytecoders.pharmaid.service.MedicationService;
import com.bytecoders.pharmaid.service.PrescriptionService;
import com.bytecoders.pharmaid.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
   * Login user endpoint.
   *
   * @param request LoginUserRequest
   * @return a ResponseEntity with a success message if the operation is successful, or an error
   *     message if the login is unsuccessful
   */
  @PostMapping(path = "/login")
  public ResponseEntity<String> login(@RequestBody @Valid LoginUserRequest request) {
    try {
      Optional<User> user = userService.loginUser(request);

      if (user.isEmpty()) {
        return new ResponseEntity<>("Forbidden", HttpStatus.UNAUTHORIZED);
      }

      ObjectMapper mapper = new ObjectMapper();
      String json = mapper.writeValueAsString(user.get());
      return new ResponseEntity<>(json, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>("Unexpected error encountered during login",
          HttpStatus.INTERNAL_SERVER_ERROR);
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

  /**
   * Add prescription endpoint.
   *
   * @param userId  user to add a prescription for
   * @param request request containing prescription-related data
   * @return a ResponseEntity with the newly creation prescription if the operation is successful,
   *     or an error message if an error occurred
   */
  @PostMapping(path = "/users/{userId}/prescriptions")
  public ResponseEntity<?> addPrescription(@PathVariable("userId") String userId,
      @RequestBody @Valid CreatePrescriptionRequest request) {
    try {
      final Optional<User> userOptional = userService.getUser(userId);

      if (userOptional.isEmpty()) {
        return new ResponseEntity<>("Provided User doesn't exist", HttpStatus.NOT_FOUND);
      }

      final Optional<Medication>
          medOptional =
          medicationService.getMedication(request.getMedicationId());

      if (medOptional.isEmpty()) {
        return new ResponseEntity<>("Medication doesn't exist", HttpStatus.NOT_FOUND);
      }

      final Prescription prescription = new Prescription();
      prescription.setUser(userOptional.get());
      prescription.setMedication(medOptional.get());
      prescription.setDosage(request.getDosage());
      prescription.setNumOfDoses(request.getNumOfDoses());
      prescription.setStartDate(request.getStartDate());
      prescription.setEndDate(request.getEndDate());
      prescription.setIsActive(request.getIsActive());

      return new ResponseEntity<>(prescriptionService.createPrescription(prescription),
          HttpStatus.CREATED);
    } catch (Exception e) {
      return new ResponseEntity<>("Unexpected error encountered while creating a prescription",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Endpoint to get user's prescriptions.
   *
   * @param userId user whose prescriptions we're trying to retrieve
   * @return a ResponseEntity with user's prescriptions if the operation is successful, or an error
   *     message if an error occurred
   */
  @GetMapping(path = "/users/{userId}/prescriptions")
  public ResponseEntity<?> getPrescriptionsForUser(@PathVariable("userId") String userId) {
    try {
      final Optional<User> userOptional = userService.getUser(userId);

      if (userOptional.isEmpty()) {
        return new ResponseEntity<>("Provided User doesn't exist", HttpStatus.NOT_FOUND);
      }

      return new ResponseEntity<>(prescriptionService.getPrescriptionsForUser(userId),
          HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>("Unexpected error encountered while getting user prescriptions",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
