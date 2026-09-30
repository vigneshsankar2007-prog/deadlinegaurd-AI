import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/constants/app_colors.dart';
import '../../core/constants/app_constants.dart';
import '../../core/widgets/app_card.dart';
import '../../providers/theme_provider.dart';
import '../../providers/auth_provider.dart';

/**
 * Screen 15: Application Settings & Preferences Screen
 */
class SettingsScreen extends StatefulWidget {
  const SettingsScreen({super.key});

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  bool _enable48h = true;
  bool _enable24h = true;
  bool _enable6h = true;
  bool _enableOverdue = true;
  bool _enableDailySummary = true;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final themeProvider = context.watch<ThemeProvider>();
    final auth = context.watch<AuthProvider>();

    return Scaffold(
      appBar: AppBar(
        title: const Text('Settings & Preferences'),
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // Appearance
          Text(
            'Appearance',
            style: theme.textTheme.titleMedium
                ?.copyWith(fontWeight: FontWeight.w700),
          ),
          const SizedBox(height: 8),
          AppCard(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            child: SwitchListTile(
              contentPadding: EdgeInsets.zero,
              secondary: Icon(
                themeProvider.isDarkMode
                    ? Icons.dark_mode
                    : Icons.light_mode,
                color: AppColors.primary,
              ),
              title: const Text(
                'Dark Theme Mode',
                style: TextStyle(fontWeight: FontWeight.w600),
              ),
              subtitle: const Text('Enable low-light dark theme palette'),
              value: themeProvider.isDarkMode,
              onChanged: (_) => themeProvider.toggleTheme(),
            ),
          ),
          const SizedBox(height: 24),

          // Deadline Reminder Settings
          Text(
            'Stage 9 Deadline Reminder Triggers',
            style: theme.textTheme.titleMedium
                ?.copyWith(fontWeight: FontWeight.w700),
          ),
          const SizedBox(height: 8),
          AppCard(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            child: Column(
              children: [
                SwitchListTile(
                  contentPadding: EdgeInsets.zero,
                  title: const Text('48-Hour Deadline Reminder'),
                  subtitle: const Text('Receive early warning alert 2 days before deadline'),
                  value: _enable48h,
                  onChanged: (v) => setState(() => _enable48h = v),
                ),
                const Divider(),
                SwitchListTile(
                  contentPadding: EdgeInsets.zero,
                  title: const Text('24-Hour Urgent Alert'),
                  subtitle: const Text('One-day countdown deliverable notification'),
                  value: _enable24h,
                  onChanged: (v) => setState(() => _enable24h = v),
                ),
                const Divider(),
                SwitchListTile(
                  contentPadding: EdgeInsets.zero,
                  title: const Text('6-Hour Critical Countdown'),
                  subtitle: const Text('Final submission preparation alert'),
                  value: _enable6h,
                  onChanged: (v) => setState(() => _enable6h = v),
                ),
                const Divider(),
                SwitchListTile(
                  contentPadding: EdgeInsets.zero,
                  title: const Text('Overdue Deliverable Alerts'),
                  subtitle: const Text('Immediate alert when deadline passes'),
                  value: _enableOverdue,
                  onChanged: (v) => setState(() => _enableOverdue = v),
                ),
                const Divider(),
                SwitchListTile(
                  contentPadding: EdgeInsets.zero,
                  title: const Text('Daily Morning Summary (07:30)'),
                  subtitle: const Text('Consolidated digest of pending deliverables'),
                  value: _enableDailySummary,
                  onChanged: (v) => setState(() => _enableDailySummary = v),
                ),
              ],
            ),
          ),
          const SizedBox(height: 24),

          // About App
          Text(
            'About System',
            style: theme.textTheme.titleMedium
                ?.copyWith(fontWeight: FontWeight.w700),
          ),
          const SizedBox(height: 8),
          AppCard(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Row(
                  children: [
                    Icon(Icons.shield_outlined,
                        color: AppColors.primary, size: 24),
                    SizedBox(width: 8),
                    Text(
                      AppConstants.appName,
                      style:
                          TextStyle(fontWeight: FontWeight.w700, fontSize: 16),
                    ),
                  ],
                ),
                const SizedBox(height: 6),
                const Text(
                  'Spring Boot 3.3 Backend • MySQL 8.0 • Flutter Material 3 Frontend',
                  style: TextStyle(fontSize: 12, color: Colors.grey),
                ),
                const SizedBox(height: 12),
                const Text(
                  'Deterministic 5-factor priority scoring, Gemini AI study planner, Pomodoro study sessions, and automated deadline reminders.',
                  style: TextStyle(fontSize: 12, height: 1.4),
                ),
                const Divider(height: 20),
                const Text('Version: ${AppConstants.appVersion}',
                    style: TextStyle(fontSize: 12, color: Colors.grey)),
              ],
            ),
          ),
          const SizedBox(height: 24),

          // Logout
          ListTile(
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(12),
              side: const BorderSide(color: AppColors.error, width: 1),
            ),
            leading: const Icon(Icons.logout, color: AppColors.error),
            title: const Text(
              'Sign Out',
              style:
                  TextStyle(color: AppColors.error, fontWeight: FontWeight.w700),
            ),
            onTap: () {
              auth.logout();
              Navigator.pushNamedAndRemoveUntil(
                context,
                AppConstants.routeLogin,
                (route) => false,
              );
            },
          ),
          const SizedBox(height: 32),
        ],
      ),
    );
  }
}
