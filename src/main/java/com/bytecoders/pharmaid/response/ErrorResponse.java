package com.bytecoders.pharmaid.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * Response class for error scenarios.
 * Contains error details including message and timestamp.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
  private String message;
  private String details;
  private String path;
  private LocalDateTime timestamp;
}