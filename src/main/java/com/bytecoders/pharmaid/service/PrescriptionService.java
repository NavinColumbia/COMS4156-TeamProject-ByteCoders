package com.bytecoders.pharmaid.service;

import com.bytecoders.pharmaid.repository.PrescriptionRepository;
import com.bytecoders.pharmaid.repository.model.Prescription;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Service operations around {@link com.bytecoders.pharmaid.repository.model.Prescription}.
 */
@Service
public class PrescriptionService {

  @Autowired
  private PrescriptionRepository prescriptionRepository;

  /**
   * Create a new prescription.
   *
   * @param prescription the prescription to create
   * @return Prescription the newly created prescription
   */
  public Prescription createPrescription(Prescription prescription) {
    return prescriptionRepository.save(prescription);
  }

  /**
   * Get a prescription by its ID.
   *
   * @param prescriptionId the prescription ID
   * @return the prescription, if found
   */
  public Optional<Prescription> getPrescriptionById(String prescriptionId) {
    return prescriptionRepository.findById(prescriptionId);
  }

  /**
   * Get all prescriptions for a specific user.
   *
   * @param userId the user ID
   * @return the user's list of prescriptions
   */
  public List<Prescription> getPrescriptionsByUserId(String userId) {
    // TODO: need to determine how to return prescriptions
    return prescriptionRepository.findByUserId(userId).stream().toList();
  }

  /**
   * Delete a prescription by its ID.
   *
   * @param prescriptionId the prescription ID
   */
  public void deletePrescription(String prescriptionId) {
    prescriptionRepository.deleteById(prescriptionId);
  }
}