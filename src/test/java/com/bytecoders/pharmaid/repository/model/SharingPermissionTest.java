package com.bytecoders.pharmaid.repository.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for SharingPermission entity.
 */
class SharingPermissionTest {

  private SharingPermission permission;
  private User owner;
  private User sharedWithUser;

  @BeforeEach
  void setUp() {
    permission = new SharingPermission();
    owner = new User();
    owner.setId("owner-id");
    owner.setEmail("owner@test.com");

    sharedWithUser = new User();
    sharedWithUser.setId("shared-id");
    sharedWithUser.setEmail("shared@test.com");
  }

  @Test
  void testSharingPermissionCreation() {
    permission.setId("test-id");
    permission.setOwner(owner);
    permission.setSharedWithUser(sharedWithUser);
    permission.setPermissionType(PermissionType.VIEW);
    permission.setStatus(SharingPermissionStatus.PENDING);
    permission.setCreatedAt(new Date());

    assertNotNull(permission.getId());
    assertEquals("test-id", permission.getId());
    assertEquals(owner, permission.getOwner());
    assertEquals(sharedWithUser, permission.getSharedWithUser());
    assertEquals(PermissionType.VIEW, permission.getPermissionType());
    assertEquals(SharingPermissionStatus.PENDING, permission.getStatus());
    assertNotNull(permission.getCreatedAt());
  }

  @Test
  void testPermissionTypeEnum() {
    permission.setPermissionType(PermissionType.VIEW);
    assertEquals(PermissionType.VIEW, permission.getPermissionType());

    permission.setPermissionType(PermissionType.EDIT);
    assertEquals(PermissionType.EDIT, permission.getPermissionType());
  }

  @Test
  void testStatusEnum() {
    permission.setStatus(SharingPermissionStatus.PENDING);
    assertEquals(SharingPermissionStatus.PENDING, permission.getStatus());

    permission.setStatus(SharingPermissionStatus.ACCEPTED);
    assertEquals(SharingPermissionStatus.ACCEPTED, permission.getStatus());

    permission.setStatus(SharingPermissionStatus.DENIED);
    assertEquals(SharingPermissionStatus.DENIED, permission.getStatus());
  }
}
