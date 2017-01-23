package ru.wtg.whereaminow.holders;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v7.app.NotificationCompat;

import java.util.Date;

import ru.wtg.whereaminow.MainActivity;
import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.WhereAmINowService;
import ru.wtg.whereaminow.helpers.MyUser;

import static android.support.v4.app.NotificationCompat.DEFAULT_ALL;
import static android.support.v4.app.NotificationCompat.DEFAULT_LIGHTS;
import static android.support.v4.app.NotificationCompat.PRIORITY_DEFAULT;
import static android.support.v4.app.NotificationCompat.PRIORITY_HIGH;
import static ru.wtg.whereaminow.State.EVENTS.ACTIVITY_PAUSE;
import static ru.wtg.whereaminow.State.EVENTS.ACTIVITY_RESUME;
import static ru.wtg.whereaminow.State.EVENTS.MOVING_AWAY_FROM;
import static ru.wtg.whereaminow.State.EVENTS.MOVING_CLOSE_TO;
import static ru.wtg.whereaminow.State.EVENTS.TRACKING_ACTIVE;
import static ru.wtg.whereaminow.State.EVENTS.TRACKING_DISABLED;
import static ru.wtg.whereaminow.State.EVENTS.TRACKING_JOIN;
import static ru.wtg.whereaminow.State.EVENTS.TRACKING_NEW;
import static ru.wtg.whereaminow.State.EVENTS.TRACKING_RECONNECTING;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_DISMISSED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_JOINED;

/**
 * Created 11/29/16.
 */
public class NotificationHolder extends AbstractPropertyHolder {

    public static final String TYPE = "notification";
    public static final String SHOW_CUSTOM_NOTIFICATION = "show_custom_notification";
    public static final String HIDE_CUSTOM_NOTIFICATION = "hide_custom_notification";

    private static final int MIN_INTERVAL_BETWEEN_DISTANCE_NOTIFICATIONS = 300;
    private static final int DELAY_BEFORE_CLEAR_NOTIFICATION = 10;

    private final State state;

    private android.support.v4.app.NotificationCompat.Builder notification;
    private Handler notificationClearHandler;

    private long lastCloseNotifyTime = 0;
    private long lastAwayNotifyTime = 0;
    private boolean showNotifications = true;

    private Runnable notificationClearRunnable = new Runnable() {
        @Override
        public void run() {
            update("", DEFAULT_LIGHTS, PRIORITY_DEFAULT);
        }
    };

