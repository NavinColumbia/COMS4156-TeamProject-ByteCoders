package com.bytecoders.pharmaid.service;

import com.bytecoders.pharmaid.repository.model.Prescription;
import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.repository.UserRepository;
import com.bytecoders.pharmaid.repository.SharingPermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private SharingPermissionRepository sharingPermissionRepository;

  public boolean canAccessPrescription(Prescription prescription) {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String currentUserId = authentication.getName(); // Assumes that the user's ID is used as the principal

    // If the current user is the owner, allow access
    if (currentUserId.equals(prescription.getUserId())) {
      return true;
    }

    User currentUser = userRepository.findById(currentUserId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    // Check if the user type has universal access
    if (currentUser.getUserType().isCanAccessAllRecords()) {
      return true;
    }

    // Check for sharing permissions
    return sharingPermissionRepository.existsByOwnerIdAndSharedWithUserId(prescription.getUserId(), currentUserId);
  }

  public boolean canModifyPrescription(Prescription prescription) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String currentUserId = authentication.getName();

    // Only the owner or a user with universal access can modify
    User currentUser = userRepository.findById(currentUserId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    return currentUserId.equals(prescription.getUserId()) || currentUser.getUserType().isCanAccessAllRecords();
  }

  // Add more authorization methods as needed
}