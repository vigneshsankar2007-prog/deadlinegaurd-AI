/**
 * Student notification domain model matching Stage 9 notification types.
 */
class NotificationModel {
  final int id;
  final String title;
  final String message;
  final String notificationType; // DEADLINE_48H, DEADLINE_24H, DEADLINE_6H, OVERDUE, HIGH_PRIORITY, DAILY_SUMMARY
  final bool isRead;
  final DateTime createdAt;
  final int? taskId;

  const NotificationModel({
    required this.id,
    required this.title,
    required this.message,
    required this.notificationType,
    required this.isRead,
    required this.createdAt,
    this.taskId,
  });

  NotificationModel copyWith({
    int? id,
    String? title,
    String? message,
    String? notificationType,
    bool? isRead,
    DateTime? createdAt,
    int? taskId,
  }) {
    return NotificationModel(
      id: id ?? this.id,
      title: title ?? this.title,
      message: message ?? this.message,
      notificationType: notificationType ?? this.notificationType,
      isRead: isRead ?? this.isRead,
      createdAt: createdAt ?? this.createdAt,
      taskId: taskId ?? this.taskId,
    );
  }
}
