import 'package:flutter/material.dart';
import '../constants/app_colors.dart';

/**
 * Stage 6 Priority Tier Badge Widget
 * Accurately visualizes CRITICAL, HIGH, MEDIUM, LOW tiers.
 */
class PriorityBadge extends StatelessWidget {
  final String priorityLevel;
  final double? score;

  const PriorityBadge({
    super.key,
    required this.priorityLevel,
    this.score,
  });

  @override
  Widget build(BuildContext context) {
    Color bg;
    Color fg;

    switch (priorityLevel.toUpperCase()) {
      case 'CRITICAL':
        bg = AppColors.priorityCriticalBg;
        fg = AppColors.priorityCritical;
        break;
      case 'HIGH':
        bg = AppColors.priorityHighBg;
        fg = AppColors.priorityHigh;
        break;
      case 'MEDIUM':
        bg = AppColors.priorityMediumBg;
        fg = const Color(0xFFD97706);
        break;
      case 'LOW':
      default:
        bg = AppColors.priorityLowBg;
        fg = AppColors.priorityLow;
        break;
    }

    final displayText = score != null
        ? '$priorityLevel (${score!.toStringAsFixed(1)})'
        : priorityLevel;

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: bg,
        borderRadius: BorderRadius.circular(6),
        border: Border.all(color: fg.withOpacity(0.3), width: 1),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            width: 6,
            height: 6,
            decoration: BoxDecoration(
              color: fg,
              shape: BoxShape.circle,
            ),
          ),
          const SizedBox(width: 6),
          Text(
            displayText,
            style: TextStyle(
              color: fg,
              fontWeight: FontWeight.w700,
              fontSize: 11,
            ),
          ),
        ],
      ),
    );
  }
}
