package com.bytecoders.pharmaid.repository;

import com.bytecoders.pharmaid.repository.model.Medication;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA medications repository.
 */
public interface MedicationRepository extends JpaRepository<Medication, String> {

}
