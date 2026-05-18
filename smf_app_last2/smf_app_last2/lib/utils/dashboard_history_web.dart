// ignore_for_file: avoid_web_libraries_in_flutter, deprecated_member_use

import 'dart:async';
import 'dart:html' as html;

class DashboardHistory {
  static String currentSlug() {
    final hash = html.window.location.hash;
    var value = hash.startsWith('#') ? hash.substring(1) : hash;
    if (value.isEmpty) return '';

    final uri = Uri.tryParse(value.startsWith('/') ? value : '/$value');
    final tab = uri?.queryParameters['tab'];
    if (tab != null && tab.trim().isNotEmpty) {
      return tab.trim();
    }

    if (value.startsWith('/')) value = value.substring(1);
    if (value.startsWith('dashboard/')) {
      value = value.substring('dashboard/'.length);
    }
    if (value.startsWith('dashboard?')) return 'dashboard';
    return value.trim();
  }

  static void replace(String slug) {
    html.window.history.replaceState(null, '', _urlFor(slug));
  }

  static void push(String slug) {
    if (currentSlug() == slug) return;
    html.window.history.pushState(null, '', _urlFor(slug));
  }

  static Stream<String> get changes =>
      html.window.onPopState.map((_) => currentSlug());

  static String _urlFor(String slug) {
    return '#/dashboard?tab=${Uri.encodeQueryComponent(slug)}';
  }
}
