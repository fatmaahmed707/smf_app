import 'dart:math' as math;

import 'package:flutter/material.dart';

import '../../data/map_zone_mapper.dart';
import '../../models/device_record.dart';
import '../../models/event_log.dart';
import '../../models/map_worker_marker.dart';
import '../../models/map_zone_view_model.dart';
import '../../models/smf_device.dart';
import '../../models/user.dart';
import '../../models/zone_layout_slot.dart';
import '../../models/zone_summary.dart';
import '../../services/api_service.dart';
import '../../services/devices_service.dart';
import '../../services/events_service.dart';
import '../../services/smf_devices_service.dart';
import '../../services/users_service.dart';
import '../../services/zones_service.dart';
import '../../widgets/campus_map_canvas.dart';
import '../../widgets/worker_marker_chip.dart';
import '../../widgets/zone_details_panel.dart';

class MapOverviewPage extends StatefulWidget {
  const MapOverviewPage({super.key});

  @override
  State<MapOverviewPage> createState() => _MapOverviewPageState();
}

class _MapOverviewPageState extends State<MapOverviewPage> {
  final ZonesService _zonesService = ZonesService();
  final UsersService _usersService = UsersService();
  final DevicesService _devicesService = DevicesService();
  final EventsService _eventsService = EventsService();
  final SmfDevicesService _smfDevicesService = SmfDevicesService();
  final MapZoneMapper _mapper = const MapZoneMapper();

  bool _isLoading = true;
  String? _errorMessage;

  List<User> _users = const [];
  List<DeviceRecord> _devices = const [];
  List<SmfDevice> _smfDevices = const [];
  List<EventLog> _events = const [];
  List<MapZoneViewModel> _viewZones = const [];

  String? _selectedZoneId;
  String? _selectedWorkerId;

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    List<ZoneSummary> zones = const [];
    List<User> users = const [];
    List<DeviceRecord> devices = const [];
    List<SmfDevice> smfDevices = const [];
    List<EventLog> events = const [];
    String? error;

    try {
      zones = await _zonesService.getZones();
    } on ApiException catch (e) {
      error = e.message;
    } catch (_) {
      error = 'Failed to load zones.';
    }

    try {
      users = await _usersService.getUsers();
    } on ApiException catch (_) {
      users = const [];
    } catch (_) {
      users = const [];
    }

    try {
      devices = await _devicesService.getDevices();
    } on ApiException catch (_) {
      devices = const [];
    } catch (_) {
      devices = const [];
    }

    try {
      smfDevices = await _smfDevicesService.getAllDevices();
    } on ApiException catch (_) {
      smfDevices = const [];
    } catch (_) {
      smfDevices = const [];
    }

    try {
      events = await _eventsService.getEvents(since: 3600 * 24);
    } on ApiException catch (_) {
      events = const [];
    } catch (_) {
      events = const [];
    }

    if (!mounted) return;

    final brightness = Theme.of(context).brightness;
    final mapped = _mapper
        .build(
          zones: zones,
          users: users,
          devices: devices,
          events: events,
          brightness: brightness,
        )
        .take(3)
        .toList();

