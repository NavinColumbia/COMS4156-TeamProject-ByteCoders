package com.bytecoders.pharmaid;

import com.bytecoders.pharmaid.repository.RefreshTokenRepository;
import com.bytecoders.pharmaid.repository.UserRepository;
import com.bytecoders.pharmaid.repository.model.RefreshToken;
import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.service.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

  @Mock
  private RefreshTokenRepository refreshTokenRepository;

  @Value("${pharmaid.app.jwtRefreshExpirationMs}")
  private Long refreshTokenDurationMs;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private RefreshTokenService refreshTokenService;

  /*
  @Test
  void createRefreshToken_success() {
    User user = new User();
    user.setId("123");

    when(userRepository.findById("123")).thenReturn(Optional.of(user));
    when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(new RefreshToken());

    RefreshToken result = refreshTokenService.createRefreshToken("123");
    result.setExpiryDate(Instant.now().plusMillis(Long.valueOf(3600000l)));

    assertNotNull(result);
    verify(refreshTokenRepository).deleteByUser(user);
    verify(refreshTokenRepository).save(any(RefreshToken.class));
  }*/

  @Test
  void verifyExpiration_tokenExpired() {
    RefreshToken expiredToken = new RefreshToken();
    expiredToken.setExpiryDate(Instant.now().minusSeconds(60));

    assertThrows(RuntimeException.class, () -> refreshTokenService.verifyExpiration(expiredToken));
  }

  // Add more tests for deleteByUserId method
}
