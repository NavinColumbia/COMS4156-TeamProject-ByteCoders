package com.bytecoders.pharmaid.service;

import com.bytecoders.pharmaid.repository.MedicationRepository;
import com.bytecoders.pharmaid.repository.model.Medication;
import com.bytecoders.pharmaid.util.ServiceUtils;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service operations around {@link com.bytecoders.pharmaid.repository.model.Medication}.
 */
@Service
public class MedicationService {

  @Autowired
  private MedicationRepository medicationRepository;

  @Autowired
  private ServiceUtils serviceUtils;

  public List<Medication> getAllMedications() {
    return medicationRepository.findAll();
  }

  /**
   * Returns a Medication or throws a ResponseStatusException.
   *
   * @param medicationId ID pertaining to the prescription
   */
  public Medication getMedication(String medicationId) {
    return serviceUtils.findEntityById(medicationId, "medication", medicationRepository);
  }
}