    setState(() {
      _users = users;
      _devices = devices;
      _smfDevices = smfDevices;
      _events = events;
      _viewZones = mapped;
      _errorMessage = error;
      _isLoading = false;
      _selectedZoneId = mapped.any((zone) => zone.id == _selectedZoneId)
          ? _selectedZoneId
          : mapped.firstOrNull?.id;
      final selectedZone = _selectedZone;
      final selectedWorkers = selectedZone == null
          ? const <MapWorkerMarker>[]
          : _workersForSingleZone(selectedZone);
      _selectedWorkerId =
          selectedWorkers.any((worker) => worker.id == _selectedWorkerId) ==
                  true
              ? _selectedWorkerId
              : selectedWorkers.firstOrNull?.id;
    });
  }

  MapZoneViewModel? get _selectedZone {
    for (final zone in _viewZones) {
      if (zone.id == _selectedZoneId) return zone;
    }
    return _viewZones.firstOrNull;
  }

  MapWorkerMarker? get _selectedWorker {
    final zone = _selectedZone;
    if (zone == null) return null;
    final workers = _workersForSingleZone(zone);
    for (final worker in workers) {
      if (worker.id == _selectedWorkerId) return worker;
    }
    return workers.firstOrNull;
  }

  void _selectZone(MapZoneViewModel zone) {
    setState(() {
      _selectedZoneId = zone.id;
      _selectedWorkerId = _workersForSingleZone(zone).firstOrNull?.id;
    });
  }

  List<MapWorkerMarker> _workersForSingleZone(MapZoneViewModel zone) {
    if (zone.workers.isNotEmpty) {
      return zone.workers;
    }

    final userById = {for (final user in _users) user.id: user};
    final smfByMac = {
      for (final device in _smfDevices) device.macAddress.toLowerCase(): device,
    };

    return _devices.asMap().entries.map((entry) {
      final index = entry.key;
      final device = entry.value;
      final user = userById[device.ownerId];
      final smfDevice = smfByMac[device.macAddress.toLowerCase()];
      return MapWorkerMarker(
        id: user?.id.isNotEmpty == true ? user!.id : device.id,
        name: user?.name.isNotEmpty == true
            ? user!.name
            : (smfDevice?.label.trim().isNotEmpty == true
                ? smfDevice!.label
                : 'Monitored device'),
        status: _workerStatusFromDevice(device.status),
        role: user?.role,
        locationLabel: zone.name,
        deviceLabel: smfDevice?.label.trim().isNotEmpty == true
            ? smfDevice!.label
            : 'Worker device',
        offsetDx: _markerOffset(index, axis: 0),
        offsetDy: _markerOffset(index, axis: 1),
        avatarUrl: user?.pictureUrl,
      );
    }).toList();
  }

  double _markerOffset(int index, {required int axis}) {
    const positions = [
      [0.26, 0.32],
      [0.44, 0.24],
      [0.62, 0.34],
      [0.36, 0.52],
      [0.56, 0.56],
      [0.72, 0.48],
      [0.25, 0.68],
      [0.49, 0.74],
      [0.67, 0.70],
    ];
    final item = positions[index % positions.length];
    return item[axis];
  }

  String _workerStatusFromDevice(String status) {
    switch (status.toUpperCase()) {
      case 'SOS':
      case 'EMERGENCY':
      case 'ACCESS_DENIED':
        return 'emergency';
      case 'OFFLINE':
      case 'WARNING':
        return 'warning';
      default:
        return 'safe';
    }
  }

  Future<void> _openZonePanel(MapZoneViewModel zone) async {
    _selectZone(zone);
    if (MediaQuery.of(context).size.width >= 1100) {
      return;
    }

    await showModalBottomSheet<void>(
      context: context,
      backgroundColor: Colors.transparent,
      isScrollControlled: true,
      builder: (context) => Padding(
        padding: const EdgeInsets.all(16),
        child: ZoneDetailsPanel(
          zone: zone,
          palette: CampusMapPalette.fromBrightness(
            Theme.of(context).brightness,
          ),
        ),
      ),
    );
  }

  Future<void> _openWorkerPanel(
    MapZoneViewModel zone,
    MapWorkerMarker worker,
  ) async {
    setState(() => _selectedWorkerId = worker.id);
    await showModalBottomSheet<void>(
      context: context,
      backgroundColor: Colors.transparent,
      builder: (context) => Padding(
        padding: const EdgeInsets.all(16),
        child: _SelectedWorkerPanel(
          palette: CampusMapPalette.fromBrightness(
            Theme.of(context).brightness,
          ),
          zone: zone,
          worker: worker,
          workers: _workersForSingleZone(zone),
          onPickWorker: (item) {
            Navigator.pop(context);
            setState(() => _selectedWorkerId = item.id);
          },
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final palette =
        CampusMapPalette.fromBrightness(Theme.of(context).brightness);
    final width = MediaQuery.of(context).size.width;
    final isDesktop = width >= 1320;
    final isWide = width >= 1100;

    if (_isLoading) {
      return const Center(child: CircularProgressIndicator());
    }

    if (_errorMessage != null && _viewZones.isEmpty) {
      return _MapErrorState(
        message: _errorMessage!,
        palette: palette,
        onRetry: _loadData,
      );
    }

    final selectedZone = _selectedZone;
    final selectedWorker = _selectedWorker;

    return SingleChildScrollView(
      padding: const EdgeInsets.all(18),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          if (width >= 1450)
            Row(
              children: [
                Expanded(child: _buildMetricsRow(palette)),
                const SizedBox(width: 18),
                _LiveMonitoringPill(palette: palette),
              ],
            )
          else
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                _LiveMonitoringPill(palette: palette),
                const SizedBox(height: 14),
                _buildMetricsRow(palette),
              ],
            ),
          const SizedBox(height: 18),
          if (isDesktop)
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Expanded(
                  flex: 10,
                  child: Column(
                    children: [
                      _buildMapSection(
                        palette: palette,
                        showInlinePanel: true,
                        selectedZone: selectedZone,
                        selectedWorker: selectedWorker,
                      ),
                    ],
                  ),
                ),
                const SizedBox(width: 18),
                SizedBox(
                  width: 318,
                  child: _buildRightRail(
                    palette: palette,
                    selectedZone: selectedZone,
                  ),
                ),
              ],
            )
          else
            Column(
              children: [
                _buildMapSection(
                  palette: palette,
                  showInlinePanel: isWide,
                  selectedZone: selectedZone,
                  selectedWorker: selectedWorker,
                ),
                const SizedBox(height: 18),
                _buildRightRail(
                  palette: palette,
                  selectedZone: selectedZone,
                ),
              ],
            ),
        ],
      ),
    );
  }

  Widget _buildMetricsRow(CampusMapPalette palette) {
    final selectedZone = _selectedZone;
    final workers = selectedZone == null
        ? const <MapWorkerMarker>[]
        : _workersForSingleZone(selectedZone);
    final totalWorkers = workers.isNotEmpty ? workers.length : _users.length;
    final safe =
        workers.where((worker) => worker.status.toLowerCase() == 'safe').length;
    final warning = workers
        .where((worker) => worker.status.toLowerCase() == 'warning')
        .length;
    final emergency = workers
        .where((worker) => worker.status.toLowerCase() == 'emergency')
        .length;
    final devicesOnline = _devices
        .where((device) => device.status.toUpperCase() != 'OFFLINE')
        .length;

    final cards = [
      _MetricCardData(
        title: 'TOTAL WORKERS',
        value: '$totalWorkers',
        secondary: 'Online ${_users.length}',
        accent: palette.accentBlue,
        icon: Icons.groups_2_outlined,
      ),
      _MetricCardData(
        title: 'SAFE',
        value: '$safe',
        secondary: _percentLabel(safe, totalWorkers),
        accent: palette.metricSafe,
        icon: Icons.shield_outlined,
      ),
      _MetricCardData(
        title: 'WARNING',
        value: '$warning',
        secondary: _percentLabel(warning, totalWorkers),
        accent: palette.metricWarning,
        icon: Icons.warning_amber_rounded,
      ),
      _MetricCardData(
        title: 'EMERGENCY',
        value: '$emergency',
        secondary: _percentLabel(emergency, totalWorkers),
        accent: palette.metricEmergency,
        icon: Icons.notification_important_outlined,
      ),
      _MetricCardData(
        title: 'DEVICES',
        value: '${_devices.length}',
        secondary: 'Registry ${_smfDevices.length} / Online $devicesOnline',
        accent: const Color(0xFF7C3AED),
        icon: Icons.desktop_windows_outlined,
      ),
    ];

    return Wrap(
      spacing: 14,
      runSpacing: 14,
      children: cards
          .map((card) => _MetricCard(palette: palette, data: card))
          .toList(),
    );
  }

  String _percentLabel(int count, int total) {
    if (total <= 0) return '0%';
    return '${((count / total) * 100).round()}%';
  }

  Widget _buildMapSection({
    required CampusMapPalette palette,
    required bool showInlinePanel,
    required MapZoneViewModel? selectedZone,
    required MapWorkerMarker? selectedWorker,
  }) {
    if (selectedZone == null) {
      return _MapErrorState(
        message: 'No monitored zones available yet.',
        palette: palette,
        onRetry: _loadData,
      );
    }

    final workers = _workersForSingleZone(selectedZone);

    return Column(
      children: [
        _SingleZoneBuildingMonitor(
          palette: palette,
          zone: selectedZone,
          zones: _viewZones,
          workers: workers,
          deviceCount: _devices.length,
          registryCount: _smfDevices.length,
          selectedWorkerId: selectedWorker?.id,
          selectedZoneId: selectedZone.id,
          onSelectZone: _selectZone,
          onSelectWorker: (worker) {
            setState(() => _selectedWorkerId = worker.id);
            if (!showInlinePanel) {
              _openWorkerPanel(selectedZone, worker);
            }
          },
        ),
        if (showInlinePanel) ...[
          const SizedBox(height: 16),
          _SelectedWorkerPanel(
            palette: palette,
            zone: selectedZone,
            worker: selectedWorker,
            workers: workers,
            onPickWorker: (worker) {
              setState(() => _selectedWorkerId = worker.id);
            },
          ),
        ],
      ],
    );
  }

  Widget _buildRightRail({
    required CampusMapPalette palette,
    required MapZoneViewModel? selectedZone,
  }) {
    final sortedEvents = [..._events]..sort((a, b) =>
        (b.createdAt ?? DateTime(1970))
            .compareTo(a.createdAt ?? DateTime(1970)));

    return Column(
      children: [
        _CampusOverviewPanel(
          palette: palette,
          zones: _viewZones,
          selectedZoneId: selectedZone?.id,
          onSelectZone: (zone) => _openZonePanel(zone),
        ),
        const SizedBox(height: 16),
        _RecentEventsPanel(
          palette: palette,
          events: sortedEvents.take(8).toList(),
        ),
        const SizedBox(height: 16),
        _ActiveWorkersPanel(
          palette: palette,
          activeWorkers: _users.length,
          totalWorkers:
              _viewZones.fold<int>(0, (sum, zone) => sum + zone.workersCount),
        ),
      ],
    );
  }
}

