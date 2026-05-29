package com.smf.dto.device;

import com.smf.model.enums.DeviceStatus;
import java.sql.Timestamp;
import java.util.UUID;

public record DeviceResponse(
    UUID id,
    String macAddress,
    UUID ownerId,
    UUID zoneId,
    String zoneName,
    Timestamp lastSeenTimestamp,
    DeviceStatus status,
    Integer violationCount) {}
