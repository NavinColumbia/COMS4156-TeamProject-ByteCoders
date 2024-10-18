package com.bytecoders.pharmaid;

import com.bytecoders.pharmaid.util.PasswordUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AppConfig with custom beans to inject.
 */
@Configuration
public class AppConfig {
  @Bean
  public PasswordUtils passwordUtils() {
    return new PasswordUtils();
  }
}
