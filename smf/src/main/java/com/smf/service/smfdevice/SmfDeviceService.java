package com.smf.service.smfdevice;

import com.smf.dto.device.SmfDeviceRequest;
import com.smf.dto.device.SmfDeviceResponse;
import com.smf.model.SmfDevice;
import com.smf.repo.SmfDeviceRepository;
import com.smf.util.AppError;
import com.smf.util.EncryptionUtil;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmfDeviceService implements ISmfDeviceService {

  private final SmfDeviceRepository smfDeviceRepository;
  private final EncryptionUtil encryptionUtil;

  @Override
  public List<SmfDeviceResponse> getAllDevices() {
    return smfDeviceRepository.findAll().stream().map(this::toResponse).toList();
  }

  @Override
  public List<SmfDeviceResponse> getUnregisteredDevices() {
    return smfDeviceRepository.findAllByIsRegisteredFalse().stream().map(this::toResponse).toList();
  }

  @Override
  public SmfDeviceResponse getDeviceById(UUID id) {
    return toResponse(
        smfDeviceRepository
            .findById(id)
            .orElseThrow(() -> new AppError(HttpStatus.NOT_FOUND, "SMF Device not found")));
  }

  @Override
  public SmfDeviceResponse getByLabel(String label) {
    return toResponse(
        smfDeviceRepository
            .findByLabel(label)
            .orElseThrow(() -> new AppError(HttpStatus.NOT_FOUND, "SMF Device not found")));
  }

  @Override
  public SmfDeviceResponse addDevice(SmfDeviceRequest request) {
    if (smfDeviceRepository.existsByMacAddress(request.macAddress())) {
      throw new AppError(HttpStatus.CONFLICT, "Device with this MAC address already exists");
    }
    SmfDevice saved =
        smfDeviceRepository.save(
            new SmfDevice(
                request.macAddress(), request.label(), encryptionUtil.encrypt(request.secret())));
    return toResponse(saved);
  }

  @Override
  public void markAsRegistered(UUID id) {
    SmfDevice device = getEntityDeviceById(id);
    device.setRegistered(true);
    smfDeviceRepository.save(device);
  }

  @Override
  public void removeDevice(UUID id) {
    SmfDevice device = getEntityDeviceById(id);
    smfDeviceRepository.delete(device);
  }

  public SmfDevice getEntityDeviceById(UUID id) {
    return smfDeviceRepository
        .findById(id)
        .orElseThrow(() -> new AppError(HttpStatus.NOT_FOUND, "SMF Device not found"));
  }

  private SmfDeviceResponse toResponse(SmfDevice device) {
    return new SmfDeviceResponse(
        device.getId(),
        device.getMacAddress(),
        device.getLabel(),
        device.isRegistered(),
        device.getCreatedAt());
  }
}
