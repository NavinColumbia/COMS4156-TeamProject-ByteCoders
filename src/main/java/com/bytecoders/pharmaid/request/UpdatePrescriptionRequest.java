package com.bytecoders.pharmaid.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Date;
import lombok.Data;

/**
 * Object to hold create prescription request body.
 */
@Data
public class UpdatePrescriptionRequest {

  private Date endDate;

  private Boolean isActive;
}
