package ru.wtg.whereaminow.helpers;

import android.content.Context;

/**
 * Created by tujger on 10/2/16.
 */

public class State {

    public static final int TRACKING_DISABLED = 0;
    public static final int TRACKING_ACTIVE = 1;

    private int tracking = TRACKING_DISABLED;


    private static volatile State instance = null;
    private Context context;

    private State() {

    }

    public static State getInstance() {
        if (instance == null) {
            synchronized (State.class){
                if (instance == null) {
                    instance = new State();
                }
            }
        }
        return instance ;
    }


    public boolean waiting(){
        return tracking == TRACKING_DISABLED;
    }


    public int getTracking() {
        return tracking;
    }

    public void setTracking(int tracking) {
        this.tracking = tracking;
    }
}
