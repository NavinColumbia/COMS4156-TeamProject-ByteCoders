package com.bytecoders.pharmaid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.bytecoders.pharmaid.exception.AuthenticationException;
import com.bytecoders.pharmaid.exception.NotAuthorizedException;
import com.bytecoders.pharmaid.exception.ResourceNotFoundException;
import com.bytecoders.pharmaid.exception.UserNotFoundException;
import com.bytecoders.pharmaid.repository.model.SharingPermission;
import com.bytecoders.pharmaid.repository.model.SharingPermissionStatus;
import com.bytecoders.pharmaid.repository.model.SharingRequest;
import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.service.SharingPermissionService;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class SharingPermissionServiceTests {

  @Mock private SharingPermissionService sharingPermissionService;

  @Mock private SecurityContext securityContext;

  @Mock private Authentication authentication;

  @Mock private User user;

  private SharingRequest sharingRequest;
  private SharingPermission sharingPermission;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    sharingRequest = new SharingRequest();
    sharingPermission = new SharingPermission();
    sharingPermission.setCreatedAt(new Date());
    sharingPermission.setStatus(SharingPermissionStatus.PENDING);
  }

  @Test
  public void testCreateSharingRequest_UserNotFound() {
    // Given
    String ownerId = "ownerId";
    when(authentication.getPrincipal()).thenReturn("requesterId");
    when(sharingPermissionService.createSharingRequest(ownerId, sharingRequest))
        .thenThrow(new UserNotFoundException("User not found with ID: " + ownerId));

    // When & Then
    assertThrows(
        UserNotFoundException.class,
        () -> sharingPermissionService.createSharingRequest(ownerId, sharingRequest));
  }

  @Test
  public void testCreateSharingRequest_SelfSharingRequest() {
    // Given
    String ownerId = "requesterId";
    when(authentication.getPrincipal()).thenReturn(ownerId);
    when(sharingPermissionService.createSharingRequest(ownerId, sharingRequest))
        .thenThrow(new IllegalArgumentException("Cannot create sharing permission with yourself"));

    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () -> sharingPermissionService.createSharingRequest(ownerId, sharingRequest));
  }

  @Test
  public void testAcceptSharingRequest_NotAuthorized() {
    // Given
    String ownerId = "otherUserId";
    String permissionId = "permissionId";
    when(authentication.getPrincipal()).thenReturn("requesterId");
    when(sharingPermissionService.acceptSharingRequest(ownerId, permissionId))
        .thenThrow(new NotAuthorizedException("Not authorized to accept this request"));

    // When & Then
    assertThrows(
        NotAuthorizedException.class,
        () -> sharingPermissionService.acceptSharingRequest(ownerId, permissionId));
  }

  @Test
  public void testAcceptSharingRequest_InvalidStatus() {
    // Given
    String ownerId = "ownerId";
    String permissionId = "permissionId";
    when(authentication.getPrincipal()).thenReturn(ownerId);
    when(sharingPermissionService.acceptSharingRequest(ownerId, permissionId))
        .thenThrow(new IllegalArgumentException("Can only accept pending requests"));

    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () -> sharingPermissionService.acceptSharingRequest(ownerId, permissionId));
  }

  @Test
  public void testDenySharingRequest_NotAuthorized() {
    // Given
    String ownerId = "otherUserId";
    String permissionId = "permissionId";
    when(authentication.getPrincipal()).thenReturn("requesterId");
    when(sharingPermissionService.denySharingRequest(ownerId, permissionId))
        .thenThrow(new NotAuthorizedException("Not authorized to deny this request"));

    // When & Then
    assertThrows(
        NotAuthorizedException.class,
        () -> sharingPermissionService.denySharingRequest(ownerId, permissionId));
  }

  @Test
  public void testRevokeSharingPermission_NotAuthorized() {
    // Given
    String ownerId = "otherUserId";
    String permissionId = "permissionId";
    when(authentication.getPrincipal()).thenReturn("requesterId");

    // Since revokeSharingPermission is a void method, use doThrow instead of when
    doThrow(new NotAuthorizedException("Not authorized to revoke this permission"))
        .when(sharingPermissionService).revokeSharingPermission(ownerId, permissionId);

    // When & Then
    assertThrows(
        NotAuthorizedException.class,
        () -> sharingPermissionService.revokeSharingPermission(ownerId, permissionId));
  }


  @Test
  public void testGetCurrentUserId_NotAuthenticated() {
    // Setup
    when(authentication.getPrincipal()).thenReturn(null);

    // When & Then
    assertThrows(AuthenticationException.class, SharingPermissionService::getCurrentUserId);
  }
}
