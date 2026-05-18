package com.smf.service.deviceauth;

import com.smf.model.RegisteredDevice;
import com.smf.repo.RegisteredDeviceRepository;
import com.smf.util.AppError;
import com.smf.util.EncryptionUtil;
import com.smf.util.HmacUtil;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeviceAuthService implements IDeviceAuthService {

  private final RegisteredDeviceRepository registeredDeviceRepository;
  private final EncryptionUtil encryptionUtil;

  private final ConcurrentHashMap<String, Instant> usedNonces = new ConcurrentHashMap<>();

  private static final Duration TIMESTAMP_WINDOW = Duration.ofSeconds(30);

  @Override
  public boolean verifyDevice(String macAddress, long timestamp, String signature)
      throws InvalidKeyException, NoSuchAlgorithmException {

    Instant now = Instant.now();

    Duration diff = Duration.between(Instant.ofEpochSecond(timestamp), now);
    if (diff.abs().getSeconds() > 30) {
      throw new AppError(HttpStatus.UNAUTHORIZED, "Timestamp expired");
    }

    String nonceKey = macAddress + ":" + timestamp;
    if (usedNonces.containsKey(nonceKey)) {
      throw new AppError(HttpStatus.UNAUTHORIZED, "Replay attack detected");
    }

    RegisteredDevice registeredDevice =
        registeredDeviceRepository
            .findBySmfDeviceMacAddress(macAddress)
            .orElseThrow(() -> new AppError(HttpStatus.NOT_FOUND, "Device not registered"));

    String decryptedSecret = encryptionUtil.decrypt(registeredDevice.getSmfDevice().getSecret());
    boolean valid =
        HmacUtil.verifySignature(
            macAddress, timestamp, decryptedSecret, signature);

    if (valid) {
      usedNonces.put(nonceKey, now);
      cleanupOldNonces();
    }

    return valid;
  }

  private void cleanupOldNonces() {
    if (usedNonces.size() > 1000) {
      Instant cutoff = Instant.now().minus(TIMESTAMP_WINDOW);
      usedNonces.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
    }
  }
}
