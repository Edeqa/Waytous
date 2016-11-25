package ru.wtg.whereaminow;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;
import ru.wtg.whereaminow.holders.AbstractViewHolder;
import ru.wtg.whereaminow.holders.PropertiesHolder;
import ru.wtg.whereaminow.interfaces.EntityHolder;
import ru.wtg.whereaminow.service_helpers.MyTracking;

import static ru.wtg.whereaminow.ExceptionActivity.EXCEPTION;
import static ru.wtg.whereaminowserver.helpers.Constants.TRACKING_ACTIVE;
import static ru.wtg.whereaminowserver.helpers.Constants.TRACKING_CONNECTING;
import static ru.wtg.whereaminowserver.helpers.Constants.TRACKING_DISABLED;
import static ru.wtg.whereaminowserver.helpers.Constants.TRACKING_GPS_REJECTED;

public class State extends MultiDexApplication {

    private static State instance = null;
    private static WhereAmINowService service;
    private static MainActivity activity;
    private SharedPreferences sharedPreferences;

    private String deviceId;
    private String token;
    private MyUsers users;
    private MyUser me;
    public MyTracking myTracking;
    private Notification notification;
    private boolean gpsAccessEnabled;
    private boolean gpsAccessRequested;
    private boolean serviceBound;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        registerEntityHolder(new PropertiesHolder());

        MyUser me = State.getInstance().getMe();
        if(me == null){
            me = new MyUser();
            setMe(me);
            me.fire(MyUser.ASSIGN_TO_CAMERA, 0);

            String name = getStringPreference("my_name",null);
            me.fire(MyUser.CHANGE_NAME, name);
            me.fire(MyUser.CHANGE_COLOR, Color.BLUE);
        }
        me.fire(MyUser.MAKE_ACTIVE);

        users = new MyUsers();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
                Log.e("whereaminow", "State", paramThrowable);

                Intent intent = new Intent(State.this, ExceptionActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(EXCEPTION, paramThrowable);
                startActivity(intent);

                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(2);
            }
        });

        Intent intent = new Intent(State.this, WhereAmINowService.class);
        if (!serviceBound) bindService(intent, serviceConnection, BIND_AUTO_CREATE);

    }

    public static State getInstance() {
//        if (instance == null) {
//            synchronized (State.class){
//                if (instance == null) {
//                    instance = new State();
//                }
//            }
//        }
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


    public Context getApplication(){
        return this;
    }

    public boolean disconnected() {
        return myTracking == null || myTracking.getStatus() == TRACKING_DISABLED;
    }

    public boolean rejected() {
        return myTracking != null && myTracking.getStatus() == TRACKING_GPS_REJECTED;
    }

    public boolean tracking() {
        return myTracking != null && myTracking.getStatus() == TRACKING_ACTIVE;
    }

    public boolean connecting() {
        return myTracking != null && myTracking.getStatus() == TRACKING_CONNECTING;
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

//    public int getNumber() {
//        return number;
//    }
//
//    public void setNumber(int number) {
//        this.number = number;
//    }

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

    private HashMap<String,EntityHolder> entityHolders = new HashMap<>();

    public void registerEntityHolder(EntityHolder entityHolder) {
        entityHolders.put(entityHolder.getType(), entityHolder);
    }

    public HashMap<String,EntityHolder> getEntityHolders(){
        return entityHolders;
    }

    public void clearViewHolders(){
        Iterator<Map.Entry<String,EntityHolder>> iter = entityHolders.entrySet().iterator();
        while (iter.hasNext()){
            Map.Entry<String, EntityHolder> entry = iter.next();
            if(entry.getValue() instanceof AbstractViewHolder) {
                iter.remove();
            }
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, final IBinder binder) {
            serviceBound = true;
            System.out.println("onServiceConnected");
        }

        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
            System.out.println("onServiceDisconnected");
        }
    };
}
