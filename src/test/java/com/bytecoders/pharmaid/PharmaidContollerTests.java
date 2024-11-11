package com.bytecoders.pharmaid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.bytecoders.pharmaid.repository.model.Medication;
import com.bytecoders.pharmaid.repository.model.Prescription;
import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.request.CreatePrescriptionRequest;
import com.bytecoders.pharmaid.request.LoginUserRequest;
import com.bytecoders.pharmaid.request.RegisterUserRequest;
import com.bytecoders.pharmaid.security.JwtAuthenticationFilter;
import com.bytecoders.pharmaid.security.JwtTokenProvider;
import com.bytecoders.pharmaid.service.MedicationService;
import com.bytecoders.pharmaid.service.PrescriptionService;
import com.bytecoders.pharmaid.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

/** This class represents a set of unit tests for {@code PharmaidController} class. */
@WebMvcTest(PharmaidController.class)
@ActiveProfiles("test")
public class PharmaidContollerTests {

  @Test
  public void registerSuccessTest() {
    final RegisterUserRequest request = new RegisterUserRequest();
    request.setEmail("ol2260@columbia.edu");
    request.setPassword("password");

    final User mockUser = new User();
    mockUser.setId("someid");
    mockUser.setEmail("ol2260@columbia.edu");

    when(userService.registerUser(request)).thenReturn(mockUser);

    final ResponseEntity<?> actualUser = testController.register(request);
    assertEquals(actualUser.getStatusCode(), HttpStatus.CREATED);
    assertEquals(actualUser.getBody(), mockUser);
  }

  @Test
  public void registerUserAlreadyExistsTest() {
    final RegisterUserRequest request = new RegisterUserRequest();
    request.setEmail("ol2260@columbia.edu");
    request.setPassword("password");

    when(userService.registerUser(request)).thenThrow(DataIntegrityViolationException.class);

    final ResponseEntity<?> actualUser = testController.register(request);
    assertEquals(actualUser.getStatusCode(), HttpStatus.BAD_REQUEST);
    assertEquals(actualUser.getBody(), "User already exists for this email");
  }

  @Test
  public void registerUserUnexpectedErrorTest() {
    final RegisterUserRequest request = new RegisterUserRequest();
    request.setEmail("ol2260@columbia.edu");
    request.setPassword("password");

    when(userService.registerUser(request)).thenThrow(RuntimeException.class);

    final ResponseEntity<?> actualUser = testController.register(request);
    assertEquals(actualUser.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    assertEquals(actualUser.getBody(), "Something went wrong");
  }

  /** Test for successful user login. */
  @Test
  void testLoginSuccess() throws Exception {
    LoginUserRequest request = new LoginUserRequest();
    request.setEmail("test@example.com");
    request.setPassword("password");

    User mockUser = new User();
    mockUser.setId("userId");
    mockUser.setEmail("test@example.com");

    when(userService.loginUser(request)).thenReturn(Optional.of(mockUser));

    ResponseEntity<String> response = testController.login(request);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(objectMapper.writeValueAsString(mockUser), response.getBody());
  }

  /** Test for failed user login. */
  @Test
  void testLoginFailed() {
    LoginUserRequest request = new LoginUserRequest();
    request.setEmail("wrong@example.com");
    request.setPassword("wrongpassword");

    when(userService.loginUser(request)).thenReturn(Optional.empty());

    ResponseEntity<String> response = testController.login(request);
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertEquals("Forbidden", response.getBody());
  }

  /** Test for getting all medications. */
  @Test
  void testGetAllMedications() {
    Medication med1 = new Medication();
    med1.setMedicationId("med1");
    Medication med2 = new Medication();
    med2.setMedicationId("med2");

    when(medicationService.getAllMedications()).thenReturn(Arrays.asList(med1, med2));

    ResponseEntity<?> response = testController.getAllMedications();
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(Arrays.asList(med1, med2), response.getBody());
  }

  /** Test for adding a prescription successfully. */
  @Test
  void testAddPrescriptionSuccess() {

    CreatePrescriptionRequest request = new CreatePrescriptionRequest();
    request.setMedicationId("medId");
    request.setDosage(1);
    request.setNumOfDoses(2);
    request.setStartDate(new Date());
    request.setEndDate(new Date());
    request.setIsActive(true);

    User mockUser = new User();
    String userId = "userId";
    mockUser.setId(userId);

    Medication mockMed = new Medication();
    mockMed.setMedicationId("medId");

    Prescription mockPrescription = new Prescription();
    mockPrescription.setPrescriptionId("prescriptionId");

    when(userService.getUser(userId)).thenReturn(Optional.of(mockUser));
    when(medicationService.getMedication("medId")).thenReturn(Optional.of(mockMed));
    when(prescriptionService.createPrescription(any(Prescription.class)))
        .thenReturn(mockPrescription);

    ResponseEntity<?> response = testController.addPrescription(userId, request);
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals(mockPrescription, response.getBody());
  }

  /** Test for adding a prescription with non-existent user. */
  @Test
  void testAddPrescriptionUserNotFound() {
    String userId = "nonExistentUserId";
    CreatePrescriptionRequest request = new CreatePrescriptionRequest();

    when(userService.getUser(userId)).thenReturn(Optional.empty());

    ResponseEntity<?> response = testController.addPrescription(userId, request);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertEquals("Provided User doesn't exist", response.getBody());
  }

  /** Test for getting prescriptions for a user. */
  @Test
  void testGetPrescriptionsForUser() {
    String userId = "userId";
    User mockUser = new User();
    mockUser.setId(userId);

    Prescription prescription1 = new Prescription();
    prescription1.setPrescriptionId("prescription1");
    Prescription prescription2 = new Prescription();
    prescription2.setPrescriptionId("prescription2");

    when(userService.getUser(userId)).thenReturn(Optional.of(mockUser));
    when(prescriptionService.getPrescriptionsForUser(userId))
        .thenReturn(Arrays.asList(prescription1, prescription2));

    ResponseEntity<?> response = testController.getPrescriptionsForUser(userId);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(Arrays.asList(prescription1, prescription2), response.getBody());
  }

  /** Test for getting prescriptions for a non-existent user. */
  @Test
  void testGetPrescriptionsForNonExistentUser() {
    String userId = "nonExistentUserId";

    when(userService.getUser(userId)).thenReturn(Optional.empty());

    ResponseEntity<?> response = testController.getPrescriptionsForUser(userId);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertEquals("Provided User doesn't exist", response.getBody());
  }

  @MockBean private UserService userService;

  @Autowired public PharmaidController testController;

  @MockBean private MedicationService medicationService;

  @MockBean private PrescriptionService prescriptionService;

  @Autowired private ObjectMapper objectMapper;

  @MockBean
  private JwtTokenProvider tokenProvider;

  @MockBean
  private JwtAuthenticationFilter jwtAuthenticationFilter;
}
