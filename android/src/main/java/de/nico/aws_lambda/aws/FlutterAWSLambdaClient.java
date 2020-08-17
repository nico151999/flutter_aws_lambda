package de.nico.aws_lambda.aws;

import com.amazonaws.AmazonClientException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;

public class FlutterAWSLambdaClient extends AWSLambdaClient {
    private String functionName;

    public FlutterAWSLambdaClient(AWSCredentialsProvider awsCredentials, ClientConfiguration clientConfiguration, String functionName) {
        super(awsCredentials, clientConfiguration);
        this.functionName = functionName;
    }

    @Override
    public InvokeResult invoke(InvokeRequest invokeRequest) throws AmazonClientException {
        if (invokeRequest.getFunctionName().equals(LambdaFunctionInterface.FLUTTER_FUNCTION_NAME)) {
            invokeRequest.setFunctionName(functionName);
        }
        return super.invoke(invokeRequest);
    }
}
