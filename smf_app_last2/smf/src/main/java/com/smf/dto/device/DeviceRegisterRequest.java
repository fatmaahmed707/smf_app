package com.smf.dto.device;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record DeviceRegisterRequest(
    @NotBlank String smfDeviceLabel,
    @NotBlank String ownerId,
    UUID zoneId) {}

