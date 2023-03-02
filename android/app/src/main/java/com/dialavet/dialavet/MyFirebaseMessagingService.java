package com.dialavet.dialavet;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.dialavet.dialavet.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.hiennv.flutter_callkit_incoming.CallkitNotificationManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import io.flutter.Log;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class MyFirebaseMessagingService extends FirebaseMessagingService implements FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware, EventChannel.StreamHandler {

    private MethodChannel channel;
    private String CallRequestId = "";
    private Context context;

    @SuppressLint("WrongThread")
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        setChannel();
        super.onMessageReceived(remoteMessage);
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        Map<String, String> data = remoteMessage.getData();
        CallRequestId = remoteMessage.getData().get("call_request_id");

        Log.d("TAG", "From NATIVE =================================>>>>>>>>>>>>>>>: " + remoteMessage);

//        if (remoteMessage.getData().size() > 0) {
//            Log.d("TAG", "Message data payload: " + remoteMessage.getData());
//        }
//
//        // Check if message contains a notification payload.
//        if (remoteMessage.getNotification() != null) {
//            Log.d("TAG", "Message Notification Body: " + remoteMessage.getNotification().getBody());
//        }

        ///Flutter Invoke
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                channel.invokeMethod("forAndroidNative", CallRequestId);
            }
        });

        ///CALLKIT
//        CallkitNotificationManager callkitNotificationManager = new CallkitNotificationManager(context);
//        Bundle bundle = new Bundle();
//        JSONObject bundleData = (JSONObject) remoteMessage.getData();
//        Iterator iter = bundleData.keys();
//        while(iter.hasNext()) {
//            String key = (String)iter.next();
//            String value = null;
//            try {
//                value = bundleData.getString(key);
//            } catch (JSONException e) {
//                throw new RuntimeException(e);
//            }
//            bundle.putString(key,value);
//        }
//        callkitNotificationManager.showIncomingNotification(bundle);

//        sendNotification(notification, data);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("onNewToken ===>", token);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void sendNotification(RemoteMessage.Notification notification, Map<String, String> data) {
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "channel_id")
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getBody())
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setContentInfo(notification.getTitle())
                .setLargeIcon(icon)
                .setColor(Color.RED)
                .setLights(Color.RED, 1000, 300)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setSmallIcon(R.mipmap.ic_launcher);

        try {
            String picture_url = data.get("picture_url");
            if (picture_url != null && !"".equals(picture_url)) {
                URL url = new URL(picture_url);
                Bitmap bigPicture = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                notificationBuilder.setStyle(
                        new NotificationCompat.BigPictureStyle().bigPicture(bigPicture).setSummaryText(notification.getBody())
                );
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Notification Channel is required for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "channel_id", "channel_name", NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("channel description");
            channel.setShowBadge(true);
            channel.canShowBadge();
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        Log.d("Android AttachedEngine", "----");
        channel = new MethodChannel(binding.getBinaryMessenger(), "dataFromFlutterChannel");
        channel.setMethodCallHandler(this);
    }


    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        Log.d("Android DetachedEngine", "----");
        channel.setMethodCallHandler(null);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        Log.d("Android onMethodCall", "----");
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        Log.d("Android onAttachedToActivity", "----");
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        Log.d("Android onDetachedFromActivityForConfigChanges", "----");

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        Log.d("Android onReattachedToActivityForConfigChanges", "----");

    }

    @Override
    public void onDetachedFromActivity() {
        Log.d("Android onDetachedFromActivity", "----");
    }

    @Override
    public void onListen(Object arguments, EventChannel.EventSink events) {

    }

    @Override
    public void onCancel(Object arguments) {

    }

    public void setChannel() {
//        this.channel = channel;
        new SetChannel();
        channel = SetChannel.METHOD_CHANNEL;
    }
}

