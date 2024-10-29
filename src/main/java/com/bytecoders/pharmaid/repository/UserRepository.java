package com.bytecoders.pharmaid.repository;

import com.bytecoders.pharmaid.repository.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA user repository. */
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);

  Optional<User> findById(String userId);
}
