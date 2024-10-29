// 5. Sharing Permission Service
package com.bytecoders.pharmaid.service;

import com.bytecoders.pharmaid.repository.SharingPermissionRepository;
import com.bytecoders.pharmaid.repository.UserRepository;
import com.bytecoders.pharmaid.repository.model.SharingPermission;
import com.bytecoders.pharmaid.repository.model.SharingPermissionStatus;
import com.bytecoders.pharmaid.repository.model.User;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

/**
 * Service for managing sharing permissions between users.
 */
@Service
public class SharingPermissionService {

  @Autowired
  private SharingPermissionRepository sharingPermissionRepository;

  @Autowired
  private UserRepository userRepository;

  /**
   * Creates a new sharing request.
   *
   * @param ownerId ID of the user who owns the records
   * @param requesterId ID of the user requesting access
   * @param request the sharing request details
   * @return ID of the created sharing permission
   */
  public String createSharingRequest(String ownerId, String requesterId,
      SharingRequest request) {
    User owner = userRepository.findById(ownerId)
        .orElseThrow(() -> new RuntimeException("Owner not found"));

    User requester = userRepository.findById(requesterId)
        .orElseThrow(() -> new RuntimeException("Requester not found"));

    SharingPermission permission = new SharingPermission();
    permission.setOwner(owner);
    permission.setSharedWithUser(requester);
    permission.setPermissionType(request.getPermissionType());
    permission.setStatus(SharingPermissionStatus.PENDING);
    permission.setCreatedAt(new Date());

    SharingPermission savedPermission = sharingPermissionRepository.save(permission);
    return savedPermission.getId();
  }

  /**
   * Accepts a sharing request.
   *
   * @param ownerId ID of the owner
   * @param requestId ID of the sharing request
   */
  public void acceptSharingRequest(String ownerId, String requestId) {
    SharingPermission permission = sharingPermissionRepository.findById(requestId)
        .orElseThrow(() -> new RuntimeException("Sharing request not found"));

    if (!permission.getOwner().getId().equals(ownerId)) {
      throw new AccessDeniedException("Not authorized to accept this request");
    }

    permission.setStatus(SharingPermissionStatus.ACCEPTED);
    sharingPermissionRepository.save(permission);
  }

  /**
   * Denies a sharing request.
   *
   * @param ownerId ID of the owner
   * @param requestId ID of the sharing request
   */
  public void denySharingRequest(String ownerId, String requestId) {
    SharingPermission permission = sharingPermissionRepository.findById(requestId)
        .orElseThrow(() -> new RuntimeException("Sharing request not found"));

    if (!permission.getOwner().getId().equals(ownerId)) {
      throw new AccessDeniedException("Not authorized to deny this request");
    }

    permission.setStatus(SharingPermissionStatus.DENIED);
    sharingPermissionRepository.save(permission);
  }
}
