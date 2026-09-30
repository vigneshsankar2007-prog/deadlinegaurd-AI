import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/constants/app_colors.dart';
import '../../core/constants/app_constants.dart';
import '../../core/widgets/app_card.dart';
import '../../core/widgets/app_button.dart';
import '../../core/widgets/priority_badge.dart';
import '../../providers/ai_provider.dart';
import '../../providers/focus_timer_provider.dart';

/**
 * Screen 9: AI Recommendations ("What should I do now?")
 */
class AIRecommendationsScreen extends StatelessWidget {
  const AIRecommendationsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final aiProvider = context.watch<AIProvider>();
    final rec = aiProvider.currentRecommendation;
    final task = rec.recommendedTask;

    return Scaffold(
      appBar: AppBar(
        title: const Text('AI Study Advisor'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            tooltip: 'Re-analyze Workload',
            onPressed: aiProvider.isGeneratingRecommendation
                ? null
                : () => aiProvider.refreshRecommendation(),
          ),
        ],
      ),
      body: aiProvider.isGeneratingRecommendation
          ? const Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  CircularProgressIndicator(),
                  SizedBox(height: 16),
                  Text('Analyzing current academic workload and deadlines...'),
                ],
              ),
            )
          : SingleChildScrollView(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Stage 7 Grounded AI Badge
                  Container(
                    padding:
                        const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                    decoration: BoxDecoration(
                      color: AppColors.primaryContainer,
                      borderRadius: BorderRadius.circular(10),
                      border: Border.all(
                          color: AppColors.primary.withOpacity(0.2)),
                    ),
                    child: const Row(
                      children: [
                        Icon(Icons.auto_awesome,
                            color: AppColors.primary, size: 20),
                        SizedBox(width: 8),
                        Expanded(
                          child: Text(
                            'Generated from your actual course deadlines & priority scores',
                            style: TextStyle(
                              color: AppColors.onPrimaryContainer,
                              fontWeight: FontWeight.w600,
                              fontSize: 12,
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 20),

                  // Main Recommendation Hero Card
                  AppCard(
                    padding: const EdgeInsets.all(20),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            Container(
                              padding: const EdgeInsets.all(8),
                              decoration: BoxDecoration(
                                color: AppColors.primary.withOpacity(0.1),
                                shape: BoxShape.circle,
                              ),
                              child: const Icon(Icons.lightbulb_outline,
                                  color: AppColors.primary, size: 24),
                            ),
                            const SizedBox(width: 12),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  const Text(
                                    'WHAT SHOULD I DO NOW?',
                                    style: TextStyle(
                                      color: AppColors.primary,
                                      fontWeight: FontWeight.w800,
                                      fontSize: 12,
                                      letterSpacing: 1.0,
                                    ),
                                  ),
                                  const SizedBox(height: 2),
                                  Text(
                                    rec.recommendationHeadline,
                                    style: const TextStyle(
                                      fontWeight: FontWeight.w700,
                                      fontSize: 17,
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          ],
                        ),
                        const Divider(height: 24),

                        // Recommended Task Details
                        Text(
                          'Target Deliverable:',
                          style: theme.textTheme.bodySmall
                              ?.copyWith(fontWeight: FontWeight.w700),
                        ),
                        const SizedBox(height: 6),
                        Container(
                          padding: const EdgeInsets.all(12),
                          decoration: BoxDecoration(
                            color: theme.scaffoldBackgroundColor,
                            borderRadius: BorderRadius.circular(10),
                          ),
                          child: Row(
                            children: [
                              Expanded(
                                child: Column(
                                  crossAxisAlignment:
                                      CrossAxisAlignment.start,
                                  children: [
                                    Text(
                                      task.title,
                                      style: const TextStyle(
                                        fontWeight: FontWeight.w700,
                                        fontSize: 14,
                                      ),
                                    ),
                                    Text(
                                      '${task.subject?.subjectCode ?? 'General'} • Due in 5 hours',
                                      style: theme.textTheme.bodySmall,
                                    ),
                                  ],
                                ),
                              ),
                              PriorityBadge(
                                priorityLevel: task.priorityLevel,
                                score: task.priorityScore,
                              ),
                            ],
                          ),
                        ),
                        const SizedBox(height: 16),

                        // Reasoning
                        Text(
                          'Why this task?',
                          style: theme.textTheme.bodySmall
                              ?.copyWith(fontWeight: FontWeight.w700),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          rec.reasoning,
                          style: theme.textTheme.bodyMedium
                              ?.copyWith(height: 1.4),
                        ),
                        const SizedBox(height: 16),

                        // Suggested Action
                        Text(
                          'Suggested Action Plan:',
                          style: theme.textTheme.bodySmall
                              ?.copyWith(fontWeight: FontWeight.w700),
                        ),
                        const SizedBox(height: 4),
                        Row(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Icon(Icons.arrow_forward_rounded,
                                size: 18, color: AppColors.primary),
                            const SizedBox(width: 8),
                            Expanded(
                              child: Text(
                                rec.suggestedAction,
                                style: const TextStyle(
                                  fontWeight: FontWeight.w600,
                                  fontSize: 14,
                                ),
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 24),

                        AppButton(
                          text:
                              'Start ${rec.recommendedFocusMinutes}-Minute Focus Session',
                          icon: Icons.timer,
                          onPressed: () {
                            context
                                .read<FocusTimerProvider>()
                                .setSelectedTask(task);
                            context
                                .read<FocusTimerProvider>()
                                .setTargetMinutes(
                                    rec.recommendedFocusMinutes);
                            Navigator.pushNamed(
                                context, AppConstants.routeFocusTimer);
                          },
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
    );
  }
}
