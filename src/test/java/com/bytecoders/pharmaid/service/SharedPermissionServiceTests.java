package com.bytecoders.pharmaid.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bytecoders.pharmaid.repository.SharedPermissionRepository;
import com.bytecoders.pharmaid.repository.UserRepository;
import com.bytecoders.pharmaid.repository.model.SharedPermission;
import com.bytecoders.pharmaid.repository.model.User;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

/** Unit tests for Shared Permission Service. */
@ExtendWith(MockitoExtension.class)
public class SharedPermissionServiceTests {

  @Mock
  private SharedPermissionRepository sharedPermissionRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private SharedPermissionService sharedPermissionService;

  private User owner;
  private User requester;
  private SharedPermission permission;

  @BeforeEach
  void setUp() {
    owner = new User();
    owner.setId("owner123");

    requester = new User();
    requester.setId("requester456");

    permission = new SharedPermission();
    permission.setId("permission789");
    permission.setOwner(owner);
    permission.setRequester(requester);
    permission.setPermissionType(0);
    permission.setStatus(0);
  }

  @Test
  void createSharingRequest_Success() {
    when(userRepository.findById("owner123")).thenReturn(Optional.of(owner));

    when(userRepository.findById("requester456")).thenReturn(Optional.of(requester));

    // permission doesn't already exist
    when(sharedPermissionRepository.findByOwnerAndRequesterAndPermissionTypeAndStatus(any(),
        any(),
        any(),
        any())).thenReturn(Optional.empty());

    when(sharedPermissionRepository.save(any())).thenReturn(permission);

    SharedPermission
        result =
        sharedPermissionService.createSharingRequest("requester456", "owner123", 0);

    assertNotNull(result);
    assertEquals(owner, result.getOwner());
    assertEquals(requester, result.getRequester());
    assertEquals(0, result.getPermissionType());
    assertEquals(0, result.getStatus());
    // verify save was called only once
    verify(sharedPermissionRepository).save(any());
  }

  @Test
  void createSharingRequest_ExistingPermission() {

    when(userRepository.findById("owner123")).thenReturn(Optional.of(owner));
    when(userRepository.findById("requester456")).thenReturn(Optional.of(requester));

    // permission already exists
    when(sharedPermissionRepository.findByOwnerAndRequesterAndPermissionTypeAndStatus(any(),
        any(),
        any(),
        any())).thenReturn(Optional.of(permission));

    SharedPermission
        result =
        sharedPermissionService.createSharingRequest("requester456", "owner123", 0);

    assertNotNull(result);
    assertEquals(permission, result);

    // verify save was never called
    verify(sharedPermissionRepository, never()).save(any());
  }

  @Test
  void createSharingRequest_OwnerNotFound() {
    // set up owner doesn't exist
    when(userRepository.findById("owner123")).thenReturn(Optional.empty());

    assertThrows(ResponseStatusException.class,
        () -> sharedPermissionService.createSharingRequest("requester456", "owner123", 0));
  }

  @Test
  void createSharingRequest_RequesterNotFound() {

    when(userRepository.findById("owner123")).thenReturn(Optional.of(owner));
    when(userRepository.findById("requester456")).thenReturn(Optional.empty());

    assertThrows(ResponseStatusException.class,
        () -> sharedPermissionService.createSharingRequest("requester456", "owner123", 0));
  }

  @Test
  void createSharingRequest_SelfPermission() {
    String userId = "user123";

    when(userRepository.findById("requester456")).thenReturn(Optional.of(requester));

    assertThrows(IllegalArgumentException.class,
        () -> sharedPermissionService.createSharingRequest("requester456", "requester456", 0));
  }

  @Test
  void createSharingRequest_InvalidPermissionType() {
    // permission can only be 0 or 1

    when(userRepository.findById("owner123")).thenReturn(Optional.of(owner));

    assertThrows(IllegalArgumentException.class,
        () -> sharedPermissionService.createSharingRequest("requester456", "owner123", 2));
  }