//package com.dialavet.dialavet;
//
//import android.content.Context;
//import android.util.Log;
//
//import androidx.annotation.NonNull;
//
//import io.flutter.embedding.engine.plugins.FlutterPlugin;
//import io.flutter.embedding.engine.plugins.activity.ActivityAware;
//import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
//import io.flutter.plugin.common.BinaryMessenger;
//import io.flutter.plugin.common.EventChannel;
//import io.flutter.plugin.common.MethodCall;
//import io.flutter.plugin.common.MethodChannel;
//import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
//import io.flutter.plugin.common.MethodChannel.Result;
//import io.flutter.plugin.common.PluginRegistry;
//
//
//public class MyFirebaseMessagingService implements FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler, ActivityAware {
//
//    private static final String TAG = "MyFirebaseService";
//    private static final String CHANNEL = "dataFromFlutterChannel";
//    private static final String EVENTS = "dataFromFlutterChannelEvent";
//    private Result parentResult;
//    private Context mContext;
//    private EventChannel eventChannel;
//    private MethodChannel methodChannel;
//    private EventChannel.EventSink events;
//
//    public void registerWith(PluginRegistry.Registrar registrar) {
//        Log.d(TAG, "registerWith");
//        whenAttachedToEngine(registrar.context(), registrar.messenger());
//    }
//
//    @Override
//    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
//        Log.d(TAG, "onAttachedToEngine");
//        whenAttachedToEngine(flutterPluginBinding.getApplicationContext(), flutterPluginBinding.getBinaryMessenger());
//    }
//
//    private void whenAttachedToEngine(Context applicationContext, BinaryMessenger messenger) {
//        this.mContext = applicationContext;
//        methodChannel = new MethodChannel(messenger, CHANNEL);
//        eventChannel = new EventChannel(messenger, EVENTS);
//        eventChannel.setStreamHandler(this);
//        methodChannel.setMethodCallHandler(this);
//        Log.d(TAG, "whenAttachedToEngine");
//    }
//
//
//    @Override
//    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
//        mContext = null;
//        methodChannel.setMethodCallHandler(null);
//        methodChannel = null;
//        eventChannel.setStreamHandler(null);
//        eventChannel = null;
//        Log.d(TAG, "onDetachedFromEngine");
//    }
//
//    @Override
//    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
//        Log.d(TAG, "onMethodCall");
//    }
//
//    @Override
//    public void onListen(Object arguments, EventChannel.EventSink events) {
//        this.events = events;
//    }
//
//    @Override
//    public void onCancel(Object arguments) {
//        invalidateEventSink();
//    }
//
//    private void invalidateEventSink() {
//        if (events != null) {
//            events.endOfStream();
//            events = null;
//        }
//    }
//
//    @Override
//    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
//        Log.d(TAG, "onAttachedToActivity");
//    }
//
//    @Override
//    public void onDetachedFromActivityForConfigChanges() {
//        Log.d(TAG, "onDetachedFromActivityForConfigChanges");
//    }
//
//    @Override
//    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
//        Log.d(TAG, "onReattachedToActivityForConfigChanges");
//    }
//
//    @Override
//    public void onDetachedFromActivity() {
//        Log.d(TAG, "onDetachedFromActivity");
//    }
//}


