/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState } from 'react';
import {
  Layers,
  Database,
  Server,
  Smartphone,
  Cpu,
  FolderTree,
  ListChecks,
  ShieldCheck,
  Sparkles,
  Calendar,
  Clock,
  TrendingUp,
  AlertTriangle,
  CheckCircle2,
  ChevronRight,
  BookOpen,
  ArrowRight,
  Calculator,
  Sliders,
  Terminal,
  FileCode2,
  BrainCircuit,
  Info,
  ExternalLink
} from 'lucide-react';

export default function App() {
  const [activeTab, setActiveTab] = useState<'overview' | 'structure' | 'database' | 'backend' | 'frontend' | 'api' | 'ai-engine' | 'roadmap'>('overview');

  // Interactive AI priority score calculator state to demonstrate Stage 1 formula
  const [calcUrgencyHours, setCalcUrgencyHours] = useState<number>(36);
  const [calcDifficulty, setCalcDifficulty] = useState<number>(4);
  const [calcAcademicWeight, setCalcAcademicWeight] = useState<number>(25);
  const [calcEstimatedHours, setCalcEstimatedHours] = useState<number>(4);
  const [calcWorkloadCount, setCalcWorkloadCount] = useState<number>(3);

  // Compute 5-factor priority formula:
  // U = max(0, min(100, 100 * (1 - hours / 168)))
  const uScore = Math.max(0, Math.min(100, 100 * (1 - calcUrgencyHours / 168)));
  // D = ((difficulty - 1) / 4) * 100
  const dScore = ((calcDifficulty - 1) / 4) * 100;
  // W = min(100, academicWeight * 2.5)
  const wScore = Math.min(100, calcAcademicWeight * 2.5);
  // E = min(100, (estimatedHours / 10) * 100)
  const eScore = Math.min(100, (calcEstimatedHours / 10) * 100);
  // L = min(100, workloadCount * 20)
  const lScore = Math.min(100, calcWorkloadCount * 20);

  const totalPriorityScore = Math.round((0.40 * uScore + 0.20 * dScore + 0.20 * wScore + 0.10 * eScore + 0.10 * lScore) * 10) / 10;

  const getPriorityTier = (score: number) => {
    if (score >= 76) return { label: 'CRITICAL', color: 'bg-red-500 text-white', border: 'border-red-500', text: 'text-red-600 dark:text-red-400' };
    if (score >= 51) return { label: 'HIGH', color: 'bg-orange-500 text-white', border: 'border-orange-500', text: 'text-orange-600 dark:text-orange-400' };
    if (score >= 31) return { label: 'MEDIUM', color: 'bg-yellow-500 text-slate-900', border: 'border-yellow-500', text: 'text-yellow-600 dark:text-yellow-400' };
    return { label: 'LOW', color: 'bg-emerald-500 text-white', border: 'border-emerald-500', text: 'text-emerald-600 dark:text-emerald-400' };
  };

  const tier = getPriorityTier(totalPriorityScore);

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 dark:bg-slate-950 dark:text-slate-100 flex flex-col font-sans">
      {/* Top Banner */}
      <header className="border-b border-slate-200 dark:border-slate-800 bg-white/80 dark:bg-slate-900/80 backdrop-blur sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 py-3.5 flex flex-col md:flex-row md:items-center justify-between gap-3">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-blue-700 via-blue-600 to-indigo-500 flex items-center justify-center shadow-lg shadow-blue-500/20 text-white">
              <BrainCircuit className="w-6 h-6" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h1 className="font-bold text-xl tracking-tight text-slate-900 dark:text-white">DEADLINEGUARD AI</h1>
                <span className="px-2 py-0.5 text-xs font-semibold rounded-full bg-blue-100 text-blue-800 dark:bg-blue-900/60 dark:text-blue-300 border border-blue-200 dark:border-blue-700">
                  STAGE 3: SPRING BOOT BACKEND
                </span>
              </div>
              <p className="text-xs text-slate-500 dark:text-slate-400">Intelligent Student Task & Deadline Management System</p>
            </div>
          </div>

          <div className="flex items-center gap-2 overflow-x-auto pb-1 md:pb-0">
            <span className="text-xs text-slate-500 dark:text-slate-400 font-medium mr-1">Status:</span>
            <div className="flex items-center gap-1.5 px-3 py-1 rounded-lg bg-emerald-50 dark:bg-emerald-950/60 border border-emerald-200 dark:border-emerald-800 text-xs text-emerald-700 dark:text-emerald-300 font-semibold">
              <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600 dark:text-emerald-400" />
              Stage 3 Complete (JPA Entities, Repositories, Config & Health API)
            </div>
          </div>
        </div>

        {/* Navigation Tabs */}
        <div className="max-w-7xl mx-auto px-4 sm:px-6 border-t border-slate-100 dark:border-slate-800/80">
          <nav className="flex space-x-1 sm:space-x-4 overflow-x-auto py-2 text-sm font-medium scrollbar-none">
            {[
              { id: 'overview', label: 'Overview', icon: Layers },
              { id: 'structure', label: 'Folder Structure', icon: FolderTree },
              { id: 'database', label: 'Database Entities', icon: Database },
              { id: 'backend', label: 'Backend Modules', icon: Server },
              { id: 'frontend', label: 'Flutter UI Screens', icon: Smartphone },
              { id: 'api', label: 'REST APIs', icon: FileCode2 },
              { id: 'ai-engine', label: 'AI Priority Engine', icon: Calculator },
              { id: 'roadmap', label: '13-Stage Roadmap', icon: ListChecks },
            ].map((tab) => {
              const Icon = tab.icon;
              const isActive = activeTab === tab.id;
              return (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id as any)}
                  className={`flex items-center gap-2 px-3 py-1.5 rounded-lg transition-all whitespace-nowrap text-xs sm:text-sm font-medium ${
                    isActive
                      ? 'bg-blue-600 text-white shadow-sm shadow-blue-500/30'
                      : 'text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800'
                  }`}
                >
                  <Icon className="w-4 h-4" />
                  {tab.label}
                </button>
              );
            })}
          </nav>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 py-6 sm:py-8 flex-1 w-full">
        {/* TAB 1: OVERVIEW */}
        {activeTab === 'overview' && (
          <div className="space-y-6">
            <div className="bg-gradient-to-br from-blue-900 via-indigo-950 to-slate-900 rounded-2xl p-6 sm:p-8 text-white shadow-xl relative overflow-hidden border border-blue-800/50">
              <div className="absolute right-0 top-0 translate-x-10 -translate-y-10 w-96 h-96 bg-blue-500/10 rounded-full blur-3xl pointer-events-none" />
              <div className="max-w-3xl space-y-4 relative z-10">
                <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-blue-500/20 border border-blue-400/30 text-blue-200 text-xs font-semibold">
                  <Sparkles className="w-3.5 h-3.5 text-blue-300" />
                  Next-Gen Academic Engineering Project
                </div>
                <h2 className="text-2xl sm:text-4xl font-extrabold tracking-tight">
                  Intelligent Student Task &amp; Deadline Management
                </h2>
                <p className="text-slate-300 text-sm sm:text-base leading-relaxed">
                  DeadlineGuard AI transforms academic task management from passive calendar reminders into an active, intelligent reasoning engine. Built specifically for undergraduate and postgraduate coursework with tight assignment deadlines, complex projects, and high-stakes exams.
                </p>
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 pt-2">
                  <div className="bg-white/10 rounded-xl p-3 border border-white/10 backdrop-blur-sm">
                    <p className="text-xs text-blue-200">Priority Engine</p>
                    <p className="text-lg font-bold text-white">5-Factor Score</p>
                    <p className="text-[11px] text-slate-300">Explainable 0–100</p>
                  </div>
                  <div className="bg-white/10 rounded-xl p-3 border border-white/10 backdrop-blur-sm">
                    <p className="text-xs text-blue-200">Backend Core</p>
                    <p className="text-lg font-bold text-white">Spring Boot 3</p>
                    <p className="text-[11px] text-slate-300">Java 21 + Security 6</p>
                  </div>
                  <div className="bg-white/10 rounded-xl p-3 border border-white/10 backdrop-blur-sm">
                    <p className="text-xs text-blue-200">Client Engine</p>
                    <p className="text-lg font-bold text-white">Flutter 3.x</p>
                    <p className="text-[11px] text-slate-300">Material 3 Cross-Platform</p>
                  </div>
                  <div className="bg-white/10 rounded-xl p-3 border border-white/10 backdrop-blur-sm">
                    <p className="text-xs text-blue-200">Database</p>
                    <p className="text-lg font-bold text-white">MySQL 8.0</p>
                    <p className="text-[11px] text-slate-300">8 Relational Tables</p>
                  </div>
                </div>
              </div>
            </div>

            {/* Core Pillars */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div className="bg-white dark:bg-slate-900 rounded-xl p-5 border border-slate-200 dark:border-slate-800 shadow-sm space-y-3">
                <div className="w-10 h-10 rounded-lg bg-blue-100 dark:bg-blue-900/50 flex items-center justify-center text-blue-600 dark:text-blue-400">
                  <Calculator className="w-5 h-5" />
                </div>
                <h3 className="font-bold text-base text-slate-900 dark:text-white">Explainable AI Prioritization</h3>
                <p className="text-xs text-slate-600 dark:text-slate-400 leading-relaxed">
                  Calculates a dynamic 0-100 score based on 40% deadline urgency, 20% difficulty, 20% academic weight, 10% estimated effort, and 10% student workload. Zero guesswork.
                </p>
              </div>

              <div className="bg-white dark:bg-slate-900 rounded-xl p-5 border border-slate-200 dark:border-slate-800 shadow-sm space-y-3">
                <div className="w-10 h-10 rounded-lg bg-indigo-100 dark:bg-indigo-900/50 flex items-center justify-center text-indigo-600 dark:text-indigo-400">
                  <BrainCircuit className="w-5 h-5" />
                </div>
                <h3 className="font-bold text-base text-slate-900 dark:text-white">"What Should I Do Now?"</h3>
                <p className="text-xs text-slate-600 dark:text-slate-400 leading-relaxed">
                  A high-priority one-click recommendation that sends verified academic state to Google Gemini, outputting concrete focus goals, estimated minutes, and deep reasoning.
                </p>
              </div>

              <div className="bg-white dark:bg-slate-900 rounded-xl p-5 border border-slate-200 dark:border-slate-800 shadow-sm space-y-3">
                <div className="w-10 h-10 rounded-lg bg-emerald-100 dark:bg-emerald-900/50 flex items-center justify-center text-emerald-600 dark:text-emerald-400">
                  <Clock className="w-5 h-5" />
                </div>
                <h3 className="font-bold text-base text-slate-900 dark:text-white">AI Study Schedule &amp; Focus</h3>
                <p className="text-xs text-slate-600 dark:text-slate-400 leading-relaxed">
                  Enter available hours, start time, and break intervals to receive a personalized Pomodoro-integrated study roadmap tailored to current pending exams and assignments.
                </p>
              </div>
            </div>

            {/* Quick Architecture Summary Cards */}
            <div className="bg-white dark:bg-slate-900 rounded-xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="font-bold text-base text-slate-900 dark:text-white">Documentation Deliverables</h3>
                  <p className="text-xs text-slate-500 dark:text-slate-400">All design documents have been initialized in the workspace repository</p>
                </div>
                <span className="text-xs bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300 px-3 py-1 rounded-full font-mono">
                  /docs/*.md
                </span>
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
                {[
                  { file: 'docs/architecture.md', title: 'System Architecture', desc: 'Layered diagrams, communication paths, and security architecture' },
                  { file: 'docs/database.md', title: 'Database Schema & ERD', desc: '8 normalized tables, FKs, constraints, indexes & data types' },
                  { file: 'docs/api.md', title: 'REST API Contracts', desc: 'Auth, Subjects, Tasks, Priority, AI, Study & Analytics endpoints' },
                  { file: 'docs/ai-engine.md', title: 'Priority Scoring & AI', desc: 'Normalized formulas, Gemini schemas & fallback engine' },
                ].map((doc, idx) => (
                  <div key={idx} className="p-3.5 rounded-lg border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-800/40 space-y-1">
                    <p className="text-xs font-mono text-blue-600 dark:text-blue-400">{doc.file}</p>
                    <p className="text-sm font-semibold text-slate-900 dark:text-white">{doc.title}</p>
                    <p className="text-xs text-slate-500 dark:text-slate-400">{doc.desc}</p>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* TAB 2: FOLDER STRUCTURE */}
        {activeTab === 'structure' && (
          <div className="space-y-6">
            <div className="bg-white dark:bg-slate-900 rounded-xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-slate-200 dark:border-slate-800 pb-4">
                <div>
                  <h3 className="font-bold text-lg text-slate-900 dark:text-white">Complete Project Architecture &amp; Folder Tree</h3>
                  <p className="text-xs text-slate-500 dark:text-slate-400">Strictly modular separation between Spring Boot backend, Flutter frontend, MySQL schemas, and documentation</p>
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-xs px-2.5 py-1 rounded bg-blue-50 dark:bg-blue-900/40 text-blue-600 dark:text-blue-300 font-medium">Clean Architecture</span>
                  <span className="text-xs px-2.5 py-1 rounded bg-indigo-50 dark:bg-indigo-900/40 text-indigo-600 dark:text-indigo-300 font-medium">Domain-Driven</span>
                </div>
              </div>

              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {/* Backend Tree */}
                <div className="space-y-3">
                  <div className="flex items-center gap-2 text-sm font-bold text-slate-800 dark:text-slate-200">
                    <Server className="w-4 h-4 text-blue-600" />
                    <span>backend/ (Java 21 Spring Boot 3.3.x)</span>
                  </div>
                  <pre className="p-4 rounded-xl bg-slate-900 text-slate-200 font-mono text-xs overflow-x-auto leading-relaxed border border-slate-800">
{`backend/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/deadlineguard/
    │   │   ├── DeadlineGuardApplication.java
    │   │   ├── config/
    │   │   │   ├── SecurityConfig.java
    │   │   │   ├── CorsConfig.java
    │   │   │   └── SwaggerConfig.java
    │   │   ├── controller/
    │   │   │   ├── AuthController.java
    │   │   │   ├── UserController.java
    │   │   │   ├── SubjectController.java
    │   │   │   ├── TaskController.java
    │   │   │   ├── PriorityController.java
    │   │   │   ├── StudySessionController.java
    │   │   │   ├── NotificationController.java
    │   │   │   ├── AnalyticsController.java
    │   │   │   └── AIController.java
    │   │   ├── service/
    │   │   │   ├── AuthService.java
    │   │   │   ├── UserService.java
    │   │   │   ├── SubjectService.java
    │   │   │   ├── TaskService.java
    │   │   │   ├── PriorityCalculationService.java
    │   │   │   ├── StudyPlanService.java
    │   │   │   ├── StudySessionService.java
    │   │   │   ├── NotificationService.java
    │   │   │   ├── AnalyticsService.java
    │   │   │   └── AIService.java
    │   │   ├── repository/
    │   │   │   ├── UserRepository.java
    │   │   │   ├── SubjectRepository.java
    │   │   │   ├── TaskRepository.java
    │   │   │   ├── TaskPriorityRepository.java
    │   │   │   ├── StudySessionRepository.java
    │   │   │   ├── NotificationRepository.java
    │   │   │   └── ProductivityStatsRepository.java
    │   │   ├── entity/
    │   │   ├── dto/
    │   │   ├── security/
    │   │   ├── ai/
    │   │   └── exception/
    │   └── resources/
    │       └── application.yml
    └── test/`}
                  </pre>
                </div>

                {/* Frontend Tree */}
                <div className="space-y-3">
                  <div className="flex items-center gap-2 text-sm font-bold text-slate-800 dark:text-slate-200">
                    <Smartphone className="w-4 h-4 text-indigo-600" />
                    <span>frontend/ (Flutter 3.x / Dart Material 3)</span>
                  </div>
                  <pre className="p-4 rounded-xl bg-slate-900 text-slate-200 font-mono text-xs overflow-x-auto leading-relaxed border border-slate-800">
{`frontend/
├── pubspec.yaml
└── lib/
    ├── main.dart
    ├── core/
    │   ├── constants/
    │   │   ├── app_colors.dart
    │   │   └── api_endpoints.dart
    │   ├── network/
    │   │   ├── api_client.dart
    │   │   └── auth_interceptor.dart
    │   └── theme/
    │       └── app_theme.dart
    ├── models/
    │   ├── user_model.dart
    │   ├── subject_model.dart
    │   ├── task_model.dart
    │   ├── priority_model.dart
    │   ├── study_session_model.dart
    │   ├── notification_model.dart
    │   └── analytics_model.dart
    ├── providers/
    │   ├── auth_provider.dart
    │   ├── task_provider.dart
    │   ├── subject_provider.dart
    │   ├── study_session_provider.dart
    │   └── notification_provider.dart
    ├── services/
    │   ├── api_service.dart
    │   ├── auth_service.dart
    │   ├── task_service.dart
    │   └── ai_service.dart
    └── ui/
        ├── screens/ (15 Screens)
        └── widgets/`}
                  </pre>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* TAB 3: DATABASE ENTITIES */}
        {activeTab === 'database' && (
          <div className="space-y-6">
            <div className="bg-white dark:bg-slate-900 rounded-xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-slate-200 dark:border-slate-800 pb-4">
                <div>
                  <h3 className="font-bold text-lg text-slate-900 dark:text-white">Database Entities (MySQL 8.0+)</h3>
                  <p className="text-xs text-slate-500 dark:text-slate-400">8 normalized tables connected via foreign keys with cascading deletions and indexed lookups</p>
                </div>
                <span className="text-xs font-mono bg-emerald-100 text-emerald-800 dark:bg-emerald-950 dark:text-emerald-300 px-3 py-1 rounded-full font-semibold">
                  InnoDB • UTF8MB4 • ACID
                </span>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                {[
                  {
                    name: 'users',
                    role: 'Student Authentication & Profile',
                    fields: ['id (BIGINT PK)', 'name', 'email (UNIQUE)', 'password_hash (BCrypt)', 'department', 'semester', 'college', 'created_at'],
                    tag: 'Primary Anchor'
                  },
                  {
                    name: 'subjects',
                    role: 'Course Catalog & Credit Tracking',
                    fields: ['id (BIGINT PK)', 'user_id (FK)', 'subject_name', 'subject_code', 'credits (1-10)', 'color_hex', 'created_at'],
                    tag: '1:N from users'
                  },
                  {
                    name: 'tasks',
                    role: 'Academic Tasks & Deadlines',
                    fields: ['id (BIGINT PK)', 'user_id (FK)', 'subject_id (FK)', 'title', 'task_type (ENUM)', 'deadline', 'difficulty (1-5)', 'academic_weight', 'status'],
                    tag: '1:N from subjects'
                  },
                  {
                    name: 'task_priorities',
                    role: 'Dynamic AI Calculation Results',
                    fields: ['id (BIGINT PK)', 'task_id (FK UNIQUE)', 'priority_score (0-100)', 'priority_level (ENUM)', 'urgency_score', 'difficulty_score', 'weight_score', 'explanation'],
                    tag: '1:1 with tasks'
                  },
                  {
                    name: 'study_sessions',
                    role: 'Focus Tracker & Study Time Logs',
                    fields: ['id (BIGINT PK)', 'user_id (FK)', 'task_id (FK opt)', 'start_time', 'end_time', 'duration_minutes', 'is_completed'],
                    tag: 'Pomodoro Logs'
                  },
                  {
                    name: 'notifications',
                    role: 'Automated 48h/24h/6h Urgency Alerts',
                    fields: ['id (BIGINT PK)', 'user_id (FK)', 'task_id (FK)', 'title', 'message', 'notification_type (ENUM)', 'is_read', 'created_at'],
                    tag: 'Urgency Engine'
                  },
                  {
                    name: 'ai_recommendations',
                    role: 'Gemini Contextual Advice History',
                    fields: ['id (BIGINT PK)', 'user_id (FK)', 'task_id (FK)', 'recommendation_type', 'recommendation_text', 'ai_reasoning', 'suggested_action'],
                    tag: 'AI History'
                  },
                  {
                    name: 'productivity_stats',
                    role: 'Daily Aggregate Performance Metrics',
                    fields: ['id (BIGINT PK)', 'user_id (FK)', 'stat_date (DATE)', 'tasks_completed', 'tasks_overdue', 'study_minutes', 'productivity_score'],
                    tag: 'Analytics Aggregates'
                  },
                ].map((table, i) => (
                  <div key={i} className="rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-800/50 p-4 space-y-2.5">
                    <div className="flex items-center justify-between">
                      <span className="font-mono text-xs font-bold text-blue-600 dark:text-blue-400 bg-blue-50 dark:bg-blue-900/30 px-2 py-0.5 rounded">
                        {table.name}
                      </span>
                      <span className="text-[10px] text-slate-500 font-medium">{table.tag}</span>
                    </div>
                    <p className="text-xs text-slate-600 dark:text-slate-300 font-medium">{table.role}</p>
                    <div className="border-t border-slate-200 dark:border-slate-700/60 pt-2 space-y-1">
                      {table.fields.map((f, fi) => (
                        <p key={fi} className="text-[11px] font-mono text-slate-500 dark:text-slate-400 truncate">
                          • {f}
                        </p>
                      ))}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* TAB 4: BACKEND MODULES & STAGE 3 SCAFFOLDING */}
        {activeTab === 'backend' && (
          <div className="space-y-6">
            {/* Stage 3 Status Header */}
            <div className="bg-white dark:bg-slate-900 rounded-xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-slate-200 dark:border-slate-800 pb-4">
                <div>
                  <div className="flex items-center gap-2">
                    <h3 className="font-bold text-lg text-slate-900 dark:text-white">Spring Boot 3.3.x Backend Scaffolding</h3>
                    <span className="text-xs font-mono font-semibold px-2 py-0.5 rounded bg-emerald-100 text-emerald-800 dark:bg-emerald-950 dark:text-emerald-300 border border-emerald-300 dark:border-emerald-700">
                      STAGE 3 COMPLETE
                    </span>
                  </div>
                  <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
                    Java 21 • Spring Data JPA • Hibernate 6 • MySQL 8+ • HikariCP • Bean Validation • Lombok • Layered Architecture
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-xs font-mono bg-blue-50 dark:bg-blue-950 text-blue-700 dark:text-blue-300 px-3 py-1.5 rounded-lg border border-blue-200 dark:border-blue-800">
                    com.deadlineguard
                  </span>
                </div>
              </div>

              {/* Scaffolding Quick Stats */}
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                <div className="p-3 rounded-lg bg-slate-50 dark:bg-slate-800/50 border border-slate-200 dark:border-slate-700/60">
                  <p className="text-[11px] text-slate-500 dark:text-slate-400 font-medium">JPA Entities</p>
                  <p className="text-xl font-bold text-blue-600 dark:text-blue-400 font-mono">8 Entities</p>
                  <p className="text-[10px] text-slate-400">100% Frozen Stage 2 Schema</p>
                </div>
                <div className="p-3 rounded-lg bg-slate-50 dark:bg-slate-800/50 border border-slate-200 dark:border-slate-700/60">
                  <p className="text-[11px] text-slate-500 dark:text-slate-400 font-medium">Spring Data Repositories</p>
                  <p className="text-xl font-bold text-indigo-600 dark:text-indigo-400 font-mono">8 Interfaces</p>
                  <p className="text-[10px] text-slate-400">Derived &amp; JPQL Queries</p>
                </div>
                <div className="p-3 rounded-lg bg-slate-50 dark:bg-slate-800/50 border border-slate-200 dark:border-slate-700/60">
                  <p className="text-[11px] text-slate-500 dark:text-slate-400 font-medium">Configuration Profiles</p>
                  <p className="text-xl font-bold text-purple-600 dark:text-purple-400 font-mono">dev / prod / test</p>
                  <p className="text-[10px] text-slate-400">HikariCP + Dialect Tuning</p>
                </div>
                <div className="p-3 rounded-lg bg-slate-50 dark:bg-slate-800/50 border border-slate-200 dark:border-slate-700/60">
                  <p className="text-[11px] text-slate-500 dark:text-slate-400 font-medium">Exception Interceptor</p>
                  <p className="text-xl font-bold text-emerald-600 dark:text-emerald-400 font-mono">Global Advice</p>
                  <p className="text-[10px] text-slate-400">Uniform ApiResponse&lt;T&gt;</p>
                </div>
              </div>
            </div>

            {/* 8 JPA Entities Card Grid */}
            <div className="bg-white dark:bg-slate-900 rounded-xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <h4 className="font-bold text-base text-slate-900 dark:text-white flex items-center gap-2">
                    <Database className="w-4 h-4 text-blue-600" />
                    JPA Entities (com.deadlineguard.entity)
                  </h4>
                  <p className="text-xs text-slate-500 dark:text-slate-400">All 8 entities mapped to frozen MySQL tables with validations, relationships, and lifecycle hooks</p>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                {[
                  {
                    clazz: 'User.java',
                    table: 'users',
                    id: 'Long id (IDENTITY)',
                    highlights: ['@NotBlank name, email (UQ)', '@JsonIgnore passwordHash', 'semester 1-12 validation', '@OneToMany subjects, tasks'],
                    status: 'Ready'
                  },
                  {
                    clazz: 'Subject.java',
                    table: 'subjects',
                    id: 'Long id (IDENTITY)',
                    highlights: ['@ManyToOne User', 'UQ (user_id, subject_code)', 'credits 1-10, colorHex regex', 'Cascade safe unlinking'],
                    status: 'Ready'
                  },
                  {
                    clazz: 'Task.java',
                    table: 'tasks',
                    id: 'Long id (IDENTITY)',
                    highlights: ['@ManyToOne User (CASCADE)', '@ManyToOne Subject (NULLABLE)', 'TaskType & TaskStatus enums', '@OneToOne TaskPriority'],
                    status: 'Ready'
                  },
                  {
                    clazz: 'TaskPriority.java',
                    table: 'task_priorities',
                    id: 'Long id (IDENTITY)',
                    highlights: ['@OneToOne Task (UQ)', '5 component subscores (0-100)', 'PriorityLevel enum (4 tiers)', 'recalculateComposite() helper'],
                    status: 'Ready'
                  },
                  {
                    clazz: 'StudySession.java',
                    table: 'study_sessions',
                    id: 'Long id (IDENTITY)',
                    highlights: ['@ManyToOne User', '@ManyToOne Task (opt)', 'startTime, endTime, duration', 'Focus mode timer logging'],
                    status: 'Ready'
                  },
                  {
                    clazz: 'Notification.java',
                    table: 'notifications',
                    id: 'Long id (IDENTITY)',
                    highlights: ['@ManyToOne User', '@ManyToOne Task (opt)', 'NotificationType (48h/24h/6h)', 'isRead boolean flag'],
                    status: 'Ready'
                  },
                  {
                    clazz: 'AIRecommendation.java',
                    table: 'ai_recommendations',
                    id: 'Long id (IDENTITY)',
                    highlights: ['@ManyToOne User', 'recommendationType (50)', 'aiReasoning TEXT column', 'suggestedAction VARCHAR(255)'],
                    status: 'Ready'
                  },
                  {
                    clazz: 'ProductivityStat.java',
                    table: 'productivity_stats',
                    id: 'Long id (IDENTITY)',
                    highlights: ['UQ (user_id, stat_date)', 'tasksCompleted & overdue count', 'daily productivityScore (5,2)', 'Analytics charts aggregator'],
                    status: 'Ready'
                  },
                ].map((ent, idx) => (
                  <div key={idx} className="rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50/70 dark:bg-slate-800/40 p-3.5 space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="font-mono text-xs font-bold text-blue-700 dark:text-blue-300 bg-blue-100/70 dark:bg-blue-900/40 px-2 py-0.5 rounded">
                        {ent.clazz}
                      </span>
                      <span className="text-[10px] font-mono text-slate-500">→ {ent.table}</span>
                    </div>
                    <p className="text-[11px] font-mono text-slate-600 dark:text-slate-300 font-semibold">{ent.id}</p>
                    <ul className="text-[11px] text-slate-500 dark:text-slate-400 space-y-1">
                      {ent.highlights.map((h, hi) => (
                        <li key={hi} className="truncate">• {h}</li>
                      ))}
                    </ul>
                  </div>
                ))}
              </div>
            </div>

            {/* 8 Spring Data Repositories */}
            <div className="bg-white dark:bg-slate-900 rounded-xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4">
              <div>
                <h4 className="font-bold text-base text-slate-900 dark:text-white flex items-center gap-2">
                  <Server className="w-4 h-4 text-indigo-600" />
                  Spring Data JPA Repositories (com.deadlineguard.repository)
                </h4>
                <p className="text-xs text-slate-500 dark:text-slate-400">Extending JpaRepository with custom derived filters and JPQL queries</p>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-3 font-mono text-xs">
                <div className="p-3 rounded-lg border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-800/40 space-y-1">
                  <p className="font-bold text-blue-600 dark:text-blue-400">UserRepository</p>
                  <p className="text-slate-600 dark:text-slate-300">• findByEmail(String email)</p>
                  <p className="text-slate-600 dark:text-slate-300">• existsByEmail(String email)</p>
                </div>
                <div className="p-3 rounded-lg border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-800/40 space-y-1">
                  <p className="font-bold text-blue-600 dark:text-blue-400">SubjectRepository</p>
                  <p className="text-slate-600 dark:text-slate-300">• findByUserId(Long userId)</p>
                  <p className="text-slate-600 dark:text-slate-300">• findByUserIdAndSubjectCode(Long userId, String code)</p>
                </div>
                <div className="p-3 rounded-lg border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-800/40 space-y-1">
                  <p className="font-bold text-blue-600 dark:text-blue-400">TaskRepository</p>
                  <p className="text-slate-600 dark:text-slate-300">• findByUserIdOrderByDeadlineAsc(Long userId)</p>
                  <p className="text-slate-600 dark:text-slate-300">• countConcurrentWorkloadTasks(...) [JPQL 72h window]</p>
                </div>
                <div className="p-3 rounded-lg border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-800/40 space-y-1">
                  <p className="font-bold text-blue-600 dark:text-blue-400">TaskPriorityRepository</p>
                  <p className="text-slate-600 dark:text-slate-300">• findByTaskId(Long taskId)</p>
                  <p className="text-slate-600 dark:text-slate-300">• findByTaskUserIdOrderByPriorityScoreDesc(Long userId)</p>
                </div>
                <div className="p-3 rounded-lg border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-800/40 space-y-1">
                  <p className="font-bold text-blue-600 dark:text-blue-400">StudySessionRepository</p>
                  <p className="text-slate-600 dark:text-slate-300">• findByUserIdOrderByStartTimeDesc(Long userId)</p>
                  <p className="text-slate-600 dark:text-slate-300">• sumDurationMinutesForPeriod(...) [JPQL aggregation]</p>
                </div>
                <div className="p-3 rounded-lg border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-800/40 space-y-1">
                  <p className="font-bold text-blue-600 dark:text-blue-400">NotificationRepository</p>
                  <p className="text-slate-600 dark:text-slate-300">• findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId)</p>
                  <p className="text-slate-600 dark:text-slate-300">• markAllAsReadForUser(Long userId) [@Modifying]</p>
                </div>
                <div className="p-3 rounded-lg border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-800/40 space-y-1">
                  <p className="font-bold text-blue-600 dark:text-blue-400">AIRecommendationRepository</p>
                  <p className="text-slate-600 dark:text-slate-300">• findTop5ByUserIdOrderByGeneratedAtDesc(Long userId)</p>
                  <p className="text-slate-600 dark:text-slate-300">• findByTaskIdOrderByGeneratedAtDesc(Long taskId)</p>
                </div>
                <div className="p-3 rounded-lg border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-800/40 space-y-1">
                  <p className="font-bold text-blue-600 dark:text-blue-400">ProductivityStatRepository</p>
                  <p className="text-slate-600 dark:text-slate-300">• findByUserIdAndStatDate(Long userId, LocalDate date)</p>
                  <p className="text-slate-600 dark:text-slate-300">• findByUserIdAndStatDateBetweenOrderByStatDateAsc(...)</p>
                </div>
              </div>
            </div>

            {/* REST API & Health Check Simulator */}
            <div className="bg-white dark:bg-slate-900 rounded-xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-slate-200 dark:border-slate-800 pb-3">
                <div>
                  <h4 className="font-bold text-base text-slate-900 dark:text-white flex items-center gap-2">
                    <Terminal className="w-4 h-4 text-emerald-600" />
                    Live Health API Spec (GET /api/v1/health &amp; /api/v1/status)
                  </h4>
                  <p className="text-xs text-slate-500 dark:text-slate-400">Standardized ApiResponse&lt;T&gt; envelope with timestamp and database connection status</p>
                </div>
                <span className="text-xs font-mono font-semibold px-2.5 py-1 rounded bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300">
                  HTTP 200 OK
                </span>
              </div>

              <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <span className="text-xs font-semibold text-slate-700 dark:text-slate-300 font-mono">GET /api/v1/health</span>
                  <pre className="p-3 rounded-lg bg-slate-950 text-slate-200 font-mono text-[11px] overflow-x-auto border border-slate-800 leading-relaxed">
{`{
  "success": true,
  "message": "DeadlineGuard API is running healthy",
  "data": {
    "status": "UP",
    "service": "deadlineguard-backend",
    "timestamp": "2026-09-26T10:35:00",
    "database": "CONNECTED",
    "databaseProduct": "MySQL",
    "databaseVersion": "8.0.36"
  },
  "timestamp": "2026-09-26T10:35:00"
}`}
                  </pre>
                </div>

                <div className="space-y-2">
                  <span className="text-xs font-semibold text-slate-700 dark:text-slate-300 font-mono">GET /api/v1/status</span>
                  <pre className="p-3 rounded-lg bg-slate-950 text-slate-200 font-mono text-[11px] overflow-x-auto border border-slate-800 leading-relaxed">
{`{
  "success": true,
  "message": "DeadlineGuard system metadata",
  "data": {
    "application": "DeadlineGuard AI",
    "version": "1.0.0-SNAPSHOT",
    "javaVersion": "21",
    "springBootVersion": "3.3.4",
    "activeProfiles": ["dev"],
    "stage": "STAGE 3 - Spring Boot Scaffolding & Configuration",
    "entitiesConfigured": 8,
    "repositoriesConfigured": 8
  },
  "timestamp": "2026-09-26T10:35:00"
}`}
                  </pre>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* TAB 5: FLUTTER UI SCREENS */}
        {activeTab === 'frontend' && (
          <div className="space-y-6">
            <div className="bg-white dark:bg-slate-900 rounded-xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-slate-200 dark:border-slate-800 pb-4">
                <div>
                  <h3 className="font-bold text-lg text-slate-900 dark:text-white">Flutter Frontend Screen Architecture</h3>
                  <p className="text-xs text-slate-500 dark:text-slate-400">15 screens structured for Material 3 with BottomNavigationBar &amp; student productivity workflow</p>
                </div>
                <span className="text-xs font-semibold px-2.5 py-1 rounded bg-blue-100 text-blue-700 dark:bg-blue-900/50 dark:text-blue-300">
                  Material 3 • Light &amp; Dark Theme
                </span>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                {[
                  { num: '01', title: 'Splash Screen', desc: 'App initialization, JWT verification & auto-route logic' },
                  { num: '02', title: 'Login Screen', desc: 'Student email/password authentication with validation' },
                  { num: '03', title: 'Register Screen', desc: 'Profile onboarding (name, college, branch, semester)' },
                  { num: '04', title: 'Home Dashboard', desc: 'Greeting, urgency highlights, completion % & "What to do" widget' },
                  { num: '05', title: 'Task List Screen', desc: 'Filter by pending/completed, sort by priority, category chips' },
                  { num: '06', title: 'Add Task Screen', desc: 'Task form with difficulty picker (1-5), academic weight & deadline' },
                  { num: '07', title: 'Task Details Screen', desc: 'Priority breakdown graph, AI explanation card, mark complete' },
                  { num: '08', title: 'Subject Management', desc: 'Course catalog, credit counters, and custom hex color badges' },
                  { num: '09', title: 'AI Recommendations', desc: 'One-click "What Should I Do Now?" with deep Gemini reasoning' },
                  { num: '10', title: 'AI Study Planner', desc: 'Interactive planner: input available hours & get hourly schedule' },
                  { num: '11', title: 'Focus / Study Mode', desc: 'Pomodoro timer with start/pause/stop and session logging' },
                  { num: '12', title: 'Notifications Screen', desc: '48h/24h/6h deadline reminders and overdue warnings' },
                  { num: '13', title: 'Productivity Analytics', desc: 'Weekly task throughput, subject workload distribution charts' },
                  { num: '14', title: 'Student Profile', desc: 'Academic details, semester level, overall study hour tally' },
                  { num: '15', title: 'Settings Screen', desc: 'Theme switch (light/dark), notification preferences, logout' },
                ].map((s, idx) => (
                  <div key={idx} className="p-3.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-800/40 flex items-start gap-3">
                    <span className="text-xs font-mono font-bold text-blue-600 dark:text-blue-400 bg-blue-100 dark:bg-blue-900/60 w-7 h-7 rounded-lg flex items-center justify-center shrink-0">
                      {s.num}
                    </span>
                    <div>
                      <h4 className="text-sm font-semibold text-slate-900 dark:text-white">{s.title}</h4>
                      <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">{s.desc}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* TAB 6: REST APIS */}
        {activeTab === 'api' && (
          <div className="space-y-6">
            <div className="bg-white dark:bg-slate-900 rounded-xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-slate-200 dark:border-slate-800 pb-4">
                <div>
                  <h3 className="font-bold text-lg text-slate-900 dark:text-white">REST API Modules &amp; Endpoints</h3>
                  <p className="text-xs text-slate-500 dark:text-slate-400">All responses wrapped in standard RFC envelope with ISO-8601 timestamps</p>
                </div>
                <span className="text-xs font-mono bg-blue-100 text-blue-800 dark:bg-blue-900/50 dark:text-blue-300 px-3 py-1 rounded-full">
                  Bearer Token Auth
                </span>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {[
                  {
                    module: 'Authentication & Profile',
                    endpoints: [
                      { m: 'POST', p: '/api/auth/register', desc: 'Create student account & return JWT' },
                      { m: 'POST', p: '/api/auth/login', desc: 'Authenticate credentials & return JWT' },
                      { m: 'GET', p: '/api/users/me', desc: 'Get logged-in student profile' },
                      { m: 'PUT', p: '/api/users/me', desc: 'Update academic profile details' },
                    ]
                  },
                  {
                    module: 'Subject Management',
                    endpoints: [
                      { m: 'POST', p: '/api/subjects', desc: 'Create course subject with credits' },
                      { m: 'GET', p: '/api/subjects', desc: 'List subjects with task metrics' },
                      { m: 'PUT', p: '/api/subjects/{id}', desc: 'Edit subject details' },
                      { m: 'DELETE', p: '/api/subjects/{id}', desc: 'Delete subject and cascade tasks' },
                    ]
                  },
                  {
                    module: 'Task & Priority Engine',
                    endpoints: [
                      { m: 'POST', p: '/api/tasks', desc: 'Create task + calculate 5-factor priority' },
                      { m: 'GET', p: '/api/tasks', desc: 'List tasks with status & category filter' },
                      { m: 'GET', p: '/api/tasks/prioritized', desc: 'List active tasks sorted by priority score' },
                      { m: 'PATCH', p: '/api/tasks/{id}/complete', desc: 'Mark complete & rebalance load' },
                    ]
                  },
                  {
                    module: 'AI Reasoning & Study Planner',
                    endpoints: [
                      { m: 'POST', p: '/api/ai/what-should-i-do', desc: 'Recommend next task with Gemini reasoning' },
                      { m: 'POST', p: '/api/ai/generate-study-plan', desc: 'Generate hourly schedule for available hours' },
                      { m: 'POST', p: '/api/study-sessions/start', desc: 'Start focus timer on a task' },
                      { m: 'GET', p: '/api/analytics/dashboard', desc: 'Weekly completion %, overdue & load' },
                    ]
                  },
                ].map((group, idx) => (
                  <div key={idx} className="p-4 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-800/40 space-y-3">
                    <h4 className="text-sm font-bold text-slate-900 dark:text-white flex items-center gap-2">
                      <FileCode2 className="w-4 h-4 text-blue-600 dark:text-blue-400" />
                      {group.module}
                    </h4>
                    <div className="space-y-2">
                      {group.endpoints.map((ep, epi) => (
                        <div key={epi} className="flex items-center justify-between p-2 rounded-lg bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-xs">
                          <div className="flex items-center gap-2 font-mono">
                            <span className={`px-1.5 py-0.5 rounded text-[10px] font-bold ${
                              ep.m === 'POST' ? 'bg-emerald-100 text-emerald-800 dark:bg-emerald-950 dark:text-emerald-300' :
                              ep.m === 'GET' ? 'bg-blue-100 text-blue-800 dark:bg-blue-950 dark:text-blue-300' :
                              ep.m === 'PUT' ? 'bg-amber-100 text-amber-800 dark:bg-amber-950 dark:text-amber-300' :
                              ep.m === 'PATCH' ? 'bg-purple-100 text-purple-800 dark:bg-purple-950 dark:text-purple-300' :
                              'bg-red-100 text-red-800 dark:bg-red-950 dark:text-red-300'
                            }`}>
                              {ep.m}
                            </span>
                            <span className="text-slate-800 dark:text-slate-200">{ep.p}</span>
                          </div>
                          <span className="text-slate-500 text-[11px] hidden sm:inline">{ep.desc}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* TAB 7: AI PRIORITY ENGINE (INTERACTIVE SIMULATOR) */}
        {activeTab === 'ai-engine' && (
          <div className="space-y-6">
            <div className="bg-white dark:bg-slate-900 rounded-xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm space-y-6">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-slate-200 dark:border-slate-800 pb-4">
                <div>
                  <h3 className="font-bold text-lg text-slate-900 dark:text-white">AI Priority Engine &amp; Mathematical Model</h3>
                  <p className="text-xs text-slate-500 dark:text-slate-400">Interactive simulation of the PriorityCalculationService 5-component weighted algorithm</p>
                </div>
                <div className="px-3 py-1 rounded-full bg-blue-50 dark:bg-blue-900/40 text-blue-600 dark:text-blue-300 text-xs font-mono font-semibold">
                  Score = 0.40U + 0.20D + 0.20W + 0.10E + 0.10L
                </div>
              </div>

              {/* Live Calculator */}
              <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* Inputs */}
                <div className="lg:col-span-2 space-y-4 bg-slate-50 dark:bg-slate-800/40 p-5 rounded-xl border border-slate-200 dark:border-slate-800">
                  <h4 className="text-sm font-bold text-slate-900 dark:text-white flex items-center gap-2">
                    <Sliders className="w-4 h-4 text-blue-600" />
                    Simulate Task Parameters
                  </h4>

                  <div className="space-y-3">
                    <div>
                      <div className="flex justify-between text-xs font-medium mb-1">
                        <span>Deadline Urgency (Hours until due: {calcUrgencyHours}h)</span>
                        <span className="font-mono text-blue-600 dark:text-blue-400">40% Weight (Subscore: {Math.round(uScore)})</span>
                      </div>
                      <input
                        type="range"
                        min="2"
                        max="168"
                        value={calcUrgencyHours}
                        onChange={(e) => setCalcUrgencyHours(Number(e.target.value))}
                        className="w-full accent-blue-600 cursor-pointer"
                      />
                      <div className="flex justify-between text-[10px] text-slate-500">
                        <span>2h (Immediate Danger)</span>
                        <span>72h (3 Days)</span>
                        <span>168h (7 Days)</span>
                      </div>
                    </div>

                    <div>
                      <div className="flex justify-between text-xs font-medium mb-1">
                        <span>Difficulty Level: {calcDifficulty}/5</span>
                        <span className="font-mono text-blue-600 dark:text-blue-400">20% Weight (Subscore: {Math.round(dScore)})</span>
                      </div>
                      <input
                        type="range"
                        min="1"
                        max="5"
                        step="1"
                        value={calcDifficulty}
                        onChange={(e) => setCalcDifficulty(Number(e.target.value))}
                        className="w-full accent-blue-600 cursor-pointer"
                      />
                      <div className="flex justify-between text-[10px] text-slate-500">
                        <span>1 = Very Easy</span>
                        <span>3 = Moderate</span>
                        <span>5 = Very Difficult</span>
                      </div>
                    </div>

                    <div>
                      <div className="flex justify-between text-xs font-medium mb-1">
                        <span>Academic Weight: {calcAcademicWeight}% of Course Grade</span>
                        <span className="font-mono text-blue-600 dark:text-blue-400">20% Weight (Subscore: {Math.round(wScore)})</span>
                      </div>
                      <input
                        type="range"
                        min="5"
                        max="50"
                        step="1"
                        value={calcAcademicWeight}
                        onChange={(e) => setCalcAcademicWeight(Number(e.target.value))}
                        className="w-full accent-blue-600 cursor-pointer"
                      />
                      <div className="flex justify-between text-[10px] text-slate-500">
                        <span>5% (Quiz)</span>
                        <span>25% (Major Project)</span>
                        <span>50% (Final Exam)</span>
                      </div>
                    </div>

                    <div>
                      <div className="flex justify-between text-xs font-medium mb-1">
                        <span>Estimated Effort: {calcEstimatedHours} Hours</span>
                        <span className="font-mono text-blue-600 dark:text-blue-400">10% Weight (Subscore: {Math.round(eScore)})</span>
                      </div>
                      <input
                        type="range"
                        min="0.5"
                        max="12"
                        step="0.5"
                        value={calcEstimatedHours}
                        onChange={(e) => setCalcEstimatedHours(Number(e.target.value))}
                        className="w-full accent-blue-600 cursor-pointer"
                      />
                    </div>

                    <div>
                      <div className="flex justify-between text-xs font-medium mb-1">
                        <span>Simultaneous Deadlines within 72h: {calcWorkloadCount} tasks</span>
                        <span className="font-mono text-blue-600 dark:text-blue-400">10% Weight (Subscore: {Math.round(lScore)})</span>
                      </div>
                      <input
                        type="range"
                        min="0"
                        max="6"
                        step="1"
                        value={calcWorkloadCount}
                        onChange={(e) => setCalcWorkloadCount(Number(e.target.value))}
                        className="w-full accent-blue-600 cursor-pointer"
                      />
                    </div>
                  </div>
                </div>

                {/* Score Output Card */}
                <div className="flex flex-col justify-between bg-gradient-to-br from-slate-900 to-slate-950 text-white p-6 rounded-xl border border-slate-800 space-y-4">
                  <div className="space-y-2">
                    <p className="text-xs uppercase tracking-wider text-slate-400 font-semibold">Priority Calculation Result</p>
                    <div className="flex items-baseline gap-3">
                      <span className="text-5xl font-black tracking-tight text-white">{totalPriorityScore}</span>
                      <span className="text-slate-400 text-sm font-medium">/ 100</span>
                    </div>

                    <div className="pt-2">
                      <span className={`inline-block px-3 py-1 rounded-md text-xs font-bold ${tier.color}`}>
                        {tier.label} PRIORITY
                      </span>
                    </div>
                  </div>

                  <div className="border-t border-slate-800 pt-3 space-y-1.5 text-xs text-slate-300">
                    <p className="font-semibold text-slate-100 flex items-center gap-1.5">
                      <BrainCircuit className="w-3.5 h-3.5 text-blue-400" />
                      Generated Explainable AI Reason:
                    </p>
                    <p className="text-slate-300 text-xs leading-relaxed italic bg-white/5 p-2.5 rounded-lg border border-white/10">
                      &quot;This task is rated as <strong>{tier.label}</strong> because it is due in <strong>{calcUrgencyHours} hours</strong>, has a difficulty rating of <strong>{calcDifficulty}/5</strong>, carries <strong>{calcAcademicWeight}%</strong> academic weight, and coincides with <strong>{calcWorkloadCount}</strong> other approaching deadlines.&quot;
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* TAB 8: 13-STAGE ROADMAP */}
        {activeTab === 'roadmap' && (
          <div className="space-y-6">
            <div className="bg-white dark:bg-slate-900 rounded-xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-slate-200 dark:border-slate-800 pb-4">
                <div>
                  <h3 className="font-bold text-lg text-slate-900 dark:text-white">13-Stage Implementation Sequence</h3>
                  <p className="text-xs text-slate-500 dark:text-slate-400">Step-by-step phased execution with testing validation at every milestone</p>
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-xs font-semibold px-2.5 py-1 rounded bg-emerald-100 text-emerald-800 dark:bg-emerald-950 dark:text-emerald-300">
                    Stage 3 Complete (JPA & Scaffolding)
                  </span>
                </div>
              </div>

              <div className="space-y-2.5">
                {[
                  { stage: 1, name: 'Project Architecture & Scaffolding', status: 'COMPLETED', desc: 'Architecture specs, tech stack justification, ERD models, REST endpoints, priority algorithms & directory setup.' },
                  { stage: 2, name: 'MySQL Database Schema & Seed Data', status: 'COMPLETED', desc: 'Created database/schema.sql and seed_data.sql with foreign keys, indexes, cascades & test student datasets.' },
                  { stage: 3, name: 'Spring Boot Backend Scaffolding', status: 'COMPLETED', desc: 'Maven pom.xml, application.yml, 8 JPA entities, 8 repositories, HikariCP pool, exception handling & Health API.' },
                  { stage: 4, name: 'Authentication & Security (JWT + BCrypt)', status: 'READY NEXT', desc: 'AuthController, AuthService, JwtTokenProvider, SecurityFilterChain, and registration/login flows.' },
                  { stage: 5, name: 'Subject & Task Management CRUD APIs', status: 'UPCOMING', desc: 'SubjectController, TaskController, validation, filtering, status lifecycle & JPQL repositories.' },
                  { stage: 6, name: 'AI Priority Engine (PriorityCalculationService)', status: 'UPCOMING', desc: '5-factor weighted algorithm implementation with explainability generators and auto-recalculation.' },
                  { stage: 7, name: 'Google Gemini AI Integration', status: 'UPCOMING', desc: 'AIService, grounded prompts for "What Should I Do Now?", structured JSON parsing & graceful local fallbacks.' },
                  { stage: 8, name: 'AI Study Planner & Focus Timer', status: 'UPCOMING', desc: 'StudyPlanService, available-hours breakdown, breaks, and study session tracking with start/pause/stop.' },
                  { stage: 9, name: 'Smart Deadline Reminder System', status: 'UPCOMING', desc: 'NotificationService, 48h/24h/6h urgency triggers, overdue notification jobs, and unread counters.' },
                  { stage: 10, name: 'Productivity Analytics & Workload Metrics', status: 'UPCOMING', desc: 'AnalyticsService, completion ratios, weekly throughput, subject workload distribution aggregates.' },
                  { stage: 11, name: 'Flutter Material 3 Frontend Implementation', status: 'UPCOMING', desc: 'Cross-platform mobile/web UI with 15 screens, blue/white student design, and dark mode.' },
                  { stage: 12, name: 'End-to-End API Integration & State Providers', status: 'UPCOMING', desc: 'Connect Flutter Dio client with backend JWT auth, tasks provider, and offline error handling.' },
                  { stage: 13, name: 'Comprehensive Testing & Documentation Verification', status: 'UPCOMING', desc: 'Unit tests for priority engine, mock auth tests, integration tests & user setup runbooks.' },
                ].map((step) => (
                  <div
                    key={step.stage}
                    className={`p-3.5 rounded-xl border flex items-start gap-3.5 transition-all ${
                      step.status === 'COMPLETED'
                        ? 'border-emerald-300 dark:border-emerald-800 bg-emerald-50/50 dark:bg-emerald-950/20'
                        : step.status === 'READY NEXT'
                        ? 'border-blue-300 dark:border-blue-700 bg-blue-50/40 dark:bg-blue-950/20 ring-1 ring-blue-400/40'
                        : 'border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-800/30'
                    }`}
                  >
                    <div className="mt-0.5">
                      {step.status === 'COMPLETED' ? (
                        <div className="w-6 h-6 rounded-full bg-emerald-600 text-white flex items-center justify-center">
                          <CheckCircle2 className="w-4 h-4" />
                        </div>
                      ) : (
                        <div className="w-6 h-6 rounded-full bg-slate-200 dark:bg-slate-700 text-slate-700 dark:text-slate-300 flex items-center justify-center text-xs font-bold font-mono">
                          {step.stage}
                        </div>
                      )}
                    </div>

                    <div className="flex-1">
                      <div className="flex items-center justify-between">
                        <h4 className="text-sm font-bold text-slate-900 dark:text-white">
                          Stage {step.stage}: {step.name}
                        </h4>
                        <span className={`text-[10px] font-bold px-2 py-0.5 rounded uppercase font-mono ${
                          step.status === 'COMPLETED'
                            ? 'bg-emerald-100 text-emerald-800 dark:bg-emerald-900 dark:text-emerald-200'
                            : step.status === 'READY NEXT'
                            ? 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200'
                            : 'bg-slate-200 text-slate-700 dark:bg-slate-800 dark:text-slate-400'
                        }`}>
                          {step.status}
                        </span>
                      </div>
                      <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">{step.desc}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}
      </main>

      {/* Footer */}
      <footer className="border-t border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 py-4">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 flex flex-col sm:flex-row items-center justify-between gap-2 text-xs text-slate-500 dark:text-slate-400">
          <p>© 2026 DeadlineGuard AI — Intelligent Student Task &amp; Deadline Management System</p>
          <p className="font-mono text-emerald-600 dark:text-emerald-400 font-semibold">Stage 3 Complete • Ready for Stage 4 (Authentication &amp; Security)</p>
        </div>
      </footer>
    </div>
  );
}
