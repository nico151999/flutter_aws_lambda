import 'package:flutter_test/flutter_test.dart';
import 'package:aws_lambda/aws_lambda.dart';

void main() {

  TestWidgetsFlutterBinding.ensureInitialized();

  test('Is method channel name correct', () async {
    expect(AWSLambda.platform.name, 'de.nico.aws_lambda/lambda');
  });
}
