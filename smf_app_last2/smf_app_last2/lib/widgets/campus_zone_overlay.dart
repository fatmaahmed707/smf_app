import 'dart:math' as math;

import 'package:flutter/material.dart';

import '../models/map_zone_view_model.dart';
import '../models/zone_layout_slot.dart';
import 'campus_map_canvas.dart';
import 'worker_marker_chip.dart';

class CampusZoneOverlay extends StatelessWidget {
  final MapZoneViewModel zone;
  final CampusMapPalette palette;
  final bool selected;
  final VoidCallback onTap;

  const CampusZoneOverlay({
    super.key,
    required this.zone,
    required this.palette,
    required this.selected,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final slot = zone.layoutSlot;
    return Positioned.fill(
      child: LayoutBuilder(
        builder: (context, constraints) {
          final width = constraints.maxWidth;
          final height = constraints.maxHeight;
          final footprint = Rect.fromLTWH(
            slot.footprint.left * width,
            slot.footprint.top * height,
            slot.footprint.width * width,
            slot.footprint.height * height,
          );
          final labelPosition = Offset(
            slot.labelAnchor.dx * width,
            slot.labelAnchor.dy * height,
          );

          return Stack(
            clipBehavior: Clip.none,
            children: [
              Positioned(
                left: footprint.left,
                top: footprint.top - footprint.height * slot.elevation * 0.9,
                width: footprint.width,
                height: footprint.height * (1 + slot.elevation * 1.2),
                child: Material(
                  color: Colors.transparent,
                  child: InkWell(
                    onTap: onTap,
                    borderRadius: BorderRadius.circular(22),
                    child: Stack(
                      clipBehavior: Clip.none,
                      children: [
                        CustomPaint(
                          size: Size(
                            footprint.width,
                            footprint.height * (1 + slot.elevation * 1.2),
                          ),
                          painter: _ZonePainter(
                            zone: zone,
                            palette: palette,
                            selected: selected,
                          ),
                        ),
                        ...zone.workers.take(12).map((worker) {
                          return Positioned(
                            left:
                                (worker.offsetDx - slot.footprint.left) * width,
                            top: (worker.offsetDy - slot.footprint.top) *
                                    height -
                                footprint.height * slot.elevation * 0.55,
                            child: WorkerMarkerChip(
                              worker: worker,
                              palette: palette,
                              size: math.max(
                                20,
                                math.min(36, footprint.width * 0.12),
                              ),
                            ),
                          );
                        }),
                      ],
                    ),
                  ),
                ),
              ),
              Positioned(
                left: slot.labelOnLeft
                    ? labelPosition.dx
                    : labelPosition.dx - 152,
                top: labelPosition.dy,
                width: 152,
                child: _ZoneLabelCard(
                  zone: zone,
                  palette: palette,
                  selected: selected,
                  alignLeft: slot.labelOnLeft,
                ),
              ),
            ],
          );
        },
      ),
    );
  }
}

class _ZoneLabelCard extends StatelessWidget {
  final MapZoneViewModel zone;
  final CampusMapPalette palette;
  final bool selected;
  final bool alignLeft;

  const _ZoneLabelCard({
    required this.zone,
    required this.palette,
    required this.selected,
    required this.alignLeft,
  });

