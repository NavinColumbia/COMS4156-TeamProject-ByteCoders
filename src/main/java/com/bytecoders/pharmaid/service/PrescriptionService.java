package com.bytecoders.pharmaid.service;

import com.bytecoders.pharmaid.repository.PrescriptionRepository;
import com.bytecoders.pharmaid.repository.UserRepository;
import com.bytecoders.pharmaid.repository.model.Prescription;
import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.service.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrescriptionService {

  @Autowired
  private PrescriptionRepository prescriptionRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private AuthorizationService authorizationService;

  @Transactional(readOnly = true)
  public Prescription getPrescriptionById(String prescriptionId) {
    Prescription prescription = prescriptionRepository.findById(prescriptionId)
        .orElseThrow(
            () -> new RuntimeException("Prescription not found with id: " + prescriptionId));

    if (!authorizationService.canAccessPrescription(prescription)) {
      throw new AccessDeniedException("You don't have permission to access this prescription");
    }

    return prescription;
  }

  @Transactional(readOnly = true)
  public List<Prescription> getPrescriptionsByUserId(String userId) {
    List<Prescription> prescriptions = prescriptionRepository.findByUserId(userId);

    return prescriptions.stream()
        .filter(authorizationService::canAccessPrescription)
        .collect(Collectors.toList());
  }

  @Transactional
  public Prescription createPrescription(String userId, Prescription prescription) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

    prescription.setUser(user);

    if (!authorizationService.canModifyPrescription(prescription)) {
      throw new AccessDeniedException(
          "You don't have permission to create a prescription for this user");
    }

    return prescriptionRepository.save(prescription);
  }

  @Transactional
  public Prescription updatePrescription(String userId, String prescriptionId,
      Prescription updatedPrescription) {
    Prescription existingPrescription = prescriptionRepository.findById(prescriptionId)
        .orElseThrow(
            () -> new RuntimeException("Prescription not found with id: " + prescriptionId));

    if (!authorizationService.canModifyPrescription(existingPrescription)) {
      throw new AccessDeniedException("You don't have permission to modify this prescription");
    }

    existingPrescription.setMedicationName(updatedPrescription.getMedicationName());
    existingPrescription.setDosage(updatedPrescription.getDosage());
    existingPrescription.setFrequency(updatedPrescription.getFrequency());
    existingPrescription.setStartDate(updatedPrescription.getStartDate());
    existingPrescription.setEndDate(updatedPrescription.getEndDate());

    return prescriptionRepository.save(existingPrescription);
  }

  @Transactional
  public void deletePrescription(String userId, String prescriptionId) {
    Prescription prescription = prescriptionRepository.findById(prescriptionId)
        .orElseThrow(
            () -> new RuntimeException("Prescription not found with id: " + prescriptionId));

    if (!authorizationService.canModifyPrescription(prescription)) {
      throw new AccessDeniedException("You don't have permission to delete this prescription");
    }

    prescriptionRepository.delete(prescription);
  }
}
