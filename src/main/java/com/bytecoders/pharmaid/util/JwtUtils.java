package com.bytecoders.pharmaid.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utils to generate, parse, and validate JSON Web Tokens (JWT).
 */
@Component
public class JwtUtils {

  @Value("${security.jwt.secret-key}")
  private String secretKey;

  @Value("${security.jwt.expiration-time}")
  private long jwtExpiration;

  /**
   * Generate a JWT token with a specified user identifier.
   *
   * @param userId Unique identifier for the user.
   * @return signed JWT as a String, with userId as the subject.
   */
  public String generateToken(String userId) {
    return Jwts.builder()
        .setSubject(userId)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
        .signWith(getSignInKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  public String extractUserId(String token) {
    return getClaims(token).getSubject();
  }

  public boolean isTokenValid(String token, String userId) {
    return extractUserId(token).equals(userId) && !isTokenExpired(token);
  }

  private Claims getClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSignInKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  private boolean isTokenExpired(String token) {
    return getClaims(token).getExpiration().before(new Date());
  }

  private Key getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
