// import 'package:flutter/material.dart';
// import '../../services/api_service.dart';
// import '../../models/user.dart';

// class DashboardController extends ChangeNotifier {
//   final ApiService _apiService = ApiService();
  
//   bool _isLoading = false;
//   String? _errorMessage;
  
//   // Dashboard Data
//   int _onlineUsersCount = 0;
//   int _activeAlertsCount = 0;
//   double _systemHealth = 0.0;
//   int _totalDevices = 0;
//   String _securityScore = 'N/A';
//   int _reportsToday = 0;
  
//   List<DashboardActivity> _recentActivities = [];
//   List<Announcement> _announcements = [];
//   List<User> _onlineUsers = [];

//   // Getters
//   bool get isLoading => _isLoading;
//   String? get errorMessage => _errorMessage;
//   int get onlineUsersCount => _onlineUsersCount;
//   int get activeAlertsCount => _activeAlertsCount;
//   double get systemHealth => _systemHealth;
//   int get totalDevices => _totalDevices;
//   String get securityScore => _securityScore;
//   int get reportsToday => _reportsToday;
//   List<DashboardActivity> get recentActivities => _recentActivities;
//   List<Announcement> get announcements => _announcements;
//   List<User> get onlineUsers => _onlineUsers;

//   /// Initialize dashboard - fetch all data
//   Future<void> initializeDashboard() async {
//     _isLoading = true;
//     _errorMessage = null;
//     notifyListeners();

//     try {
//       // Fetch all dashboard data
//       await Future.wait([
//         fetchDashboardStats(),
//         fetchRecentActivities(),
//         fetchAnnouncements(),
//         fetchOnlineUsers(),
//       ]);
      
//       _isLoading = false;
//       notifyListeners();
//     } catch (e) {
//       _errorMessage = 'Failed to load dashboard: ${e.toString()}';
//       _isLoading = false;
//       notifyListeners();
//     }
//   }

//   /// Fetch dashboard statistics
//   Future<void> fetchDashboardStats() async {
//     try {
//       final stats = await _apiService.getDashboardStats();
      
//       _onlineUsersCount = stats['onlineUsers'] ?? 0;
//       _activeAlertsCount = stats['activeAlerts'] ?? 0;
//       _systemHealth = (stats['systemHealth'] ?? 0).toDouble();
//       _totalDevices = stats['totalDevices'] ?? 0;
//       _securityScore = stats['securityScore'] ?? 'N/A';
//       _reportsToday = stats['reportsToday'] ?? 0;
      
//       notifyListeners();
//     } catch (e) {
//       debugPrint('Error fetching stats: $e');
//     }
//   }

//   /// Fetch recent activities
//   Future<void> fetchRecentActivities() async {
//     try {
//       _recentActivities = await _apiService.getRecentActivities();
//       notifyListeners();
//     } catch (e) {
//       debugPrint('Error fetching activities: $e');
//     }
//   }

//   /// Fetch announcements
//   Future<void> fetchAnnouncements() async {
//     try {
//       _announcements = await _apiService.getAnnouncements();
//       notifyListeners();
//     } catch (e) {
//       debugPrint('Error fetching announcements: $e');
//     }
//   }

//   /// Fetch online users
//   Future<void> fetchOnlineUsers() async {
//     try {
//       _onlineUsers = await _apiService.getOnlineUsers();
//       notifyListeners();
//     } catch (e) {
//       debugPrint('Error fetching users: $e');
//     }
//   }

//   /// Refresh dashboard data
//   Future<void> refresh() async {
//     await initializeDashboard();
//   }

//   /// Clear error
//   void clearError() {
//     _errorMessage = null;
//     notifyListeners();
//   }
// }

// // Dashboard Activity Model
// class DashboardActivity {
//   final String id;
//   final String title;
//   final String description;
//   final DateTime timestamp;
//   final ActivityType type;

//   DashboardActivity({
//     required this.id,
//     required this.title,
//     required this.description,
//     required this.timestamp,
//     required this.type,
//   });

//   factory DashboardActivity.fromJson(Map<String, dynamic> json) {
//     return DashboardActivity(
//       id: json['id'] ?? '',
//       title: json['title'] ?? '',
//       description: json['description'] ?? '',
//       timestamp: DateTime.parse(json['timestamp']),
//       type: ActivityType.values.firstWhere(
//         (e) => e.toString() == 'ActivityType.${json['type']}',
//         orElse: () => ActivityType.info,
//       ),
//     );
//   }

//   Map<String, dynamic> toJson() {
//     return {
//       'id': id,
//       'title': title,
//       'description': description,
//       'timestamp': timestamp.toIso8601String(),
//       'type': type.toString().split('.').last,
//     };
//   }
// }

// enum ActivityType {
//   userJoined,
//   alert,
//   backup,
//   systemUpdate,
//   securityEvent,
//   info,
// }

// // Announcement Model
// class Announcement {
//   final String id;
//   final String title;
//   final String message;
//   final AnnouncementPriority priority;
//   final DateTime timestamp;
//   final bool isRead;

//   Announcement({
//     required this.id,
//     required this.title,
//     required this.message,
//     required this.priority,
//     required this.timestamp,
//     this.isRead = false,
//   });

//   factory Announcement.fromJson(Map<String, dynamic> json) {
//     return Announcement(
//       id: json['id'] ?? '',
//       title: json['title'] ?? '',
//       message: json['message'] ?? '',
//       priority: AnnouncementPriority.values.firstWhere(
//         (e) => e.toString() == 'AnnouncementPriority.${json['priority']}',
//         orElse: () => AnnouncementPriority.info,
//       ),
//       timestamp: DateTime.parse(json['timestamp']),
//       isRead: json['isRead'] ?? false,
//     );
//   }

//   Map<String, dynamic> toJson() {
//     return {
//       'id': id,
//       'title': title,
//       'message': message,
//       'priority': priority.toString().split('.').last,
//       'timestamp': timestamp.toIso8601String(),
//       'isRead': isRead,
//     };
//   }
// }

// enum AnnouncementPriority {
//   critical,
//   warning,
//   info,
//   success,
// }









