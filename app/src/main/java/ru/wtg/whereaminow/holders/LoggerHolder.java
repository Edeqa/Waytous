package ru.wtg.whereaminow.holders;

import android.location.Location;

import ru.wtg.whereaminow.helpers.MyUser;

/**
 * Created 11/30/16.
 */
public class LoggerHolder extends AbstractPropertyHolder {
    private static final String TYPE = "logger";

    public LoggerHolder() {
        System.out.println("LOGGER:SYSTEMCONSTRUCT");
    }

    @Override
    public String getType(){
        return TYPE;
    }

    @Override
    public Logger create(MyUser myUser) {
        if (myUser == null) return null;
        System.out.println("LOGGER:CREATEUSER:"+myUser.getProperties().getName());
        return new Logger(myUser);
    }

    @Override
    public boolean dependsOnEvent() {
        System.out.println("LOGGER:DEPENDSONEVENT");
        return true;
    }

    @Override
    public boolean onEvent(String event, Object object) {
        System.out.println("LOGGER:ONSYSTEMEVENT:"+event+":"+object);
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
            System.out.println("LOGGER:USERCONSTRUCT:"+myUser.getProperties().getName());
        }

        @Override
        public boolean onEvent(String event, Object object) {
            System.out.println("LOGGER:ONUSEREVENT:"+myUser.getProperties().getName()+":"+event+":"+object);
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

        @Override
        public void remove() {
            System.out.println("LOGGER:USERREMOVE:"+myUser.getProperties().getName());
        }
    }

}
