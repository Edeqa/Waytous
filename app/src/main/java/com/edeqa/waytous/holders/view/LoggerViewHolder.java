package com.edeqa.waytous.holders.view;

import android.location.Location;
import android.util.Log;

import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.abstracts.AbstractView;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.helpers.MyUser;


/**
 * Created 11/30/16.
 */
@SuppressWarnings({"HardCodedStringLiteral", "WeakerAccess", "unused"})
public class LoggerViewHolder extends AbstractViewHolder<LoggerViewHolder.LoggerView> {
    private static final String TYPE = "LoggerViewHolder";

    public LoggerViewHolder(MainActivity context) {
        super(context);
        Log.i(TYPE,"LoggerViewHolder:init");
    }

    @Override
    public String getType(){
        return TYPE;
    }

    @Override
    public LoggerView create(MyUser myUser) {
        if (myUser == null) return null;
        Log.i(TYPE,"createView:"+myUser);
        return new LoggerView(myUser);
    }

    @Override
    public boolean dependsOnEvent() {
        Log.i(TYPE,"dependsOnEvent");
        return true;
    }

    @Override
    public boolean onEvent(String event, Object object) {
        Log.i(TYPE,"onEvent:MAIN:"+event+":"+object);
        return true;
    }

    @Override
    public boolean dependsOnUser() {
        Log.i(TYPE,"dependsOnUser");
        return true;
    }

    @SuppressWarnings("WeakerAccess")
    public class LoggerView extends AbstractView {
        LoggerView(MyUser myUser) {
            super(myUser);
            Log.i(TYPE,"LoggerView:init");
        }

        @Override
        public boolean onEvent(String event, Object object) {
            Log.i(TYPE,"onEvent:VIEW:"+event+":"+object+":"+myUser.getProperties().getNumber()+":"+myUser.getProperties().getDisplayName());
            return true;
        }

        @Override
        public boolean dependsOnLocation() {
            Log.i(TYPE,"dependsOnLocation:"+myUser);
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
