import 'package:flutter/material.dart';

class ReportsPage extends StatelessWidget {
  final dynamic palette;

  const ReportsPage({super.key, required this.palette});

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(
      builder: (context, constraints) {
        final width = constraints.maxWidth;
        final metricColumns = width >= 1200
            ? 4
            : width >= 760
                ? 2
                : 1;
        final metricRatio = width >= 1200
            ? 1.35
            : width >= 760
                ? 1.65
                : 1.45;

        return SingleChildScrollView(
          padding: EdgeInsets.all(width < 560 ? 16 : 24),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _Header(palette: palette),
              const SizedBox(height: 22),
              GridView.count(
                crossAxisCount: metricColumns,
                shrinkWrap: true,
                physics: const NeverScrollableScrollPhysics(),
                crossAxisSpacing: 20,
                mainAxisSpacing: 20,
                childAspectRatio: metricRatio,
                children: const [
                  _MetricCard(
                    label: 'Incident Reports',
                    value: '128',
                    subtitle: 'Generated this month',
                    icon: Icons.description_outlined,
                    color: Color(0xFF38BDF8),
                  ),
                  _MetricCard(
                    label: 'Compliance Score',
                    value: '96%',
                    subtitle: 'Site safety readiness',
                    icon: Icons.verified_user_outlined,
                    color: Color(0xFF22C55E),
                  ),
                  _MetricCard(
                    label: 'Pending Exports',
                    value: '07',
                    subtitle: 'Awaiting manager review',
                    icon: Icons.outbox_outlined,
                    color: Color(0xFFFBBF24),
                  ),
                  _MetricCard(
                    label: 'Critical Findings',
                    value: '04',
                    subtitle: 'Require escalation',
                    icon: Icons.report_gmailerrorred_rounded,
                    color: Color(0xFFEF4444),
                  ),
                ],
              ),
              const SizedBox(height: 22),
              LayoutBuilder(
                builder: (context, constraints) {
                  final stacked = constraints.maxWidth < 1050;
                  if (stacked) {
                    return Column(
                      children: [
                        _ReportsList(palette: palette),
                        const SizedBox(height: 20),
                        _ExportPanel(palette: palette),
                      ],
                    );
                  }
                  return Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Expanded(flex: 7, child: _ReportsList(palette: palette)),
                      const SizedBox(width: 20),
                      Expanded(flex: 4, child: _ExportPanel(palette: palette)),
                    ],
                  );
                },
              ),
            ],
          ),
        );
      },
    );
  }
}

class _Header extends StatelessWidget {
  final dynamic palette;

