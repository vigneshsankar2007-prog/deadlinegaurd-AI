import '../models/user_model.dart';
import '../models/subject_model.dart';
import '../models/task_model.dart';
import '../models/notification_model.dart';
import '../models/study_session_model.dart';
import '../models/analytics_model.dart';
import '../models/ai_recommendation_model.dart';
import '../models/study_plan_model.dart';

/**
 * Centralized, realistic mock dataset strictly mirroring DeadlineGuard AI's backend domain.
 */
class MockData {
  MockData._();

  // Current Student User
  static const UserModel initialUser = UserModel(
    id: 1,
    name: 'Alex Morgan',
    email: 'alex.morgan@university.edu',
    college: 'College of Engineering & Technology',
    department: 'Computer Science & Engineering',
    semester: 5,
  );

  // Enrolled University Subjects
  static final List<SubjectModel> initialSubjects = [
    const SubjectModel(
      id: 1,
      subjectName: 'Design & Analysis of Algorithms',
      subjectCode: 'CS301',
      credits: 4,
      colorHex: '#3B82F6', // Blue
      taskCount: 3,
      completedTaskCount: 1,
    ),
    const SubjectModel(
      id: 2,
      subjectName: 'Operating Systems & Concurrency',
      subjectCode: 'CS302',
      credits: 4,
      colorHex: '#10B981', // Emerald
      taskCount: 3,
      completedTaskCount: 2,
    ),
    const SubjectModel(
      id: 3,
      subjectName: 'Database Management Systems',
      subjectCode: 'CS303',
      credits: 3,
      colorHex: '#8B5CF6', // Purple
      taskCount: 2,
      completedTaskCount: 0,
    ),
    const SubjectModel(
      id: 4,
      subjectName: 'Discrete Mathematics & Logic',
      subjectCode: 'MATH205',
      credits: 3,
      colorHex: '#F59E0B', // Amber
      taskCount: 2,
      completedTaskCount: 1,
    ),
    const SubjectModel(
      id: 5,
      subjectName: 'Compiler Construction',
      subjectCode: 'CS450',
      credits: 4,
      colorHex: '#EC4899', // Pink
      taskCount: 1,
      completedTaskCount: 0,
    ),
  ];

