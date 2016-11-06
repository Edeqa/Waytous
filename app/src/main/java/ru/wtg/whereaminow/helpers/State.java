package ru.wtg.whereaminow.helpers;

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.google.android.gms.iid.InstanceID;

import ru.wtg.whereaminow.MainActivity;
import ru.wtg.whereaminow.WhereAmINowService;
import ru.wtg.whereaminow.service_helpers.MyTracking;

import static ru.wtg.whereaminowserver.helpers.Constants.TRACKING_ACTIVE;
import static ru.wtg.whereaminowserver.helpers.Constants.TRACKING_CONNECTING;
import static ru.wtg.whereaminowserver.helpers.Constants.TRACKING_DISABLED;
import static ru.wtg.whereaminowserver.helpers.Constants.TRACKING_GPS_REJECTED;

/**
 * Created by tujger on 10/2/16.
 */

public class State {

    private int tracking = TRACKING_DISABLED;

    private static State instance = null;
    private static Context application;
    private static WhereAmINowService service;
    private static MainActivity activity;
    private SharedPreferences sharedPreferences;

    private String deviceId;
    private String token;
    private int number;
    private MyUsers users;
    public MyTracking myTracking;
    private Notification notification;

    private State() {
        users = new MyUsers();
    }

    public static State getInstance() {
        if (instance == null) {
//            synchronized (State.class){
                if (instance == null) {
                    instance = new State();
                }
//            }
        }
        return instance ;
    }

    public WhereAmINowService getService() {
        return service;
    }

    public void setService(WhereAmINowService service) {
        State.service = service;
    }

    public void setActivity(MainActivity activity) {
        State.activity = activity;
    }

    public MainActivity getActivity(){
        return activity;
    }

    public void setApplication(Context context){
        application = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);
    }

    public Context getApplication(){
        return application;
    }

    public boolean disconnected(){
        return tracking == TRACKING_DISABLED;
    }

    public boolean rejected(){
        return tracking == TRACKING_GPS_REJECTED;
    }

    public boolean tracking(){
        return tracking == TRACKING_ACTIVE;
    }

    public boolean connecting(){
        return tracking == TRACKING_CONNECTING;
    }


    public int getTracking() {
        return tracking;
    }

    public void setStatus(int tracking) {
        this.tracking = tracking;
    }

    public void checkDeviceId() {
        deviceId = sharedPreferences.getString("device_id",null);
        if(deviceId == null){

//            InstanceID.getInstance(application).getId()
            deviceId = Settings.Secure.getString(activity.getContentResolver(),Settings.Secure.ANDROID_ID);
            sharedPreferences.edit().putString("device_id",deviceId).apply();
        }
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

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public MyUsers getUsers() {
        return users;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    public Notification getNotification() {
        return notification;
    }
}
