package com.bytecoders.pharmaid.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.bytecoders.pharmaid.repository.SharingPermissionRepository;
import com.bytecoders.pharmaid.repository.UserRepository;
import com.bytecoders.pharmaid.repository.model.PermissionType;
import com.bytecoders.pharmaid.repository.model.Prescription;
import com.bytecoders.pharmaid.repository.model.SharingPermissionStatus;
import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.security.CustomUserDetails;

/**
 *
 */
@Service
public class AuthorizationService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private SharingPermissionRepository sharingPermissionRepository;

  
  /** 
   * @param currentUserId
   * @param targetUserId
   * @return boolean
   */
  public boolean canAccessUserRecords(String currentUserId, String targetUserId) {

    if (currentUserId.equals(targetUserId)) {
      return true;
    }

    User currentUser = userRepository.findById(currentUserId)
        .orElseThrow(() -> new RuntimeException("Current user not found"));

    User targetUser = userRepository.findById(targetUserId)
        .orElseThrow(() -> new RuntimeException("Target user not found"));

    List<PermissionType> allowedPermissions = Arrays.asList(PermissionType.VIEW,
        PermissionType.EDIT);

    boolean hasUserPermission = sharingPermissionRepository.existsByOwnerAndSharedWithUserAndPermissionTypeInAndStatus(
        targetUser, currentUser, allowedPermissions, SharingPermissionStatus.ACCEPTED);

    if (hasUserPermission) {
      return true;
    }

    return false;
  }

  public boolean canModifyUserRecords(String currentUserId, String targetUserId) {
    if (currentUserId.equals(targetUserId)) {
      return true;
    }

    User currentUser = userRepository.findById(currentUserId)
        .orElseThrow(() -> new RuntimeException("Current user not found"));

    User targetUser = userRepository.findById(targetUserId)
        .orElseThrow(() -> new RuntimeException("Target user not found"));

    PermissionType requiredPermission = PermissionType.EDIT;

    boolean hasUserPermission = sharingPermissionRepository.existsByOwnerAndSharedWithUserAndPermissionTypeAndStatus(
        targetUser, currentUser, requiredPermission, SharingPermissionStatus.ACCEPTED);

    if (hasUserPermission) {
      return true;
    }

    return false;
  }

  public boolean canAccessPrescription(Prescription prescription) {
    String currentUserId = getCurrentUserId();

    if (currentUserId.equals(prescription.getUser().getId())) {
      return true;
    }

    return canAccessUserRecords(currentUserId, prescription.getUser().getId());
  }

  public boolean canModifyPrescription(Prescription prescription) {
    String currentUserId = getCurrentUserId();

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
