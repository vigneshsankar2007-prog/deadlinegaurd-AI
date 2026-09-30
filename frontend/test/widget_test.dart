import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:provider/provider.dart';
import 'package:deadlineguard_frontend/app.dart';
import 'package:deadlineguard_frontend/providers/theme_provider.dart';
import 'package:deadlineguard_frontend/providers/auth_provider.dart';
import 'package:deadlineguard_frontend/providers/task_provider.dart';
import 'package:deadlineguard_frontend/providers/subject_provider.dart';
import 'package:deadlineguard_frontend/providers/notification_provider.dart';
import 'package:deadlineguard_frontend/providers/focus_timer_provider.dart';
import 'package:deadlineguard_frontend/providers/analytics_provider.dart';
import 'package:deadlineguard_frontend/providers/ai_provider.dart';
import 'package:deadlineguard_frontend/screens/dashboard/dashboard_screen.dart';
import 'package:deadlineguard_frontend/screens/tasks/task_list_screen.dart';
import 'package:deadlineguard_frontend/screens/tasks/task_details_screen.dart';
import 'package:deadlineguard_frontend/screens/subjects/subject_management_screen.dart';
import 'package:deadlineguard_frontend/screens/focus/focus_timer_screen.dart';
import 'package:deadlineguard_frontend/screens/notifications/notifications_screen.dart';
import 'package:deadlineguard_frontend/screens/analytics/analytics_screen.dart';

Widget createTestableWidget(Widget child) {
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
    child: MaterialApp(home: child),
  );
}

void main() {
  group('Stage 11: Flutter Frontend Test Suite', () {
    testWidgets('1. App starts successfully', (WidgetTester tester) async {
      await tester.pumpWidget(const DeadlineGuardApp());
      expect(find.byType(MaterialApp), findsOneWidget);
    });

    test('2. Theme toggle works locally', () {
      final themeProvider = ThemeProvider();
      expect(themeProvider.isDarkMode, isFalse);
      themeProvider.toggleTheme();
      expect(themeProvider.isDarkMode, isTrue);
      themeProvider.toggleTheme();
      expect(themeProvider.isDarkMode, isFalse);
    });

    test('3. Login validation checks empty and invalid email', () async {
      final authProvider = AuthProvider();
      final resEmpty = await authProvider.login('', '123');
      expect(resEmpty, isFalse);
      expect(authProvider.errorMessage, isNotNull);

      final resValid = await authProvider.login('test@university.edu', 'password123');
      expect(resValid, isTrue);
      expect(authProvider.isAuthenticated, isTrue);
    });

    test('4. Register validation enforces password match', () async {
      final authProvider = AuthProvider();
      final res = await authProvider.register(
        name: 'New Student',
        email: 'new@university.edu',
        password: 'password123',
        confirmPassword: 'mismatch_password',
        college: 'Engineering',
        department: 'CS',
        semester: 3,
      );
      expect(res, isFalse);
      expect(authProvider.errorMessage, contains('do not match'));
    });

    test('5 & 6 & 7. Task list provider filters and sorts deliverables', () {
      final taskProvider = TaskProvider();
      expect(taskProvider.allTasks.isNotEmpty, isTrue);

      // Filter by COMPLETED
      taskProvider.setStatusFilter('COMPLETED');
      expect(taskProvider.filteredTasks.every((t) => t.isCompleted), isTrue);

      // Filter by ALL
      taskProvider.setStatusFilter('ALL');
      expect(taskProvider.filteredTasks.length, equals(taskProvider.allTasks.length));

      // Sort by Priority
      taskProvider.setSortOption('Priority (Highest First)');
      final sorted = taskProvider.filteredTasks;
      for (int i = 0; i < sorted.length - 1; i++) {
        expect(sorted[i].priorityScore >= sorted[i + 1].priorityScore, isTrue);
      }
    });

    test('8. Add Task adds a local task to state', () {
      final taskProvider = TaskProvider();
      final initialCount = taskProvider.allTasks.length;

      taskProvider.addTask(
        title: 'New Unit Test Deliverable',
        taskType: 'ASSIGNMENT',
        deadline: DateTime.now().add(const Duration(days: 3)),
        difficulty: 3,
        academicWeight: 15.0,
        estimatedHours: 2.5,
      );

      expect(taskProvider.allTasks.length, equals(initialCount + 1));
      expect(taskProvider.allTasks.last.title, equals('New Unit Test Deliverable'));
    });

    testWidgets('9. Task details shows Stage 6 priority data',
        (WidgetTester tester) async {
      await tester.pumpWidget(createTestableWidget(const TaskDetailsScreen(taskId: 1)));
      await tester.pumpAndSettle();

      expect(find.textContaining('Stage 6 Priority Calculation Breakdown'), findsOneWidget);
      expect(find.textContaining('Urgency (40%)'), findsOneWidget);
    });

    testWidgets('10. Subject list renders mock courses',
        (WidgetTester tester) async {
      await tester.pumpWidget(createTestableWidget(const SubjectManagementScreen()));
      await tester.pumpAndSettle();

      expect(find.text('CS301'), findsOneWidget);
      expect(find.text('CS302'), findsOneWidget);
    });

    test('11 & 12. Notification unread count and mark as read work', () {
      final notifProvider = NotificationProvider();
      final initialUnread = notifProvider.unreadCount;
      expect(initialUnread > 0, isTrue);

      notifProvider.markAllAsRead();
      expect(notifProvider.unreadCount, equals(0));
    });

    test('13 & 14 & 15. Focus timer starts, pauses, and stops', () {
      final timerProvider = FocusTimerProvider();
      expect(timerProvider.isIdle, isTrue);

      timerProvider.startTimer();
      expect(timerProvider.isRunning, isTrue);

      timerProvider.pauseTimer();
      expect(timerProvider.isPaused, isTrue);

      timerProvider.resumeTimer();
      expect(timerProvider.isRunning, isTrue);

      timerProvider.stopTimer();
      expect(timerProvider.isIdle, isTrue);
    });

    testWidgets('16. Analytics screen renders productivity metrics',
        (WidgetTester tester) async {
      await tester.pumpWidget(createTestableWidget(const AnalyticsScreen()));
      await tester.pumpAndSettle();

      expect(find.text('Productivity Analytics'), findsOneWidget);
      expect(find.text('7-Day Productivity Score Trend'), findsOneWidget);
    });

    testWidgets('17. Dashboard screen renders greetings and KPI cards',
        (WidgetTester tester) async {
      await tester.pumpWidget(createTestableWidget(const DashboardScreen()));
      await tester.pumpAndSettle();

      expect(find.textContaining('Hello,'), findsOneWidget);
      expect(find.text('WHAT SHOULD I DO NOW?'), findsOneWidget);
    });
  });
}
