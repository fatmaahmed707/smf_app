import 'package:flutter/material.dart';

import '../models/map_worker_marker.dart';
import 'campus_map_canvas.dart';

class WorkerMarkerChip extends StatelessWidget {
  final MapWorkerMarker worker;
  final CampusMapPalette palette;
  final double size;

  const WorkerMarkerChip({
    super.key,
    required this.worker,
    required this.palette,
    this.size = 36,
  });

  @override
  Widget build(BuildContext context) {
    final statusColor = switch (worker.status) {
      'emergency' => palette.metricEmergency,
      'warning' => palette.metricWarning,
      'offline' => palette.metricOffline,
      _ => palette.metricSafe,
    };

    return Container(
      width: size,
      height: size,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        color: palette.panelBackground,
        border: Border.all(color: statusColor, width: 2.4),
        boxShadow: [
          BoxShadow(
            color: statusColor.withValues(alpha: 0.28),
            blurRadius: 12,
            spreadRadius: 1,
          ),
        ],
      ),
      child: ClipOval(
        child: worker.avatarUrl != null && worker.avatarUrl!.isNotEmpty
            ? Image.network(
                worker.avatarUrl!,
                fit: BoxFit.cover,
                errorBuilder: (_, __, ___) => _InitialsAvatar(
                  name: worker.name,
                  color: statusColor,
                  textColor: palette.textPrimary,
                ),
              )
            : _InitialsAvatar(
                name: worker.name,
                color: statusColor,
                textColor: palette.textPrimary,
              ),
      ),
    );
  }
}

class _InitialsAvatar extends StatelessWidget {
  final String name;
  final Color color;
  final Color textColor;

  const _InitialsAvatar({
    required this.name,
    required this.color,
    required this.textColor,
  });

  @override
  Widget build(BuildContext context) {
    final parts = name
        .trim()
        .split(RegExp(r'\s+'))
        .where((part) => part.isNotEmpty)
        .toList();
    final initials = parts.isEmpty
        ? 'W'
        : parts
            .take(2)
            .map((part) => part.substring(0, 1))
            .join()
            .toUpperCase();

    return DecoratedBox(
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [
            color.withValues(alpha: 0.28),
            color.withValues(alpha: 0.08),
          ],
        ),
      ),
      child: Center(
        child: Text(
          initials,
          style: TextStyle(
            color: textColor,
            fontWeight: FontWeight.w800,
            fontSize: 11.5,
          ),
        ),
      ),
    );
  }
}
