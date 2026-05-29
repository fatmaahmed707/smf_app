package com.smf.dto.device;

import com.smf.model.enums.EventTypes;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeviceEventRequest(@NotBlank String macAddress, @NotNull EventTypes event) {}

