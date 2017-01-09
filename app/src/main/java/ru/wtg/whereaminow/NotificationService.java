package ru.wtg.whereaminow;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created 1/8/17.
 */

public class NotificationService extends FirebaseMessagingService {

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        System.out.println("REMOTEMESSAGE:"+remoteMessage);

    }

}