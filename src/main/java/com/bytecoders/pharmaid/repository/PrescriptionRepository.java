package com.bytecoders.pharmaid.repository;

import com.bytecoders.pharmaid.repository.model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * JPA prescriptions repository.
 */
public interface PrescriptionRepository extends JpaRepository<Prescription, String> {

  List<Prescription> findAllByUserId(String userId);
}