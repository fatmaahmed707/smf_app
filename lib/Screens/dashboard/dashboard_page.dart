import '../announcements/announcement_model.dart';
import '../announcements/announcement_popup.dart';
import '../announcements/announcement_page.dart';

import 'package:flutter/material.dart';
import 'package:lucide_icons/lucide_icons.dart';
import 'package:provider/provider.dart';

import '../../theme/app_theme.dart';
import '../../widgets/stats_card.dart';
import '../../widgets/announcement_card.dart';

import '../../widgets/security_update_banner.dart';
import '../../widgets/live_activity_feed.dart';
import '../../widgets/worker_status_panel.dart';
import '../../widgets/system_status_panel.dart';

import 'emergency_dashboard_page.dart';

class DashboardPage extends StatefulWidget {
  const DashboardPage({super.key});

  @override
  State<DashboardPage> createState() => _DashboardPageState();
}

class _DashboardPageState extends State<DashboardPage> {

  int _selectedIndex = 0;

  /// Announcement storage
  List<AnnouncementModel> announcements = [];

  @override
  void initState() {
    super.initState();
  }

  /// 🚨 TRIGGER ANNOUNCEMENT
  void triggerAnnouncement() {

    final newAnnouncement = AnnouncementModel(
      title: "Evacuation Required - Zone B",
      message: "Fire detected in Zone B. Evacuate immediately.",
      priority: "critical",
      sender: "Security Control",
      timestamp: DateTime.now(),
    );

    announcements.insert(0, newAnnouncement);

    /// popup alert
    Navigator.push(
      context,
      PageRouteBuilder(
        opaque: false,
        pageBuilder: (context, _, __) =>
            AnnouncementPopup(announcement: newAnnouncement),
      ),
    );

    setState(() {});
  }

  /// Bottom Navigation Pages
  Widget _buildPage() {

    switch (_selectedIndex) {

      case 0:
        return _buildDashboardHome();

      case 1:
        return const Center(child: Text("Map Page"));

      case 2:
        return AnnouncementPage(
          announcements: announcements,
        );

      case 3:
        return const Center(child: Text("Devices Page"));

      case 4:
        return const Center(child: Text("Profile Page"));

      default:
        return _buildDashboardHome();
    }
  }

