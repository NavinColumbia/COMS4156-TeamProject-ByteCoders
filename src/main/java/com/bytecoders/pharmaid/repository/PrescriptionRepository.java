package com.bytecoders.pharmaid.repository;

import com.bytecoders.pharmaid.repository.model.Prescription;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 */
@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, String> {

  //List<Prescription> findByUser(User user);
  // Method to find prescriptions by User ID
  List<Prescription> findByUserId(String userId);

  @Modifying
  @Transactional
  @Query("DELETE FROM Prescription p WHERE p.user.id = :userId")
  void deleteByUserId(@Param("userId") String userId);
}