package com.smf.dto.device;

import jakarta.validation.constraints.NotBlank;

public record SmfDeviceRequest(
    @NotBlank String macAddress,
    @NotBlank String label,
    @NotBlank String secret) {}