class _SingleZoneBuildingMonitor extends StatelessWidget {
  final CampusMapPalette palette;
  final MapZoneViewModel zone;
  final List<MapZoneViewModel> zones;
  final List<MapWorkerMarker> workers;
  final int deviceCount;
  final int registryCount;
  final String? selectedWorkerId;
  final String? selectedZoneId;
  final ValueChanged<MapZoneViewModel> onSelectZone;
  final ValueChanged<MapWorkerMarker> onSelectWorker;

  const _SingleZoneBuildingMonitor({
    required this.palette,
    required this.zone,
    required this.zones,
    required this.workers,
    required this.deviceCount,
    required this.registryCount,
    required this.selectedWorkerId,
    required this.selectedZoneId,
    required this.onSelectZone,
    required this.onSelectWorker,
  });

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    return AspectRatio(
      aspectRatio: MediaQuery.of(context).size.width >= 900 ? 1.72 : 0.92,
      child: Container(
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(28),
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [
              palette.panelBackground,
              palette.pageBackground,
              isDark ? const Color(0xFF06111F) : const Color(0xFFEAF4FF),
            ],
          ),
          border: Border.all(color: zone.statusColor.withValues(alpha: 0.26)),
          boxShadow: [
            BoxShadow(
              color: zone.statusColor.withValues(alpha: 0.10),
              blurRadius: 34,
              offset: const Offset(0, 18),
            ),
          ],
        ),
        clipBehavior: Clip.antiAlias,
        child: LayoutBuilder(
          builder: (context, constraints) {
            final isCompact = constraints.maxWidth < 720;
            final displayWorkers = workers.isNotEmpty
                ? workers.take(isCompact ? 4 : 6).toList()
                : _fallbackWorkers(zone);
            final selectedZoneIndex = math.max(
              0,
              zones.indexWhere((item) => item.id == zone.id),
            );
            return Stack(
              children: [
                Positioned.fill(
                  child: ColorFiltered(
                    colorFilter: isDark
                        ? const ColorFilter.mode(
                            Colors.transparent,
                            BlendMode.dst,
                          )
                        : const ColorFilter.matrix([
                            1.18,
                            0,
                            0,
                            0,
                            18,
                            0,
                            1.18,
                            0,
                            0,
                            18,
                            0,
                            0,
                            1.18,
                            0,
                            20,
                            0,
                            0,
                            0,
                            1,
                            0,
                          ]),
                    child: Image.asset(
                      'assets/images/isometric_factory_map.png',
                      fit: BoxFit.cover,
                      alignment: Alignment.center,
                      filterQuality: FilterQuality.high,
                    ),
                  ),
                ),
                Positioned.fill(
                  child: DecoratedBox(
                    decoration: BoxDecoration(
                      gradient: isDark
                          ? RadialGradient(
                              center: const Alignment(0.08, -0.08),
                              radius: 0.92,
                              colors: [
                                Colors.transparent,
                                palette.pageBackground.withValues(alpha: 0.18),
                              ],
                            )
                          : LinearGradient(
                              begin: Alignment.topCenter,
                              end: Alignment.bottomCenter,
                              colors: [
                                Colors.white.withValues(alpha: 0.18),
                                palette.pageBackground.withValues(alpha: 0.30),
                              ],
                            ),
                    ),
                  ),
                ),
                Positioned.fill(
                  child: _ReferenceZoneLayer(
                    palette: palette,
                    zones: zones,
                    selectedZoneId: selectedZoneId,
                    onSelectZone: onSelectZone,
                  ),
                ),
                Positioned(
                  left: isCompact ? 18 : 24,
                  top: isCompact ? 18 : constraints.maxHeight * 0.30,
                  child: _BuildingInfoCallout(
                    palette: palette,
                    zone: zone,
                    workersCount: workers.isNotEmpty
                        ? workers.length
                        : math.max(zone.workersCount, displayWorkers.length),
                  ),
                ),
                Positioned(
                  right: isCompact ? 18 : 26,
                  top: isCompact ? 104 : constraints.maxHeight * 0.34,
                  child: _BuildingStatusCallout(
                    palette: palette,
                    zone: zone,
                  ),
                ),
                Positioned.fill(
                  child: Stack(
                    children: [
                      for (var i = 0; i < displayWorkers.length; i++)
                        _ReferenceWorkerMarker(
                          palette: palette,
                          worker: displayWorkers[i],
                          index: i,
                          zoneIndex: selectedZoneIndex,
                          totalWorkers: displayWorkers.length,
                          selected: displayWorkers[i].id == selectedWorkerId,
                          onTap: () => onSelectWorker(displayWorkers[i]),
                        ),
                    ],
                  ),
                ),
                Positioned(
                  left: 18,
                  right: 18,
                  bottom: 18,
                  child: _SingleZoneLegend(
                    palette: palette,
                    safe: workers
                        .where((item) => item.status.toLowerCase() == 'safe')
                        .length,
                    warning: workers
                        .where((item) => item.status.toLowerCase() == 'warning')
                        .length,
                    emergency: workers
                        .where(
                            (item) => item.status.toLowerCase() == 'emergency')
                        .length,
                    offline: workers.isEmpty ? deviceCount : 0,
                  ),
                ),
              ],
            );
          },
        ),
      ),
    );
  }

  List<MapWorkerMarker> _fallbackWorkers(MapZoneViewModel zone) {
    return List.generate(5, (index) {
      return MapWorkerMarker(
        id: 'preview-worker-$index',
        name: 'Worker ${index + 1}',
        status: 'safe',
        role: 'Operator',
        locationLabel: zone.name,
        deviceLabel: 'Wearable ${index + 1}',
        offsetDx: 0,
        offsetDy: 0,
      );
    });
  }
}

