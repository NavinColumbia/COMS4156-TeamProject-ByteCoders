package com.bytecoders.pharmaid;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bytecoders.pharmaid.openapi.model.ShareRequest;
import com.bytecoders.pharmaid.repository.model.SharedPermission;
import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.service.SharedPermissionService;
import com.bytecoders.pharmaid.util.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;

/**
 * Accept, Deny, Request, Revoke endpoint Tests. Mocks JwtUtil and SharePermission.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ShareControllerTest {

  @Autowired
  private WebApplicationContext context;

  private MockMvc mockMvc;

  @MockBean
  private SharedPermissionService sharedPermissionService;

  @Autowired
  private ObjectMapper objectMapper;

  private static final String REQUESTER_ID = "testUser123";
  private static final String OWNER_ID = "ownerUser456";
  private static final String SHARE_REQUEST_ID = "requestId789";

  private User testRequester;
  private User testOwner;
  private SharedPermission testPermission;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(springSecurity())
        .build();

    testRequester = new User();
    testRequester.setId(REQUESTER_ID);
    testRequester.setUserType(1);

    testOwner = new User();
    testOwner.setId(OWNER_ID);
    testOwner.setUserType(1);

    testPermission = new SharedPermission();
    testPermission.setId(SHARE_REQUEST_ID);
    testPermission.setOwner(testOwner);
    testPermission.setRequester(testRequester);
    testPermission.setPermissionType(1);
  }

  @Test
  @WithMockUser
  void requestAccess_Success() throws Exception {
    ShareRequest shareRequest = new ShareRequest();
    shareRequest.setPermissionType(1); //edit
    testPermission.setStatus(0); //pending

    try (MockedStatic<JwtUtils> jwtUtils = Mockito.mockStatic(JwtUtils.class)) {
      //mock it to return the id of the requester
      jwtUtils.when(JwtUtils::getLoggedInUserId).thenReturn(REQUESTER_ID);
      //mock the service to return the Share Permission Object
      Mockito.when(sharedPermissionService.createSharingRequest(
              anyString(), anyString(), any(Integer.class)))
          .thenReturn(testPermission);


      mockMvc.perform(post("/share/request/" + OWNER_ID)
              .with(SecurityMockMvcRequestPostProcessors.csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(shareRequest)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value(SHARE_REQUEST_ID))
          .andExpect(jsonPath("$.owner.id").value(OWNER_ID))
          .andExpect(jsonPath("$.requester.id").value(REQUESTER_ID));
    }
  }

  @Test
  @WithMockUser
  void acceptRequest_Success() throws Exception {
    testPermission.setStatus(1);

    try (MockedStatic<JwtUtils> jwtUtils = Mockito.mockStatic(JwtUtils.class)) {
      jwtUtils.when(JwtUtils::getLoggedInUserId).thenReturn(REQUESTER_ID);
      Mockito.when(sharedPermissionService.acceptDenySharingRequest(
              anyString(), anyString(), eq(1)))
          .thenReturn(testPermission);

      mockMvc.perform(post("/share/" + SHARE_REQUEST_ID + "/accept")
              .with(SecurityMockMvcRequestPostProcessors.csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.status").value(1));
    }
  }

  @Test
  @WithMockUser
  void denyRequest_Success() throws Exception {
    testPermission.setStatus(2);

    try (MockedStatic<JwtUtils> jwtUtils = Mockito.mockStatic(JwtUtils.class)) {
      jwtUtils.when(JwtUtils::getLoggedInUserId).thenReturn(REQUESTER_ID);
      Mockito.when(sharedPermissionService.acceptDenySharingRequest(
              anyString(), anyString(), eq(2)))
          .thenReturn(testPermission);

      mockMvc.perform(post("/share/" + SHARE_REQUEST_ID + "/deny")
              .with(SecurityMockMvcRequestPostProcessors.csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.status").value(2));
    }
  }

  @Test
  @WithMockUser
  void revokeAccess_Success() throws Exception {
    try (MockedStatic<JwtUtils> jwtUtils = Mockito.mockStatic(JwtUtils.class)) {
      jwtUtils.when(JwtUtils::getLoggedInUserId).thenReturn(REQUESTER_ID);

      doNothing()
          .when(sharedPermissionService)
          .revokeSharingPermission(anyString(), anyString());

      mockMvc.perform(post("/share/" + SHARE_REQUEST_ID + "/revoke")
              .with(SecurityMockMvcRequestPostProcessors.csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").value("Access revoked successfully"));
    }
  }

  @Test
  @WithMockUser
  void revokeAccess_Unauthorized() throws Exception {
    try (MockedStatic<JwtUtils> jwtUtils = Mockito.mockStatic(JwtUtils.class)) {
      jwtUtils.when(JwtUtils::getLoggedInUserId).thenReturn(REQUESTER_ID);

      doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access"))
          .when(sharedPermissionService)
          .revokeSharingPermission(anyString(), anyString());

      mockMvc.perform(post("/share/" + SHARE_REQUEST_ID + "/revoke")
              .with(SecurityMockMvcRequestPostProcessors.csrf()))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$").value("Unauthorized access"));
    }
  }

  @Test
  @WithMockUser
  void handleInternalServerError() throws Exception {
    try (MockedStatic<JwtUtils> jwtUtils = Mockito.mockStatic(JwtUtils.class)) {
      jwtUtils.when(JwtUtils::getLoggedInUserId).thenReturn(REQUESTER_ID);

      doThrow(new RuntimeException("Unexpected error"))
          .when(sharedPermissionService)
          .revokeSharingPermission(anyString(), anyString());

      mockMvc.perform(post("/share/" + SHARE_REQUEST_ID + "/revoke")
              .with(SecurityMockMvcRequestPostProcessors.csrf()))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$").value("Internal Server Error"));
    }
  }
}