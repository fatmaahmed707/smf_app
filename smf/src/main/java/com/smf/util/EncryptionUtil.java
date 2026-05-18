package com.smf.util;

import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EncryptionUtil {

  private static final String ALGORITHM = "AES/GCM/NoPadding";
  private static final int GCM_IV_LENGTH = 12;
  private static final int GCM_TAG_LENGTH = 128;

  private final SecretKey secretKey;
  private final SecureRandom secureRandom;

  public EncryptionUtil(@Value("${smf.encryption.key}") String encryptionKey) {
    byte[] keyBytes = hexToBytes(encryptionKey);
    if (keyBytes.length != 32) {
      throw new IllegalArgumentException("Encryption key must be 32 bytes (256-bit)");
    }
    this.secretKey = new SecretKeySpec(keyBytes, "AES");
    this.secureRandom = new SecureRandom();
  }

  public String encrypt(String plaintext) {
    try {
      byte[] iv = new byte[GCM_IV_LENGTH];
      secureRandom.nextBytes(iv);

      Cipher cipher = Cipher.getInstance(ALGORITHM);
      GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

      byte[] ciphertext = cipher.doFinal(plaintext.getBytes());

      byte[] combined = new byte[iv.length + ciphertext.length];
      System.arraycopy(iv, 0, combined, 0, iv.length);
      System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

      return bytesToHex(combined);
    } catch (Exception e) {
      throw new RuntimeException("Encryption failed", e);
    }
  }

  public String decrypt(String encryptedText) {
    try {
      byte[] combined = hexToBytes(encryptedText);

      byte[] iv = new byte[GCM_IV_LENGTH];
      byte[] ciphertext = new byte[combined.length - GCM_IV_LENGTH];

      System.arraycopy(combined, 0, iv, 0, iv.length);
      System.arraycopy(combined, iv.length, ciphertext, 0, ciphertext.length);

      Cipher cipher = Cipher.getInstance(ALGORITHM);
      GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
      cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

      return new String(cipher.doFinal(ciphertext));
    } catch (Exception e) {
      throw new RuntimeException("Decryption failed", e);
    }
  }

  private static byte[] hexToBytes(String hex) {
    int len = hex.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
          + Character.digit(hex.charAt(i + 1), 16));
    }
    return data;
  }

  private static String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder(bytes.length * 2);
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }
}
