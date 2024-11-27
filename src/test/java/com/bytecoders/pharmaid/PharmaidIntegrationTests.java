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

  //  private final RestTemplate restTemplate = new RestTemplateBuilder().build();
  Map<String, String> patientUser = new HashMap<>();
  Map<String, String> healthcareUser = new HashMap<>();
  Map<String, String> firstResponderUser = new HashMap<>();

  @BeforeAll
  void setup() throws IOException {
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
  void integrationTestActiveProfile_Success() {
    assertEquals("staging", activeProfile);
    log.info("(1) FullIntegrationTests active Spring profile: {}", activeProfile);
  }

  @Test
  @Order(2)
  void helloEndpoint_Success() {
    String url = baseUrl + "/hello";
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode(), "Unexpected HTTP status from /hello");
    assertEquals("Hello :)", response.getBody(), "Unexpected response body from /hello");
    log.info("(2) Response from /hello: {}", response.getBody());
  }

  @Test
  @Order(3)
  void registerClients() {
    List<Map<String, String>> users = List.of(patientUser, healthcareUser, firstResponderUser);

    // register all entities in `users`
    for (Map<String, String> user : users) {
      String url = baseUrl + "/register";
      HttpEntity<Map<String, String>> entity = new HttpEntity<>(user, null);
      ResponseEntity<Map<String, String>> response =
          restTemplate.exchange(url, HttpMethod.POST, entity,
              new ParameterizedTypeReference<Map<String, String>>() {
              });
      Map<String, String> responseBody = (Map<String, String>) response.getBody();

      // Validate response
      assertNotNull(responseBody, "Response body should not be null for " + user.get("email"));

      assertNotNull(responseBody.get("id"), "userId should not be null for " + user.get("email"));

      assertEquals(HttpStatus.CREATED, response.getStatusCode(),
          "Login failed for " + user.get("email"));

      log.info("(3) Registered {}", user.get("userType"));
    }
  }

  @Test
  @Order(4)
  void loginClients() {
    List<Map<String, String>> users = List.of(patientUser, healthcareUser, firstResponderUser);

    // register all entities in `users`
    for (Map<String, String> user : users) {
      Map<String, String> loginRequest = new HashMap<>();
      loginRequest.put("email", user.get("email"));
      loginRequest.put("password", user.get("password"));

      String url = baseUrl + "/login";
      HttpEntity<Map<String, String>> entity = new HttpEntity<>(loginRequest, null);
      ResponseEntity<Map<String, String>> response =
          restTemplate.exchange(url, HttpMethod.POST, entity,
              new ParameterizedTypeReference<Map<String, String>>() {
              });
      Map<String, String> responseBody = (Map<String, String>) response.getBody();

      // Validate response
      assertNotNull(responseBody, "Response body should not be null for " + user.get("email"));

      assertNotNull(responseBody.get("userId"),
          "userId should not be null for " + user.get("email"));

      assertEquals(HttpStatus.OK, response.getStatusCode(),
          "Login failed for " + user.get("email"));

      user.put("userId", responseBody.get("userId"));
      user.put("JWT", responseBody.get("token"));
      log.info("(4) Logged in {}", responseBody.get("userType"));
      log.info("(4) {} JWT: {}", user.get("userType"), user.get("JWT"));
    }
  }

  @Test
  @Order(5)
  void firstResponderRequestPatientViewAccess() {
    // request access to VIEW patient prescriptions
    final String url =
        String.format("%s/users/%s/requests?requesterId=%s", baseUrl, patientUser.get("userId"),
            firstResponderUser.get("userId"));

    // create request body
    Map<String, String> requestBody = new HashMap<>();
    requestBody.put("sharePermissionType", "VIEW");

    // create headers
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + firstResponderUser.get("JWT"));
    headers.setContentType(MediaType.APPLICATION_JSON);

    // create POST request
    HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
    ResponseEntity<Map<String, Object>> response =
        restTemplate.exchange(url, HttpMethod.POST, entity,
            new ParameterizedTypeReference<Map<String, Object>>() {
            });
    Map<String, Object> responseBody = (Map<String, Object>) response.getBody();

    // add shareRequestId to user Map
    patientUser.put("FR_shareRequestId", (String) responseBody.get("id"));

    assertEquals(HttpStatus.CREATED, response.getStatusCode(),
        "VIEW access request failed for " + firstResponderUser.get("email")
            + " to request access to patient records of " + patientUser.get("email"));
    log.info("(5) Created FIRST_RESPONDER share request");
  }

  @Test
  @Order(6)
  void healthcareProviderRequestPatientEditAccess() {
    // request access to VIEW patient prescriptions
    final String url =
        String.format("%s/users/%s/requests?requesterId=%s", baseUrl, patientUser.get("userId"),
            healthcareUser.get("userId"));

    // create request body
    Map<String, String> requestBody = new HashMap<>();
    requestBody.put("sharePermissionType", "EDIT");

    // create headers
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + healthcareUser.get("JWT"));
    headers.setContentType(MediaType.APPLICATION_JSON);

    // create POST request
    HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
    ResponseEntity<Map<String, Object>> response =
        restTemplate.exchange(url, HttpMethod.POST, entity,
            new ParameterizedTypeReference<Map<String, Object>>() {
            });

    // Create response body, add shareRequestId to patientUser object
    Map<String, Object> responseBody = (Map<String, Object>) response.getBody();

    // add shareRequestId to user Map
    patientUser.put("HC_shareRequestId", (String) responseBody.get("id"));

    assertEquals(HttpStatus.CREATED, response.getStatusCode(),
        "EDIT access request failed for " + healthcareUser.get("email")
            + " to request access to patient records of " + patientUser.get("email"));

    log.info("(6) Created HEALTHCARE_PROVIDER share request");
  }

  @Test
  @Order(7)
  void createPatientPrescription() {
    final String url =
        String.format("%s/users/%s/prescriptions", baseUrl, patientUser.get("userId"));

    // create request body
    Map<String, String> requestBody = new HashMap<>();
    requestBody.put("medicationId", "5ae86c4c-b85d-4e87-9db1-24931f431372");
    requestBody.put("dosage", "1");
    requestBody.put("numOfDoses", "2");
    requestBody.put("startDate", "2024-11-26");
    requestBody.put("endDate", "2024-12-13");
    requestBody.put("isActive", "true");

    // create headers
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + patientUser.get("JWT"));
    headers.setContentType(MediaType.APPLICATION_JSON);

    // create POST request
    HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
    ResponseEntity<Map<String, Object>> response =
        restTemplate.exchange(url, HttpMethod.POST, entity,
            new ParameterizedTypeReference<Map<String, Object>>() {
            });

    assertEquals(HttpStatus.CREATED, response.getStatusCode(),
        "Create prescription failed for " + patientUser.get("email"));

    Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
    patientUser.put("prescriptionId", (String) responseBody.get("id"));
    log.info("(7) Created patient prescriptionId: {}", responseBody.get("id"));
  }

  @Test
  @Order(8)
  void acceptHealthcareShareRequest() {
    final String url =
        String.format("%s/users/%s/requests/%s/accept", baseUrl, patientUser.get("userId"),
            patientUser.get("HC_shareRequestId"));

    // create headers
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + patientUser.get("JWT"));
    headers.setContentType(MediaType.APPLICATION_JSON);

    // create POST request
    HttpEntity<Void> entity = new HttpEntity<>(null, headers);
    ResponseEntity<Map<String, Object>> response =
        restTemplate.exchange(url, HttpMethod.POST, entity,
            new ParameterizedTypeReference<Map<String, Object>>() {
            });

    assertEquals(HttpStatus.OK, response.getStatusCode(),
        "Share request accept with HEALTHCARE_PROVIDER failed for " + patientUser.get("email"));

    Map<String, Object> responseBody = response.getBody();
    log.info("(8) Accepted share request from HEALTHCARE_PROVIDER");
  }

  @Test
  @Order(9)
  void viewPrescriptionsAsFirstResponder() {
    final String url =
        String.format("%s/users/%s/prescriptions", baseUrl, patientUser.get("userId"));

    // create headers
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + firstResponderUser.get("JWT"));
    headers.setContentType(MediaType.APPLICATION_JSON);

    // create GET request
    HttpEntity<Void> entity = new HttpEntity<>(null, headers);
    ResponseEntity<List<Map<String, Object>>> response =
        restTemplate.exchange(url, HttpMethod.GET, entity,
            new ParameterizedTypeReference<List<Map<String, Object>>>() {
            });

    assertEquals(HttpStatus.OK, response.getStatusCode(),
        "Prescription VIEW via FIRST_RESPONDER failed");
    log.info("(9) Viewed prescriptions as FIRST_RESPONDER");
  }

  @Test
  @Order(10)
  void editPrescriptionsAsPatient() {
    final String url =
        String.format("%s/users/%s/prescriptions/%s", baseUrl, patientUser.get("userId"),
            patientUser.get("prescriptionId"));

    // request body
    Map<String, String> requestBody = new HashMap<>();
    requestBody.put("endDate", "2025-01-20");

    // create headers
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + patientUser.get("JWT"));
    headers.setContentType(MediaType.APPLICATION_JSON);

    // create PATCH request
    HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
    ResponseEntity<Map<String, Object>> response =
        restTemplate.exchange(url, HttpMethod.PATCH, entity,
            new ParameterizedTypeReference<Map<String, Object>>() {
            });

    assertEquals(HttpStatus.OK, response.getStatusCode(),
        "Prescription failed to update via PATIENT");
    log.info("(10) Updated prescriptions as PATIENT");
  }

  @Test
  @Order(11)
  void editPrescriptionsAsHealthcareProvider() {
    final String url =
        String.format("%s/users/%s/prescriptions/%s", baseUrl, patientUser.get("userId"),
            patientUser.get("prescriptionId"));

    // request body
    Map<String, String> requestBody = new HashMap<>();
    requestBody.put("isActive", "false");

    // create headers
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + healthcareUser.get("JWT"));
    headers.setContentType(MediaType.APPLICATION_JSON);

    // create PATCH request
    HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
    ResponseEntity<Map<String, Object>> response =
        restTemplate.exchange(url, HttpMethod.PATCH, entity,
            new ParameterizedTypeReference<Map<String, Object>>() {
            });

    assertEquals(HttpStatus.OK, response.getStatusCode(),
        "Prescription failed to update via PATIENT");
    log.info("(11) Updated prescriptions as HEALTHCARE_PROVIDER");
  }

  @Test
  @Order(12)
  void revokePrescriptionAccessHealthcareProvider() {
    final String url =
        String.format("%s/users/%s/requests/%s/revoke", baseUrl, patientUser.get("userId"),
            patientUser.get("HC_shareRequestId"));

    // create headers
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + patientUser.get("JWT"));
    headers.setContentType(MediaType.APPLICATION_JSON);

    // create POST request
    HttpEntity<Void> entity = new HttpEntity<>(null, headers);
    ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode(),
        "Access failed to revoke from HEALTHCARE_PROVIDER to PATIENT");
    log.info("(12) Revoked EDIT access from HEALTHCARE_PROVIDER to PATIENT");
  }

  @Test
  @Order(13)
  void revokePrescriptionAccessFirstResponder() {
    final String url =
        String.format("%s/users/%s/requests/%s/revoke", baseUrl, patientUser.get("userId"),
            patientUser.get("FR_shareRequestId"));

    // create headers
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + patientUser.get("JWT"));
    headers.setContentType(MediaType.APPLICATION_JSON);

    // create POST request
    HttpEntity<Void> entity = new HttpEntity<>(null, headers);
    ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode(),
        "Access failed to revoke from FIRST_RESPONDER to PATIENT");
    log.info("(13) Revoked VIEW access from FIRST_RESPONDER to PATIENT");
  }

  @Test
  @Order(14)
  void deleteClients() {
    List<Map<String, String>> users = List.of(patientUser, healthcareUser, firstResponderUser);

    // register all entities in `users`
    for (Map<String, String> user : users) {
      final String url = String.format("%s/users/%s", baseUrl, user.get("userId"));

      // headers
      HttpHeaders headers = new HttpHeaders();
      headers.set("Authorization", "Bearer " + user.get("JWT"));
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<Void> entity = new HttpEntity<>(null, headers);

      // response
      ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity,
          new ParameterizedTypeReference<String>() {
          });
      String responseBody = (String) response.getBody();

      // asserts
      assertEquals("User deleted successfully", responseBody);
      assertEquals(HttpStatus.OK, response.getStatusCode(),
          "Client deletion failed for " + user.get("email"));
      log.info("(14) Deleted client: {}", user.get("email"));
    }
  }
}