  @override
  Widget build(BuildContext context) {
    return CustomPaint(
      painter: _ZoneConnectorPainter(
        color: zone.statusColor,
        alignLeft: alignLeft,
      ),
      child: Container(
        margin: EdgeInsets.only(
          left: alignLeft ? 0 : 12,
          right: alignLeft ? 12 : 0,
        ),
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
        decoration: BoxDecoration(
          color: palette.panelBackground.withValues(alpha: 0.96),
          borderRadius: BorderRadius.circular(18),
          border: Border.all(
            color: selected
                ? zone.statusColor
                : zone.statusColor.withValues(alpha: 0.36),
            width: selected ? 1.5 : 1,
          ),
          boxShadow: [
            BoxShadow(
              color: zone.statusColor.withValues(alpha: selected ? 0.18 : 0.08),
              blurRadius: 18,
              offset: const Offset(0, 10),
            ),
          ],
        ),
        child: Column(
          crossAxisAlignment:
              alignLeft ? CrossAxisAlignment.start : CrossAxisAlignment.end,
          children: [
            Text(
              zone.name.toUpperCase(),
              textAlign: alignLeft ? TextAlign.left : TextAlign.right,
              style: TextStyle(
                color: zone.statusColor,
                fontWeight: FontWeight.w800,
                fontSize: 11.8,
                letterSpacing: 0.3,
              ),
            ),
            const SizedBox(height: 5),
            Text(
              zone.area,
              textAlign: alignLeft ? TextAlign.left : TextAlign.right,
              style: TextStyle(
                color: palette.textPrimary,
                fontWeight: FontWeight.w600,
                fontSize: 12.2,
              ),
            ),
            const SizedBox(height: 6),
            Text(
              '${zone.workersCount} Workers',
              textAlign: alignLeft ? TextAlign.left : TextAlign.right,
              style: TextStyle(
                color: palette.textMuted,
                fontWeight: FontWeight.w600,
                fontSize: 12,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _ZoneConnectorPainter extends CustomPainter {
  final Color color;
  final bool alignLeft;

  const _ZoneConnectorPainter({
    required this.color,
    required this.alignLeft,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = color.withValues(alpha: 0.78)
      ..strokeWidth = 1.8
      ..style = PaintingStyle.stroke;
    final dotPaint = Paint()
      ..color = color
      ..style = PaintingStyle.fill;

    final start = alignLeft
        ? Offset(size.width - 6, size.height * 0.36)
        : Offset(6, size.height * 0.36);
    final bend = alignLeft
        ? Offset(size.width + 18, size.height * 0.36)
        : Offset(-18, size.height * 0.36);
    final end = alignLeft
        ? Offset(size.width + 18, size.height * 0.66)
        : Offset(-18, size.height * 0.66);
    final path = Path()
      ..moveTo(start.dx, start.dy)
      ..lineTo(bend.dx, bend.dy)
      ..lineTo(end.dx, end.dy);
    canvas.drawPath(path, paint);
    canvas.drawCircle(end, 3.2, dotPaint);
  }

  @override
  bool shouldRepaint(covariant _ZoneConnectorPainter oldDelegate) {
    return oldDelegate.color != color || oldDelegate.alignLeft != alignLeft;
  }
}

class _ZonePainter extends CustomPainter {
  final MapZoneViewModel zone;
  final CampusMapPalette palette;
  final bool selected;

  const _ZonePainter({
    required this.zone,
    required this.palette,
    required this.selected,
  });

  @override
  void paint(Canvas canvas, Size size) {
    switch (zone.layoutSlot.visualType) {
      case ZoneVisualType.court:
        _paintCourt(canvas, size);
        break;
      case ZoneVisualType.gate:
        _paintGate(canvas, size);
        break;
      case ZoneVisualType.restricted:
        _paintRestricted(canvas, size);
        break;
      case ZoneVisualType.assembly:
        _paintAssembly(canvas, size);
        break;
      case ZoneVisualType.utility:
        _paintUtility(canvas, size);
        break;
      case ZoneVisualType.generic:
        _paintGeneric(canvas, size);
        break;
      case ZoneVisualType.building:
        _paintBuilding(canvas, size);
        break;
    }
  }

  void _paintBuilding(Canvas canvas, Size size) {
    final top = size.height * 0.22;
    final roof = Path()
      ..moveTo(size.width * 0.16, top + size.height * 0.14)
      ..lineTo(size.width * 0.48, top)
      ..lineTo(size.width * 0.85, top + size.height * 0.14)
      ..lineTo(size.width * 0.54, top + size.height * 0.28)
      ..close();
    final leftWall = Path()
      ..moveTo(size.width * 0.16, top + size.height * 0.14)
      ..lineTo(size.width * 0.54, top + size.height * 0.28)
      ..lineTo(size.width * 0.54, size.height * 0.92)
      ..lineTo(size.width * 0.16, size.height * 0.75)
      ..close();
    final rightWall = Path()
      ..moveTo(size.width * 0.54, top + size.height * 0.28)
      ..lineTo(size.width * 0.85, top + size.height * 0.14)
      ..lineTo(size.width * 0.85, size.height * 0.59)
      ..lineTo(size.width * 0.54, size.height * 0.92)
      ..close();

    final glowPaint = Paint()
      ..color = zone.statusColor.withValues(alpha: selected ? 0.20 : 0.12)
      ..maskFilter = const MaskFilter.blur(BlurStyle.normal, 12);
    canvas.drawPath(roof.shift(const Offset(0, 0)), glowPaint);

    canvas.drawPath(
      roof,
      Paint()
        ..shader = LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [
            palette.buildingRoof,
            palette.buildingRoof.withValues(alpha: 0.75),
          ],
        ).createShader(Offset.zero & size),
    );
    canvas.drawPath(leftWall, Paint()..color = palette.buildingWall);
    canvas.drawPath(
      rightWall,
      Paint()..color = palette.buildingWall.withValues(alpha: 0.88),
    );

    final outline = Paint()
      ..color = zone.statusColor.withValues(alpha: 0.82)
      ..style = PaintingStyle.stroke
      ..strokeWidth = selected ? 2.4 : 1.6;
    canvas.drawPath(roof, outline);
    canvas.drawPath(leftWall, outline);
    canvas.drawPath(rightWall, outline);

    final windowPaint = Paint()
      ..color = palette.buildingAccent.withValues(alpha: 0.72)
      ..style = PaintingStyle.fill;
    for (var row = 0; row < 3; row++) {
      for (var col = 0; col < 4; col++) {
        final dx = size.width * (0.23 + col * 0.07);
        final dy = size.height * (0.42 + row * 0.12);
        canvas.drawRRect(
          RRect.fromRectAndRadius(
            Rect.fromLTWH(dx, dy, size.width * 0.034, size.height * 0.07),
            const Radius.circular(2),
          ),
          windowPaint,
        );
      }
    }
  }

  void _paintCourt(Canvas canvas, Size size) {
    final court = Path()
      ..moveTo(size.width * 0.12, size.height * 0.26)
      ..lineTo(size.width * 0.50, size.height * 0.08)
      ..lineTo(size.width * 0.90, size.height * 0.28)
      ..lineTo(size.width * 0.52, size.height * 0.48)
      ..close();

    final fill = Paint()
      ..color = zone.statusColor.withValues(alpha: 0.42)
      ..style = PaintingStyle.fill;
    canvas.drawPath(court, fill);
    canvas.drawPath(
      court,
      Paint()
        ..color = zone.statusColor
        ..style = PaintingStyle.stroke
        ..strokeWidth = selected ? 2.2 : 1.6,
    );

    final line = Paint()
      ..color = Colors.white.withValues(alpha: 0.65)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1.2;
    canvas.drawLine(
      Offset(size.width * 0.30, size.height * 0.18),
      Offset(size.width * 0.70, size.height * 0.36),
      line,
    );
    canvas.drawLine(
      Offset(size.width * 0.26, size.height * 0.34),
      Offset(size.width * 0.66, size.height * 0.14),
      line,
    );
    canvas.drawCircle(
      Offset(size.width * 0.50, size.height * 0.27),
      size.width * 0.08,
      line,
    );
  }

  void _paintGate(Canvas canvas, Size size) {
    final gate = RRect.fromRectAndRadius(
      Rect.fromLTWH(size.width * 0.14, size.height * 0.32, size.width * 0.72,
          size.height * 0.34),
      const Radius.circular(18),
    );
    canvas.drawRRect(
      gate,
      Paint()..color = palette.buildingWall.withValues(alpha: 0.92),
    );
    final road = Paint()
      ..color = palette.campusRoad.withValues(alpha: 0.86)
      ..style = PaintingStyle.fill;
    canvas.drawRect(
      Rect.fromLTWH(size.width * 0.38, size.height * 0.40, size.width * 0.24,
          size.height * 0.34),
      road,
    );
    final outline = Paint()
      ..color = zone.statusColor
      ..style = PaintingStyle.stroke
      ..strokeWidth = selected ? 2.4 : 1.6;
    canvas.drawRRect(gate, outline);
  }

  void _paintRestricted(Canvas canvas, Size size) {
    final pad = RRect.fromRectAndRadius(
      Rect.fromLTWH(
        size.width * 0.10,
        size.height * 0.18,
        size.width * 0.80,
        size.height * 0.62,
      ),
      const Radius.circular(16),
    );
    canvas.drawRRect(
      pad,
      Paint()..color = zone.statusColor.withValues(alpha: 0.16),
    );
    canvas.drawRRect(
      pad,
      Paint()
        ..color = zone.statusColor
        ..style = PaintingStyle.stroke
        ..strokeWidth = selected ? 2.2 : 1.8,
    );
    final iconPainter = TextPainter(
      text: TextSpan(
        text: '🔒',
        style: TextStyle(fontSize: size.height * 0.22),
      ),
      textDirection: TextDirection.ltr,
    )..layout();
    iconPainter.paint(
      canvas,
      Offset(size.width * 0.42, size.height * 0.34),
    );
  }

  void _paintAssembly(Canvas canvas, Size size) {
    final center = Offset(size.width * 0.50, size.height * 0.52);
    final circle = Paint()
      ..color = zone.statusColor.withValues(alpha: 0.18)
      ..style = PaintingStyle.fill;
    final stroke = Paint()
      ..color = zone.statusColor
      ..style = PaintingStyle.stroke
      ..strokeWidth = selected ? 2.4 : 1.6;
    canvas.drawCircle(center, size.width * 0.26, circle);
    canvas.drawCircle(center, size.width * 0.34, stroke);
    canvas.drawCircle(
      center,
      size.width * 0.44,
      stroke..color = zone.statusColor.withValues(alpha: 0.42),
    );
    final iconPainter = TextPainter(
      text: TextSpan(
        text: '👥',
        style: TextStyle(fontSize: size.height * 0.28),
      ),
      textDirection: TextDirection.ltr,
    )..layout();
    iconPainter.paint(
      canvas,
      Offset(size.width * 0.37, size.height * 0.36),
    );
  }

  void _paintUtility(Canvas canvas, Size size) {
    _paintGeneric(canvas, size);
    final spark = Paint()
      ..color = palette.accentBlue
      ..strokeWidth = 2.2
      ..style = PaintingStyle.stroke;
    final path = Path()
      ..moveTo(size.width * 0.42, size.height * 0.30)
      ..lineTo(size.width * 0.52, size.height * 0.45)
      ..lineTo(size.width * 0.47, size.height * 0.45)
      ..lineTo(size.width * 0.57, size.height * 0.64);
    canvas.drawPath(path, spark);
  }

  void _paintGeneric(Canvas canvas, Size size) {
    final building = RRect.fromRectAndRadius(
      Rect.fromLTWH(size.width * 0.16, size.height * 0.18, size.width * 0.68,
          size.height * 0.52),
      const Radius.circular(16),
    );
    canvas.drawRRect(
      building,
      Paint()..color = palette.buildingWall.withValues(alpha: 0.94),
    );
    canvas.drawRRect(
      building,
      Paint()
        ..color = zone.statusColor
        ..style = PaintingStyle.stroke
        ..strokeWidth = selected ? 2.2 : 1.6,
    );
  }

  @override
  bool shouldRepaint(covariant _ZonePainter oldDelegate) {
    return oldDelegate.zone != zone ||
        oldDelegate.palette != palette ||
        oldDelegate.selected != selected;
  }
}
