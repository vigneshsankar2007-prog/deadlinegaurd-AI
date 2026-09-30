import 'subject_model.dart';

/**
 * Student academic task domain model with complete Stage 6 priority parameters.
 */
class TaskModel {
  final int id;
  final String title;
  final String? description;
  final String taskType;
  final SubjectModel? subject;
  final DateTime deadline;
  final String status; // PENDING, IN_PROGRESS, COMPLETED, OVERDUE
  final int difficulty; // 1 - 5
  final double academicWeight; // 0.00 - 100.00
  final double estimatedHours;

  // Stage 6 Authoritative Priority Engine Breakdown
  final double priorityScore;
  final String priorityLevel; // LOW, MEDIUM, HIGH, CRITICAL
  final double urgencyScore;
  final double difficultyScore;
  final double weightScore;
  final double effortScore;
  final double workloadScore;
  final String explanationText;

  const TaskModel({
    required this.id,
    required this.title,
    this.description,
    required this.taskType,
    this.subject,
    required this.deadline,
    required this.status,
    required this.difficulty,
    required this.academicWeight,
    required this.estimatedHours,
    required this.priorityScore,
    required this.priorityLevel,
    required this.urgencyScore,
    required this.difficultyScore,
    required this.weightScore,
    required this.effortScore,
    required this.workloadScore,
    required this.explanationText,
  });

  bool get isCompleted => status.toUpperCase() == 'COMPLETED';
  bool get isOverdue =>
      status.toUpperCase() != 'COMPLETED' && deadline.isBefore(DateTime.now());

  TaskModel copyWith({
    int? id,
    String? title,
    String? description,
    String? taskType,
    SubjectModel? subject,
    DateTime? deadline,
    String? status,
    int? difficulty,
    double? academicWeight,
    double? estimatedHours,
    double? priorityScore,
    String? priorityLevel,
    double? urgencyScore,
    double? difficultyScore,
    double? weightScore,
    double? effortScore,
    double? workloadScore,
    String? explanationText,
  }) {
    return TaskModel(
      id: id ?? this.id,
      title: title ?? this.title,
      description: description ?? this.description,
      taskType: taskType ?? this.taskType,
      subject: subject ?? this.subject,
      deadline: deadline ?? this.deadline,
      status: status ?? this.status,
      difficulty: difficulty ?? this.difficulty,
      academicWeight: academicWeight ?? this.academicWeight,
      estimatedHours: estimatedHours ?? this.estimatedHours,
      priorityScore: priorityScore ?? this.priorityScore,
      priorityLevel: priorityLevel ?? this.priorityLevel,
      urgencyScore: urgencyScore ?? this.urgencyScore,
      difficultyScore: difficultyScore ?? this.difficultyScore,
      weightScore: weightScore ?? this.weightScore,
      effortScore: effortScore ?? this.effortScore,
      workloadScore: workloadScore ?? this.workloadScore,
      explanationText: explanationText ?? this.explanationText,
    );
  }
}