  @override
  Widget build(BuildContext context) {

    final themeProvider = Provider.of<ThemeProvider>(context);

    return Scaffold(

      backgroundColor: themeProvider.isDarkMode
          ? const Color(0xFF0A1628)
          : const Color(0xFFF4F6FA),

      /// Floating Buttons
      floatingActionButton: Stack(
        children: [

          /// Announcement button
          Positioned(
            bottom: 90,
            right: 0,
            child: FloatingActionButton(
              heroTag: "announcement",
              backgroundColor: Colors.orange,
              onPressed: triggerAnnouncement,
              child: const Icon(Icons.campaign),
            ),
          ),

          /// Emergency button
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
                    builder: (context) =>
                        const EmergencyDashboardPage(),
                  ),
                );
              },
              child: const Icon(Icons.warning),
            ),
          ),
        ],
      ),

      bottomNavigationBar: _buildBottomNav(themeProvider.isDarkMode),

      body: SafeArea(
        child: _buildPage(),
      ),
    );
  }

  /// DASHBOARD HOME
  Widget _buildDashboardHome() {

    final themeProvider = Provider.of<ThemeProvider>(context);
    final size = MediaQuery.of(context).size;
    final isWeb = size.width > 800;

    return SingleChildScrollView(

      padding: EdgeInsets.all(isWeb ? 24 : 16),

      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [

          _buildHeader(themeProvider),

          const SizedBox(height: 24),

          /// Stats
          _buildStatsCards(),

          const SizedBox(height: 24),

          /// Security Banner
          const SecurityUpdateBanner(),

          const SizedBox(height: 24),

          /// Announcement Card
          const AnnouncementCard(),

          const SizedBox(height: 24),

          /// Live Activity Feed
          const LiveActivityFeed(),

          const SizedBox(height: 24),

          /// Worker Status Panel
          const WorkerStatusPanel(),

          const SizedBox(height: 24),

          /// System Status
          const SystemStatusPanel(),

          const SizedBox(height: 24),

          /// Online Users
          _buildOnlineUsersSection(themeProvider.isDarkMode),

          const SizedBox(height: 80),
        ],
      ),
    );
  }

  /// HEADER
  Widget _buildHeader(ThemeProvider themeProvider) {

    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,

      children: [

        Column(
          crossAxisAlignment: CrossAxisAlignment.start,

          children: [

            Text(
              "SMF Control Center",
              style: TextStyle(
                color: themeProvider.isDarkMode
                    ? Colors.white
                    : Colors.black87,
                fontSize: 24,
                fontWeight: FontWeight.bold,
              ),
            ),

            const SizedBox(height: 4),

            Text(
              "Real-time Security Monitoring",
              style: TextStyle(
                color: themeProvider.isDarkMode
                    ? Colors.white.withOpacity(0.6)
                    : Colors.black.withOpacity(0.6),
                fontSize: 14,
              ),
            ),
          ],
        ),

        Row(
          children: [

            _buildLiveIndicator(),

            const SizedBox(width: 12),

            /// Theme Switch
            IconButton(
              icon: Icon(
                themeProvider.isDarkMode
                    ? Icons.wb_sunny
                    : Icons.nightlight_round,
                color: themeProvider.isDarkMode
                    ? Colors.white
                    : Colors.black87,
              ),
              onPressed: () {
                themeProvider.toggleTheme();
              },
            ),

            /// Notification icon
            Stack(
              children: [

                IconButton(
                  icon: Icon(
                    Icons.notifications,
                    color: themeProvider.isDarkMode
                        ? Colors.white
                        : Colors.black87,
                  ),
                  onPressed: () {
                    setState(() {
                      _selectedIndex = 2;
                    });
                  },
                ),

                if (announcements.isNotEmpty)
                  Positioned(
                    right: 6,
                    top: 6,
                    child: Container(
                      padding: const EdgeInsets.all(4),
                      decoration: const BoxDecoration(
                        color: Colors.red,
                        shape: BoxShape.circle,
                      ),
                      child: Text(
                        announcements.length.toString(),
                        style: const TextStyle(
                          color: Colors.white,
                          fontSize: 10,
                        ),
                      ),
                    ),
                  ),
              ],
            ),
          ],
        ),
      ],
    );
  }

  /// LIVE indicator
  Widget _buildLiveIndicator() {

    return const Row(
      children: [

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

  /// Stats Cards
  Widget _buildStatsCards() {

    return const Wrap(
      spacing: 12,
      runSpacing: 12,

      children: [

        StatsCard(
          title: "Online Users",
          value: "210",
          icon: LucideIcons.users,
          color: Colors.green,
          trend: "+12 from yesterday",
          isPositive: true,
        ),

        StatsCard(
          title: "Active Alerts",
          value: "3",
          icon: LucideIcons.alertTriangle,
          color: Colors.orange,
          trend: "-2 from yesterday",
          isPositive: true,
        ),

        StatsCard(
          title: "Devices",
          value: "185",
          icon: LucideIcons.cpu,
          color: Colors.blue,
          trend: "+5 new devices",
          isPositive: true,
        ),
      ],
    );
  }

  /// ONLINE USERS
  Widget _buildOnlineUsersSection(bool isDarkMode) {

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,

      children: [

        Text(
          "Online Users",
          style: TextStyle(
            color: isDarkMode ? Colors.white : Colors.black87,
            fontSize: 18,
            fontWeight: FontWeight.bold,
          ),
        ),

        const SizedBox(height: 12),
      ],
    );
  }

  /// Bottom Navigation
  Widget _buildBottomNav(bool isDarkMode) {

    return BottomNavigationBar(

      currentIndex: _selectedIndex,

      onTap: (index) {
        setState(() {
          _selectedIndex = index;
        });
      },

      selectedItemColor: Colors.blueAccent,

      unselectedItemColor:
          isDarkMode ? Colors.white70 : Colors.black54,

      type: BottomNavigationBarType.fixed,

      items: const [

        BottomNavigationBarItem(
            icon: Icon(Icons.home), label: "Home"),

        BottomNavigationBarItem(
            icon: Icon(Icons.map), label: "Map"),

        BottomNavigationBarItem(
            icon: Icon(Icons.warning), label: "Alerts"),

        BottomNavigationBarItem(
            icon: Icon(Icons.devices), label: "Devices"),

        BottomNavigationBarItem(
            icon: Icon(Icons.person), label: "Profile"),
      ],
    );
  }
}
