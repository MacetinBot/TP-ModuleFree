package com.superking.parchisi.shell.secondary;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class Utils {
  private Utils() {
    // Constructor privado para evitar instanciación
  }
  
  // Convierte bytes a hexadecimal
  public static String bytesToHex(byte[] bytes) {
    StringBuilder hexString = new StringBuilder();
    for (byte b : bytes) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }
  
  // ThreadLocal con MessageDigest para SHA-256
  private static final ThreadLocal<MessageDigest> SHA256_DIGEST =
          ThreadLocal.withInitial(() -> {
            try {
              return MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
              // Imposible en Android API 24+
              throw new RuntimeException("SHA-256 not supported", e);
            }
          });
  
  // SHA-256 de un String
  public static String sha256(String input) {
    MessageDigest digest = Objects.requireNonNull(SHA256_DIGEST.get());
    digest.reset();
    byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
    return bytesToHex(hash);
  }
  
  // SHA-256 de un byte array
  public static String sha256(byte[] input) {
    MessageDigest digest = Objects.requireNonNull(SHA256_DIGEST.get());
    digest.reset();
    byte[] hash = digest.digest(input);
    return bytesToHex(hash);
  }
  
  // Agregar este método en Utils.java
  public static byte[] sha256Bytes(String input) {
    MessageDigest digest = Objects.requireNonNull(SHA256_DIGEST.get());
    digest.reset();
    return digest.digest(input.getBytes(StandardCharsets.UTF_8));
  }
}