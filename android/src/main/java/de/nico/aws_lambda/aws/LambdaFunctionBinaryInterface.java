package de.nico.aws_lambda.aws;

import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunction;

public interface LambdaFunctionBinaryInterface extends LambdaFunctionInterface {
    @LambdaFunction(functionName = FLUTTER_FUNCTION_NAME)
    byte[] lambdaFunction(byte[] request);
}
