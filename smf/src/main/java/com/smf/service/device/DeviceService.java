package com.smf.service.device;

import com.smf.dto.device.DeviceRegisterRequest;
import com.smf.dto.device.DeviceResponse;
import com.smf.dto.device.SmfDeviceResponse;
import com.smf.model.Device;
import com.smf.model.User;
import com.smf.model.enums.DeviceStatus;
import com.smf.repo.DeviceRepository;
import com.smf.service.registereddevice.IRegisteredDeviceService;
import com.smf.service.smfdevice.ISmfDeviceService;
import com.smf.service.user.IUserService;
import com.smf.service.zone.IZoneService;
import com.smf.util.AppError;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class DeviceService implements IDeviceService {

  private final DeviceRepository deviceRepository;
  private final ISmfDeviceService smfDeviceService;
  private final IRegisteredDeviceService registeredDeviceService;
  private final IUserService userService;
  private final IZoneService zoneService;

  public DeviceService(
      DeviceRepository deviceRepository,
      ISmfDeviceService smfDeviceService,
      @Lazy IRegisteredDeviceService registeredDeviceService,
      IUserService userService,
      @Lazy IZoneService zoneService) {
    this.deviceRepository = deviceRepository;
    this.smfDeviceService = smfDeviceService;
    this.registeredDeviceService = registeredDeviceService;
    this.userService = userService;
    this.zoneService = zoneService;
  }

  @Override
  @Transactional
  public DeviceResponse registerDevice(DeviceRegisterRequest request) {
    SmfDeviceResponse smfDevice = smfDeviceService.getByLabel(request.smfDeviceLabel());

    if (smfDevice.isRegistered()) {
      throw new AppError(HttpStatus.CONFLICT, "Device already registered");
    }

    User owner = userService.findUserById(UUID.fromString(request.ownerId()));

    Device device = new Device(owner);

    if (request.zoneId() != null) {
      device.setLastZone(zoneService.findZoneById(request.zoneId()));
    }

    device = deviceRepository.save(device);

    registeredDeviceService.registerDevice(smfDevice.id(), device.getId());

    return mapToDeviceResponse(device);
  }

  @Override
  public DeviceResponse getDeviceById(UUID deviceId) {
    Device device =
        deviceRepository
            .findById(deviceId)
            .orElseThrow(() -> new AppError(HttpStatus.NOT_FOUND, "Device not found"));

    return mapToDeviceResponse(device);
  }

  @Override
  public List<DeviceResponse> getAllDevices() {
    return deviceRepository.findAll().stream()
        .map(this::mapToDeviceResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public DeviceResponse updateDevice(UUID deviceId, DeviceRegisterRequest request) {
    Device device =
        deviceRepository
            .findById(deviceId)
            .orElseThrow(() -> new AppError(HttpStatus.NOT_FOUND, "Device not found"));

    User owner = userService.findUserById(UUID.fromString(request.ownerId()));

    device.setOwner(owner);

    if (request.zoneId() != null) {
      device.setLastZone(zoneService.findZoneById(request.zoneId()));
    }

    device = deviceRepository.save(device);

    return mapToDeviceResponse(device);
  }

  @Override
  @Transactional
  public void deleteDevice(UUID deviceId) {
    Device device =
        deviceRepository
            .findById(deviceId)
            .orElseThrow(() -> new AppError(HttpStatus.NOT_FOUND, "Device not found"));

    deviceRepository.delete(device);
  }

  @Override
  @Transactional
  public DeviceResponse handleSos(String macAddress) {
    Device device =
        deviceRepository
            .findByMacAddress(macAddress)
            .orElseThrow(() -> new AppError(HttpStatus.NOT_FOUND, "Device not found"));

    device.setStatus(DeviceStatus.SOS);
    device = deviceRepository.save(device);

    return mapToDeviceResponse(device);
  }

  @Override
  @Transactional
  public DeviceResponse handleOffline(String macAddress) {
    Device device =
        deviceRepository
            .findByMacAddress(macAddress)
            .orElseThrow(() -> new AppError(HttpStatus.NOT_FOUND, "Device not found"));

    device.setStatus(DeviceStatus.OFFLINE);
    device = deviceRepository.save(device);

    return mapToDeviceResponse(device);
  }

  @Override
  public Device findDeviceByMacAddress(String macAddress) {
    return deviceRepository
        .findByMacAddress(macAddress)
        .orElseThrow(() -> new AppError(HttpStatus.NOT_FOUND, "Device not found"));
  }

  @Override
  public Device findDeviceById(UUID deviceId) {
    return deviceRepository
        .findById(deviceId)
        .orElseThrow(() -> new AppError(HttpStatus.NOT_FOUND, "Device not found"));
  }

  @Override
  @Transactional
  public int incrementViolationCount(String macAddress) {
    int updatedRows = deviceRepository.incrementViolationCount(macAddress);
    if (updatedRows == 0) {
      throw new AppError(HttpStatus.NOT_FOUND, "Device not found");
    }
    return updatedRows;
  }

  @Override
  @Transactional
  public DeviceResponse updateDeviceZone(String macAddress, UUID zoneId) {
    Device device = findDeviceByMacAddress(macAddress);
    device.setLastZone(zoneService.findZoneById(zoneId));
    device = deviceRepository.save(device);
    return mapToDeviceResponse(device);
  }

  private DeviceResponse mapToDeviceResponse(Device device) {
    return new DeviceResponse(
        device.getId(),
        device.getMacAddress(),
        device.getOwner() != null ? device.getOwner().getId() : null,
        device.getLastZone() != null ? device.getLastZone().getId() : null,
        device.getLastZone() != null ? device.getLastZone().getName() : null,
        device.getLastSeenTimestamp(),
        device.getStatus(),
        device.getViolationCount() != null ? device.getViolationCount() : 0);
  }
}

