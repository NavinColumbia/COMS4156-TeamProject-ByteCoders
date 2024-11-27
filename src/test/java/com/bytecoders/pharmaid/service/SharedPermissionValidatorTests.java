package com.bytecoders.pharmaid.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.bytecoders.pharmaid.openapi.model.SharePermissionType;
import com.bytecoders.pharmaid.openapi.model.ShareRequestStatus;
import com.bytecoders.pharmaid.openapi.model.UserType;
import com.bytecoders.pharmaid.repository.SharedPermissionRepository;
import com.bytecoders.pharmaid.repository.model.SharedPermission;
import com.bytecoders.pharmaid.repository.model.User;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Unit tests for Shared Permission Validator.
 */
@ExtendWith(MockitoExtension.class)
public class SharedPermissionValidatorTests {


  @Mock
  private SharedPermissionRepository sharedPermissionRepository;

  @Mock
  private UserService userService;

  @InjectMocks
  private SharedPermissionValidator permissionValidator;

  private User owner;
  private User requester;
  private SharedPermission permission;
  private String nonExistentUserId;

  @BeforeEach
  void setUp() {
    // owner user of the health records
    owner = new User();
    owner.setId("owner123");

    // user making a request to act on another user's health records
    requester = new User();
    requester.setId("requester456");

    permission = new SharedPermission();
    permission.setOwner(owner);
    permission.setRequester(requester);
    permission.setSharePermissionType(SharePermissionType.VIEW);
    permission.setStatus(ShareRequestStatus.ACCEPT);

    // create a nonExistent user
    nonExistentUserId = "nonExistentUser123";
  }

  @Test
  void validateEditPermission_Success_SameUser() {
    assertDoesNotThrow(
        () -> permissionValidator.validateEditPermission(owner.getId(), owner.getId()),
        "No exception should be thrown when requester and owner are the same user");
  }

  @Test
  void validateEditPermission_Failure_RequesterNotFound() {
    when(userService.getUser(nonExistentUserId)).thenThrow(
        new ResponseStatusException(HttpStatus.NOT_FOUND,
            String.format("Provided userId does not exist: %s", nonExistentUserId)));

    // Verify the exception is thrown and contains the correct details
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> permissionValidator.validateEditPermission(nonExistentUserId, owner.getId()));

