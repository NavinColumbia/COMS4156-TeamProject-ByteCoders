package com.bytecoders.pharmaid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.request.RegisterUserRequest;
import com.bytecoders.pharmaid.service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

/** This class represents a set of unit tests for {@code PharmaidController} class. */
@SpringBootTest
@ContextConfiguration
public class PharmaidContollerTests {
  /**
   * Unit tests setup.
   */
  @BeforeAll
  public static void setup() {
    testApplication = new PharmaidApplication();
    testController = new PharmaidController();
  }

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

  @Mock
  private UserService userService;

  @InjectMocks
  public static PharmaidApplication testApplication;

  @InjectMocks
  public static PharmaidController testController;
}
