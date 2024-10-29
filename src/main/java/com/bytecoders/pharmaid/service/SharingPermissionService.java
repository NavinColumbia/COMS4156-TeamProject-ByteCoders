package com.bytecoders.pharmaid.service;

import com.bytecoders.pharmaid.repository.SharingPermissionRepository;
import com.bytecoders.pharmaid.repository.UserRepository;
import com.bytecoders.pharmaid.repository.model.SharingPermission;
import com.bytecoders.pharmaid.repository.model.SharingPermissionStatus;
import com.bytecoders.pharmaid.repository.model.SharingRequest;
import com.bytecoders.pharmaid.repository.model.User;
import java.util.Date;
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
   * @param ownerId The ID of the user who owns the records.
   * @param request The sharing request details.
   * @return String The ID of the created sharing permission.
   * @throws RuntimeException if either the owner or requester is not found.
   */
  public String createSharingRequest(String ownerId, SharingRequest request) {

    User owner =
        userRepository.findById(ownerId).orElseThrow(() -> new RuntimeException("Owner not found"));

    String requesterId = getCurrentUserId();

    User requester =
        userRepository
            .findById(requesterId)
            .orElseThrow(() -> new RuntimeException("Requester not found"));

    SharingPermission sharingPermission = new SharingPermission();
    sharingPermission.setOwner(owner);
    sharingPermission.setSharedWithUser(requester);
    sharingPermission.setPermissionType(request.getPermissionType());
    sharingPermission.setStatus(SharingPermissionStatus.PENDING);
    sharingPermission.setCreatedAt(new Date());

    SharingPermission savedPermission = sharingPermissionRepository.save(sharingPermission);
    return savedPermission.getId();
  }

  /**
   * Accepts a pending sharing request.
   *
   * @param ownerId The ID of the user who owns the records.
   * @param requestId The ID of the sharing request to accept.
   * @throws AccessDeniedException if the current user is not authorized.
   * @throws RuntimeException if the sharing request is not found.
   */
  public void acceptSharingRequest(String ownerId, String requestId) {
    String currentUserId = getCurrentUserId();

    if (!currentUserId.equals(ownerId)) {
      throw new AccessDeniedException("You are not authorized to accept this request.");
    }

    SharingPermission permission =
        sharingPermissionRepository
            .findById(requestId)
            .orElseThrow(() -> new RuntimeException("Sharing request not found"));

    if (!permission.getOwner().getId().equals(ownerId)) {
      throw new AccessDeniedException("You are not authorized to accept this request.");
    }

    permission.setStatus(SharingPermissionStatus.ACCEPTED);
    sharingPermissionRepository.save(permission);
  }

  /**
   * Denies a pending sharing request.
   *
   * @param ownerId The ID of the user who owns the records.
   * @param requestId The ID of the sharing request to deny.
   * @throws AccessDeniedException if the current user is not authorized.
   * @throws RuntimeException if the sharing request is not found.
   */
  public void denySharingRequest(String ownerId, String requestId) {
    String currentUserId = getCurrentUserId();

    if (!currentUserId.equals(ownerId)) {
      throw new AccessDeniedException("You are not authorized to deny this request.");
    }

    SharingPermission permission =
        sharingPermissionRepository
            .findById(requestId)
            .orElseThrow(() -> new RuntimeException("Sharing request not found"));

    if (!permission.getOwner().getId().equals(ownerId)) {
      throw new AccessDeniedException("You are not authorized to deny this request.");
    }

    permission.setStatus(SharingPermissionStatus.DENIED);
    sharingPermissionRepository.save(permission);
  }

  /**
   * Revokes an existing sharing permission.
   *
   * @param ownerId The ID of the user who owns the records.
   * @param requestId The ID of the sharing permission to revoke.
   * @throws AccessDeniedException if the current user is not authorized.
   * @throws RuntimeException if the sharing permission is not found.
   */
  public void revokeSharingPermission(String ownerId, String requestId) {
    String currentUserId = getCurrentUserId();

    if (!currentUserId.equals(ownerId)) {
      throw new AccessDeniedException("You are not authorized to revoke this permission.");
    }

    SharingPermission permission =
        sharingPermissionRepository
            .findById(requestId)
            .orElseThrow(() -> new RuntimeException("Sharing permission not found"));

    if (!permission.getOwner().getId().equals(ownerId)) {
      throw new AccessDeniedException("You are not authorized to revoke this permission.");
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
