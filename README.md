# aws_lambda

A flutter plugin made to allow the usage of aws lambdas via each platform's native aws lambda plugin.

This plugin offers a lightweight implementation of AWS's lambda service library into your Flutter
project. It allows the same configurations that AWS's library also does. Every time you call a
lambda function in Dart your parameters are simply passed to the platform's native AWS lambda library.

## Getting Started

Just add the project to your pubspec.yaml file, get an instance of the AWSLambda class providing
your credentials to the class's constructor and call the callLambda method passing at least the
lambda function's name. For more details have a look at the provided example..