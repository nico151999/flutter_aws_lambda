package de.nico.aws_lambda;

import android.content.Context;
import android.os.Handler;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Regions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import de.nico.aws_lambda.aws.FlutterAWSLambdaClient;
import de.nico.aws_lambda.aws.LambdaBinaryBinder;
import de.nico.aws_lambda.aws.LambdaFunctionBinaryInterface;
import de.nico.aws_lambda.aws.LambdaFunctionMapInterface;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** AwsLambdaPlugin */
public class AWSLambdaPlugin implements FlutterPlugin, MethodCallHandler {
  private static final String METHOD_CHANNEL_NAME = "de.nico.aws_lambda/lambda";

  private MethodChannel mChannel;
  private Context mContext;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    mContext = flutterPluginBinding.getApplicationContext();
    mChannel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), METHOD_CHANNEL_NAME);
    mChannel.setMethodCallHandler(this);
  }

  // This static function is optional and equivalent to onAttachedToEngine. It supports the old
  // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
  // plugin registration via this function while apps migrate to use the new Android APIs
  // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
  //
  // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
  // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
  // depending on the user's project. onAttachedToEngine or registerWith must both be defined
  // in the same class.
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), METHOD_CHANNEL_NAME);
    AWSLambdaPlugin plugin = new AWSLambdaPlugin();
    plugin.mContext = registrar.context();
    channel.setMethodCallHandler(plugin);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    try {
      Pair<Object, Map<String, Object>> arguments = parseArguments(call);
      boolean binaryMode = !(arguments.first instanceof Map);
      runFunction(call.method, arguments.first, arguments.second, binaryMode, result);
    } catch (Exception e) {
      result.error(
              e.getClass().getName(),
              e.getMessage(),
              stackTraceToString(e.getStackTrace())
      );
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    mChannel.setMethodCallHandler(null);
  }

  private Pair<Object, Map<String, Object>> parseArguments(MethodCall call) throws IllegalArgumentException {
    String preferencesKey = "preferences";
    String parametersKey = "parameters";
    Object arguments = call.arguments();
    if (!(arguments instanceof Map)) {
      throw new IllegalArgumentException("The passed arguments must be of type Map");
    }
    Map<String, Object> argumentsMap = (Map<String, Object>) arguments;
    if (!argumentsMap.containsKey(preferencesKey)) {
      throw new IllegalArgumentException("The passed argument");
    }
    Object preferences = argumentsMap.get(preferencesKey);
    if (!(preferences instanceof Map)) {
      throw new IllegalArgumentException("Preferences must be of type Map");
    }
    Map<String, Object> preferencesMap = (Map<String, Object>) preferences;
    Object parameters;
    if (argumentsMap.containsKey(parametersKey)) {
      parameters = argumentsMap.get(parametersKey);
      if (!(parameters instanceof Map || parameters instanceof byte[])) {
        throw new IllegalArgumentException("If passed, parameters must be of type Map or of type UInt8List");
      }
    } else {
      parameters = null;
    }
    return new Pair<>(parameters, preferencesMap);
  }

  private void runFunction(String functionName, Object parameters, Map<String, Object> preferences, final boolean binaryMode, final MethodChannel.Result result) throws NullPointerException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    String clientConfigurationKey = "clientConfiguration";
    Regions region = getRegion(preferences, "region");
    Regions cognitoRegion = getRegion(preferences, "cognitoRegion");

    CognitoCachingCredentialsProvider cognitoProvider = new CognitoCachingCredentialsProvider(
            mContext,
            (String) preferences.get("identityPoolId"),
            cognitoRegion
    );

    ClientConfiguration configuration = new ClientConfiguration();
    if (preferences.containsKey(clientConfigurationKey)) {
      Map<String, Object> configurations = (Map<String, Object>) preferences.get(clientConfigurationKey);
      for(Map.Entry<String, Object> entry : configurations.entrySet()) {
        String configurationName = entry.getKey();
        Object configurationValue = entry.getValue();
        Class<?> configurationType;
        try {
          configurationType = (Class<?>) configurationValue.getClass().getField("TYPE").get(null);
        } catch (NoSuchFieldException e) {
          configurationType = configurationValue.getClass();
        }
        Method setter = configuration.getClass().getDeclaredMethod(
                "set" + configurationName,
                configurationType
        );
        setter.invoke(configuration, configurationValue);
      }
    }

    FlutterAWSLambdaClient lambdaClient = new FlutterAWSLambdaClient(
            cognitoProvider,
            configuration,
            functionName
    );

    final LambdaInvokerFactory factory = LambdaInvokerFactory.builder()
            .context(mContext)
            .region(region)
            .lambdaClient(lambdaClient)
            .credentialsProvider(cognitoProvider)
            .clientConfiguration(configuration)
            .build();

    new Thread() {
      @Override
      public void run() {
        Handler mainHandler = new Handler(mContext.getMainLooper());
        Runnable runnable;
        try {
          Object res;
          if (binaryMode) {
            res = factory.build(
                    LambdaFunctionBinaryInterface.class,
                    new LambdaBinaryBinder()
            ).lambdaFunction((byte[]) parameters);
          } else {
            res = factory.build(LambdaFunctionMapInterface.class)
                    .lambdaFunction((Map<String, Object>) parameters);
          }
          runnable = () -> result.success(res);
        } catch (LambdaFunctionException lfe) {
          runnable = () -> result.error(
                  "EXCEPTION",
                  "The lambda operation failed",
                  lfe.getDetails()
          );
        } catch (AmazonServiceException ase) {
          runnable = () -> result.error(
                  "EXCEPTION",
                  "The lambda operation failed",
                  ase.getErrorMessage()
          );
        }
        mainHandler.post(runnable);
      }
    }.start();
  }

  private Regions getRegion(Map<String, Object> preferences, String regionKey) {
    try {
      if (preferences.containsKey(regionKey)) {
        return Regions.fromName((String) preferences.get(regionKey));
      } else {
        return Regions.DEFAULT_REGION;
      }
    } catch (IllegalArgumentException | NullPointerException e) {
      return Regions.DEFAULT_REGION;
    }
  }

  private String stackTraceToString(StackTraceElement[] elements) {
    StringBuilder stackTrace = new StringBuilder();
    for (StackTraceElement st : elements) {
      stackTrace.append(st.toString());
    }
    return stackTrace.toString();
  }
}
