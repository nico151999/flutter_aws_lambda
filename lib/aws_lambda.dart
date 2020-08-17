import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/services.dart';

class AWSLambda {
  static const MethodChannel platform = const MethodChannel(
      'de.nico.aws_lambda/lambda');
  String identityPoolId;
  String region;
  String cognitoRegion;
  Map<String, dynamic> clientConfiguration;

  /// Parameters:
  /// * [identityPoolId] the identity-pool-id provided by AWS as a [String].
  /// * [region] the region the lambda should be executed in as a [String].
  ///   The default region will be used if no region is provided.
  /// * [cognitoRegion] the cognito region that should be used as a [String].
  ///   The default region will be used if no cognitoRegion is provided.
  /// * [clientConfiguration] a [Map] with the AWS configurator's setter method
  ///   without the set-prefix as Map-key and the related value as Map-value
  AWSLambda(this.identityPoolId,
      {this.region, this.cognitoRegion, this.clientConfiguration});

  /// Parameters:
  /// * [functionName] is the name of the lambda that is to be called
  /// * [parameters] is a variable that either has to be of type [Uint8List]
  ///   or [Map] depending on what the lambda function takes as an input
  ///   parameter. If the lambd function takes no input parameter at all, just
  ///   do not pass [parameters].
  /// Return:
  /// * If [callLambda] is called with a [Uint8List] as [parameters] value the
  ///   lambda function is called with a byte array parameter and expected to
  ///   return a byte array as well which will cause [callLambda] to return
  ///   a [Uint8List].
  /// * If [callLambda] is called with a [Map] as [parameters] value the
  ///   lambda function is called with a json parameter and expected to return
  ///   a json as well which will cause [callLambda] to return a [Uint8List].
  Future<dynamic> callLambda(String functionName, [dynamic parameters]) async {
    Map<String, dynamic> preferences = {'identityPoolId': identityPoolId};
    ({
      'region': region,
      'cognitoRegion': cognitoRegion,
      'clientConfiguration': clientConfiguration
    }).forEach((key, value) {
      if (value != null) {
        preferences[key] = value;
      }
    });
    Map<String, dynamic> pass = {'preferences': preferences};
    if (parameters != null) {
      if (parameters is Map<String, dynamic> || parameters is Uint8List) {
        pass['parameters'] = parameters;
      } else {
        throw FormatException("Unsupported parameter type");
      }
    }
    return await platform.invokeMethod(functionName, pass);
  }
}