package com.smf.util;

import static org.junit.jupiter.api.Assertions.*;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.Test;

class HmacUtilTest {

  @Test
  void computeSignature_returnsConsistentSignature() throws InvalidKeyException, NoSuchAlgorithmException {
    String macAddress = "AA:BB:CC:DD:EE:FF";
    long timestamp = 1234567890L;
    String secret = "test-secret";

    String signature1 = HmacUtil.computeSignature(macAddress, timestamp, secret);
    String signature2 = HmacUtil.computeSignature(macAddress, timestamp, secret);

    assertEquals(signature1, signature2);
    assertNotNull(signature1);
  }

  @Test
  void computeSignature_differentInputsDifferentSignatures() throws InvalidKeyException, NoSuchAlgorithmException {
    String macAddress = "AA:BB:CC:DD:EE:FF";
    long timestamp = 1234567890L;
    String secret = "test-secret";

    String signature1 = HmacUtil.computeSignature(macAddress, timestamp, secret);
    String signature2 = HmacUtil.computeSignature(macAddress, timestamp, "different-secret");

    assertNotEquals(signature1, signature2);
  }

  @Test
  void verifySignature_returnsTrueForValidSignature() throws InvalidKeyException, NoSuchAlgorithmException {
    String macAddress = "AA:BB:CC:DD:EE:FF";
    long timestamp = 1234567890L;
    String secret = "test-secret";

    String signature = HmacUtil.computeSignature(macAddress, timestamp, secret);

    assertTrue(HmacUtil.verifySignature(macAddress, timestamp, secret, signature));
  }

  @Test
  void verifySignature_returnsFalseForInvalidSignature() throws InvalidKeyException, NoSuchAlgorithmException {
    String macAddress = "AA:BB:CC:DD:EE:FF";
    long timestamp = 1234567890L;
    String secret = "test-secret";

    assertFalse(HmacUtil.verifySignature(macAddress, timestamp, secret, "invalid-signature"));
  }
}
