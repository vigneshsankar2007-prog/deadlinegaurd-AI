import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/constants/app_colors.dart';
import '../../core/constants/app_constants.dart';
import '../../core/widgets/app_card.dart';
import '../../core/widgets/app_button.dart';
import '../../core/widgets/priority_badge.dart';
import '../../core/widgets/status_chip.dart';
import '../../providers/task_provider.dart';

/**
 * Screen 7: Task Details & Stage 6 Priority Breakdown Screen
 */
class TaskDetailsScreen extends StatelessWidget {
  final int taskId;

  const TaskDetailsScreen({super.key, required this.taskId});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final taskProvider = context.watch<TaskProvider>();
    final task = taskProvider.getTaskById(taskId);

    if (task == null) {
      return Scaffold(
        appBar: AppBar(title: const Text('Deliverable Details')),
        body: const Center(child: Text('Task not found')),
      );
    }

    final diff = task.deadline.difference(DateTime.now());
    final deadlineString = diff.isNegative
        ? 'Overdue by ${diff.inHours.abs()} hours'
        : 'Due in ${diff.inDays} days, ${diff.inHours % 24} hours';

    return Scaffold(
      appBar: AppBar(
        title: const Text('Deliverable Details'),
        actions: [
          IconButton(
            icon: Icon(
              task.isCompleted
                  ? Icons.check_circle
                  : Icons.check_circle_outline,
              color: task.isCompleted ? AppColors.success : null,
            ),
            tooltip: 'Toggle Completion',
            onPressed: () {
              taskProvider.toggleTaskCompletion(task.id);
            },
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Header card
            AppCard(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Expanded(
                        child: Text(
                          task.title,
                          style: theme.textTheme.titleLarge?.copyWith(
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                      ),
                      PriorityBadge(
                        priorityLevel: task.priorityLevel,
                        score: task.priorityScore,
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  Row(
                    children: [
                      if (task.subject != null) ...[
                        Container(
                          padding: const EdgeInsets.symmetric(
                              horizontal: 8, vertical: 4),
                          decoration: BoxDecoration(
                            color: task.subject!.color.withOpacity(0.15),
                            borderRadius: BorderRadius.circular(6),
                          ),
                          child: Text(
                            '${task.subject!.subjectCode} • ${task.subject!.subjectName}',
                            style: TextStyle(
                              color: task.subject!.color,
                              fontSize: 12,
                              fontWeight: FontWeight.w700,
                            ),
                          ),
                        ),
                        const SizedBox(width: 8),
                      ],
                      StatusChip(status: task.status),
                    ],
                  ),
                  const Divider(height: 24),
                  Row(
                    children: [
                      const Icon(Icons.alarm, size: 18, color: AppColors.primary),
                      const SizedBox(width: 8),
                      Text(
                        deadlineString,
                        style: TextStyle(
                          fontWeight: FontWeight.w600,
                          color: task.isOverdue ? AppColors.error : null,
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),

            // Description
            if (task.description != null && task.description!.isNotEmpty) ...[
              Text(
                'Description & Instructions',
                style: theme.textTheme.titleMedium
                    ?.copyWith(fontWeight: FontWeight.w700),
              ),
              const SizedBox(height: 8),
              AppCard(
                child: Text(
                  task.description!,
                  style: theme.textTheme.bodyMedium?.copyWith(height: 1.5),
                ),
              ),
              const SizedBox(height: 20),
            ],

            // Deliverable Attributes Grid
            Text(
              'Academic Attributes',
              style: theme.textTheme.titleMedium
                  ?.copyWith(fontWeight: FontWeight.w700),
            ),
            const SizedBox(height: 8),
            Row(
              children: [
                Expanded(
                  child: _buildAttributeTile(
                    title: 'Type',
                    value: task.taskType,
                    icon: Icons.category_outlined,
                  ),
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: _buildAttributeTile(
                    title: 'Effort',
                    value: '${task.estimatedHours} hrs',
                    icon: Icons.timer_outlined,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 10),
            Row(
              children: [
                Expanded(
                  child: _buildAttributeTile(
                    title: 'Difficulty',
                    value: '${task.difficulty} / 5',
                    icon: Icons.trending_up,
                  ),
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: _buildAttributeTile(
                    title: 'Course Weight',
                    value: '${task.academicWeight.toStringAsFixed(0)}%',
                    icon: Icons.assessment_outlined,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 24),

            // Stage 6 Authoritative Priority Engine Breakdown
            Row(
              children: [
                const Icon(Icons.analytics_outlined,
                    color: AppColors.primary, size: 20),
                const SizedBox(width: 8),
                Text(
                  'Stage 6 Priority Calculation Breakdown',
                  style: theme.textTheme.titleMedium
                      ?.copyWith(fontWeight: FontWeight.w700),
                ),
              ],
            ),
            const SizedBox(height: 8),
            AppCard(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      const Text(
                        'Composite Priority Score',
                        style: TextStyle(fontWeight: FontWeight.w600),
                      ),
                      Text(
                        '${task.priorityScore.toStringAsFixed(1)} / 100',
                        style: const TextStyle(
                          fontWeight: FontWeight.w800,
                          fontSize: 18,
                          color: AppColors.primary,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 6),
                  Text(
                    'Formula: 0.40×Urgency + 0.20×Difficulty + 0.20×Weight + 0.10×Effort + 0.10×Workload',
                    style: theme.textTheme.bodySmall?.copyWith(fontSize: 10),
                  ),
                  const Divider(height: 20),

                  // Subscores
                  _buildSubscoreBar('Urgency (40%)', task.urgencyScore),
                  const SizedBox(height: 10),
                  _buildSubscoreBar('Difficulty (20%)', task.difficultyScore),
                  const SizedBox(height: 10),
                  _buildSubscoreBar('Academic Weight (20%)', task.weightScore),
                  const SizedBox(height: 10),
                  _buildSubscoreBar('Estimated Effort (10%)', task.effortScore),
                  const SizedBox(height: 10),
                  _buildSubscoreBar('Workload Tension (10%)', task.workloadScore),

                  const Divider(height: 20),
                  Text(
                    'Explainable System Reasoning:',
                    style: theme.textTheme.bodySmall
                        ?.copyWith(fontWeight: FontWeight.w700),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    task.explanationText,
                    style: theme.textTheme.bodyMedium?.copyWith(
                      fontStyle: FontStyle.italic,
                      height: 1.4,
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 28),

            // Focus mode action
            AppButton(
              text: 'Start Focus Study Session',
              icon: Icons.play_arrow,
              onPressed: () {
                Navigator.pushNamed(context, AppConstants.routeFocusTimer);
              },
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildAttributeTile({
    required String title,
    required String value,
    required IconData icon,
  }) {
    return AppCard(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 12),
      child: Row(
        children: [
          Icon(icon, size: 20, color: AppColors.primary),
          const SizedBox(width: 8),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(title, style: const TextStyle(fontSize: 11)),
              Text(
                value,
                style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 13),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildSubscoreBar(String label, double score) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(label, style: const TextStyle(fontSize: 12)),
            Text('${score.toStringAsFixed(1)}%',
                style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 12)),
          ],
        ),
        const SizedBox(height: 4),
        ClipRRect(
          borderRadius: BorderRadius.circular(4),
          child: LinearProgressIndicator(
            value: (score / 100.0).clamp(0.0, 1.0),
            minHeight: 6,
            backgroundColor: const Color(0xFFE2E8F0),
            valueColor: AlwaysStoppedAnimation<Color>(
              score > 75
                  ? AppColors.priorityCritical
                  : (score > 50 ? AppColors.priorityHigh : AppColors.primary),
            ),
          ),
        ),
      ],
    );
  }
}
