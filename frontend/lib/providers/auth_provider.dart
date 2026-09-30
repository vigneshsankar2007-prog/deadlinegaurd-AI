import 'package:flutter/material.dart';
import '../models/user_model.dart';
import '../mock/mock_data.dart';

/**
 * State provider for student authentication and profile (mock implementation for Stage 11).
 */
class AuthProvider with ChangeNotifier {
  UserModel? _currentUser = MockData.initialUser;
  bool _isAuthenticated = true;
  bool _isLoading = false;
  String? _errorMessage;

  UserModel? get currentUser => _currentUser;
  bool get isAuthenticated => _isAuthenticated;
  bool get isLoading => _isLoading;
  String? get errorMessage => _errorMessage;

  Future<bool> login(String email, String password) async {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    await Future.delayed(const Duration(milliseconds: 600));

    if (email.trim().isEmpty || !email.contains('@')) {
      _errorMessage = 'Please enter a valid university email address';
      _isLoading = false;
      notifyListeners();
      return false;
    }

    if (password.length < 6) {
      _errorMessage = 'Password must be at least 6 characters';
      _isLoading = false;
      notifyListeners();
      return false;
    }

    _currentUser = MockData.initialUser.copyWith(email: email.trim());
    _isAuthenticated = true;
    _isLoading = false;
    notifyListeners();
    return true;
  }

  Future<bool> register({
    required String name,
    required String email,
    required String password,
    required String confirmPassword,
    required String college,
    required String department,
    required int semester,
  }) async {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    await Future.delayed(const Duration(milliseconds: 600));

    if (password != confirmPassword) {
      _errorMessage = 'Passwords do not match';
      _isLoading = false;
      notifyListeners();
      return false;
    }

    _currentUser = UserModel(
      id: 2,
      name: name.trim(),
      email: email.trim(),
      college: college.trim(),
      department: department.trim(),
      semester: semester,
    );
    _isAuthenticated = true;
    _isLoading = false;
    notifyListeners();
    return true;
  }

  void logout() {
    _currentUser = null;
    _isAuthenticated = false;
    notifyListeners();
  }
}
