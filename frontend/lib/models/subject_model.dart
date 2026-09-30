import 'package:flutter/material.dart';

/**
 * Course catalog subject domain model.
 */
class SubjectModel {
  final int id;
  final String subjectName;
  final String subjectCode;
  final int credits;
  final String colorHex;
  final int taskCount;
  final int completedTaskCount;

  const SubjectModel({
    required this.id,
    required this.subjectName,
    required this.subjectCode,
    required this.credits,
    required this.colorHex,
    this.taskCount = 0,
    this.completedTaskCount = 0,
  });

  double get completionPercentage =>
      taskCount == 0 ? 0.0 : (completedTaskCount / taskCount) * 100.0;

  Color get color {
    try {
      final hex = colorHex.replaceAll('#', '');
      return Color(int.parse('FF$hex', radix: 16));
    } catch (_) {
      return const Color(0xFF3B82F6);
    }
  }

  SubjectModel copyWith({
    int? id,
    String? subjectName,
    String? subjectCode,
    int? credits,
    String? colorHex,
    int? taskCount,
    int? completedTaskCount,
  }) {
    return SubjectModel(
      id: id ?? this.id,
      subjectName: subjectName ?? this.subjectName,
      subjectCode: subjectCode ?? this.subjectCode,
      credits: credits ?? this.credits,
      colorHex: colorHex ?? this.colorHex,
      taskCount: taskCount ?? this.taskCount,
      completedTaskCount: completedTaskCount ?? this.completedTaskCount,
    );
  }
}
