import 'package:flutter/material.dart';

class SettingsPage extends StatefulWidget {
  final dynamic palette;
  final VoidCallback onOpenRoles;
  final VoidCallback onOpenZones;
  final VoidCallback onOpenDevices;

  const SettingsPage({
    super.key,
    required this.palette,
    required this.onOpenRoles,
    required this.onOpenZones,
    required this.onOpenDevices,
  });

  @override
  State<SettingsPage> createState() => _SettingsPageState();
}

class _SettingsPageState extends State<SettingsPage> {
  bool liveAlerts = true;
  bool riskDigest = true;
  bool emergencyEscalation = true;
  bool accessReview = false;

  @override
  Widget build(BuildContext context) {
    final palette = widget.palette;
    final width = MediaQuery.of(context).size.width;
    final compact = width < 560;

    return SingleChildScrollView(
      padding: EdgeInsets.all(compact ? 14 : 24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
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
            child: compact
                ? Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: _settingsHeaderContent(palette, compact),
                  )
                : Row(children: _settingsHeaderContent(palette, compact)),
          ),
          const SizedBox(height: 22),
          LayoutBuilder(
            builder: (context, constraints) {
              final stacked = constraints.maxWidth < 1100;
              if (stacked) {
                return Column(
                  children: [
                    _adminWorkspace(palette),
                    const SizedBox(height: 20),
                    _preferencesPanel(palette),
                  ],
                );
              }
              return Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Expanded(flex: 6, child: _adminWorkspace(palette)),
                  const SizedBox(width: 20),
                  Expanded(flex: 5, child: _preferencesPanel(palette)),
                ],
              );
            },
          ),
        ],
      ),
    );
  }

  List<Widget> _settingsHeaderContent(dynamic palette, bool compact) {
    final textBlock = Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Settings',
          style: TextStyle(
            color: palette.textPrimary,
            fontSize: compact ? 28 : 34,
            fontWeight: FontWeight.w800,
          ),
        ),
        const SizedBox(height: 8),
        Text(
          'Configure monitoring preferences, admin controls, and secure workspace shortcuts.',
          style: TextStyle(color: palette.textMuted, fontSize: 16),
        ),
      ],
    );

    return [
      Container(
        width: compact ? 58 : 72,
        height: compact ? 58 : 72,
        decoration: BoxDecoration(
          shape: BoxShape.circle,
          gradient: RadialGradient(
            colors: [
              palette.primaryBlue2.withOpacity(0.28),
              palette.primaryBlue.withOpacity(0.08),
            ],
          ),
        ),
        child: Icon(
          Icons.tune_rounded,
          color: palette.primaryBlue2,
          size: compact ? 28 : 34,
        ),
      ),
      SizedBox(width: compact ? 0 : 18, height: compact ? 14 : 0),
      if (compact) textBlock else Expanded(child: textBlock),
    ];
  }

  Widget _adminWorkspace(dynamic palette) {
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
            'Admin Workspace',
            style: TextStyle(
              color: palette.textPrimary,
              fontSize: 22,
              fontWeight: FontWeight.w800,
            ),
          ),
          const SizedBox(height: 12),
          Text(
            'Jump into the missing management surfaces for access control and hardware operations.',
            style: TextStyle(color: palette.textMuted, height: 1.5),
          ),
          const SizedBox(height: 18),
          _settingsActionCard(
            palette: palette,
            title: 'Roles Management',
            subtitle: 'Create, review, and organize operational roles.',
            icon: Icons.admin_panel_settings_outlined,
            accent: const Color(0xFF38BDF8),
            onTap: widget.onOpenRoles,
          ),
          const SizedBox(height: 14),
          _settingsActionCard(
            palette: palette,
            title: 'Zones Management',
            subtitle:
                'Define protected areas, checkpoints, and access boundaries.',
            icon: Icons.location_city_outlined,
            accent: const Color(0xFFFBBF24),
            onTap: widget.onOpenZones,
          ),
          const SizedBox(height: 14),
          _settingsActionCard(
            palette: palette,
            title: 'SMF Device Registry',
            subtitle: 'Register trusted site hardware and device credentials.',
            icon: Icons.memory_outlined,
            accent: const Color(0xFF22C55E),
            onTap: widget.onOpenDevices,
          ),
        ],
      ),
    );
  }

  Widget _preferencesPanel(dynamic palette) {
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
            'Notification & Control Preferences',
            style: TextStyle(
              color: palette.textPrimary,
              fontSize: 22,
              fontWeight: FontWeight.w800,
            ),
          ),
          const SizedBox(height: 18),
          _toggleTile(
            palette: palette,
            title: 'Live Alert Broadcasts',
            subtitle:
                'Stream critical alerts instantly to the operations dashboard.',
            value: liveAlerts,
            onChanged: (value) => setState(() => liveAlerts = value),
          ),
          _toggleTile(
            palette: palette,
            title: 'Daily Risk Digest',
            subtitle:
                'Prepare a summary of incidents and exceptions every morning.',
            value: riskDigest,
            onChanged: (value) => setState(() => riskDigest = value),
          ),
          _toggleTile(
            palette: palette,
            title: 'Emergency Escalation',
            subtitle:
                'Notify leadership immediately for SOS and access breach events.',
            value: emergencyEscalation,
            onChanged: (value) => setState(() => emergencyEscalation = value),
          ),
          _toggleTile(
            palette: palette,
            title: 'Weekly Access Review',
            subtitle: 'Collect role and zone review summaries for compliance.',
            value: accessReview,
            onChanged: (value) => setState(() => accessReview = value),
          ),
        ],
      ),
    );
  }

  Widget _settingsActionCard({
    required dynamic palette,
    required String title,
    required String subtitle,
    required IconData icon,
    required Color accent,
    required VoidCallback onTap,
  }) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(18),
      child: Container(
        padding: const EdgeInsets.all(18),
        decoration: BoxDecoration(
          color: palette.innerCardBackground,
          borderRadius: BorderRadius.circular(18),
          border: Border.all(color: palette.innerCardBorder),
        ),
        child: Row(
          children: [
            Container(
              width: 48,
              height: 48,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: accent.withOpacity(0.14),
                boxShadow: [
                  BoxShadow(
                    color: accent.withOpacity(0.2),
                    blurRadius: 16,
                  ),
                ],
              ),
              child: Icon(icon, color: accent),
            ),
            const SizedBox(width: 14),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: TextStyle(
                      color: palette.textPrimary,
                      fontWeight: FontWeight.w700,
                      fontSize: 16,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    subtitle,
                    style: TextStyle(color: palette.textMuted),
                  ),
                ],
              ),
            ),
            Icon(Icons.chevron_right_rounded, color: palette.textMuted),
          ],
        ),
      ),
    );
  }

  Widget _toggleTile({
    required dynamic palette,
    required String title,
    required String subtitle,
    required bool value,
    required ValueChanged<bool> onChanged,
  }) {
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: palette.innerCardBackground,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: palette.innerCardBorder),
      ),
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: TextStyle(
                    color: palette.textPrimary,
                    fontWeight: FontWeight.w700,
                    fontSize: 16,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  subtitle,
                  style: TextStyle(color: palette.textMuted, height: 1.45),
                ),
              ],
            ),
          ),
          const SizedBox(width: 12),
          Switch(
            value: value,
            onChanged: onChanged,
          ),
        ],
      ),
    );
  }
}
