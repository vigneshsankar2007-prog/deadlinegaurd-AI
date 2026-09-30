/**
 * Focus study timer session domain model matching Stage 8.
 */
class StudySessionModel {
  final int id;
  final int? taskId;
  final String? taskTitle;
  final DateTime startTime;
  final DateTime? endTime;
  final int durationMinutes;
  final bool isCompleted;
  final String? notes;

  const StudySessionModel({
    required this.id,
    this.taskId,
    this.taskTitle,
    required this.startTime,
    this.endTime,
    required this.durationMinutes,
    required this.isCompleted,
    this.notes,
  });

  StudySessionModel copyWith({
    int? id,
    int? taskId,
    String? taskTitle,
    DateTime? startTime,
    DateTime? endTime,
    int? durationMinutes,
    bool? isCompleted,
    String? notes,
  }) {
    return StudySessionModel(
      id: id ?? this.id,
      taskId: taskId ?? this.taskId,
      taskTitle: taskTitle ?? this.taskTitle,
      startTime: startTime ?? this.startTime,
      endTime: endTime ?? this.endTime,
      durationMinutes: durationMinutes ?? this.durationMinutes,
      isCompleted: isCompleted ?? this.isCompleted,
      notes: notes ?? this.notes,
    );
  }
}
