import 'package:flutter/material.dart';

class LiveActivityFeed extends StatelessWidget {
  const LiveActivityFeed({super.key});

  @override
  Widget build(BuildContext context) {
    final activities = [
      "Worker 21 entered Zone A",
      "Device 14 connected",
      "Worker 15 heart rate normal",
      "SOS button pressed",
      "Worker 12 left Zone C"
    ];

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: const Color(0xFF1B2A41),
        borderRadius: BorderRadius.circular(14),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            "Live Activity",
            style: TextStyle(
                color: Colors.white,
                fontSize: 18,
                fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 10),
          ...activities.map(
            (activity) => Padding(
              padding: const EdgeInsets.symmetric(vertical: 6),
              child: Row(
                children: [
                  const Icon(Icons.circle, size: 8, color: Colors.green),
                  const SizedBox(width: 10),
                  Expanded(
                    child: Text(
                      activity,
                      style: const TextStyle(color: Colors.white70),
                    ),
                  ),
                ],
              ),
            ),
          )
        ],
      ),
    );
  }
}