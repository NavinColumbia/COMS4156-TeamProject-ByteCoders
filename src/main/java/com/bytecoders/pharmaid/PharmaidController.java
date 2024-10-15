package com.bytecoders.pharmaid;

import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.web.bind.annotation.GetMapping;
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
    final User user = userService.createUser();
    ObjectMapper mapper = new ObjectMapper();

    String json = mapper.writeValueAsString(user);
    return json;
  }
}
