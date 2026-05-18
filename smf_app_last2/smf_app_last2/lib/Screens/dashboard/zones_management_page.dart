import 'package:flutter/material.dart';

import '../../models/role_summary.dart';
import '../../models/zone_summary.dart';
import '../../services/api_service.dart';
import '../../services/roles_service.dart';
import '../../services/zones_service.dart';

class ZonesManagementPage extends StatefulWidget {
  const ZonesManagementPage({super.key});

  @override
  State<ZonesManagementPage> createState() => _ZonesManagementPageState();
}

class _ZonesManagementPageState extends State<ZonesManagementPage> {
  final ZonesService _zonesService = ZonesService();
  final RolesService _rolesService = RolesService();

  bool _isLoading = true;
  String? _errorMessage;
  List<ZoneSummary> _zones = const [];
  List<RoleSummary> _roles = const [];

  @override
  void initState() {
    super.initState();
    _loadZones();
  }

  Future<void> _loadZones() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final results = await Future.wait([
        _zonesService.getZones(),
        _rolesService.getRoles(),
      ]);
      if (!mounted) return;
      setState(() {
        _zones = results[0] as List<ZoneSummary>;
        _roles = results[1] as List<RoleSummary>;
        _isLoading = false;
      });
    } on ApiException catch (error) {
      if (!mounted) return;
      setState(() {
        _errorMessage = error.message;
        _isLoading = false;
      });
    } catch (_) {
      if (!mounted) return;
      setState(() {
        _errorMessage = 'Failed to load zones.';
        _isLoading = false;
      });
    }
  }

  Future<void> _createZone() async {
    final name = await _showZoneNameDialog(context, title: 'Create Zone');
    if (name == null) return;
    await _runMutation(() => _zonesService.createZone(name));
  }

  Future<void> _editZone(ZoneSummary zone) async {
    final name = await _showZoneNameDialog(
      context,
      title: 'Update Zone',
      initialValue: zone.name,
    );
    if (name == null) return;
    await _runMutation(() => _zonesService.updateZone(id: zone.id, name: name));
  }

  Future<void> _deleteZone(ZoneSummary zone) async {
    final confirmed = await showDialog<bool>(
          context: context,
          builder: (context) => AlertDialog(
            title: const Text('Delete zone?'),
            content: Text('This will delete ${zone.name}.'),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(context, false),
                child: const Text('Cancel'),
              ),
              FilledButton(
                onPressed: () => Navigator.pop(context, true),
                child: const Text('Delete'),
              ),
            ],
          ),
        ) ??
        false;
    if (!confirmed) return;
    await _runMutation(() => _zonesService.deleteZone(zone.id));
  }

  Future<void> _assignRole(ZoneSummary zone) async {
    final role = await _pickRole(context, roles: _roles, title: 'Assign Role');
    if (role == null) return;
    await _runMutation(
      () => _zonesService.assignRoleToZone(zoneId: zone.id, roleId: role.id),
    );
  }

  Future<void> _removeRole(ZoneSummary zone) async {
    final role = await _pickRole(context, roles: _roles, title: 'Remove Role');
    if (role == null) return;
    await _runMutation(
      () => _zonesService.removeRoleFromZone(zoneId: zone.id, roleId: role.id),
    );
  }

  Future<void> _runMutation(Future<dynamic> Function() task) async {
    try {
      await task();
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Zone saved successfully.')),
      );
      await _loadZones();
    } on ApiException catch (error) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(error.message)),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final bg = isDark ? const Color(0xFF020B1F) : const Color(0xFFF6FAFF);
    final bgEnd = isDark ? const Color(0xFF03142D) : const Color(0xFFEEF6FF);
    final card = isDark
        ? const Color.fromRGBO(5, 18, 45, 0.72)
        : const Color.fromRGBO(255, 255, 255, 0.86);
    final border = isDark
        ? const Color.fromRGBO(56, 189, 248, 0.22)
        : const Color.fromRGBO(59, 130, 246, 0.16);
    final text = isDark ? const Color(0xFFF8FAFC) : const Color(0xFF061B5B);
    final muted = isDark ? const Color(0xFF9DB2D8) : const Color(0xFF6678A5);

    return Container(
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [bg, bgEnd],
        ),
      ),
      child: LayoutBuilder(
        builder: (context, constraints) {
          final width = constraints.maxWidth;
          final columns = width >= 1100 ? 2 : 1;
          final cardHeight = width < 520
              ? 390.0
              : width < 760
                  ? 340.0
                  : 285.0;

          return GridView.builder(
            padding: EdgeInsets.all(width < 560 ? 14 : 24),
            gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
              crossAxisCount: columns,
              crossAxisSpacing: 20,
              mainAxisSpacing: 20,
              mainAxisExtent: cardHeight,
            ),
            itemCount: _isLoading || _errorMessage != null || _zones.isEmpty
                ? 2
                : _zones.length + 1,
            itemBuilder: (context, index) {
              if (index == 0) {
                return Container(
                  padding: const EdgeInsets.all(24),
                  decoration: BoxDecoration(
                    color: card,
                    borderRadius: BorderRadius.circular(26),
                    border: Border.all(color: border),
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Row(
                        children: [
                          Container(
                            width: 64,
                            height: 64,
                            decoration: BoxDecoration(
                              shape: BoxShape.circle,
                              color: const Color(0xFFFBBF24)
                                  .withValues(alpha: 0.14),
                            ),
                            child: const Icon(
                              Icons.location_city_outlined,
                              color: Color(0xFFFBBF24),
                              size: 30,
                            ),
                          ),
                          const SizedBox(width: 16),
                          Expanded(
                            child: Text(
                              'Zones',
                              style: TextStyle(
                                color: text,
                                fontSize: 30,
                                fontWeight: FontWeight.w800,
                              ),
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 12),
                      Text(
                        'Design secure areas, define operational boundaries, and plan access-control visibility.',
                        style: TextStyle(color: muted, height: 1.5),
                      ),
                      const SizedBox(height: 14),
                      Wrap(
                        spacing: 10,
                        runSpacing: 10,
                        children: [
                          FilledButton.icon(
                            onPressed: _isLoading ? null : _createZone,
                            icon: const Icon(Icons.add_rounded),
                            label: const Text('Add Zone'),
                          ),
                          OutlinedButton.icon(
                            onPressed: _isLoading ? null : _loadZones,
                            icon: const Icon(Icons.refresh_rounded),
                            label: const Text('Refresh'),
                          ),
                        ],
                      ),
                    ],
                  ),
                );
              }

              if (_isLoading) {
                return const Center(child: CircularProgressIndicator());
              }

              if (_errorMessage != null || _zones.isEmpty) {
                return _StateCard(
                  message: _errorMessage ?? 'No monitored zones available yet.',
                  text: text,
                  muted: muted,
                  onRetry: _loadZones,
                );
              }

              final zone = _zones[index - 1];
              final severity = _severityFromZone(zone);
              final accent = severity == 'High'
                  ? const Color(0xFFEF4444)
                  : severity == 'Medium'
                      ? const Color(0xFFFBBF24)
                      : const Color(0xFF38BDF8);

              return Container(
                padding: const EdgeInsets.all(20),
                decoration: BoxDecoration(
                  color: card,
                  borderRadius: BorderRadius.circular(22),
                  border: Border.all(color: border),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Row(
                      children: [
                        Container(
                          width: 46,
                          height: 46,
                          decoration: BoxDecoration(
                            shape: BoxShape.circle,
                            color: accent.withValues(alpha: 0.14),
                          ),
                          child: Icon(Icons.place_outlined, color: accent),
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: Text(
                            zone.name,
                            style: TextStyle(
                              color: text,
                              fontWeight: FontWeight.w700,
                              fontSize: 18,
                            ),
                          ),
                        ),
                      ],
                    ),
                    Text(
                      'Monitored access area',
                      style: TextStyle(color: muted, height: 1.45),
                    ),
                    Container(
                      padding: const EdgeInsets.symmetric(
                          horizontal: 12, vertical: 8),
                      decoration: BoxDecoration(
                        color: accent.withValues(alpha: 0.14),
                        borderRadius: BorderRadius.circular(999),
                      ),
                      child: Text(
                        '$severity Security',
                        style: TextStyle(
                          color: accent,
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                    ),
                    Wrap(
                      spacing: 8,
                      runSpacing: 8,
                      children: [
                        OutlinedButton.icon(
                          onPressed: () => _editZone(zone),
                          icon: const Icon(Icons.edit_rounded, size: 18),
                          label: const Text('Edit'),
                        ),
                        OutlinedButton.icon(
                          onPressed: () => _assignRole(zone),
                          icon: const Icon(Icons.group_add_outlined, size: 18),
                          label: const Text('Assign Role'),
                        ),
                        OutlinedButton.icon(
                          onPressed: () => _removeRole(zone),
                          icon:
                              const Icon(Icons.group_remove_outlined, size: 18),
                          label: const Text('Remove Role'),
                        ),
                        OutlinedButton.icon(
                          onPressed: () => _deleteZone(zone),
                          icon: const Icon(Icons.delete_outline_rounded,
                              size: 18),
                          label: const Text('Delete'),
                        ),
                      ],
                    ),
                  ],
                ),
              );
            },
          );
        },
      ),
    );
  }

  String _severityFromZone(ZoneSummary zone) {
    final status = zone.status?.toLowerCase();
    if (status == 'emergency' || status == 'high') return 'High';
    if (status == 'warning' || status == 'medium') return 'Medium';
    return 'Low';
  }
}

