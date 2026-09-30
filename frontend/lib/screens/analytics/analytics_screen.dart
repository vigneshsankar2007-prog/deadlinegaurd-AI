import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:fl_chart/fl_chart.dart';
import '../../core/constants/app_colors.dart';
import '../../core/widgets/app_card.dart';
import '../../providers/analytics_provider.dart';
import '../../providers/task_provider.dart';
import '../../providers/subject_provider.dart';

/**
 * Screen 13: Productivity Analytics Screen (Stage 10)
 * Visualizes 7-day completion bars, productivity trend line chart, and subject workload.
 */
class AnalyticsScreen extends StatelessWidget {
  const AnalyticsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final analytics = context.watch<AnalyticsProvider>();
    final taskProvider = context.watch<TaskProvider>();
    final subjectProvider = context.watch<SubjectProvider>();
    final data = analytics.getDashboardData(taskProvider, subjectProvider);
    final weeklyPoints = data.weeklyPoints;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Productivity Analytics'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // KPI Overview Banner
            AppCard(
              child: Column(
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceAround,
                    children: [
                      _buildMetricColumn(
                        'Weekly Focus',
                        '${data.weeklyStudyMinutes}m',
                        '${(data.weeklyStudyMinutes / 60).toStringAsFixed(1)} hrs',
                        AppColors.primary,
                      ),
                      _buildMetricColumn(
                        'Completed',
                        '${data.weeklyCompletedTasks}',
                        'Deliverables',
                        AppColors.success,
                      ),
                      _buildMetricColumn(
                        'Overdue',
                        '${data.weeklyOverdueTasks}',
                        'Pending review',
                        data.weeklyOverdueTasks > 0
                            ? AppColors.error
                            : AppColors.textSecondaryLight,
                      ),
                    ],
                  ),
                ],
              ),
            ),
            const SizedBox(height: 20),

            // Chart 1: 7-Day Productivity Score Trend (LineChart)
            Text(
              '7-Day Productivity Score Trend',
              style: theme.textTheme.titleMedium
                  ?.copyWith(fontWeight: FontWeight.w700),
            ),
            const SizedBox(height: 8),
            AppCard(
              padding: const EdgeInsets.fromLTRB(16, 20, 20, 16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  SizedBox(
                    height: 180,
                    child: LineChart(
                      LineChartData(
                        minY: 0,
                        maxY: 100,
                        gridData: const FlGridData(show: false),
                        titlesData: FlTitlesData(
                          rightTitles: const AxisTitles(
                              sideTitles: SideTitles(showTitles: false)),
                          topTitles: const AxisTitles(
                              sideTitles: SideTitles(showTitles: false)),
                          leftTitles: AxisTitles(
                            sideTitles: SideTitles(
                              showTitles: true,
                              reservedSize: 32,
                              interval: 25,
                              getTitlesWidget: (val, meta) => Text(
                                val.toInt().toString(),
                                style: const TextStyle(fontSize: 10),
                              ),
                            ),
                          ),
                          bottomTitles: AxisTitles(
                            sideTitles: SideTitles(
                              showTitles: true,
                              interval: 1,
                              getTitlesWidget: (val, meta) {
                                final idx = val.toInt();
                                if (idx >= 0 && idx < weeklyPoints.length) {
                                  final dt = weeklyPoints[idx].date;
                                  return Padding(
                                    padding: const EdgeInsets.only(top: 4),
                                    child: Text(
                                      '${dt.month}/${dt.day}',
                                      style: const TextStyle(fontSize: 9),
                                    ),
                                  );
                                }
                                return const SizedBox.shrink();
                              },
                            ),
                          ),
                        ),
                        borderData: FlBorderData(show: false),
                        lineBarsData: [
                          LineChartBarData(
                            spots: [
                              for (int i = 0; i < weeklyPoints.length; i++)
                                FlSpot(i.toDouble(),
                                    weeklyPoints[i].productivityScore),
                            ],
                            isCurved: true,
                            color: AppColors.primary,
                            barWidth: 3,
                            isStrokeCapRound: true,
                            belowBarData: BarAreaData(
                              show: true,
                              color: AppColors.primary.withOpacity(0.12),
                            ),
                            dotData: const FlDotData(show: true),
                          ),
                        ],
                      ),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 24),

            // Chart 2: Daily Focus Minutes (BarChart)
            Text(
              'Daily Focus Minutes',
              style: theme.textTheme.titleMedium
                  ?.copyWith(fontWeight: FontWeight.w700),
            ),
            const SizedBox(height: 8),
            AppCard(
              padding: const EdgeInsets.fromLTRB(16, 20, 20, 16),
              child: SizedBox(
                height: 180,
                child: BarChart(
                  BarChartData(
                    alignment: BarChartAlignment.spaceAround,
                    maxY: 140,
                    gridData: const FlGridData(show: false),
                    borderData: FlBorderData(show: false),
                    titlesData: FlTitlesData(
                      rightTitles: const AxisTitles(
                          sideTitles: SideTitles(showTitles: false)),
                      topTitles: const AxisTitles(
                          sideTitles: SideTitles(showTitles: false)),
                      leftTitles: AxisTitles(
                        sideTitles: SideTitles(
                          showTitles: true,
                          reservedSize: 32,
                          interval: 30,
                          getTitlesWidget: (val, meta) => Text(
                            '${val.toInt()}m',
                            style: const TextStyle(fontSize: 10),
                          ),
                        ),
                      ),
                      bottomTitles: AxisTitles(
                        sideTitles: SideTitles(
                          showTitles: true,
                          getTitlesWidget: (val, meta) {
                            final idx = val.toInt();
                            if (idx >= 0 && idx < weeklyPoints.length) {
                              final dt = weeklyPoints[idx].date;
                              return Padding(
                                padding: const EdgeInsets.only(top: 4),
                                child: Text(
                                  '${dt.month}/${dt.day}',
                                  style: const TextStyle(fontSize: 9),
                                ),
                              );
                            }
                            return const SizedBox.shrink();
                          },
                        ),
                      ),
                    ),
                    barGroups: [
                      for (int i = 0; i < weeklyPoints.length; i++)
                        BarChartGroupData(
                          x: i,
                          barRods: [
                            BarChartRodData(
                              toY: weeklyPoints[i].studyMinutes.toDouble(),
                              color: AppColors.secondary,
                              width: 14,
                              borderRadius: BorderRadius.circular(4),
                            ),
                          ],
                        ),
                    ],
                  ),
                ),
              ),
            ),
            const SizedBox(height: 24),

            // Subject Workload Distribution
            Text(
              'Course Workload & Focus Distribution',
              style: theme.textTheme.titleMedium
                  ?.copyWith(fontWeight: FontWeight.w700),
            ),
            const SizedBox(height: 12),
            ListView.separated(
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              itemCount: data.subjects.length,
              separatorBuilder: (_, __) => const SizedBox(height: 8),
              itemBuilder: (context, index) {
                final sub = data.subjects[index];
                return AppCard(
                  child: Row(
                    children: [
                      Container(
                        width: 42,
                        height: 42,
                        decoration: BoxDecoration(
                          color: AppColors.primaryContainer,
                          borderRadius: BorderRadius.circular(8),
                        ),
                        alignment: Alignment.center,
                        child: Text(
                          sub.subjectCode,
                          style: const TextStyle(
                            fontWeight: FontWeight.w800,
                            fontSize: 11,
                            color: AppColors.onPrimaryContainer,
                          ),
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              sub.subjectName,
                              style: const TextStyle(
                                  fontWeight: FontWeight.w600, fontSize: 13),
                            ),
                            Text(
                              '${sub.taskCount} tasks • ${sub.estimatedWorkloadHours.toStringAsFixed(1)} estimated hrs',
                              style: theme.textTheme.bodySmall,
                            ),
                          ],
                        ),
                      ),
                      Text(
                        '${sub.actualStudyMinutes}m',
                        style: const TextStyle(
                          fontWeight: FontWeight.w700,
                          fontSize: 14,
                          color: AppColors.primary,
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

  Widget _buildMetricColumn(
    String label,
    String value,
    String subtext,
    Color color,
  ) {
    return Column(
      children: [
        Text(
          value,
          style: TextStyle(
            fontWeight: FontWeight.w800,
            fontSize: 22,
            color: color,
          ),
        ),
        const SizedBox(height: 2),
        Text(
          label,
          style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 12),
        ),
        Text(
          subtext,
          style: const TextStyle(color: Colors.grey, fontSize: 11),
        ),
      ],
    );
  }
}
