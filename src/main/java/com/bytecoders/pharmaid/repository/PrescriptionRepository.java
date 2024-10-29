package com.bytecoders.pharmaid.repository;

import com.bytecoders.pharmaid.repository.model.Prescription;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA prescriptions repository. */
public interface PrescriptionRepository extends JpaRepository<Prescription, String> {

  List<Prescription> findAllByUserId(String userId);
}
