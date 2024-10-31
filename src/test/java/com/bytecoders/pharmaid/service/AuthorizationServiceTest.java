package com.bytecoders.pharmaid.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.bytecoders.pharmaid.repository.SharingPermissionRepository;
import com.bytecoders.pharmaid.repository.UserRepository;
import com.bytecoders.pharmaid.repository.model.PermissionType;
import com.bytecoders.pharmaid.repository.model.SharingPermissionStatus;
import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.repository.model.UserType;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for the AuthorizationService class.
 * Verifies access and modification permissions for user records.
 */
class AuthorizationServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private SharingPermissionRepository sharingPermissionRepository;
  @InjectMocks private AuthorizationService authorizationService;

  private User currentUser;
  private User targetUser;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    currentUser = new User();
    currentUser.setId("currentUserId");
    targetUser = new User();
    targetUser.setId("targetUserId");
  }

  /**
   * Tests that a first responder has access to user records.
   */
  @Test
  void canAccessUserRecords_firstResponder_shouldReturnTrue() {
    currentUser.setUserType(UserType.FIRST_RESPONDER);
    when(userRepository.findById("currentUserId")).thenReturn(Optional.of(currentUser));

    boolean canAccess = authorizationService.canAccessUserRecords("currentUserId", "targetUserId");

    assertTrue(canAccess);
  }

  /**
   * Tests that a user can access their own records.
   */
  @Test
  void canAccessUserRecords_sameUser_shouldReturnTrue() {
    when(userRepository.findById("currentUserId")).thenReturn(Optional.of(currentUser));

    boolean canAccess = authorizationService.canAccessUserRecords("currentUserId", "currentUserId");

    assertTrue(canAccess);
  }

  /**
   * Tests that a user cannot access records without the correct permission.
   */
  @Test
  void canAccessUserRecords_noPermission_shouldReturnFalse() {
    when(userRepository.findById("currentUserId")).thenReturn(Optional.of(currentUser));
    when(userRepository.findById("targetUserId")).thenReturn(Optional.of(targetUser));
    when(sharingPermissionRepository.existsByOwnerAndSharedWithUserAndPermissionTypeInAndStatus(
        targetUser,
        currentUser,
        List.of(PermissionType.VIEW, PermissionType.EDIT),
        SharingPermissionStatus.ACCEPTED))
        .thenReturn(false);

    boolean canAccess = authorizationService.canAccessUserRecords("currentUserId", "targetUserId");

    assertFalse(canAccess);
  }

  /**
   * Tests that a user can modify records if they have the required permissions.
   */
  @Test
  void canModifyUserRecords_withPermission_shouldReturnTrue() {
    when(userRepository.findById("currentUserId")).thenReturn(Optional.of(currentUser));
    when(userRepository.findById("targetUserId")).thenReturn(Optional.of(targetUser));
    when(sharingPermissionRepository.existsByOwnerAndSharedWithUserAndPermissionTypeAndStatus(
        targetUser, currentUser, PermissionType.EDIT, SharingPermissionStatus.ACCEPTED))
        .thenReturn(true);

    boolean canModify = authorizationService.canModifyUserRecords("currentUserId", "targetUserId");

    assertTrue(canModify);
  }

  /**
   * Tests that a user cannot modify records without the required permissions.
   */
  @Test
  void canModifyUserRecords_noPermission_shouldReturnFalse() {
    when(userRepository.findById("currentUserId")).thenReturn(Optional.of(currentUser));
    when(userRepository.findById("targetUserId")).thenReturn(Optional.of(targetUser));
    when(sharingPermissionRepository.existsByOwnerAndSharedWithUserAndPermissionTypeAndStatus(
        targetUser, currentUser, PermissionType.EDIT, SharingPermissionStatus.ACCEPTED))
        .thenReturn(false);

    boolean canModify = authorizationService.canModifyUserRecords("currentUserId", "targetUserId");

    assertFalse(canModify);
  }
}
