package ru.wtg.whereaminow.holders;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import ru.wtg.whereaminow.MainActivity;
import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.WhereAmINowService;
import ru.wtg.whereaminow.helpers.MyUser;

import static ru.wtg.whereaminow.State.CHANGE_COLOR;
import static ru.wtg.whereaminow.State.CHANGE_NAME;
import static ru.wtg.whereaminow.State.CHANGE_NUMBER;
import static ru.wtg.whereaminow.State.MAKE_ACTIVE;
import static ru.wtg.whereaminow.State.MAKE_INACTIVE;
import static ru.wtg.whereaminow.State.SELECT_USER;
import static ru.wtg.whereaminow.State.STARTED;
import static ru.wtg.whereaminow.State.STOPPED;
import static ru.wtg.whereaminow.State.UNSELECT_USER;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_NAME;

/**
 * Created 11/29/16.
 */
public class NotificationHolder extends AbstractPropertyHolder<NotificationHolder.NotificationUpdate> {
    public static final String TYPE = "notification";
    private final State state;
    private Notification notification;

    public NotificationHolder(State state) {
        this.state = state;
    }

    @Override
    public String getType(){
        return TYPE;
    }

    @Override
    public String[] getOwnEvents() {
        return new String[0];
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
            case STARTED:
                Intent notificationIntent = new Intent(state, MainActivity.class);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                PendingIntent pendingIntent = PendingIntent.getActivity(state, 0, notificationIntent, 0);

                notification = new NotificationCompat.Builder(state)
                        .setSmallIcon(R.drawable.ic_navigation_twinks_white_24dp)
                        .setContentTitle(state.getApplication().getString(R.string.app_name))
                        .setContentText("Doing some work...")
                        .setAutoCancel(true)
                        .addAction(R.drawable.ic_navigation_twinks_black_24dp, "View", pendingIntent)
                        .addAction(R.drawable.ic_clear_black_24dp, "Stop",
                                PendingIntent.getService(state, (int) System.currentTimeMillis(), new Intent(state, WhereAmINowService.class).putExtra("mode", "stop"),0))
                        .setContentIntent(pendingIntent)
                        .build();

                state.setNotification(notification);

                break;
            case STOPPED:
                state.setNotification(null);
                notification = null;
                break;
        }

        return true;
    }

    @Override
    public boolean dependsOnUser() {
        return true;
    }


    public class NotificationUpdate extends AbstractProperty {
        NotificationUpdate(MyUser myUser) {
            super(myUser);
        }

        @Override
        public boolean onEvent(String event, Object object) {
            return true;
        }

        @Override
        public boolean dependsOnLocation() {
            return false;
        }
    }

}
