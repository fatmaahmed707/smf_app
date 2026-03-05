import 'package:flutter/material.dart';

class SystemStatusPanel extends StatelessWidget {
  const SystemStatusPanel({super.key});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: const Color(0xFF1B2A41),
        borderRadius: BorderRadius.circular(14),
      ),
      child: const Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            "System Status",
            style: TextStyle(
                color: Colors.white,
                fontSize: 18,
                fontWeight: FontWeight.bold),
          ),
          SizedBox(height: 12),
          Text("Server: Online", style: TextStyle(color: Colors.green)),
          Text("Database: Connected", style: TextStyle(color: Colors.green)),
          Text("ESP32 Devices: Connected", style: TextStyle(color: Colors.green)),
          Text("Network: Stable", style: TextStyle(color: Colors.green)),
        ],
      ),
    );
  }
}