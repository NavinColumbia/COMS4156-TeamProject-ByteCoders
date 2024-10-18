package com.bytecoders.pharmaid;

import com.bytecoders.pharmaid.dto.AuthResponse;
import com.bytecoders.pharmaid.dto.LoginRequest;
import com.bytecoders.pharmaid.dto.RegisterRequest;
import com.bytecoders.pharmaid.dto.TokenRefreshRequest;
import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.repository.model.RefreshToken;
import com.bytecoders.pharmaid.repository.model.UserType;
import com.bytecoders.pharmaid.security.JwtTokenProvider;
import com.bytecoders.pharmaid.service.RefreshTokenService;
import com.bytecoders.pharmaid.service.UserService;
import com.bytecoders.pharmaid.service.UserTypeService;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;



import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  @Autowired
  private AuthenticationManager authenticationManager;

  @Autowired
  private UserService userService;

  @Autowired
  private RefreshTokenService refreshTokenService;

  @Autowired
  private UserTypeService userTypeService;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private JwtTokenProvider tokenProvider;

  @PostMapping("/login")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            loginRequest.getEmail(),
            loginRequest.getPassword()
        )
    );

    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = tokenProvider.generateToken(authentication);
    return ResponseEntity.ok(new AuthResponse(jwt));
  }

  @PostMapping("/register")
  public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
    if (userService.existsByEmail(registerRequest.getEmail())) {
      return ResponseEntity.badRequest().body("Error: Email is already in use!");
    }

    UserType userType = userTypeService.getUserTypeByName(registerRequest.getUserType())
        .orElseThrow(() -> new RuntimeException("Error: User Type is not found."));

    User user = new User();
    user.setEmail(registerRequest.getEmail());
    user.setHashedPassword(passwordEncoder.encode(registerRequest.getPassword()));
    user.setUserType(userType);

    userService.createUser(user);

    return ResponseEntity.ok("User registered successfully!");
  }
/*
  @PostMapping("/refresh")
  public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
    String requestRefreshToken = request.getRefreshToken();

    return tokenProvider.findByRefreshToken(requestRefreshToken)
        .map(tokenProvider::verifyExpiration)
        .map(User::getEmail)
        .map(email -> {
          String token = tokenProvider.generateTokenFromUsername(email);
          return ResponseEntity.ok(new AuthResponse(token, requestRefreshToken));
        })
        .orElse(ResponseEntity.badRequest().body("Refresh token is not in database!"));
  }*/
@PostMapping("/refresh")
public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
  String requestRefreshToken = request.getRefreshToken();

  Optional<AuthResponse> authResponseOptional = refreshTokenService.findByToken(requestRefreshToken)
      .map(refreshTokenService::verifyExpiration)
      .map(RefreshToken::getUser)
      .map(user -> {
        String token = tokenProvider.generateToken(user);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());
        return new AuthResponse(token, newRefreshToken.getToken());
      });

  if (authResponseOptional.isPresent()) {
    return ResponseEntity.ok(authResponseOptional.get());
  } else {
    return ResponseEntity.badRequest().body(new ErrorResponse("Refresh token is not in database!"));
  }
}


  /*
  @PostMapping("/logout")
  public ResponseEntity<?> logoutUser(@RequestHeader("Authorization") String token) {
    if (token != null && token.startsWith("Bearer ")) {
      token = token.substring(7);
      tokenProvider.invalidateToken(token);
    }
    SecurityContextHolder.clearContext();
    return ResponseEntity.ok("Logout successful");
  }*/

  @PostMapping("/logout")
  public ResponseEntity<?> logoutUser(@RequestHeader("Authorization") String token) {
    if (token != null && token.startsWith("Bearer ")) {
      token = token.substring(7);
      String userId = tokenProvider.getUserIdFromJWT(token);
      refreshTokenService.deleteByUserId(userId);
    }
    SecurityContextHolder.clearContext();
    return ResponseEntity.ok("Logout successful");
  }
}