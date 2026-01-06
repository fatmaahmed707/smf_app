import 'package:flutter/material.dart';
import 'package:lucide_icons/lucide_icons.dart';

import '../main.dart';
import '../screens/emergency_dashboard_screen.dart';
import '../widgets/stat_card.dart';
import '../widgets/online_user_tile.dart';
import '../widgets/bottom_nav.dart';
import '../widgets/alert_card.dart';

class DashboardScreen extends StatelessWidget {
  const DashboardScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isWide = MediaQuery.of(context).size.width > 600;

    return Scaffold(
      backgroundColor: theme.scaffoldBackgroundColor,

      /// 🔴 FLOATING ACTION BUTTONS
      floatingActionButton: Stack(
        children: [
          Positioned(
            bottom: 90,
            right: 0,
            child: FloatingActionButton(
              heroTag: "announcement",
              backgroundColor: Colors.orange,
              onPressed: () {},
              child: const Icon(Icons.campaign),
            ),
          ),
          Positioned(
            bottom: 20,
            right: 0,
            child: FloatingActionButton(
              heroTag: "emergency",
              backgroundColor: Colors.red,
              onPressed: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => const EmergencyDashboardScreen(),
                  ),
                );
              },
              child: const Icon(Icons.warning),
            ),
          ),
        ],
      ),

      bottomNavigationBar: const BottomNav(),

      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              /// ================= HEADER =================
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        "SMF Control Center",
                        style: theme.textTheme.titleLarge,
                      ),
                      const SizedBox(height: 4),
                      Text(
                        "Real-time Security Monitoring",
                        style: theme.textTheme.bodyMedium,
                      ),
                    ],
                  ),
                  Row(
                    children: [
                      const _LiveIndicator(),
                      const SizedBox(width: 8),

                      /// ☀️🌙 THEME TOGGLE
                      IconButton(
                        icon: Icon(
                          themeNotifier.value == ThemeMode.dark
                              ? Icons.wb_sunny
                              : Icons.nightlight_round,
                        ),
                        onPressed: () {
                          themeNotifier.value =
                              themeNotifier.value == ThemeMode.dark
                                  ? ThemeMode.light
                                  : ThemeMode.dark;
                        },
                      ),

                      const Icon(Icons.notifications),
                    ],
                  ),
                ],
              ),

              const SizedBox(height: 24),

              /// ================= STATS =================
              Wrap(
                spacing: 12,
                runSpacing: 12,
                children: const [
                  StatCard(
                    title: "Online Users",
                    value: "210",
                    icon: LucideIcons.users,
                    color: Colors.green,
                    trend: "+12",
                  ),
                  StatCard(
                    title: "Active Alerts",
                    value: "3",
                    icon: LucideIcons.alertTriangle,
                    color: Colors.orange,
                    trend: "-2",
                  ),
                  StatCard(
                    title: "Devices",
                    value: "185",
                    icon: LucideIcons.cpu,
                    color: Colors.blue,
                    trend: "+5",
                  ),
                ],
              ),

              const SizedBox(height: 24),

              /// ================= ANNOUNCEMENTS =================
              const AlertCard(),

              const SizedBox(height: 24),

              /// ================= USERS =================
              Text(
                "Online Users",
                style: theme.textTheme.titleMedium,
              ),
              const SizedBox(height: 12),

              const OnlineUserTile(
                name: "Jessica Lee",
                zone: "Zone 1",
                device: "SMF-027",
                battery: 0.85,
              ),
              const OnlineUserTile(
                name: "Michael Smith",
                zone: "Zone 2",
                device: "SMF-102",
                battery: 0.92,
              ),
            ],
          ),
        ),
      ),
    );
  }
}

/// 🔴 LIVE STATUS INDICATOR
class _LiveIndicator extends StatelessWidget {
  const _LiveIndicator();

  @override
  Widget build(BuildContext context) {
    return Row(
      children: const [
        Icon(Icons.circle, size: 10, color: Colors.green),
        SizedBox(width: 6),
        Text(
          "Live",
          style: TextStyle(
            color: Colors.green,
            fontWeight: FontWeight.w600,
          ),
        ),
      ],
    );
  }
}
