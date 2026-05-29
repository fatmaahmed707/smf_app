package com.smf.service.deviceauth;

public interface IDeviceAuthService {
  boolean verifyDevice(String macAddress, long timestamp, String signature)
      throws java.security.InvalidKeyException, java.security.NoSuchAlgorithmException;
}
