package com.bytecoders.pharmaid;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.bytecoders.pharmaid.openapi.model.*;
import com.bytecoders.pharmaid.repository.model.Medication;
import com.bytecoders.pharmaid.repository.model.Prescription;
import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.service.MedicationService;
import com.bytecoders.pharmaid.service.PrescriptionService;
import com.bytecoders.pharmaid.service.UserService;
import com.bytecoders.pharmaid.util.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

import org.apache.coyote.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

/**
 * This class represents a set of unit tests for {@code PharmaidController} class.
 */
@WebMvcTest(PharmaidController.class)
public class PharmaidControllerTests {

  @Test
  public void registerSuccessTest() {
    final RegisterUserRequest request = new RegisterUserRequest();
    request.setEmail("ol2260@columbia.edu");
    request.setPassword("password");
    request.setUserType(UserType.PATIENT);

    final User mockUser = new User();
    mockUser.setId("someid");
    mockUser.setEmail("ol2260@columbia.edu");
    mockUser.setUserType(UserType.PATIENT);

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
    request.setUserType(UserType.PATIENT);

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
    request.setUserType(UserType.PATIENT);

    when(userService.registerUser(request)).thenThrow(RuntimeException.class);

    final ResponseEntity<?> actualUser = testController.register(request);
    assertEquals(actualUser.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    assertEquals(actualUser.getBody(), "Something went wrong");
  }

  /**
   * Test for successful user login.
   */
  @Test
  void testLoginSuccess() throws Exception {
    LoginUserRequest request = new LoginUserRequest();
    request.setEmail("test@example.com");
    request.setPassword("password");

    LoginUserResponse mockLoginResponse = new LoginUserResponse();
    mockLoginResponse.setUserId("userId");
    mockLoginResponse.setEmail("test@example.com");
    mockLoginResponse.setToken("mock.jwt.token");

    when(userService.loginUser(request)).thenReturn(Optional.of(mockLoginResponse));
    ResponseEntity<?> response = testController.login(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(mockLoginResponse, response.getBody());
  }

  /**
   * Test for failed user login.
   */
  @Test
  void testLoginFailed() {
    LoginUserRequest request = new LoginUserRequest();
    request.setEmail("wrong@example.com");
    request.setPassword("wrongpassword");

    when(userService.loginUser(request)).thenReturn(Optional.empty());

    ResponseEntity<?> response = testController.login(request);
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertEquals("Invalid email or password", response.getBody());
  }

  /**
   * Test for successfully deleting a user
   *
   */
  @Test
  public void deleteUserSuccess() {
    User mockUser = new User();
    String userId = "userId";
    mockUser.setId(userId);

    Prescription mockPrescription = new Prescription();
    String prescriptionId = "prescriptionId";
    mockPrescription.setUser(mockUser);
    mockPrescription.setId(prescriptionId);
    List<Prescription> mockUsersPrescriptions = new ArrayList<>();
    mockUsersPrescriptions.add(mockPrescription);

    final ResponseEntity<?> deletedUser = testController.deleteUser(userId);
    assertEquals(deletedUser.getStatusCode(), HttpStatus.OK);
    assertEquals(deletedUser.getBody(), "User deleted successfully");
  }

  /**
   * Test for deleting a user that does not exist
   *
   */
  @Test
  public void deleteUserInvalidUser() {
    String userId = "userId";

    when(userService.getUser(userId)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND,
            String.format("Provided userId does not exist: %s", userId)));

    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
      testController.deleteUser(userId);
    });

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals(String.format("Provided userId does not exist: %s", userId),
            exception.getReason());
  }

  /**
   * Test for getting all medications.
   */
  @Test
  void testGetAllMedications() {
    Medication med1 = new Medication();
    med1.setId("med1");
    Medication med2 = new Medication();
    med2.setId("med2");

    when(medicationService.getAllMedications()).thenReturn(Arrays.asList(med1, med2));

    ResponseEntity<?> response = testController.getAllMedications();
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(Arrays.asList(med1, med2), response.getBody());
  }

  /**
   * Test for adding a prescription successfully.
   */
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
    mockMed.setId("medId");

    Prescription mockPrescription = new Prescription();
    mockPrescription.setId("prescriptionId");

    when(userService.getUser(userId)).thenReturn(mockUser);
    when(medicationService.getMedication("medId")).thenReturn(mockMed);
    when(prescriptionService.createPrescription(any(Prescription.class))).thenReturn(
        mockPrescription);

    ResponseEntity<?> response = testController.addPrescription(userId, request);
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals(mockPrescription, response.getBody());
  }

  /**
   * Test for adding a prescription with non-existent user.
   */
  @Test
  void testAddPrescriptionUserNotFound() {
    String userId = "nonExistentUserId";
    CreatePrescriptionRequest request = new CreatePrescriptionRequest();

    when(userService.getUser(userId)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND,
        String.format("Provided userId does not exist: %s", userId)));

    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
      testController.addPrescription(userId, request);
    });

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals(String.format("Provided userId does not exist: %s", userId),
        exception.getReason());
  }

  @Test
  void testAddPrescriptionUserWithoutPermission() {
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

    doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN,
        String.format("User %s is not authorized to add prescriptions for user: %s",
            jwtUtils.getLoggedInUserId(), userId))).when(prescriptionService)
        .createPrescription(any(Prescription.class));

    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
      testController.addPrescription(userId, request);
    });

    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    assertEquals(String.format("User %s is not authorized to add prescriptions for user: %s",
        jwtUtils.getLoggedInUserId(), userId), exception.getReason());
  }

  /**
   * Test for updating a prescription successfully.
   */
  @Test
  void testUpdatePrescriptionSuccess() {
    UpdatePrescriptionRequest request = new UpdatePrescriptionRequest();
    request.setEndDate(new Date());
    request.setIsActive(true);

    User mockUser = new User();
    String userId = "userId";
    mockUser.setId(userId);
    when(userService.getUser(userId)).thenReturn(mockUser);

    Prescription mockPrescription = new Prescription();
    String prescriptionId = "prescriptionId";
    mockPrescription.setStartDate(new Date(request.getEndDate().getTime() - 24*60*60*1000));
    mockPrescription.setUser(mockUser);
    mockPrescription.setId(prescriptionId);
    when(prescriptionService.getPrescription(prescriptionId)).thenReturn(mockPrescription);

    Prescription updatedPrescription = new Prescription();
    updatedPrescription.setUser(mockUser);
    updatedPrescription.setId(prescriptionId);
    updatedPrescription.setStartDate(new Date(request.getEndDate().getTime() - 24*60*60*1000));
    updatedPrescription.setEndDate(request.getEndDate());
    updatedPrescription.setIsActive(true);
    when(prescriptionService.updatePrescription(any(Prescription.class))).thenReturn(updatedPrescription);

    ResponseEntity<?> response = testController.updateUsersPrescription(userId, prescriptionId, request);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(updatedPrescription, response.getBody());
  }

  /**
   * Test for updating a prescription without permission.
   */
  @Test
  void testUpdatePrescriptionUserWithoutPermission() {
    UpdatePrescriptionRequest request = new UpdatePrescriptionRequest();
    request.setEndDate(new Date());
    request.setIsActive(true);

    User mockUser = new User();
    String userId = "userId";
    mockUser.setId(userId);
    when(userService.getUser(userId)).thenReturn(mockUser);

    Prescription mockPrescription = new Prescription();
    String prescriptionId = "prescriptionId";
    mockPrescription.setUser(mockUser);
    mockPrescription.setId(prescriptionId);
    mockPrescription.setStartDate(new Date(request.getEndDate().getTime() - 24*60*60*1000));
    when(prescriptionService.getPrescription(prescriptionId)).thenReturn(mockPrescription);

    doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN,
            String.format("User %s is not authorized to edit prescriptions for user: %s",
                    jwtUtils.getLoggedInUserId(), userId))).when(prescriptionService)
            .updatePrescription(any(Prescription.class));

    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
      testController.updateUsersPrescription(userId, prescriptionId, request);
    });

    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    assertEquals(String.format("User %s is not authorized to edit prescriptions for user: %s",
            jwtUtils.getLoggedInUserId(), userId), exception.getReason());
  }

  /**
   * Test for updating a prescription with invalid prescription/user combo
   */
  @Test
  public void updatePrescriptionInvalidPrescriptionUserCombo() {
    User mockUser = new User();
    String userId = "userId";
    mockUser.setId(userId);
    when(userService.getUser(userId)).thenReturn(mockUser);

    User someOtherUser = new User();
    mockUser.setId("someOtherUserId");

    Prescription mockPrescription = new Prescription();
    String prescriptionId = "prescriptionId";
    mockPrescription.setUser(someOtherUser);
    mockPrescription.setId(prescriptionId);
    when(prescriptionService.getPrescription(prescriptionId)).thenReturn(mockPrescription);

    UpdatePrescriptionRequest request = new UpdatePrescriptionRequest();
    request.setEndDate(new Date());
    request.setIsActive(true);

    final ResponseEntity<?> updatedPrescription = testController.updateUsersPrescription(userId, prescriptionId, request);
    assertEquals(updatedPrescription.getStatusCode(), HttpStatus.NOT_FOUND);
    assertEquals(updatedPrescription.getBody(), "Provided prescription/user combination doesn't exist");
  }

  /**
   * Test for updating a user's prescriptions with an invalid update request
   */
  @Test
  public void updateUserPrescripionInvalidRequest() {
    String userId = "userId";
    String prescriptionId = "prescriptionId";
    UpdatePrescriptionRequest request = new UpdatePrescriptionRequest();

    final ResponseEntity<?> updatedPrescription = testController.updateUsersPrescription(userId, prescriptionId, request);
    assertEquals(HttpStatus.BAD_REQUEST, updatedPrescription.getStatusCode());
    assertEquals("Invalid update request", updatedPrescription.getBody() );
  }

  /**
   * Test for successfully deleting a user's prescription
   */
   @Test
   public void removePrescriptionSuccess() {
    User mockUser = new User();
    String userId = "userId";
    mockUser.setId(userId);

    Prescription mockPrescription = new Prescription();
    String prescriptionId = "prescriptionId";
    mockPrescription.setUser(mockUser);
    mockPrescription.setId(prescriptionId);
    mockPrescription.setEndDate(new Date());
    mockPrescription.setIsActive(true);

    when(userService.getUser(userId)).thenReturn(mockUser);
    when(prescriptionService.getPrescription(prescriptionId)).thenReturn(mockPrescription);
 
    final ResponseEntity<?> deletedPrescription = testController.removePrescription(userId, prescriptionId);
    assertEquals(HttpStatus.OK, deletedPrescription.getStatusCode());
    assertEquals("Prescription removed.", deletedPrescription.getBody() );
   }


  /**
   * Test for failed remove prescription operation due to invalid user
   */
  @Test
  public void removePrescriptionInvalidUser() {
    User mockUser = new User();
    String userId = "userId";
    mockUser.setId(userId);

    Prescription mockPrescription = new Prescription();
    String prescriptionId = "prescriptionId";
    mockPrescription.setId(prescriptionId);

    when(userService.getUser(userId)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND,
            String.format("Provided user does not exist: %s", userId)));

    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
      testController.removePrescription(userId, prescriptionId);
    });

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals(String.format("Provided user does not exist: %s", userId),
            exception.getReason());

  }

  /**
   * Test for failed remove prescription operation due to invalid prescription
   */
  @Test
  public void removePrescriptionInvalidPrescription() {
    User mockUser = new User();
    String userId = "userId";
    mockUser.setId(userId);

    Prescription mockPrescription = new Prescription();
    String prescriptionId = "prescriptionId";
    mockPrescription.setId(prescriptionId);

    when(prescriptionService.getPrescription(prescriptionId)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND,
            String.format("Provided prescription does not exist: %s", prescriptionId)));

    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
      testController.removePrescription(userId, prescriptionId);
    });

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals(String.format("Provided prescription does not exist: %s", prescriptionId),
            exception.getReason());
  }

  /**
   * Test for failure to remove prescription operation because prescription does not exist for the provided user
   */
  @Test
  public void removePrescriptionInvalidUserPrescriptionCombo() {
    User mockUser = new User();
    String userId = "userId";
    mockUser.setId(userId);

    User someOtherUser = new User();
    String someOtherUserId = "someOtherUserId";
    mockUser.setId(someOtherUserId);

    Prescription mockPrescription = new Prescription();
    String prescriptionId = "prescriptionId";
    mockPrescription.setId(prescriptionId);
    mockPrescription.setUser(someOtherUser);

    when(prescriptionService.getPrescription(prescriptionId)).thenReturn(mockPrescription);
    when(userService.getUser(userId)).thenReturn(mockUser);

    ResponseEntity<?> removedPrescription = testController.removePrescription(userId, prescriptionId);
    assertEquals(HttpStatus.NOT_FOUND, removedPrescription.getStatusCode());
    assertEquals("Provided prescription/user combination doesn't exist", removedPrescription.getBody());
  }

  /**
   * Test for removing a user's prescription without permission
   */
  @Test
  void removePrescriptionWithoutPermission() {
    User mockUser = new User();
    String userId = "userId";
    mockUser.setId(userId);

    Prescription mockPrescription = new Prescription();
    String prescriptionId = "prescriptionId";
    mockPrescription.setId(prescriptionId);
    mockPrescription.setUser(mockUser);

    when(userService.getUser(userId)).thenReturn(mockUser);
    when(prescriptionService.getPrescription(prescriptionId)).thenReturn(mockPrescription);

    doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN,
            String.format("User %s is not authorized to edit prescriptions of user: %s",
                    jwtUtils.getLoggedInUserId(), userId))).when(prescriptionService)
            .deletePrescription(prescriptionId);

    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
      testController.removePrescription(userId, prescriptionId);
    });

    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    assertEquals(String.format("User %s is not authorized to edit prescriptions of user: %s",
            jwtUtils.getLoggedInUserId(), userId), exception.getReason());
  }

  /**
   * Test for getting prescriptions for a user.
   */
  @Test
  void testGetPrescriptionsForUser() {
    String userId = "userId";
    User mockUser = new User();
    mockUser.setId(userId);

    Prescription prescription1 = new Prescription();
    prescription1.setId("prescription1");
    Prescription prescription2 = new Prescription();
    prescription2.setId("prescription2");

    when(userService.getUser(userId)).thenReturn(mockUser);
    when(prescriptionService.getPrescriptionsForUser(userId)).thenReturn(
        Arrays.asList(prescription1, prescription2));

    ResponseEntity<?> response = testController.getPrescriptionsForUser(userId);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(Arrays.asList(prescription1, prescription2), response.getBody());
  }

  @Test
  void testGetPrescriptionsUserWithoutPermission() {
    String userId = "userId";

    doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN,
        String.format("User %s is not authorized to view prescriptions of user: %s",
            jwtUtils.getLoggedInUserId(), userId))).when(prescriptionService)
        .getPrescriptionsForUser(userId);

    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
      testController.getPrescriptionsForUser(userId);
    });

    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    assertEquals(String.format("User %s is not authorized to view prescriptions of user: %s",
        jwtUtils.getLoggedInUserId(), userId), exception.getReason());
  }

  /**
   * Test for getting prescriptions for a non-existent user.
   */
  @Test
  void testGetPrescriptionsForNonExistentUser() {
    String userId = "nonExistentUserId";

    when(userService.getUser(userId)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND,
        String.format("Provided userId does not exist: %s", userId)));

    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
      testController.getPrescriptionsForUser(userId);
    });

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals(String.format("Provided userId does not exist: %s", userId),
        exception.getReason());
  }

  @MockBean
  private UserService userService;

  @Autowired
  public PharmaidController testController;

  @MockBean
  private MedicationService medicationService;

  @MockBean
  private PrescriptionService prescriptionService;

  @MockBean
  private JwtUtils jwtUtils;

  @Autowired
  private ObjectMapper objectMapper;
}