class _BuildingInfoCallout extends StatelessWidget {
  final CampusMapPalette palette;
  final MapZoneViewModel zone;
  final int workersCount;

  const _BuildingInfoCallout({
    required this.palette,
    required this.zone,
    required this.workersCount,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      constraints: const BoxConstraints(maxWidth: 284),
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: palette.pageBackground.withValues(alpha: 0.78),
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: palette.accentBlue.withValues(alpha: 0.70)),
        boxShadow: [
          BoxShadow(
            color: palette.accentBlue.withValues(alpha: 0.18),
            blurRadius: 24,
          ),
        ],
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(Icons.factory_outlined, color: palette.accentBlue, size: 44),
          const SizedBox(width: 16),
          Flexible(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisSize: MainAxisSize.min,
              children: [
                Text(
                  zone.name.isEmpty ? 'BUILDING A' : zone.name.toUpperCase(),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: TextStyle(
                    color: palette.accentBlue,
                    fontSize: 18,
                    fontWeight: FontWeight.w900,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  zone.area.isEmpty ? 'Production Area' : zone.area,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: TextStyle(
                    color: palette.textPrimary,
                    fontSize: 16,
                    fontWeight: FontWeight.w600,
                  ),
                ),
                const SizedBox(height: 10),
                Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Icon(Icons.groups_2_outlined,
                        color: palette.accentBlue, size: 22),
                    const SizedBox(width: 10),
                    Text(
                      '$workersCount Workers',
                      style: TextStyle(
                        color: palette.textPrimary,
                        fontSize: 16,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _BuildingStatusCallout extends StatelessWidget {
  final CampusMapPalette palette;
  final MapZoneViewModel zone;

  const _BuildingStatusCallout({
    required this.palette,
    required this.zone,
  });

  @override
  Widget build(BuildContext context) {
    final safe = zone.status.toLowerCase() == 'safe';
    final color = safe ? palette.metricSafe : zone.statusColor;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 16),
      decoration: BoxDecoration(
        color: palette.pageBackground.withValues(alpha: 0.78),
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: color.withValues(alpha: 0.72)),
        boxShadow: [
          BoxShadow(color: color.withValues(alpha: 0.16), blurRadius: 24),
        ],
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            width: 52,
            height: 52,
            decoration: BoxDecoration(
              color: color.withValues(alpha: 0.14),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Icon(Icons.verified_user_outlined, color: color, size: 32),
          ),
          const SizedBox(width: 14),
          Text(
            safe ? 'STATUS\nSAFE' : 'STATUS\n${zone.status.toUpperCase()}',
            style: TextStyle(
              color: color,
              height: 1.15,
              fontSize: 18,
              fontWeight: FontWeight.w900,
            ),
          ),
        ],
      ),
    );
  }
}

class _ReferenceZoneLayer extends StatelessWidget {
  final CampusMapPalette palette;
  final List<MapZoneViewModel> zones;
  final String? selectedZoneId;
  final ValueChanged<MapZoneViewModel> onSelectZone;

  const _ReferenceZoneLayer({
    required this.palette,
    required this.zones,
    required this.selectedZoneId,
    required this.onSelectZone,
  });

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(
      builder: (context, constraints) {
        final size = Size(constraints.maxWidth, constraints.maxHeight);
        return MouseRegion(
          cursor: SystemMouseCursors.click,
          child: GestureDetector(
            behavior: HitTestBehavior.translucent,
            onTapDown: (details) {
              final zone = _zoneAt(details.localPosition, size);
              if (zone != null) {
                onSelectZone(zone);
              }
            },
            child: CustomPaint(
              painter: _ReferenceZonePainter(
                palette: palette,
                zones: zones,
                selectedZoneId: selectedZoneId,
              ),
            ),
          ),
        );
      },
    );
  }

  MapZoneViewModel? _zoneAt(Offset point, Size size) {
    for (var index = zones.length - 1; index >= 0; index--) {
      if (_zonePathForIndex(index, size).contains(point)) {
        return zones[index];
      }
    }
    return null;
  }
}

class _ReferenceZonePainter extends CustomPainter {
  final CampusMapPalette palette;
  final List<MapZoneViewModel> zones;
  final String? selectedZoneId;

  const _ReferenceZonePainter({
    required this.palette,
    required this.zones,
    required this.selectedZoneId,
  });

  @override
  void paint(Canvas canvas, Size size) {
    for (var index = 0; index < zones.length; index++) {
      final zone = zones[index];
      final selected = zone.id == selectedZoneId;
      final path = _zonePathForIndex(index, size);
      final color = zone.statusColor;

      if (selected) {
        canvas.drawPath(
          path,
          Paint()
            ..color = color.withValues(alpha: 0.20)
            ..maskFilter = const MaskFilter.blur(BlurStyle.normal, 8),
        );
      }

      canvas.drawPath(
        path,
        Paint()
          ..color = color.withValues(alpha: selected ? 0.18 : 0.075)
          ..style = PaintingStyle.fill,
      );
      canvas.drawPath(
        path,
        Paint()
          ..color = color.withValues(alpha: selected ? 0.92 : 0.52)
          ..style = PaintingStyle.stroke
          ..strokeWidth = selected ? 2.4 : 1.4,
      );

      final center = _zoneCenterForIndex(index, size);
      canvas.drawCircle(
        center,
        selected ? 12 : 9,
        Paint()..color = palette.pageBackground.withValues(alpha: 0.86),
      );
      canvas.drawCircle(
        center,
        selected ? 12 : 9,
        Paint()
          ..color = color
          ..style = PaintingStyle.stroke
          ..strokeWidth = selected ? 2.4 : 1.8,
      );
      canvas.drawCircle(center, 4.2, Paint()..color = color);

      if (selected) {
        _drawSelectedLabel(canvas, size, zone, center, color);
      }
    }
  }

  void _drawSelectedLabel(
    Canvas canvas,
    Size size,
    MapZoneViewModel zone,
    Offset center,
    Color color,
  ) {
    final label = zone.name.trim().isEmpty ? 'Zone' : zone.name.trim();
    final textPainter = TextPainter(
      text: TextSpan(
        text: label,
        style: TextStyle(
          color: palette.textPrimary,
          fontSize: 12,
          fontWeight: FontWeight.w800,
        ),
      ),
      maxLines: 1,
      ellipsis: '...',
      textDirection: TextDirection.ltr,
    )..layout(maxWidth: 136);

    final rect = Rect.fromLTWH(
      (center.dx - textPainter.width / 2 - 9)
          .clamp(10, size.width - textPainter.width - 28),
      (center.dy + 16).clamp(10, size.height - 36),
      textPainter.width + 18,
      28,
    );
    final rrect = RRect.fromRectAndRadius(rect, const Radius.circular(9));
    canvas.drawRRect(
      rrect,
      Paint()..color = palette.pageBackground.withValues(alpha: 0.88),
    );
    canvas.drawRRect(
      rrect,
      Paint()
        ..color = color.withValues(alpha: 0.50)
        ..style = PaintingStyle.stroke,
    );
    textPainter.paint(canvas, Offset(rect.left + 9, rect.top + 6));
  }

  @override
  bool shouldRepaint(covariant _ReferenceZonePainter oldDelegate) {
    return oldDelegate.zones != zones ||
        oldDelegate.selectedZoneId != selectedZoneId ||
        oldDelegate.palette != palette;
  }
}

Path _zonePathForIndex(int index, Size size) {
  final points = _zonePoints[index % _zonePoints.length];
  final path = Path()
    ..moveTo(points.first.dx * size.width, points.first.dy * size.height);
  for (final point in points.skip(1)) {
    path.lineTo(point.dx * size.width, point.dy * size.height);
  }
  return path..close();
}

Offset _zoneCenterForIndex(int index, Size size) {
  final point = _zoneCenters[index % _zoneCenters.length];
  return Offset(point.dx * size.width, point.dy * size.height);
}

Offset _workerPointInZone({
  required int zoneIndex,
  required int workerIndex,
  required int totalWorkers,
}) {
  final points = _zonePoints[zoneIndex % _zonePoints.length];
  final topLeft = points[0];
  final topRight = points[1];
  final bottomRight = points[2];
  final bottomLeft = points[3];
  const anchors = [
    Offset(0.50, 0.48),
    Offset(0.32, 0.36),
    Offset(0.68, 0.38),
    Offset(0.40, 0.66),
    Offset(0.62, 0.66),
    Offset(0.50, 0.28),
  ];
  final anchor = anchors[workerIndex % anchors.length];
  final u = totalWorkers == 1
      ? 0.50
      : (anchor.dx + (workerIndex * 0.03)).clamp(0.26, 0.74).toDouble();
  final v = (anchor.dy + (workerIndex * 0.02)).clamp(0.24, 0.72).toDouble();
  final top = Offset.lerp(topLeft, topRight, u)!;
  final bottom = Offset.lerp(bottomLeft, bottomRight, u)!;
  return Offset.lerp(top, bottom, v)!;
}

const _zonePoints = [
  [
    Offset(0.06, 0.12),
    Offset(0.36, 0.12),
    Offset(0.36, 0.86),
    Offset(0.06, 0.86),
  ],
  [
    Offset(0.36, 0.12),
    Offset(0.66, 0.12),
    Offset(0.66, 0.86),
    Offset(0.36, 0.86),
  ],
  [
    Offset(0.66, 0.12),
    Offset(0.96, 0.12),
    Offset(0.96, 0.86),
    Offset(0.66, 0.86),
  ],
];

const _zoneCenters = [
  Offset(0.21, 0.49),
  Offset(0.51, 0.49),
  Offset(0.81, 0.49),
];

class _ReferenceWorkerMarker extends StatelessWidget {
  final CampusMapPalette palette;
  final MapWorkerMarker worker;
  final int index;
  final int zoneIndex;
  final int totalWorkers;
  final bool selected;
  final VoidCallback onTap;

  const _ReferenceWorkerMarker({
    required this.palette,
    required this.worker,
    required this.index,
    required this.zoneIndex,
    required this.totalWorkers,
    required this.selected,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final position = _workerPointInZone(
      zoneIndex: zoneIndex,
      workerIndex: index,
      totalWorkers: totalWorkers,
    );
    return Positioned.fill(
      child: Align(
        alignment: Alignment(position.dx * 2 - 1, position.dy * 2 - 1),
        child: GestureDetector(
          onTap: onTap,
          child: TweenAnimationBuilder<double>(
            tween: Tween(begin: 0.96, end: selected ? 1.10 : 1),
            duration: const Duration(milliseconds: 220),
            builder: (context, scale, child) =>
                Transform.scale(scale: scale, child: child),
            child: Stack(
              clipBehavior: Clip.none,
              children: [
                WorkerMarkerChip(
                  worker: worker,
                  palette: palette,
                  size: selected ? 66 : 58,
                ),
                Positioned(
                  right: 2,
                  bottom: 1,
                  child: Container(
                    width: 13,
                    height: 13,
                    decoration: BoxDecoration(
                      color: _workerColor(worker.status),
                      shape: BoxShape.circle,
                      border:
                          Border.all(color: palette.pageBackground, width: 2),
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Color _workerColor(String status) {
    switch (status.toLowerCase()) {
      case 'emergency':
        return palette.metricEmergency;
      case 'warning':
        return palette.metricWarning;
      case 'offline':
        return palette.metricOffline;
      default:
        return palette.metricSafe;
    }
  }
}

class _SingleZoneLegend extends StatelessWidget {
  final CampusMapPalette palette;
  final int safe;
  final int warning;
  final int emergency;
  final int offline;

  const _SingleZoneLegend({
    required this.palette,
    required this.safe,
    required this.warning,
    required this.emergency,
    required this.offline,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
      decoration: BoxDecoration(
        color: palette.panelBackground.withValues(alpha: 0.88),
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: palette.panelBorder),
      ),
      child: Wrap(
        spacing: 14,
        runSpacing: 8,
        alignment: WrapAlignment.center,
        children: [
          _legendItem('Safe', safe, palette.metricSafe),
          _legendItem('Warning', warning, palette.metricWarning),
          _legendItem('Emergency', emergency, palette.metricEmergency),
          _legendItem('Offline', offline, palette.metricOffline),
        ],
      ),
    );
  }

  Widget _legendItem(String label, int value, Color color) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Container(
          width: 9,
          height: 9,
          decoration: BoxDecoration(color: color, shape: BoxShape.circle),
        ),
        const SizedBox(width: 7),
        Text(
          label,
          style: TextStyle(
            color: palette.textSecondary,
            fontWeight: FontWeight.w700,
            fontSize: 12.5,
          ),
        ),
      ],
    );
  }
}

class _MapErrorState extends StatelessWidget {
  final String message;
  final CampusMapPalette palette;
  final VoidCallback onRetry;

  const _MapErrorState({
    required this.message,
    required this.palette,
    required this.onRetry,
  });

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Container(
        padding: const EdgeInsets.all(24),
        decoration: BoxDecoration(
          color: palette.panelBackground,
          borderRadius: BorderRadius.circular(22),
          border: Border.all(color: palette.panelBorder),
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              Icons.map_outlined,
              color: palette.textMuted,
              size: 42,
            ),
            const SizedBox(height: 14),
            Text(
              message,
              style: TextStyle(
                color: palette.textPrimary,
                fontWeight: FontWeight.w600,
              ),
            ),
            const SizedBox(height: 14),
            ElevatedButton(
              onPressed: onRetry,
              child: const Text('Retry'),
            ),
          ],
        ),
      ),
    );
  }
}

class _MetricCardData {
  final String title;
  final String value;
  final String secondary;
  final Color accent;
  final IconData icon;

  const _MetricCardData({
    required this.title,
    required this.value,
    required this.secondary,
    required this.accent,
    required this.icon,
  });
}

class _MetricCard extends StatelessWidget {
  final CampusMapPalette palette;
  final _MetricCardData data;

  const _MetricCard({
    required this.palette,
    required this.data,
  });

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: 240,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
        decoration: BoxDecoration(
          color: palette.panelBackground,
          borderRadius: BorderRadius.circular(20),
          border: Border.all(
            color: data.accent.withValues(alpha: 0.24),
          ),
          boxShadow: [
            BoxShadow(
              color: data.accent.withValues(alpha: 0.07),
              blurRadius: 20,
              offset: const Offset(0, 10),
            ),
          ],
        ),
        child: Row(
          children: [
            Container(
              width: 48,
              height: 48,
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(14),
                color: data.accent.withValues(alpha: 0.12),
              ),
              child: Icon(
                data.icon,
                color: data.accent,
                size: 26,
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    data.title,
                    style: TextStyle(
                      color: palette.textMuted,
                      fontSize: 12,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                  const SizedBox(height: 6),
                  Row(
                    children: [
                      Text(
                        data.value,
                        style: TextStyle(
                          color: palette.textPrimary,
                          fontSize: 22,
                          fontWeight: FontWeight.w800,
                        ),
                      ),
                      const Spacer(),
                      Text(
                        data.secondary,
                        style: TextStyle(
                          color: data.accent,
                          fontSize: 13,
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _LiveMonitoringPill extends StatelessWidget {
  final CampusMapPalette palette;

  const _LiveMonitoringPill({required this.palette});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
      decoration: BoxDecoration(
        color: palette.panelBackground,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: palette.panelBorder),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            width: 12,
            height: 12,
            decoration: BoxDecoration(
              color: palette.metricSafe,
              shape: BoxShape.circle,
            ),
          ),
          const SizedBox(width: 12),
          Text(
            'LIVE MONITORING',
            style: TextStyle(
              color: palette.textSecondary,
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(width: 14),
          SizedBox(
            width: 160,
            height: 24,
            child: CustomPaint(
              painter: _LinePulsePainter(color: palette.metricSafe),
            ),
          ),
        ],
      ),
    );
  }
}

class _CampusOverviewPanel extends StatelessWidget {
  final CampusMapPalette palette;
  final List<MapZoneViewModel> zones;
  final String? selectedZoneId;
  final ValueChanged<MapZoneViewModel> onSelectZone;

  const _CampusOverviewPanel({
    required this.palette,
    required this.zones,
    required this.selectedZoneId,
    required this.onSelectZone,
  });

  @override
  Widget build(BuildContext context) {
    return _SidePanel(
      palette: palette,
      title: 'ZONE OVERVIEW',
      child: Column(
        children: [
          ...zones.map(
            (zone) => GestureDetector(
              onTap: () => onSelectZone(zone),
              child: AnimatedContainer(
                duration: const Duration(milliseconds: 160),
                margin: const EdgeInsets.only(bottom: 10),
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: selectedZoneId == zone.id
                      ? zone.statusColor.withValues(alpha: 0.10)
                      : palette.track,
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(
                    color: selectedZoneId == zone.id
                        ? zone.statusColor.withValues(alpha: 0.42)
                        : palette.panelBorder,
                  ),
                ),
                child: Row(
                  children: [
                    Container(
                      width: 34,
                      height: 34,
                      decoration: BoxDecoration(
                        borderRadius: BorderRadius.circular(12),
                        color: zone.statusColor.withValues(alpha: 0.14),
                      ),
                      child: Icon(
                        _zoneIcon(zone.layoutSlot.visualType),
                        color: zone.statusColor,
                        size: 18,
                      ),
                    ),
                    const SizedBox(width: 10),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            zone.name,
                            style: TextStyle(
                              color: palette.textPrimary,
                              fontWeight: FontWeight.w700,
                            ),
                          ),
                          const SizedBox(height: 2),
                          Text(
                            zone.area,
                            style: TextStyle(
                              color: palette.textMuted,
                              fontSize: 12.5,
                            ),
                          ),
                        ],
                      ),
                    ),
                    _CountColumn(
                      label: 'W',
                      value: zone.workersCount,
                      color: palette.textPrimary,
                    ),
                    _CountColumn(
                      label: 'S',
                      value: zone.safeCount,
                      color: palette.metricSafe,
                    ),
                    _CountColumn(
                      label: 'W',
                      value: zone.warningCount,
                      color: palette.metricWarning,
                    ),
                    _CountColumn(
                      label: 'E',
                      value: zone.emergencyCount,
                      color: palette.metricEmergency,
                    ),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  IconData _zoneIcon(ZoneVisualType type) {
    switch (type) {
      case ZoneVisualType.building:
        return Icons.corporate_fare_outlined;
      case ZoneVisualType.court:
        return Icons.sports_basketball_outlined;
      case ZoneVisualType.gate:
        return Icons.shield_outlined;
      case ZoneVisualType.restricted:
        return Icons.lock_outline_rounded;
      case ZoneVisualType.assembly:
        return Icons.groups_2_outlined;
      case ZoneVisualType.utility:
        return Icons.electrical_services_outlined;
      case ZoneVisualType.generic:
        return Icons.place_outlined;
    }
  }
}

class _RecentEventsPanel extends StatelessWidget {
  final CampusMapPalette palette;
  final List<EventLog> events;

  const _RecentEventsPanel({
    required this.palette,
    required this.events,
  });

  @override
  Widget build(BuildContext context) {
    return _SidePanel(
      palette: palette,
      title: 'RECENT EVENTS',
      trailing: Text(
        'View All',
        style: TextStyle(
          color: palette.accentBlue,
          fontWeight: FontWeight.w700,
          fontSize: 12.5,
        ),
      ),
      child: Column(
        children: [
          if (events.isEmpty)
            Text(
              'No recent events.',
              style: TextStyle(color: palette.textMuted),
            )
          else
            ...events.map(
              (event) => Padding(
                padding: const EdgeInsets.only(bottom: 14),
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Container(
                      width: 8,
                      height: 8,
                      margin: const EdgeInsets.only(top: 6),
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        color: _eventColor(event.eventType, palette),
                      ),
                    ),
                    const SizedBox(width: 10),
                    SizedBox(
                      width: 58,
                      child: Text(
                        _timeAgo(event.createdAt),
                        style: TextStyle(
                          color: palette.textMuted,
                          fontSize: 12.5,
                        ),
                      ),
                    ),
                    Expanded(
                      child: Text.rich(
                        TextSpan(
                          children: [
                            TextSpan(
                              text: event.message?.isNotEmpty == true
                                  ? event.message!
                                  : event.eventType,
                              style: TextStyle(
                                color: event.eventType == 'SOS_TRIGGERED'
                                    ? palette.metricEmergency
                                    : palette.textSecondary,
                                fontWeight: FontWeight.w600,
                                height: 1.4,
                              ),
                            ),
                            if ((event.zoneName ?? '').isNotEmpty)
                              TextSpan(
                                text: ' in ${event.zoneName}',
                                style: TextStyle(
                                  color: palette.textMuted,
                                  fontWeight: FontWeight.w500,
                                ),
                              ),
                          ],
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            )
        ],
      ),
    );
  }

  Color _eventColor(String type, CampusMapPalette palette) {
    switch (type) {
      case 'SOS_TRIGGERED':
      case 'ACCESS_DENIED':
        return palette.metricEmergency;
      case 'DEVICE_OFFLINE':
        return palette.metricWarning;
      default:
        return palette.metricSafe;
    }
  }

  String _timeAgo(DateTime? dateTime) {
    if (dateTime == null) return '--';
    final diff = DateTime.now().difference(dateTime);
    if (diff.inMinutes < 1) return 'now';
    if (diff.inMinutes < 60) return '${diff.inMinutes} min';
    if (diff.inHours < 24) return '${diff.inHours} hr';
    return '${diff.inDays} d';
  }
}

class _ActiveWorkersPanel extends StatelessWidget {
  final CampusMapPalette palette;
  final int activeWorkers;
  final int totalWorkers;

  const _ActiveWorkersPanel({
    required this.palette,
    required this.activeWorkers,
    required this.totalWorkers,
  });

  @override
  Widget build(BuildContext context) {
    return _SidePanel(
      palette: palette,
      title: 'ACTIVE WORKERS',
      trailing: Text(
        '$activeWorkers / $totalWorkers',
        style: TextStyle(
          color: palette.textPrimary,
          fontWeight: FontWeight.w800,
          fontSize: 15,
        ),
      ),
      child: Column(
        children: [
          SizedBox(
            width: double.infinity,
            height: 42,
            child: CustomPaint(
              painter: _LinePulsePainter(color: palette.metricSafe),
            ),
          ),
          const SizedBox(height: 12),
          TextButton(
            onPressed: () {},
            child: Text(
              'View All Workers',
              style: TextStyle(
                color: palette.accentBlue,
                fontWeight: FontWeight.w700,
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _SidePanel extends StatelessWidget {
  final CampusMapPalette palette;
  final String title;
  final Widget child;
  final Widget? trailing;

  const _SidePanel({
    required this.palette,
    required this.title,
    required this.child,
    this.trailing,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: palette.panelBackground,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: palette.panelBorder),
        boxShadow: [
          BoxShadow(
            color: palette.panelShadow.withValues(alpha: 0.10),
            blurRadius: 20,
            offset: const Offset(0, 12),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Expanded(
                child: Text(
                  title,
                  style: TextStyle(
                    color: palette.textPrimary,
                    fontSize: 17,
                    fontWeight: FontWeight.w800,
                  ),
                ),
              ),
              if (trailing != null) trailing!,
            ],
          ),
          const SizedBox(height: 16),
          child,
        ],
      ),
    );
  }
}

class _CountColumn extends StatelessWidget {
  final String label;
  final int value;
  final Color color;

  const _CountColumn({
    required this.label,
    required this.value,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: 28,
      child: Column(
        children: [
          Text(
            '$value',
            style: TextStyle(
              color: color,
              fontWeight: FontWeight.w800,
              fontSize: 13,
            ),
          ),
        ],
      ),
    );
  }
}

class _SelectedWorkerPanel extends StatelessWidget {
  final CampusMapPalette palette;
  final MapZoneViewModel zone;
  final MapWorkerMarker? worker;
  final List<MapWorkerMarker> workers;
  final ValueChanged<MapWorkerMarker> onPickWorker;

  const _SelectedWorkerPanel({
    required this.palette,
    required this.zone,
    required this.worker,
    required this.workers,
    required this.onPickWorker,
  });

  @override
  Widget build(BuildContext context) {
    final selectedWorker = worker ?? workers.firstOrNull;

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: palette.panelBackground,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: palette.panelBorder),
        boxShadow: [
          BoxShadow(
            color: palette.panelShadow.withValues(alpha: 0.10),
            blurRadius: 24,
            offset: const Offset(0, 14),
          ),
        ],
      ),
      child: Column(
        children: [
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              if (selectedWorker != null)
                WorkerMarkerChip(
                  worker: selectedWorker,
                  palette: palette,
                  size: 74,
                )
              else
                Container(
                  width: 74,
                  height: 74,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    color: palette.track,
                  ),
                  child: Icon(Icons.person_outline, color: palette.textMuted),
                ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      selectedWorker?.name ?? zone.name,
                      style: TextStyle(
                        color: palette.textPrimary,
                        fontSize: 18,
                        fontWeight: FontWeight.w800,
                      ),
                    ),
                    const SizedBox(height: 8),
                    Wrap(
                      spacing: 10,
                      runSpacing: 6,
                      children: [
                        Text(
                          selectedWorker?.role ?? zone.area,
                          style: TextStyle(
                            color: palette.textSecondary,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                        Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 10,
                            vertical: 4,
                          ),
                          decoration: BoxDecoration(
                            color: zone.statusColor.withValues(alpha: 0.12),
                            borderRadius: BorderRadius.circular(999),
                          ),
                          child: Text(
                            zone.status.toUpperCase(),
                            style: TextStyle(
                              color: zone.statusColor,
                              fontWeight: FontWeight.w700,
                              fontSize: 11,
                            ),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 10),
                    Text(
                      'Current Location',
                      style: TextStyle(
                        color: palette.textMuted,
                        fontSize: 12,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      selectedWorker?.locationLabel ?? zone.area,
                      style: TextStyle(
                        color: palette.metricSafe,
                        fontWeight: FontWeight.w700,
                        fontSize: 17,
                      ),
                    ),
                  ],
                ),
              ),
              if (zone.latestEvent != null)
                Expanded(
                  child: Padding(
                    padding: const EdgeInsets.only(left: 16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Last Event',
                          style: TextStyle(
                            color: palette.textMuted,
                            fontSize: 12,
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                        const SizedBox(height: 6),
                        Text(
                          zone.latestEvent!.eventType,
                          style: TextStyle(
                            color: palette.textSecondary,
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                        const SizedBox(height: 6),
                        Text(
                          zone.latestEvent!.message ??
                              zone.latestEvent!.macAddress,
                          style: TextStyle(
                            color: palette.textMuted,
                            height: 1.4,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
            ],
          ),
          if (workers.isNotEmpty) ...[
            const SizedBox(height: 18),
            SizedBox(
              height: 54,
              child: ListView.separated(
                scrollDirection: Axis.horizontal,
                itemCount: workers.length,
                separatorBuilder: (_, __) => const SizedBox(width: 12),
                itemBuilder: (context, index) {
                  final item = workers[index];
                  final isSelected = item.id == selectedWorker?.id;
                  return GestureDetector(
                    onTap: () => onPickWorker(item),
                    child: AnimatedContainer(
                      duration: const Duration(milliseconds: 160),
                      padding: const EdgeInsets.symmetric(
                          horizontal: 8, vertical: 6),
                      decoration: BoxDecoration(
                        color: isSelected
                            ? zone.statusColor.withValues(alpha: 0.10)
                            : palette.track,
                        borderRadius: BorderRadius.circular(16),
                        border: Border.all(
                          color: isSelected
                              ? zone.statusColor.withValues(alpha: 0.36)
                              : palette.panelBorder,
                        ),
                      ),
                      child: Row(
                        children: [
                          WorkerMarkerChip(
                            worker: item,
                            palette: palette,
                            size: 34,
                          ),
                          const SizedBox(width: 8),
                          Text(
                            item.name,
                            style: TextStyle(
                              color: palette.textSecondary,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                        ],
                      ),
                    ),
                  );
                },
              ),
            ),
          ],
        ],
      ),
    );
  }
}

class _LinePulsePainter extends CustomPainter {
  final Color color;

  const _LinePulsePainter({
    required this.color,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = color
      ..style = PaintingStyle.stroke
      ..strokeWidth = 2.2
      ..strokeCap = StrokeCap.round;

    final glowPaint = Paint()
      ..color = color.withValues(alpha: 0.24)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 5
      ..maskFilter = const MaskFilter.blur(BlurStyle.normal, 8);

    final path = Path();
    for (var i = 0; i < 28; i++) {
      final x = size.width * (i / 27);
      final seed = math.sin(i * 0.9) * 0.28 + math.cos(i * 1.4) * 0.11;
      final y = size.height * (0.52 - seed);
      if (i == 0) {
        path.moveTo(x, y);
      } else {
        path.lineTo(x, y);
      }
    }
    canvas.drawPath(path, glowPaint);
    canvas.drawPath(path, paint);
  }

  @override
  bool shouldRepaint(covariant _LinePulsePainter oldDelegate) {
    return oldDelegate.color != color;
  }
}

extension<T> on List<T> {
  T? get firstOrNull => isEmpty ? null : first;
}
