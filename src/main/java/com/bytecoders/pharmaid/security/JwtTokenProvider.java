package com.bytecoders.pharmaid.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** JwtTokenProvider. */
@Component
public class JwtTokenProvider {

  /** set in application.properties. **/
  @Value("${app.jwt.secret}")
  private String jwtSecret;

  /** set in application.properties. **/
  @Value("${app.jwt.expiration}")
  private int jwtExpirationMs;

  private Key key;

  /**
   *   Post Constructor - key is created After token provider is created
   *   but before it is used anywhere.
   * */
  @PostConstruct
  public void init() {
    this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Generates JWT token.
   *
   * @param userId user id.
   * @return jwt token.
   */
  public String generateToken(String userId) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

    return Jwts.builder()
        .setSubject(userId)
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(key, SignatureAlgorithm.HS512)
        .compact();
  }

  /**
   * Get user ID from JWT token.
   *
   * @param token the JWT token
   * @return the user ID
   */
  public String getUserIdFromJwt(String token) {
    Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    return claims.getSubject();
  }

  /**
   * Validates JWT token.
   *
   * @param token the JWT token to validate
   * @return true if valid, false otherwise
   */
  public boolean validateToken(String token) {
    if (token == null || token.trim().isEmpty()) {
      return false;
    }
    try {
      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
      return true;
    } catch (JwtException ex) {
      return false;
    }
  }
}
