package com.bytecoders.pharmaid.service;

import com.bytecoders.pharmaid.openapi.model.SharePermissionType;
import com.bytecoders.pharmaid.openapi.model.ShareRequestStatus;
import com.bytecoders.pharmaid.repository.SharedPermissionRepository;
import com.bytecoders.pharmaid.repository.model.SharedPermission;
import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.util.ServiceUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Functionality to create, accept, deny, revoke permission share requests.
 */
@Slf4j
@Service
public class SharedPermissionService {

  @Autowired
  private SharedPermissionRepository sharedPermissionRepository;

  @Autowired
  private UserService userService;

  @Autowired
  private ServiceUtils serviceUtils;

  @Autowired
  private SharedPermissionValidator permissionValidator;

  /**
   * Creates a new sharing request.
   *
   * @param ownerId        Id of owner.
   * @param permissionType the {@link SharePermissionType} for the share request
   * @return SharedPermission object.
   */
  public SharedPermission createSharingRequest(
      String requesterId, String ownerId, SharePermissionType permissionType) {
    // find owner and requester Users; throw exception if one does not exist
    User owner = userService.getUser(ownerId);
    User requester = userService.getUser(requesterId);

    // ensure valid setup for a share request
    permissionValidator.validateCreateShareRequestSetup(owner, requester, permissionType,
        requesterId);

    // set attributes for permission
    SharedPermission permission = new SharedPermission();
    permission.setOwner(owner);
    permission.setRequester(requester);
    permission.setSharePermissionType(permissionType);
    permission.setStatus(permissionValidator.retrieveShareRequestStatus(requester.getUserType()));
    return sharedPermissionRepository.save(permission);
  }

  /**
   * Decision of a health records owner to act a share request.
   *
   * @param ownerId        owner id
   * @param shareRequestId id of share request to be acted on
   * @param requestStatus  The action taken to a share request
   * @return SharedPermission object
   */
  public SharedPermission shareRequestAction(
      String ownerId, String shareRequestId, ShareRequestStatus requestStatus, String requesterId) {
    SharedPermission permission = getPermission(shareRequestId);

    // Throw IllegalArgumentException if any property is invalid in the share request action
    permissionValidator.validateShareRequestAction(permission, ownerId, requestStatus, requesterId);

    // update permission status
    permission.setStatus(requestStatus);
    return sharedPermissionRepository.save(permission);
  }

  /**
   * Revokes an already accepted share permission.
   *
   * @param ownerId        owner id
   * @param shareRequestId share request id
   */
  public void revokeSharingPermission(String ownerId, String shareRequestId, String requesterId) {
    SharedPermission permission = getPermission(shareRequestId);

    // Throw IllegalArgumentException if any property is invalid when revoking the permission
    permissionValidator.validateRevokeSharePermission(permission, ownerId, requesterId);

    // delete the permission
    sharedPermissionRepository.delete(permission);
  }

  /**
   * Returns a SharedPermission or throws a ResponseStatusException.
   *
   * @param shareRequestId ID pertaining to the share request
   */
  public SharedPermission getPermission(String shareRequestId) {
    return serviceUtils.findEntityById(shareRequestId, "shareRequest",
        sharedPermissionRepository);
  }
}