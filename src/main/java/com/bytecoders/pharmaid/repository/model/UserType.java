package com.bytecoders.pharmaid.repository.model;

/** Enum representing the different types of users in the system. */
public enum UserType {
  /** First responder user type with emergency access capabilities. */
  FIRST_RESPONDER,

  /** Regular patient user type. */
  PATIENT,

  /** Healthcare provider user type. */
  HEALTH_CARE_PROVIDER
}
