package com.smf.dto.zone;

import jakarta.validation.constraints.NotBlank;

public record ZoneRequest(@NotBlank String name) {}
