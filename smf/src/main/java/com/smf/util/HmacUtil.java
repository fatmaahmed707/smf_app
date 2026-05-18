package com.smf.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HmacUtil {

  private static final String HMAC_ALGORITHM = "HmacSHA256";

  private HmacUtil() {}

  public static String computeSignature(String macAddress, long timestamp, String secret)
      throws InvalidKeyException, NoSuchAlgorithmException {
    String payload = macAddress + ":" + timestamp;
    Mac mac = Mac.getInstance(HMAC_ALGORITHM);
    SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(), HMAC_ALGORITHM);
    mac.init(keySpec);
    return Base64.getEncoder().encodeToString(mac.doFinal(payload.getBytes()));
  }

  public static boolean verifySignature(
      String macAddress, long timestamp, String secret, String signature)
      throws InvalidKeyException, NoSuchAlgorithmException {
    return computeSignature(macAddress, timestamp, secret).equals(signature);
  }
}
