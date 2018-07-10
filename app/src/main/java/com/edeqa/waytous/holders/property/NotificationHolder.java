package com.edeqa.waytous.holders.property;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;

import com.edeqa.helpers.interfaces.Consumer;
import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;
import com.edeqa.waytous.State;
import com.edeqa.waytous.WaytousService;
import com.edeqa.waytous.abstracts.AbstractProperty;
import com.edeqa.waytous.abstracts.AbstractPropertyHolder;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.helpers.SettingItem;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.support.v4.app.NotificationCompat.DEFAULT_ALL;
import static android.support.v4.app.NotificationCompat.DEFAULT_LIGHTS;
import static android.support.v4.app.NotificationCompat.PRIORITY_DEFAULT;
import static android.support.v4.app.NotificationCompat.PRIORITY_HIGH;
import static android.support.v4.app.NotificationCompat.PRIORITY_LOW;
import static com.edeqa.waytous.Constants.USER_DISMISSED;
import static com.edeqa.waytous.Constants.USER_JOINED;
import static com.edeqa.waytous.helpers.Events.ACTIVITY_PAUSE;
import static com.edeqa.waytous.helpers.Events.ACTIVITY_RESUME;
import static com.edeqa.waytous.helpers.Events.MOVING_AWAY_FROM;
import static com.edeqa.waytous.helpers.Events.MOVING_CLOSE_TO;
import static com.edeqa.waytous.helpers.Events.TRACKING_ACTIVE;
import static com.edeqa.waytous.helpers.Events.TRACKING_DISABLED;
import static com.edeqa.waytous.helpers.Events.TRACKING_JOIN;
import static com.edeqa.waytous.helpers.Events.TRACKING_NEW;
import static com.edeqa.waytous.helpers.Events.TRACKING_RECONNECTING;
import static com.edeqa.waytous.holders.property.TrackingHolder.PREFERENCE_TERMS_OF_SERVICE_CONFIRMED;
import static com.edeqa.waytous.holders.view.SettingsViewHolder.CREATE_SETTINGS;

/**
 * Created 11/29/16.
 */
@SuppressWarnings({"WeakerAccess", "HardCodedStringLiteral"})
public class NotificationHolder extends AbstractPropertyHolder {

    public static final String NOTIFICATION_CHANNEL_ID_SERVICE = "com.edeqa.waytous_service";
    public static final String NOTIFICATION_CHANNEL_ID_TASK = "com.edeqa.waytous_task";


    public static final String SHOW_CUSTOM_NOTIFICATION = "show_custom_notification";
    public static final String HIDE_CUSTOM_NOTIFICATION = "hide_custom_notification";

    private static final int MIN_INTERVAL_BETWEEN_DISTANCE_NOTIFICATIONS = 300;
    private static final int DELAY_BEFORE_CLEAR_NOTIFICATION = 5;
    private static final String PREFERENCE_NOTIFICATION_NEW_MESSAGE = "notification_new_message";
    private static final String PREFERENCE_NOTIFICATION_USER_ONLINE = "notification_user_online";
    private static final String PREFERENCE_NOTIFICATION_CLOSE_TO_USER = "notification_close_to_user";
    private static final String PREFERENCE_NOTIFICATION_AWAY_FROM_USER = "notification_away_from_user";
    private static final String PREFERENCE_NOTIFICATION_SOUNDS = "notification_sounds";

    private final State state;

    private android.support.v4.app.NotificationCompat.Builder notification;
    private Handler notificationClearHandler;

    private long lastCloseNotifyTime = 0;
    private long lastAwayNotifyTime = 0;
    private boolean showNotifications = true;
    private long becomesActive = 0;

