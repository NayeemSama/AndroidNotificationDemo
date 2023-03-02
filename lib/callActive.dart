import 'package:flutter/material.dart';

class CallActive extends StatelessWidget {
  const CallActive({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      body: Center(
        child: Text('Call Active', style: TextStyle(fontSize: 32, color: Colors.black, fontWeight: FontWeight.bold)),
      ),
    );
  }
}
