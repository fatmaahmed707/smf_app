package com.smf.dto.zone;

import java.util.Set;
import java.util.UUID;

public record ZoneAccessResult(
    boolean granted,
    UUID zoneId,
    String zoneName,
    Set<String> userRoles,
    Set<String> zoneAllowedRoles,
    String message) {}
