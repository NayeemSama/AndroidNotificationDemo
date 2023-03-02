package com.dialavet.dialavet;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import com.hiennv.flutter_callkit_incoming.CallkitNotificationManager;
import com.hiennv.flutter_callkit_incoming.Data;
import com.hiennv.flutter_callkit_incoming.FlutterCallkitIncomingPlugin;
import com.onesignal.OSNotification;
import com.onesignal.OSMutableNotification;
import com.onesignal.OSNotificationReceivedEvent;
import com.onesignal.OneSignal.OSRemoteNotificationReceivedHandler;

import java.util.HashMap;
import java.util.Iterator;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class NotificationServiceExtension implements FlutterPlugin, MethodChannel.MethodCallHandler, OSRemoteNotificationReceivedHandler {

    private MethodChannel channel;

    private Context context;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        Log.d("Android AttachedEngine", "----");
        channel = new MethodChannel(binding.getBinaryMessenger(), "dataFromFlutterChannel");
        channel.setMethodCallHandler(this);
//        context = binding.getApplicationContext();
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
    public void remoteNotificationReceived(Context context, OSNotificationReceivedEvent notificationReceivedEvent) {
        setChannel();
        Log.d("Android OSReceived", "----");
//        OSNotification notification = notificationReceivedEvent.getNotification();

        // Example of modifying the notification's accent color
//        OSMutableNotification mutableNotification = notification.mutableCopy();
//        mutableNotification.setExtender(builder -> {
//            // Sets the accent color to Green on Android 5+ devices.
//            // Accent color controls icon and action buttons on Android 5+. Accent color does not change app title on Android 10+
//            builder.setColor(new BigInteger("FF00FF00", 16).intValue());
//            // Sets the notification Title to Red
//            Spannable spannableTitle = new SpannableString(notification.getTitle());
//            spannableTitle.setSpan(new ForegroundColorSpan(Color.RED),0,notification.getTitle().length(),0);
//            builder.setContentTitle(spannableTitle);
//            // Sets the notification Body to Blue
//            Spannable spannableBody = new SpannableString(notification.getBody());
//            spannableBody.setSpan(new ForegroundColorSpan(Color.BLUE),0,notification.getBody().length(),0);
//            builder.setContentText(spannableBody);
//            //Force remove push from Notification Center after 30 seconds
//            builder.setTimeoutAfter(30000);
//            return builder;
//        });

        CallkitNotificationManager callkitNotificationManager = new CallkitNotificationManager(context);

        OSNotification notification = notificationReceivedEvent.getNotification();
        JSONObject data = notification.getAdditionalData();
        OSMutableNotification mutableNotification = notification.mutableCopy();
        mutableNotification.setExtender(builder -> builder.setColor(context.getResources().getColor(R.color.accept)));

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            try {
//                Log.d("try", "----");
//                data.append("nameCaller","Johnny");
//            } catch (JSONException e) {
//                Log.d("catch", "----");
//
//                throw new RuntimeException(e);
//            }
//        }
//        notificationReceivedEvent.complete(mutableNotification);

        ///CALLKIT
        Bundle bundle = new Bundle();
        Iterator iter = data.keys();
        while(iter.hasNext()) {
            String key = (String)iter.next();
            String value = null;
            try {
                value = data.getString(key);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            bundle.putString(key,value);
        }

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("id","12345613245");
        map.put("nameCaller","nameCaller");
        map.put("handle","handle");
        map.put("type","1");
        Data callData = new Data(map);

//        FlutterCallkitIncomingPlugin.Companion.getInstance().showIncomingNotification(callData);
//        callkitNotificationManager.showIncomingNotification(bundle);

        ///Flutter Invoke
//        new Handler(Looper.getMainLooper()).post(new Runnable() {
//            @Override
//            public void run() {
//                channel.invokeMethod("forAndroidNative", "CallRequestId");
//            }
//        });

        Log.d("OneSignalExample", "Received Notification Data ----");
    }

    public void setChannel() {
//        this.channel = channel;
        new SetChannel();
        channel = SetChannel.METHOD_CHANNEL;
    }
}