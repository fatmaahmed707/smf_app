package com.smf.service.deviceauth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.smf.model.Device;
import com.smf.model.RegisteredDevice;
import com.smf.model.SmfDevice;
import com.smf.repo.RegisteredDeviceRepository;
import com.smf.util.AppError;
import com.smf.util.EncryptionUtil;
import com.smf.util.HmacUtil;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class DeviceAuthServiceTest {

  @Mock private RegisteredDeviceRepository registeredDeviceRepository;
  @Mock private EncryptionUtil encryptionUtil;

  private DeviceAuthService deviceAuthService;

  private SmfDevice smfDevice;
  private Device device;
  private RegisteredDevice registeredDevice;
  private String secret = "test-secret";

  @BeforeEach
  void setUp() {
    deviceAuthService = new DeviceAuthService(registeredDeviceRepository, encryptionUtil);
    lenient().when(encryptionUtil.decrypt(anyString())).thenReturn(secret);

    smfDevice = new SmfDevice();
    smfDevice.setId(UUID.randomUUID());
    smfDevice.setMacAddress("AA:BB:CC:DD:EE:FF");
    smfDevice.setLabel("device-label-001");
    smfDevice.setSecret(secret);

    device = new Device();
    device.setId(UUID.randomUUID());
    device.setMacAddress("AA:BB:CC:DD:EE:FF");

    registeredDevice = new RegisteredDevice(smfDevice, device);
  }

  @Test
  void verifyDevice_success() throws InvalidKeyException, NoSuchAlgorithmException {
    long timestamp = Instant.now().getEpochSecond();
    String signature = HmacUtil.computeSignature("AA:BB:CC:DD:EE:FF", timestamp, secret);

    when(registeredDeviceRepository.findBySmfDeviceMacAddress("AA:BB:CC:DD:EE:FF"))
        .thenReturn(Optional.of(registeredDevice));

    boolean result = deviceAuthService.verifyDevice("AA:BB:CC:DD:EE:FF", timestamp, signature);

    assertTrue(result);
    verify(registeredDeviceRepository).findBySmfDeviceMacAddress("AA:BB:CC:DD:EE:FF");
  }

  @Test
  void verifyDevice_invalidSignature() throws InvalidKeyException, NoSuchAlgorithmException {
    long timestamp = Instant.now().getEpochSecond();
    String invalidSignature = "invalid-signature";

    when(registeredDeviceRepository.findBySmfDeviceMacAddress("AA:BB:CC:DD:EE:FF"))
        .thenReturn(Optional.of(registeredDevice));

    boolean result = deviceAuthService.verifyDevice("AA:BB:CC:DD:EE:FF", timestamp, invalidSignature);

    assertFalse(result);
  }

  @Test
  void verifyDevice_timestampExpired() {
    long expiredTimestamp = Instant.now().getEpochSecond() - 60;
    String signature = "some-signature";

    AppError exception = assertThrows(
        AppError.class,
        () -> deviceAuthService.verifyDevice("AA:BB:CC:DD:EE:FF", expiredTimestamp, signature));

    assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    assertEquals("Timestamp expired", exception.getMessage());
  }

  @Test
  void verifyDevice_deviceNotRegistered() {
    long timestamp = Instant.now().getEpochSecond();
    String signature = "some-signature";

    when(registeredDeviceRepository.findBySmfDeviceMacAddress("AA:BB:CC:DD:EE:FF"))
        .thenReturn(Optional.empty());

    AppError exception = assertThrows(
        AppError.class,
        () -> deviceAuthService.verifyDevice("AA:BB:CC:DD:EE:FF", timestamp, signature));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    assertEquals("Device not registered", exception.getMessage());
  }

  @Test
  void verifyDevice_futureTimestampExpired() {
    long futureTimestamp = Instant.now().getEpochSecond() + 60;
    String signature = "some-signature";

    AppError exception = assertThrows(
        AppError.class,
        () -> deviceAuthService.verifyDevice("AA:BB:CC:DD:EE:FF", futureTimestamp, signature));

    assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    assertEquals("Timestamp expired", exception.getMessage());
  }

  @Test
  void verifyDevice_timestampWithin30Seconds() throws InvalidKeyException, NoSuchAlgorithmException {
    long timestamp = Instant.now().getEpochSecond() - 25;
    String signature = HmacUtil.computeSignature("AA:BB:CC:DD:EE:FF", timestamp, secret);

    when(registeredDeviceRepository.findBySmfDeviceMacAddress("AA:BB:CC:DD:EE:FF"))
        .thenReturn(Optional.of(registeredDevice));

    boolean result = deviceAuthService.verifyDevice("AA:BB:CC:DD:EE:FF", timestamp, signature);

    assertTrue(result);
  }

  @Test
  void verifyDevice_tamperedSignature() throws InvalidKeyException, NoSuchAlgorithmException {
    long timestamp = Instant.now().getEpochSecond();
    String signatureForDifferentMac = HmacUtil.computeSignature("AA:BB:CC:DD:EE:00", timestamp, secret);

    when(registeredDeviceRepository.findBySmfDeviceMacAddress("AA:BB:CC:DD:EE:FF"))
        .thenReturn(Optional.of(registeredDevice));

    boolean result = deviceAuthService.verifyDevice("AA:BB:CC:DD:EE:FF", timestamp, signatureForDifferentMac);

    assertFalse(result);
  }

  @Test
  void verifyDevice_boundaryExactly30SecondsShouldPass() throws InvalidKeyException, NoSuchAlgorithmException {
    long timestamp = Instant.now().getEpochSecond() - 30;
    String signature = HmacUtil.computeSignature("AA:BB:CC:DD:EE:FF", timestamp, secret);

    when(registeredDeviceRepository.findBySmfDeviceMacAddress("AA:BB:CC:DD:EE:FF"))
        .thenReturn(Optional.of(registeredDevice));

    boolean result = deviceAuthService.verifyDevice("AA:BB:CC:DD:EE:FF", timestamp, signature);

    assertTrue(result);
  }

  @Test
  void verifyDevice_boundary31SecondsShouldFail() {
    long timestamp = Instant.now().getEpochSecond() - 31;
    String signature = "some-signature";

    AppError exception = assertThrows(
        AppError.class,
        () -> deviceAuthService.verifyDevice("AA:BB:CC:DD:EE:FF", timestamp, signature));

    assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    assertEquals("Timestamp expired", exception.getMessage());
  }

  @Test
  void verifyDevice_replayAttackShouldFail() throws InvalidKeyException, NoSuchAlgorithmException {
    long timestamp = Instant.now().getEpochSecond() - 5;
    String signature = HmacUtil.computeSignature("AA:BB:CC:DD:EE:FF", timestamp, secret);

    when(registeredDeviceRepository.findBySmfDeviceMacAddress("AA:BB:CC:DD:EE:FF"))
        .thenReturn(Optional.of(registeredDevice));

    boolean firstAttempt = deviceAuthService.verifyDevice("AA:BB:CC:DD:EE:FF", timestamp, signature);
    assertTrue(firstAttempt);

    AppError exception = assertThrows(
        AppError.class,
        () -> deviceAuthService.verifyDevice("AA:BB:CC:DD:EE:FF", timestamp, signature));

    assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    assertEquals("Replay attack detected", exception.getMessage());
  }
}
