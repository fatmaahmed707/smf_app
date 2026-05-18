package com.smf.dto.zone;

import java.util.UUID;

public record ZoneResponse(
    UUID id,
    String name
) {}