    public NotificationHolder(State state) {
        super(state);
        this.state = state;

        Intent notificationIntent = new Intent(state, MainActivity.class);
//        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

//        PendingIntent pendingIntent = PendingIntent.getActivity(state, 0, notificationIntent, 0);
        PendingIntent pendingIntent = PendingIntent.getActivity(state, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingStopIntent = PendingIntent.getService(state, (int) System.currentTimeMillis(), new Intent(state, WaytousService.class).putExtra("mode", "stop"),0);

        initChannel(state);

        notification = new NotificationCompat.Builder(state, NOTIFICATION_CHANNEL_ID_TASK)
                .setVisibility(Notification.VISIBILITY_SECRET)
                .setLargeIcon(BitmapFactory.decodeResource(state.getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.drawable.ic_notification_twinks)
//                .setAutoCancel(true)
//                .addAction(R.drawable.ic_notification_twinks, "View", pendingIntent)
                .addAction(R.drawable.ic_notification_clear, state.getString(R.string.stop), pendingStopIntent)
                .setContentIntent(pendingIntent)
                .setContentTitle("Waytous Title")
                .setContentText("Waytous text")
                .setPriority(Notification.PRIORITY_HIGH);

        state.setNotification(notification.build());

        notificationClearHandler = new Handler();
    }

    private void initChannel(State state) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) state.getSystemService(NOTIFICATION_SERVICE);
            nm.createNotificationChannel(new NotificationChannel(NOTIFICATION_CHANNEL_ID_SERVICE, "Waytous Service", NotificationManager.IMPORTANCE_DEFAULT));
            nm.createNotificationChannel(new NotificationChannel(NOTIFICATION_CHANNEL_ID_TASK, "Waytous Info", NotificationManager.IMPORTANCE_DEFAULT));
        }
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
                if(State.getInstance().getBooleanPreference(PREFERENCE_TERMS_OF_SERVICE_CONFIRMED, false)) {
                    update(state.getString(R.string.creating_group), DEFAULT_LIGHTS, PRIORITY_DEFAULT, null);
                }
                break;
            case TRACKING_JOIN:
                if(State.getInstance().getBooleanPreference(PREFERENCE_TERMS_OF_SERVICE_CONFIRMED, false)) {
                    update(state.getString(R.string.joining_group), DEFAULT_LIGHTS, PRIORITY_DEFAULT, null);
                }
                break;
            case TRACKING_DISABLED:
//                state.setNotification(null);
//                notification = null;
                break;
            case TRACKING_ACTIVE:
                becomesActive = new Date().getTime();
                updateIcon(R.drawable.ic_notification_twinks);
                update(state.getString(R.string.you_have_joined), DEFAULT_LIGHTS, PRIORITY_HIGH, null);
                break;
            case USER_JOINED:
                MyUser user = (MyUser) object;
                long currentTime = new Date().getTime();
                if(currentTime - becomesActive > 30 * 1000 && user != null && user.isUser()) {
                    long lastOffline = ((NotificationUpdate) user.getProperty(getType())).lastOfflineTime;
                    String s = State.getInstance().getStringPreference(PREFERENCE_NOTIFICATION_USER_ONLINE, null);
                    if(lastOffline == 0) {
                        update(state.getString(R.string.s_has_joined, user.getProperties().getDisplayName()), DEFAULT_ALL, PRIORITY_HIGH, s != null ? Uri.parse(s) : null);
                    } else if(currentTime - lastOffline > 15 * 60 * 1000) {
                        update(state.getString(R.string.user_s_is_online, user.getProperties().getDisplayName()), DEFAULT_ALL, PRIORITY_HIGH, s != null ? Uri.parse(s) : null);
                    } else {
                        update(state.getString(R.string.user_s_is_online, user.getProperties().getDisplayName()), DEFAULT_LIGHTS, PRIORITY_LOW, null);
                    }
                } else {
                    assert user != null;
                    update(state.getString(R.string.user_s_is_online, user.getProperties().getDisplayName()), DEFAULT_LIGHTS, PRIORITY_LOW, null);
                }
                break;
            case USER_DISMISSED:
                user = (MyUser) object;
                if(user != null && user.isUser()) {
                    update(state.getString(R.string.user_s_is_offline, user.getProperties().getDisplayName()), DEFAULT_LIGHTS, PRIORITY_LOW, null);
                    ((NotificationUpdate)user.getProperty(getType())).lastOfflineTime = new Date().getTime();
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
                update((message != null && message.length() > 0) ? message : state.getString(R.string.reconnecting), DEFAULT_LIGHTS, PRIORITY_DEFAULT, null);
                break;
            case SHOW_CUSTOM_NOTIFICATION:
                final Notification notification = (Notification) object;
                if(notification != null){
                    if(MainActivity.isVisible()) notification.priority = Notification.PRIORITY_LOW;
                    NotificationManager notificationManager = (NotificationManager) state.getSystemService(NOTIFICATION_SERVICE);
                    assert notificationManager != null;
                    notificationManager.notify(1977, notification);
                }
                break;
            case HIDE_CUSTOM_NOTIFICATION:
                NotificationManager notificationManager = (NotificationManager) state.getSystemService(NOTIFICATION_SERVICE);
                assert notificationManager != null;
                notificationManager.cancel(1977);
                break;
            case CREATE_SETTINGS:
                SettingItem.Page item = (SettingItem.Page) object;
                Map<String,String> items = getNotificationSounds();

                item.add(new SettingItem.Page(getType()).setTitle(state.getString(R.string.notifications))
                        .add(new SettingItem.Group(PREFERENCE_NOTIFICATION_SOUNDS).setTitle(state.getString(R.string.sounds)))
                        .add(new SettingItem.List(PREFERENCE_NOTIFICATION_USER_ONLINE)
                                .setItems(items)
                                .setOnItemSelectedCallback(previewSound)
                                .setTitle(state.getString(R.string.user_online))
                                .setGroupId(PREFERENCE_NOTIFICATION_SOUNDS))
                        .add(new SettingItem.List(PREFERENCE_NOTIFICATION_CLOSE_TO_USER)
                                .setItems(items)
                                .setOnItemSelectedCallback(previewSound)
                                .setTitle(state.getString(R.string.close_to_user))
                                .setGroupId(PREFERENCE_NOTIFICATION_SOUNDS))
                        .add(new SettingItem.List(PREFERENCE_NOTIFICATION_AWAY_FROM_USER)
                                .setItems(items)
                                .setOnItemSelectedCallback(previewSound)
                                .setTitle(state.getString(R.string.away_from_user))
                                .setGroupId(PREFERENCE_NOTIFICATION_SOUNDS))
                        .add(new SettingItem.List(PREFERENCE_NOTIFICATION_NEW_MESSAGE)
                                .setItems(items)
                                .setOnItemSelectedCallback(previewSound)
                                .setTitle(state.getString(R.string.new_message))
                                .setGroupId(PREFERENCE_NOTIFICATION_SOUNDS)));
                break;
        }

