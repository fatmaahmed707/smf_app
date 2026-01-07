import 'dart:async';
import 'package:flutter/material.dart';
import 'package:lucide_icons/lucide_icons.dart';

class EmergencyDashboardPage extends StatefulWidget {
  const EmergencyDashboardPage({super.key});

  @override
  State<EmergencyDashboardPage> createState() => _EmergencyDashboardPageState();
}

class _EmergencyDashboardPageState extends State<EmergencyDashboardPage>
    with TickerProviderStateMixin {
  bool showIntro = true;
  int alertLevel = 1;

  late AnimationController pulseController;
  late AnimationController sirenController;

  @override
  void initState() {
    super.initState();

    pulseController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 1),
    )..repeat(reverse: true);

    sirenController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 1),
    )..repeat();

    // Intro animation
    Future.delayed(const Duration(seconds: 2), () {
      if (mounted) {
        setState(() => showIntro = false);
      }
    });

    // Escalate alert level
    Timer.periodic(const Duration(seconds: 2), (timer) {
      if (mounted && alertLevel < 3) {
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
    return Scaffold(
      backgroundColor: Colors.red.shade900,
      body: Stack(
        children: [
          // Pulsing background
          FadeTransition(
            opacity: pulseController,
            child: Container(color: Colors.red.withOpacity(0.15)),
          ),

          // Main content
          SafeArea(
            child: Column(
              children: [
                _buildHeader(),
                _buildSirenBanner(),
                Expanded(child: _buildContent()),
              ],
            ),
          ),

          // Intro overlay
          if (showIntro) _buildIntroOverlay(),
        ],
      ),
    );
  }

  Widget _buildHeader() {
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
                      fontWeight: FontWeight.bold,
                    ),
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

  Widget _buildSirenBanner() {
    return AnimatedBuilder(
      animation: sirenController,
      builder: (_, __) {
        return Container(
          padding: const EdgeInsets.all(8),
          color: sirenController.value > 0.5 ? Colors.red : Colors.orange,
          child: const Center(
            child: Text(
              "ALL UNITS RESPOND • EMERGENCY SERVICES DISPATCHED",
              style: TextStyle(
                color: Colors.white,
                fontWeight: FontWeight.bold,
              ),
            ),
          ),
        );
      },
    );
  }

  Widget _buildContent() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        children: [
          _buildStatsGrid(),
          const SizedBox(height: 16),
          _buildPersonnelCard(),
          const SizedBox(height: 16),
          _buildContactsCard(),
          const SizedBox(height: 16),
          _buildActionsCard(),
        ],
      ),
    );
  }

  Widget _buildStatsGrid() {
    final stats = [
      ("Location", "Zone 1", LucideIcons.mapPin),
      ("Response", "2:34", LucideIcons.clock),
      ("Nearby", "5 Units", LucideIcons.users),
      ("Status", "ACTIVE", LucideIcons.radio),
    ];

    return Wrap(
      spacing: 12,
      runSpacing: 12,
      children: stats.map((s) => _buildStatCard(s.$1, s.$2, s.$3)).toList(),
    );
  }

  Widget _buildStatCard(String title, String value, IconData icon) {
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
          Text(
            value,
            style: const TextStyle(
              color: Colors.white,
              fontSize: 18,
              fontWeight: FontWeight.bold,
            ),
          ),
          Text(title, style: const TextStyle(color: Colors.white70)),
        ],
      ),
    );
  }

  Widget _buildPersonnelCard() {
    return _buildGlassCard(
      title: "Emergency Personnel",
      icon: LucideIcons.users,
      child: const Column(
        children: [
          ListTile(
            leading: CircleAvatar(
              backgroundColor: Colors.red,
              child: Text("JL", style: TextStyle(color: Colors.white)),
            ),
            title: Text("Jessica Lee", style: TextStyle(color: Colors.white)),
            subtitle: Text("Zone 1 • 125 BPM",
                style: TextStyle(color: Colors.white70)),
            trailing: Icon(Icons.navigation, color: Colors.white),
          ),
          Divider(color: Colors.white24),
          ListTile(
            leading: CircleAvatar(
              backgroundColor: Colors.orange,
              child: Text("MS", style: TextStyle(color: Colors.white)),
            ),
            title: Text("Michael Smith", style: TextStyle(color: Colors.white)),
            subtitle: Text("Zone 2 • 95 BPM",
                style: TextStyle(color: Colors.white70)),
          ),
        ],
      ),
    );
  }

  Widget _buildContactsCard() {
    return _buildGlassCard(
      title: "Emergency Contacts",
      icon: LucideIcons.phone,
      child: const Column(
        children: [
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

  Widget _buildActionsCard() {
    final actions = [
      ("Broadcast", LucideIcons.volume2),
      ("Lock", LucideIcons.shield),
      ("Backup", LucideIcons.phone),
      ("Cameras", LucideIcons.activity),
    ];

    return _buildGlassCard(
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

  Widget _buildGlassCard({
    required String title,
    required IconData icon,
    required Widget child,
  }) {
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
              Text(
                title,
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          child,
        ],
      ),
    );
  }

  Widget _buildIntroOverlay() {
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
