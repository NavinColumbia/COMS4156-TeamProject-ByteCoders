package com.bytecoders.pharmaid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;  

/**
 * This class contains all the API routes for the system.
 *
 */
@RestController  
public class PharmaidController {
    
  /**
   * Basic hello endpoint for testing. 
   *
   * @return A String 
   */
  @GetMapping({ "/", "/hello"})
  public String index() {
    return "Hello :)";
  }
}
