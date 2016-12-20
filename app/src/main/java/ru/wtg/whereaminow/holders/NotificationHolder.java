package ru.wtg.whereaminow.holders;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.NotificationCompat;

import java.util.Date;

import ru.wtg.whereaminow.MainActivity;
import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.WhereAmINowService;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.UserMessage;

import static ru.wtg.whereaminow.State.ACTIVITY_PAUSE;
import static ru.wtg.whereaminow.State.ACTIVITY_RESUME;
import static ru.wtg.whereaminow.State.TRACKING_ACCEPTED;
import static ru.wtg.whereaminow.State.TRACKING_STARTED;
import static ru.wtg.whereaminow.State.TRACKING_STOPPED;
import static ru.wtg.whereaminow.holders.MessagesHolder.PRIVATE_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_DISMISSED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_JOINED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_MESSAGE;

/**
 * Created 11/29/16.
 */
public class NotificationHolder extends AbstractPropertyHolder<NotificationHolder.NotificationUpdate> {
    public static final String TYPE = "notification";
    private final State state;
    private boolean showNotifications = true;
    private android.support.v4.app.NotificationCompat.Builder notification;

    public NotificationHolder(State state) {
        this.state = state;
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
            case TRACKING_STARTED:
                Intent notificationIntent = new Intent(state, MainActivity.class);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                PendingIntent pendingIntent = PendingIntent.getActivity(state, 0, notificationIntent, 0);

                notification = new NotificationCompat.Builder(state)
                        .setLargeIcon(BitmapFactory.decodeResource(state.getResources(), R.mipmap.ic_launcher))
                        .setSmallIcon(R.drawable.ic_navigation_twinks_white)
                        .setAutoCancel(true)
                        .addAction(R.drawable.ic_navigation_twinks_white, "View", pendingIntent)
                        .setContentTitle("Creating group...")
                        .addAction(R.drawable.ic_clear_white, "Stop",
                                PendingIntent.getService(state, (int) System.currentTimeMillis(), new Intent(state, WhereAmINowService.class).putExtra("mode", "stop"),0))
                        .setContentIntent(pendingIntent)
                        .setPriority(Notification.PRIORITY_HIGH);

                state.setNotification(notification.build());

                break;
            case TRACKING_STOPPED:
                state.setNotification(null);
                notification = null;
                break;
            case TRACKING_ACCEPTED:
                update("You have joined to the group.");
                break;
            case USER_JOINED:
                MyUser user = (MyUser) object;
                if(user != null && user.isUser()) {
                    update(user.getProperties().getDisplayName() + " has joined the group.");
                    System.out.println("NOTIFICATION:JOINED:" + user.getProperties().getDisplayName());
                }
                break;
            case USER_DISMISSED:
                user = (MyUser) object;
                if(user != null && user.isUser()) {
                    update(user.getProperties().getDisplayName() + " has left the group.");
                }
                break;
            case ACTIVITY_RESUME:
                showNotifications = false;
                break;
            case ACTIVITY_PAUSE:
                showNotifications = true;
                break;
        }

        return true;
    }

    @Override
    public boolean dependsOnUser() {
        return true;
    }

    private void update(String text) {
        if(notification == null) return;
        notification.setDefaults(Notification.DEFAULT_ALL);
        if(showNotifications) {
            notification.setPriority(Notification.PRIORITY_HIGH);
        } else {
            notification.setPriority(Notification.PRIORITY_DEFAULT);
        }
        notification.setContentTitle(state.getUsers().getCountActive() + " user(s) active.");
        if(text != null) {
            notification.setContentText(text);
            notification.setTicker(text);
        }
        notification.setWhen(new Date().getTime());
        notification.setSound(null);

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
                case USER_MESSAGE:
                case PRIVATE_MESSAGE:
                    String text = (String) object;
                    update(myUser.getProperties().getDisplayName() + ": "+ text);
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
