package com.smf.service.zone;

import com.smf.dto.zone.ZoneAccessResult;
import com.smf.dto.zone.ZoneEntryRequest;
import com.smf.dto.zone.ZoneRequest;
import com.smf.dto.zone.ZoneResponse;
import com.smf.model.Zone;
import java.util.List;
import java.util.UUID;

public interface IZoneService {
  ZoneResponse createZone(ZoneRequest request);

  ZoneResponse getZoneById(UUID id);

  List<ZoneResponse> searchByName(String name);

  List<ZoneResponse> getAllZones();

  ZoneResponse updateZone(UUID id, ZoneRequest request);

  void deleteZone(UUID id);

  boolean canDeviceAccessZone(UUID deviceId, UUID zoneId);

  List<ZoneResponse> getAccessibleZones(UUID deviceId);

  void assignRoleToZone(UUID zoneId, Long roleId);

  void removeRoleFromZone(UUID zoneId, Long roleId);

  Zone findZoneById(UUID zoneId);

  ZoneAccessResult checkZoneAccess(String macAddress, ZoneEntryRequest request);
}
