package com.bytecoders.pharmaid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.bytecoders.pharmaid.openapi.model.SharePermissionType;
import com.bytecoders.pharmaid.openapi.model.ShareRequest;
import com.bytecoders.pharmaid.openapi.model.ShareRequestStatus;
import com.bytecoders.pharmaid.openapi.model.UserType;
import com.bytecoders.pharmaid.repository.model.SharedPermission;
import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.service.SharedPermissionService;
import com.bytecoders.pharmaid.util.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

/**
 * Accept, Deny, Request, Revoke endpoint Tests. Mocks JwtUtil and SharePermission.
 */
@WebMvcTest(ShareController.class)
class ShareControllerTest {

  @MockBean
  private JwtUtils jwtUtils;

  @MockBean
  private SharedPermissionService sharedPermissionService;

  @Autowired
  private ShareController shareController;

  @Autowired
  private ObjectMapper objectMapper;

  private User owner;
  private User requester;
  private SharedPermission permission;
  private ShareRequest request;

  @BeforeEach
  void setUp() {
    // owner user of the health records
    owner = new User();
    owner.setId("owner123");

    // user making a request to interact with another user's health records
    requester = new User();
    requester.setId("requester456");
    requester.setUserType(UserType.HEALTHCARE_PROVIDER);

    // create a share permission
    permission = new SharedPermission();
    permission.setId("permission789");
    permission.setOwner(owner);
    permission.setRequester(requester);
    permission.setSharePermissionType(SharePermissionType.EDIT);
    permission.setStatus(ShareRequestStatus.PENDING);

    // create a share request
    request = new ShareRequest();
    request.setSharePermissionType(SharePermissionType.EDIT);
  }

  @Test
  void requestAccess_Success() {
    // Mock log in and createSharingRequest()
    when(jwtUtils.getLoggedInUserId()).thenReturn(requester.getId());
    when(sharedPermissionService.createSharingRequest(requester.getId(), owner.getId(),
        SharePermissionType.EDIT)).thenReturn(permission);

    // Generate requestAccess() request
    ResponseEntity<?> response = shareController.requestAccess(owner.getId(), request);

    // Assertions
    assertEquals(response.getStatusCode(), HttpStatus.CREATED);
    SharedPermission responseBody = (SharedPermission) response.getBody();
    assertEquals(permission, responseBody);
  }

  @Test
  void acceptRequest_Success() {
    // Mock login and shareRequestAction()
    when(jwtUtils.getLoggedInUserId()).thenReturn(owner.getId());
    when(sharedPermissionService.shareRequestAction(owner.getId(), permission.getId(),
        ShareRequestStatus.ACCEPT)).thenReturn(permission);

    // Generate acceptShareRequest() request
    ResponseEntity<?> response =
        shareController.acceptShareRequest(owner.getId(), permission.getId());

    // Assertions
    assertEquals(response.getStatusCode(), HttpStatus.OK);
    SharedPermission responseBody = (SharedPermission) response.getBody();
    assertEquals(permission, responseBody);
  }

  @Test
  void denyRequest_Success() {
    // Mock login and shareRequestAction()
    when(jwtUtils.getLoggedInUserId()).thenReturn(owner.getId());
    when(sharedPermissionService.shareRequestAction(owner.getId(), permission.getId(),
        ShareRequestStatus.DENY)).thenReturn(permission);

    // Generate denyShareRequest() request
    ResponseEntity<?> response =
        shareController.denyShareRequest(owner.getId(), permission.getId());

    // Assertions
    assertEquals(response.getStatusCode(), HttpStatus.OK);
    SharedPermission responseBody = (SharedPermission) response.getBody();
    assertEquals(permission, responseBody);
  }


  @Test
  void revokeAccess_Success() {
    // Mock login and shareRequestAction()
    when(jwtUtils.getLoggedInUserId()).thenReturn(owner.getId());
    doNothing().when(sharedPermissionService)
        .revokeSharingPermission(owner.getId(), permission.getId());

    // Generate denyShareRequest() request
    ResponseEntity<?> response =
        shareController.revokeShareAccess(owner.getId(), permission.getId());

    // Assertions
    assertEquals(response.getStatusCode(), HttpStatus.OK);
    assertEquals(response.getBody(), "Access revoked successfully");
  }


  @Test
  void revokeAccess_Unauthorized() {
    final String unauthorizedMessage = "Not authorized to revoke this request";

    // Mock login and revokeSharingPermission() to throw unauthorized access error
    when(jwtUtils.getLoggedInUserId()).thenReturn(owner.getId());
    doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, unauthorizedMessage)).when(
            sharedPermissionService)
        .revokeSharingPermission(eq(owner.getId()), eq(permission.getId()));

    // Generate revokeShareAccess() request
    ResponseEntity<?> response =
        shareController.revokeShareAccess(owner.getId(), permission.getId());

    // Assertions
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertEquals(unauthorizedMessage, response.getBody());
  }
}