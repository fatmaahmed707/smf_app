import 'dart:async';
import 'package:flutter/material.dart';
import 'package:lucide_icons/lucide_icons.dart';

class EmergencyDashboardScreen extends StatefulWidget {
  const EmergencyDashboardScreen({super.key});

  @override
  State<EmergencyDashboardScreen> createState() =>
      _EmergencyDashboardScreenState();
}

class _EmergencyDashboardScreenState extends State<EmergencyDashboardScreen>
    with TickerProviderStateMixin {
  bool showIntro = true;
  int alertLevel = 1;

  late AnimationController pulseController;
  late AnimationController sirenController;

  @override
  void initState() {
    super.initState();

    pulseController =
        AnimationController(vsync: this, duration: const Duration(seconds: 1))
          ..repeat(reverse: true);

    sirenController =
        AnimationController(vsync: this, duration: const Duration(seconds: 1))
          ..repeat();

    // Intro animation
    Future.delayed(const Duration(seconds: 2), () {
      setState(() => showIntro = false);
    });

    // Escalate alert level
    Timer.periodic(const Duration(seconds: 2), (timer) {
      if (alertLevel < 3) {
        setState(() => alertLevel++);
      } else {
        timer.cancel();
      }
    });
  }

  @override
  void dispose() {
    pulseController.dispose();
    sirenController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    return Scaffold(
      backgroundColor: isDark ? Colors.red.shade900 : Colors.red.shade50,
      body: Stack(
        children: [
          /// 🔴 Pulsing background
          FadeTransition(
            opacity: pulseController,
            child: Container(color: Colors.red.withOpacity(0.15)),
          ),

          /// 🚨 MAIN CONTENT
          SafeArea(
            child: Column(
              children: [
                _header(theme),
                _sirenBanner(),
                Expanded(child: _content(theme)),
              ],
            ),
          ),

          /// ⚠ INTRO ALERT
          if (showIntro) _introOverlay(),
        ],
      ),
    );
  }

  // ================= HEADER =================
  Widget _header(ThemeData theme) {
    return Container(
      padding: const EdgeInsets.all(16),
      color: Colors.red.shade900.withOpacity(0.9),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Row(
            children: [
              ScaleTransition(
                scale: pulseController,
                child: const Icon(Icons.warning, color: Colors.white, size: 30),
              ),
              const SizedBox(width: 12),
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    "EMERGENCY MODE ACTIVE",
                    style: TextStyle(
                        color: Colors.white,
                        fontSize: 18,
                        fontWeight: FontWeight.bold),
                  ),
                  Text(
                    "Alert Level $alertLevel • Teams Notified",
                    style: const TextStyle(color: Colors.white70),
                  ),
                ],
              ),
            ],
          ),
          IconButton(
            icon: const Icon(Icons.close, color: Colors.white),
            onPressed: () => Navigator.pop(context),
          )
        ],
      ),
    );
  }

  // ================= SIREN =================
  Widget _sirenBanner() {
    return AnimatedBuilder(
      animation: sirenController,
      builder: (_, __) {
        return Container(
          padding: const EdgeInsets.all(8),
          color: sirenController.value > 0.5 ? Colors.red : Colors.orange,
          child: const Center(
            child: Text(
              "ALL UNITS RESPOND • EMERGENCY SERVICES DISPATCHED",
              style:
                  TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
            ),
          ),
        );
      },
    );
  }

  // ================= CONTENT =================
  Widget _content(ThemeData theme) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        children: [
          _statsGrid(),
          const SizedBox(height: 16),
          _personnelCard(),
          const SizedBox(height: 16),
          _contactsCard(),
          const SizedBox(height: 16),
          _actionsCard(),
        ],
      ),
    );
  }

  // ================= STATS =================
  Widget _statsGrid() {
    final stats = [
      ("Location", "Zone 1", LucideIcons.mapPin),
      ("Response", "2:34", LucideIcons.clock),
      ("Nearby", "5 Units", LucideIcons.users),
      ("Status", "ACTIVE", LucideIcons.radio),
    ];

    return Wrap(
      spacing: 12,
      runSpacing: 12,
      children: stats.map((s) => _statCard(s.$1, s.$2, s.$3)).toList(),
    );
  }

  Widget _statCard(String title, String value, IconData icon) {
    return Container(
      width: 160,
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.1),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(icon, color: Colors.white),
          const SizedBox(height: 8),
          Text(value,
              style: const TextStyle(
                  color: Colors.white,
                  fontSize: 18,
                  fontWeight: FontWeight.bold)),
          Text(title, style: const TextStyle(color: Colors.white70)),
        ],
      ),
    );
  }

  // ================= PERSONNEL =================
  Widget _personnelCard() {
    return _glassCard(
      title: "Emergency Personnel",
      icon: LucideIcons.users,
      child: Column(
        children: const [
          ListTile(
            leading:
                CircleAvatar(backgroundColor: Colors.red, child: Text("JL")),
            title: Text("Jessica Lee", style: TextStyle(color: Colors.white)),
            subtitle: Text("Zone 1 • 125 BPM",
                style: TextStyle(color: Colors.white70)),
            trailing: Icon(Icons.navigation, color: Colors.white),
          ),
          Divider(color: Colors.white24),
          ListTile(
            leading:
                CircleAvatar(backgroundColor: Colors.orange, child: Text("MS")),
            title: Text("Michael Smith", style: TextStyle(color: Colors.white)),
            subtitle: Text("Zone 2 • 95 BPM",
                style: TextStyle(color: Colors.white70)),
          ),
        ],
      ),
    );
  }

  // ================= CONTACTS =================
  Widget _contactsCard() {
    return _glassCard(
      title: "Emergency Contacts",
      icon: LucideIcons.phone,
      child: Column(
        children: const [
          ListTile(
            leading: Icon(Icons.phone, color: Colors.white),
            title: Text("Emergency Services",
                style: TextStyle(color: Colors.white)),
            subtitle: Text("911", style: TextStyle(color: Colors.white70)),
          ),
          ListTile(
            leading: Icon(Icons.shield, color: Colors.white),
            title: Text("Security Team", style: TextStyle(color: Colors.white)),
            subtitle:
                Text("+1-555-0123", style: TextStyle(color: Colors.white70)),
          ),
        ],
      ),
    );
  }

  // ================= ACTIONS =================
  Widget _actionsCard() {
    final actions = [
      ("Broadcast", LucideIcons.volume2),
      ("Lock", LucideIcons.shield),
      ("Backup", LucideIcons.phone),
      ("Cameras", LucideIcons.activity),
    ];

    return _glassCard(
      title: "Quick Actions",
      icon: LucideIcons.zap,
      child: Wrap(
        spacing: 12,
        runSpacing: 12,
        children: actions
            .map(
              (a) => ElevatedButton.icon(
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.red,
                ),
                onPressed: () {},
                icon: Icon(a.$2),
                label: Text(a.$1),
              ),
            )
            .toList(),
      ),
    );
  }

  // ================= GLASS CARD =================
  Widget _glassCard(
      {required String title, required IconData icon, required Widget child}) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.1),
        borderRadius: BorderRadius.circular(16),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(icon, color: Colors.white),
              const SizedBox(width: 8),
              Text(title,
                  style: const TextStyle(
                      color: Colors.white,
                      fontSize: 18,
                      fontWeight: FontWeight.bold)),
            ],
          ),
          const SizedBox(height: 12),
          child,
        ],
      ),
    );
  }

  // ================= INTRO OVERLAY =================
  Widget _introOverlay() {
    return Container(
      color: Colors.red.withOpacity(0.9),
      child: Center(
        child: ScaleTransition(
          scale: pulseController,
          child: const Icon(Icons.warning, color: Colors.white, size: 120),
        ),
      ),
    );
  }
}
