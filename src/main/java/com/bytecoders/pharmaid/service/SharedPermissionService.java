package com.bytecoders.pharmaid.service;

import com.bytecoders.pharmaid.repository.SharedPermissionRepository;
import com.bytecoders.pharmaid.repository.UserRepository;
import com.bytecoders.pharmaid.repository.model.SharedPermission;
import com.bytecoders.pharmaid.repository.model.User;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Has methods to create, accept, deny, revoke permission share requests. Logic to ensure requester
 * is current user yet be added.
 */
@Slf4j
@Service
public class SharedPermissionService {

  private static final int PENDING = 0;
  private static final int ACCEPT = 1;
  private static final int DENY = 2;

  private static final int VIEW = 0;
  private static final int EDIT = 1;


  @Autowired
  private SharedPermissionRepository sharedPermissionRepository;

  @Autowired
  private UserRepository userRepository;

  /**
   * Creates a new sharing request.
   *
   * @param ownerId        Id of owner.
   * @param permissionType 0 if view, 1 if edit.
   * @return SharedPermission object.
   */
  public SharedPermission createSharingRequest(String requesterId,
      String ownerId,
      Integer permissionType) {

    User
        owner =
        userRepository.findById(ownerId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "User not found with ID: " + ownerId));

    if (ownerId.equals(requesterId)) {
      throw new IllegalArgumentException("Cannot create shared permission with self");
    }

    if (!(permissionType == VIEW || permissionType == EDIT)) {
      throw new IllegalArgumentException("Permission Type should be view or edit");
    }

    User
        requester =
        userRepository.findById(requesterId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "User not found with ID: " + requesterId));

    // Checks if permission already exists and return it if found.
    Optional<SharedPermission>
        existingPermission =
        sharedPermissionRepository.findByOwnerAndRequesterAndPermissionTypeAndStatus(owner,
            requester,
            permissionType,
            PENDING);

    if (existingPermission.isPresent()) {
      return existingPermission.get();
    }

    SharedPermission permission = new SharedPermission();
    permission.setOwner(owner);
    permission.setRequester(requester);
    permission.setPermissionType(permissionType);
    permission.setStatus(PENDING);

    return sharedPermissionRepository.save(permission);
  }

  /**
   * Accepts/Denies a share request.
   *
   * @param ownerId      owner id
   * @param permissionId id of permission to be accepted/denied
   * @param accept       1 if accept, 2 if deny
   * @return SharedPermission object
   */
  public SharedPermission acceptDenySharingRequest(String ownerId,
      String permissionId,
      int accept) {

    SharedPermission
        permission =
        sharedPermissionRepository.findById(permissionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                " Request not found with id : " + permissionId));

    if (!permission.getOwner().getId().equals(ownerId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
          " Not Authorized to accept this request ");
    }

    if (permission.getStatus() != PENDING) {
      throw new IllegalArgumentException("Only Pending Requests can be accepted");
    }

    if (!(accept == ACCEPT || accept == DENY)) {
      throw new IllegalArgumentException(" Pending Requests must be accepted or rejected ");

    }

    permission.setStatus(accept);
    return sharedPermissionRepository.save(permission);
  }

  /**
   * Revokes an already accepted share permission.
   *
   * @param ownerId      owner id
   * @param permissionId share request id
   */
  public void revokeSharingPermission(String ownerId, String permissionId) {

    SharedPermission
        permission =
        sharedPermissionRepository.findById(permissionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                " Request not found with id : " + permissionId));

    if (!permission.getOwner().getId().equals(ownerId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
          " Not Authorized to revoke this request ");
    }

    if (permission.getStatus() != ACCEPT) {
      throw new IllegalArgumentException("Can only revoke already accepted shared permissions");
    }

    sharedPermissionRepository.delete(permission);
  }
}
