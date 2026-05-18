package com.smf.service.device;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.smf.dto.device.DeviceRegisterRequest;
import com.smf.dto.device.DeviceResponse;
import com.smf.dto.device.SmfDeviceResponse;
import com.smf.model.Device;
import com.smf.model.User;
import com.smf.model.Zone;
import com.smf.model.enums.DeviceStatus;
import com.smf.repo.DeviceRepository;
import com.smf.service.registereddevice.IRegisteredDeviceService;
import com.smf.service.smfdevice.ISmfDeviceService;
import com.smf.service.user.IUserService;
import com.smf.service.zone.IZoneService;
import com.smf.util.AppError;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

  @Mock private DeviceRepository deviceRepository;

  @Mock private ISmfDeviceService smfDeviceService;

  @Mock private IRegisteredDeviceService registeredDeviceService;

  @Mock private IUserService userService;

  @Mock private IZoneService zoneService;

  @InjectMocks private DeviceService deviceService;

  private UUID ownerId;
  private UUID smfDeviceId;
  private UUID zoneId;
  private User owner;
  private Zone zone;
  private SmfDeviceResponse smfDeviceResponse;
  private DeviceRegisterRequest request;

  @BeforeEach
  void setUp() {
    ownerId = UUID.randomUUID();
    smfDeviceId = UUID.randomUUID();
    zoneId = UUID.randomUUID();

    owner = new User("owner@test.com", "owner", "password");
    owner.setId(ownerId);

    zone = new Zone();
    zone.setId(zoneId);
    zone.setName("Test Zone");

    smfDeviceResponse =
        new SmfDeviceResponse(smfDeviceId, null, "device-label-001", false, null);

    request =
        new DeviceRegisterRequest(
            "device-label-001",
            ownerId.toString(),
            zoneId);
  }

  @Test
  void registerDevice_success() {
    when(smfDeviceService.getByLabel("device-label-001")).thenReturn(smfDeviceResponse);
    when(userService.findUserById(ownerId)).thenReturn(owner);
    when(zoneService.findZoneById(zoneId)).thenReturn(zone);
    when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> {
      Device d = invocation.getArgument(0);
      d.setId(UUID.randomUUID());
      return d;
    });

    DeviceResponse response = deviceService.registerDevice(request);

    assertNotNull(response);
    assertEquals(ownerId, response.ownerId());
    assertEquals(zoneId, response.zoneId());
    assertEquals("Test Zone", response.zoneName());
    verify(registeredDeviceService).registerDevice(eq(smfDeviceId), any(UUID.class));
  }

  @Test
  void registerDevice_alreadyRegistered() {
    SmfDeviceResponse registeredSmfDevice =
        new SmfDeviceResponse(smfDeviceId, null, "device-label-001", true, null);
    when(smfDeviceService.getByLabel("device-label-001")).thenReturn(registeredSmfDevice);

    AppError exception = assertThrows(AppError.class, () -> deviceService.registerDevice(request));

    assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    assertEquals("Device already registered", exception.getMessage());
  }

  @Test
  void getDeviceById_success() {
    UUID deviceId = UUID.randomUUID();
    Device device = new Device(owner);
    device.setId(deviceId);
    device.setMacAddress("AA:BB:CC:DD:EE:FF");
    device.setLastZone(zone);

    when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

    DeviceResponse response = deviceService.getDeviceById(deviceId);

    assertEquals(deviceId, response.id());
    assertEquals("AA:BB:CC:DD:EE:FF", response.macAddress());
    assertEquals(zoneId, response.zoneId());
    assertEquals("Test Zone", response.zoneName());
  }

  @Test
  void getDeviceById_notFound() {
    UUID deviceId = UUID.randomUUID();
    when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

    AppError exception = assertThrows(AppError.class, () -> deviceService.getDeviceById(deviceId));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
  }

  @Test
  void updateDevice_success() {
    UUID deviceId = UUID.randomUUID();
    Device device = new Device(owner);
    device.setId(deviceId);
    device.setMacAddress("AA:BB:CC:DD:EE:FF");

    when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
    when(userService.findUserById(ownerId)).thenReturn(owner);
    when(zoneService.findZoneById(zoneId)).thenReturn(zone);
    when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> invocation.getArgument(0));

    DeviceResponse response = deviceService.updateDevice(deviceId, request);

    assertEquals(zoneId, response.zoneId());
    assertEquals("Test Zone", response.zoneName());
  }

  @Test
  void updateDevice_notFound() {
    UUID deviceId = UUID.randomUUID();
    when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

    AppError exception = assertThrows(AppError.class, () -> deviceService.updateDevice(deviceId, request));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
  }

  @Test
  void deleteDevice_success() {
    UUID deviceId = UUID.randomUUID();
    Device device = new Device(owner);
    when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

    assertDoesNotThrow(() -> deviceService.deleteDevice(deviceId));
    verify(deviceRepository).delete(device);
  }

  @Test
  void deleteDevice_notFound() {
    UUID deviceId = UUID.randomUUID();
    when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

    AppError exception = assertThrows(AppError.class, () -> deviceService.deleteDevice(deviceId));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
  }

  @Test
  void handleSos_updatesStatus() {
    String macAddress = "AA:BB:CC:DD:EE:FF";
    Device device = new Device(owner);
    device.setMacAddress(macAddress);

    when(deviceRepository.findByMacAddress(macAddress)).thenReturn(Optional.of(device));
    when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> invocation.getArgument(0));

    DeviceResponse response = deviceService.handleSos(macAddress);

    assertEquals(DeviceStatus.SOS, response.status());
  }

  @Test
  void handleOffline_updatesStatus() {
    String macAddress = "AA:BB:CC:DD:EE:FF";
    Device device = new Device(owner);
    device.setMacAddress(macAddress);

    when(deviceRepository.findByMacAddress(macAddress)).thenReturn(Optional.of(device));
    when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> invocation.getArgument(0));

    DeviceResponse response = deviceService.handleOffline(macAddress);

    assertEquals(DeviceStatus.OFFLINE, response.status());
  }
}
