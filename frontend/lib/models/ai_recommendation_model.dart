import 'task_model.dart';

/**
 * AI "What should I do now?" recommendation model matching Stage 7.
 */
class AIRecommendationModel {
  final TaskModel recommendedTask;
  final String recommendationHeadline;
  final String reasoning;
  final String suggestedAction;
  final int recommendedFocusMinutes;
  final DateTime generatedAt;

  const AIRecommendationModel({
    required this.recommendedTask,
    required this.recommendationHeadline,
    required this.reasoning,
    required this.suggestedAction,
    required this.recommendedFocusMinutes,
    required this.generatedAt,
  });
}
