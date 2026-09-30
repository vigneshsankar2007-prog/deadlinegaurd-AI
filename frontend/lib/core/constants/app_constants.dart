/**
 * Centralized Application Constants and Route Definitions
 */
class AppConstants {
  AppConstants._();

  static const String appName = 'DeadlineGuard AI';
  static const String appTagline = 'Intelligent Academic Task & Deadline Management';
  static const String appVersion = 'v1.0.0';

  // Navigation Route Names (All 15 Planned Screens)
  static const String routeSplash = '/';
  static const String routeLogin = '/login';
  static const String routeRegister = '/register';
  static const String routeDashboard = '/dashboard';
  static const String routeTasks = '/tasks';
  static const String routeAddTask = '/tasks/add';
  static const String routeTaskDetails = '/tasks/details';
  static const String routeSubjects = '/subjects';
  static const String routeAIRecommendations = '/ai/recommendations';
  static const String routeStudyPlanner = '/study-planner';
  static const String routeFocusTimer = '/focus-timer';
  static const String routeNotifications = '/notifications';
  static const String routeAnalytics = '/analytics';
  static const String routeProfile = '/profile';
  static const String routeSettings = '/settings';

  // Task Statuses
  static const List<String> taskStatuses = [
    'ALL',
    'PENDING',
    'IN_PROGRESS',
    'COMPLETED',
    'OVERDUE',
  ];

  // Task Types matching MySQL schema
  static const List<String> taskTypes = [
    'ASSIGNMENT',
    'LAB',
    'PROJECT',
    'QUIZ',
    'EXAM',
    'READING',
    'OTHER',
  ];

  // Sort Options
  static const String sortPriority = 'Priority (Highest First)';
  static const String sortDeadline = 'Deadline (Earliest First)';
  static const String sortEffort = 'Effort (Shortest First)';

  static const List<String> sortOptions = [
    sortPriority,
    sortDeadline,
    sortEffort,
  ];
}