  // Academic Deliverables with Stage 6 Priority Explanations
  static final List<TaskModel> initialTasks = [
    TaskModel(
      id: 1,
      title: 'Dynamic Programming & Graph Lab',
      description: 'Implement Dijkstra shortest path and Bellman-Ford algorithms with benchmark analysis.',
      taskType: 'LAB',
      subject: initialSubjects[0],
      deadline: DateTime.now().add(const Duration(hours: 5)),
      status: 'IN_PROGRESS',
      difficulty: 4,
      academicWeight: 25.0,
      estimatedHours: 4.5,
      priorityScore: 88.50,
      priorityLevel: 'CRITICAL',
      urgencyScore: 95.00,
      difficultyScore: 80.00,
      weightScore: 85.00,
      effortScore: 78.00,
      workloadScore: 80.00,
      explanationText: 'Critical priority due to immediate deadline in 5 hours, high 25% academic weighting, and complex graph implementation effort.',
    ),
    TaskModel(
      id: 2,
      title: 'Kernel Process Scheduler Simulation',
      description: 'Develop multi-level feedback queue CPU scheduler in C++ with preemptive context switching.',
      taskType: 'PROJECT',
      subject: initialSubjects[1],
      deadline: DateTime.now().add(const Duration(hours: 22)),
      status: 'PENDING',
      difficulty: 5,
      academicWeight: 35.0,
      estimatedHours: 8.0,
      priorityScore: 82.20,
      priorityLevel: 'CRITICAL',
      urgencyScore: 88.00,
      difficultyScore: 92.00,
      weightScore: 90.00,
      effortScore: 85.00,
      workloadScore: 70.00,
      explanationText: 'Urgent major semester milestone due in under 24 hours with heavy 35% course weighting.',
    ),
    TaskModel(
      id: 3,
      title: 'Relational Normalization & SQL Triggers',
      description: 'Design BCNF database schema with complex validation triggers and stored procedures.',
      taskType: 'ASSIGNMENT',
      subject: initialSubjects[2],
      deadline: DateTime.now().add(const Duration(days: 2, hours: 4)),
      status: 'PENDING',
      difficulty: 3,
      academicWeight: 15.0,
      estimatedHours: 3.0,
      priorityScore: 58.40,
      priorityLevel: 'HIGH',
      urgencyScore: 62.00,
      difficultyScore: 60.00,
      weightScore: 55.00,
      effortScore: 50.00,
      workloadScore: 55.00,
      explanationText: 'Moderate urgency with upcoming 50-hour window and substantial SQL schema deliverables.',
    ),
    TaskModel(
      id: 4,
      title: 'Predicate Calculus Problem Set 4',
      description: 'Solve first-order predicate logic formal proof deductions and quantifiers.',
      taskType: 'ASSIGNMENT',
      subject: initialSubjects[3],
      deadline: DateTime.now().subtract(const Duration(hours: 3)),
      status: 'OVERDUE',
      difficulty: 3,
      academicWeight: 10.0,
      estimatedHours: 2.0,
      priorityScore: 78.00,
      priorityLevel: 'CRITICAL',
      urgencyScore: 100.00,
      difficultyScore: 60.00,
      weightScore: 45.00,
      effortScore: 40.00,
      workloadScore: 60.00,
      explanationText: 'Past deadline and marked OVERDUE. Requires immediate submission to avoid penalty points.',
    ),
    TaskModel(
      id: 5,
      title: 'Lexical Analyzer & Regex Parser',
      description: 'Write Flex specification for scanning C-like programming language tokens.',
      taskType: 'PROJECT',
      subject: initialSubjects[4],
      deadline: DateTime.now().add(const Duration(days: 5)),
      status: 'PENDING',
      difficulty: 4,
      academicWeight: 20.0,
      estimatedHours: 6.0,
      priorityScore: 44.80,
      priorityLevel: 'MEDIUM',
      urgencyScore: 35.00,
      difficultyScore: 80.00,
      weightScore: 70.00,
      effortScore: 65.00,
      workloadScore: 30.00,
      explanationText: 'High difficulty but ample 5-day lead time allows paced preparation.',
    ),
    TaskModel(
      id: 6,
      title: 'Thread Synchronization Mutex Lab',
      description: 'Solve producer-consumer and readers-writers synchronization problems using POSIX semaphores.',
      taskType: 'LAB',
      subject: initialSubjects[1],
      deadline: DateTime.now().subtract(const Duration(days: 2)),
      status: 'COMPLETED',
      difficulty: 4,
      academicWeight: 15.0,
      estimatedHours: 3.5,
      priorityScore: 0.0,
      priorityLevel: 'LOW',
      urgencyScore: 0.0,
      difficultyScore: 75.00,
      weightScore: 60.00,
      effortScore: 55.00,
      workloadScore: 0.0,
      explanationText: 'Completed deliverable.',
    ),
    TaskModel(
      id: 7,
      title: 'Divide & Conquer Recursion Quiz',
      description: 'Master theorem recurrence relations and quicksort median partitioning analysis.',
      taskType: 'QUIZ',
      subject: initialSubjects[0],
      deadline: DateTime.now().subtract(const Duration(days: 3)),
      status: 'COMPLETED',
      difficulty: 3,
      academicWeight: 10.0,
      estimatedHours: 2.0,
      priorityScore: 0.0,
      priorityLevel: 'LOW',
      urgencyScore: 0.0,
      difficultyScore: 50.00,
      weightScore: 40.00,
      effortScore: 30.00,
      workloadScore: 0.0,
      explanationText: 'Completed deliverable.',
    ),
  ];

