package com.bytecoders.pharmaid.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utils to hash passwords with salt and verify hashed passwords.
 */
public class PasswordUtils {
  public static String hashPassword(String password) {
    return BCrypt.hashpw(password, BCrypt.gensalt());
  }

  public static boolean verifyPassword(String password, String hashedPassword) {
    return BCrypt.checkpw(password, hashedPassword);
  }
}
