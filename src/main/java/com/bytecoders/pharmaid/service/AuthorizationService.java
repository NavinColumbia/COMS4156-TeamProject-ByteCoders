package com.bytecoders.pharmaid.service;

import com.bytecoders.pharmaid.exception.UserNotFoundException;
import com.bytecoders.pharmaid.repository.SharingPermissionRepository;
import com.bytecoders.pharmaid.repository.UserRepository;
import com.bytecoders.pharmaid.repository.model.PermissionType;
import com.bytecoders.pharmaid.repository.model.SharingPermissionStatus;
import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.repository.model.UserType;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service class providing functions to validate view and edit permissions.
 * Handles authorization checks for accessing and modifying user records.
 */
@Service
public class AuthorizationService {

  @Autowired private UserRepository userRepository;

  @Autowired private SharingPermissionRepository sharingPermissionRepository;

  /**
   * Checks if the current user can access another user's records.
   *
   * @param currentUserId The ID of the user requesting access.
   * @param targetUserId The ID of the user whose records are being accessed.
   * @return boolean True if access is allowed, false otherwise.
   * @throws UserNotFoundException if either user is not found.
   */
  public boolean canAccessUserRecords(String currentUserId, String targetUserId) {
    // Get current user
    User currentUser =
        userRepository
            .findById(currentUserId)
            .orElseThrow(() -> new RuntimeException("Current user not found"));

    // First responders can access any records
    if (currentUser.getUserType() == UserType.FIRST_RESPONDER) {
      return true;
    }

    // If it's the same user
    if (currentUserId.equals(targetUserId)) {
      return true;
    }

    User targetUser =
        userRepository
            .findById(targetUserId)
            .orElseThrow(() -> new UserNotFoundException("Target user not found"));

    List<PermissionType> allowedPermissions =
        Arrays.asList(PermissionType.VIEW, PermissionType.EDIT);

    boolean hasUserPermission =
        sharingPermissionRepository.existsByOwnerAndSharedWithUserAndPermissionTypeInAndStatus(
            targetUser, currentUser, allowedPermissions, SharingPermissionStatus.ACCEPTED);

    return hasUserPermission;
  }

  /**
   * Checks if the current user can modify another user's records.
   *
   * @param currentUserId The ID of the user requesting modification.
   * @param targetUserId The ID of the user whose records are being modified.
   * @return boolean True if modification is allowed, false otherwise.
   * @throws UserNotFoundException if either user is not found.
   */
  public boolean canModifyUserRecords(String currentUserId, String targetUserId) {

    if (currentUserId.equals(targetUserId)) {
      return true;
    }

    User currentUser =
        userRepository
            .findById(currentUserId)
            .orElseThrow(() -> new UserNotFoundException("Current user not found"));

    User targetUser =
        userRepository
            .findById(targetUserId)
            .orElseThrow(() -> new UserNotFoundException("Target user not found"));

    PermissionType requiredPermission = PermissionType.EDIT;

    return sharingPermissionRepository.existsByOwnerAndSharedWithUserAndPermissionTypeAndStatus(
        targetUser, currentUser, requiredPermission, SharingPermissionStatus.ACCEPTED);
  }
}