    public NotificationHolder(State state) {
        this.state = state;

        Intent notificationIntent = new Intent(state, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(state, 0, notificationIntent, 0);
        PendingIntent pendingStopIntent = PendingIntent.getService(state, (int) System.currentTimeMillis(), new Intent(state, WhereAmINowService.class).putExtra("mode", "stop"),0);

        notification = new NotificationCompat.Builder(state)
                .setVisibility(Notification.VISIBILITY_PRIVATE)
                .setLargeIcon(BitmapFactory.decodeResource(state.getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.drawable.ic_notification_twinks)
//                .setAutoCancel(true)
//                .addAction(R.drawable.ic_notification_twinks, "View", pendingIntent)
                .addAction(R.drawable.ic_notification_clear, "Stop", pendingStopIntent)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_HIGH);

        state.setNotification(notification.build());

        notificationClearHandler = new Handler();
    }

    @Override
    public String getType(){
        return TYPE;
    }

    @Override
    public NotificationUpdate create(MyUser myUser) {
        if (myUser == null) return null;
        return new NotificationUpdate(myUser);
    }

    @Override
    public boolean dependsOnEvent() {
        return true;
    }

    @Override
    public boolean onEvent(String event, Object object) {

        switch (event){
            case TRACKING_NEW:
                update("Creating group...", DEFAULT_LIGHTS, PRIORITY_DEFAULT);
                break;
            case TRACKING_JOIN:
                update("Joining group...", DEFAULT_LIGHTS, PRIORITY_DEFAULT);
                break;
            case TRACKING_DISABLED:
//                state.setNotification(null);
//                notification = null;
                break;
            case TRACKING_ACTIVE:
                update("You have joined.", DEFAULT_LIGHTS, PRIORITY_DEFAULT);
                break;
            case USER_JOINED:
                MyUser user = (MyUser) object;
                if(user != null && user.isUser()) {
                    update(user.getProperties().getDisplayName() + " has joined.", DEFAULT_LIGHTS, PRIORITY_HIGH);
                }
                break;
            case USER_DISMISSED:
                user = (MyUser) object;
                if(user != null && user.isUser()) {
                    update(user.getProperties().getDisplayName() + " has left.", DEFAULT_LIGHTS, PRIORITY_DEFAULT);
                }
                break;
            case ACTIVITY_RESUME:
                showNotifications = false;
                break;
            case ACTIVITY_PAUSE:
                showNotifications = true;
                break;
            case TRACKING_RECONNECTING:
                String message = (String) object;
                update((message != null && message.length() > 0) ? message : "Reconnect...", DEFAULT_LIGHTS, PRIORITY_DEFAULT);
                break;
            case SHOW_CUSTOM_NOTIFICATION:
                final Notification notification = (Notification) object;
                if(notification != null){
                    NotificationManager notificationManager = (NotificationManager) state.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(1977, notification);
                }
                break;
            case HIDE_CUSTOM_NOTIFICATION:
                NotificationManager notificationManager = (NotificationManager) state.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(1977);

        }

        return true;
    }

    @Override
    public boolean dependsOnUser() {
        return true;
    }

    private void update(String text, int defaults, int priority) {
        if(notification == null) return;
        if(state.tracking_disabled()) return;
        if(showNotifications) {
            notification.setDefaults(defaults);
//            notification.setDefaults(defaults != 0 ? defaults : Notification.DEFAULT_LIGHTS);
            notification.setPriority(priority);
        } else {
            notification.setDefaults(defaults);
//            notification.setDefaults(defaults != 0 ? defaults : Notification.DEFAULT_ALL);
            notification.setPriority(priority);
        }

        if("".equals(text)) {
        } else if(text != null) {
            notification.setContentTitle(text);
            notification.setContentText(state.getUsers().getCountActive() + " user(s) active.");
        } else {
            notification.setContentTitle(state.getUsers().getCountActive() + " user(s) active.");
            notification.setContentText(null);
        }
        notification.setWhen(new Date().getTime());
        notification.setSound(null);
        notification.setVibrate(new long[]{0L, 0L});

        NotificationManager notificationManager = (NotificationManager) state.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1976, notification.build());

        notificationClearHandler.removeCallbacks(notificationClearRunnable);
        notificationClearHandler.postDelayed(notificationClearRunnable, DELAY_BEFORE_CLEAR_NOTIFICATION * 1000);
    }

    public class NotificationUpdate extends AbstractProperty {
        NotificationUpdate(MyUser myUser) {
            super(myUser);
        }

        @Override
        public boolean onEvent(String event, Object object) {
            switch (event) {
                case MOVING_CLOSE_TO:
                    long currentTime = new Date().getTime();
                    if(currentTime - lastCloseNotifyTime > MIN_INTERVAL_BETWEEN_DISTANCE_NOTIFICATIONS * 1000) {
                        update("Close to " + myUser.getProperties().getDisplayName(), DEFAULT_ALL, PRIORITY_HIGH);
                    }
                    lastCloseNotifyTime = currentTime;
                    break;
                case MOVING_AWAY_FROM:
                    currentTime = new Date().getTime();
                    if(currentTime - lastAwayNotifyTime > MIN_INTERVAL_BETWEEN_DISTANCE_NOTIFICATIONS * 1000) {
                        update("Away from " + myUser.getProperties().getDisplayName(), DEFAULT_LIGHTS, PRIORITY_DEFAULT);
                    }
                    lastAwayNotifyTime = currentTime;
                    break;
            }
            return true;
        }

        @Override
        public boolean dependsOnLocation() {
            return false;
        }
    }

}
