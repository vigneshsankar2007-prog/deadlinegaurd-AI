import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/constants/app_colors.dart';
import '../../core/constants/app_constants.dart';
import '../../core/widgets/app_card.dart';
import '../../core/widgets/empty_state.dart';
import '../../core/widgets/priority_badge.dart';
import '../../core/widgets/status_chip.dart';
import '../../providers/task_provider.dart';

/**
 * Screen 5: Task List Screen
 * Supports filter chips (All, Pending, In Progress, Completed, Overdue),
 * sorting dropdown, search bar, and FAB to add task.
 */
class TaskListScreen extends StatelessWidget {
  const TaskListScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final taskProvider = context.watch<TaskProvider>();
    final tasks = taskProvider.filteredTasks;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Academic Deliverables',
            style: TextStyle(fontWeight: FontWeight.w700)),
        actions: [
          PopupMenuButton<String>(
            icon: const Icon(Icons.sort),
            tooltip: 'Sort Deliverables',
            initialValue: taskProvider.selectedSortOption,
            onSelected: (val) => taskProvider.setSortOption(val),
            itemBuilder: (context) => AppConstants.sortOptions
                .map((opt) => PopupMenuItem(value: opt, child: Text(opt)))
                .toList(),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () =>
            Navigator.pushNamed(context, AppConstants.routeAddTask),
        icon: const Icon(Icons.add),
        label: const Text('Add Deliverable'),
        backgroundColor: AppColors.primary,
        foregroundColor: Colors.white,
      ),
      body: Column(
        children: [
          // Search Bar
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 8, 16, 8),
            child: TextField(
              decoration: InputDecoration(
                hintText: 'Search tasks, subjects, codes...',
                prefixIcon: const Icon(Icons.search, size: 20),
                contentPadding:
                    const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                suffixIcon: taskProvider.searchQuery.isNotEmpty
                    ? IconButton(
                        icon: const Icon(Icons.clear, size: 18),
                        onPressed: () => taskProvider.setSearchQuery(''),
                      )
                    : null,
              ),
              onChanged: (val) => taskProvider.setSearchQuery(val),
            ),
          ),

          // Status Filter Chips
          SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
            child: Row(
              children: AppConstants.taskStatuses.map((status) {
                final isSelected = taskProvider.selectedStatusFilter == status;
                return Padding(
                  padding: const EdgeInsets.only(right: 8),
                  child: FilterChip(
                    label: Text(status.replaceAll('_', ' ')),
                    selected: isSelected,
                    onSelected: (_) => taskProvider.setStatusFilter(status),
                    selectedColor: AppColors.primaryContainer,
                    labelStyle: TextStyle(
                      color: isSelected
                          ? AppColors.onPrimaryContainer
                          : theme.textTheme.bodyMedium?.color,
                      fontWeight:
                          isSelected ? FontWeight.w700 : FontWeight.w500,
                      fontSize: 12,
                    ),
                  ),
                );
              }).toList(),
            ),
          ),

          const SizedBox(height: 8),

          // Task List View
          Expanded(
            child: tasks.isEmpty
                ? const EmptyStateWidget(
                    icon: Icons.assignment_outlined,
                    title: 'No deliverables found',
                    description:
                        'Try changing your status filter or search query.',
                  )
                : ListView.separated(
                    padding: const EdgeInsets.fromLTRB(16, 8, 16, 88),
                    itemCount: tasks.length,
                    separatorBuilder: (_, __) => const SizedBox(height: 12),
                    itemBuilder: (context, index) {
                      final task = tasks[index];
                      return AppCard(
                        onTap: () {
                          Navigator.pushNamed(
                            context,
                            AppConstants.routeTaskDetails,
                            arguments: task.id,
                          );
                        },
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Checkbox(
                                  value: task.isCompleted,
                                  onChanged: (_) {
                                    taskProvider.toggleTaskCompletion(task.id);
                                  },
                                  shape: RoundedRectangleBorder(
                                    borderRadius: BorderRadius.circular(4),
                                  ),
                                ),
                                const SizedBox(width: 4),
                                Expanded(
                                  child: Column(
                                    crossAxisAlignment:
                                        CrossAxisAlignment.start,
                                    children: [
                                      Text(
                                        task.title,
                                        style: TextStyle(
                                          fontWeight: FontWeight.w600,
                                          fontSize: 15,
                                          decoration: task.isCompleted
                                              ? TextDecoration.lineThrough
                                              : null,
                                        ),
                                      ),
                                      if (task.description != null &&
                                          task.description!.isNotEmpty) ...[
                                        const SizedBox(height: 4),
                                        Text(
                                          task.description!,
                                          maxLines: 2,
                                          overflow: TextOverflow.ellipsis,
                                          style: theme.textTheme.bodySmall,
                                        ),
                                      ],
                                    ],
                                  ),
                                ),
                                const SizedBox(width: 8),
                                PriorityBadge(
                                  priorityLevel: task.priorityLevel,
                                  score: task.priorityScore,
                                ),
                              ],
                            ),
                            const Divider(height: 20),
                            Row(
                              children: [
                                if (task.subject != null) ...[
                                  Container(
                                    padding: const EdgeInsets.symmetric(
                                        horizontal: 6, vertical: 2),
                                    decoration: BoxDecoration(
                                      color:
                                          task.subject!.color.withOpacity(0.15),
                                      borderRadius: BorderRadius.circular(4),
                                    ),
                                    child: Text(
                                      task.subject!.subjectCode,
                                      style: TextStyle(
                                        color: task.subject!.color,
                                        fontSize: 11,
                                        fontWeight: FontWeight.w700,
                                      ),
                                    ),
                                  ),
                                  const SizedBox(width: 8),
                                ],
                                StatusChip(status: task.status),
                                const Spacer(),
                                Icon(Icons.timer_outlined,
                                    size: 14,
                                    color: theme.textTheme.bodySmall?.color),
                                const SizedBox(width: 4),
                                Text(
                                  '${task.estimatedHours}h',
                                  style: theme.textTheme.bodySmall,
                                ),
                                const SizedBox(width: 12),
                                Icon(Icons.event_outlined,
                                    size: 14,
                                    color: task.isOverdue
                                        ? AppColors.error
                                        : theme.textTheme.bodySmall?.color),
                                const SizedBox(width: 4),
                                Text(
                                  _formatDate(task.deadline),
                                  style: TextStyle(
                                    fontSize: 11,
                                    color: task.isOverdue
                                        ? AppColors.error
                                        : theme.textTheme.bodySmall?.color,
                                    fontWeight: task.isOverdue
                                        ? FontWeight.w700
                                        : FontWeight.normal,
                                  ),
                                ),
                              ],
                            ),
                          ],
                        ),
                      );
                    },
                  ),
          ),
        ],
      ),
    );
  }

  String _formatDate(DateTime dt) {
    return '${dt.month}/${dt.day} ${dt.hour.toString().padLeft(2, '0')}:${dt.minute.toString().padLeft(2, '0')}';
  }
}
