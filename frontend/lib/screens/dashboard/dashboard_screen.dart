import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/constants/app_colors.dart';
import '../../core/constants/app_constants.dart';
import '../../core/widgets/app_card.dart';
import '../../core/widgets/priority_badge.dart';
import '../../core/widgets/status_chip.dart';
import '../../providers/auth_provider.dart';
import '../../providers/task_provider.dart';
import '../../providers/subject_provider.dart';
import '../../providers/analytics_provider.dart';
import '../../providers/notification_provider.dart';
import '../../providers/ai_provider.dart';

/**
 * Screen 4: Home Dashboard
 * Main student cockpit providing immediate productivity KPIs, AI recommendation,
 * urgent deadlines, and quick academic actions.
 */
class DashboardScreen extends StatelessWidget {
  const DashboardScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    final auth = context.watch<AuthProvider>();
    final taskProvider = context.watch<TaskProvider>();
    final subjectProvider = context.watch<SubjectProvider>();
    final analyticsProvider = context.watch<AnalyticsProvider>();
    final notifProvider = context.watch<NotificationProvider>();
    final aiProvider = context.watch<AIProvider>();

    final dashboardData =
        analyticsProvider.getDashboardData(taskProvider, subjectProvider);
    final user = auth.currentUser;

    // Highest priority active deliverables
    final focusTasks = taskProvider.allTasks
        .where((t) => !t.isCompleted)
        .toList()
      ..sort((a, b) => b.priorityScore.compareTo(a.priorityScore));

