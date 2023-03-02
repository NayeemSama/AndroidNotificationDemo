import 'package:awesome_notifications/awesome_notifications.dart';
import 'package:dialavet/callActive.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_callkit_incoming/entities/android_params.dart';
import 'package:flutter_callkit_incoming/entities/call_event.dart';
import 'package:flutter_callkit_incoming/entities/call_kit_params.dart';
import 'package:flutter_callkit_incoming/entities/ios_params.dart';
import 'package:flutter_callkit_incoming/flutter_callkit_incoming.dart';
import 'package:onesignal_flutter/onesignal_flutter.dart';
import 'package:uuid/uuid.dart';

@pragma('vm:entry-point')
Future<void> _firebaseMessagingBackgroundHandler(RemoteMessage message) async {
  print('_firebaseMessagingBackgroundHandler flutter');
  await AwesomeNotifications().createNotification(
    content: NotificationContent(
      id: 123456,
      channelKey: 'calling',
      title: 'Caller Name',
      category: NotificationCategory.Call,
      body: "Caller name is calling",
      wakeUpScreen: true,
      locked: true,
      fullScreenIntent: true,
      payload: {'notificationId': '1234567890'},
    ),
    actionButtons: [
      NotificationActionButton(key: 'declineKey', label: 'Decline', color: Colors.red),
      NotificationActionButton(key: 'acceptKey', label: 'Accept', color: Colors.green),
    ],
  );
}

final GlobalKey<NavigatorState> navigatorKey = new GlobalKey<NavigatorState>();

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp();
  runApp(const MyApp());
}

var token = "";

