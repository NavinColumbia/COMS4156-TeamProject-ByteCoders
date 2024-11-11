package com.bytecoders.pharmaid.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security Config.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Autowired private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Autowired private JwtTokenProvider tokenProvider;

  /**
   * Filter chain - access control for endpoints.Not for test profile.
   *
   * @param http HttpSecurity object
   * @return SecurityFilterChain
   * @throws Exception if an error occurs
   */
  @Profile("!test")
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
                          // checks if Authorization header exists
                          String authHeader = request.getHeader("Authorization");
                          if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.getWriter().write("Invalid or missing token");
                            return;
                          }

                          // checks if substring token following "Bearer " is valid token
                          String token = authHeader.substring(7);
                          if (!tokenProvider.validateToken(token)) {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.getWriter().write("Invalid token");
                            return;
                          }

                          //logs out
                          SecurityContextHolder.clearContext();
                          response.setStatus(HttpStatus.OK.value());
                          response.getWriter().write("Logged out successfully");
                        }));

    return http.build();
  }

  @Profile("test") //For test just allow everything
  @Configuration
  public static class TestSecurityConfig {
    @Bean
    public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
      http.csrf(csrf -> csrf.disable())
          .sessionManagement(
              session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .authorizeHttpRequests(auth ->
              auth.anyRequest().permitAll()); // Allow all requests in test profile
      return http.build();
    }
  }

}
