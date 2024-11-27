package com.bytecoders.pharmaid;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;

@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PharmaidIntegrationTests {

  @Value("${spring.profiles.active}")
  private String activeProfile;

  @Value("${pharmaid.staging.base-url}")
  private String baseUrl;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  private final RestTemplate restTemplate = createRestTemplate();

  private RestTemplate createRestTemplate() {
    CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpComponentsClientHttpRequestFactory requestFactory =
        new HttpComponentsClientHttpRequestFactory();
    return new RestTemplate(requestFactory);
  }

  Map<String, String> patientUser = new HashMap<>();
  Map<String, String> healthcareUser = new HashMap<>();
  Map<String, String> firstResponderUser = new HashMap<>();

  @BeforeAll
  void setup() throws IOException {
    // (0) setup
    jdbcTemplate.execute("DELETE FROM prescriptions;");
    jdbcTemplate.execute("DELETE FROM shared_permissions;");
    jdbcTemplate.execute("DELETE FROM users;");
    jdbcTemplate.execute("DELETE FROM medications;");
    log.info("Removed all prior entries from pharmaid-staging db...");

    // setup medications table
    Resource resource = new ClassPathResource("sql/medications_seed.sql");
    String sql = Files.readString(resource.getFile().toPath());
    jdbcTemplate.execute(sql);
    log.info("Medications table populated successfully.");

    // set Patient User variables
    patientUser.put("email", "patient@example.com");
    patientUser.put("password", "patientPassword");
    patientUser.put("userType", "PATIENT");

    // set Healthcare User variables
    healthcareUser.put("email", "healthcare@example.com");
    healthcareUser.put("password", "healthcarePassword");
    healthcareUser.put("userType", "HEALTHCARE_PROVIDER");

    // set firstResponder User variables
    firstResponderUser.put("email", "responder@example.com");
    firstResponderUser.put("password", "firstResponderPassword");
    firstResponderUser.put("userType", "FIRST_RESPONDER");
  }

  @Test
  @Order(1)
  void fullIntegrationTest() throws InterruptedException {

    // (1) TEST ACTIVE PROFILE
    Thread.sleep(1000);
    assertEquals("staging", activeProfile);
    log.info("(1) FullIntegrationTests active Spring profile: {}", activeProfile);

    // (2) TEST HELLO ENDPOINT
    ResponseEntity<String> helloResponse =
        restTemplate.getForEntity(baseUrl + "/hello", String.class);
    assertEquals(HttpStatus.OK, helloResponse.getStatusCode(),
        "Unexpected HTTP status from /hello");
    assertEquals("Hello :)", helloResponse.getBody(), "Unexpected response body from /hello");
    log.info("(2) Response from /hello: {}", helloResponse.getBody());

    // (3) REGISTER CLIENT
    List<Map<String, String>> users = List.of(patientUser, healthcareUser, firstResponderUser);
    final String registerUrl = baseUrl + "/register";
    for (Map<String, String> user : users) {
      Thread.sleep(1000);

      // request body and (null) headers
      HttpEntity<Map<String, String>> registerEntity = new HttpEntity<>(user, null);

      // register request response
      ResponseEntity<Map<String, String>> registerResponse =
          restTemplate.exchange(registerUrl, HttpMethod.POST, registerEntity,
              new ParameterizedTypeReference<Map<String, String>>() {
              });

      // assertions
      Map<String, String> registerResponseBody = (Map<String, String>) registerResponse.getBody();
      assertNotNull(registerResponseBody,
          "Register client response body should not be null for " + user.get("email"));
      assertNotNull(registerResponseBody.get("id"),
          "Register client userId should not be null for " + user.get("email"));
      assertEquals(HttpStatus.CREATED, registerResponse.getStatusCode(),
          "Register client failed for " + user.get("email"));

      log.info("(3) Registered {}", user.get("userType"));
    }

    // (4) LOGIN CLIENTS
    final String loginUrl = baseUrl + "/login";
    for (Map<String, String> user : users) {
      Thread.sleep(1000);

      // create login request body
      Map<String, String> loginRequest = new HashMap<>();
      loginRequest.put("email", user.get("email"));
      loginRequest.put("password", user.get("password"));

      // create login request
      HttpEntity<Map<String, String>> loginEntity = new HttpEntity<>(loginRequest, null);
      ResponseEntity<Map<String, String>> response =
          restTemplate.exchange(loginUrl, HttpMethod.POST, loginEntity,
              new ParameterizedTypeReference<Map<String, String>>() {
              });

      // assertions
      Map<String, String> responseBody = (Map<String, String>) response.getBody();
      assertNotNull(responseBody, "Response body should not be null for " + user.get("email"));
      assertNotNull(responseBody.get("userId"),
          "userId should not be null for " + user.get("email"));
      assertEquals(HttpStatus.OK, response.getStatusCode(),
          "Login failed for " + user.get("email"));

      // store userId and JWT in user object
      user.put("userId", responseBody.get("userId"));
      user.put("JWT", responseBody.get("token"));
      log.info("(4) Logged in {}", user.get("userType"));
    }

    // (5) FIRST_RESPONDER VIEW ACCESS
    Thread.sleep(1000);
    final String viewRequestUrl =
        String.format("%s/users/%s/requests?requesterId=%s", baseUrl, patientUser.get("userId"),
            firstResponderUser.get("userId"));

    // request body
    Map<String, String> viewRequestBody = new HashMap<>();
    viewRequestBody.put("sharePermissionType", "VIEW");

    // create headers
    HttpHeaders viewRequestHeaders = new HttpHeaders();
    viewRequestHeaders.set("Authorization", "Bearer " + firstResponderUser.get("JWT"));
    viewRequestHeaders.setContentType(MediaType.APPLICATION_JSON);

    // request body and headers
    HttpEntity<Map<String, String>> viewRequestEntity =
        new HttpEntity<>(viewRequestBody, viewRequestHeaders);

    // generate request
    ResponseEntity<Map<String, Object>> viewRequestResponse =
        restTemplate.exchange(viewRequestUrl, HttpMethod.POST, viewRequestEntity,
            new ParameterizedTypeReference<Map<String, Object>>() {
            });

    // add shareRequestId to user Map
    Map<String, Object> viewResponseBody = (Map<String, Object>) viewRequestResponse.getBody();
    patientUser.put("FR_shareRequestId", (String) viewResponseBody.get("id"));

    // assertions
    assertEquals(HttpStatus.CREATED, viewRequestResponse.getStatusCode(),
        "VIEW access request failed for " + firstResponderUser.get("email")
            + " to request access to patient records of " + patientUser.get("email"));
    log.info("(5) Created FIRST_RESPONDER share request");

    // (6) HEALTHCARE_PROVIDER EDIT ACCESS
    Thread.sleep(1000);
    final String editAccessUrl =
        String.format("%s/users/%s/requests?requesterId=%s", baseUrl, patientUser.get("userId"),
            healthcareUser.get("userId"));

    // create request body
    Map<String, String> editRequestBody = new HashMap<>();
    editRequestBody.put("sharePermissionType", "EDIT");

    // create headers
    HttpHeaders editRequestHeaders = new HttpHeaders();
    editRequestHeaders.set("Authorization", "Bearer " + healthcareUser.get("JWT"));
    editRequestHeaders.setContentType(MediaType.APPLICATION_JSON);

    // create POST request
    HttpEntity<Map<String, String>> editRequestEntity =
        new HttpEntity<>(editRequestBody, editRequestHeaders);
    ResponseEntity<Map<String, Object>> editRequestResponse =
        restTemplate.exchange(editAccessUrl, HttpMethod.POST, editRequestEntity,
            new ParameterizedTypeReference<Map<String, Object>>() {
            });

    // Create response body, add shareRequestId to patientUser object
    Map<String, Object> editRequestResponseBody =
        (Map<String, Object>) editRequestResponse.getBody();

    // add shareRequestId to user Map
    patientUser.put("HC_shareRequestId", (String) editRequestResponseBody.get("id"));

    assertEquals(HttpStatus.CREATED, editRequestResponse.getStatusCode(),
        "EDIT access request failed for " + healthcareUser.get("email")
            + " to request access to patient records of " + patientUser.get("email"));

    log.info("(6) Created HEALTHCARE_PROVIDER share request");

    // (7) PATIENT CREATE PRESCRIPTION
    Thread.sleep(1000);
    final String prescriptionUrl =
        String.format("%s/users/%s/prescriptions", baseUrl, patientUser.get("userId"));

    // create request body
    Map<String, String> prescriptionRequestBody = new HashMap<>();
    prescriptionRequestBody.put("medicationId", "5ae86c4c-b85d-4e87-9db1-24931f431372");
    prescriptionRequestBody.put("dosage", "1");
    prescriptionRequestBody.put("numOfDoses", "2");
    prescriptionRequestBody.put("startDate", "2024-11-26");
    prescriptionRequestBody.put("endDate", "2024-12-13");
    prescriptionRequestBody.put("isActive", "true");

    // create headers
    HttpHeaders prescriptionHeaders = new HttpHeaders();
    prescriptionHeaders.set("Authorization", "Bearer " + patientUser.get("JWT"));
    prescriptionHeaders.setContentType(MediaType.APPLICATION_JSON);

    // create POST request
    HttpEntity<Map<String, String>> prescriptionEntity =
        new HttpEntity<>(prescriptionRequestBody, prescriptionHeaders);

    ResponseEntity<Map<String, Object>> prescriptionResponse =
        restTemplate.exchange(prescriptionUrl, HttpMethod.POST, prescriptionEntity,
            new ParameterizedTypeReference<Map<String, Object>>() {
            });

    assertEquals(HttpStatus.CREATED, prescriptionResponse.getStatusCode(),
        "Create prescription failed for " + patientUser.get("email"));

    Map<String, Object> prescriptionResponseBody =
        (Map<String, Object>) prescriptionResponse.getBody();
    patientUser.put("prescriptionId", (String) prescriptionResponseBody.get("id"));
    log.info("(7) Created PATIENT prescription");

    // (8) ACCEPT HEALTHCARE_PROVIDER SHARE REQUEST
    Thread.sleep(1000);
    final String acceptShareUrlHealth =
        String.format("%s/users/%s/requests/%s/accept", baseUrl, patientUser.get("userId"),
            patientUser.get("HC_shareRequestId"));

    // create headers
    HttpHeaders acceptHealthHeaders = new HttpHeaders();
    acceptHealthHeaders.set("Authorization", "Bearer " + patientUser.get("JWT"));
    acceptHealthHeaders.setContentType(MediaType.APPLICATION_JSON);

    // create POST request
    HttpEntity<Void> acceptHealthEntity = new HttpEntity<>(null, acceptHealthHeaders);

    ResponseEntity<Map<String, Object>> acceptHealthResponse =
        restTemplate.exchange(acceptShareUrlHealth, HttpMethod.POST, acceptHealthEntity,
            new ParameterizedTypeReference<Map<String, Object>>() {
            });

    // assertions
    assertEquals(HttpStatus.OK, acceptHealthResponse.getStatusCode(),
        "Share request accept with HEALTHCARE_PROVIDER failed for " + patientUser.get("email"));
    log.info("(8) Accepted share request from HEALTHCARE_PROVIDER");

    // (9) FIRST_RESPONDER VIEW PRESCRIPTIONS
    Thread.sleep(1000);
    final String viewPrescriptionsUrl =
        String.format("%s/users/%s/prescriptions", baseUrl, patientUser.get("userId"));

    // create headers
    HttpHeaders viewPrescriptionsHeaders = new HttpHeaders();
    viewPrescriptionsHeaders.set("Authorization", "Bearer " + firstResponderUser.get("JWT"));
    viewPrescriptionsHeaders.setContentType(MediaType.APPLICATION_JSON);

    // GET prescriptions
    HttpEntity<Void> viewPrescriptionsEntity = new HttpEntity<>(null, viewPrescriptionsHeaders);

    ResponseEntity<List<Map<String, Object>>> viewPrescriptionsResponse =
        restTemplate.exchange(viewPrescriptionsUrl, HttpMethod.GET, viewPrescriptionsEntity,
            new ParameterizedTypeReference<List<Map<String, Object>>>() {
            });

    // assertions
    assertEquals(HttpStatus.OK, viewPrescriptionsResponse.getStatusCode(),
        "Prescription VIEW via FIRST_RESPONDER failed");
    log.info("(9) Viewed prescriptions as FIRST_RESPONDER");

    // (10) PATIENT EDIT PRESCRIPTIONS
    Thread.sleep(1000);
    final String ediPrescriptionsPatientUrl =
        String.format("%s/users/%s/prescriptions/%s", baseUrl, patientUser.get("userId"),
            patientUser.get("prescriptionId"));

    // request body
    Map<String, String> editPrescriptionsPatientRequestBody = new HashMap<>();
    editPrescriptionsPatientRequestBody.put("endDate", "2025-01-20");

    // create headers
    HttpHeaders editPrescriptionsPatientHeaders = new HttpHeaders();
    editPrescriptionsPatientHeaders.set("Authorization", "Bearer " + patientUser.get("JWT"));
    editPrescriptionsPatientHeaders.setContentType(MediaType.APPLICATION_JSON);

    // PATCH prescriptions as PATIENT
    HttpEntity<Map<String, String>> editPrescriptionsPatientEntity =
        new HttpEntity<>(editPrescriptionsPatientRequestBody, editPrescriptionsPatientHeaders);

    ResponseEntity<Map<String, Object>> editPrescriptionsPatientResponse =
        restTemplate.exchange(ediPrescriptionsPatientUrl, HttpMethod.PATCH,
            editPrescriptionsPatientEntity,
            new ParameterizedTypeReference<Map<String, Object>>() {
            });

    assertEquals(HttpStatus.OK, editPrescriptionsPatientResponse.getStatusCode(),
        "Prescription failed to update via PATIENT");
    log.info("(10) Updated prescriptions as PATIENT");

    // (11) HEALTHCARE_PROVIDER EDIT PRESCRIPTIONS
    Thread.sleep(1000);
    final String editPrescriptionsHealthcareUrl =
        String.format("%s/users/%s/prescriptions/%s", baseUrl, patientUser.get("userId"),
            patientUser.get("prescriptionId"));

    // request body
    Map<String, String> editPrescriptionsHealthcareRequestBody = new HashMap<>();
    editPrescriptionsHealthcareRequestBody.put("isActive", "false");

    // create headers
    HttpHeaders editPrescriptionsHealthcareHeaders = new HttpHeaders();
    editPrescriptionsHealthcareHeaders.set("Authorization",
        "Bearer " + healthcareUser.get("JWT"));
    editPrescriptionsHealthcareHeaders.setContentType(MediaType.APPLICATION_JSON);

    // PATCH prescriptions as HEALTHCARE_PROVIDER
    HttpEntity<Map<String, String>> editPrescriptionsHealthcareEntity =
        new HttpEntity<>(editPrescriptionsHealthcareRequestBody,
            editPrescriptionsHealthcareHeaders);

    ResponseEntity<Map<String, Object>> editPrescriptionsHealthcareResponse =
        restTemplate.exchange(editPrescriptionsHealthcareUrl, HttpMethod.PATCH,
            editPrescriptionsHealthcareEntity,
            new ParameterizedTypeReference<Map<String, Object>>() {
            });

    assertEquals(HttpStatus.OK, editPrescriptionsHealthcareResponse.getStatusCode(),
        "Prescription failed to update via PATIENT");
    log.info("(11) Updated prescriptions as HEALTHCARE_PROVIDER");

    // (12) REVOKE ACCESS HEALTHCARE_PROVIDER
    Thread.sleep(1000);
    final String revokeAccessHealthcareUrl =
        String.format("%s/users/%s/requests/%s/revoke", baseUrl, patientUser.get("userId"),
            patientUser.get("HC_shareRequestId"));

    // create headers
    HttpHeaders revokeAccessHealthcareHeaders = new HttpHeaders();
    revokeAccessHealthcareHeaders.set("Authorization", "Bearer " + patientUser.get("JWT"));
    revokeAccessHealthcareHeaders.setContentType(MediaType.APPLICATION_JSON);

    // POST revoke access
    HttpEntity<Void> revokeAccessHealthcareEntity =
        new HttpEntity<>(null, revokeAccessHealthcareHeaders);

    ResponseEntity<String> revokeAccessHealthcareResponse =
        restTemplate.postForEntity(revokeAccessHealthcareUrl, revokeAccessHealthcareEntity,
            String.class);

    assertEquals(HttpStatus.OK, revokeAccessHealthcareResponse.getStatusCode(),
        "Access failed to revoke from HEALTHCARE_PROVIDER to PATIENT");
    log.info("(12) Revoked EDIT access from HEALTHCARE_PROVIDER to PATIENT");

    // (13) REVOKE ACCESS FIRST_RESPONDER
    Thread.sleep(1000);
    final String revokeAccessResponderUrl =
        String.format("%s/users/%s/requests/%s/revoke", baseUrl, patientUser.get("userId"),
            patientUser.get("FR_shareRequestId"));

    // create headers
    HttpHeaders revokeAccessResponderHeaders = new HttpHeaders();
    revokeAccessResponderHeaders.set("Authorization", "Bearer " + patientUser.get("JWT"));
    revokeAccessResponderHeaders.setContentType(MediaType.APPLICATION_JSON);

    // POST revoke access
    HttpEntity<Void> revokeAccessResponderEntity =
        new HttpEntity<>(null, revokeAccessResponderHeaders);

    ResponseEntity<String> revokeAccessResponderResponse =
        restTemplate.postForEntity(revokeAccessResponderUrl, revokeAccessResponderEntity,
            String.class);

    assertEquals(HttpStatus.OK, revokeAccessResponderResponse.getStatusCode(),
        "Access failed to revoke from FIRST_RESPONDER to PATIENT");
    log.info("(13) Revoked VIEW access from FIRST_RESPONDER to PATIENT");

    // (14) DELETE CLIENTS
    List<Map<String, String>> deleteUsers =
        List.of(patientUser, healthcareUser, firstResponderUser);

    // register all entities in `users`
    for (Map<String, String> user : deleteUsers) {
      Thread.sleep(1000);
      final String deleteUrl = String.format("%s/users/%s", baseUrl, user.get("userId"));

      // headers
      HttpHeaders deleteHeaders = new HttpHeaders();
      deleteHeaders.set("Authorization", "Bearer " + user.get("JWT"));
      deleteHeaders.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<Void> deleteEntity = new HttpEntity<>(null, deleteHeaders);

      // response
      ResponseEntity<String> deleteResponse =
          restTemplate.exchange(deleteUrl, HttpMethod.DELETE, deleteEntity,
              new ParameterizedTypeReference<String>() {
              });
      String responseBody = (String) deleteResponse.getBody();

      // asserts
      assertEquals("User deleted successfully", responseBody);
      assertEquals(HttpStatus.OK, deleteResponse.getStatusCode(),
          "Client deletion failed for " + user.get("email"));
      log.info("(14) Deleted client: {}", user.get("email"));
    }
  }
}
