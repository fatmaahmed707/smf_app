package com.smf.repo;

import com.smf.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.Optional;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {

	Optional<Device> findByMacAddress(String macAddress);

@Modifying
@Query("UPDATE Device d SET d.violationCount = COALESCE(d.violationCount, 0) + 1 WHERE d.macAddress = :macAddress")
  int incrementViolationCount(@Param("macAddress") String macAddress);
}
