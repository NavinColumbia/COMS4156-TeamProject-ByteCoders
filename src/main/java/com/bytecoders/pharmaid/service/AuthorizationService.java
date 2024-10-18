package com.bytecoders.pharmaid.service;

import com.bytecoders.pharmaid.repository.model.PermissionType;
import com.bytecoders.pharmaid.repository.model.Prescription;
import com.bytecoders.pharmaid.repository.model.SharingPermissionStatus;
import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.repository.UserRepository;
import com.bytecoders.pharmaid.repository.SharingPermissionRepository;
import com.bytecoders.pharmaid.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class AuthorizationService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private SharingPermissionRepository sharingPermissionRepository;

  public boolean canAccessUserRecords(String currentUserId, String targetUserId) {
    // Users can always access their own records
    if (currentUserId.equals(targetUserId)) {
      return true;
    }

    User currentUser = userRepository.findById(currentUserId)
        .orElseThrow(() -> new RuntimeException("Current user not found"));

    User targetUser = userRepository.findById(targetUserId)
        .orElseThrow(() -> new RuntimeException("Target user not found"));

    List<PermissionType> allowedPermissions = Arrays.asList(PermissionType.VIEW, PermissionType.EDIT);

    // Check user-specific permissions
    boolean hasUserPermission = sharingPermissionRepository.existsByOwnerAndSharedWithUserAndPermissionTypeInAndStatus(
        targetUser, currentUser, allowedPermissions, SharingPermissionStatus.ACCEPTED);

    if (hasUserPermission) {
      return true;
    }

    // Check organization-based permissions
    if (currentUser.getOrganization() != null) {
      boolean hasOrgPermission = sharingPermissionRepository.existsByOwnerAndSharedWithOrganizationAndPermissionTypeInAndStatus(
          targetUser, currentUser.getOrganization(), allowedPermissions, SharingPermissionStatus.ACCEPTED);

      if (hasOrgPermission) {
        return true;
      }
    }

    // No permissions found
    return false;
  }

  public boolean canModifyUserRecords(String currentUserId, String targetUserId) {
    // Users can always modify their own records
    if (currentUserId.equals(targetUserId)) {
      return true;
    }

    User currentUser = userRepository.findById(currentUserId)
        .orElseThrow(() -> new RuntimeException("Current user not found"));

    User targetUser = userRepository.findById(targetUserId)
        .orElseThrow(() -> new RuntimeException("Target user not found"));

    PermissionType requiredPermission = PermissionType.EDIT;

    // Check user-specific permissions
    boolean hasUserPermission = sharingPermissionRepository.existsByOwnerAndSharedWithUserAndPermissionTypeAndStatus(
        targetUser, currentUser, requiredPermission, SharingPermissionStatus.ACCEPTED);

    if (hasUserPermission) {
      return true;
    }

    // Check organization-based permissions
    if (currentUser.getOrganization() != null) {
      boolean hasOrgPermission = sharingPermissionRepository.existsByOwnerAndSharedWithOrganizationAndPermissionTypeAndStatus(
          targetUser, currentUser.getOrganization(), requiredPermission, SharingPermissionStatus.ACCEPTED);

      if (hasOrgPermission) {
        return true;
      }
    }

    // No permissions found
    return false;
  }

  public boolean canAccessPrescription(Prescription prescription) {
    String currentUserId = getCurrentUserId();

    // The owner can always access their prescriptions
    if (currentUserId.equals(prescription.getUser().getId())) {
      return true;
    }

    return canAccessUserRecords(currentUserId, prescription.getUser().getId());
  }

  public boolean canModifyPrescription(Prescription prescription) {
    String currentUserId = getCurrentUserId();

    // The owner can always modify their prescriptions
    if (currentUserId.equals(prescription.getUser().getId())) {
      return true;
    }

    return canModifyUserRecords(currentUserId, prescription.getUser().getId());
  }

  private String getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      return userDetails.getUser().getId();
    }
    return null;
  }

}
