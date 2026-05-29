package com.smf.service.registereddevice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.smf.model.Device;
import com.smf.model.RegisteredDevice;
import com.smf.model.SmfDevice;
import com.smf.repo.RegisteredDeviceRepository;
import com.smf.service.device.IDeviceService;
import com.smf.service.smfdevice.ISmfDeviceService;
import com.smf.util.AppError;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class RegisteredDeviceServiceTest {

  @Mock private RegisteredDeviceRepository registeredDeviceRepository;

  @Mock private ISmfDeviceService smfDeviceService;

  @Mock private IDeviceService deviceService;

  @InjectMocks private RegisteredDeviceService registeredDeviceService;

  private UUID smfDeviceId;
  private UUID deviceId;
  private SmfDevice smfDevice;
  private Device device;

  @BeforeEach
  void setUp() {
    smfDeviceId = UUID.randomUUID();
    deviceId = UUID.randomUUID();

    smfDevice = new SmfDevice();
    smfDevice.setId(smfDeviceId);
    smfDevice.setMacAddress("AA:BB:CC:DD:EE:FF");
    smfDevice.setLabel("device-001");
    smfDevice.setSecret("secret");
    smfDevice.setRegistered(false);

    device = new Device();
    device.setId(deviceId);
    device.setMacAddress("AA:BB:CC:DD:EE:FF");
  }

  @Test
  void registerDevice_success() {
    when(smfDeviceService.getEntityDeviceById(smfDeviceId)).thenReturn(smfDevice);
    when(deviceService.findDeviceById(deviceId)).thenReturn(device);
    when(registeredDeviceRepository.save(any(RegisteredDevice.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    RegisteredDevice result = registeredDeviceService.registerDevice(smfDeviceId, deviceId);

    assertNotNull(result);
    assertEquals(smfDevice, result.getSmfDevice());
    assertEquals(device, result.getDevice());
    verify(smfDeviceService).markAsRegistered(smfDeviceId);
    verify(registeredDeviceRepository).save(any(RegisteredDevice.class));
  }

  @Test
  void registerDevice_alreadyRegistered() {
    smfDevice.setRegistered(true);
    when(smfDeviceService.getEntityDeviceById(smfDeviceId)).thenReturn(smfDevice);

    AppError error = assertThrows(
        AppError.class,
        () -> registeredDeviceService.registerDevice(smfDeviceId, deviceId));

    assertEquals(HttpStatus.CONFLICT, error.getStatus());
    assertEquals("SMF Device is already registered", error.getMessage());
  }
}
