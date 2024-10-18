package com.bytecoders.pharmaid.service;

import com.bytecoders.pharmaid.repository.MedicationRepository;
import com.bytecoders.pharmaid.repository.model.Medication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MedicationService {

  @Autowired
  private MedicationRepository medicationRepository;

  public List<Medication> getAllMedications() {
    return medicationRepository.findAll();
  }

  public Optional<Medication> getMedication(String medicationId) {
    return medicationRepository.findByMedicationId(medicationId);
  }
}