  // Stage 9 Realistic Notifications
  static final List<NotificationModel> initialNotifications = [
    NotificationModel(
      id: 1,
      title: 'Urgent: Deadline in 6 Hours',
      message: 'Urgent: "Dynamic Programming & Graph Lab" is due in approximately 5 hours. Complete code review and benchmark plots.',
      notificationType: 'DEADLINE_6H',
      isRead: false,
      createdAt: DateTime.now().subtract(const Duration(minutes: 45)),
      taskId: 1,
    ),
    NotificationModel(
      id: 2,
      title: 'Task Overdue',
      message: '"Predicate Calculus Problem Set 4" passed its deadline 3 hours ago and remains incomplete. Submit immediately.',
      notificationType: 'OVERDUE',
      isRead: false,
      createdAt: DateTime.now().subtract(const Duration(hours: 3)),
      taskId: 4,
    ),
    NotificationModel(
      id: 3,
      title: 'Deadline in 24 Hours',
      message: '"Kernel Process Scheduler Simulation" is due in 22 hours. Review test suite and schedule your next focus session.',
      notificationType: 'DEADLINE_24H',
      isRead: false,
      createdAt: DateTime.now().subtract(const Duration(hours: 5)),
      taskId: 2,
    ),
    NotificationModel(
      id: 4,
      title: 'High-Priority Deliverable Alert',
      message: '"Dynamic Programming & Graph Lab" currently has an 88.50 CRITICAL priority score. Top recommendation.',
      notificationType: 'HIGH_PRIORITY',
      isRead: true,
      createdAt: DateTime.now().subtract(const Duration(hours: 12)),
      taskId: 1,
    ),
    NotificationModel(
      id: 5,
      title: 'Daily Academic Summary',
      message: 'You have 4 active deliverables, 1 overdue, and 2 critical priority tasks. Nearest deadline: 5 hours.',
      notificationType: 'DAILY_SUMMARY',
      isRead: true,
      createdAt: DateTime.now().subtract(const Duration(hours: 18)),
    ),
  ];

  // Stage 8 Focus Study Sessions
  static final List<StudySessionModel> initialStudySessions = [
    StudySessionModel(
      id: 1,
      taskId: 1,
      taskTitle: 'Dynamic Programming & Graph Lab',
      startTime: DateTime.now().subtract(const Duration(hours: 3)),
      endTime: DateTime.now().subtract(const Duration(hours: 2, minutes: 10)),
      durationMinutes: 50,
      isCompleted: true,
      notes: 'Completed Bellman-Ford implementation and initial vertex test cases.',
    ),
    StudySessionModel(
      id: 2,
      taskId: 2,
      taskTitle: 'Kernel Process Scheduler Simulation',
      startTime: DateTime.now().subtract(const Duration(hours: 6)),
      endTime: DateTime.now().subtract(const Duration(hours: 5, minutes: 15)),
      durationMinutes: 45,
      isCompleted: true,
      notes: 'Implemented priority queue data structures.',
    ),
    StudySessionModel(
      id: 3,
      taskId: 6,
      taskTitle: 'Thread Synchronization Mutex Lab',
      startTime: DateTime.now().subtract(const Duration(days: 2)),
      endTime: DateTime.now().subtract(const Duration(days: 2, hours: -1)),
      durationMinutes: 60,
      isCompleted: true,
      notes: 'Final POSIX semaphore verification.',
    ),
  ];

  // Stage 10 Analytics 7-Day Trend Points
  static final List<DailyAnalyticsPointModel> initialWeeklyPoints = [
    DailyAnalyticsPointModel(
      date: DateTime.now().subtract(const Duration(days: 6)),
      tasksCompleted: 1,
      tasksOverdue: 0,
      studyMinutes: 75,
      productivityScore: 78.50,
    ),
    DailyAnalyticsPointModel(
      date: DateTime.now().subtract(const Duration(days: 5)),
      tasksCompleted: 2,
      tasksOverdue: 0,
      studyMinutes: 120,
      productivityScore: 92.00,
    ),
    DailyAnalyticsPointModel(
      date: DateTime.now().subtract(const Duration(days: 4)),
      tasksCompleted: 0,
      tasksOverdue: 0,
      studyMinutes: 45,
      productivityScore: 55.00,
    ),
    DailyAnalyticsPointModel(
      date: DateTime.now().subtract(const Duration(days: 3)),
      tasksCompleted: 1,
      tasksOverdue: 1,
      studyMinutes: 60,
      productivityScore: 68.20,
    ),
    DailyAnalyticsPointModel(
      date: DateTime.now().subtract(const Duration(days: 2)),
      tasksCompleted: 2,
      tasksOverdue: 0,
      studyMinutes: 110,
      productivityScore: 89.40,
    ),
    DailyAnalyticsPointModel(
      date: DateTime.now().subtract(const Duration(days: 1)),
      tasksCompleted: 1,
      tasksOverdue: 0,
      studyMinutes: 90,
      productivityScore: 84.10,
    ),
    DailyAnalyticsPointModel(
      date: DateTime.now(),
      tasksCompleted: 1,
      tasksOverdue: 1,
      studyMinutes: 95,
      productivityScore: 76.80,
    ),
  ];

