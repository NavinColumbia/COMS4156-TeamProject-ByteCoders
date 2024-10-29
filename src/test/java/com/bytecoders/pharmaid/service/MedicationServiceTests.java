package com.bytecoders.pharmaid.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.bytecoders.pharmaid.repository.MedicationRepository;
import com.bytecoders.pharmaid.repository.model.Medication;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Tests for {@link com.bytecoders.pharmaid.service.MedicationService}. */
@ExtendWith(MockitoExtension.class)
public class MedicationServiceTests {
  @Mock private MedicationRepository medicationRepository;

  @InjectMocks private MedicationService medicationService = new MedicationService();

  @Test
  public void testGetAllMedications() {
    final Medication mockMedication = new Medication();
    mockMedication.setMedicationId("someMedicationId");
    mockMedication.setMedicationName("Ibuprofen");

    when(medicationRepository.findAll()).thenReturn(List.of(mockMedication));

    final List<Medication> medications = medicationService.getAllMedications();
    assertEquals(medications, List.of(mockMedication));
  }
}
