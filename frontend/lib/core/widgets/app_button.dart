import 'package:flutter/material.dart';

/**
 * Reusable full-width or inline button supporting loading and icons.
 */
class AppButton extends StatelessWidget {
  final String text;
  final VoidCallback? onPressed;
  final bool isLoading;
  final bool isOutlined;
  final IconData? icon;
  final Color? color;
  final double? width;

  const AppButton({
    super.key,
    required this.text,
    required this.onPressed,
    this.isLoading = false,
    this.isOutlined = false,
    this.icon,
    this.color,
    this.width,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    Widget childWidget = isLoading
        ? SizedBox(
            height: 20,
            width: 20,
            child: CircularProgressIndicator(
              strokeWidth: 2.5,
              valueColor: AlwaysStoppedAnimation<Color>(
                isOutlined ? theme.primaryColor : Colors.white,
              ),
            ),
          )
        : Row(
            mainAxisSize: MainAxisSize.min,
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              if (icon != null) ...[
                Icon(icon, size: 18),
                const SizedBox(width: 8),
              ],
              Text(
                text,
                style: const TextStyle(fontWeight: FontWeight.w600),
              ),
            ],
          );

    return SizedBox(
      width: width,
      height: 50,
      child: isOutlined
          ? OutlinedButton(
              onPressed: isLoading ? null : onPressed,
              style: color != null
                  ? OutlinedButton.styleFrom(
                      foregroundColor: color,
                      side: BorderSide(color: color!),
                    )
                  : null,
              child: childWidget,
            )
          : ElevatedButton(
              onPressed: isLoading ? null : onPressed,
              style: color != null
                  ? ElevatedButton.styleFrom(backgroundColor: color)
                  : null,
              child: childWidget,
            ),
    );
  }
}