  // Stage 7 Grounded AI Recommendation
  static final AIRecommendationModel initialAIRecommendation = AIRecommendationModel(
    recommendedTask: initialTasks[0],
    recommendationHeadline: 'Focus on Dynamic Programming & Graph Lab Immediately',
    reasoning: 'This deliverable has the highest urgent priority score (88.50/100) and is due in only 5 hours. It carries a heavy 25% course weight. Completing the benchmark script now prevents deadline penalties.',
    suggestedAction: 'Start a 50-minute Pomodoro focus block to verify Bellman-Ford negative cycle tests.',
    recommendedFocusMinutes: 50,
    generatedAt: DateTime.now().subtract(const Duration(minutes: 15)),
  );

  // Stage 8 Generated AI Study Plan
  static final StudyPlanScheduleModel initialStudyPlan = StudyPlanScheduleModel(
    requestedHours: 3.5,
    totalStudyMinutes: 180,
    totalBreakMinutes: 30,
    summaryNotes: 'Optimized multi-task study schedule sequencing your highest urgency deliverables first, punctuated by mandatory cognitive recovery breaks.',
    blocks: [
      const StudyPlanBlockModel(
        taskId: 1,
        title: 'Dynamic Programming & Graph Lab (Block 1)',
        startTime: '14:00',
        endTime: '14:50',
        durationMinutes: 50,
        isBreak: false,
        reason: 'Most urgent deadline (due in 5h, 88.5 priority). Focus on shortest path core.',
      ),
      const StudyPlanBlockModel(
        title: 'Cognitive Reset Break',
        startTime: '14:50',
        endTime: '15:00',
        durationMinutes: 10,
        isBreak: true,
        reason: 'Hydrate, stretch, and let algorithms consolidate.',
      ),
      const StudyPlanBlockModel(
        taskId: 1,
        title: 'Graph Lab Benchmark & Submission Wrap-up',
        startTime: '15:00',
        endTime: '15:45',
        durationMinutes: 45,
        isBreak: false,
        reason: 'Finalize timing plots and upload deliverables.',
      ),
      const StudyPlanBlockModel(
        title: 'Active Rest Break',
        startTime: '15:45',
        endTime: '16:00',
        durationMinutes: 15,
        isBreak: true,
        reason: 'Step away from screen before switching cognitive contexts.',
      ),
      const StudyPlanBlockModel(
        taskId: 2,
        title: 'Kernel Scheduler Architecture & Context Switching',
        startTime: '16:00',
        endTime: '16:50',
        durationMinutes: 50,
        isBreak: false,
        reason: 'Second highest priority (82.2 score, due tomorrow). Implement queue state machine.',
      ),
      const StudyPlanBlockModel(
        title: 'Quick Refresh Break',
        startTime: '16:50',
        endTime: '16:55',
        durationMinutes: 5,
        isBreak: true,
        reason: 'Short pause before final review session.',
      ),
      const StudyPlanBlockModel(
        taskId: 4,
        title: 'Predicate Calculus Problem Set (Overdue Recovery)',
        startTime: '16:55',
        endTime: '17:30',
        durationMinutes: 35,
        isBreak: false,
        reason: 'Clear the overdue problem set to stop penalty points accumulation.',
      ),
    ],
  );
}
