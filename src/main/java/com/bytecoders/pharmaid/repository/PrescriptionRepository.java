package com.bytecoders.pharmaid.repository;

import com.bytecoders.pharmaid.repository.model.Prescription;
import com.bytecoders.pharmaid.repository.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA prescriptions repository.
 */


@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, String> {
  List<Prescription> findByUser(User user);
}