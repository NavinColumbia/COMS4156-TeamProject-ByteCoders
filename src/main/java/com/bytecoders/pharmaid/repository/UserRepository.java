package com.bytecoders.pharmaid.repository;

import com.bytecoders.pharmaid.repository.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

  Optional<User> findByEmail(String email);

  Optional<User> findById(String id);

  boolean existsByEmail(String email);
}