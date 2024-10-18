package com.bytecoders.pharmaid.repository;

import com.bytecoders.pharmaid.repository.model.Medication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicationRepository extends JpaRepository<Medication, String> {
  Optional<Medication> findByMedicationId(String medicationId);
}
