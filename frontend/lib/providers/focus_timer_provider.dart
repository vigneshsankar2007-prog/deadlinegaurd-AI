import 'dart:async';
import 'package:flutter/material.dart';
import '../models/task_model.dart';
import '../models/study_session_model.dart';
import '../mock/mock_data.dart';

enum TimerState { idle, running, paused, completed }

/**
 * State provider driving the Focus Mode Pomodoro/Study Session timer (Stage 8).
 */
class FocusTimerProvider with ChangeNotifier {
  Timer? _timer;
  TimerState _timerState = TimerState.idle;
  int _secondsElapsed = 0;
  int _targetMinutes = 25; // Default 25-minute Pomodoro
  TaskModel? _selectedTask = MockData.initialTasks[0];
  final List<StudySessionModel> _completedSessions =
      List.from(MockData.initialStudySessions);

  TimerState get timerState => _timerState;
  int get secondsElapsed => _secondsElapsed;
  int get targetMinutes => _targetMinutes;
  TaskModel? get selectedTask => _selectedTask;
  List<StudySessionModel> get completedSessions =>
      List.unmodifiable(_completedSessions);

  bool get isRunning => _timerState == TimerState.running;
  bool get isPaused => _timerState == TimerState.paused;
  bool get isIdle => _timerState == TimerState.idle;

  String get formattedTime {
    final minutes = (_secondsElapsed ~/ 60).toString().padLeft(2, '0');
    final seconds = (_secondsElapsed % 60).toString().padLeft(2, '0');
    return '$minutes:$seconds';
  }

  double get progressPercentage {
    final targetSeconds = _targetMinutes * 60;
    if (targetSeconds == 0) return 0.0;
    return (_secondsElapsed / targetSeconds).clamp(0.0, 1.0);
  }

  void setSelectedTask(TaskModel? task) {
    if (_timerState == TimerState.idle) {
      _selectedTask = task;
      notifyListeners();
    }
  }

  void setTargetMinutes(int minutes) {
    if (_timerState == TimerState.idle) {
      _targetMinutes = minutes;
      notifyListeners();
    }
  }

  void startTimer() {
    if (_timerState == TimerState.idle) {
      _secondsElapsed = 0;
      _timerState = TimerState.running;
      _startTicker();
      notifyListeners();
    }
  }

  void pauseTimer() {
    if (_timerState == TimerState.running) {
      _timer?.cancel();
      _timerState = TimerState.paused;
      notifyListeners();
    }
  }

  void resumeTimer() {
    if (_timerState == TimerState.paused) {
      _timerState = TimerState.running;
      _startTicker();
      notifyListeners();
    }
  }

  void stopTimer() {
    _timer?.cancel();
    if (_secondsElapsed > 60) {
      // Record completed session if more than 1 minute
      final durationMin = (_secondsElapsed / 60).ceil();
      final newSession = StudySessionModel(
        id: _completedSessions.length + 1,
        taskId: _selectedTask?.id,
        taskTitle: _selectedTask?.title,
        startTime: DateTime.now().subtract(Duration(seconds: _secondsElapsed)),
        endTime: DateTime.now(),
        durationMinutes: durationMin,
        isCompleted: true,
        notes: 'Focused study session completed.',
      );
      _completedSessions.insert(0, newSession);
    }
    _secondsElapsed = 0;
    _timerState = TimerState.idle;
    notifyListeners();
  }

  void _startTicker() {
    _timer = Timer.periodic(const Duration(seconds: 1), (timer) {
      _secondsElapsed++;
      notifyListeners();
    });
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }
}
