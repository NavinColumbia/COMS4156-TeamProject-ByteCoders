package com.bytecoders.pharmaid;

import java.util.Date;

public class ErrorResponse {
  private String message;
  private Date timestamp;

  public ErrorResponse(String message) {
    this.message = message;
    this.timestamp = new Date();
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }
// Getters and setters
}