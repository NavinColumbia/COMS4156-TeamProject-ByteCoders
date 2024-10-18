package com.bytecoders.pharmaid;

import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.request.LoginUserRequest;
import com.bytecoders.pharmaid.request.RegisterUserRequest;
import com.bytecoders.pharmaid.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * This class contains all the API routes for the system.
 *
 */
@RestController  
public class PharmaidController {
  @Autowired
  private UserService userService;
    
  /**
   * Basic hello endpoint for testing. 
   *
   * @return A String 
   */
  @GetMapping({ "/", "/hello"})
  public String index() throws JsonProcessingException {
    return "Hello :)";
  }

  /**
   * Register user endpoint.
   *
   * @param request RegisterUserRequest
   * @return a ResponseEntity with a success message if the operation is successful, or an error
   *     message if the course is not found
   */
  @PostMapping({ "/register"})
  public ResponseEntity<String> register(@RequestBody @Valid RegisterUserRequest request) {
    try {
      final User user = userService.registerUser(request);
      ObjectMapper mapper = new ObjectMapper();
      String json = mapper.writeValueAsString(user);
      return new ResponseEntity<>(json, HttpStatus.CREATED);
    } catch (DataIntegrityViolationException e) {
      return new ResponseEntity<>("User already exists for this email", HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Login user endpoint.
   *
   * @param request LoginUserRequest
   * @return a ResponseEntity with a success message if the operation is successful, or an error
   *     message if the course is not found
   */
  @PostMapping(path = "/login")
  public ResponseEntity<String> login(@RequestBody @Valid LoginUserRequest request) {
    try {
      Optional<User> user = userService.loginUser(request);

      if (user.isEmpty()) {
        return new ResponseEntity<>("Forbidden", HttpStatus.UNAUTHORIZED);
      }

      ObjectMapper mapper = new ObjectMapper();
      String json = mapper.writeValueAsString(user.get());
      return new ResponseEntity<>(json, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(
          "Unexpected error encountered during login", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }


}
