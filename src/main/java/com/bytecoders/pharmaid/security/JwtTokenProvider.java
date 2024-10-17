package com.bytecoders.pharmaid.security;


import com.bytecoders.pharmaid.repository.model.User;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenProvider {

  @Value("${app.jwtSecret}")
  private String jwtSecret;

  @Value("${app.jwtExpirationInMs}")
  private int jwtExpirationInMs;

  public String generateToken(Authentication authentication) {
    User userPrincipal = (User) authentication.getPrincipal();
    return generateToken(userPrincipal);
  }

  public String generateToken(User user) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

    return Jwts.builder()
        .setSubject(user.getId())
        .claim("email", user.getEmail())
        .claim("userType", user.getUserType().getName())
        .setIssuedAt(new Date())
        .setExpiration(expiryDate)
        .signWith(SignatureAlgorithm.HS512, jwtSecret)
        .compact();
  }

  public String getUserIdFromJWT(String token) {
    Claims claims = Jwts.parser()
        .setSigningKey(jwtSecret)
        .parseClaimsJws(token)
        .getBody();

    return claims.getSubject();
  }

  public boolean validateToken(String authToken) {
    try {
      Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
      return true;
    } catch (SignatureException ex) {
      // Log error about invalid JWT signature
    } catch (MalformedJwtException ex) {
      // Log error about invalid JWT token
    } catch (ExpiredJwtException ex) {
      // Log error about expired JWT token
    } catch (UnsupportedJwtException ex) {
      // Log error about unsupported JWT token
    } catch (IllegalArgumentException ex) {
      // Log error about invalid token
    }
    return false;
  }
}
