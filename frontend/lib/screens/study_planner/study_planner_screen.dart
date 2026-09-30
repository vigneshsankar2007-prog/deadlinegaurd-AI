import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/constants/app_colors.dart';
import '../../core/widgets/app_card.dart';
import '../../core/widgets/app_button.dart';
import '../../providers/ai_provider.dart';

/**
 * Screen 10: AI Study Planner Screen (Stage 8)
 */
class StudyPlannerScreen extends StatefulWidget {
  const StudyPlannerScreen({super.key});

  @override
  State<StudyPlannerScreen> createState() => _StudyPlannerScreenState();
}

class _StudyPlannerScreenState extends State<StudyPlannerScreen> {
  double _availableHours = 3.5;
  String _startTime = '14:00';
  int _breakDuration = 10;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final aiProvider = context.watch<AIProvider>();
    final plan = aiProvider.currentPlan;

    return Scaffold(
      appBar: AppBar(
        title: const Text('AI Study Schedule Planner'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Controls Card
            AppCard(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Plan Your Study Session',
                    style: theme.textTheme.titleMedium
                        ?.copyWith(fontWeight: FontWeight.w700),
                  ),
                  const SizedBox(height: 16),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      const Text('Available Study Time:'),
                      Text(
                        '${_availableHours.toStringAsFixed(1)} Hours',
                        style: const TextStyle(
                            fontWeight: FontWeight.w700, color: AppColors.primary),
                      ),
                    ],
                  ),
                  Slider(
                    value: _availableHours,
                    min: 1.0,
                    max: 8.0,
                    divisions: 14,
                    label: '${_availableHours.toStringAsFixed(1)}h',
                    onChanged: (val) => setState(() => _availableHours = val),
                  ),
                  const SizedBox(height: 12),
                  Row(
                    children: [
                      Expanded(
                        child: DropdownButtonFormField<String>(
                          value: _startTime,
                          decoration: const InputDecoration(
                            labelText: 'Start Time',
                            prefixIcon: Icon(Icons.schedule),
                          ),
                          items: ['09:00', '12:00', '14:00', '16:00', '18:00', '20:00']
                              .map((t) =>
                                  DropdownMenuItem(value: t, child: Text(t)))
                              .toList(),
                          onChanged: (val) {
                            if (val != null) setState(() => _startTime = val);
                          },
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: DropdownButtonFormField<int>(
                          value: _breakDuration,
                          decoration: const InputDecoration(
                            labelText: 'Break Duration',
                            prefixIcon: Icon(Icons.coffee_outlined),
                          ),
                          items: [5, 10, 15, 20]
                              .map((m) => DropdownMenuItem(
                                  value: m, child: Text('$m mins')))
                              .toList(),
                          onChanged: (val) {
                            if (val != null) setState(() => _breakDuration = val);
                          },
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  AppButton(
                    text: 'Generate AI Study Schedule',
                    icon: Icons.auto_awesome,
                    isLoading: aiProvider.isGeneratingPlan,
                    onPressed: () {
                      aiProvider.generateStudyPlan(
                        availableHours: _availableHours,
                        startTime: _startTime,
                        breakDurationMinutes: _breakDuration,
                      );
                    },
                  ),
                ],
              ),
            ),
            const SizedBox(height: 24),

            // Summary Header
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  'Optimized Schedule Blocks',
                  style: theme.textTheme.titleMedium
                      ?.copyWith(fontWeight: FontWeight.w700),
                ),
                Text(
                  '${plan.totalStudyMinutes}m study • ${plan.totalBreakMinutes}m breaks',
                  style: theme.textTheme.bodySmall?.copyWith(
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),

            // Timeline Blocks
            ListView.separated(
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              itemCount: plan.blocks.length,
              separatorBuilder: (_, __) => const SizedBox(height: 10),
              itemBuilder: (context, index) {
                final block = plan.blocks[index];
                final isBreak = block.isBreak;

                return Container(
                  decoration: BoxDecoration(
                    color: isBreak
                        ? Colors.amber.withOpacity(0.08)
                        : theme.cardTheme.color,
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(
                      color: isBreak
                          ? Colors.amber.withOpacity(0.4)
                          : const Color(0xFFE2E8F0),
                    ),
                  ),
                  padding: const EdgeInsets.all(14),
                  child: Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Container(
                        padding: const EdgeInsets.symmetric(
                            horizontal: 8, vertical: 4),
                        decoration: BoxDecoration(
                          color: isBreak
                              ? Colors.amber.withOpacity(0.2)
                              : AppColors.primaryContainer,
                          borderRadius: BorderRadius.circular(6),
                        ),
                        child: Text(
                          '${block.startTime} - ${block.endTime}',
                          style: TextStyle(
                            fontSize: 11,
                            fontWeight: FontWeight.w700,
                            color: isBreak
                                ? Colors.amber[900]
                                : AppColors.onPrimaryContainer,
                          ),
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              children: [
                                Icon(
                                  isBreak
                                      ? Icons.coffee_outlined
                                      : Icons.laptop_chromebook,
                                  size: 16,
                                  color: isBreak
                                      ? Colors.amber[800]
                                      : AppColors.primary,
                                ),
                                const SizedBox(width: 6),
                                Expanded(
                                  child: Text(
                                    block.title,
                                    style: TextStyle(
                                      fontWeight: FontWeight.w700,
                                      fontSize: 14,
                                      color: isBreak ? Colors.amber[900] : null,
                                    ),
                                  ),
                                ),
                              ],
                            ),
                            const SizedBox(height: 4),
                            Text(
                              block.reason,
                              style: theme.textTheme.bodySmall?.copyWith(
                                fontStyle: FontStyle.italic,
                              ),
                            ),
                          ],
                        ),
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