        return true;
    }


    @Override
    public boolean dependsOnUser() {
        return true;
    }

    private void update(String text, int defaults, int priority, Uri sound) {
        if(notification == null) return;
        if(state.tracking_disabled()) return;

        if(MainActivity.isVisible()) priority = PRIORITY_DEFAULT;

        if(showNotifications) {
            notification.setDefaults(defaults);
//            notification.setDefaults(defaults != 0 ? defaults : Notification.DEFAULT_LIGHTS);
            if(sound != null) notification.setSound(sound);
            notification.setPriority(priority);
        } else {
            notification.setDefaults(defaults);
//            notification.setDefaults(defaults != 0 ? defaults : Notification.DEFAULT_ALL);
            notification.setSound(null);
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
        notification.setVibrate(new long[]{0L, 0L});

        NotificationManager notificationManager = (NotificationManager) state.getSystemService(NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.notify(1976, notification.build());

        notificationClearHandler.removeCallbacks(notificationClearRunnable);
        notificationClearHandler.postDelayed(notificationClearRunnable, DELAY_BEFORE_CLEAR_NOTIFICATION * 1000);
    }

    private void updateIcon(int resource) {
        notification.setSmallIcon(resource);
        NotificationManager notificationManager = (NotificationManager) state.getSystemService(NOTIFICATION_SERVICE);
        assert notificationManager != null;
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
            NotificationManager notificationManager = (NotificationManager) state.getSystemService(NOTIFICATION_SERVICE);
//            notificationManager.notify(1976, notification.build());
            assert notificationManager != null;
            notificationManager.cancel(1976);
        }
    };

    private Consumer<String> previewSound = new Consumer<String>() {

        public Ringtone player;

        @Override
        public void accept(String arg) {
            if(arg != null && arg.length() > 0) {
                if(player != null) {
                    player.stop();
                }
                player = RingtoneManager.getRingtone(state, Uri.parse(arg));
                player.play();
            }
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
                        String s = State.getInstance().getStringPreference(PREFERENCE_NOTIFICATION_CLOSE_TO_USER, null);
                        update(state.getString(R.string.close_to_s, myUser.getProperties().getDisplayName()), DEFAULT_ALL, PRIORITY_HIGH, s != null ? Uri.parse(s) : null);
                    }
                    lastCloseNotifyTime = currentTime;
                    break;
                case MOVING_AWAY_FROM:
                    currentTime = new Date().getTime();
                    if(currentTime - lastAwayNotifyTime > MIN_INTERVAL_BETWEEN_DISTANCE_NOTIFICATIONS * 1000) {
                        String s = State.getInstance().getStringPreference(PREFERENCE_NOTIFICATION_AWAY_FROM_USER, null);
                        update(state.getString(R.string.away_from_s, myUser.getProperties().getDisplayName()), DEFAULT_LIGHTS, PRIORITY_DEFAULT, s != null ? Uri.parse(s) : null);
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

    @SuppressWarnings("unchecked")
    public Map<String,String> getNotificationSounds() {
        Map items = new LinkedHashMap();
        RingtoneManager manager = new RingtoneManager(State.getInstance());
        manager.setType(RingtoneManager.TYPE_NOTIFICATION);
        Cursor cursor = manager.getCursor();

        items.put(state.getString(R.string.none), "");
        while (cursor.moveToNext()) {
            String title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
            String id = cursor.getString(RingtoneManager.ID_COLUMN_INDEX);
            String uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX);

            items.put(title, uri + "/" + id);
        }

        return items;
    }
}
