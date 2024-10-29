package com.bytecoders.pharmaid.util;

import org.mindrot.jbcrypt.BCrypt;

/** Utils to hash passwords with salt and verify hashed passwords. */
public class PasswordUtils {
  public String hashPassword(String password) {
    return BCrypt.hashpw(password, BCrypt.gensalt());
  }

  public boolean verifyPassword(String password, String hashedPassword) {
    return BCrypt.checkpw(password, hashedPassword);
  }
}
