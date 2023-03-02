package com.dialavet.dialavet;

import androidx.annotation.NonNull;

import io.flutter.Log;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

public class MainActivity extends FlutterActivity {

    private static final String TAG = "MyFirebaseService";
    private static final String CHANNEL = "dataFromFlutterChannel";
    public static MethodChannel METHOD_CHANNEL;

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        Log.d("FlutterFragmentActivity","configureFlutterEngine");
        METHOD_CHANNEL = new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL);
        new SetChannel().setMETHOD_CHANNEL(METHOD_CHANNEL);
    }
}

class SetChannel {
    public static MethodChannel METHOD_CHANNEL;

    public void setMETHOD_CHANNEL(MethodChannel METHOD_CHANNEL) {
        SetChannel.METHOD_CHANNEL = METHOD_CHANNEL;
    }
    public MethodChannel getMETHOD_CHANNEL() {
        return METHOD_CHANNEL;
    }
}