package com.bytecoders.pharmaid;

import com.bytecoders.pharmaid.security.JwtRequestFilter;
import com.bytecoders.pharmaid.util.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/** Config class for PharmaId security and password management settings. */
@Configuration
@EnableWebSecurity
public class AppConfig {

  @Autowired
  private JwtRequestFilter jwtRequestFilter;

  @Bean
  public PasswordUtils passwordUtils() {
    return new PasswordUtils();
  }

  /**
   * Configure the security filter chain. Defines the endpoints that can be accessed without auth
   * via requestMatchers().
   *
   * @param http the HttpSecurity to configure
   * @return the configured SecurityFilterChain
   * @throws Exception if an error occurs during configuration
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(auth -> {
      auth.requestMatchers("/hello", "/login", "/register").permitAll();
      auth.anyRequest().authenticated();
    }).sessionManagement(
        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}