package com.bytecoders.pharmaid.repository;


import com.bytecoders.pharmaid.repository.model.RefreshToken;
import com.bytecoders.pharmaid.repository.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


/**
 *
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByToken(String token);

  @Modifying
  @Query("DELETE FROM RefreshToken r WHERE r.user = :user")
  int deleteByUser(@Param("user") User user);


}