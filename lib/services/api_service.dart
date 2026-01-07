// import 'dart:convert';
// import 'package:flutter/foundation.dart';
// import '../models/user.dart';
// import '../Screens/dashboard/dashboard_controller.dart';

// class ApiService {
//   // Change this to your actual backend URL
//   static const String baseUrl = 'https://your-api.com/api';
  
//   // For development/testing - set to true to use mock data
//   static const bool useMockData = true;

//   // ==================== AUTHENTICATION ====================

//   /// Login with email and password
//   Future<User?> login(String email, String password) async {
//     if (useMockData) {
//       // Mock authentication for testing
//       await Future.delayed(const Duration(seconds: 1)); // Simulate network delay
      
//       // Demo credentials - remove in production!
//       if (email == 'admin@smf.com' && password == 'admin123') {
//         return User(
//           id: '1',
//           name: 'Admin',
//           email: email,
//           role: UserRole.admin,
//           isOnline: true,
//           lastSeen: DateTime.now(),
//         );
//       } else if (email == 'user@smf.com' && password == 'user123') {
//         return User(
//           id: '2',
//           name: 'User',
//           email: email,
//           role: UserRole.user,
//           isOnline: true,
//           lastSeen: DateTime.now(),
//         );
//       }
      
//       throw Exception('Invalid credentials');
//     }

//     // Real API call - uncomment and modify when ready
//     /*
//     try {
//       final response = await http.post(
//         Uri.parse('$baseUrl/auth/login'),
//         headers: {'Content-Type': 'application/json'},
//         body: jsonEncode({
//           'email': email,
//           'password': password,
//         }),
//       );

//       if (response.statusCode == 200) {
//         final data = jsonDecode(response.body);
//         return User.fromJson(data['user']);
//       } else {
//         throw Exception('Login failed');
//       }
//     } catch (e) {
//       throw Exception('Network error: $e');
//     }
//     */
    
//     return null;
//   }

//   /// Logout
//   Future<void> logout() async {
//     if (useMockData) {
//       await Future.delayed(const Duration(milliseconds: 500));
//       return;
//     }

//     // Real API call
//     /*
//     await http.post(Uri.parse('$baseUrl/auth/logout'));
//     */
//   }

//   // ==================== DASHBOARD ====================

//   /// Get dashboard statistics
//   Future<Map<String, dynamic>> getDashboardStats() async {
//     if (useMockData) {
//       await Future.delayed(const Duration(milliseconds: 800));
      
//       return {
//         'onlineUsers': 142,
//         'activeAlerts': 3,
//         'systemHealth': 98.5,
//         'totalDevices': 847,
//         'securityScore': 'A+',
//         'reportsToday': 24,
//       };
//     }

//     // Real API call
//     /*
//     final response = await http.get(Uri.parse('$baseUrl/dashboard/stats'));
//     if (response.statusCode == 200) {
//       return jsonDecode(response.body);
//     }
//     throw Exception('Failed to load stats');
//     */
    
//     return {};
//   }

//   /// Get recent activities
//   Future<List<DashboardActivity>> getRecentActivities() async {
//     if (useMockData) {
//       await Future.delayed(const Duration(milliseconds: 600));
      
//       return [
//         DashboardActivity(
//           id: '1',
//           title: 'New user registered',
//           description: 'John Doe joined the system',
//           timestamp: DateTime.now().subtract(const Duration(minutes: 5)),
//           type: ActivityType.userJoined,
//         ),
//         DashboardActivity(
//           id: '2',
//           title: 'Security alert triggered',
//           description: 'Unusual activity detected in Zone B',
//           timestamp: DateTime.now().subtract(const Duration(minutes: 15)),
//           type: ActivityType.alert,
//         ),
//         DashboardActivity(
//           id: '3',
//           title: 'System backup completed',
//           description: 'All data backed up successfully',
//           timestamp: DateTime.now().subtract(const Duration(hours: 1)),
//           type: ActivityType.backup,
//         ),
//         DashboardActivity(
//           id: '4',
//           title: 'System update available',
//           description: 'Version 2.1.0 is ready to install',
//           timestamp: DateTime.now().subtract(const Duration(hours: 2)),
//           type: ActivityType.systemUpdate,
//         ),
//       ];
//     }

//     // Real API call
//     /*
//     final response = await http.get(Uri.parse('$baseUrl/dashboard/activities'));
//     if (response.statusCode == 200) {
//       final List<dynamic> data = jsonDecode(response.body);
//       return data.map((json) => DashboardActivity.fromJson(json)).toList();
//     }
//     throw Exception('Failed to load activities');
//     */
    
//     return [];
//   }

//   /// Get announcements
//   Future<List<Announcement>> getAnnouncements() async {
//     if (useMockData) {
//       await Future.delayed(const Duration(milliseconds: 700));
      
