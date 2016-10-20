package ru.wtg.whereaminow.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;

import ru.wtg.whereaminow.MainActivity;
import ru.wtg.whereaminow.service_helpers.MyTracking;

import static ru.wtg.whereaminowserver.helpers.Constants.TRACKING_ACTIVE;
import static ru.wtg.whereaminowserver.helpers.Constants.TRACKING_DISABLED;
import static ru.wtg.whereaminowserver.helpers.Constants.TRACKING_GPS_REJECTED;

/**
 * Created by tujger on 10/2/16.
 */

public class State {

    private int tracking = TRACKING_DISABLED;


    private static volatile State instance = null;
    private Context context;
    private MainActivity mainContext;
    private SharedPreferences sharedPreferences;

    private String deviceId;
    private String token;
    public MyTracking myTracking;

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

    public boolean rejected(){
        return tracking == TRACKING_GPS_REJECTED;
    }

    public boolean tracking(){
        return tracking == TRACKING_ACTIVE;
    }


    public int getTracking() {
        return tracking;
    }

    public void setStatus(int tracking) {
        this.tracking = tracking;
    }

    public void setMainContext(MainActivity mainContext) {
        this.mainContext = mainContext;
        setContext(mainContext);
    }

    public void checkDeviceId() {
        deviceId = sharedPreferences.getString("device_id",null);
        if(deviceId == null){
            deviceId = Settings.Secure.getString(mainContext.getContentResolver(),Settings.Secure.ANDROID_ID);
            sharedPreferences.edit().putString("device_id",deviceId).apply();
        }
    }

    public void setContext(Context context) {
        this.context = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
