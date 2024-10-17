package com.bytecoders.pharmaid.service;

import com.bytecoders.pharmaid.repository.PrescriptionRepository;
import com.bytecoders.pharmaid.repository.SharingPermissionRepository;
import com.bytecoders.pharmaid.repository.UserRepository;
import com.bytecoders.pharmaid.repository.model.PermissionType;
import com.bytecoders.pharmaid.repository.model.Prescription;
import com.bytecoders.pharmaid.repository.model.SharingPermission;
import com.bytecoders.pharmaid.repository.model.User;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class SharingPermissionService {
  @Autowired
  private SharingPermissionRepository sharingPermissionRepository;

  @Autowired
  private UserRepository userRepository;

  public String createSharingRequest(String ownerId, String requesterId) {
    User owner = userRepository.findById(ownerId)
        .orElseThrow(() -> new RuntimeException("Owner not found"));
    User requester = userRepository.findById(requesterId)
        .orElseThrow(() -> new RuntimeException("Requester not found"));

    SharingPermission sharingPermission = new SharingPermission();
    sharingPermission.setOwner(owner);
    sharingPermission.setSharedWithUser(requester);
    sharingPermission.setPermissionType(PermissionType.VIEW);
    sharingPermission.setCreatedAt(new Date());

    SharingPermission savedPermission = sharingPermissionRepository.save(sharingPermission);
    return savedPermission.getId();
  }

  public void acceptSharingRequest(String ownerId, String requestId) {
    SharingPermission permission = sharingPermissionRepository.findById(requestId)
        .orElseThrow(() -> new RuntimeException("Sharing request not found"));

    if (!permission.getOwner().getId().equals(ownerId)) {
      throw new RuntimeException("Unauthorized to accept this request");
    }

    permission.setPermissionType(PermissionType.VIEW);
    sharingPermissionRepository.save(permission);
  }

  public void denySharingRequest(String ownerId, String requestId) {
    SharingPermission permission = sharingPermissionRepository.findById(requestId)
        .orElseThrow(() -> new RuntimeException("Sharing request not found"));

    if (!permission.getOwner().getId().equals(ownerId)) {
      throw new RuntimeException("Unauthorized to deny this request");
    }

    sharingPermissionRepository.delete(permission);
  }

  public void revokeSharingPermission(String ownerId, String requestId) {
    SharingPermission permission = sharingPermissionRepository.findById(requestId)
        .orElseThrow(() -> new RuntimeException("Sharing permission not found"));

    if (!permission.getOwner().getId().equals(ownerId)) {
      throw new RuntimeException("Unauthorized to revoke this permission");
    }

    sharingPermissionRepository.delete(permission);
  }
}