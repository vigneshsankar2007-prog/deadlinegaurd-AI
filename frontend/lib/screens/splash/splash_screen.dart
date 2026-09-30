import 'package:flutter/material.dart';
import '../../core/constants/app_colors.dart';
import '../../core/constants/app_constants.dart';

/**
 * Screen 1: Splash Screen
 * Features DeadlineGuard AI branding, tagline, and smooth local transition.
 */
class SplashScreen extends StatefulWidget {
  const SplashScreen({super.key});

  @override
  State<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen> {
  @override
  void initState() {
    super.initState();
    _navigateToNext();
  }

  Future<void> _navigateToNext() async {
    await Future.delayed(const Duration(milliseconds: 1500));
    if (mounted) {
      Navigator.pushReplacementNamed(context, AppConstants.routeDashboard);
    }
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Scaffold(
      body: Container(
        width: double.infinity,
        decoration: BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: isDark
                ? [const Color(0xFF0F172A), const Color(0xFF1E293B)]
                : [AppColors.primary, const Color(0xFF2563EB)],
          ),
        ),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Spacer(),
            Container(
              padding: const EdgeInsets.all(24),
              decoration: BoxDecoration(
                color: Colors.white.withOpacity(0.15),
                shape: BoxShape.circle,
                border: Border.all(color: Colors.white.withOpacity(0.3), width: 2),
              ),
              child: const Icon(
                Icons.shield_outlined,
                size: 72,
                color: Colors.white,
              ),
            ),
            const SizedBox(height: 24),
            const Text(
              AppConstants.appName,
              style: TextStyle(
                color: Colors.white,
                fontSize: 28,
                fontWeight: FontWeight.w800,
                letterSpacing: 0.5,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              AppConstants.appTagline,
              textAlign: TextAlign.center,
              style: TextStyle(
                color: Colors.white.withOpacity(0.85),
                fontSize: 14,
                fontWeight: FontWeight.w400,
              ),
            ),
            const Spacer(),
            const SizedBox(
              width: 36,
              height: 36,
              child: CircularProgressIndicator(
                strokeWidth: 3,
                valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
              ),
            ),
            const SizedBox(height: 48),
          ],
        ),
      ),
    );
  }
}
