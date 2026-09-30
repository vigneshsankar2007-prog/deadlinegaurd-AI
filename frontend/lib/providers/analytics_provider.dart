import 'package:flutter/material.dart';
import '../models/analytics_model.dart';
import '../mock/mock_data.dart';
import 'task_provider.dart';
import 'subject_provider.dart';

/**
 * State provider computing Stage 10 Productivity Analytics for charts and KPI cards.
 */
class AnalyticsProvider with ChangeNotifier {
  final List<DailyAnalyticsPointModel> _weeklyPoints =
      List.from(MockData.initialWeeklyPoints);

  List<DailyAnalyticsPointModel> get weeklyPoints =>
      List.unmodifiable(_weeklyPoints);

  AnalyticsDashboardModel getDashboardData(
    TaskProvider taskProvider,
    SubjectProvider subjectProvider,
  ) {
    final tasks = taskProvider.allTasks;
    final totalTasks = tasks.length;
    final completedTasks = tasks.where((t) => t.isCompleted).length;
    final pendingTasks =
        tasks.where((t) => t.status.toUpperCase() == 'PENDING').length;
    final overdueTasks = tasks.where((t) => t.isOverdue).length;
    final activeTasks =
        tasks.where((t) => !t.isCompleted).length;

    final criticalTasks =
        tasks.where((t) => !t.isCompleted && t.priorityLevel.toUpperCase() == 'CRITICAL').length;

    final completionPct =
        totalTasks == 0 ? 0.0 : (completedTasks / totalTasks) * 100.0;
    final overdueRate =
        totalTasks == 0 ? 0.0 : (overdueTasks / totalTasks) * 100.0;

    // Study minutes calculated from weekly points
    int weeklyStudyMins = 0;
    int weeklyCompleted = 0;
    int weeklyOverdue = 0;
    for (var point in _weeklyPoints) {
      weeklyStudyMins += point.studyMinutes;
      weeklyCompleted += point.tasksCompleted;
      weeklyOverdue += point.tasksOverdue;
    }

    final totalStudyMins = weeklyStudyMins + 120; // including earlier history
    final totalStudyHours = totalStudyMins / 60.0;
    final averageSessionMins = 48.0;

    // Subject workloads
    final List<SubjectWorkloadModel> subjectWorkloads = [];
    for (var sub in subjectProvider.subjects) {
      final subTasks = tasks.where((t) => t.subject?.id == sub.id).toList();
      final subCompleted = subTasks.where((t) => t.isCompleted).length;
      double workloadHours = 0;
      for (var t in subTasks) {
        workloadHours += t.estimatedHours;
      }

      subjectWorkloads.add(SubjectWorkloadModel(
        subjectId: sub.id,
        subjectName: sub.subjectName,
        subjectCode: sub.subjectCode,
        colorHex: sub.colorHex,
        taskCount: subTasks.length,
        completedTaskCount: subCompleted,
        estimatedWorkloadHours: workloadHours,
        actualStudyMinutes: subCompleted * 50 + 30,
        completionPercentage: subTasks.isEmpty
            ? 0.0
            : (subCompleted / subTasks.length) * 100.0,
      ));
    }

    return AnalyticsDashboardModel(
      totalTasks: totalTasks,
      completedTasks: completedTasks,
      pendingTasks: pendingTasks,
      overdueTasks: overdueTasks,
      completionPercentage: completionPct,
      overdueRate: overdueRate,
      totalStudyMinutes: totalStudyMins,
      totalStudyHours: totalStudyHours,
      averageSessionMinutes: averageSessionMins,
      currentProductivityScore: _weeklyPoints.last.productivityScore,
      weeklyStudyMinutes: weeklyStudyMins,
      weeklyCompletedTasks: weeklyCompleted,
      weeklyOverdueTasks: weeklyOverdue,
      activeTaskCount: activeTasks,
      criticalTaskCount: criticalTasks,
      weeklyPoints: _weeklyPoints,
      subjects: subjectWorkloads,
    );
  }
}
