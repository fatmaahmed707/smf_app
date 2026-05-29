package com.smf.service.device;

import com.smf.dto.device.DeviceRegisterRequest;
import com.smf.dto.device.DeviceResponse;
import com.smf.model.Device;
import java.util.List;
import java.util.UUID;

public interface IDeviceService {
  DeviceResponse registerDevice(DeviceRegisterRequest request);

  DeviceResponse getDeviceById(UUID deviceId);

  List<DeviceResponse> getAllDevices();

  DeviceResponse updateDevice(UUID deviceId, DeviceRegisterRequest request);

  void deleteDevice(UUID deviceId);

  DeviceResponse handleSos(String macAddress);

  DeviceResponse handleOffline(String macAddress);

  Device findDeviceByMacAddress(String macAddress);

  Device findDeviceById(UUID deviceId);

  int incrementViolationCount(String macAddress);

  DeviceResponse updateDeviceZone(String macAddress, UUID zoneId);
}
