import 'package:flutter/material.dart';
import '../models/task_model.dart';
import '../models/subject_model.dart';
import '../mock/mock_data.dart';
import '../core/constants/app_constants.dart';

/**
 * State provider managing tasks, filters, sorting, and lifecycle transitions.
 */
class TaskProvider with ChangeNotifier {
  final List<TaskModel> _tasks = List.from(MockData.initialTasks);
  String _selectedStatusFilter = 'ALL';
  String _selectedSortOption = AppConstants.sortPriority;
  String _searchQuery = '';

  List<TaskModel> get allTasks => List.unmodifiable(_tasks);
  String get selectedStatusFilter => _selectedStatusFilter;
  String get selectedSortOption => _selectedSortOption;
  String get searchQuery => _searchQuery;

  List<TaskModel> get filteredTasks {
    List<TaskModel> list = _tasks.where((task) {
      // Status filter
      if (_selectedStatusFilter != 'ALL') {
        if (_selectedStatusFilter == 'OVERDUE') {
          if (!task.isOverdue) return false;
        } else if (task.status.toUpperCase() != _selectedStatusFilter) {
          return false;
        }
      }

      // Search query
      if (_searchQuery.isNotEmpty) {
        final query = _searchQuery.toLowerCase();
        final titleMatches = task.title.toLowerCase().contains(query);
        final subjectMatches =
            task.subject?.subjectName.toLowerCase().contains(query) ?? false;
        final codeMatches =
            task.subject?.subjectCode.toLowerCase().contains(query) ?? false;
        if (!titleMatches && !subjectMatches && !codeMatches) return false;
      }

      return true;
    }).toList();

    // Sort
    if (_selectedSortOption == AppConstants.sortPriority) {
      list.sort((a, b) => b.priorityScore.compareTo(a.priorityScore));
    } else if (_selectedSortOption == AppConstants.sortDeadline) {
      list.sort((a, b) => a.deadline.compareTo(b.deadline));
    } else if (_selectedSortOption == AppConstants.sortEffort) {
      list.sort((a, b) => a.estimatedHours.compareTo(b.estimatedHours));
    }

    return list;
  }

  void setStatusFilter(String filter) {
    _selectedStatusFilter = filter;
    notifyListeners();
  }

  void setSortOption(String sort) {
    _selectedSortOption = sort;
    notifyListeners();
  }

  void setSearchQuery(String query) {
    _searchQuery = query;
    notifyListeners();
  }

  TaskModel? getTaskById(int id) {
    try {
      return _tasks.firstWhere((t) => t.id == id);
    } catch (_) {
      return null;
    }
  }

  void addTask({
    required String title,
    String? description,
    required String taskType,
    SubjectModel? subject,
    required DateTime deadline,
    required int difficulty,
    required double academicWeight,
    required double estimatedHours,
  }) {
    final nextId = _tasks.isEmpty
        ? 1
        : _tasks.map((t) => t.id).reduce((a, b) => a > b ? a : b) + 1;

    // Use initial default priority scores for new tasks
    final newTask = TaskModel(
      id: nextId,
      title: title.trim(),
      description: description?.trim(),
      taskType: taskType,
      subject: subject,
      deadline: deadline,
      status: 'PENDING',
      difficulty: difficulty,
      academicWeight: academicWeight,
      estimatedHours: estimatedHours,
      priorityScore: 65.00,
      priorityLevel: 'HIGH',
      urgencyScore: 70.00,
      difficultyScore: (difficulty * 20.0),
      weightScore: academicWeight,
      effortScore: (estimatedHours * 10.0).clamp(0.0, 100.0),
      workloadScore: 60.00,
      explanationText: 'Calculated using Stage 6 5-factor priority formula based on estimated hours and deadline.',
    );

    _tasks.add(newTask);
    notifyListeners();
  }

  void toggleTaskCompletion(int taskId) {
    final index = _tasks.indexWhere((t) => t.id == taskId);
    if (index != -1) {
      final task = _tasks[index];
      final newStatus = task.isCompleted ? 'PENDING' : 'COMPLETED';
      _tasks[index] = task.copyWith(
        status: newStatus,
        priorityScore: newStatus == 'COMPLETED' ? 0.0 : task.priorityScore,
        priorityLevel: newStatus == 'COMPLETED' ? 'LOW' : task.priorityLevel,
      );
      notifyListeners();
    }
  }

  void deleteTask(int taskId) {
    _tasks.removeWhere((t) => t.id == taskId);
    notifyListeners();
  }
}
