import 'dart:math' as math;
import 'dart:ui';

import '../models/device_record.dart';
import '../models/event_log.dart';
import '../models/map_worker_marker.dart';
import '../models/map_zone_view_model.dart';
import '../models/user.dart';
import '../models/zone_layout_slot.dart';
import '../models/zone_summary.dart';
import 'zone_layout_registry.dart';

class MapZoneMapper {
  const MapZoneMapper();

  List<MapZoneViewModel> build({
    required List<ZoneSummary> zones,
    required List<User> users,
    required List<DeviceRecord> devices,
    required List<EventLog> events,
    required Brightness brightness,
  }) {
    final usedSlots = <String>{};
    final deviceByMac = {
      for (final device in devices) device.macAddress.toLowerCase(): device,
    };
    final userById = {for (final user in users) user.id: user};

    return zones.asMap().entries.map((entry) {
      final index = entry.key;
      final zone = entry.value;
      final slot = _resolveSlot(zone, index, usedSlots);
      final zoneEvents = _eventsForZone(zone, events);
      final workers = _buildWorkersForZone(
        zone: zone,
        slot: slot,
        zoneEvents: zoneEvents,
        users: users,
        userById: userById,
        deviceByMac: deviceByMac,
      );

      final emergencyCount =
          zone.emergencyCount ?? _countEvents(zoneEvents, _isEmergencyEvent);
      final warningCount =
          zone.warningCount ?? _countEvents(zoneEvents, _isWarningEvent);
      final totalWorkers = zone.workersCount ?? workers.length;
      final safeCount = zone.safeCount ??
          math.max(0, totalWorkers - emergencyCount - warningCount);
      final status =
          _statusForZone(zone.status, safeCount, warningCount, emergencyCount);
      final statusColor = _statusColor(status, brightness);
      final latestEvent = zoneEvents.isEmpty ? null : zoneEvents.first;

      return MapZoneViewModel(
        id: zone.id,
        name: zone.name,
        type: zone.type ?? slot.defaultType,
        area: zone.area ?? slot.defaultArea,
        workersCount: math.max(totalWorkers, workers.length),
        safeCount: safeCount,
        warningCount: warningCount,
        emergencyCount: emergencyCount,
        status: status,
        statusColor: statusColor,
        layoutSlot: slot,
        latestEvent: latestEvent,
        workers: workers,
      );
    }).toList();
  }

  ZoneLayoutSlot _resolveSlot(
    ZoneSummary zone,
    int index,
    Set<String> usedSlots,
  ) {
    final directSlot = ZoneLayoutRegistry.findByZoneName(zone.name);
    if (directSlot != null && usedSlots.add(directSlot.key)) {
      return _overrideWithApiPosition(zone, directSlot);
    }

    for (final slot in ZoneLayoutRegistry.primarySlots) {
      if (usedSlots.add(slot.key)) {
        return _overrideWithApiPosition(zone, slot);
      }
    }

    final fallback = ZoneLayoutRegistry.fallbackAt(index);
    return _overrideWithApiPosition(zone, fallback);
  }

  ZoneLayoutSlot _overrideWithApiPosition(
    ZoneSummary zone,
    ZoneLayoutSlot base,
  ) {
    if (zone.positionX == null && zone.positionY == null) {
      return base;
    }

    final left =
        ((zone.positionX ?? base.footprint.left).clamp(0.02, 0.92) as num)
            .toDouble();
    final top =
        ((zone.positionY ?? base.footprint.top).clamp(0.02, 0.92) as num)
            .toDouble();
    final width = math.min(base.footprint.width, 0.22);
    final height = math.min(base.footprint.height, 0.20);
    final footprint = Rect.fromLTWH(
      math.min(left, 0.98 - width),
      math.min(top, 0.98 - height),
      width,
      height,
    );
    final workerField = Rect.fromLTWH(
      footprint.left + width * 0.18,
      footprint.top + height * 0.16,
      width * 0.58,
      height * 0.36,
    );
    final labelAnchor = Offset(
      footprint.left + (base.labelOnLeft ? -0.08 : width + 0.05),
      footprint.top - 0.06,
    );

    return ZoneLayoutSlot(
      key: base.key,
      visualType: base.visualType,
      footprint: footprint,
      workerField: workerField,
      labelAnchor: labelAnchor,
      labelOnLeft: base.labelOnLeft,
      elevation: base.elevation,
      defaultType: base.defaultType,
      defaultArea: base.defaultArea,
    );
  }

  List<EventLog> _eventsForZone(ZoneSummary zone, List<EventLog> events) {
    final zoneName = _normalize(zone.name);
    final related = events.where((event) {
      final candidate = _normalize(event.zoneName ?? '');
      return candidate.isNotEmpty &&
          (candidate == zoneName ||
              candidate.contains(zoneName) ||
              zoneName.contains(candidate));
    }).toList();

    related.sort((a, b) => (b.createdAt ?? DateTime(1970))
        .compareTo(a.createdAt ?? DateTime(1970)));
    return related;
  }

