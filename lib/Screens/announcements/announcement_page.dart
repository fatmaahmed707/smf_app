import 'package:flutter/material.dart';
import 'announcement_model.dart';

class AnnouncementPage extends StatelessWidget {
  final List<AnnouncementModel> announcements;

  const AnnouncementPage({super.key, required this.announcements});

  Color getPriorityColor(String priority) {
    switch (priority.toLowerCase()) {
      case "critical":
        return Colors.red;
      case "high":
        return Colors.orange;
      case "medium":
        return Colors.blue;
      default:
        return Colors.green;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0A192F),
      appBar: AppBar(
        backgroundColor: const Color(0xFF0A192F),
        title: const Text("Important Announcements"),
      ),
      body: ListView.builder(
        itemCount: announcements.length,
        itemBuilder: (context, index) {
          final announcement = announcements[index];
          final color = getPriorityColor(announcement.priority);

          return Card(
            color: const Color(0xFF1E1E2E),
            margin: const EdgeInsets.all(10),
            child: ListTile(
              leading: Icon(Icons.notifications, color: color),
              title: Text(
                announcement.title,
                style: const TextStyle(color: Colors.white),
              ),
              subtitle: Text(
                announcement.message,
                style: const TextStyle(color: Colors.white70),
              ),
              trailing: announcement.isRead
                  ? const Icon(Icons.check, color: Colors.green)
                  : const Icon(Icons.circle, color: Colors.red, size: 10),
            ),
          );
        },
      ),
    );
  }
}