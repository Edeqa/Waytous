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
    private static final String TYPE = "LoggerHolder";

    public LoggerHolder() {
        super(null);
        Log.i(TYPE,"LoggerHolder:init");
    }

    @Override
    public String getType(){
        return TYPE;
    }

    @Override
    public LoggerProperty create(MyUser myUser) {
        if (myUser == null) return null;
        Log.i(TYPE,"createProperty:"+myUser);
        return new LoggerProperty(myUser);
    }

    @Override
    public boolean dependsOnEvent() {
        Log.i(TYPE,"dependsOnEvent");
        return true;
    }

    @Override
    public boolean onEvent(String event, Object object) {
        Log.i(TYPE,"onEvent:MAIN:"+this.hashCode()+":"+event+":"+object);
        return true;
    }

    @Override
    public boolean dependsOnUser() {
        Log.i(TYPE,"dependsOnUser");
        return true;
    }

    private class LoggerProperty extends AbstractProperty {
        LoggerProperty(MyUser myUser) {
            super(myUser);
            Log.i(TYPE,"LoggerProperty:init");
        }

        @Override
        public boolean onEvent(String event, Object object) {
            Log.i(TYPE,"onEvent:PROPERTY:"+this.hashCode()+":"+event+":"+object+":"+myUser.getProperties().getNumber()+":"+myUser.getProperties().getDisplayName());
            return true;
        }

        @Override
        public boolean dependsOnLocation() {
            Log.i(TYPE,"dependsOnLocation:"+myUser.getClass());
            return true;
        }

        @Override
        public void onChangeLocation(Location location) {
            Log.i(TYPE,"onChangeLocation:"+myUser.getClass());
        }

        @Override
        public void remove() {
            Log.i(TYPE,"remove:"+myUser.getProperties().getNumber()+":"+myUser.getProperties().getDisplayName());
        }
    }

}