class _StateCard extends StatelessWidget {
  final String message;
  final Color text;
  final Color muted;
  final VoidCallback onRetry;

  const _StateCard({
    required this.message,
    required this.text,
    required this.muted,
    required this.onRetry,
  });

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(Icons.location_off_outlined, color: muted, size: 36),
          const SizedBox(height: 10),
          Text(
            message,
            textAlign: TextAlign.center,
            style: TextStyle(color: text, fontWeight: FontWeight.w700),
          ),
          const SizedBox(height: 12),
          OutlinedButton.icon(
            onPressed: onRetry,
            icon: const Icon(Icons.refresh_rounded),
            label: const Text('Retry'),
          ),
        ],
      ),
    );
  }
}

Future<String?> _showZoneNameDialog(
  BuildContext context, {
  required String title,
  String? initialValue,
}) async {
  final controller = TextEditingController(text: initialValue ?? '');
  final formKey = GlobalKey<FormState>();

  final result = await showDialog<String>(
    context: context,
    builder: (context) => AlertDialog(
      title: Text(title),
      content: Form(
        key: formKey,
        child: TextFormField(
          controller: controller,
          decoration: const InputDecoration(
            labelText: 'Zone name',
            border: OutlineInputBorder(),
          ),
          validator: (value) => value == null || value.trim().isEmpty
              ? 'Zone is required.'
              : null,
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text('Cancel'),
        ),
        FilledButton(
          onPressed: () {
            if (formKey.currentState!.validate()) {
              Navigator.pop(context, controller.text.trim());
            }
          },
          child: const Text('Save'),
        ),
      ],
    ),
  );

  controller.dispose();
  return result;
}

Future<RoleSummary?> _pickRole(
  BuildContext context, {
  required List<RoleSummary> roles,
  required String title,
}) async {
  if (roles.isEmpty) {
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('No roles available.')),
    );
    return null;
  }

  var selected = roles.first;
  return showDialog<RoleSummary>(
    context: context,
    builder: (context) => StatefulBuilder(
      builder: (context, setDialogState) => AlertDialog(
        title: Text(title),
        content: DropdownButtonFormField<RoleSummary>(
          initialValue: selected,
          decoration: const InputDecoration(
            labelText: 'Role',
            border: OutlineInputBorder(),
          ),
          items: roles
              .map(
                (role) => DropdownMenuItem<RoleSummary>(
                  value: role,
                  child: Text(role.roleName),
                ),
              )
              .toList(),
          onChanged: (role) {
            if (role != null) {
              setDialogState(() => selected = role);
            }
          },
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(context, selected),
            child: const Text('Apply'),
          ),
        ],
      ),
    ),
  );
}
