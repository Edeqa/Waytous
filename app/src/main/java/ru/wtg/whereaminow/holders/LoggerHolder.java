package ru.wtg.whereaminow.holders;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.support.v7.app.NotificationCompat;

import ru.wtg.whereaminow.MainActivity;
import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.WhereAmINowService;
import ru.wtg.whereaminow.helpers.MyUser;

import static ru.wtg.whereaminow.State.STARTED;
import static ru.wtg.whereaminow.State.STOPPED;

/**
 * Created 11/30/16.
 */
public class LoggerHolder extends AbstractPropertyHolder<LoggerHolder.Logger> {
    private static final String TYPE = "logger";
    private final State state;

    public LoggerHolder(State state) {
        this.state = state;
        System.out.println("LOGGER:CONSTRUCTOR");
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
    public Logger create(MyUser myUser) {
        if (myUser == null) return null;
        System.out.println("LOGGER:CREATE:"+myUser.getProperties().getName());
        return new Logger(myUser);
    }

    @Override
    public boolean dependsOnEvent() {
        System.out.println("LOGGER:DEPENDSONEVENT");
        return true;
    }

    @Override
    public boolean onEvent(String event, Object object) {
        System.out.println("LOGGER:ONEVENTSYSTEM:"+event+":"+object);
        return true;
    }

    @Override
    public boolean dependsOnUser() {
        System.out.println("LOGGER:DEPENDSONUSER");
        return true;
    }

    public class Logger extends AbstractProperty {
        Logger(MyUser myUser) {
            super(myUser);
            System.out.println("LOGGER:CONSTRUCTORUSER:"+myUser.getProperties().getName());
        }

        @Override
        public boolean onEvent(String event, Object object) {
            System.out.println("LOGGER:ONEVENTUSER:"+myUser.getProperties().getName()+":"+event+":"+object);
            return true;
        }

        @Override
        public boolean dependsOnLocation() {
            System.out.println("LOGGER:DEPENDSONLOCATION:"+myUser.getProperties().getName());
            return true;
        }

        @Override
        public void onChangeLocation(Location location) {
            System.out.println("LOGGER:ONCHANGELOCATION:"+myUser.getProperties().getName());
        }
    }

}
