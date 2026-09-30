import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/constants/app_colors.dart';
import '../../core/widgets/app_card.dart';
import '../../core/widgets/app_button.dart';
import '../../models/subject_model.dart';
import '../../providers/subject_provider.dart';
import '../../providers/task_provider.dart';

/**
 * Screen 8: Subject Management Screen
 */
class SubjectManagementScreen extends StatelessWidget {
  const SubjectManagementScreen({super.key});

  void _showSubjectDialog(BuildContext context, {SubjectModel? existing}) {
    final nameCtrl = TextEditingController(text: existing?.subjectName ?? '');
    final codeCtrl = TextEditingController(text: existing?.subjectCode ?? '');
    final credCtrl =
        TextEditingController(text: existing?.credits.toString() ?? '3');
    String selectedColor = existing?.colorHex ?? '#3B82F6';

    final colors = [
      '#3B82F6', // Blue
      '#10B981', // Green
      '#8B5CF6', // Purple
      '#F59E0B', // Amber
      '#EC4899', // Pink
      '#06B6D4', // Cyan
    ];

    showDialog(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setState) => AlertDialog(
          title: Text(existing == null ? 'Add Subject' : 'Edit Subject'),
          content: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextField(
                  controller: nameCtrl,
                  decoration: const InputDecoration(
                    labelText: 'Subject Name',
                    hintText: 'e.g., Computer Networks',
                  ),
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: codeCtrl,
                  decoration: const InputDecoration(
                    labelText: 'Course Catalog Code',
                    hintText: 'e.g., CS401',
                  ),
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: credCtrl,
                  keyboardType: TextInputType.number,
                  decoration: const InputDecoration(
                    labelText: 'Academic Credits (1 - 6)',
                  ),
                ),
                const SizedBox(height: 16),
                const Align(
                  alignment: Alignment.centerLeft,
                  child: Text('Theme Color',
                      style: TextStyle(fontWeight: FontWeight.w600, fontSize: 13)),
                ),
                const SizedBox(height: 8),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceAround,
                  children: colors.map((hex) {
                    final color = Color(
                        int.parse('FF${hex.replaceAll('#', '')}', radix: 16));
                    final isSel = selectedColor == hex;
                    return GestureDetector(
                      onTap: () => setState(() => selectedColor = hex),
                      child: Container(
                        width: 32,
                        height: 32,
                        decoration: BoxDecoration(
                          color: color,
                          shape: BoxShape.circle,
                          border: isSel
                              ? Border.all(color: Colors.white, width: 3)
                              : null,
                          boxShadow: isSel
                              ? [const BoxShadow(color: Colors.black26, blurRadius: 4)]
                              : null,
                        ),
                      ),
                    );
                  }).toList(),
                ),
              ],
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(ctx),
              child: const Text('Cancel'),
            ),
            ElevatedButton(
              onPressed: () {
                final name = nameCtrl.text.trim();
                final code = codeCtrl.text.trim();
                final creds = int.tryParse(credCtrl.text) ?? 3;

                if (name.isNotEmpty && code.isNotEmpty) {
                  final provider = context.read<SubjectProvider>();
                  if (existing == null) {
                    provider.addSubject(
                      name: name,
                      code: code,
                      credits: creds,
                      colorHex: selectedColor,
                    );
                  } else {
                    provider.updateSubject(
                      id: existing.id,
                      name: name,
                      code: code,
                      credits: creds,
                      colorHex: selectedColor,
                    );
                  }
                  Navigator.pop(ctx);
                }
              },
              child: const Text('Save'),
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final subjectProvider = context.watch<SubjectProvider>();
    final taskProvider = context.watch<TaskProvider>();
    final subjects = subjectProvider.subjects;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Course Catalog & Subjects'),
        actions: [
          IconButton(
            icon: const Icon(Icons.add),
            tooltip: 'Add Course',
            onPressed: () => _showSubjectDialog(context),
          ),
        ],
      ),
      body: subjects.isEmpty
          ? Center(
              child: Padding(
                padding: const EdgeInsets.all(24),
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    const Icon(Icons.menu_book_outlined,
                        size: 64, color: AppColors.primary),
                    const SizedBox(height: 16),
                    const Text('No courses enrolled yet',
                        style: TextStyle(
                            fontWeight: FontWeight.w700, fontSize: 18)),
                    const SizedBox(height: 8),
                    const Text('Add your semester subjects to track deliverables.'),
                    const SizedBox(height: 24),
                    AppButton(
                      text: 'Add First Course',
                      onPressed: () => _showSubjectDialog(context),
                    ),
                  ],
                ),
              ),
            )
          : ListView.separated(
              padding: const EdgeInsets.all(16),
              itemCount: subjects.length,
              separatorBuilder: (_, __) => const SizedBox(height: 12),
              itemBuilder: (context, index) {
                final subject = subjects[index];
                final subjectTasks = taskProvider.allTasks
                    .where((t) => t.subject?.id == subject.id)
                    .toList();
                final completedCount =
                    subjectTasks.where((t) => t.isCompleted).length;
                final taskCount = subjectTasks.length;
                final completionPct = taskCount == 0
                    ? 0.0
                    : (completedCount / taskCount) * 100.0;

                return AppCard(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Container(
                            padding: const EdgeInsets.symmetric(
                                horizontal: 10, vertical: 6),
                            decoration: BoxDecoration(
                              color: subject.color.withOpacity(0.15),
                              borderRadius: BorderRadius.circular(8),
                            ),
                            child: Text(
                              subject.subjectCode,
                              style: TextStyle(
                                color: subject.color,
                                fontWeight: FontWeight.w800,
                                fontSize: 14,
                              ),
                            ),
                          ),
                          const SizedBox(width: 12),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  subject.subjectName,
                                  style: const TextStyle(
                                    fontWeight: FontWeight.w700,
                                    fontSize: 16,
                                  ),
                                ),
                                Text(
                                  '${subject.credits} Academic Credits • $taskCount Deliverables',
                                  style: theme.textTheme.bodySmall,
                                ),
                              ],
                            ),
                          ),
                          PopupMenuButton<String>(
                            onSelected: (val) {
                              if (val == 'edit') {
                                _showSubjectDialog(context, existing: subject);
                              } else if (val == 'delete') {
                                _confirmDelete(context, subject);
                              }
                            },
                            itemBuilder: (context) => const [
                              PopupMenuItem(
                                  value: 'edit', child: Text('Edit Course')),
                              PopupMenuItem(
                                value: 'delete',
                                child: Text('Delete',
                                    style: TextStyle(color: AppColors.error)),
                              ),
                            ],
                          ),
                        ],
                      ),
                      const SizedBox(height: 16),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text(
                            'Completion Progress: $completedCount of $taskCount done',
                            style: const TextStyle(fontSize: 12),
                          ),
                          Text(
                            '${completionPct.toStringAsFixed(0)}%',
                            style: const TextStyle(
                                fontWeight: FontWeight.w700, fontSize: 12),
                          ),
                        ],
                      ),
                      const SizedBox(height: 6),
                      ClipRRect(
                        borderRadius: BorderRadius.circular(4),
                        child: LinearProgressIndicator(
                          value: (completionPct / 100.0).clamp(0.0, 1.0),
                          minHeight: 6,
                          backgroundColor: const Color(0xFFE2E8F0),
                          valueColor:
                              AlwaysStoppedAnimation<Color>(subject.color),
                        ),
                      ),
                    ],
                  ),
                );
              },
            ),
    );
  }

  void _confirmDelete(BuildContext context, SubjectModel subject) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text('Delete ${subject.subjectCode}?'),
        content: const Text(
          'Tasks associated with this subject will become unassigned. This action cannot be undone.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(backgroundColor: AppColors.error),
            onPressed: () {
              context.read<SubjectProvider>().deleteSubject(subject.id);
              Navigator.pop(ctx);
            },
            child: const Text('Delete'),
          ),
        ],
      ),
    );
  }
}
