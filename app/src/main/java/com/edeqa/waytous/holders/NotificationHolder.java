package com.edeqa.waytous.holders;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v7.app.NotificationCompat;

import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;
import com.edeqa.waytous.State;
import com.edeqa.waytous.WaytousService;
import com.edeqa.waytous.abstracts.AbstractProperty;
import com.edeqa.waytous.abstracts.AbstractPropertyHolder;
import com.edeqa.waytous.helpers.MyUser;

import java.util.Date;

import static android.support.v4.app.NotificationCompat.DEFAULT_ALL;
import static android.support.v4.app.NotificationCompat.DEFAULT_LIGHTS;
import static android.support.v4.app.NotificationCompat.PRIORITY_DEFAULT;
import static android.support.v4.app.NotificationCompat.PRIORITY_HIGH;
import static android.support.v4.app.NotificationCompat.PRIORITY_LOW;
import static com.edeqa.waytous.helpers.Events.ACTIVITY_PAUSE;
import static com.edeqa.waytous.helpers.Events.ACTIVITY_RESUME;
import static com.edeqa.waytous.helpers.Events.MOVING_AWAY_FROM;
import static com.edeqa.waytous.helpers.Events.MOVING_CLOSE_TO;
import static com.edeqa.waytous.helpers.Events.TRACKING_ACTIVE;
import static com.edeqa.waytous.helpers.Events.TRACKING_DISABLED;
import static com.edeqa.waytous.helpers.Events.TRACKING_JOIN;
import static com.edeqa.waytous.helpers.Events.TRACKING_NEW;
import static com.edeqa.waytous.helpers.Events.TRACKING_RECONNECTING;
import static com.edeqa.waytousserver.helpers.Constants.USER_DISMISSED;
import static com.edeqa.waytousserver.helpers.Constants.USER_JOINED;

/**
 * Created 11/29/16.
 */
public class NotificationHolder extends AbstractPropertyHolder {

    @SuppressWarnings("HardCodedStringLiteral")
    public static final String TYPE = "notification";
    @SuppressWarnings({"HardCodedStringLiteral", "WeakerAccess"})
    public static final String SHOW_CUSTOM_NOTIFICATION = "show_custom_notification";
    @SuppressWarnings({"HardCodedStringLiteral", "WeakerAccess"})
    public static final String HIDE_CUSTOM_NOTIFICATION = "hide_custom_notification";

    private static final int MIN_INTERVAL_BETWEEN_DISTANCE_NOTIFICATIONS = 300;
    private static final int DELAY_BEFORE_CLEAR_NOTIFICATION = 5;

    private final State state;

    private android.support.v4.app.NotificationCompat.Builder notification;
    private Handler notificationClearHandler;

    private long lastCloseNotifyTime = 0;
    private long lastAwayNotifyTime = 0;
    private boolean showNotifications = true;