  @Test
  void acceptSharingRequest_Success() {

    permission.setStatus(0); // pending
    when(sharedPermissionRepository.findById("permission789")).thenReturn(Optional.of(permission));
    when(sharedPermissionRepository.save(any())).thenReturn(permission);

    SharedPermission
        result =
        sharedPermissionService.acceptDenySharingRequest("owner123", "permission789", 1);

    assertNotNull(result);
    assertEquals(1, result.getStatus()); // accepted
    verify(sharedPermissionRepository).save(permission);
  }

  @Test
  void denySharingRequest_Success() {

    permission.setStatus(0); // pending
    when(sharedPermissionRepository.findById("permission789")).thenReturn(Optional.of(permission));
    when(sharedPermissionRepository.save(any())).thenReturn(permission);

    SharedPermission
        result =
        sharedPermissionService.acceptDenySharingRequest("owner123", "permission789", 2);

    assertNotNull(result);
    assertEquals(2, result.getStatus()); // accepted
    verify(sharedPermissionRepository).save(permission);
  }

  @Test
  void acceptDenySharingRequest_NotFound() {
    // request with permission id doesn't exist
    when(sharedPermissionRepository.findById("permission789")).thenReturn(Optional.empty());

    assertThrows(ResponseStatusException.class,
        () -> sharedPermissionService.acceptDenySharingRequest("owner123", "permission789", 1));
  }

  @Test
  void acceptDenySharingRequest_NotAuthorized() {

    // in before each permission's owner is set
    when(sharedPermissionRepository.findById("permission789")).thenReturn(Optional.of(permission));

    assertThrows(ResponseStatusException.class,
        () -> sharedPermissionService.acceptDenySharingRequest("wrongOwner", "permission789", 1));
  }

  @Test
  void acceptDenySharingRequest_NotPending() {

    // already accepted
    permission.setStatus(1);
    when(sharedPermissionRepository.findById("permission789")).thenReturn(Optional.of(permission));

    assertThrows(IllegalArgumentException.class,
        () -> sharedPermissionService.acceptDenySharingRequest("owner123", "permission789", 1));
  }

  @Test
  void acceptDenySharingRequest_InvalidAcceptValue() {

    when(sharedPermissionRepository.findById("permission789")).thenReturn(Optional.of(permission));

    // accept or deny 1 or 2
    assertThrows(IllegalArgumentException.class,
        () -> sharedPermissionService.acceptDenySharingRequest("owner123", "permission789", 3));
  }

  @Test
  void revokeSharingPermission_Success() {

    // first set accepted
    permission.setStatus(1);
    when(sharedPermissionRepository.findById("permission789")).thenReturn(Optional.of(permission));

    sharedPermissionService.revokeSharingPermission("owner123", "permission789");

    // verify delete happens exactly once
    verify(sharedPermissionRepository).delete(permission);
  }

  @Test
  void revokeSharingPermission_NotFound() {
    // permission id not found
    when(sharedPermissionRepository.findById("permission789")).thenReturn(Optional.empty());

    assertThrows(ResponseStatusException.class,
        () -> sharedPermissionService.revokeSharingPermission("owner123", "permission789"));
  }

  @Test
  void revokeSharingPermission_NotAuthorized() {

    // owner set in before each
    permission.setStatus(1);
    when(sharedPermissionRepository.findById("permission789")).thenReturn(Optional.of(permission));

    assertThrows(ResponseStatusException.class,
        () -> sharedPermissionService.revokeSharingPermission("wrongOwner", "permission789"));
  }

  @Test
  void revokeSharingPermission_NotAccepted() {

    permission.setStatus(0); // pending
    when(sharedPermissionRepository.findById("permission789")).thenReturn(Optional.of(permission));

    // can't call revoke on a yet to be accepted request
    assertThrows(IllegalArgumentException.class,
        () -> sharedPermissionService.revokeSharingPermission("owner123", "permission789"));
  }
}
