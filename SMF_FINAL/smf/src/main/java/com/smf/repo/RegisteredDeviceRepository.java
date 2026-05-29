package com.smf.repo;

import com.smf.model.RegisteredDevice;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegisteredDeviceRepository
    extends JpaRepository<RegisteredDevice, RegisteredDevice.RegisteredDeviceId> {
  Optional<RegisteredDevice> findByDeviceId(UUID deviceId);

  Optional<RegisteredDevice> findBySmfDeviceMacAddress(String macAddress);
}