    public NotificationHolder(State state) {
        this.state = state;

        Intent notificationIntent = new Intent(state, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(state, 0, notificationIntent, 0);
        PendingIntent pendingStopIntent = PendingIntent.getService(state, (int) System.currentTimeMillis(), new Intent(state, WaytousService.class).putExtra("mode", "stop"),0);

        notification = new NotificationCompat.Builder(state)
                .setVisibility(Notification.VISIBILITY_SECRET)
                .setLargeIcon(BitmapFactory.decodeResource(state.getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.drawable.ic_notification_twinks)
//                .setAutoCancel(true)
//                .addAction(R.drawable.ic_notification_twinks, "View", pendingIntent)
                .addAction(R.drawable.ic_notification_clear, state.getString(R.string.stop), pendingStopIntent)
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
                update(state.getString(R.string.creating_group), DEFAULT_LIGHTS, PRIORITY_DEFAULT);
                break;
            case TRACKING_JOIN:
                update(state.getString(R.string.joining_group), DEFAULT_LIGHTS, PRIORITY_DEFAULT);
                break;
            case TRACKING_DISABLED:
//                state.setNotification(null);
//                notification = null;
                break;
            case TRACKING_ACTIVE:
                updateIcon(R.drawable.ic_notification_twinks);
                update(state.getString(R.string.you_have_joined), DEFAULT_LIGHTS, PRIORITY_DEFAULT);
                break;
            case USER_JOINED:
                MyUser user = (MyUser) object;
                if(user != null && user.isUser()) {
                    long lastOffline = ((NotificationUpdate)user.getEntity(TYPE)).lastOfflineTime;
                    if(lastOffline == 0) {
                        update(state.getString(R.string.s_has_joined, user.getProperties().getDisplayName()), DEFAULT_ALL, PRIORITY_HIGH);
                    } else if(new Date().getTime() - lastOffline > 15 * 60 * 1000) {
                        update(state.getString(R.string.user_s_is_online, user.getProperties().getDisplayName()), DEFAULT_ALL, PRIORITY_HIGH);
                    } else {
                        update(state.getString(R.string.user_s_is_online, user.getProperties().getDisplayName()), DEFAULT_LIGHTS, PRIORITY_LOW);
                    }
                }
                break;
            case USER_DISMISSED:
                user = (MyUser) object;
                if(user != null && user.isUser()) {
                    update(state.getString(R.string.user_s_is_offline, user.getProperties().getDisplayName()), DEFAULT_LIGHTS, PRIORITY_LOW);
                    ((NotificationUpdate)user.getEntity(TYPE)).lastOfflineTime = new Date().getTime();
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
                updateIcon(R.drawable.ic_notification_twinks_pause);
                update((message != null && message.length() > 0) ? message : state.getString(R.string.reconnecting), DEFAULT_LIGHTS, PRIORITY_DEFAULT);
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

        if(text != null) {
            notification.setContentTitle(text);
            notification.setContentText(state.getString(R.string.d_users_online, state.getUsers().getCountActive()));
        } else {
            notification.setContentTitle(state.getString(R.string.d_users_online, state.getUsers().getCountActive()));
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

    private void updateIcon(int resource) {
        notification.setSmallIcon(resource);
        NotificationManager notificationManager = (NotificationManager) state.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1976, notification.build());
    }

    private Runnable notificationClearRunnable = new Runnable() {
        @Override
        public void run() {
            if(notification == null) return;
            if(state.tracking_disabled()) return;
            if(showNotifications) {
                notification.setDefaults(DEFAULT_LIGHTS);
            } else {
                notification.setDefaults(DEFAULT_LIGHTS);
            }
            NotificationManager notificationManager = (NotificationManager) state.getSystemService(Context.NOTIFICATION_SERVICE);
//            notificationManager.notify(1976, notification.build());
            notificationManager.cancel(1976);
        }
    };

    @SuppressWarnings("WeakerAccess")
    public class NotificationUpdate extends AbstractProperty {
        private long lastOfflineTime = 0;

        NotificationUpdate(MyUser myUser) {
            super(myUser);

        }

        @Override
        public boolean onEvent(String event, Object object) {
            switch (event) {
                case MOVING_CLOSE_TO:
                    long currentTime = new Date().getTime();
                    if(currentTime - lastCloseNotifyTime > MIN_INTERVAL_BETWEEN_DISTANCE_NOTIFICATIONS * 1000) {
                        update(state.getString(R.string.close_to_s, myUser.getProperties().getDisplayName()), DEFAULT_ALL, PRIORITY_HIGH);
                    }
                    lastCloseNotifyTime = currentTime;
                    break;
                case MOVING_AWAY_FROM:
                    currentTime = new Date().getTime();
                    if(currentTime - lastAwayNotifyTime > MIN_INTERVAL_BETWEEN_DISTANCE_NOTIFICATIONS * 1000) {
                        update(state.getString(R.string.away_from_s, myUser.getProperties().getDisplayName()), DEFAULT_LIGHTS, PRIORITY_DEFAULT);
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
