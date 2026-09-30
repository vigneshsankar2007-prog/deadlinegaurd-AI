import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'core/theme/app_theme.dart';
import 'core/constants/app_constants.dart';
import 'core/routing/app_router.dart';
import 'providers/theme_provider.dart';
import 'providers/auth_provider.dart';
import 'providers/task_provider.dart';
import 'providers/subject_provider.dart';
import 'providers/notification_provider.dart';
import 'providers/focus_timer_provider.dart';
import 'providers/analytics_provider.dart';
import 'providers/ai_provider.dart';

/**
 * Root DeadlineGuard AI Application Widget with centralized MultiProvider.
 */
class DeadlineGuardApp extends StatelessWidget {
  const DeadlineGuardApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => ThemeProvider()),
        ChangeNotifierProvider(create: (_) => AuthProvider()),
        ChangeNotifierProvider(create: (_) => TaskProvider()),
        ChangeNotifierProvider(create: (_) => SubjectProvider()),
        ChangeNotifierProvider(create: (_) => NotificationProvider()),
        ChangeNotifierProvider(create: (_) => FocusTimerProvider()),
        ChangeNotifierProvider(create: (_) => AnalyticsProvider()),
        ChangeNotifierProvider(create: (_) => AIProvider()),
      ],
      child: Consumer<ThemeProvider>(
        builder: (context, themeProvider, _) {
          return MaterialApp(
            title: AppConstants.appName,
            debugShowCheckedModeBanner: false,
            theme: AppTheme.lightTheme,
            darkTheme: AppTheme.darkTheme,
            themeMode: themeProvider.themeMode,
            initialRoute: AppConstants.routeSplash,
            onGenerateRoute: AppRouter.onGenerateRoute,
          );
        },
      ),
    );
  }
}
