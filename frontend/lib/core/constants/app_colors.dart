import 'package:flutter/material.dart';

/**
 * DeadlineGuard AI Design System - Centralized Color Palette
 * Clean blue & white academic productivity theme with Material 3 contrast.
 */
class AppColors {
  AppColors._();

  // Primary Academic Blue Palette
  static const Color primary = Color(0xFF1E40AF); // Blue 800
  static const Color primaryLight = Color(0xFF3B82F6); // Blue 500
  static const Color primaryDark = Color(0xFF172554); // Blue 950
  static const Color primaryContainer = Color(0xFFDBEAFE); // Blue 100
  static const Color onPrimaryContainer = Color(0xFF1E3A8A);

  // Secondary Accents
  static const Color secondary = Color(0xFF0EA5E9); // Sky 500
  static const Color secondaryContainer = Color(0xFFE0F2FE);
  static const Color tertiary = Color(0xFF6366F1); // Indigo 500

  // Neutral Light Surfaces
  static const Color backgroundLight = Color(0xFFF8FAFC); // Slate 50
  static const Color surfaceLight = Color(0xFFFFFFFF);
  static const Color surfaceVariantLight = Color(0xFFF1F5F9); // Slate 100
  static const Color borderLight = Color(0xFFE2E8F0); // Slate 200
  static const Color textPrimaryLight = Color(0xFF0F172A); // Slate 900
  static const Color textSecondaryLight = Color(0xFF64748B); // Slate 500
  static const Color textTertiaryLight = Color(0xFF94A3B8); // Slate 400

  // Neutral Dark Surfaces
  static const Color backgroundDark = Color(0xFF0B132B);
  static const Color surfaceDark = Color(0xFF1C2541);
  static const Color surfaceVariantDark = Color(0xFF2B3A67);
  static const Color borderDark = Color(0xFF3A4D7A);
  static const Color textPrimaryDark = Color(0xFFF8FAFC);
  static const Color textSecondaryDark = Color(0xFF94A3B8);
  static const Color textTertiaryDark = Color(0xFF64748B);

  // Stage 6 Priority Tier Colors
  static const Color priorityCritical = Color(0xFFEF4444); // Red 500
  static const Color priorityCriticalBg = Color(0xFFFEE2E2);
  static const Color priorityHigh = Color(0xFFF97316); // Orange 500
  static const Color priorityHighBg = Color(0xFFFFEDD5);
  static const Color priorityMedium = Color(0xFFF59E0B); // Amber 500
  static const Color priorityMediumBg = Color(0xFFFEF3C7);
  static const Color priorityLow = Color(0xFF10B981); // Emerald 500
  static const Color priorityLowBg = Color(0xFFD1FAE5);

  // Lifecycle Status Colors
  static const Color statusPending = Color(0xFF64748B);
  static const Color statusInProgress = Color(0xFF2563EB);
  static const Color statusCompleted = Color(0xFF059669);
  static const Color statusOverdue = Color(0xFFDC2626);

  // Functional Alerts
  static const Color success = Color(0xFF10B981);
  static const Color warning = Color(0xFFF59E0B);
  static const Color error = Color(0xFFEF4444);
  static const Color info = Color(0xFF3B82F6);
}
