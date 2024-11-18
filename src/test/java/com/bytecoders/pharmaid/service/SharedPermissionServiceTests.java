package com.bytecoders.pharmaid.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bytecoders.pharmaid.openapi.model.SharePermissionType;
import com.bytecoders.pharmaid.openapi.model.ShareRequestStatus;
import com.bytecoders.pharmaid.openapi.model.UserType;
import com.bytecoders.pharmaid.repository.SharedPermissionRepository;
import com.bytecoders.pharmaid.repository.model.SharedPermission;
import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.util.ServiceUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Unit tests for Shared Permission Service.
 */
@ExtendWith(MockitoExtension.class)
public class SharedPermissionServiceTests {

  @Mock
  private SharedPermissionRepository sharedPermissionRepository;

  @Mock
  private UserService userService;

  @Mock
  private ServiceUtils serviceUtils;

  @Mock
  private SharedPermissionValidator permissionValidator;

  @InjectMocks
  private SharedPermissionService sharedPermissionService;

  private User owner;
  private User requester;
  private SharedPermission permission;
  private final String nonExistentUserId = "nonExistentUser123";

  @BeforeEach
  void setUp() {
    // owner user of the health records
    owner = new User();
    owner.setId("owner123");

    // user making a request to act on another user's health records
    requester = new User();
    requester.setId("requester456");
    requester.setUserType(UserType.HEALTHCARE_PROVIDER);

    // a permission set between owner and requester
    permission = new SharedPermission();
    permission.setId("permission789");
    permission.setOwner(owner);
    permission.setRequester(requester);
    permission.setSharePermissionType(SharePermissionType.VIEW);
    permission.setStatus(ShareRequestStatus.PENDING);
  }

  @Test
  void createSharingRequest_Success() {
    // mock owner, requester, and saved permission
    when(userService.getUser(owner.getId())).thenReturn(owner);
    when(userService.getUser(requester.getId())).thenReturn(requester);
    when(sharedPermissionRepository.save(any(SharedPermission.class))).thenReturn(permission);

    SharedPermission result =
        sharedPermissionService.createSharingRequest(requester.getId(), owner.getId(),
            permission.getSharePermissionType());

    // assertions and verify
    assertNotNull(result);
    assertEquals(owner, result.getOwner());
    assertEquals(requester, result.getRequester());
    assertEquals(permission.getSharePermissionType(), result.getSharePermissionType());
    assertEquals(permission.getStatus(), result.getStatus());
    verify(permissionValidator).validateCreateShareRequestSetup(owner, requester,
        SharePermissionType.VIEW);
    verify(sharedPermissionRepository).save(any(SharedPermission.class));
  }

  @Test
  void shareRequestAction_AcceptSuccess() {
    when(serviceUtils.findEntityById(eq(permission.getId()), eq("shareRequest"),
        eq(sharedPermissionRepository))).thenReturn(permission);
    when(sharedPermissionRepository.save(any(SharedPermission.class))).thenReturn(permission);

    // ACCEPT share action
    SharedPermission result =
        sharedPermissionService.shareRequestAction(owner.getId(), permission.getId(),
            ShareRequestStatus.ACCEPT);

    // assertions and verify
    assertNotNull(result);
    assertEquals(ShareRequestStatus.ACCEPT, result.getStatus());
    assertEquals(permission, result);
    verify(permissionValidator).validateShareRequestAction(permission, owner.getId(),
        ShareRequestStatus.ACCEPT);
    verify(sharedPermissionRepository).save(permission);
  }

  @Test
  void shareRequestAction_DenySuccess() {
    when(serviceUtils.findEntityById(eq(permission.getId()), eq("shareRequest"),
        eq(sharedPermissionRepository))).thenReturn(permission);
    when(sharedPermissionRepository.save(any(SharedPermission.class))).thenReturn(permission);

    // DENY share action
    SharedPermission result =
        sharedPermissionService.shareRequestAction(owner.getId(), permission.getId(),
            ShareRequestStatus.DENY);

    // assertions and verify
    assertNotNull(result);
    assertEquals(ShareRequestStatus.DENY, result.getStatus());
    assertEquals(permission, result);
    verify(permissionValidator).validateShareRequestAction(permission, owner.getId(),
        ShareRequestStatus.DENY);
    verify(sharedPermissionRepository).save(permission);
  }

  @Test
  void shareRequestAction_PendingFailure() {
    when(serviceUtils.findEntityById(eq(permission.getId()), eq("shareRequest"),
        eq(sharedPermissionRepository))).thenReturn(permission);

    doThrow(
        new IllegalArgumentException("Cannot choose PENDING as a share action response")).when(
            permissionValidator)
        .validateShareRequestAction(permission, owner.getId(), ShareRequestStatus.PENDING);

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      sharedPermissionService.shareRequestAction(owner.getId(), permission.getId(),
          ShareRequestStatus.PENDING);
    });

    // assertions and verify
    assertEquals("Cannot choose PENDING as a share action response", exception.getMessage());
    verify(permissionValidator).validateShareRequestAction(permission, owner.getId(),
        ShareRequestStatus.PENDING);
    verify(sharedPermissionRepository, never()).save(any());
  }

  @Test
  void revokeSharingPermission_Success() {
    permission.setStatus(ShareRequestStatus.ACCEPT);
    when(serviceUtils.findEntityById(eq(permission.getId()), eq("shareRequest"),
        eq(sharedPermissionRepository))).thenReturn(permission);

    doNothing().when(permissionValidator)
        .validateRevokeSharePermission(permission, owner.getId());

    sharedPermissionService.revokeSharingPermission(owner.getId(), permission.getId());

    verify(permissionValidator).validateRevokeSharePermission(permission, owner.getId());
    verify(sharedPermissionRepository).delete(permission);
  }

  @Test
  void revokeSharingPermission_Failure_InvalidOwner() {
    when(serviceUtils.findEntityById(eq(permission.getId()), eq("shareRequest"),
        eq(sharedPermissionRepository))).thenReturn(permission);

    doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN,
        "Not authorized to act on this request")).when(permissionValidator)
        .validateRevokeSharePermission(eq(permission), eq(nonExistentUserId));

    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
      sharedPermissionService.revokeSharingPermission(nonExistentUserId, permission.getId());
    });

    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    assertEquals("Not authorized to act on this request", exception.getReason());
  }

  @Test
  void revokeSharingPermission_Failure_InvalidRevokeRequest() {
    when(serviceUtils.findEntityById(eq(permission.getId()), eq("shareRequest"),
        eq(sharedPermissionRepository))).thenReturn(permission);

    // throw exception when attempting to revoke PENDING share request
    doThrow(
        new IllegalArgumentException("Can only revoke already accepted shared permissions")).when(
        permissionValidator).validateRevokeSharePermission(permission, owner.getId());

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      sharedPermissionService.revokeSharingPermission(owner.getId(), permission.getId());
    });

    // assertions and verify
    assertEquals("Can only revoke already accepted shared permissions", exception.getMessage());
    verify(permissionValidator).validateRevokeSharePermission(permission, owner.getId());
    verify(sharedPermissionRepository, never()).delete(any());
  }
}
