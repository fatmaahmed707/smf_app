import 'package:flutter/material.dart';

class OnlineUserTile extends StatelessWidget {
  final String name;
  final String zone;
  final String device;
  final double battery;

  const OnlineUserTile({
    super.key,
    required this.name,
    required this.zone,
    required this.device,
    required this.battery,
  });

  @override
  Widget build(BuildContext context) {
    final percent = (battery * 100).toInt();

    return Container(
      margin: const EdgeInsets.only(bottom: 10),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: const Color(0xFF1C2A45),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Row(
        children: [
          CircleAvatar(
            backgroundColor: Colors.green,
            child: Text(name[0], style: const TextStyle(color: Colors.white)),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(name, style: const TextStyle(color: Colors.white)),
                Text(
                  "$zone • $device",
                  style: const TextStyle(color: Colors.white70),
                ),
              ],
            ),
          ),
          Text("$percent%", style: const TextStyle(color: Colors.greenAccent)),
        ],
      ),
    );
  }
}
