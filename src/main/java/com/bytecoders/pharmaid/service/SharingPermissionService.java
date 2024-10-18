package com.bytecoders.pharmaid.service;

import com.bytecoders.pharmaid.repository.SharingPermissionRepository;
import com.bytecoders.pharmaid.repository.UserRepository;
import com.bytecoders.pharmaid.repository.model.SharingPermission;
import com.bytecoders.pharmaid.repository.model.SharingPermissionStatus;
import com.bytecoders.pharmaid.repository.model.SharingRequest;
import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.security.CustomUserDetails;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


@Service
public class SharingPermissionService {

  @Autowired
  private SharingPermissionRepository sharingPermissionRepository;

  @Autowired
  private UserRepository userRepository;


  public String createSharingRequest(String ownerId, SharingRequest request) {

    User owner = userRepository.findById(ownerId)
        .orElseThrow(() -> new RuntimeException("Owner not found"));

    String requesterId = getCurrentUserId();

    User requester = userRepository.findById(requesterId)
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

  public void acceptSharingRequest(String ownerId, String requestId) {
    String currentUserId = getCurrentUserId();

    if (!currentUserId.equals(ownerId)) {
      throw new AccessDeniedException("You are not authorized to accept this request.");
    }

    SharingPermission permission = sharingPermissionRepository.findById(requestId)
        .orElseThrow(() -> new RuntimeException("Sharing request not found"));

    if (!permission.getOwner().getId().equals(ownerId)) {
      throw new AccessDeniedException("You are not authorized to accept this request.");
    }

    permission.setStatus(SharingPermissionStatus.ACCEPTED);
    sharingPermissionRepository.save(permission);
  }

  public void denySharingRequest(String ownerId, String requestId) {
    String currentUserId = getCurrentUserId();

    if (!currentUserId.equals(ownerId)) {
      throw new AccessDeniedException("You are not authorized to deny this request.");
    }

    SharingPermission permission = sharingPermissionRepository.findById(requestId)
        .orElseThrow(() -> new RuntimeException("Sharing request not found"));

    if (!permission.getOwner().getId().equals(ownerId)) {
      throw new AccessDeniedException("You are not authorized to deny this request.");
    }

    permission.setStatus(SharingPermissionStatus.DENIED);
    sharingPermissionRepository.save(permission);
  }

  public void revokeSharingPermission(String ownerId, String requestId) {
    String currentUserId = getCurrentUserId();

    if (!currentUserId.equals(ownerId)) {
      throw new AccessDeniedException("You are not authorized to revoke this permission.");
    }

    SharingPermission permission = sharingPermissionRepository.findById(requestId)
        .orElseThrow(() -> new RuntimeException("Sharing permission not found"));

    if (!permission.getOwner().getId().equals(ownerId)) {
      throw new AccessDeniedException("You are not authorized to revoke this permission.");
    }

    sharingPermissionRepository.delete(permission);
  }


  private String getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      String userId = userDetails.getUser().getId();

      return userId;
    }

    return null;
  }


}
