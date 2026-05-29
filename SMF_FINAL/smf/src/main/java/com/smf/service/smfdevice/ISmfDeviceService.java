package com.smf.service.smfdevice;

import com.smf.dto.device.SmfDeviceRequest;
import com.smf.dto.device.SmfDeviceResponse;
import com.smf.model.SmfDevice;
import java.util.List;
import java.util.UUID;

public interface ISmfDeviceService {

  List<SmfDeviceResponse> getAllDevices();

  List<SmfDeviceResponse> getUnregisteredDevices();

  SmfDeviceResponse getDeviceById(UUID id);

  SmfDevice getEntityDeviceById(UUID id);

  SmfDeviceResponse addDevice(SmfDeviceRequest request);

  void markAsRegistered(UUID id);

  SmfDeviceResponse getByLabel(String label);

  void removeDevice(UUID id);
}