  List<MapWorkerMarker> _buildWorkersForZone({
    required ZoneSummary zone,
    required ZoneLayoutSlot slot,
    required List<EventLog> zoneEvents,
    required List<User> users,
    required Map<String, User> userById,
    required Map<String, DeviceRecord> deviceByMac,
  }) {
    final markers = <MapWorkerMarker>[];
    final seen = <String>{};

    void addWorker({
      required String id,
      required String name,
      String? avatarUrl,
      String? role,
      String? locationLabel,
      String? deviceLabel,
      String? status,
      double? x,
      double? y,
    }) {
      final workerId = id.isEmpty ? name : id;
      if (!seen.add(workerId)) return;
      final generated = _workerOffset(
        workerId,
        slot.workerField,
        preferredX: x,
        preferredY: y,
      );
      markers.add(
        MapWorkerMarker(
          id: workerId,
          name: name,
          status: status ?? _workerStatusFromEvents(zoneEvents, workerId),
          avatarUrl: avatarUrl,
          role: role,
          locationLabel: locationLabel,
          offsetDx: generated.dx,
          offsetDy: generated.dy,
          deviceLabel: deviceLabel,
        ),
      );
    }

    for (final worker in zone.workers) {
      addWorker(
        id: worker.id,
        name: worker.name,
        avatarUrl: worker.avatarUrl,
        locationLabel: worker.location,
        status: worker.status,
        x: worker.positionX,
        y: worker.positionY,
      );
    }

    for (final user in users) {
      if (_matchesZone(user.location, zone.name)) {
        addWorker(
          id: user.id,
          name: user.name,
          avatarUrl: user.pictureUrl,
          role: user.role,
          locationLabel: user.location,
        );
      }
    }

    for (final event in zoneEvents) {
      final device = deviceByMac[event.macAddress.toLowerCase()];
      if (device == null) continue;
      final user = userById[device.ownerId];
      if (user == null) continue;
      addWorker(
        id: user.id,
        name: user.name,
        avatarUrl: user.pictureUrl,
        role: user.role,
        locationLabel: user.location,
        deviceLabel: device.label,
        status: _statusFromEventType(event.eventType),
      );
    }

    if (markers.isEmpty &&
        zone.workersCount != null &&
        zone.workersCount! > 0) {
      for (var i = 0; i < zone.workersCount!; i++) {
        addWorker(
          id: '${zone.id}_worker_$i',
          name: 'Worker ${i + 1}',
          status: i < zone.emergencyCountOrZero
              ? 'emergency'
              : i < zone.emergencyCountOrZero + zone.warningCountOrZero
                  ? 'warning'
                  : 'safe',
        );
      }
    }

    return markers;
  }

  Offset _workerOffset(
    String seed,
    Rect field, {
    double? preferredX,
    double? preferredY,
  }) {
    final left = field.left;
    final top = field.top;
    final width = field.width;
    final height = field.height;

    if (preferredX != null && preferredY != null) {
      return Offset(
        left + preferredX.clamp(0, 1) * width,
        top + preferredY.clamp(0, 1) * height,
      );
    }

    final hash = seed.runes.fold<int>(0, (sum, char) => sum + char);
    final dx = ((hash % 11) / 10) * width;
    final dy = (((hash ~/ 11) % 7) / 6) * height;
    return Offset(left + dx, top + dy);
  }

  bool _matchesZone(String? location, String zoneName) {
    if (location == null || location.trim().isEmpty) return false;
    final normalizedLocation = _normalize(location);
    final normalizedZone = _normalize(zoneName);
    return normalizedLocation.contains(normalizedZone) ||
        normalizedZone.contains(normalizedLocation);
  }

  int _countEvents(
    List<EventLog> events,
    bool Function(String eventType) matcher,
  ) {
    return events.where((event) => matcher(event.eventType)).length;
  }

  bool _isEmergencyEvent(String eventType) {
    return eventType == 'SOS_TRIGGERED' || eventType == 'ACCESS_DENIED';
  }

  bool _isWarningEvent(String eventType) {
    return eventType == 'DEVICE_OFFLINE';
  }

  String _statusForZone(
    String? explicitStatus,
    int safeCount,
    int warningCount,
    int emergencyCount,
  ) {
    final normalized = explicitStatus?.trim().toLowerCase();
    if (normalized != null && normalized.isNotEmpty) {
      return normalized;
    }
    if (emergencyCount > 0) return 'emergency';
    if (warningCount > 0) return 'warning';
    if (safeCount > 0) return 'safe';
    return 'offline';
  }

  Color _statusColor(String status, Brightness brightness) {
    switch (status) {
      case 'emergency':
        return brightness == Brightness.dark
            ? const Color(0xFFFF4343)
            : const Color(0xFFF04438);
      case 'warning':
        return brightness == Brightness.dark
            ? const Color(0xFFFFA320)
            : const Color(0xFFF79009);
      case 'offline':
        return brightness == Brightness.dark
            ? const Color(0xFF9AA4B2)
            : const Color(0xFF98A2B3);
      default:
        return brightness == Brightness.dark
            ? const Color(0xFF22D96B)
            : const Color(0xFF16A34A);
    }
  }

  String _workerStatusFromEvents(List<EventLog> zoneEvents, String workerId) {
    for (final event in zoneEvents) {
      final metaUserId = event.metadata['userId']?.toString();
      if (metaUserId == null || metaUserId != workerId) {
        continue;
      }
      return _statusFromEventType(event.eventType);
    }
    return 'safe';
  }

  String _statusFromEventType(String eventType) {
    switch (eventType) {
      case 'SOS_TRIGGERED':
      case 'ACCESS_DENIED':
        return 'emergency';
      case 'DEVICE_OFFLINE':
        return 'warning';
      case 'DEVICE_ONLINE':
      case 'ACCESS_GRANTED':
        return 'safe';
      default:
        return 'safe';
    }
  }

  String _normalize(String value) {
    return value.trim().toLowerCase().replaceAll(RegExp(r'\s+'), ' ');
  }
}

extension on ZoneSummary {
  int get warningCountOrZero => warningCount ?? 0;
  int get emergencyCountOrZero => emergencyCount ?? 0;
}
