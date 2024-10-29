package com.bytecoders.pharmaid;import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.repository.model.Prescription;
import com.bytecoders.pharmaid.repository.model.SharingRequest;
import com.bytecoders.pharmaid.service.AuthorizationService;
import com.bytecoders.pharmaid.service.PrescriptionService;
import com.bytecoders.pharmaid.service.SharingPermissionService;
import com.bytecoders.pharmaid.service.UserService;
import com.bytecoders.pharmaid.service.MedicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)// Replace YourController with the actual controller class name
public class SharingPermissionTests {

  @Mock private SecurityContext securityContext;
  @Mock private Authentication authentication;

  @Autowired private MockMvc mockMvc;

  @MockBean private AuthorizationService authorizationService;
  @MockBean private PrescriptionService prescriptionService;
  @MockBean private UserService userService;
  @MockBean private MedicationService medicationService;
  @MockBean private SharingPermissionService sharingPermissionService;

  @BeforeEach
  public void setup() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);
  }

  /**
   * Test for `getCurrentUserId` with authenticated user.
   */
  @Test
  public void testGetCurrentUserId_authenticatedUser() {
    User user = new User();
    user.setId("user123");

    when(authentication.getPrincipal()).thenReturn(user);

    String userId = sharingPermissionService.getCurrentUserId();
    assertEquals("user123", userId);
  }

  /**
   * Test for `getCurrentUserId` with no authentication.
   */
  @Test
  public void testGetCurrentUserId_notAuthenticated() {
    when(securityContext.getAuthentication()).thenReturn(null);

    RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
      sharingPermissionService.getCurrentUserId();
    });
    assertEquals("User not authenticated", thrown.getMessage());
  }

  /**
   * Test for `addPrescription` endpoint when authorized.
   */
  @Test
  public void testAddPrescription_authorized() throws Exception {
    when(authorizationService.canModifyUserRecords(anyString(), anyString())).thenReturn(true);

    mockMvc.perform(post("/user123/records/prescriptions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"medicationId\": \"med123\", \"dosage\": 5}")
            .header("Authorization", "Bearer validToken"))
        .andExpect(status().isCreated());
  }

  /**
   * Test for `addPrescription` endpoint when unauthorized.
   */
  @Test
  public void testAddPrescription_unauthorized() throws Exception {
    when(authorizationService.canModifyUserRecords(anyString(), anyString())).thenReturn(false);

    mockMvc.perform(post("/user123/records/prescriptions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"medicationId\": \"med123\", \"dosage\": 5}")
            .header("Authorization", "Bearer validToken"))
        .andExpect(status().isForbidden())
        .andExpect(content().string("Not authorized to create prescriptions for this user"));
  }

  /**
   * Test for `getPrescriptionsForUser` endpoint when authorized.
   */
  @Test
  public void testGetPrescriptionsForUser_authorized() throws Exception {
    when(userService.getUser(anyString())).thenReturn(Optional.of(new User()));
    when(authorizationService.canAccessUserRecords(anyString(), anyString())).thenReturn(true);

    List<Prescription> prescriptions = List.of(new Prescription());
    when(prescriptionService.getPrescriptionsForUser(anyString())).thenReturn(prescriptions);

    mockMvc.perform(get("/users/user123/prescriptions")
            .header("Authorization", "Bearer validToken"))
        .andExpect(status().isOk())
        .andExpect(content().json("[{}]"));  // Adjust JSON content as needed
  }

  /**
   * Test for `getPrescriptionsForUser` endpoint when unauthorized.
   */
  @Test
  public void testGetPrescriptionsForUser_unauthorized() throws Exception {
    when(authorizationService.canAccessUserRecords(anyString(), anyString())).thenReturn(false);

    mockMvc.perform(get("/users/user123/prescriptions")
            .header("Authorization", "Bearer validToken"))
        .andExpect(status().isForbidden())
        .andExpect(content().string("You are not authorized to view these prescriptions"));
  }

  /**
   * Test for `requestUserPrescriptions` endpoint.
   */
  @Test
  public void testRequestUserPrescriptions() throws Exception {
    when(sharingPermissionService.createSharingRequest(anyString(), any(SharingRequest.class)))
        .thenReturn("requestId123");

    mockMvc.perform(post("/user123/records/request")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"permissionType\": \"VIEW\"}")
            .header("Authorization", "Bearer validToken"))
        .andExpect(status().isOk())
        .andExpect(content().string("requestId123"));
  }
}


