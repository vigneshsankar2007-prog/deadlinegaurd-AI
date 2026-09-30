import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/constants/app_constants.dart';
import '../../core/widgets/app_button.dart';
import '../../providers/auth_provider.dart';

/**
 * Screen 3: Student Registration Screen
 */
class RegisterScreen extends StatefulWidget {
  const RegisterScreen({super.key});

  @override
  State<RegisterScreen> createState() => _RegisterScreenState();
}

class _RegisterScreenState extends State<RegisterScreen> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();
  final _confirmPasswordController = TextEditingController();
  final _collegeController = TextEditingController();
  final _departmentController = TextEditingController();
  int _selectedSemester = 5;
  bool _obscurePassword = true;

  @override
  void dispose() {
    _nameController.dispose();
    _emailController.dispose();
    _passwordController.dispose();
    _confirmPasswordController.dispose();
    _collegeController.dispose();
    _departmentController.dispose();
    super.dispose();
  }

  void _handleRegister() async {
    if (_formKey.currentState?.validate() ?? false) {
      final authProvider = context.read<AuthProvider>();
      final success = await authProvider.register(
        name: _nameController.text,
        email: _emailController.text,
        password: _passwordController.text,
        confirmPassword: _confirmPasswordController.text,
        college: _collegeController.text,
        department: _departmentController.text,
        semester: _selectedSemester,
      );

      if (success && mounted) {
        Navigator.pushReplacementNamed(context, AppConstants.routeDashboard);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final authProvider = context.watch<AuthProvider>();

    return Scaffold(
      appBar: AppBar(
        title: const Text('Create Student Account'),
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24),
          child: Form(
            key: _formKey,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                if (authProvider.errorMessage != null) ...[
                  Container(
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: Colors.red.withOpacity(0.1),
                      borderRadius: BorderRadius.circular(8),
                      border: Border.all(color: Colors.red.withOpacity(0.3)),
                    ),
                    child: Text(
                      authProvider.errorMessage!,
                      style: const TextStyle(color: Colors.red, fontSize: 13),
                    ),
                  ),
                  const SizedBox(height: 16),
                ],
                TextFormField(
                  controller: _nameController,
                  decoration: const InputDecoration(
                    labelText: 'Full Name',
                    prefixIcon: Icon(Icons.person_outline),
                  ),
                  validator: (v) =>
                      v == null || v.trim().isEmpty ? 'Name is required' : null,
                ),
                const SizedBox(height: 16),
                TextFormField(
                  controller: _emailController,
                  keyboardType: TextInputType.emailAddress,
                  decoration: const InputDecoration(
                    labelText: 'University Email',
                    prefixIcon: Icon(Icons.email_outlined),
                  ),
                  validator: (v) => v == null || !v.contains('@')
                      ? 'Valid email required'
                      : null,
                ),
                const SizedBox(height: 16),
                TextFormField(
                  controller: _collegeController,
                  decoration: const InputDecoration(
                    labelText: 'College / University',
                    prefixIcon: Icon(Icons.account_balance_outlined),
                  ),
                  validator: (v) => v == null || v.trim().isEmpty
                      ? 'College is required'
                      : null,
                ),
                const SizedBox(height: 16),
                TextFormField(
                  controller: _departmentController,
                  decoration: const InputDecoration(
                    labelText: 'Academic Department',
                    prefixIcon: Icon(Icons.business_outlined),
                  ),
                  validator: (v) => v == null || v.trim().isEmpty
                      ? 'Department is required'
                      : null,
                ),
                const SizedBox(height: 16),
                DropdownButtonFormField<int>(
                  value: _selectedSemester,
                  decoration: const InputDecoration(
                    labelText: 'Current Semester',
                    prefixIcon: Icon(Icons.calendar_today_outlined),
                  ),
                  items: List.generate(8, (i) => i + 1)
                      .map((sem) => DropdownMenuItem(
                            value: sem,
                            child: Text('Semester $sem'),
                          ))
                      .toList(),
                  onChanged: (val) {
                    if (val != null) {
                      setState(() => _selectedSemester = val);
                    }
                  },
                ),
                const SizedBox(height: 16),
                TextFormField(
                  controller: _passwordController,
                  obscureText: _obscurePassword,
                  decoration: InputDecoration(
                    labelText: 'Password',
                    prefixIcon: const Icon(Icons.lock_outline),
                    suffixIcon: IconButton(
                      icon: Icon(
                        _obscurePassword
                            ? Icons.visibility_outlined
                            : Icons.visibility_off_outlined,
                      ),
                      onPressed: () {
                        setState(() => _obscurePassword = !_obscurePassword);
                      },
                    ),
                  ),
                  validator: (v) => v == null || v.length < 6
                      ? 'Password must be at least 6 characters'
                      : null,
                ),
                const SizedBox(height: 16),
                TextFormField(
                  controller: _confirmPasswordController,
                  obscureText: _obscurePassword,
                  decoration: const InputDecoration(
                    labelText: 'Confirm Password',
                    prefixIcon: Icon(Icons.lock_reset_outlined),
                  ),
                  validator: (v) {
                    if (v == null || v.isEmpty) {
                      return 'Please confirm your password';
                    }
                    if (v != _passwordController.text) {
                      return 'Passwords do not match';
                    }
                    return null;
                  },
                ),
                const SizedBox(height: 24),
                AppButton(
                  text: 'Register Account',
                  isLoading: authProvider.isLoading,
                  onPressed: _handleRegister,
                ),
                const SizedBox(height: 16),
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Text('Already have an account?',
                        style: theme.textTheme.bodyMedium),
                    TextButton(
                      onPressed: () => Navigator.pop(context),
                      child: const Text('Sign In'),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
