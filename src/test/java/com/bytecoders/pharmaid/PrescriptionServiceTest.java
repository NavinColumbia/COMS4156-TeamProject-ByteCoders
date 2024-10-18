package com.bytecoders.pharmaid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bytecoders.pharmaid.repository.PrescriptionRepository;
import com.bytecoders.pharmaid.repository.UserRepository;
import com.bytecoders.pharmaid.repository.model.Prescription;
import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.service.AuthorizationService;
import com.bytecoders.pharmaid.service.PrescriptionService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;


@ExtendWith(MockitoExtension.class)
class PrescriptionServiceTest {

  @Mock
  private PrescriptionRepository prescriptionRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private AuthorizationService authorizationService;

  @InjectMocks
  private PrescriptionService prescriptionService;

  @Test
  void getPrescriptionById_success() {
    Prescription prescription = new Prescription();
    prescription.setId("123");
    when(prescriptionRepository.findById("123")).thenReturn(Optional.of(prescription));
    when(authorizationService.canAccessPrescription(prescription)).thenReturn(true);

    Prescription result = prescriptionService.getPrescriptionById("123");

    assertEquals("123", result.getId());
    verify(prescriptionRepository).findById("123");
  }

  @Test
  void getPrescriptionById_accessDenied() {
    Prescription prescription = new Prescription();
    prescription.setId("123");
    when(prescriptionRepository.findById("123")).thenReturn(Optional.of(prescription));
    when(authorizationService.canAccessPrescription(prescription)).thenReturn(false);

    assertThrows(AccessDeniedException.class, () -> prescriptionService.getPrescriptionById("123"));
  }

  @Test
  void getPrescriptionsByUserId_success() {
    Prescription prescription = new Prescription();
    when(prescriptionRepository.findByUserId("user123")).thenReturn(List.of(prescription));
    when(authorizationService.canAccessPrescription(prescription)).thenReturn(true);

    List<Prescription> result = prescriptionService.getPrescriptionsByUserId("user123");

    assertEquals(1, result.size());
    verify(prescriptionRepository).findByUserId("user123");
  }

  @Test
  void createPrescription_success() {
    User user = new User();
    user.setId("user123");

    Prescription prescription = new Prescription();
    when(userRepository.findById("user123")).thenReturn(Optional.of(user));
    when(authorizationService.canModifyPrescription(prescription)).thenReturn(true);
    when(prescriptionRepository.save(prescription)).thenReturn(prescription);

    Prescription result = prescriptionService.createPrescription("user123", prescription);

    assertNotNull(result);
    verify(prescriptionRepository).save(prescription);
  }

  @Test
  void createPrescription_accessDenied() {
    User user = new User();
    user.setId("user123");

    Prescription prescription = new Prescription();
    when(userRepository.findById("user123")).thenReturn(Optional.of(user));
    when(authorizationService.canModifyPrescription(prescription)).thenReturn(false);

    assertThrows(AccessDeniedException.class,
        () -> prescriptionService.createPrescription("user123", prescription));
  }

  @Test
  void deletePrescription_success() {
    Prescription prescription = new Prescription();
    prescription.setId("123");
    when(prescriptionRepository.findById("123")).thenReturn(Optional.of(prescription));
    when(authorizationService.canModifyPrescription(prescription)).thenReturn(true);

    prescriptionService.deletePrescription("user123", "123");

    verify(prescriptionRepository).delete(prescription);
  }

  @Test
  void deletePrescription_accessDenied() {
    Prescription prescription = new Prescription();
    prescription.setId("123");
    when(prescriptionRepository.findById("123")).thenReturn(Optional.of(prescription));
    when(authorizationService.canModifyPrescription(prescription)).thenReturn(false);

    assertThrows(AccessDeniedException.class,
        () -> prescriptionService.deletePrescription("user123", "123"));
  }
}

