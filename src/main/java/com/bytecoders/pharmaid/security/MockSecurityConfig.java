package com.bytecoders.pharmaid.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Test Security Config. To be removed after JWT register/login done.
 */
@Profile("test")
@Configuration
public class MockSecurityConfig {

  /**
   * Filter chain Allows access to all endpoints for test profile.
   *
   * @param http HttpSecurity object
   * @return SecurityFilterChain
   */
  @Bean
  public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth ->
            auth.anyRequest().permitAll());
    return http.build();
  }
}
