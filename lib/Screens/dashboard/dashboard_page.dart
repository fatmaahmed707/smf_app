import 'package:flutter/material.dart';
import 'package:lucide_icons/lucide_icons.dart';
import 'package:provider/provider.dart';
import '../../theme/app_theme.dart';
import '../../widgets/stats_card.dart';
import '../../widgets/announcement_card.dart';
import 'emergency_dashboard_page.dart';

class DashboardPage extends StatefulWidget {
  const DashboardPage({super.key});

  @override
  State<DashboardPage> createState() => _DashboardPageState();
}

class _DashboardPageState extends State<DashboardPage> {
  int _selectedIndex = 0;

  @override
  Widget build(BuildContext context) {
    final themeProvider = Provider.of<ThemeProvider>(context);
    final size = MediaQuery.of(context).size;
    final isWeb = size.width > 800;

    return Scaffold(
      backgroundColor: themeProvider.isDarkMode
          ? const Color(0xFF0A1628)
          : const Color(0xFFF4F6FA),

      // Floating Action Buttons
      floatingActionButton: Stack(
        children: [
          Positioned(
            bottom: 90,
            right: 0,
            child: FloatingActionButton(
              heroTag: "announcement",
              backgroundColor: Colors.orange,
              onPressed: () {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                      content: Text('Announcement feature coming soon!')),
                );
              },
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
                    builder: (context) => const EmergencyDashboardPage(),
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
        child: SingleChildScrollView(
          padding: EdgeInsets.all(isWeb ? 24.0 : 16.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _buildHeader(themeProvider),
              const SizedBox(height: 24),
              _buildStatsCards(),
              const SizedBox(height: 24),
              const AnnouncementCard(),
              const SizedBox(height: 24),
              _buildOnlineUsersSection(themeProvider.isDarkMode),
              const SizedBox(height: 80),
            ],
          ),
        ),
      ),
    );
  }

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
                color: themeProvider.isDarkMode ? Colors.white : Colors.black87,
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
            IconButton(
              icon: Icon(
                themeProvider.isDarkMode
                    ? Icons.wb_sunny
                    : Icons.nightlight_round,
                color: themeProvider.isDarkMode ? Colors.white : Colors.black87,
              ),
              onPressed: () {
                themeProvider.toggleTheme();
              },
            ),
            IconButton(
              icon: Icon(
                Icons.notifications,
                color: themeProvider.isDarkMode ? Colors.white : Colors.black87,
              ),
              onPressed: () {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('Notifications coming soon!')),
                );
              },
            ),
          ],
        ),
      ],
    );
  }

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
        _buildUserTile(
          "Jessica Lee",
          "Zone 1",
          "SMF-027",
          0.85,
          isDarkMode,
        ),
        const SizedBox(height: 10),
        _buildUserTile(
          "Michael Smith",
          "Zone 2",
          "SMF-102",
          0.92,
          isDarkMode,
        ),
        const SizedBox(height: 10),
        _buildUserTile(
          "Jeff Miller",
          "Z-Prime",
          "SMF-045",
          0.78,
          isDarkMode,
        ),
      ],
    );
  }

  Widget _buildUserTile(
    String name,
    String zone,
    String device,
    double battery,
    bool isDarkMode,
  ) {
    final percent = (battery * 100).toInt();
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: isDarkMode ? const Color(0xFF1C2A45) : Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 10,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Row(
        children: [
          CircleAvatar(
            backgroundColor: Colors.green,
            child: Text(
              name[0],
              style: const TextStyle(color: Colors.white),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  name,
                  style: TextStyle(
                    color: isDarkMode ? Colors.white : Colors.black87,
                    fontWeight: FontWeight.w600,
                  ),
                ),
                Text(
                  "$zone • $device",
                  style: TextStyle(
                    color: isDarkMode ? Colors.white70 : Colors.black54,
                    fontSize: 12,
                  ),
                ),
              ],
            ),
          ),
          Text(
            "$percent%",
            style: const TextStyle(
              color: Colors.greenAccent,
              fontWeight: FontWeight.bold,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildBottomNav(bool isDarkMode) {
    return Container(
      decoration: BoxDecoration(
        color: isDarkMode ? const Color(0xFF0A1628) : Colors.white,
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.1),
            blurRadius: 10,
            offset: const Offset(0, -2),
          ),
        ],
      ),
      child: BottomNavigationBar(
        currentIndex: _selectedIndex,
        onTap: (index) => setState(() => _selectedIndex = index),
        backgroundColor: Colors.transparent,
        selectedItemColor: Colors.blueAccent,
        unselectedItemColor: isDarkMode ? Colors.white70 : Colors.black54,
        type: BottomNavigationBarType.fixed,
        elevation: 0,
        items: const [
          BottomNavigationBarItem(icon: Icon(Icons.home), label: "Home"),
          BottomNavigationBarItem(icon: Icon(Icons.map), label: "Map"),
          BottomNavigationBarItem(icon: Icon(Icons.warning), label: "Alerts"),
          BottomNavigationBarItem(icon: Icon(Icons.devices), label: "Devices"),
          BottomNavigationBarItem(icon: Icon(Icons.person), label: "Profile"),
        ],
      ),
    );
  }
}
