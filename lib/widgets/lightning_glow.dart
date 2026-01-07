// import 'package:flutter/material.dart';

// class LightningGlow extends StatefulWidget {
//   final Widget child;

//   const LightningGlow({super.key, required this.child});

//   @override
//   State<LightningGlow> createState() => _LightningGlowState();
// }

// class _LightningGlowState extends State<LightningGlow>
//     with SingleTickerProviderStateMixin {
//   late AnimationController _controller;
//   late Animation<double> _scale;
//   late Animation<double> _opacity;

//   @override
//   void initState() {
//     super.initState();

//     _controller = AnimationController(
//       vsync: this,
//       duration: const Duration(milliseconds: 900),
//     )..repeat(reverse: true);

//     _scale = Tween<double>(begin: 1.0, end: 1.18).animate(
//       CurvedAnimation(parent: _controller, curve: Curves.easeInOut),
//     );

//     _opacity = Tween<double>(begin: 0.25, end: 0.8).animate(
//       CurvedAnimation(parent: _controller, curve: Curves.easeInOut),
//     );
//   }

//   @override
//   Widget build(BuildContext context) {
//     return AnimatedBuilder(
//       animation: _controller,
//       builder: (_, __) {
//         return Stack(
//           alignment: Alignment.center,
//           children: [
//             Opacity(
//               opacity: _opacity.value,
//               child: Transform.scale(
//                 scale: _scale.value,
//                 child: Container(
//                   width: 130,
//                   height: 130,
//                   decoration: const BoxDecoration(
//                     shape: BoxShape.circle,
//                     gradient: RadialGradient(
//                       colors: [
//                         Color(0xFF4CC9F0),
//                         Color(0xFF4361EE),
//                         Colors.transparent,
//                       ],
//                     ),
//                   ),
//                 ),
//               ),
//             ),
//             widget.child,
//           ],
//         );
//       },
//     );
//   }

//   @override
//   void dispose() {
//     _controller.dispose();
//     super.dispose();
//   }
// }
