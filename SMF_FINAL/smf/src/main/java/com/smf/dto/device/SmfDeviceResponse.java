package com.smf.dto.device;

import java.time.LocalDateTime;
import java.util.UUID;

public record SmfDeviceResponse(
    UUID id,
    String macAddress,
    String label,
    boolean isRegistered,
    LocalDateTime createdAt) {}
