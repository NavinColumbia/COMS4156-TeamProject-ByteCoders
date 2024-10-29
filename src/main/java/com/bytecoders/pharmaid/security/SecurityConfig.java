package com.bytecoders.pharmaid.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration class for the application. Configures web security, authentication, and
 * authorization rules.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Autowired private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Autowired private JwtTokenProvider tokenProvider;

  /**
   * Configures the security filter chain for HTTP security.
   *
   * @param http the HttpSecurity object to configure
   * @return the configured SecurityFilterChain
   * @throws Exception if an error occurs during configuration
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/register", "/login", "/hello")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .logout(
            logout ->
                logout
                    .logoutUrl("/logout")
                    .logoutSuccessHandler(
                        (request, response, auth) -> {
                          String authHeader = request.getHeader("Authorization");
                          if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.getWriter().write("Invalid or missing token");
                            return;
                          }

                          String token = authHeader.substring(7);
                          if (!tokenProvider.validateToken(token)) {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.getWriter().write("Invalid token");
                            return;
                          }

                          SecurityContextHolder.clearContext();
                          response.setStatus(HttpStatus.OK.value());
                          response.getWriter().write("Logged out successfully");
                        }));

    return http.build();
  }

  /**
   * Creates a BCryptPasswordEncoder bean for password hashing.
   *
   * @return the password encoder
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
