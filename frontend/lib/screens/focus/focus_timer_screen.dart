import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/constants/app_colors.dart';
import '../../core/widgets/app_card.dart';
import '../../core/widgets/app_button.dart';
import '../../models/task_model.dart';
import '../../providers/focus_timer_provider.dart';
import '../../providers/task_provider.dart';

/**
 * Screen 11: Focus Mode / Study Timer Screen (Stage 8)
 */
class FocusTimerScreen extends StatelessWidget {
  const FocusTimerScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    final timerProvider = context.watch<FocusTimerProvider>();
    final taskProvider = context.watch<TaskProvider>();
    final activeTasks = taskProvider.allTasks.where((t) => !t.isCompleted).toList();

    return Scaffold(
      appBar: AppBar(
        title: const Text('Focus Mode Timer'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20),
        child: Column(
          children: [
            // Deliverable Selector
            AppCard(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              child: DropdownButtonHideUnderline(
                child: DropdownButton<TaskModel?>(
                  isExpanded: true,
                  value: timerProvider.selectedTask,
                  hint: const Text('Select a deliverable to focus on...'),
                  items: [
                    const DropdownMenuItem<TaskModel?>(
                      value: null,
                      child: Text('General Focus Session (No Task)'),
                    ),
                    ...activeTasks.map((t) => DropdownMenuItem<TaskModel?>(
                          value: t,
                          child: Text(
                            '${t.subject?.subjectCode ?? 'General'} • ${t.title}',
                            overflow: TextOverflow.ellipsis,
                            style: const TextStyle(fontWeight: FontWeight.w600),
                          ),
                        )),
                  ],
                  onChanged: timerProvider.isIdle
                      ? (task) => timerProvider.setSelectedTask(task)
                      : null,
                ),
              ),
            ),
            const SizedBox(height: 32),

            // Large Circular Timer Widget
            Center(
              child: Stack(
                alignment: Alignment.center,
                children: [
                  SizedBox(
                    width: 250,
                    height: 250,
                    child: CircularProgressIndicator(
                      value: timerProvider.progressPercentage == 0
                          ? 0.05
                          : timerProvider.progressPercentage,
                      strokeWidth: 10,
                      backgroundColor: isDark
                          ? const Color(0xFF1E293B)
                          : const Color(0xFFE2E8F0),
                      valueColor: AlwaysStoppedAnimation<Color>(
                        timerProvider.isRunning
                            ? AppColors.primary
                            : (timerProvider.isPaused
                                ? AppColors.warning
                                : AppColors.primaryLight),
                      ),
                    ),
                  ),
                  Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text(
                        timerProvider.formattedTime,
                        style: const TextStyle(
                          fontSize: 54,
                          fontWeight: FontWeight.w800,
                          letterSpacing: 2,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        timerProvider.isRunning
                            ? 'STAY FOCUSED'
                            : (timerProvider.isPaused
                                ? 'SESSION PAUSED'
                                : 'TARGET: ${timerProvider.targetMinutes} MINS'),
                        style: TextStyle(
                          fontSize: 12,
                          fontWeight: FontWeight.w700,
                          letterSpacing: 1.2,
                          color: timerProvider.isRunning
                              ? AppColors.primary
                              : (timerProvider.isPaused
                                  ? AppColors.warning
                                  : theme.textTheme.bodySmall?.color),
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
            const SizedBox(height: 32),

            // Quick Target Minutes Presets
            if (timerProvider.isIdle) ...[
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [15, 25, 45, 60].map((mins) {
                  final isSelected = timerProvider.targetMinutes == mins;
                  return Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 4),
                    child: ChoiceChip(
                      label: Text('${mins}m'),
                      selected: isSelected,
                      onSelected: (_) => timerProvider.setTargetMinutes(mins),
                      selectedColor: AppColors.primaryContainer,
                      labelStyle: TextStyle(
                        fontWeight:
                            isSelected ? FontWeight.w700 : FontWeight.w500,
                        color: isSelected ? AppColors.onPrimaryContainer : null,
                      ),
                    ),
                  );
                }).toList(),
              ),
              const SizedBox(height: 24),
            ],

            // Timer Controls
            if (timerProvider.isIdle)
              AppButton(
                text: 'Start Focus Session',
                icon: Icons.play_arrow,
                onPressed: () => timerProvider.startTimer(),
              )
            else
              Row(
                children: [
                  Expanded(
                    child: timerProvider.isRunning
                        ? AppButton(
                            text: 'Pause',
                            icon: Icons.pause,
                            color: AppColors.warning,
                            onPressed: () => timerProvider.pauseTimer(),
                          )
                        : AppButton(
                            text: 'Resume',
                            icon: Icons.play_arrow,
                            color: AppColors.primary,
                            onPressed: () => timerProvider.resumeTimer(),
                          ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: AppButton(
                      text: 'Complete & Stop',
                      icon: Icons.stop,
                      color: AppColors.error,
                      isOutlined: true,
                      onPressed: () => timerProvider.stopTimer(),
                    ),
                  ),
                ],
              ),
            const SizedBox(height: 36),

            // Completed Sessions History
            Align(
              alignment: Alignment.centerLeft,
              child: Text(
                'Recent Completed Sessions',
                style: theme.textTheme.titleMedium
                    ?.copyWith(fontWeight: FontWeight.w700),
              ),
            ),
            const SizedBox(height: 12),
            if (timerProvider.completedSessions.isEmpty)
              const Center(
                child: Padding(
                  padding: EdgeInsets.all(16),
                  child: Text('No focus sessions completed yet today.'),
                ),
              )
            else
              ListView.separated(
                shrinkWrap: true,
                physics: const NeverScrollableScrollPhysics(),
                itemCount: timerProvider.completedSessions.take(4).length,
                separatorBuilder: (_, __) => const SizedBox(height: 8),
                itemBuilder: (context, index) {
                  final session = timerProvider.completedSessions[index];
                  return AppCard(
                    padding:
                        const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
                    child: Row(
                      children: [
                        const Icon(Icons.check_circle,
                            color: AppColors.success, size: 20),
                        const SizedBox(width: 10),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                session.taskTitle ?? 'General Focus Session',
                                style: const TextStyle(
                                    fontWeight: FontWeight.w600, fontSize: 13),
                              ),
                              Text(
                                '${session.durationMinutes} minutes focus',
                                style: theme.textTheme.bodySmall,
                              ),
                            ],
                          ),
                        ),
                        Text(
                          '${session.durationMinutes}m',
                          style: const TextStyle(
                              fontWeight: FontWeight.w800, fontSize: 14),
                        ),
                      ],
                    ),
                  );
                },
              ),
          ],
        ),
      ),
    );
  }
}
