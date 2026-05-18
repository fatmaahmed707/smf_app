package com.smf.repo;

import com.smf.model.SmfDevice;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SmfDeviceRepository extends JpaRepository<SmfDevice, UUID> {
  boolean existsByMacAddress(String macAddress);

  Optional<SmfDevice> findByLabel(String label);

  Optional<SmfDevice> findByMacAddress(String macAddress);

  List<SmfDevice> findAllByIsRegisteredFalse();
}