    // Assert the exception details
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals(String.format("Provided userId does not exist: %s", nonExistentUserId),
        exception.getReason());
  }

  @Test
  void validateEditPermission_Failure_OwnerNotFound() {
    // requester is found
    when(userService.getUser(requester.getId())).thenReturn(requester);

    when(userService.getUser(nonExistentUserId)).thenThrow(
        new ResponseStatusException(HttpStatus.NOT_FOUND,
            String.format("Provided userId does not exist: %s", nonExistentUserId)));

    // Verify the exception is thrown and contains the correct details
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> permissionValidator.validateEditPermission(requester.getId(), nonExistentUserId));

    // Assert the exception details
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals(String.format("Provided userId does not exist: %s", nonExistentUserId),
        exception.getReason());
  }

  @Test
  void validateEditPermission_Success_HasEditPermission() {
    permission.setSharePermissionType(SharePermissionType.EDIT);

    when(userService.getUser(owner.getId())).thenReturn(owner);
    when(userService.getUser(requester.getId())).thenReturn(requester);
    when(sharedPermissionRepository.findByOwnerAndRequesterAndSharePermissionTypeAndStatus(owner,
        requester, SharePermissionType.EDIT, ShareRequestStatus.ACCEPT)).thenReturn(
        Optional.of(permission));

    assertDoesNotThrow(
        () -> permissionValidator.validateEditPermission(requester.getId(), owner.getId()));
  }

  @Test
  void validateEditPermission_Failure_NoEditPermission() {
    when(userService.getUser(owner.getId())).thenReturn(owner);
    when(userService.getUser(requester.getId())).thenReturn(requester);
    when(sharedPermissionRepository.findByOwnerAndRequesterAndSharePermissionTypeAndStatus(owner,
        requester, SharePermissionType.EDIT, ShareRequestStatus.ACCEPT)).thenReturn(
        Optional.empty());

    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
      permissionValidator.validateEditPermission(requester.getId(), owner.getId());
    });

    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    assertEquals(
        String.format("User %s is not authorized to edit records of user: %s", requester.getId(),
            owner.getId()), exception.getReason());
  }

  @Test
  void validateViewPermission_Success_SameUser() {
    assertDoesNotThrow(
        () -> permissionValidator.validateViewPermission(owner.getId(), owner.getId()),
        "No exception should be thrown when requester and owner are the same user");
  }

  @Test
  void validateViewPermission_Failure_RequesterNotFound() {
    // Requester does not exist
    when(userService.getUser(nonExistentUserId)).thenThrow(
        new ResponseStatusException(HttpStatus.NOT_FOUND,
            String.format("Provided userId does not exist: %s", nonExistentUserId)));

    // Verify the exception is thrown and contains the correct details
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> permissionValidator.validateViewPermission(nonExistentUserId, owner.getId()));

    // Assert the exception details
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals(String.format("Provided userId does not exist: %s", nonExistentUserId),
        exception.getReason());
  }

  @Test
  void validateViewPermission_Failure_OwnerNotFound() {
    // Requester is found
    when(userService.getUser(requester.getId())).thenReturn(requester);

    // Owner does not exist
    when(userService.getUser(nonExistentUserId)).thenThrow(
        new ResponseStatusException(HttpStatus.NOT_FOUND,
            String.format("Provided userId does not exist: %s", nonExistentUserId)));

    // Verify the exception is thrown and contains the correct details
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> permissionValidator.validateViewPermission(requester.getId(), nonExistentUserId));

    // Assert the exception details
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals(String.format("Provided userId does not exist: %s", nonExistentUserId),
        exception.getReason());
  }

  @Test
  void validateViewPermission_Success_HasViewPermission() {
    // ensure requester has VIEW permission
    permission.setSharePermissionType(SharePermissionType.VIEW);

    when(userService.getUser(owner.getId())).thenReturn(owner);
    when(userService.getUser(requester.getId())).thenReturn(requester);
    when(
        sharedPermissionRepository.findFirstByOwnerAndRequesterAndSharePermissionTypeInAndStatusIn(
            eq(owner), eq(requester),
            eq(List.of(SharePermissionType.EDIT, SharePermissionType.VIEW)),
            eq(List.of(ShareRequestStatus.ACCEPT)))).thenReturn(Optional.of(permission));

    assertDoesNotThrow(
        () -> permissionValidator.validateViewPermission(requester.getId(), owner.getId()),
        "No exception should be thrown when requester and owner are the same user");
  }

  @Test
  void validateViewPermission_Success_HasEditPermission() {
    // user has EDIT permission and should be able to pass validateViewPermission
    permission.setSharePermissionType(SharePermissionType.EDIT);

    when(userService.getUser(owner.getId())).thenReturn(owner);
    when(userService.getUser(requester.getId())).thenReturn(requester);
    when(
        sharedPermissionRepository.findFirstByOwnerAndRequesterAndSharePermissionTypeInAndStatusIn(
            eq(owner), eq(requester),
            eq(List.of(SharePermissionType.EDIT, SharePermissionType.VIEW)),
            eq(List.of(ShareRequestStatus.ACCEPT)))).thenReturn(Optional.of(permission));

    assertDoesNotThrow(
        () -> permissionValidator.validateViewPermission(requester.getId(), owner.getId()),
        "No exception should be thrown when requester and owner are the same user");
  }

  @Test
  void validateViewPermission_Failure_NoViewPermission() {
    when(userService.getUser(owner.getId())).thenReturn(owner);
    when(userService.getUser(requester.getId())).thenReturn(requester);
    when(
        sharedPermissionRepository.findFirstByOwnerAndRequesterAndSharePermissionTypeInAndStatusIn(
            eq(owner), eq(requester),
            eq(List.of(SharePermissionType.EDIT, SharePermissionType.VIEW)),
            eq(List.of(ShareRequestStatus.ACCEPT)))).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
      permissionValidator.validateViewPermission(requester.getId(), owner.getId());
    });

    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    assertEquals(
        String.format("User %s is not authorized to view records of user: %s", requester.getId(),
            owner.getId()), exception.getReason());
  }

  @Test
  void validateLoggedInUser_Success() {
    // test void method to ensure no exceptions are thrown
    permissionValidator.validateLoggedInUser(owner.getId(), owner.getId());
  }

  @Test
  void validateLoggedInUser_Failure() {
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> permissionValidator.validateLoggedInUser("wrongId123", owner.getId()));

    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    assertEquals("Cannot act on a shared permission action on behalf of another user",
        exception.getReason());
  }

  @Test
  void validateRequesterUserType_Success_HealthcareProvider() {
    // No exception thrown for HEALTHCARE_PROVIDER
    assertDoesNotThrow(
        () -> permissionValidator.validateRequesterUserType(UserType.HEALTHCARE_PROVIDER));
  }

  @Test
  void validateRequesterUserType_Success_FirstResponder() {
    // No exception thrown for FIRST_RESPONDER
    assertDoesNotThrow(
        () -> permissionValidator.validateRequesterUserType(UserType.FIRST_RESPONDER));
  }

  @Test
  void validateRequesterUserType_Failure_Patient() {
    // Exception thrown for PATIENT
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> permissionValidator.validateRequesterUserType(UserType.PATIENT));
    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    assertEquals("Patients cannot create share requests", exception.getReason());
  }

  @Test
  void validateFirstResponderRequest_Success_FirstResponder_View() {
    // No exception thrown for FIRST_RESPONDER requesting VIEW
    assertDoesNotThrow(
        () -> permissionValidator.validateFirstResponderRequest(UserType.FIRST_RESPONDER,
            SharePermissionType.VIEW));
  }

  @Test
  void validateFirstResponderRequest_Success_HealthcareProvider_Edit() {
    // No exception thrown for HEALTHCARE_PROVIDER requesting EDIT
    assertDoesNotThrow(
        () -> permissionValidator.validateFirstResponderRequest(UserType.HEALTHCARE_PROVIDER,
            SharePermissionType.EDIT));
  }

  @Test
  void validateFirstResponderRequest_Success_HealthcareProvider_View() {
    // No exception thrown for HEALTHCARE_PROVIDER requesting VIEW
    assertDoesNotThrow(
        () -> permissionValidator.validateFirstResponderRequest(UserType.HEALTHCARE_PROVIDER,
            SharePermissionType.VIEW));
  }

  @Test
  void validateFirstResponderRequest_Failure_FirstResponder_Edit() {
    // Exception thrown for FIRST_RESPONDER requesting EDIT
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> permissionValidator.validateFirstResponderRequest(UserType.FIRST_RESPONDER,
            SharePermissionType.EDIT));
    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    assertEquals("First responders cannot create EDIT requests", exception.getReason());
  }

  @Test
  void validateDistinctOwnerAndRequester_Success_DifferentUsers() {
    // No exception thrown for distinct users
    assertDoesNotThrow(() -> permissionValidator.validateDistinctOwnerAndRequester(owner.getId(),
        requester.getId()));
  }

  @Test
  void validateDistinctOwnerAndRequester_Failure_SameUser() {
    // Exception thrown for same user
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> permissionValidator.validateDistinctOwnerAndRequester(owner.getId(),
            owner.getId()));
    assertEquals("Cannot create shared permission with self", exception.getMessage());
  }

  @Test
  void validateExistingPermission_Success_NoExistingPermission() {
    // No existing permissions
    when(
        sharedPermissionRepository.findFirstByOwnerAndRequesterAndSharePermissionTypeInAndStatusIn(
            eq(owner), eq(requester), any(), any())).thenReturn(Optional.empty());

    assertDoesNotThrow(() -> permissionValidator.validateExistingPermission(owner, requester,
        SharePermissionType.VIEW));
  }

  @Test
  void validateExistingPermission_Failure_PendingPermission() {
    // PENDING request
    permission.setStatus(ShareRequestStatus.PENDING);

    when(
        sharedPermissionRepository.findFirstByOwnerAndRequesterAndSharePermissionTypeInAndStatusIn(
            eq(owner), eq(requester), any(), any())).thenReturn(Optional.of(permission));

    Optional<SharedPermission> result =
        permissionValidator.validateExistingPermission(owner, requester,
            SharePermissionType.VIEW);

    assertTrue(result.isPresent(), "Expected existing PENDING permission");
    assertEquals(permission, result.get(), "Returned permission should match the existing one");
  }

  @Test
  void validateExistingPermission_Failure_AcceptPermission() {
    // ACCEPT permission
    permission.setStatus(ShareRequestStatus.ACCEPT);

    when(
        sharedPermissionRepository.findFirstByOwnerAndRequesterAndSharePermissionTypeInAndStatusIn(
            eq(owner), eq(requester), any(), any())).thenReturn(Optional.of(permission));

    Optional<SharedPermission> result =
        permissionValidator.validateExistingPermission(owner, requester,
            SharePermissionType.VIEW);

    assertTrue(result.isPresent(), "Expected existing ACCEPT permission");
    assertEquals(permission, result.get(), "Returned permission should match the existing one");
  }

  @Test
  void checkForHigherAccessPermission_Success_NoEditAccess() {
    // EDIT access does not exist
    when(sharedPermissionRepository.findByOwnerAndRequesterAndSharePermissionTypeAndStatus(
        eq(owner), eq(requester), eq(SharePermissionType.EDIT),
        eq(ShareRequestStatus.ACCEPT))).thenReturn(Optional.empty());

    assertDoesNotThrow(
        () -> permissionValidator.checkForHigherAccessPermission(owner, requester));
  }

  @Test
  void checkForHigherAccessPermission_Success_EditAccessExists() {
    permission.setStatus(ShareRequestStatus.ACCEPT);

    // EDIT access exists
    when(sharedPermissionRepository.findByOwnerAndRequesterAndSharePermissionTypeAndStatus(
        eq(owner), eq(requester), eq(SharePermissionType.EDIT),
        eq(ShareRequestStatus.ACCEPT))).thenReturn(Optional.of(permission));

    Optional<SharedPermission> result =
        permissionValidator.checkForHigherAccessPermission(owner, requester);

    assertTrue(result.isPresent(), "Expected existing EDIT access permission");
    assertEquals(permission, result.get(), "Returned permission should match the existing one");
  }

  @Test
  void retrieveShareRequestStatus_Success_FirstResponder() {
    ShareRequestStatus status =
        permissionValidator.retrieveShareRequestStatus(UserType.FIRST_RESPONDER);
    assertEquals(ShareRequestStatus.ACCEPT, status);
  }

  @Test
  void retrieveShareRequestStatus_Success_NonFirstResponder() {
    ShareRequestStatus status =
        permissionValidator.retrieveShareRequestStatus(UserType.HEALTHCARE_PROVIDER);
    assertEquals(ShareRequestStatus.PENDING, status);
  }

  @Test
  void validateProperOwner_Success() {
    assertDoesNotThrow(
        () -> permissionValidator.validateProperOwner(owner.getId(), owner.getId()));
  }

  @Test
  void validateProperOwner_Failure_NotAuthorized() {
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> permissionValidator.validateProperOwner(owner.getId(), requester.getId()));

    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    assertEquals("Not authorized to act on this request", exception.getReason());
  }

  @Test
  void validateProperRequestStatusAction_Success_Pending() {
    assertDoesNotThrow(
        () -> permissionValidator.validateProperRequestStatusAction(ShareRequestStatus.PENDING));
  }

  @Test
  void validateProperRequestStatusAction_Failure_Accept() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> permissionValidator.validateProperRequestStatusAction(ShareRequestStatus.ACCEPT));

    assertEquals("Only Pending requests can be acted on", exception.getMessage());
  }

  @Test
  void validateProperRequestStatusAction_Failure_Deny() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> permissionValidator.validateProperRequestStatusAction(ShareRequestStatus.DENY));

    assertEquals("Only Pending requests can be acted on", exception.getMessage());
  }

  @Test
  void validateProperRevokeRequest_Success_Accept() {
    assertDoesNotThrow(
        () -> permissionValidator.validateProperRevokeRequest(ShareRequestStatus.ACCEPT));
  }

  @Test
  void validateProperRevokeRequest_Failure_Pending() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> permissionValidator.validateProperRevokeRequest(ShareRequestStatus.PENDING));

    assertEquals("Can only revoke already accepted shared permissions", exception.getMessage());
  }

  @Test
  void validateProperRevokeRequest_Failure_Deny() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> permissionValidator.validateProperRevokeRequest(ShareRequestStatus.DENY));

    assertEquals("Can only revoke already accepted shared permissions", exception.getMessage());
  }

  @Test
  void validateRequestStatusResponse_Success_Accept() {
    assertDoesNotThrow(
        () -> permissionValidator.validateRequestStatusResponse(ShareRequestStatus.ACCEPT));
  }

  @Test
  void validateRequestStatusResponse_Success_Deny() {
    assertDoesNotThrow(
        () -> permissionValidator.validateRequestStatusResponse(ShareRequestStatus.DENY));
  }

  @Test
  void validateRequestStatusResponse_Failure_Pending() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> permissionValidator.validateRequestStatusResponse(ShareRequestStatus.PENDING));

    assertEquals("Cannot choose PENDING as a share action response", exception.getMessage());
  }
}