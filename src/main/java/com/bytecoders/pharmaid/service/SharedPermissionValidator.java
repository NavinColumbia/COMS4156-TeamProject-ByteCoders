package com.bytecoders.pharmaid.service;

import com.bytecoders.pharmaid.openapi.model.SharePermissionType;
import com.bytecoders.pharmaid.openapi.model.ShareRequestStatus;
import com.bytecoders.pharmaid.openapi.model.UserType;
import com.bytecoders.pharmaid.repository.SharedPermissionRepository;
import com.bytecoders.pharmaid.repository.model.SharedPermission;
import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.util.JwtUtils;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Helper methods to validate all input in SharedPermissionService. */
@Slf4j
@Service
public class SharedPermissionValidator {

  @Autowired
  private SharedPermissionRepository sharedPermissionRepository;

  @Autowired
  private UserService userService;

  @Autowired
  private JwtUtils jwtUtils;

  /**
   * Checks if current user can EDIT another user's records.
   *
   * @param requesterId user requesting modification
   * @param ownerId     user whose records are being modified
   */
  public void validateEditPermission(String requesterId, String ownerId) {
    if (isSameUser(requesterId, ownerId)) {
      return;
    }

    // throw an exception if either requester or owner does not exist
    User requester = userService.getUser(requesterId);
    User owner = userService.getUser(ownerId);

    // check if the requester has EDIT permission
    Optional<SharedPermission> permission =
        sharedPermissionRepository.findByOwnerAndRequesterAndSharePermissionTypeAndStatus(owner,
            requester, SharePermissionType.EDIT, ShareRequestStatus.ACCEPT);

    if (permission.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
          String.format("User %s is not authorized to edit records of user: %s", requesterId,
              ownerId));
    }
  }

  /**
   * Checks if current user can VIEW another user's records.
   *
   * @param requesterId user requesting modification
   * @param ownerId     user whose records are being modified
   */
  public void validateViewPermission(String requesterId, String ownerId) {
    if (isSameUser(requesterId, ownerId)) {
      return;
    }

    // throw an exception if either requester or owner does not exist
    User requester = userService.getUser(requesterId);
    User owner = userService.getUser(ownerId);

    // check if requester has either VIEW or EDIT permission
    List<SharePermissionType> relevantPermissions =
        List.of(SharePermissionType.EDIT, SharePermissionType.VIEW);

    Optional<SharedPermission> permission =
        sharedPermissionRepository.findFirstByOwnerAndRequesterAndSharePermissionTypeInAndStatusIn(
            owner, requester, relevantPermissions, List.of(ShareRequestStatus.ACCEPT));

    if (permission.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
          String.format("User %s is not authorized to view records of user: %s", requesterId,
              ownerId));
    }
  }

  private boolean isSameUser(String requesterId, String ownerId) {
    return requesterId.equals(ownerId);
  }

  /**
   * Checks if the logged in user has permissions to perform a sharing action.
   *
   * @param loggedInUserId the logged in user
   * @param userId         the user needing proper permissions for creating or acting on a
   *                       request
   */
  public void validateLoggedInUser(String loggedInUserId, String userId) {
    if (!loggedInUserId.equals(userId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
          "Cannot act on a shared permission action on behalf of another user");
    }
  }

  /**
   * Validates whether the requester has permission to create a share request.
   *
   * @param userType the {@link UserType} whose permissions are being validated
   */
  public void validateRequesterUserType(UserType userType) {
    if (userType == UserType.PATIENT) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
          "Patients cannot create share requests");
    }
  }

  /**
   * Validates that if the user is a FIRST_RESPONDER, they are not making an EDIT request.
   *
   * @param userType the {@link UserType} whose permissions are being validated
   */
  public void validateFirstResponderRequest(
      UserType userType, SharePermissionType permissionType) {
    if (userType == UserType.FIRST_RESPONDER && permissionType == SharePermissionType.EDIT) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
          "First responders cannot create EDIT requests");
    }
  }

  /**
   * Validates that if the requester and owner are not the same user.
   *
   * @param ownerId     the userId of the owner of the health records
   * @param requesterId the userId of the user requesting permissions
   */
  public void validateDistinctOwnerAndRequester(String ownerId, String requesterId) {
    if (isSameUser(requesterId, ownerId)) {
      throw new IllegalArgumentException("Cannot create shared permission with self");
    }
  }

  /**
   * Validation checks to ensure proper setup for creating a share request.
   *
   * @param owner          the {@link User} who owns the records being shared
   * @param requester      the {@link User} requesting access to the records
   * @param permissionType the type of share permission being requested
   * @return if a permission exists, return permission; else empty return
   */
  public Optional<SharedPermission> validateCreateShareRequestAttributes(
      User owner, User requester, SharePermissionType permissionType) {

    // Validate existing share requests for the provided permissionType
    Optional<SharedPermission> existingPermission =
        validateExistingPermission(owner, requester, permissionType);
    if (existingPermission.isPresent()) {
      return existingPermission;
    }

    // Check for higher-level access if requesting VIEW
    if (permissionType == SharePermissionType.VIEW) {
      return checkForHigherAccessPermission(owner, requester);
    }
    return Optional.empty();
  }

  /**
   * Validates whether an existing share request already exists between the requester and owner.
   *
   * @param owner          the {@link User} who owns the records being shared
   * @param requester      the {@link User} requesting access to the records
   * @param permissionType the type of share permission being requested
   * @return if a permission exists, return permission; else empty return
   */
  public Optional<SharedPermission> validateExistingPermission(
      User owner, User requester, SharePermissionType permissionType) {
    return sharedPermissionRepository.findFirstByOwnerAndRequesterAndSharePermissionTypeInAndStatusIn(
        owner, requester, List.of(permissionType),
        List.of(ShareRequestStatus.PENDING, ShareRequestStatus.ACCEPT));
  }


  /**
   * Checks if the requester already has higher access permission to the owner's health records.
   *
   * @param owner     the {@link User} who owns the records being shared
   * @param requester the {@link User} requesting access
   * @return if a permission exists, return permission; else empty return
   */
  public Optional<SharedPermission> checkForHigherAccessPermission(User owner, User requester) {
    return sharedPermissionRepository.findByOwnerAndRequesterAndSharePermissionTypeAndStatus(
        owner, requester, SharePermissionType.EDIT, ShareRequestStatus.ACCEPT);
  }

  /**
   * Processes all validations to ensure proper setup for a share request.
   *
   * @param owner          the {@link User} who owns the health records
   * @param requester      the {@link User} requesting access to the health records
   * @param permissionType the {@link SharePermissionType} the requester is seeking to obtain
   * @return if a permission exists, return permission; else empty return
   */
  public Optional<SharedPermission> validateCreateShareRequestSetup(
      User owner, User requester, SharePermissionType permissionType) {
    validateLoggedInUser(jwtUtils.getLoggedInUserId(), requester.getId());
    validateRequesterUserType(requester.getUserType());
    validateDistinctOwnerAndRequester(owner.getId(), requester.getId());
    validateFirstResponderRequest(requester.getUserType(), permissionType);
    return validateCreateShareRequestAttributes(owner, requester, permissionType);
  }

  /**
   * Set the permission status from createSharingRequest based on the requester's UserType.
   *
   * @param userType the {@link UserType} of the requester
   */
  public ShareRequestStatus retrieveShareRequestStatus(UserType userType) {
    // first responders automatically have accepted permissions
    if (userType == UserType.FIRST_RESPONDER) {
      return ShareRequestStatus.ACCEPT;
    }
    return ShareRequestStatus.PENDING;
  }

  /**
   * Validates that the shared permission owner matches the user acting on the request.
   *
   * @param sharedPermissionOwnerId the user ID of shared permission owner
   * @param actingUserId            the user ID of user acting on the request
   * @throws ResponseStatusException if the acting user is not authorized to act on the request
   */
  public void validateProperOwner(String sharedPermissionOwnerId, String actingUserId) {
    if (!isSameUser(sharedPermissionOwnerId, actingUserId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
          "Not authorized to act on this request");
    }
  }

  /**
   * Validates that the shared permission can be acted on.
   *
   * @param status the ShareRequestStatus
   * @throws ResponseStatusException if the status is not pending, throw exception
   */
  public void validateProperRequestStatusAction(ShareRequestStatus status) {
    if (status != ShareRequestStatus.PENDING) {
      throw new IllegalArgumentException("Only Pending requests can be acted on");
    }
  }

  /**
   * Validates that the shared permission can be revoked.
   *
   * @param status the ShareRequestStatus
   * @throws ResponseStatusException if the status is not pending, throw exception
   */
  public void validateProperRevokeRequest(ShareRequestStatus status) {
    if (status != ShareRequestStatus.ACCEPT) {
      throw new IllegalArgumentException("Can only revoke already accepted shared permissions");
    }
  }

  /**
   * Validates that the shared permission response is valid. Status response cannot be PENDING
   *
   * @param status the {@link ShareRequestStatus} to propagate to a permission
   * @throws ResponseStatusException if the status is pending, throw exception
   */
  public void validateRequestStatusResponse(ShareRequestStatus status) {
    if (status == ShareRequestStatus.PENDING) {
      throw new IllegalArgumentException("Cannot choose PENDING as a share action response");
    }
  }

  /**
   * Validates the proper setup for an owner to act on a share request.
   *
   * @param permission     the {@link SharedPermission} to act on
   * @param actingOwnerId  the userId acting on the share request
   * @param responseStatus the {@link ShareRequestStatus} to propagate to a permission
   * @throws ResponseStatusException if the status is pending, throw exception
   */
  public void validateShareRequestAction(
      SharedPermission permission, String actingOwnerId, ShareRequestStatus responseStatus) {
    validateLoggedInUser(jwtUtils.getLoggedInUserId(), actingOwnerId);
    validateProperOwner(permission.getOwner().getId(), actingOwnerId);
    validateProperRequestStatusAction(permission.getStatus());
    validateRequestStatusResponse(responseStatus);
  }

  /**
   * Validates the proper setup for an owner to revoke access to another user for their health
   * records.
   *
   * @param permission    the {@link SharedPermission} to act on
   * @param actingOwnerId the userId acting on the share request
   * @throws ResponseStatusException if the status is pending, throw exception
   */
  public void validateRevokeSharePermission(SharedPermission permission, String actingOwnerId) {
    validateLoggedInUser(jwtUtils.getLoggedInUserId(), actingOwnerId);
    validateProperOwner(permission.getOwner().getId(), actingOwnerId);
    validateProperRevokeRequest(permission.getStatus());
  }
}