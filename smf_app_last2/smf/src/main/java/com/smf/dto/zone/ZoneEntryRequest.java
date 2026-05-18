package com.smf.dto.zone;

import java.util.UUID;

// NOTE: zoneId is temporary, will be replaced with coordinate-based zone detection
public record ZoneEntryRequest(UUID zoneId) {}
