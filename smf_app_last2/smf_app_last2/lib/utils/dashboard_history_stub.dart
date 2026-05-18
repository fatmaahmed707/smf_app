import 'dart:async';

class DashboardHistory {
  static String currentSlug() => '';

  static void replace(String slug) {}

  static void push(String slug) {}

  static Stream<String> get changes => const Stream<String>.empty();
}
