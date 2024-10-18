package com.bytecoders.pharmaid;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.bytecoders.pharmaid.repository.SharingPermissionRepository;
import com.bytecoders.pharmaid.repository.UserRepository;
import com.bytecoders.pharmaid.repository.model.PermissionType;
import com.bytecoders.pharmaid.repository.model.Prescription;
import com.bytecoders.pharmaid.repository.model.SharingPermissionStatus;
import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.security.CustomUserDetails;
import com.bytecoders.pharmaid.service.AuthorizationService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private SharingPermissionRepository sharingPermissionRepository;

  @Mock
  private SecurityContext securityContext;

  @Mock
  private Authentication authentication;

  @InjectMocks
  private AuthorizationService authorizationService;

  @Test
  void canAccessUserRecords_ownerAccess() {
    String currentUserId = "user123";
    String targetUserId = "user123";

    boolean result = authorizationService.canAccessUserRecords(currentUserId, targetUserId);

    assertTrue(result);
  }

  @Test
  void canAccessUserRecords_permissionGranted() {
    String currentUserId = "user123";
    String targetUserId = "user456";

    User currentUser = new User();
    User targetUser = new User();
    when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
    when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetUser));
    when(sharingPermissionRepository.existsByOwnerAndSharedWithUserAndPermissionTypeInAndStatus(
        targetUser, currentUser, List.of(PermissionType.VIEW, PermissionType.EDIT),
        SharingPermissionStatus.ACCEPTED))
        .thenReturn(true);

    boolean result = authorizationService.canAccessUserRecords(currentUserId, targetUserId);

    assertTrue(result);
  }

  @Test
  void canModifyUserRecords_noPermission() {
    String currentUserId = "user123";
    String targetUserId = "user456";

    User currentUser = new User();
    User targetUser = new User();
    when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
    when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetUser));
    when(sharingPermissionRepository.existsByOwnerAndSharedWithUserAndPermissionTypeAndStatus(
        targetUser, currentUser, PermissionType.EDIT, SharingPermissionStatus.ACCEPTED))
        .thenReturn(false);

    boolean result = authorizationService.canModifyUserRecords(currentUserId, targetUserId);

    assertFalse(result);
  }

  @Test
  void canAccessPrescription_ownerAccess() {
    User user = new User();
    user.setId("user123");
    Prescription prescription = new Prescription();
    prescription.setUser(user);

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(new CustomUserDetails(user));
    SecurityContextHolder.setContext(securityContext);

    boolean result = authorizationService.canAccessPrescription(prescription);

    assertTrue(result);
  }


}

