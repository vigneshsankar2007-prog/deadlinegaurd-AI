/**
 * Stage 10 Productivity Analytics domain models for dashboard and charts.
 */
class DailyAnalyticsPointModel {
  final DateTime date;
  final int tasksCompleted;
  final int tasksOverdue;
  final int studyMinutes;
  final double productivityScore;

  const DailyAnalyticsPointModel({
    required this.date,
    required this.tasksCompleted,
    required this.tasksOverdue,
    required this.studyMinutes,
    required this.productivityScore,
  });
}

class SubjectWorkloadModel {
  final int subjectId;
  final String subjectName;
  final String subjectCode;
  final String colorHex;
  final int taskCount;
  final int completedTaskCount;
  final double estimatedWorkloadHours;
  final int actualStudyMinutes;
  final double completionPercentage;

  const SubjectWorkloadModel({
    required this.subjectId,
    required this.subjectName,
    required this.subjectCode,
    required this.colorHex,
    required this.taskCount,
    required this.completedTaskCount,
    required this.estimatedWorkloadHours,
    required this.actualStudyMinutes,
    required this.completionPercentage,
  });
}

class AnalyticsDashboardModel {
  final int totalTasks;
  final int completedTasks;
  final int pendingTasks;
  final int overdueTasks;
  final double completionPercentage;
  final double overdueRate;
  final int totalStudyMinutes;
  final double totalStudyHours;
  final double averageSessionMinutes;
  final double currentProductivityScore;
  final int weeklyStudyMinutes;
  final int weeklyCompletedTasks;
  final int weeklyOverdueTasks;
  final int activeTaskCount;
  final int criticalTaskCount;
  final List<DailyAnalyticsPointModel> weeklyPoints;
  final List<SubjectWorkloadModel> subjects;

  const AnalyticsDashboardModel({
    required this.totalTasks,
    required this.completedTasks,
    required this.pendingTasks,
    required this.overdueTasks,
    required this.completionPercentage,
    required this.overdueRate,
    required this.totalStudyMinutes,
    required this.totalStudyHours,
    required this.averageSessionMinutes,
    required this.currentProductivityScore,
    required this.weeklyStudyMinutes,
    required this.weeklyCompletedTasks,
    required this.weeklyOverdueTasks,
    required this.activeTaskCount,
    required this.criticalTaskCount,
    required this.weeklyPoints,
    required this.subjects,
  });
}
