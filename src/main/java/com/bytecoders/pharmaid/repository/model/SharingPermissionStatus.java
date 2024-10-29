package com.bytecoders.pharmaid.repository.model;

/** Enum representing the possible states of a sharing permission request. */
public enum SharingPermissionStatus {
  /** Request is awaiting approval. */
  PENDING,

  /** Request has been approved. */
  ACCEPTED,

  /** Request has been denied. */
  DENIED
}
