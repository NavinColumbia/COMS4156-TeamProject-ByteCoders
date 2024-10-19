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
public class CreatePrescriptionRequest {
  @NotEmpty(message = "Medication id is required")
  private String medicationId;

  @NotNull
  @Positive(message = "Dosage must be positive")
  private int dosage;

  @NotNull
  @Positive(message = "Number of doses must be positive")
  private int numOfDoses;

  @NotNull(message = "Start date is required")
  private Date startDate;

  private Date endDate;

  @NotNull(message = "Active flag is required")
  private Boolean isActive;
}
