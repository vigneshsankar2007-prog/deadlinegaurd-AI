import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/constants/app_colors.dart';
import '../../core/widgets/app_card.dart';
import '../../models/notification_model.dart';
import '../../providers/notification_provider.dart';

/**
 * Screen 12: Notifications Screen (Stage 9)
 */
class NotificationsScreen extends StatelessWidget {
  const NotificationsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final notifProvider = context.watch<NotificationProvider>();
    final notifications = notifProvider.notifications;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Deadline Reminders & Alerts'),
        actions: [
          if (notifProvider.unreadCount > 0)
            TextButton(
              onPressed: () => notifProvider.markAllAsRead(),
              child: const Text('Mark all read'),
            ),
        ],
      ),
      body: notifications.isEmpty
          ? const Center(
              child: Text('No notifications at this time.'),
            )
          : ListView.separated(
              padding: const EdgeInsets.all(16),
              itemCount: notifications.length,
              separatorBuilder: (_, __) => const SizedBox(height: 10),
              itemBuilder: (context, index) {
                final notif = notifications[index];
                return _buildNotificationCard(context, notif, notifProvider);
              },
            ),
    );
  }

  Widget _buildNotificationCard(
    BuildContext context,
    NotificationModel notif,
    NotificationProvider provider,
  ) {
    final theme = Theme.of(context);
    IconData icon;
    Color iconColor;

    switch (notif.notificationType) {
      case 'OVERDUE':
        icon = Icons.error_outline;
        iconColor = AppColors.statusOverdue;
        break;
      case 'DEADLINE_6H':
        icon = Icons.alarm_on;
        iconColor = AppColors.priorityCritical;
        break;
      case 'DEADLINE_24H':
        icon = Icons.warning_amber_rounded;
        iconColor = AppColors.priorityHigh;
        break;
      case 'DEADLINE_48H':
        icon = Icons.schedule;
        iconColor = AppColors.primary;
        break;
      case 'HIGH_PRIORITY':
        icon = Icons.priority_high;
        iconColor = AppColors.priorityCritical;
        break;
      case 'DAILY_SUMMARY':
      default:
        icon = Icons.summarize_outlined;
        iconColor = AppColors.secondary;
        break;
    }

    return AppCard(
      color: notif.isRead
          ? theme.cardTheme.color
          : (theme.brightness == Brightness.light
              ? const Color(0xFFEFF6FF)
              : const Color(0xFF1E293B)),
      onTap: () {
        if (!notif.isRead) {
          provider.markAsRead(notif.id);
        }
      },
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: iconColor.withOpacity(0.12),
              shape: BoxShape.circle,
            ),
            child: Icon(icon, size: 20, color: iconColor),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Expanded(
                      child: Text(
                        notif.title,
                        style: TextStyle(
                          fontWeight:
                              notif.isRead ? FontWeight.w600 : FontWeight.w700,
                          fontSize: 14,
                        ),
                      ),
                    ),
                    if (!notif.isRead)
                      Container(
                        width: 8,
                        height: 8,
                        decoration: const BoxDecoration(
                          color: AppColors.primary,
                          shape: BoxShape.circle,
                        ),
                      ),
                  ],
                ),
                const SizedBox(height: 4),
                Text(
                  notif.message,
                  style: theme.textTheme.bodyMedium?.copyWith(
                    fontSize: 13,
                    height: 1.35,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  _formatRelativeTime(notif.createdAt),
                  style: theme.textTheme.bodySmall?.copyWith(fontSize: 11),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  String _formatRelativeTime(DateTime dt) {
    final diff = DateTime.now().difference(dt);
    if (diff.inMinutes < 60) {
      return '${diff.inMinutes}m ago';
    } else if (diff.inHours < 24) {
      return '${diff.inHours}h ago';
    }
    return '${diff.inDays}d ago';
  }
}
