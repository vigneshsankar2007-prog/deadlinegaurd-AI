import 'package:flutter/material.dart';
import '../constants/app_colors.dart';

/**
 * Task lifecycle status chip: PENDING, IN_PROGRESS, COMPLETED, OVERDUE.
 */
class StatusChip extends StatelessWidget {
  final String status;

  const StatusChip({super.key, required this.status});

  @override
  Widget build(BuildContext context) {
    Color color;
    String label = status.replaceAll('_', ' ');

    switch (status.toUpperCase()) {
      case 'COMPLETED':
        color = AppColors.statusCompleted;
        break;
      case 'IN_PROGRESS':
        color = AppColors.statusInProgress;
        break;
      case 'OVERDUE':
        color = AppColors.statusOverdue;
        break;
      case 'PENDING':
      default:
        color = AppColors.statusPending;
        break;
    }

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
      decoration: BoxDecoration(
        color: color.withOpacity(0.12),
        borderRadius: BorderRadius.circular(6),
      ),
      child: Text(
        label,
        style: TextStyle(
          color: color,
          fontSize: 11,
          fontWeight: FontWeight.w600,
        ),
      ),
    );
  }
}
