package com.smf.service.registereddevice;

import com.smf.model.Device;
import com.smf.model.RegisteredDevice;
import com.smf.model.SmfDevice;
import com.smf.repo.RegisteredDeviceRepository;
import com.smf.service.device.IDeviceService;
import com.smf.service.smfdevice.ISmfDeviceService;
import com.smf.util.AppError;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class RegisteredDeviceService implements IRegisteredDeviceService {

  private final RegisteredDeviceRepository registeredDeviceRepository;
  private final ISmfDeviceService smfDeviceService;
  private final IDeviceService deviceService;

  @Override
  @Transactional
  public RegisteredDevice registerDevice(UUID smfDeviceId, UUID deviceId) {
    SmfDevice smfDevice = smfDeviceService.getEntityDeviceById(smfDeviceId);

    if (smfDevice.isRegistered()) {
      throw new AppError(HttpStatus.CONFLICT, "SMF Device is already registered");
    }

    Device device = deviceService.findDeviceById(deviceId);

    RegisteredDevice registeredDevice = new RegisteredDevice(smfDevice, device);
    smfDeviceService.markAsRegistered(smfDeviceId);

    return registeredDeviceRepository.save(registeredDevice);
  }
}
