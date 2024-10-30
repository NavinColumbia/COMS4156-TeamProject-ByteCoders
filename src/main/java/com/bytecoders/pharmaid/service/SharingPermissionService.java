package com.bytecoders.pharmaid.service;

import com.bytecoders.pharmaid.exception.NotAuthorizedException;
import com.bytecoders.pharmaid.exception.ResourceNotFoundException;
import com.bytecoders.pharmaid.exception.UserNotFoundException;
import com.bytecoders.pharmaid.repository.SharingPermissionRepository;
import com.bytecoders.pharmaid.repository.UserRepository;
import com.bytecoders.pharmaid.repository.model.SharingPermission;
import com.bytecoders.pharmaid.repository.model.SharingPermissionStatus;
import com.bytecoders.pharmaid.repository.model.SharingRequest;
import com.bytecoders.pharmaid.repository.model.User;
import java.util.Date;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Service class providing helper functions to manage sharing requests and permissions.
 * Handles creation, acceptance, denial, and revocation of sharing requests between users.
 */
@Slf4j
@Service
public class SharingPermissionService {

  @Autowired
  private SharingPermissionRepository sharingPermissionRepository;

  @Autowired
  private UserRepository userRepository;


  /**
   * Creates a new sharing request between users.
   *
   * @param ownerId The ID of the user who owns the records
   * @param request The sharing request details
   * @return The created sharing permission
   * @throws UserNotFoundException if either owner or requester is not found
   * @throws IllegalArgumentException if invalid request parameters
   */
  public SharingPermission createSharingRequest(String ownerId, SharingRequest request) {
    User owner = userRepository.findById(ownerId)
        .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + ownerId));

    String requesterId = getCurrentUserId();

    if (ownerId.equals(requesterId)) {
      throw new IllegalArgumentException("Cannot create sharing permission with yourself");
    }

    User requester = userRepository.findById(requesterId)
        .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + requesterId));

    // Check if permission already exists
    boolean permissionExists = sharingPermissionRepository
        .existsByOwnerAndSharedWithUserAndPermissionTypeAndStatus(
            owner,
            requester,
            request.getPermissionType(),
            SharingPermissionStatus.PENDING);

    if (permissionExists) {
      throw new IllegalArgumentException("A pending sharing request already exists");
    }

    SharingPermission permission = new SharingPermission();
    permission.setOwner(owner);
    permission.setSharedWithUser(requester);
    permission.setPermissionType(request.getPermissionType());
    permission.setStatus(SharingPermissionStatus.PENDING);
    permission.setCreatedAt(new Date());

    return sharingPermissionRepository.save(permission);
  }


  /**
   * Accepts a pending sharing request.
   *
   * @param ownerId The ID of the user who owns the records
   * @param permissionId The ID of the permission to accept
   * @return The updated sharing permission
   * @throws ResourceNotFoundException if permission not found
   * @throws NotAuthorizedException if user not authorized
   * @throws IllegalArgumentException if permission not in correct state
   */
  public SharingPermission acceptSharingRequest(String ownerId, String permissionId) {
    String currentUserId = getCurrentUserId();
    if (!currentUserId.equals(ownerId)) {
      throw new NotAuthorizedException("Not authorized to accept this request");
    }

    SharingPermission permission = sharingPermissionRepository.findById(permissionId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Sharing request not found with ID: " + permissionId));

    if (!permission.getOwner().getId().equals(ownerId)) {
      throw new NotAuthorizedException("Not authorized to accept this request");
    }

    if (permission.getStatus() != SharingPermissionStatus.PENDING) {
      throw new IllegalArgumentException("Can only accept pending requests");
    }

    permission.setStatus(SharingPermissionStatus.ACCEPTED);
    return sharingPermissionRepository.save(permission);
  }


  /**
   * Denies a pending sharing request.
   *
   * @param ownerId The ID of the user who owns the records
   * @param permissionId The ID of the permission to deny
   * @return The updated sharing permission
   * @throws ResourceNotFoundException if permission not found
   * @throws NotAuthorizedException if user not authorized
   * @throws IllegalArgumentException if permission not in correct state
   */
  public SharingPermission denySharingRequest(String ownerId, String permissionId) {
    String currentUserId = getCurrentUserId();
    if (!currentUserId.equals(ownerId)) {
      throw new NotAuthorizedException("Not authorized to deny this request");
    }

    SharingPermission permission = sharingPermissionRepository.findById(permissionId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Sharing request not found with ID: " + permissionId));

    if (!permission.getOwner().getId().equals(ownerId)) {
      throw new NotAuthorizedException("Not authorized to deny this request");
    }

    if (permission.getStatus() != SharingPermissionStatus.PENDING) {
      throw new IllegalArgumentException("Can only deny pending requests");
    }

    permission.setStatus(SharingPermissionStatus.DENIED);
    return sharingPermissionRepository.save(permission);
  }



  /**
   * Revokes an existing sharing permission.
   *
   * @param ownerId The ID of the user who owns the records.
   * @param permissionId The ID of the sharing permission to revoke.
   * @throws AccessDeniedException if the current user is not authorized.
   * @throws RuntimeException if the sharing permission is not found.
   */
  public void revokeSharingPermission(String ownerId, String permissionId) {
    String currentUserId = getCurrentUserId();
    if (!currentUserId.equals(ownerId)) {
      throw new NotAuthorizedException("Not authorized to revoke this permission");
    }

    SharingPermission permission = sharingPermissionRepository.findById(permissionId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Sharing permission not found with ID: " + permissionId));

    if (!permission.getOwner().getId().equals(ownerId)) {
      throw new NotAuthorizedException("Not authorized to revoke this permission");
    }

    if (permission.getStatus() != SharingPermissionStatus.ACCEPTED) {
      throw new IllegalArgumentException("Can only revoke accepted permissions");
    }

    sharingPermissionRepository.delete(permission);
  }

  /**
   * Retrieves the ID of the currently authenticated user.
   *
   * @return The ID of the current user.
   * @throws RuntimeException if no user is authenticated.
   */
  public String getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    log.debug(
        "Authentication principal type: {}",
        authentication != null ? authentication.getPrincipal().getClass() : "null");

    if (authentication != null && authentication.getPrincipal() instanceof User) {
      User user = (User) authentication.getPrincipal();
      return user.getId();
    }
    throw new RuntimeException("User not authenticated");
  }
}
