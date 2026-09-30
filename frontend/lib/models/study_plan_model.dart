/**
 * AI Study Planner schedule block domain model matching Stage 8.
 */
class StudyPlanBlockModel {
  final int? taskId;
  final String title;
  final String startTime;
  final String endTime;
  final int durationMinutes;
  final bool isBreak;
  final String reason;

  const StudyPlanBlockModel({
    this.taskId,
    required this.title,
    required this.startTime,
    required this.endTime,
    required this.durationMinutes,
    required this.isBreak,
    required this.reason,
  });
}

class StudyPlanScheduleModel {
  final double requestedHours;
  final int totalStudyMinutes;
  final int totalBreakMinutes;
  final List<StudyPlanBlockModel> blocks;
  final String summaryNotes;

  const StudyPlanScheduleModel({
    required this.requestedHours,
    required this.totalStudyMinutes,
    required this.totalBreakMinutes,
    required this.blocks,
    required this.summaryNotes,
  });
}