  const _Header({required this.palette});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        color: palette.cardBackground,
        borderRadius: BorderRadius.circular(28),
        border: Border.all(color: palette.cardBorder),
        boxShadow: [
          BoxShadow(
            color: palette.cardShadow,
            blurRadius: 32,
            offset: const Offset(0, 12),
          ),
        ],
      ),
      child: Wrap(
        spacing: 18,
        runSpacing: 16,
        crossAxisAlignment: WrapCrossAlignment.center,
        children: [
          Container(
            width: 72,
            height: 72,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              gradient: RadialGradient(
                colors: [
                  palette.primaryBlue2.withOpacity(0.28),
                  palette.primaryBlue.withOpacity(0.08),
                ],
              ),
              boxShadow: [
                BoxShadow(
                  color: palette.primaryBlue2.withOpacity(0.22),
                  blurRadius: 20,
                ),
              ],
            ),
            child: Icon(
              Icons.assessment_rounded,
              color: palette.primaryBlue2,
              size: 34,
            ),
          ),
          ConstrainedBox(
            constraints: const BoxConstraints(minWidth: 220, maxWidth: 680),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Reports',
                  style: TextStyle(
                    color: palette.textPrimary,
                    fontSize: 34,
                    fontWeight: FontWeight.w800,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  'Operational summaries, compliance exports, and security reporting workspace.',
                  style: TextStyle(
                    color: palette.textMuted,
                    fontSize: 16,
                  ),
                ),
              ],
            ),
          ),
          DecoratedBox(
            decoration: BoxDecoration(
              color: palette.textMuted.withOpacity(0.10),
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: palette.cardBorder),
            ),
            child: ElevatedButton.icon(
              onPressed: () {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('Report generation endpoint not available.'),
                  ),
                );
              },
              icon: const Icon(Icons.info_outline_rounded),
              label: const Text('Generate Report'),
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.transparent,
                shadowColor: Colors.transparent,
                foregroundColor: palette.textMuted,
                padding: const EdgeInsets.symmetric(
                  horizontal: 18,
                  vertical: 18,
                ),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(16),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _MetricCard extends StatelessWidget {
  final String label;
  final String value;
  final String subtitle;
  final IconData icon;
  final Color color;

  const _MetricCard({
    required this.label,
    required this.value,
    required this.subtitle,
    required this.icon,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).brightness == Brightness.dark
        ? (
            bg: const Color.fromRGBO(5, 18, 45, 0.72),
            border: const Color.fromRGBO(56, 189, 248, 0.22),
            text: const Color(0xFFF8FAFC),
            muted: const Color(0xFF9DB2D8),
          )
        : (
            bg: const Color.fromRGBO(255, 255, 255, 0.86),
            border: const Color.fromRGBO(59, 130, 246, 0.16),
            text: const Color(0xFF061B5B),
            muted: const Color(0xFF6678A5),
          );

    return LayoutBuilder(
      builder: (context, constraints) {
        final compact = constraints.maxHeight < 220;
        final iconSize = compact ? 42.0 : 48.0;

        return Container(
          padding: EdgeInsets.all(compact ? 16 : 20),
          decoration: BoxDecoration(
            color: colors.bg,
            borderRadius: BorderRadius.circular(22),
            border: Border.all(color: colors.border),
            boxShadow: [
              BoxShadow(
                color: color.withValues(alpha: 0.16),
                blurRadius: 28,
                offset: const Offset(0, 12),
              ),
            ],
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Container(
                width: iconSize,
                height: iconSize,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: color.withValues(alpha: 0.14),
                  boxShadow: [
                    BoxShadow(
                      color: color.withValues(alpha: 0.24),
                      blurRadius: 18,
                    ),
                  ],
                ),
                child: Icon(icon, color: color, size: compact ? 22 : 24),
              ),
              SizedBox(height: compact ? 10 : 14),
              Text(
                label,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
                style: TextStyle(
                  color: colors.text,
                  fontWeight: FontWeight.w700,
                  fontSize: compact ? 14 : 15,
                ),
              ),
              const Spacer(),
              FittedBox(
                fit: BoxFit.scaleDown,
                alignment: Alignment.centerLeft,
                child: Text(
                  value,
                  style: TextStyle(
                    color: colors.text,
                    fontWeight: FontWeight.w800,
                    fontSize: compact ? 32 : 38,
                  ),
                ),
              ),
              const SizedBox(height: 4),
              Text(
                subtitle,
                maxLines: compact ? 1 : 2,
                overflow: TextOverflow.ellipsis,
                style: TextStyle(color: colors.muted),
              ),
            ],
          ),
        );
      },
    );
  }
}

class _ReportsList extends StatelessWidget {
  final dynamic palette;

  const _ReportsList({required this.palette});

