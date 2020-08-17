package de.nico.aws_lambda.aws;

import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunction;

import java.util.Map;

public interface LambdaFunctionMapInterface extends LambdaFunctionInterface {
    @LambdaFunction(functionName = FLUTTER_FUNCTION_NAME)
    Map<String, Object> lambdaFunction(Map<String, Object> request);
}