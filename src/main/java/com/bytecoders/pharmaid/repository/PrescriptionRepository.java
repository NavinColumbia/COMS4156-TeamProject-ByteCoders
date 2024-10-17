package com.bytecoders.pharmaid.repository;

import com.bytecoders.pharmaid.repository.model.Prescription;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA prescriptions repository.
 */
public interface PrescriptionRepository extends JpaRepository<Prescription, String> {

  Optional<Prescription> findByUserId(String userId);
}