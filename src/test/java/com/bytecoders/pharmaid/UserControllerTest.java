package com.bytecoders.pharmaid;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bytecoders.pharmaid.repository.PrescriptionRepository;
import com.bytecoders.pharmaid.repository.RefreshTokenRepository;
import com.bytecoders.pharmaid.repository.UserRepository;
import com.bytecoders.pharmaid.repository.model.PermissionType;
import com.bytecoders.pharmaid.repository.model.Prescription;
import com.bytecoders.pharmaid.repository.model.SharingRequest;
import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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

  /**
   *
   */
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


  }

  /**
   *
   * @throws Exception
   */
  @Test
  public void testGetUserAccount_Success() throws Exception {
    mockMvc.perform(get("/api/users/{user_id}", testUser.getId())
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(testUser.getEmail()));
  }

  /**
   *
   * @throws Exception
   */
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

  /**
   *
   * @throws Exception
   */
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

  /**
   *
   * @throws Exception
   */
  @Test
  public void testDeleteUserAccount_Success() throws Exception {
    mockMvc.perform(delete("/api/users/{user_id}", testUser.getId())
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk());
  }

  /**
   *
   * @throws Exception
   */
  @Test
  public void testGetUserHealthRecords_Success() throws Exception {
    mockMvc.perform(get("/api/users/{user_id}/records", testUser.getId())
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk());
  }

  /**
   *
   * @throws Exception
   */
  @Test
  public void testRemoveUserPrescription_Success() throws Exception {
    mockMvc.perform(
            delete("/api/users/{user_id}/records/prescriptions/{prescription_id}", testUser.getId(),
                testPrescription.getId())
                .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk());
  }

  /**
   *
   * @throws Exception
   */
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

  /**
   *
   * @throws Exception
   */
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

}
