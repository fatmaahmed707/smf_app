package com.smf.service.registereddevice;

import com.smf.model.RegisteredDevice;
import java.util.UUID;

public interface IRegisteredDeviceService {

  RegisteredDevice registerDevice(UUID smfDeviceId, UUID deviceId);
}
