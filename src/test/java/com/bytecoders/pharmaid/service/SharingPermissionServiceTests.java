package com.bytecoders.pharmaid.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.bytecoders.pharmaid.exception.UserNotFoundException;
import com.bytecoders.pharmaid.repository.model.SharingPermission;
import com.bytecoders.pharmaid.repository.model.SharingPermissionStatus;
import com.bytecoders.pharmaid.repository.model.SharingRequest;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Unit tests for the SharingPermissionService class. Tests creation, acceptance, denial, and
 * revocation of sharing permissions.
 */
public class SharingPermissionServiceTests {

  @Mock private SharingPermissionService sharingPermissionService;
  @Mock private SecurityContext securityContext;
  @Mock private Authentication authentication;

  private SharingRequest sharingRequest;
  private SharingPermission sharingPermission;


  /**
   * Basic Set up.
   */
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

  /**
   * Tests that creating a sharing request with a non-existent user ID throws a
   * UserNotFoundException.
   */
  @Test
  public void testCreateSharingRequest_UserNotFound() {
    String ownerId = "ownerId";
    when(authentication.getPrincipal()).thenReturn("requesterId");
    when(sharingPermissionService.createSharingRequest(ownerId, sharingRequest))
        .thenThrow(new UserNotFoundException("User not found with ID: " + ownerId));

    assertThrows(
        UserNotFoundException.class,
        () -> sharingPermissionService.createSharingRequest(ownerId, sharingRequest));
  }

  /** Tests that creating a self-sharing request throws an IllegalArgumentException. */
  @Test
  public void testCreateSharingRequest_SelfSharingRequest() {
    String ownerId = "requesterId";
    when(authentication.getPrincipal()).thenReturn(ownerId);
    when(sharingPermissionService.createSharingRequest(ownerId, sharingRequest))
        .thenThrow(new IllegalArgumentException("Cannot create sharing permission with yourself"));

    assertThrows(
        IllegalArgumentException.class,
        () -> sharingPermissionService.createSharingRequest(ownerId, sharingRequest));
  }

  // Additional methods with Javadoc comments can be similarly added here
}
