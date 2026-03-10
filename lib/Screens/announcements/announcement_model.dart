class AnnouncementModel {
  final String title;
  final String message;
  final String priority; // critical, high, medium, info
  final String sender;
  final DateTime timestamp;
  bool isRead;

  AnnouncementModel({
    required this.title,
    required this.message,
    required this.priority,
    required this.sender,
    required this.timestamp,
    this.isRead = false,
  });
}