//       return [
//         Announcement(
//           id: '1',
//           title: 'System Maintenance Scheduled',
//           message: 'Scheduled maintenance will occur on January 15, 2026 from 2:00 AM to 4:00 AM EST. All monitoring services will remain active.',
//           priority: AnnouncementPriority.warning,
//           timestamp: DateTime.now().subtract(const Duration(hours: 2)),
//           isRead: false,
//         ),
//         Announcement(
//           id: '2',
//           title: 'New Feature Released',
//           message: 'Real-time zone monitoring is now available in the dashboard.',
//           priority: AnnouncementPriority.info,
//           timestamp: DateTime.now().subtract(const Duration(days: 1)),
//           isRead: false,
//         ),
//         Announcement(
//           id: '3',
//           title: 'Security Update Applied',
//           message: 'Latest security patches have been successfully applied to all systems.',
//           priority: AnnouncementPriority.success,
//           timestamp: DateTime.now().subtract(const Duration(days: 2)),
//           isRead: true,
//         ),
//       ];
//     }

//     // Real API call
//     /*
//     final response = await http.get(Uri.parse('$baseUrl/announcements'));
//     if (response.statusCode == 200) {
//       final List<dynamic> data = jsonDecode(response.body);
//       return data.map((json) => Announcement.fromJson(json)).toList();
//     }
//     throw Exception('Failed to load announcements');
//     */
    
//     return [];
//   }

//   /// Get online users
//   Future<List<User>> getOnlineUsers() async {
//     if (useMockData) {
//       await Future.delayed(const Duration(milliseconds: 900));
      
//       return List.generate(142, (index) {
//         return User(
//           id: 'user_$index',
//           name: 'User ${index + 1}',
//           email: 'user${index + 1}@smf.com',
//           role: index < 10 ? UserRole.admin : UserRole.user,
//           isOnline: true,
//           lastSeen: DateTime.now().subtract(Duration(minutes: index % 60)),
//         );
//       });
//     }

//     // Real API call
//     /*
//     final response = await http.get(Uri.parse('$baseUrl/users/online'));
//     if (response.statusCode == 200) {
//       final List<dynamic> data = jsonDecode(response.body);
//       return data.map((json) => User.fromJson(json)).toList();
//     }
//     throw Exception('Failed to load users');
//     */
    
//     return [];
//   }

//   // ==================== USERS ====================

//   /// Get all users
//   Future<List<User>> getAllUsers() async {
//     if (useMockData) {
//       await Future.delayed(const Duration(milliseconds: 1000));
//       return await getOnlineUsers(); // Reuse mock data
//     }

//     // Real API call
//     /*
//     final response = await http.get(Uri.parse('$baseUrl/users'));
//     if (response.statusCode == 200) {
//       final List<dynamic> data = jsonDecode(response.body);
//       return data.map((json) => User.fromJson(json)).toList();
//     }
//     throw Exception('Failed to load users');
//     */
    
//     return [];
//   }

//   /// Get user by ID
//   Future<User?> getUserById(String userId) async {
//     if (useMockData) {
//       await Future.delayed(const Duration(milliseconds: 500));
      
//       return User(
//         id: userId,
//         name: 'User Name',
//         email: 'user@smf.com',
//         role: UserRole.user,
//         isOnline: true,
//         lastSeen: DateTime.now(),
//       );
//     }

//     // Real API call
//     /*
//     final response = await http.get(Uri.parse('$baseUrl/users/$userId'));
//     if (response.statusCode == 200) {
//       return User.fromJson(jsonDecode(response.body));
//     }
//     return null;
//     */
    
//     return null;
//   }

//   // ==================== ALERTS ====================

//   /// Get all alerts
//   Future<List<Map<String, dynamic>>> getAlerts() async {
//     if (useMockData) {
//       await Future.delayed(const Duration(milliseconds: 700));
      
//       return [
//         {
//           'id': '1',
//           'title': 'Suspicious Activity',
//           'description': 'Multiple failed login attempts detected',
//           'severity': 'high',
//           'timestamp': DateTime.now().subtract(const Duration(minutes: 10)).toIso8601String(),
//           'isResolved': false,
//         },
//         {
//           'id': '2',
//           'title': 'Device Offline',
//           'description': 'Device #423 has been offline for 30 minutes',
//           'severity': 'medium',
//           'timestamp': DateTime.now().subtract(const Duration(minutes: 30)).toIso8601String(),
//           'isResolved': false,
//         },
//         {
//           'id': '3',
//           'title': 'Low Battery Warning',
//           'description': 'Sensor battery below 20%',
//           'severity': 'low',
//           'timestamp': DateTime.now().subtract(const Duration(hours: 1)).toIso8601String(),
//           'isResolved': false,
//         },
//       ];
//     }

//     // Real API call
//     /*
//     final response = await http.get(Uri.parse('$baseUrl/alerts'));
//     if (response.statusCode == 200) {
//       return List<Map<String, dynamic>>.from(jsonDecode(response.body));
//     }
//     throw Exception('Failed to load alerts');
//     */
    
//     return [];
//   }
// }
