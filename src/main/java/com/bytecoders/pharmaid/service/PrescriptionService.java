package com.bytecoders.pharmaid.service;

import com.bytecoders.pharmaid.repository.PrescriptionRepository;
import com.bytecoders.pharmaid.repository.model.Prescription;
import com.bytecoders.pharmaid.util.JwtUtils;
import com.bytecoders.pharmaid.util.ServiceUtils;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Service operations around {@link com.bytecoders.pharmaid.repository.model.Prescription}.
 */
@Slf4j
@Service
public class PrescriptionService {

  @Autowired
  private PrescriptionRepository prescriptionRepository;

  @Autowired
  private SharedPermissionValidator permissionValidator;

  @Autowired
  private JwtUtils jwtUtils;

  @Autowired
  private ServiceUtils serviceUtils;

  /**
   * Create a new prescription.
   *
   * @param prescription the prescription to create
   * @return Prescription the newly created prescription
   */
  public Prescription createPrescription(Prescription prescription) {
    // check if user has permissions to create a prescription
    permissionValidator.validateEditPermission(jwtUtils.getLoggedInUserId(),
        prescription.getUser().getId());

    return prescriptionRepository.save(prescription);
  }

  /**
   * Returns a Prescription or throws a ResponseStatusException.
   *
   * @param prescriptionId ID pertaining to the prescription
   */
  public Prescription getPrescription(String prescriptionId) {
    return serviceUtils.findEntityById(prescriptionId, "prescription", prescriptionRepository);
  }

  /**
   * Delete a prescription by its ID.
   *
   * @param prescriptionId the prescription ID
   */
  public void deletePrescription(String prescriptionId) {
    prescriptionRepository.deleteById(prescriptionId);
  }

  /**
   * Retrieve a list of provided user's prescriptions.
   *
   * @param userId the user ID
   */
  public List<Prescription> getPrescriptionsForUser(String userId) {
    // check if user has permissions to view prescriptions
    permissionValidator.validateViewPermission(jwtUtils.getLoggedInUserId(), userId);

    return prescriptionRepository.findAllByUserId(userId);
  }
}