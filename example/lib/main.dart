import 'package:aws_lambda/aws_lambda.dart';
import 'package:flutter/material.dart';
import 'dart:async';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {

  String _firstResult;
  int _secondResult;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  Future<void> initPlatformState() async {
    AWSLambda lambda = AWSLambda(
      'YOUR_IDENTITY_POOL_ID',
      region: 'us-east-1',
      cognitoRegion: 'us-east-1',
      clientConfiguration: <String, dynamic>{
        'ConnectionTimeout': 60000,
        'SocketTimeout': 60000
      }
    );
    Map<String, dynamic> result = await lambda.callLambda(
        'YOUR_LAMBDA_FUNCTION_NAME',
        <String, dynamic>{
          'YOUR_FIRST_PARAMETER_KEY': 1,
          'YOUR_SECOND_PARAMETER_KEY': 'two',
          'YOUR_THIRD_PARAMETER_KEY': 3.0,
        }
    );
    _firstResult = result['YOUR_FIRST_RESULT_PARAMETER_KEY'];
    _secondResult = result['YOUR_SECOND_RESULT_PARAMETER_KEY'];
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('aws_lambda plugin example app'),
        ),
        body: Center(
          child: Text('First result: ${_firstResult == null ? 'Waiting for result' : _firstResult}\nSecondResult: ${_secondResult == null ? 'Waiting for result' : _secondResult}'),
        ),
      ),
    );
  }
}
