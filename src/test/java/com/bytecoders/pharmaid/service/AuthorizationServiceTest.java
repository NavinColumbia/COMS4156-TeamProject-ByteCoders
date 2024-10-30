package com.bytecoders.pharmaid.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.bytecoders.pharmaid.exception.*;
import com.bytecoders.pharmaid.repository.SharingPermissionRepository;
import com.bytecoders.pharmaid.repository.UserRepository;
import com.bytecoders.pharmaid.repository.model.PermissionType;
import com.bytecoders.pharmaid.repository.model.SharingPermissionStatus;
import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.repository.model.UserType;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for the AuthorizationService class.
 * Verifies authorization logic for accessing and modifying user records.
 */
@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {
  @Mock private UserRepository userRepository;
  @Mock private SharingPermissionRepository sharingPermissionRepository;
  @InjectMocks private AuthorizationService authorizationService;

  private User owner;
  private User requester;
  private User firstResponder;

  @BeforeEach
  void setUp() {
    owner = new User();
    owner.setId("ownerId");
    owner.setUserType(UserType.PATIENT);

    requester = new User();
    requester.setId("requesterId");
    requester.setUserType(UserType.PATIENT);

    firstResponder = new User();
    firstResponder.setId("responderId");
    firstResponder.setUserType(UserType.FIRST_RESPONDER);
  }

  @Test
  void canAccessUserRecords_FirstResponder_ReturnsTrue() {
    when(userRepository.findById("responderId"))
        .thenReturn(Optional.of(firstResponder));

    boolean result = authorizationService
        .canAccessUserRecords("responderId", "targetId");

    assertTrue(result);
    verify(userRepository).findById("responderId");
  }

  @Test
  void canAccessUserRecords_UserNotFound_ThrowsNotFoundException() {
    when(userRepository.findById("requesterId"))
        .thenReturn(Optional.empty());

    UserNotFoundException exception = assertThrows(
        UserNotFoundException.class,
        () -> authorizationService.canAccessUserRecords("requesterId", "ownerId")
    );

    assertEquals("Current user not found with ID: requesterId", exception.getMessage());
    verify(userRepository).findById("requesterId");
  }

  @Test
  void canAccessUserRecords_TargetUserNotFound_ThrowsNotFoundException() {
    when(userRepository.findById("requesterId"))
        .thenReturn(Optional.of(requester));
    when(userRepository.findById("ownerId"))
        .thenReturn(Optional.empty());

    UserNotFoundException exception = assertThrows(
        UserNotFoundException.class,
        () -> authorizationService.canAccessUserRecords("requesterId", "ownerId")
    );

    assertEquals("Target user not found with ID: ownerId", exception.getMessage());
    verify(userRepository).findById("requesterId");
    verify(userRepository).findById("ownerId");
  }

  @Test
  void canAccessUserRecords_WithPermission_ReturnsTrue() {
    when(userRepository.findById("requesterId"))
        .thenReturn(Optional.of(requester));
    when(userRepository.findById("ownerId"))
        .thenReturn(Optional.of(owner));
    when(sharingPermissionRepository
        .existsByOwnerAndSharedWithUserAndPermissionTypeInAndStatus(
            eq(owner),
            eq(requester),
            argThat(list -> list.containsAll(Arrays.asList(
                PermissionType.VIEW, PermissionType.EDIT))),
            eq(SharingPermissionStatus.ACCEPTED)))
        .thenReturn(true);

    boolean result = authorizationService
        .canAccessUserRecords("requesterId", "ownerId");

    assertTrue(result);
    verify(userRepository).findById("requesterId");
    verify(userRepository).findById("ownerId");
    verify(sharingPermissionRepository)
        .existsByOwnerAndSharedWithUserAndPermissionTypeInAndStatus(
            any(), any(), any(), any());
  }

  @Test
  void canModifyUserRecords_WithoutPermission_ReturnsFalse() {
    when(userRepository.findById("requesterId"))
        .thenReturn(Optional.of(requester));
    when(userRepository.findById("ownerId"))
        .thenReturn(Optional.of(owner));
    when(sharingPermissionRepository
        .existsByOwnerAndSharedWithUserAndPermissionTypeAndStatus(
            owner, requester, PermissionType.EDIT,
            SharingPermissionStatus.ACCEPTED))
        .thenReturn(false);

    boolean result = authorizationService
        .canModifyUserRecords("requesterId", "ownerId");

    assertFalse(result);
    verify(userRepository).findById("requesterId");
    verify(userRepository).findById("ownerId");
  }
}