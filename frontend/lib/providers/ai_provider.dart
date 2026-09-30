import 'package:flutter/material.dart';
import '../models/ai_recommendation_model.dart';
import '../models/study_plan_model.dart';
import '../mock/mock_data.dart';

/**
 * State provider managing AI Recommendations and Study Planner schedules (Stage 7 & 8).
 */
class AIProvider with ChangeNotifier {
  AIRecommendationModel _currentRecommendation =
      MockData.initialAIRecommendation;
  StudyPlanScheduleModel _currentPlan = MockData.initialStudyPlan;
  bool _isGeneratingPlan = false;
  bool _isGeneratingRecommendation = false;

  AIRecommendationModel get currentRecommendation => _currentRecommendation;
  StudyPlanScheduleModel get currentPlan => _currentPlan;
  bool get isGeneratingPlan => _isGeneratingPlan;
  bool get isGeneratingRecommendation => _isGeneratingRecommendation;

  Future<void> refreshRecommendation() async {
    _isGeneratingRecommendation = true;
    notifyListeners();

    await Future.delayed(const Duration(milliseconds: 700));

    _currentRecommendation = MockData.initialAIRecommendation;
    _isGeneratingRecommendation = false;
    notifyListeners();
  }

  Future<void> generateStudyPlan({
    required double availableHours,
    required String startTime,
    required int breakDurationMinutes,
  }) async {
    _isGeneratingPlan = true;
    notifyListeners();

    await Future.delayed(const Duration(milliseconds: 800));

    final totalMins = (availableHours * 60).toInt();
    final breakMins = (totalMins * 0.15).toInt();
    final studyMins = totalMins - breakMins;

    _currentPlan = StudyPlanScheduleModel(
      requestedHours: availableHours,
      totalStudyMinutes: studyMins,
      totalBreakMinutes: breakMins,
      summaryNotes:
          'Personalized $availableHours-hour study schedule sequenced by Stage 6 priority, starting at $startTime with $breakDurationMinutes-minute breaks.',
      blocks: MockData.initialStudyPlan.blocks,
    );

    _isGeneratingPlan = false;
    notifyListeners();
  }
}
