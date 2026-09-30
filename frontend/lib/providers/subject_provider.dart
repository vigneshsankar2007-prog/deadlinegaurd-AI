import 'package:flutter/material.dart';
import '../models/subject_model.dart';
import '../mock/mock_data.dart';

/**
 * State provider managing course subjects enrolled by the student.
 */
class SubjectProvider with ChangeNotifier {
  final List<SubjectModel> _subjects = List.from(MockData.initialSubjects);

  List<SubjectModel> get subjects => List.unmodifiable(_subjects);

  SubjectModel? getSubjectById(int id) {
    try {
      return _subjects.firstWhere((s) => s.id == id);
    } catch (_) {
      return null;
    }
  }

  void addSubject({
    required String name,
    required String code,
    required int credits,
    required String colorHex,
  }) {
    final nextId = _subjects.isEmpty
        ? 1
        : _subjects.map((s) => s.id).reduce((a, b) => a > b ? a : b) + 1;

    final newSubject = SubjectModel(
      id: nextId,
      subjectName: name.trim(),
      subjectCode: code.trim().toUpperCase(),
      credits: credits,
      colorHex: colorHex,
      taskCount: 0,
      completedTaskCount: 0,
    );

    _subjects.add(newSubject);
    notifyListeners();
  }

  void updateSubject({
    required int id,
    required String name,
    required String code,
    required int credits,
    required String colorHex,
  }) {
    final index = _subjects.indexWhere((s) => s.id == id);
    if (index != -1) {
      _subjects[index] = _subjects[index].copyWith(
        subjectName: name.trim(),
        subjectCode: code.trim().toUpperCase(),
        credits: credits,
        colorHex: colorHex,
      );
      notifyListeners();
    }
  }

  void deleteSubject(int id) {
    _subjects.removeWhere((s) => s.id == id);
    notifyListeners();
  }
}
