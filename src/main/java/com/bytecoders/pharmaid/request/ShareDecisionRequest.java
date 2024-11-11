package com.bytecoders.pharmaid.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShareDecisionRequest {
  @NotNull private Integer action; // 1=accept, 2=deny
}
