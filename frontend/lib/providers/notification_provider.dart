import 'package:flutter/material.dart';
import '../models/notification_model.dart';
import '../mock/mock_data.dart';

/**
 * State provider managing student deadline alerts and system notifications.
 */
class NotificationProvider with ChangeNotifier {
  final List<NotificationModel> _notifications =
      List.from(MockData.initialNotifications);

  List<NotificationModel> get notifications =>
      List.unmodifiable(_notifications);

  int get unreadCount => _notifications.where((n) => !n.isRead).length;

  void markAsRead(int id) {
    final index = _notifications.indexWhere((n) => n.id == id);
    if (index != -1 && !_notifications[index].isRead) {
      _notifications[index] = _notifications[index].copyWith(isRead: true);
      notifyListeners();
    }
  }

  void markAllAsRead() {
    for (int i = 0; i < _notifications.length; i++) {
      if (!_notifications[i].isRead) {
        _notifications[i] = _notifications[i].copyWith(isRead: true);
      }
    }
    notifyListeners();
  }
}
