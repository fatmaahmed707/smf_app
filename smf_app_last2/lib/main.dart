import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'Screens/announcements/announcements_page.dart';
import 'providers/language_provider.dart';
import 'Screens/profile/profile_page.dart';
import 'services/auth_service.dart';
import 'theme/app_theme.dart';
import 'Screens/login/login_page.dart';
import 'Screens/login/register_page.dart';
import 'Screens/dashboard/dashboard_page.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final hasSession = await AuthService.instance.restoreSession();

  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => ThemeProvider()),
        ChangeNotifierProvider(create: (_) => LanguageProvider()),
      ],
      child: SMFApp(initiallyAuthenticated: hasSession),
    ),
  );
}

class SMFApp extends StatelessWidget {
  final bool initiallyAuthenticated;

  const SMFApp({
    super.key,
    required this.initiallyAuthenticated,
  });

  @override
  Widget build(BuildContext context) {
    return Consumer<ThemeProvider>(
      builder: (context, themeProvider, child) {
        return MaterialApp(
          title: 'SMF - Security Monitoring',
          debugShowCheckedModeBanner: false,
          theme: AppTheme.lightTheme(),
          darkTheme: AppTheme.darkTheme(),
          themeMode: themeProvider.themeMode,
          initialRoute: initiallyAuthenticated ? '/dashboard' : '/login',
          routes: {
            '/login': (context) => const LoginPage(),
            '/register': (context) => const RegisterPage(),
            '/dashboard': (context) => const DashboardPage(),
            '/announcements': (context) => const AnnouncementsPage(),
            '/profile': (context) => const ProfilePage(),
          },
        );
      },
    );
  }
}
