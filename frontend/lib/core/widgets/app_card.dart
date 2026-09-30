import 'package:flutter/material.dart';

/**
 * Reusable rounded surface card with consistent border and padding.
 */
class AppCard extends StatelessWidget {
  final Widget child;
  final EdgeInsetsGeometry? padding;
  final VoidCallback? onTap;
  final Color? color;
  final BorderSide? border;

  const AppCard({
    super.key,
    required this.child,
    this.padding,
    this.onTap,
    this.color,
    this.border,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final cardWidget = Container(
      decoration: BoxDecoration(
        color: color ?? theme.cardTheme.color,
        borderRadius: BorderRadius.circular(16),
        border: border != null
            ? Border.fromBorderSide(border!)
            : Border.all(
                color: theme.brightness == Brightness.light
                    ? const Color(0xFFE2E8F0)
                    : const Color(0xFF334155),
                width: 1,
              ),
      ),
      padding: padding ?? const EdgeInsets.all(16),
      child: child,
    );

    if (onTap != null) {
      return InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(16),
        child: cardWidget,
      );
    }

    return cardWidget;
  }
}
