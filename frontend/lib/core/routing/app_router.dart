import 'package:flutter/material.dart';
import '../constants/app_constants.dart';
import '../../screens/splash/splash_screen.dart';
import '../../screens/auth/login_screen.dart';
import '../../screens/auth/register_screen.dart';
import '../../screens/main_navigation_shell.dart';
import '../../screens/tasks/task_list_screen.dart';
import '../../screens/tasks/add_task_screen.dart';
import '../../screens/tasks/task_details_screen.dart';
import '../../screens/subjects/subject_management_screen.dart';
import '../../screens/ai/ai_recommendations_screen.dart';
import '../../screens/study_planner/study_planner_screen.dart';
import '../../screens/focus/focus_timer_screen.dart';
import '../../screens/notifications/notifications_screen.dart';
import '../../screens/analytics/analytics_screen.dart';
import '../../screens/profile/profile_screen.dart';
import '../../screens/settings/settings_screen.dart';

/**
 * Centralized declarative route generator for all 15 screens.
 */
class AppRouter {
  AppRouter._();

  static Route<dynamic> onGenerateRoute(RouteSettings settings) {
    switch (settings.name) {
      case AppConstants.routeSplash:
        return MaterialPageRoute(builder: (_) => const SplashScreen());

      case AppConstants.routeLogin:
        return MaterialPageRoute(builder: (_) => const LoginScreen());

      case AppConstants.routeRegister:
        return MaterialPageRoute(builder: (_) => const RegisterScreen());

      case AppConstants.routeDashboard:
        final initialIndex = settings.arguments as int? ?? 0;
        return MaterialPageRoute(
          builder: (_) => MainNavigationShell(initialIndex: initialIndex),
        );

      case AppConstants.routeTasks:
        return MaterialPageRoute(builder: (_) => const TaskListScreen());

      case AppConstants.routeAddTask:
        return MaterialPageRoute(builder: (_) => const AddTaskScreen());

      case AppConstants.routeTaskDetails:
        final taskId = settings.arguments as int? ?? 1;
        return MaterialPageRoute(
          builder: (_) => TaskDetailsScreen(taskId: taskId),
        );

      case AppConstants.routeSubjects:
        return MaterialPageRoute(
            builder: (_) => const SubjectManagementScreen());

      case AppConstants.routeAIRecommendations:
        return MaterialPageRoute(
            builder: (_) => const AIRecommendationsScreen());

      case AppConstants.routeStudyPlanner:
        return MaterialPageRoute(builder: (_) => const StudyPlannerScreen());

      case AppConstants.routeFocusTimer:
        return MaterialPageRoute(builder: (_) => const FocusTimerScreen());

      case AppConstants.routeNotifications:
        return MaterialPageRoute(builder: (_) => const NotificationsScreen());

      case AppConstants.routeAnalytics:
        return MaterialPageRoute(builder: (_) => const AnalyticsScreen());

      case AppConstants.routeProfile:
        return MaterialPageRoute(builder: (_) => const ProfileScreen());

      case AppConstants.routeSettings:
        return MaterialPageRoute(builder: (_) => const SettingsScreen());

      default:
        return MaterialPageRoute(
          builder: (_) => Scaffold(
            body: Center(
              child: Text('No route defined for ${settings.name}'),
            ),
          ),
        );
    }
  }
}
