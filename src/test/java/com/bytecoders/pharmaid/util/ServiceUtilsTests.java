package com.bytecoders.pharmaid.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.bytecoders.pharmaid.repository.PrescriptionRepository;
import com.bytecoders.pharmaid.repository.UserRepository;
import com.bytecoders.pharmaid.repository.model.Prescription;
import com.bytecoders.pharmaid.repository.model.User;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Tests for {@link ServiceUtils}. */

@ExtendWith(MockitoExtension.class)
public class ServiceUtilsTests {

  @Mock
  private PrescriptionRepository prescriptionRepository;

  @Mock
  private UserRepository userRepository;

  private ServiceUtils serviceUtils;
  private static final String MOCK_UUID = "9101d183-26e6-45b7-a8c4-25f24fdb36fa";
  private Prescription prescription;
  private User user;

  @BeforeEach
  void setup() {
    serviceUtils = new ServiceUtils();

    // mock prescription
    prescription = new Prescription();
    prescription.setId(MOCK_UUID);

    // mock user
    user = new User();
    user.setId(MOCK_UUID);
  }

  @Test
  void findEntityById_Prescription_EntityExists() {
    when(prescriptionRepository.findById(MOCK_UUID)).thenReturn(Optional.of(prescription));
    Prescription result =
        serviceUtils.findEntityById(MOCK_UUID, "prescription", prescriptionRepository);
    assertEquals(prescription, result,
        "findEntityById should return the prescription when it exists");
  }

  // permission

  @Test
  void findEntityById_User_EntityExists() {
    when(userRepository.findById(MOCK_UUID)).thenReturn(Optional.of(user));
    User result = serviceUtils.findEntityById(MOCK_UUID, "user", userRepository);
    assertEquals(user, result, "findEntityById should return the user when it exists");
  }

  @Test
  void findEntityById_Prescription_EntityNotFound() {
    when(prescriptionRepository.findById(MOCK_UUID)).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> serviceUtils.findEntityById(MOCK_UUID, "prescription", prescriptionRepository));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode(),
        "Should throw 404 NOT FOUND when prescription not found");
    assertEquals("Provided prescriptionId does not exist: " + MOCK_UUID, exception.getReason(),
        "Exception message should match expected message");
  }

  @Test
  void findEntityById_User_EntityNotFound() {
    when(userRepository.findById(MOCK_UUID)).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> serviceUtils.findEntityById(MOCK_UUID, "user", userRepository));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode(),
        "Should throw 404 NOT FOUND when user not found");
    assertEquals("Provided userId does not exist: " + MOCK_UUID, exception.getReason(),
        "Exception message should match expected message");
  }
}