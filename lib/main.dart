import 'package:flutter/material.dart';
import 'screens/dashboard_screen.dart';

/// GLOBAL THEME CONTROLLER
final ValueNotifier<ThemeMode> themeNotifier = ValueNotifier(ThemeMode.dark);

void main() {
  runApp(const SMFApp());
}

class SMFApp extends StatelessWidget {
  const SMFApp({super.key});

  @override
  Widget build(BuildContext context) {
    return ValueListenableBuilder<ThemeMode>(
      valueListenable: themeNotifier,
      builder: (context, mode, _) {
        return MaterialApp(
          debugShowCheckedModeBanner: false,
          title: 'SMF Dashboard',
          themeMode: mode,
          theme: ThemeData(
            brightness: Brightness.light,
            scaffoldBackgroundColor: const Color(0xFFF4F6FA),
          ),
          darkTheme: ThemeData(
            brightness: Brightness.dark,
            scaffoldBackgroundColor: const Color(0xFF0A1628),
          ),
          home: const DashboardScreen(),
        );
      },
    );
  }
}
