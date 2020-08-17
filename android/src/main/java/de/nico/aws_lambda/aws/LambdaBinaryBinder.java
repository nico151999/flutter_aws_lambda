package de.nico.aws_lambda.aws;

import com.amazonaws.mobileconnectors.lambdainvoker.LambdaDataBinder;

public class LambdaBinaryBinder implements LambdaDataBinder {
    @Override
    public <T> T deserialize(byte[] content, Class<T> clazz) {
        if (!clazz.equals(byte[].class)) {
            throw new IllegalArgumentException(this.getClass().getName() + " can only handle byte arrays");
        }
        return (T) content;
    }

    @Override
    public byte[] serialize(Object object) {
        if (object != null && !(object instanceof byte[])) {
            throw new IllegalArgumentException(this.getClass().getName() + " can only handle byte arrays");
        }
        return (byte[]) object;
    }
}
