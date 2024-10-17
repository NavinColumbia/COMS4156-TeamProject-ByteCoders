package com.bytecoders.pharmaid.service;

import com.bytecoders.pharmaid.repository.PrescriptionRepository;
import com.bytecoders.pharmaid.repository.SharingPermissionRepository;
import com.bytecoders.pharmaid.repository.UserRepository;
import com.bytecoders.pharmaid.repository.model.Prescription;
import com.bytecoders.pharmaid.repository.model.SharingPermission;
import com.bytecoders.pharmaid.repository.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private SharingPermissionRepository sharingPermissionRepository;

  public boolean canAccessPrescriptions(String requesterId, String ownerId) {
    if (requesterId.equals(ownerId)) {
      return true;
    }

    User requester = userRepository.findById(requesterId)
        .orElseThrow(() -> new RuntimeException("Requester not found"));

    if (requester.getUserType().isCanAccessAllRecords()) {
      return true;
    }

    User owner = userRepository.findById(ownerId)
        .orElseThrow(() -> new RuntimeException("Owner not found"));

    Optional<SharingPermission> permission = sharingPermissionRepository.findByOwnerAndSharedWithUser(owner, requester);
    if (permission.isPresent()) {
      return true;
    }

    if (requester.getOrganization() != null) {
      Optional<SharingPermission> orgPermission = sharingPermissionRepository.findByOwnerAndSharedWithOrganization(owner, requester.getOrganization());
      return orgPermission.isPresent();
    }

    return false;
  }
}