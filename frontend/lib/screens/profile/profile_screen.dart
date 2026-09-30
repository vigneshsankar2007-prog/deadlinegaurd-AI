import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/constants/app_colors.dart';
import '../../core/constants/app_constants.dart';
import '../../core/widgets/app_card.dart';
import '../../core/widgets/app_button.dart';
import '../../providers/auth_provider.dart';
import '../../providers/task_provider.dart';
import '../../providers/analytics_provider.dart';
import '../../providers/subject_provider.dart';

/**
 * Screen 14: Student Profile Screen
 */
class ProfileScreen extends StatelessWidget {
  const ProfileScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final auth = context.watch<AuthProvider>();
    final taskProvider = context.watch<TaskProvider>();
    final subjectProvider = context.watch<SubjectProvider>();
    final analytics = context.watch<AnalyticsProvider>();
    final data = analytics.getDashboardData(taskProvider, subjectProvider);
    final user = auth.currentUser;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Student Profile'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            // Avatar & Name Card
            Center(
              child: Column(
                children: [
                  CircleAvatar(
                    radius: 46,
                    backgroundColor: AppColors.primaryContainer,
                    child: Text(
                      user != null && user.name.isNotEmpty
                          ? user.name.split(' ').map((n) => n[0]).take(2).join()
                          : 'ST',
                      style: const TextStyle(
                        fontWeight: FontWeight.w800,
                        fontSize: 28,
                        color: AppColors.onPrimaryContainer,
                      ),
                    ),
                  ),
                  const SizedBox(height: 12),
                  Text(
                    user?.name ?? 'Student User',
                    style: theme.textTheme.titleLarge
                        ?.copyWith(fontWeight: FontWeight.w700),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    user?.email ?? 'student@university.edu',
                    style: theme.textTheme.bodyMedium,
                  ),
                ],
              ),
            ),
            const SizedBox(height: 24),

            // Academic Enrollment Card
            AppCard(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Academic Information',
                    style: theme.textTheme.titleMedium
                        ?.copyWith(fontWeight: FontWeight.w700),
                  ),
                  const Divider(height: 20),
                  _buildProfileRow('College', user?.college ?? 'Engineering'),
                  const SizedBox(height: 10),
                  _buildProfileRow(
                      'Department', user?.department ?? 'Computer Science'),
                  const SizedBox(height: 10),
                  _buildProfileRow(
                      'Semester', 'Semester ${user?.semester ?? 5}'),
                ],
              ),
            ),
            const SizedBox(height: 16),

            // Academic Productivity Overview
            AppCard(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Productivity Milestones',
                    style: theme.textTheme.titleMedium
                        ?.copyWith(fontWeight: FontWeight.w700),
                  ),
                  const Divider(height: 20),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceAround,
                    children: [
                      _buildStatColumn('Total Tasks', '${data.totalTasks}'),
                      _buildStatColumn('Completed', '${data.completedTasks}'),
                      _buildStatColumn('Focus Hours',
                          '${data.totalStudyHours.toStringAsFixed(1)}h'),
                      _buildStatColumn('Score',
                          data.currentProductivityScore.toStringAsFixed(0)),
                    ],
                  ),
                ],
              ),
            ),
            const SizedBox(height: 32),

            // Settings & Logout actions
            AppButton(
              text: 'App Settings & Preferences',
              icon: Icons.settings_outlined,
              isOutlined: true,
              onPressed: () {
                Navigator.pushNamed(context, AppConstants.routeSettings);
              },
            ),
            const SizedBox(height: 12),
            AppButton(
              text: 'Log Out',
              icon: Icons.logout,
              color: AppColors.error,
              isOutlined: true,
              onPressed: () {
                auth.logout();
                Navigator.pushNamedAndRemoveUntil(
                  context,
                  AppConstants.routeLogin,
                  (route) => false,
                );
              },
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildProfileRow(String label, String value) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(label, style: const TextStyle(color: Colors.grey, fontSize: 13)),
        Text(
          value,
          style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 13),
        ),
      ],
    );
  }

  Widget _buildStatColumn(String label, String value) {
    return Column(
      children: [
        Text(
          value,
          style: const TextStyle(
            fontWeight: FontWeight.w800,
            fontSize: 20,
            color: AppColors.primary,
          ),
        ),
        const SizedBox(height: 2),
        Text(
          label,
          style: const TextStyle(fontSize: 11, color: Colors.grey),
        ),
      ],
    );
  }
}
