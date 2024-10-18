package com.bytecoders.pharmaid;

import com.bytecoders.pharmaid.repository.PrescriptionRepository;
import com.bytecoders.pharmaid.repository.RefreshTokenRepository;
import com.bytecoders.pharmaid.repository.SharingPermissionRepository;
import com.bytecoders.pharmaid.repository.UserRepository;
import com.bytecoders.pharmaid.repository.model.Prescription;
import com.bytecoders.pharmaid.repository.model.SharingPermission;
import com.bytecoders.pharmaid.repository.model.SharingPermissionStatus;
import com.bytecoders.pharmaid.repository.model.SharingRequest;
import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.security.CustomUserDetails;
import com.bytecoders.pharmaid.security.JwtTokenProvider;
import com.bytecoders.pharmaid.service.SharingPermissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Collections;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.bytecoders.pharmaid.repository.model.PermissionType;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PrescriptionRepository prescriptionRepository;

  @Autowired
  private RefreshTokenRepository refreshTokenRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private JwtTokenProvider tokenProvider;


 // @Autowired
 // private SharingPermissionRepository sharingPermissionRepository;

 // @Autowired
 // private SharingPermissionService sharingPermissionService;

  private User testUser;
  private String accessToken;
  private Prescription testPrescription;
  private String requestId;

  @BeforeEach
  public void setup() {
    prescriptionRepository.deleteAll();
    userRepository.deleteAll();

    // Create a test user and a test prescription
    testUser = new User();
    testUser.setEmail("testuser4@example.com");
    testUser.setHashedPassword(passwordEncoder.encode("Password123!"));
    userRepository.save(testUser);

    testPrescription = new Prescription();
    testPrescription.setUser(testUser);
    testPrescription.setMedicationName("Ibuprofen");
    testPrescription.setDosage("200mg");
    testPrescription.setFrequency("Twice a day");
    prescriptionRepository.save(testPrescription);

    // Generate JWT token for authentication
    accessToken = tokenProvider.generateToken(testUser);

/*
    // Create a sharing request for testing
    SharingRequest sharingRequest = new SharingRequest();
    sharingRequest.setPermissionType(PermissionType.VIEW);

    requestId = sharingPermissionService.createSharingRequest(testUser.getId(), sharingRequest);
    CustomUserDetails userDetails = new CustomUserDetails(testUser);
    Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null);
    SecurityContextHolder.getContext().setAuthentication(authentication);
*/

  }

  @Test
  public void testGetUserAccount_Success() throws Exception {
    mockMvc.perform(get("/api/users/{user_id}", testUser.getId())
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(testUser.getEmail()));
  }

  @Test
  public void testGetUserAccount_Unauthorized() throws Exception {
    // Create another user
    User anotherUser = new User();
    anotherUser.setEmail("another@example.com");
    anotherUser.setHashedPassword(passwordEncoder.encode("Password123!"));
    userRepository.save(anotherUser);

    mockMvc.perform(get("/api/users/{user_id}", anotherUser.getId())
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isForbidden());
  }

  @Test
  public void testUpdateUserAccount_Success() throws Exception {
    testUser.setEmail("updated@example.com");

    mockMvc.perform(patch("/api/users/{user_id}", testUser.getId())
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testUser)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("updated@example.com"));
  }

  @Test
  public void testDeleteUserAccount_Success() throws Exception {
    mockMvc.perform(delete("/api/users/{user_id}", testUser.getId())
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk());
  }

  @Test
  public void testGetUserHealthRecords_Success() throws Exception {
    mockMvc.perform(get("/api/users/{user_id}/records", testUser.getId())
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk());
  }
/*
  @Test
  public void testAddUserPrescription_Success() throws Exception {
    Prescription prescription = new Prescription();
    prescription.setMedicationName("Aspirin");
    prescription.setDosage("100mg");
    prescription.setFrequency("Once a day");

    mockMvc.perform(post("/api/users/{user_id}/records/prescriptions", testUser.getId())
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(prescription)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.medicationName").value("Aspirin"));
  }*/
  /*
  @Test
  public void testUpdateUserPrescription_Success() throws Exception {
    testPrescription.setDosage("400mg");

    mockMvc.perform(patch("/api/users/{user_id}/records/prescriptions/{prescription_id}", testUser.getId(), testPrescription.getId())
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testPrescription)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.dosage").value("400mg"));
  }*/

  @Test
  public void testRemoveUserPrescription_Success() throws Exception {
    mockMvc.perform(delete("/api/users/{user_id}/records/prescriptions/{prescription_id}", testUser.getId(), testPrescription.getId())
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk());
  }

  @Test
  public void testRequestUserPrescriptions_Success() throws Exception {
    SharingRequest sharingRequest = new SharingRequest();
    sharingRequest.setPermissionType(PermissionType.VIEW);

    mockMvc.perform(post("/api/users/{user_id}/records/request", testUser.getId())
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(sharingRequest)))
        .andExpect(status().isOk()); // Replace with the expected request ID or response body
  }

  @Test
  public void testAddUserPrescription_Success() throws Exception {
    Prescription prescription = new Prescription();
    prescription.setMedicationName("Aspirin");
    prescription.setDosage("100mg");
    prescription.setFrequency("Once a day");
    prescription.setStartDate(new Date());
    prescription.setEndDate(new Date());

    mockMvc.perform(post("/api/users/{user_id}/records/prescriptions", testUser.getId())
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(prescription)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.medicationName").value("Aspirin"));
  }
/*

  @Test
  public void testAcceptUserPrescriptionsRequest_Success() throws Exception {
    mockMvc.perform(post("/api/users/{user_id}/records/{request_id}/accept", testUser.getId(), requestId)
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk());
  }

  @Test
  public void testDenyUserPrescriptionsRequest_Success() throws Exception {
    mockMvc.perform(post("/api/users/{user_id}/records/{request_id}/deny", testUser.getId(), requestId)
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk());
  }

  @Test
  public void testRevokeUserPrescriptionAccess_Success() throws Exception {
    // Assume the request was previously accepted
    SharingPermission permission = new SharingPermission();
    permission.setId(requestId);
    permission.setOwner(testUser);
    permission.setStatus(SharingPermissionStatus.ACCEPTED);
    permission.setPermissionType(PermissionType.VIEW);
    permission.setCreatedAt(new Date());
    sharingPermissionRepository.save(permission);

    mockMvc.perform(post("/api/users/{user_id}/records/{request_id}/revoke", testUser.getId(), requestId)
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk());
  }
*/

}