  @override
  Widget build(BuildContext context) {
    const reports = [
      (
        'Daily Operations Summary',
        'Generated automatically at 06:00 AM',
        'Ready'
      ),
      (
        'Weekly Safety Compliance',
        'Compiled from alerts, inspections, and device logs',
        'Pending'
      ),
      (
        'Emergency Drill Audit',
        'Prepared for site leadership review',
        'Review'
      ),
      (
        'Device Health Snapshot',
        'Tracks maintenance, uptime, and firmware coverage',
        'Ready'
      ),
    ];

    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: palette.cardBackground,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: palette.cardBorder),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Recent Reports',
            style: TextStyle(
              color: palette.textPrimary,
              fontSize: 22,
              fontWeight: FontWeight.w800,
            ),
          ),
          const SizedBox(height: 16),
          ...reports.map(
            (report) => Container(
              margin: const EdgeInsets.only(bottom: 12),
              padding: const EdgeInsets.all(18),
              decoration: BoxDecoration(
                color: palette.innerCardBackground,
                borderRadius: BorderRadius.circular(18),
                border: Border.all(color: palette.innerCardBorder),
              ),
              child: LayoutBuilder(
                builder: (context, constraints) {
                  final compact = constraints.maxWidth < 620;
                  final leading = Container(
                    width: 42,
                    height: 42,
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      color: palette.primaryBlue.withOpacity(0.14),
                    ),
                    child: Icon(
                      Icons.insert_drive_file_outlined,
                      color: palette.primaryBlue2,
                    ),
                  );
                  final details = Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        report.$1,
                        style: TextStyle(
                          color: palette.textPrimary,
                          fontWeight: FontWeight.w700,
                          fontSize: 16,
                        ),
                      ),
                      const SizedBox(height: 6),
                      Text(
                        report.$2,
                        style: TextStyle(color: palette.textMuted),
                      ),
                    ],
                  );
                  final status = Container(
                    padding:
                        const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                    decoration: BoxDecoration(
                      color: palette.primaryBlue.withOpacity(0.12),
                      borderRadius: BorderRadius.circular(999),
                    ),
                    child: Text(
                      report.$3,
                      style: TextStyle(
                        color: palette.primaryBlue2,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                  );

                  if (compact) {
                    return Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            leading,
                            const SizedBox(width: 14),
                            Expanded(child: details),
                          ],
                        ),
                        const SizedBox(height: 12),
                        status,
                      ],
                    );
                  }

                  return Row(
                    children: [
                      leading,
                      const SizedBox(width: 14),
                      Expanded(child: details),
                      const SizedBox(width: 12),
                      status,
                    ],
                  );
                },
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _ExportPanel extends StatelessWidget {
  final dynamic palette;

  const _ExportPanel({required this.palette});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: palette.cardBackground,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: palette.cardBorder),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Export Center',
            style: TextStyle(
              color: palette.textPrimary,
              fontSize: 22,
              fontWeight: FontWeight.w800,
            ),
          ),
          const SizedBox(height: 12),
          Text(
            'Prepare executive-ready summaries for security leadership, compliance teams, and site operations.',
            style: TextStyle(color: palette.textMuted, height: 1.5),
          ),
          const SizedBox(height: 18),
          _ActionTile(
            palette: palette,
            title: 'Executive Snapshot',
            subtitle: 'Security posture, live risks, and priority responses.',
            icon: Icons.insights_outlined,
          ),
          const SizedBox(height: 12),
          _ActionTile(
            palette: palette,
            title: 'Compliance Bundle',
            subtitle:
                'Incident traceability, audits, and maintenance evidence.',
            icon: Icons.fact_check_outlined,
          ),
          const SizedBox(height: 12),
          _ActionTile(
            palette: palette,
            title: 'Field Operations Pack',
            subtitle:
                'Worker activity, zone movement, and alert response summaries.',
            icon: Icons.route_outlined,
          ),
        ],
      ),
    );
  }
}

class _ActionTile extends StatelessWidget {
  final dynamic palette;
  final String title;
  final String subtitle;
  final IconData icon;

  const _ActionTile({
    required this.palette,
    required this.title,
    required this.subtitle,
    required this.icon,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: palette.innerCardBackground,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: palette.innerCardBorder),
      ),
      child: Row(
        children: [
          Container(
            width: 40,
            height: 40,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              color: palette.primaryBlue.withOpacity(0.14),
            ),
            child: Icon(icon, color: palette.primaryBlue2, size: 20),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: TextStyle(
                    color: palette.textPrimary,
                    fontWeight: FontWeight.w700,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  subtitle,
                  style: TextStyle(color: palette.textMuted, fontSize: 13),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