    return Scaffold(
      appBar: AppBar(
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Hello, ${user?.name.split(' ').first ?? 'Student'} 👋',
              style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 18),
            ),
            Text(
              user != null
                  ? '${user.department} • Sem ${user.semester}'
                  : 'College Student',
              style: TextStyle(
                fontSize: 12,
                color: isDark ? AppColors.textSecondaryDark : AppColors.textSecondaryLight,
              ),
            ),
          ],
        ),
        actions: [
          Stack(
            children: [
              IconButton(
                icon: const Icon(Icons.notifications_outlined),
                onPressed: () {
                  Navigator.pushNamed(context, AppConstants.routeNotifications);
                },
              ),
              if (notifProvider.unreadCount > 0)
                Positioned(
                  right: 8,
                  top: 8,
                  child: Container(
                    padding: const EdgeInsets.all(4),
                    decoration: const BoxDecoration(
                      color: AppColors.error,
                      shape: BoxShape.circle,
                    ),
                    constraints: const BoxConstraints(minWidth: 16, minHeight: 16),
                    child: Text(
                      '${notifProvider.unreadCount}',
                      textAlign: TextAlign.center,
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 10,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                ),
            ],
          ),
          IconButton(
            icon: const Icon(Icons.settings_outlined),
            onPressed: () {
              Navigator.pushNamed(context, AppConstants.routeSettings);
            },
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () async {
          await aiProvider.refreshRecommendation();
        },
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(16),
          physics: const AlwaysScrollableScrollPhysics(),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // 1. KPI Metrics Grid
              Row(
                children: [
                  Expanded(
                    child: _buildMetricCard(
                      context,
                      title: 'Completion',
                      value: '${dashboardData.completionPercentage.toStringAsFixed(0)}%',
                      subtitle: '${dashboardData.completedTasks}/${dashboardData.totalTasks} Tasks',
                      icon: Icons.check_circle_outline,
                      color: AppColors.success,
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: _buildMetricCard(
                      context,
                      title: 'Productivity',
                      value: dashboardData.currentProductivityScore.toStringAsFixed(1),
                      subtitle: 'Score / 100',
                      icon: Icons.speed_outlined,
                      color: AppColors.primary,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              Row(
                children: [
                  Expanded(
                    child: _buildMetricCard(
                      context,
                      title: 'Focus Study',
                      value: '${dashboardData.totalStudyHours.toStringAsFixed(1)}h',
                      subtitle: '${dashboardData.totalStudyMinutes} mins total',
                      icon: Icons.timer_outlined,
                      color: AppColors.secondary,
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: _buildMetricCard(
                      context,
                      title: 'Overdue',
                      value: '${dashboardData.overdueTasks}',
                      subtitle: '${dashboardData.overdueRate.toStringAsFixed(0)}% overdue rate',
                      icon: Icons.warning_amber_rounded,
                      color: dashboardData.overdueTasks > 0
                          ? AppColors.error
                          : AppColors.textSecondaryLight,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 20),

              // 2. AI Recommendation Banner
              _buildAIRecommendationCard(context, aiProvider),
              const SizedBox(height: 20),

              // 3. Quick Action Buttons
              Text(
                'Quick Actions',
                style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700),
              ),
              const SizedBox(height: 12),
              SingleChildScrollView(
                scrollDirection: Axis.horizontal,
                child: Row(
                  children: [
                    _buildActionButton(
                      context,
                      label: 'Add Task',
                      icon: Icons.add_task,
                      color: AppColors.primary,
                      onTap: () =>
                          Navigator.pushNamed(context, AppConstants.routeAddTask),
                    ),
                    const SizedBox(width: 10),
                    _buildActionButton(
                      context,
                      label: 'Focus Mode',
                      icon: Icons.play_arrow_rounded,
                      color: AppColors.secondary,
                      onTap: () =>
                          Navigator.pushNamed(context, AppConstants.routeFocusTimer),
                    ),
                    const SizedBox(width: 10),
                    _buildActionButton(
                      context,
                      label: 'Study Planner',
                      icon: Icons.auto_awesome,
                      color: AppColors.tertiary,
                      onTap: () =>
                          Navigator.pushNamed(context, AppConstants.routeStudyPlanner),
                    ),
                    const SizedBox(width: 10),
                    _buildActionButton(
                      context,
                      label: 'Subjects',
                      icon: Icons.menu_book_outlined,
                      color: AppColors.warning,
                      onTap: () =>
                          Navigator.pushNamed(context, AppConstants.routeSubjects),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 24),

              // 4. Today's Urgent Focus Tasks
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    'High Priority Focus',
                    style: theme.textTheme.titleMedium
                        ?.copyWith(fontWeight: FontWeight.w700),
                  ),
                  TextButton(
                    onPressed: () =>
                        Navigator.pushNamed(context, AppConstants.routeTasks),
                    child: const Text('View All'),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              if (focusTasks.isEmpty)
                const AppCard(
                  child: Center(
                    child: Padding(
                      padding: EdgeInsets.all(16),
                      child: Text('🎉 All deliverables completed! Enjoy your break.'),
                    ),
                  ),
                )
              else
                ListView.separated(
                  shrinkWrap: true,
                  physics: const NeverScrollableScrollPhysics(),
                  itemCount: focusTasks.take(3).length,
                  separatorBuilder: (_, __) => const SizedBox(height: 10),
                  itemBuilder: (context, index) {
                    final task = focusTasks[index];
                    return AppCard(
                      onTap: () {
                        Navigator.pushNamed(
                          context,
                          AppConstants.routeTaskDetails,
                          arguments: task.id,
                        );
                      },
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Expanded(
                                child: Text(
                                  task.title,
                                  style: const TextStyle(
                                    fontWeight: FontWeight.w600,
                                    fontSize: 15,
                                  ),
                                ),
                              ),
                              PriorityBadge(
                                priorityLevel: task.priorityLevel,
                                score: task.priorityScore,
                              ),
                            ],
                          ),
                          const SizedBox(height: 8),
                          Row(
                            children: [
                              if (task.subject != null) ...[
                                Container(
                                  padding: const EdgeInsets.symmetric(
                                      horizontal: 6, vertical: 2),
                                  decoration: BoxDecoration(
                                    color: task.subject!.color.withOpacity(0.15),
                                    borderRadius: BorderRadius.circular(4),
                                  ),
                                  child: Text(
                                    task.subject!.subjectCode,
                                    style: TextStyle(
                                      color: task.subject!.color,
                                      fontSize: 11,
                                      fontWeight: FontWeight.w700,
                                    ),
                                  ),
                                ),
                                const SizedBox(width: 8),
                              ],
                              StatusChip(status: task.status),
                              const Spacer(),
                              Icon(Icons.access_time,
                                  size: 14,
                                  color: isDark
                                      ? AppColors.textSecondaryDark
                                      : AppColors.textSecondaryLight),
                              const SizedBox(width: 4),
                              Text(
                                _formatDeadline(task.deadline),
                                style: TextStyle(
                                  fontSize: 12,
                                  color: task.isOverdue
                                      ? AppColors.error
                                      : (isDark
                                          ? AppColors.textSecondaryDark
                                          : AppColors.textSecondaryLight),
                                  fontWeight: task.isOverdue
                                      ? FontWeight.w700
                                      : FontWeight.normal,
                                ),
                              ),
                            ],
                          ),
                        ],
                      ),
                    );
                  },
                ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildMetricCard(
    BuildContext context, {
    required String title,
    required String value,
    required String subtitle,
    required IconData icon,
    required Color color,
  }) {
    final theme = Theme.of(context);
    return AppCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                title,
                style: theme.textTheme.bodyMedium?.copyWith(fontSize: 12),
              ),
              Container(
                padding: const EdgeInsets.all(6),
                decoration: BoxDecoration(
                  color: color.withOpacity(0.12),
                  shape: BoxShape.circle,
                ),
                child: Icon(icon, size: 16, color: color),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Text(
            value,
            style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 22),
          ),
          const SizedBox(height: 2),
          Text(
            subtitle,
            style: theme.textTheme.bodySmall?.copyWith(fontSize: 11),
          ),
        ],
      ),
    );
  }

  Widget _buildAIRecommendationCard(BuildContext context, AIProvider ai) {
    final theme = Theme.of(context);
    final rec = ai.currentRecommendation;

    return Container(
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [
            AppColors.primaryDark,
            AppColors.primary,
          ],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: AppColors.primary.withOpacity(0.25),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              const Icon(Icons.auto_awesome, color: Colors.amberAccent, size: 18),
              const SizedBox(width: 8),
              const Text(
                'WHAT SHOULD I DO NOW?',
                style: TextStyle(
                  color: Colors.white70,
                  fontSize: 11,
                  fontWeight: FontWeight.w700,
                  letterSpacing: 1.0,
                ),
              ),
              const Spacer(),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                decoration: BoxDecoration(
                  color: Colors.white.withOpacity(0.15),
                  borderRadius: BorderRadius.circular(6),
                ),
                child: Text(
                  '${rec.recommendedFocusMinutes} MIN BLOCK',
                  style: const TextStyle(
                    color: Colors.white,
                    fontSize: 10,
                    fontWeight: FontWeight.w700,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Text(
            rec.recommendationHeadline,
            style: const TextStyle(
              color: Colors.white,
              fontWeight: FontWeight.w700,
              fontSize: 16,
            ),
          ),
          const SizedBox(height: 6),
          Text(
            rec.reasoning,
            maxLines: 3,
            overflow: TextOverflow.ellipsis,
            style: const TextStyle(
              color: Colors.white70,
              fontSize: 12,
              height: 1.4,
            ),
          ),
          const SizedBox(height: 14),
          SizedBox(
            width: double.infinity,
            height: 38,
            child: ElevatedButton.icon(
              onPressed: () {
                Navigator.pushNamed(context, AppConstants.routeAIRecommendations);
              },
              icon: const Icon(Icons.play_arrow_rounded, size: 18),
              label: const Text('Start Recommended Session',
                  style: TextStyle(fontSize: 13, fontWeight: FontWeight.w700)),
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.white,
                foregroundColor: AppColors.primary,
                elevation: 0,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(10),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildActionButton(
    BuildContext context, {
    required String label,
    required IconData icon,
    required Color color,
    required VoidCallback onTap,
  }) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(12),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        decoration: BoxDecoration(
          color: color.withOpacity(0.08),
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: color.withOpacity(0.2)),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(icon, size: 18, color: color),
            const SizedBox(width: 8),
            Text(
              label,
              style: TextStyle(
                color: color,
                fontWeight: FontWeight.w600,
                fontSize: 13,
              ),
            ),
          ],
        ),
      ),
    );
  }

  String _formatDeadline(DateTime dt) {
    final diff = dt.difference(DateTime.now());
    if (diff.isNegative) {
      final hours = diff.inHours.abs();
      return hours < 24 ? '$hours hours overdue' : '${diff.inDays.abs()}d overdue';
    }
    if (diff.inHours < 24) {
      return 'Due in ${diff.inHours}h';
    }
    return 'Due in ${diff.inDays}d';
  }
}
