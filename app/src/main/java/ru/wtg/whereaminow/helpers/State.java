package ru.wtg.whereaminow.helpers;

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.HashMap;

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

    private static State instance = null;
    private static Context application;
    private static WhereAmINowService service;
    private static MainActivity activity;
    private SharedPreferences sharedPreferences;

    private String deviceId;
    private String token;
    private int number;
    private MyUsers users;
    private MyUser me;
    public MyTracking myTracking;
    private Notification notification;
    private boolean gpsAccessEnabled;
    private boolean gpsAccessRequested;

    private State() {
        users = new MyUsers();
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
        if(myTracking == null) return true;
        return myTracking.getStatus() == TRACKING_DISABLED;
    }

    public boolean rejected(){
        if(myTracking == null) return false;
        return myTracking.getStatus() == TRACKING_GPS_REJECTED;
    }

    public boolean tracking(){
        if(myTracking == null) return false;
        return myTracking.getStatus() == TRACKING_ACTIVE;
    }

    public boolean connecting(){
        if(myTracking == null) return false;
        return myTracking.getStatus() == TRACKING_CONNECTING;
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


    public void setGpsAccessEnabled(boolean gpsAccessEnabled) {
        this.gpsAccessEnabled = gpsAccessEnabled;
    }

    public void setGpsAccessRequested(boolean gpsAccessRequested) {
        this.gpsAccessRequested = gpsAccessRequested;
    }

    public boolean isGpsAccessRequested() {
        return gpsAccessRequested;
    }


    public String getStringPreference(String key, String defaultValue){
        return sharedPreferences.getString(key,defaultValue);
    }

    public void setPreference(String key, String value){
        if(value != null && value.length()>0){
            sharedPreferences.edit().putString(key,value).apply();
        } else {
            sharedPreferences.edit().remove(key).apply();
        }
    }

    public MyUser getMe() {
        return me;
    }

    public void setMe(MyUser me) {
        this.me = me;
    }

    private HashMap<String,ViewHolder> viewHolders = new HashMap<>();

    public void registerViewHolder(ViewHolder viewHolder) {
        viewHolders.put(viewHolder.getType(), viewHolder);
    }

    public HashMap<String,ViewHolder> getViewHolders(){
        return viewHolders;
    }

    public void clearViewHolders(){
        viewHolders.clear();
    }
}