Future<void> showCallkitIncoming(String uuid, String? title, String? message, String? callRequestId) async {
  var params = CallKitParams(
      id: uuid,
      nameCaller: title,
      //TITLE
      appName: 'Callkit',
      avatar: null,
      handle: message,
      //MESSAGE
      type: 1,
      duration: 30000,
      textAccept: 'Accept',
      textDecline: 'Decline',
      textMissedCall: 'Missed call',
      textCallback: 'Call back',
      extra: <String, dynamic>{'callRequestId': callRequestId},
      headers: <String, dynamic>{'apiKey': 'abc@123!', 'platform': 'flutter'},
      android: AndroidParams(
          isCustomNotification: true,
          isShowLogo: false,
          isShowCallback: false,
          isShowMissedCallNotification: false,
          ringtonePath: 'resource://raw/notification_tone',
          backgroundColor: '#59EBAF',
          backgroundUrl: null,
          actionColor: '#4CAF50'),
      ios: IOSParams(
          iconName: 'CallKitLogo',
          handleType: '',
          supportsVideo: true,
          maximumCallGroups: 1,
          maximumCallsPerCallGroup: 1,
          audioSessionMode: 'default',
          audioSessionActive: true,
          audioSessionPreferredSampleRate: 44100.0,
          audioSessionPreferredIOBufferDuration: 0.005,
          supportsDTMF: true,
          supportsHolding: false,
          supportsGrouping: false,
          supportsUngrouping: false,
          ringtonePath: 'notification_tone'));
  await FlutterCallkitIncoming.showCallkitIncoming(params);
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  late final FirebaseMessaging _firebaseMessaging;
  final channelName = 'dataFromFlutterChannel';
  var methodChannel;
  var _initialized;

  BuildContext? widgetContext;

  initFirebase() async {
    await Firebase.initializeApp();
    _firebaseMessaging = FirebaseMessaging.instance;
    FirebaseMessaging.onBackgroundMessage(_firebaseMessagingBackgroundHandler);
    FirebaseMessaging.onMessage.listen((RemoteMessage message) async {
      print(
          'Message title: ${message.notification?.title}, body: ${message.notification?.body}, data: ${message.data}');
      print('Foreground Message ${message.toMap()}');
    });

    await _firebaseMessaging.getToken().then((token) {
      print('Device Token FCM: $token');
    });
    await _firebaseMessaging.getAPNSToken().then((token) {
      print('Device Token APNS: $token');
    });
  }

  initOneSignal() async {
    OneSignal.shared.setLogLevel(OSLogLevel.none, OSLogLevel.none);

    OneSignal.shared.setAppId("5364bd8a-6832-410c-9f14-d10bccb5c0c9");

    final status = await OneSignal.shared.getDeviceState();
    const externalUserId = '512';

    OneSignal.shared.setExternalUserId(externalUserId).then((results) {
      print('setExternalUserId - ${results.toString()}');
    }).catchError((error) {
      print('setExternalUserId - ${error.toString()}');
    });

    OneSignal.shared.promptUserForPushNotificationPermission().then((accepted) {
      print("Accepted permission: ${accepted}");
      print('promptUserForPushNotificationPermission');
    });

    OneSignal.shared.setNotificationWillShowInForegroundHandler((OSNotificationReceivedEvent event) {
      // Will be called whenever a notification is received in foreground
      // Display Notification, pass null param for not displaying the notification

      print('setNotificationWillShowInForegroundHandler');
      // event.complete(event.notification);
    });

    OneSignal.shared.setNotificationOpenedHandler((OSNotificationOpenedResult result) {
      // Will be called whenever a notification is opened/button pressed.
      print("SetNotificationOpenedHandler: ");
    });

    OneSignal.shared.setPermissionObserver((OSPermissionStateChanges changes) {
      // Will be called whenever the permission changes
      // (ie. user taps Allow on the permission prompt in iOS)
      print("SetPermissionObserver: ");
    });

    OneSignal.shared.setSubscriptionObserver((OSSubscriptionStateChanges changes) {
      // Will be called whenever the subscription changes
      // (ie. user gets registered with OneSignal and gets a user ID)
      print("SetSubscriptionObserver: ");
    });

    OneSignal.shared.setEmailSubscriptionObserver((OSEmailSubscriptionStateChanges emailChanges) {
      // Will be called whenever then user's email subscription changes
      // (ie. OneSignal.setEmail(email) is called and the user gets registered
      print("SetEmailSubscriptionObserver: ");
    });

    final String? osUserID = status?.userId;
    final String? osUserIDs = status?.pushToken;
    print("One User ID ==> $osUserID");
    print("One User ID ==> $osUserIDs");
  }

  @pragma('vm:entry-point')
  Future<void> _handleMethod(MethodCall call) async {
    print('Fluttet _handleMethod called !!!!!!!!!!!!!!!!!!!!!');
    showCallkitIncoming(Uuid().v4(), 'title', 'message', '1234');
  }

  @override
  void initState() {
    methodChannel = MethodChannel(channelName);
    methodChannel.setMethodCallHandler(this._handleMethod);
    widgetContext = context;
    initFirebase();
    initOneSignal();
    initAwesome();
    listenerEvent(onEvent);
    super.initState();
  }

  Future<void> listenerEvent(Function? callback) async {
    try {
      FlutterCallkitIncoming.onEvent.listen((event) async {
        if (kDebugMode) {
          print('HOME: $event');
        }
        switch (event!.event) {
          case Event.ACTION_CALL_INCOMING:
            break;
          case Event.ACTION_CALL_START:
            break;
          case Event.ACTION_CALL_ACCEPT:
            print("ACTION_CALL_ACCEPT");
            // Navigator.push(
            //   context,
            //   MaterialPageRoute(builder: (context) => const CallActive()),
            // );
            navigatorKey.currentState!.push(MaterialPageRoute(builder: (context) => const CallActive()));

            break;
          case Event.ACTION_CALL_DECLINE:
            break;
          case Event.ACTION_CALL_ENDED:
            break;
          case Event.ACTION_CALL_TIMEOUT:
            break;
          case Event.ACTION_CALL_CALLBACK:
            break;
          case Event.ACTION_CALL_TOGGLE_HOLD:
            break;
          case Event.ACTION_CALL_TOGGLE_MUTE:
            break;
          case Event.ACTION_CALL_TOGGLE_DMTF:
            break;
          case Event.ACTION_CALL_TOGGLE_GROUP:
            break;
          case Event.ACTION_CALL_TOGGLE_AUDIO_SESSION:
            break;
          case Event.ACTION_DID_UPDATE_DEVICE_PUSH_TOKEN_VOIP:
            break;
        }
        if (callback != null) {
          callback(event.toString());
        }
      });
    } on Exception {
      if (kDebugMode) {
        print('!!!!========Exception========!!!!');
      }
    }
  }

  onEvent(event) {
    if (!mounted) return;
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      navigatorKey: navigatorKey,
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: const MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }

  void initAwesome() async {
    AwesomeNotifications().initialize(
      null,
      [
        NotificationChannel(
          channelKey: 'calling',
          channelName: 'Call Channel',
          channelDescription: 'Notification channel for basic tests',
          importance: NotificationImportance.Max,
          defaultPrivacy: NotificationPrivacy.Public,
          defaultRingtoneType: DefaultRingtoneType.Ringtone,
        )
      ],
      // Channel groups are only visual and are not required
      channelGroups: [
        NotificationChannelGroup(
          channelGroupKey: 'basic_channel_group',
          channelGroupName: 'Basic group',
        )
      ],
    );

    await AwesomeNotifications().requestPermissionToSendNotifications(
        channelKey: 'calling',
        permissions: [
          NotificationPermission.Alert,
          NotificationPermission.Sound,
          NotificationPermission.Vibration,
        ],
    );

    AwesomeNotifications().setListeners(
      onActionReceivedMethod: NotificationController.onActionReceivedMethod,
      onNotificationCreatedMethod: NotificationController.onNotificationCreatedMethod,
      onNotificationDisplayedMethod: NotificationController.onNotificationDisplayedMethod,
      onDismissActionReceivedMethod: NotificationController.onDismissActionReceivedMethod,
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {

  TextEditingController editingController = TextEditingController();

  @override
  void initState() {
    // TODO: implement initState
    super.initState();
    Future.delayed(Duration(seconds: 10),(){
      setState(() {
        OneSignal.shared.getDeviceState().then((value) {
          token = value!.userId!;
          print('token - $token');
          editingController.text = token;
        });
      });
    });
  }

  int _counter = 0;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            const Text(
              'You have pushed the button this many times:',
            ),
            TextFormField(
              controller: editingController,
            ),
            Text(token),
            Text(
              '$_counter',
              style: Theme.of(context).textTheme.headline4,
            ),
          ],
        ),
      ),
    );
  }
}

