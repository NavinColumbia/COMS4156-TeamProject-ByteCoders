package com.bytecoders.pharmaid;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bytecoders.pharmaid.repository.RefreshTokenRepository;
import com.bytecoders.pharmaid.repository.UserRepository;
import com.bytecoders.pharmaid.repository.model.RefreshToken;
import com.bytecoders.pharmaid.service.RefreshTokenService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 */
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

  /**
   *
   */
  @Test
  void verifyExpiration_tokenExpired() {
    RefreshToken expiredToken = new RefreshToken();
    expiredToken.setExpiryDate(Instant.now().minusSeconds(60));

    assertThrows(RuntimeException.class, () -> refreshTokenService.verifyExpiration(expiredToken));
  }

}
