package com.bytecoders.pharmaid.util;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests for hashing password logic.
 */
public class PasswordUtilsTests {
  private final PasswordUtils passwordUtils = new PasswordUtils();

  @Test
  public void testHashPassword() {
    final String hashedPassword = passwordUtils.hashPassword("someSecretPassword");
    assertNotEquals("someSecretPassword", hashedPassword);
    assertTrue(passwordUtils.verifyPassword("someSecretPassword", hashedPassword));
  }
}