class NotificationController {
  /// Use this method to detect when a new notification or a schedule is created
  @pragma("vm:entry-point")
  static Future<void> onNotificationCreatedMethod(ReceivedNotification receivedNotification) async {
    // Your code goes here
  }

  /// Use this method to detect every time that a new notification is displayed
  @pragma("vm:entry-point")
  static Future<void> onNotificationDisplayedMethod(ReceivedNotification receivedNotification) async {
    // Your code goes here
  }

  /// Use this method to detect if the user dismissed a notification
  @pragma("vm:entry-point")
  static Future<void> onDismissActionReceivedMethod(ReceivedAction receivedAction) async {
    // Your code goes here
  }

  /// Use this method to detect when the user taps on a notification or action button
  @pragma("vm:entry-point")
  static Future<void> onActionReceivedMethod(ReceivedAction receivedAction) async {
    // Navigate into pages, avoiding to open the notification details page over another details page already opened
    // MyApp.navigatorKey.currentState?.pushNamedAndRemoveUntil('/notification-page',
    //         (route) => (route.settings.name != '/notification-page') || route.isFirst,
    //     arguments: receivedAction);

    print('onActionReceivedMethod ');
    print('onActionReceivedMethod ${receivedAction.buttonKeyInput}');
    print('onActionReceivedMethod ${receivedAction.buttonKeyPressed}');
    print('onActionReceivedMethod ${receivedAction.actionType}');
    if(receivedAction.actionType==ActionType.Default){
      if(receivedAction.buttonKeyPressed=="acceptKey"){
        navigatorKey.currentState!.push(MaterialPageRoute(builder: (context) => const CallActive()));
      } else if(receivedAction.buttonKeyPressed=="declineKey"){

      }
    }

  }
}
