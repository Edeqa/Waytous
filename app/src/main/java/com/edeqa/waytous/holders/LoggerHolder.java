package com.edeqa.waytous.holders;

import android.location.Location;
import android.util.Log;

import com.edeqa.waytous.abstracts.AbstractProperty;
import com.edeqa.waytous.abstracts.AbstractPropertyHolder;
import com.edeqa.waytous.helpers.MyUser;


/**
 * Created 11/30/16.
 */
public class LoggerHolder extends AbstractPropertyHolder {
    private static final String TYPE = "logger";

    public LoggerHolder() {
        Log.i("Logger","SYSTEMCONSTRUCT");
    }

    @Override
    public String getType(){
        return TYPE;
    }

    @Override
    public Logger create(MyUser myUser) {
        if (myUser == null) return null;
        Log.i("Logger","CREATEUSER:"+myUser.getProperties().getName());
        return new Logger(myUser);
    }

    @Override
    public boolean dependsOnEvent() {
        Log.i("Logger","DEPENDSONEVENT");
        return true;
    }

    @Override
    public boolean onEvent(String event, Object object) {
        Log.i("Logger","ONSYSTEMEVENT:"+event+":"+object);
        return true;
    }

    @Override
    public boolean dependsOnUser() {
        Log.i("Logger","DEPENDSONUSER");
        return true;
    }

    private class Logger extends AbstractProperty {
        Logger(MyUser myUser) {
            super(myUser);
            Log.i("Logger","USERCONSTRUCT:"+myUser.getProperties().getName());
        }

        @Override
        public boolean onEvent(String event, Object object) {
            Log.i("Logger","ONUSEREVENT:"+myUser.getProperties().getName()+":"+event+":"+object);
            return true;
        }

        @Override
        public boolean dependsOnLocation() {
            Log.i("Logger","DEPENDSONLOCATION:"+myUser.getProperties().getName());
            return true;
        }

        @Override
        public void onChangeLocation(Location location) {
            Log.i("Logger","ONCHANGELOCATION:"+myUser.getProperties().getName());
        }

        @Override
        public void remove() {
            Log.i("Logger","USERREMOVE:"+myUser.getProperties().getName());
        }
    }

}
