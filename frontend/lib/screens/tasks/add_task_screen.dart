import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/constants/app_constants.dart';
import '../../core/widgets/app_button.dart';
import '../../models/subject_model.dart';
import '../../providers/task_provider.dart';
import '../../providers/subject_provider.dart';

/**
 * Screen 6: Add Task Screen
 */
class AddTaskScreen extends StatefulWidget {
  const AddTaskScreen({super.key});

  @override
  State<AddTaskScreen> createState() => _AddTaskScreenState();
}

class _AddTaskScreenState extends State<AddTaskScreen> {
  final _formKey = GlobalKey<FormState>();
  final _titleController = TextEditingController();
  final _descController = TextEditingController();
  final _hoursController = TextEditingController(text: '3.0');

  String _taskType = 'ASSIGNMENT';
  SubjectModel? _selectedSubject;
  DateTime _deadline = DateTime.now().add(const Duration(days: 2));
  double _difficulty = 3.0; // 1 - 5
  double _academicWeight = 20.0; // 0 - 100

  @override
  void dispose() {
    _titleController.dispose();
    _descController.dispose();
    _hoursController.dispose();
    super.dispose();
  }

  Future<void> _pickDateTime() async {
    final pickedDate = await showDatePicker(
      context: context,
      initialDate: _deadline,
      firstDate: DateTime.now().subtract(const Duration(days: 1)),
      lastDate: DateTime.now().add(const Duration(days: 365)),
    );

    if (pickedDate != null && mounted) {
      final pickedTime = await showTimePicker(
        context: context,
        initialTime: TimeOfDay.fromDateTime(_deadline),
      );

      if (pickedTime != null) {
        setState(() {
          _deadline = DateTime(
            pickedDate.year,
            pickedDate.month,
            pickedDate.day,
            pickedTime.hour,
            pickedTime.minute,
          );
        });
      }
    }
  }

  void _handleSubmit() {
    if (_formKey.currentState?.validate() ?? false) {
      final hours = double.tryParse(_hoursController.text) ?? 2.0;

      context.read<TaskProvider>().addTask(
            title: _titleController.text,
            description: _descController.text.isNotEmpty
                ? _descController.text
                : null,
            taskType: _taskType,
            subject: _selectedSubject,
            deadline: _deadline,
            difficulty: _difficulty.round(),
            academicWeight: _academicWeight,
            estimatedHours: hours,
          );

      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Deliverable added successfully!')),
      );

      Navigator.pop(context);
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final subjects = context.watch<SubjectProvider>().subjects;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Add Academic Deliverable'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              TextFormField(
                controller: _titleController,
                decoration: const InputDecoration(
                  labelText: 'Task Title *',
                  hintText: 'e.g., Bellman-Ford Shortest Path Algorithm',
                ),
                validator: (v) =>
                    v == null || v.trim().isEmpty ? 'Title is required' : null,
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _descController,
                maxLines: 3,
                decoration: const InputDecoration(
                  labelText: 'Description (Optional)',
                  hintText: 'Add guidelines, submission instructions, or notes...',
                ),
              ),
              const SizedBox(height: 16),
              Row(
                children: [
                  Expanded(
                    child: DropdownButtonFormField<String>(
                      value: _taskType,
                      decoration: const InputDecoration(labelText: 'Task Type'),
                      items: AppConstants.taskTypes
                          .map((t) => DropdownMenuItem(
                                value: t,
                                child: Text(t),
                              ))
                          .toList(),
                      onChanged: (val) {
                        if (val != null) setState(() => _taskType = val);
                      },
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: DropdownButtonFormField<SubjectModel?>(
                      value: _selectedSubject,
                      decoration: const InputDecoration(labelText: 'Subject'),
                      items: [
                        const DropdownMenuItem<SubjectModel?>(
                          value: null,
                          child: Text('Unassigned'),
                        ),
                        ...subjects.map((s) => DropdownMenuItem<SubjectModel?>(
                              value: s,
                              child: Text(s.subjectCode),
                            )),
                      ],
                      onChanged: (val) => setState(() => _selectedSubject = val),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 16),

              // Deadline Picker
              ListTile(
                contentPadding:
                    const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                  side: BorderSide(
                    color: theme.brightness == Brightness.light
                        ? const Color(0xFFE2E8F0)
                        : const Color(0xFF334155),
                  ),
                ),
                leading: Icon(Icons.event_outlined, color: theme.primaryColor),
                title: const Text('Deadline Date & Time'),
                subtitle: Text(
                  '${_deadline.year}-${_deadline.month.toString().padLeft(2, '0')}-${_deadline.day.toString().padLeft(2, '0')} at ${_deadline.hour.toString().padLeft(2, '0')}:${_deadline.minute.toString().padLeft(2, '0')}',
                  style: const TextStyle(fontWeight: FontWeight.w600),
                ),
                trailing: TextButton(
                  onPressed: _pickDateTime,
                  child: const Text('Change'),
                ),
              ),
              const SizedBox(height: 16),

              TextFormField(
                controller: _hoursController,
                keyboardType: const TextInputType.numberWithOptions(decimal: true),
                decoration: const InputDecoration(
                  labelText: 'Estimated Effort (Hours) *',
                  prefixIcon: Icon(Icons.timer_outlined),
                ),
                validator: (v) {
                  if (v == null || v.isEmpty) return 'Effort is required';
                  final n = double.tryParse(v);
                  if (n == null || n <= 0) return 'Enter valid positive hours';
                  return null;
                },
              ),
              const SizedBox(height: 20),

              // Difficulty Slider
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text('Difficulty Level (1 = Easy, 5 = Very Hard)',
                      style: TextStyle(fontWeight: FontWeight.w600)),
                  Text('${_difficulty.round()} / 5',
                      style: const TextStyle(fontWeight: FontWeight.w700)),
                ],
              ),
              Slider(
                value: _difficulty,
                min: 1.0,
                max: 5.0,
                divisions: 4,
                label: '${_difficulty.round()}',
                onChanged: (val) => setState(() => _difficulty = val),
              ),
              const SizedBox(height: 12),

              // Academic Weight Slider
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text('Academic Weight (% of final course grade)',
                      style: TextStyle(fontWeight: FontWeight.w600)),
                  Text('${_academicWeight.round()}%',
                      style: const TextStyle(fontWeight: FontWeight.w700)),
                ],
              ),
              Slider(
                value: _academicWeight,
                min: 0.0,
                max: 100.0,
                divisions: 20,
                label: '${_academicWeight.round()}%',
                onChanged: (val) => setState(() => _academicWeight = val),
              ),
              const SizedBox(height: 28),

              AppButton(
                text: 'Save Deliverable',
                onPressed: _handleSubmit,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