//import android.content.Context;
//import android.view.KeyEvent;
//
//import androidx.annotation.NonNull;
//
//import io.flutter.Log;
//import io.flutter.embedding.engine.plugins.FlutterPlugin;
//import io.flutter.embedding.engine.plugins.activity.ActivityAware;
//import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
//import io.flutter.plugin.common.MethodCall;
//import io.flutter.plugin.common.MethodChannel;
//
//public class MyFirebaseMessagingService implements FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware {
//
//    private ActivityPluginBinding _activityBinding;
//    private FlutterPluginBinding _flutterBinding;
//    private MethodChannel _channel;
//
//    private static final String TAG = "MyFirebaseService";
//    private static final String CHANNEL = "dataFromFlutterChannel";
//    private static final String EVENTS = "dataFromFlutterChannelEvent";
//
//
//    @Override
//    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
//        _flutterBinding = flutterPluginBinding;
//        _channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "com.example.test/myplugin");
//        _channel.setMethodCallHandler(this);
//        Log.d(TAG, "onAttachedToEngine");
//    }
//
//    @Override
//    public void onDetachedFromEngine(@NonNull FlutterPlugin.FlutterPluginBinding binding) {
//        _channel.setMethodCallHandler(null);
//        _channel = null;
//        _flutterBinding = null;
//        Log.d(TAG, "onDetachedFromEngine");
//
//    }
//
//    // ActivityAware  overrides
//
//    @Override
//    public void onAttachedToActivity(ActivityPluginBinding binding) {
//        _activityBinding = binding;
//        Log.d(TAG, "onAttachedToActivity");
//
//    }
//
//    @Override
//    public void   onDetachedFromActivity() {
//        _activityBinding = null;
//        Log.d(TAG, "onDetachedFromActivity");
//
//    }
//
//    @Override
//    public void   onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {
//        _activityBinding = binding;
//        Log.d(TAG, "onReattachedToActivityForConfigChanges");
//
//
//    }
//
//    @Override
//    public void   onDetachedFromActivityForConfigChanges() {
//        _activityBinding = null;
//        Log.d(TAG, "onDetachedFromActivityForConfigChanges");
//
//    }
//
//    // MethodCallHandler overrides
//
//    @Override
//    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
//        // Handle calls
//        Log.d(TAG, "onMethodCall");
//
//    }
//
//    // Implementation
//
//    public Context getApplicationContext() {
//        return (_flutterBinding != null) ? _flutterBinding.getApplicationContext() : null;
//    }
//
//    public KeyEvent.Callback getActivity() {
//        return (_activityBinding != null) ? _activityBinding.getActivity() : null;
//    }
//}



//import android.app.Activity;
//
//import androidx.annotation.NonNull;
//
//import com.google.firebase.messaging.FirebaseMessagingService;
//import com.google.firebase.messaging.RemoteMessage;
//
//import io.flutter.Log;
//import io.flutter.embedding.engine.plugins.FlutterPlugin;
//import io.flutter.plugin.common.MethodCall;
//import io.flutter.plugin.common.MethodChannel;
//import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
//import io.flutter.plugin.common.MethodChannel.Result;
//import io.flutter.embedding.engine.plugins.activity.ActivityAware;
//import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
//
//public class MyFirebaseMessagingService extends FirebaseMessagingService implements FlutterPlugin, MethodCallHandler, ActivityAware {
//
//    private MethodChannel channel;
//    private Activity activity;
//    private static final String TAG = "MyFirebaseService";
//
//
//    @Override
//    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
//        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_custom_plugin");
//        channel.setMethodCallHandler(this);
////        channel.setMethodCallHandler(new FlutterBlufiPlugin(flutterPluginBinding.activity()));
//        Log.d(TAG, "onAttachedToEngine");
//    }
//
//    @Override
//    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
//        Log.d(TAG, "onDetachedFromEngine");
//
//    }
//
//    @Override
//    public void onAttachedToActivity(ActivityPluginBinding activityPluginBinding) {
//        // TODO: your plugin is now attached to an Activity
//        this.activity = activityPluginBinding.getActivity();
//        Log.d(TAG, "onAttachedToActivity");
//
//    }
//
//    @Override
//    public void onDetachedFromActivityForConfigChanges() {
//        // TODO: the Activity your plugin was attached to was destroyed to change configuration.
//        // This call will be followed by onReattachedToActivityForConfigChanges().
//        Log.d(TAG, "onDetachedFromActivityForConfigChanges");
//
//    }
//
//    @Override
//    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding activityPluginBinding) {
//        // TODO: your plugin is now attached to a new Activity after a configuration change.
//        Log.d(TAG, "onReattachedToActivityForConfigChanges");
//
//    }
//
//    @Override
//    public void onDetachedFromActivity() {
//        // TODO: your plugin is no longer associated with an Activity. Clean up references.
//        Log.d(TAG, "onDetachedFromActivity");
//
//    }
//
//    @Override
//    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
//        Log.d(TAG, "onMethodCall");
//    }
//
//    public void setChannel(MethodChannel channel) {
//        this.channel = channel;
//    }
//
//    @Override
//    public void onMessageReceived(@NonNull RemoteMessage message) {
//        super.onMessageReceived(message);
//        Log.d("","");
//    }
//}