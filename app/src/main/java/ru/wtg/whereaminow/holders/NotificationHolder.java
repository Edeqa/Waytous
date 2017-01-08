package ru.wtg.whereaminow.holders;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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
import static ru.wtg.whereaminow.State.ACTIVITY_PAUSE;
import static ru.wtg.whereaminow.State.ACTIVITY_RESUME;
import static ru.wtg.whereaminow.State.MOVING_AWAY_FROM;
import static ru.wtg.whereaminow.State.MOVING_CLOSE_TO;
import static ru.wtg.whereaminow.State.TRACKING_ACTIVE;
import static ru.wtg.whereaminow.State.TRACKING_DISABLED;
import static ru.wtg.whereaminow.State.TRACKING_JOIN;
import static ru.wtg.whereaminow.State.TRACKING_NEW;
import static ru.wtg.whereaminow.State.TRACKING_RECONNECTING;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_DISMISSED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_JOINED;

/**
 * Created 11/29/16.
 */
public class NotificationHolder extends AbstractPropertyHolder {

    public static final String TYPE = "notification";
    public static final String CUSTOM_NOTIFICATION = "custom_notification";
    public static final String CUSTOM_NOTIFICATION_MESSAGE = "custom_notification_message";
    public static final String CUSTOM_NOTIFICATION_DEFAULTS = "custom_notification_defaults";
    public static final String CUSTOM_NOTIFICATION_PRIORITY = "custom_notification_priority";

    private final State state;
    private boolean showNotifications = true;
    private android.support.v4.app.NotificationCompat.Builder notification;

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
                .addAction(R.drawable.ic_notification_twinks, "View", pendingIntent)
                .addAction(R.drawable.ic_notification_clear, "Stop", pendingStopIntent)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_HIGH);

        state.setNotification(notification.build());

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
                update("You have joined to the group.", DEFAULT_LIGHTS, PRIORITY_DEFAULT);
                break;
            case USER_JOINED:
                MyUser user = (MyUser) object;
                if(user != null && user.isUser()) {
                    update(user.getProperties().getDisplayName() + " has joined the group.", DEFAULT_LIGHTS, PRIORITY_HIGH);
                    System.out.println("NOTIFICATION:JOINED:" + user.getProperties().getDisplayName());
                }
                break;
            case USER_DISMISSED:
                user = (MyUser) object;
                if(user != null && user.isUser()) {
                    update(user.getProperties().getDisplayName() + " has left the group.", DEFAULT_LIGHTS, PRIORITY_DEFAULT);
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
                update((message != null && message.length() > 0) ? message : "Disconnected. Trying to reconnect", DEFAULT_LIGHTS, PRIORITY_DEFAULT);
                break;
            case CUSTOM_NOTIFICATION:
                final Bundle m = (Bundle) object;
                if(m != null){
                    String text = m.getString(CUSTOM_NOTIFICATION_MESSAGE, null);
                    int defaults = m.getInt(CUSTOM_NOTIFICATION_DEFAULTS, 0);
                    int priority = m.getInt(CUSTOM_NOTIFICATION_PRIORITY, PRIORITY_DEFAULT);
                    update(text, defaults, priority);
                }
                break;
        }

        return true;
    }

    @Override
    public boolean dependsOnUser() {
        return true;
    }

    private void update(String text, int defaults, int priority) {
        if(notification == null) return;
        if(showNotifications) {
            notification.setDefaults(defaults);
//            notification.setDefaults(defaults != 0 ? defaults : Notification.DEFAULT_LIGHTS);
            notification.setPriority(priority);
        } else {
            notification.setDefaults(defaults);
//            notification.setDefaults(defaults != 0 ? defaults : Notification.DEFAULT_ALL);
            notification.setPriority(priority);
        }

        notification.setContentTitle(state.getUsers().getCountActive() + " user(s) active.");
        if(text != null) {
            notification.setContentText(text);
//            notification.setTicker(text);
        }
        notification.setWhen(new Date().getTime());
        notification.setSound(null);
        notification.setVibrate(new long[]{0L, 0L});

        NotificationManager notificationManager = (NotificationManager) state.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1976, notification.build());
    }

    public class NotificationUpdate extends AbstractProperty {
        NotificationUpdate(MyUser myUser) {
            super(myUser);
        }

        @Override
        public boolean onEvent(String event, Object object) {
            switch (event) {
                case MOVING_CLOSE_TO:
//                    System.out.println("CLOSE TO:"+myUser.getProperties().getDisplayName());
                    update("You are close to " + myUser.getProperties().getDisplayName(), DEFAULT_ALL, PRIORITY_HIGH);
                    break;
                case MOVING_AWAY_FROM:
//                    System.out.println("AWAY FROM:"+myUser.getProperties().getDisplayName());
                    update("You are moved away from " + myUser.getProperties().getDisplayName(), DEFAULT_ALL, PRIORITY_HIGH);
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
