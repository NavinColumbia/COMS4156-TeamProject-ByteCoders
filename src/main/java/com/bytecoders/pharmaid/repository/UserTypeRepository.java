package com.bytecoders.pharmaid.repository;


import com.bytecoders.pharmaid.repository.model.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserTypeRepository extends JpaRepository<UserType, String> {
  Optional<UserType> findByName(String name);
  boolean existsByName(String name);
}