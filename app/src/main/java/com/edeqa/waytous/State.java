package com.edeqa.waytous;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.edeqa.eventbus.AbstractEntityHolder;
import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.interfaces.Runnable2;
import com.edeqa.waytous.abstracts.AbstractPropertyHolder;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.helpers.Events;
import com.edeqa.waytous.helpers.GeoTrackFilter;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.helpers.MyUsers;
import com.edeqa.waytous.holders.property.GpsHolder;
import com.edeqa.waytous.holders.property.MessagesHolder;
import com.edeqa.waytous.holders.property.NotificationHolder;
import com.edeqa.waytous.holders.property.PropertiesHolder;
import com.edeqa.waytous.holders.property.TrackingHolder;
import com.edeqa.waytous.interfaces.Tracking;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TooManyListenersException;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.edeqa.waytous.helpers.Events.CHANGE_COLOR;
import static com.edeqa.waytous.helpers.Events.CHANGE_NAME;
import static com.edeqa.waytous.helpers.Events.MAKE_ACTIVE;
import static com.edeqa.waytous.helpers.Events.SELECT_USER;
import static com.edeqa.waytous.holders.property.PropertiesHolder.PREFERENCE_MY_NAME;

public class State extends MultiDexApplication {

    public static final int API = 1;

    private static State instance = null;

    private Tracking tracking;
    private WaytousService service;

    private SharedPreferences sharedPreferences;
    private MyUsers users;
    private MyUser me;
    private GeoTrackFilter gpsFilter;
    private Notification notification;
    private AtomicBoolean continueFiring = new AtomicBoolean();
    private EventBus bus;

    private String deviceId;
    private String token;
    private boolean gpsAccessAllowed;
    private boolean gpsAccessRequested;
    private boolean serviceBound;


    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, final IBinder binder) {
            serviceBound = true;
        }
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };
    private EventBus<AbstractPropertyHolder> systemPropertyBus;
    private EventBus<AbstractViewHolder> systemViewBus;
    private EventBus.Runner androidRunner;
    private LinkedHashMap<String, AbstractPropertyHolder> userPropertyHolders;
    private LinkedHashMap<String, AbstractViewHolder> userViewHolders;

    public static State getInstance() {
        return instance ;
    }

    @Override
    public void onCreate() {

//        Constants.OPTIONS = new Options(new String[]{""+BuildConfig.DEBUG});
        try {
            InputStream stream = getAssets().open("options.json"); //NON-NLS

            Reader reader = new InputStreamReader(stream);
            Constants.OPTIONS = new Options(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onCreate();
        instance = this;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            systemPropertyBus = new EventBus<>("SystemPropertyHolder"); //NON-NLS
            systemViewBus = new EventBus<>("SystemViewHolder"); //NON-NLS
        } catch (TooManyListenersException e) {
            e.printStackTrace();
        }
        userPropertyHolders = new LinkedHashMap<>();
        userViewHolders = new LinkedHashMap<>();

        final Handler handler = new Handler(Looper.getMainLooper());
        androidRunner = new EventBus.Runner() {
            @Override
            public void post(Runnable runnable) {
                handler.post(runnable);
            }
        };

        systemViewBus.setRunner(androidRunner);

        registerEntityHolder(new PropertiesHolder(this),null); // ---> need to be first!
        registerEntityHolder(new TrackingHolder(this),null); // ---> need to be second!
        registerEntityHolder(new MessagesHolder(this),null); // ---> need to be before NotificationHolder
        registerEntityHolder(new NotificationHolder(this),null); // ---> need to be after MessagesHolder
        registerEntityHolder(new GpsHolder(this),null);
//        registerEntityHolder(new LoggerHolder(),null);

        gpsFilter = new GeoTrackFilter(1.);

        MyUser me = State.getInstance().getMe();
        if(me == null){
            try {
                me = new MyUser();
            } catch (TooManyListenersException e) {
                e.printStackTrace();
            }
            setMe(me);
            me.setUser(true);
            me.fire(SELECT_USER, 0);

            String name = getStringPreference(PREFERENCE_MY_NAME,null);
            me.fire(CHANGE_NAME, name);
            me.fire(CHANGE_COLOR, Color.BLUE);
        }
        me.fire(MAKE_ACTIVE);

        users = new MyUsers();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
                Log.e("Waytous", "State", paramThrowable); //NON-NLS

                Intent intent = new Intent(State.this, ExceptionActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(ExceptionActivity.EXCEPTION, paramThrowable);
                startActivity(intent);

                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(2);
            }
        });

        Intent intent = new Intent(State.this, WaytousService.class);
        if (!serviceBound) bindService(intent, serviceConnection, BIND_AUTO_CREATE);

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        users.forAllUsers(new Runnable2<Integer, MyUser>() {
            @Override
            public void call(Integer number, MyUser myUser) {
                myUser.removeViews();
                myUser.createViews();
            }
        });
    }

    public WaytousService getService() {
        return service;
    }

    public void setService(WaytousService service) {
        this.service = service;
    }

    public boolean tracking_disabled() {
        return tracking == null || Events.TRACKING_DISABLED.equals(tracking.getStatus());
    }

    public boolean tracking_connecting() {
        return tracking != null && Events.TRACKING_CONNECTING.equals(tracking.getStatus());
    }

    public boolean tracking_reconnecting() {
        return tracking != null && Events.TRACKING_RECONNECTING.equals(tracking.getStatus());
    }

    public boolean tracking_error() {
        return tracking != null && Events.TRACKING_ERROR.equals(tracking.getStatus());
    }

    public boolean tracking_expired() {
        return tracking != null && Events.TRACKING_EXPIRED.equals(tracking.getStatus());
    }

    public boolean tracking_active() {
        return tracking != null &&  Events.TRACKING_ACTIVE.equals(tracking.getStatus());
    }

    public String getDeviceId() {
//        System.out.println("DEVICEID:"+deviceId);
        if(deviceId == null) {
            deviceId = FirebaseInstanceId.getInstance().getToken();//getStringPreference("device_id", null);
//            if(deviceId == null) {
//                deviceId = FirebaseInstanceId.getInstance().getToken();
//                deviceId = UUID.randomUUID().toString();
//                setPreference("device_id", deviceId);
//            }
        }
        return deviceId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public MyUsers getUsers() {
        return users;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    public boolean isGpsAccessRequested() {
        return gpsAccessRequested;
    }

    public void setGpsAccessRequested(boolean gpsAccessRequested) {
        this.gpsAccessRequested = gpsAccessRequested;
    }

    public String getStringPreference(String key, String defaultValue){
        return sharedPreferences.getString(key,defaultValue);
    }

    public boolean getBooleanPreference(String key, boolean defaultValue){
        return sharedPreferences.getBoolean(key,defaultValue);
    }

    public Integer getIntegerPreference(String key, int defaultValue){
        return sharedPreferences.getInt(key,defaultValue);
    }

    public void setPreference(String key, String value){
        if(value != null && value.length()>0){
            sharedPreferences.edit().putString(key,value).apply();
        } else {
            sharedPreferences.edit().remove(key).apply();
        }
    }

    public void setPreference(String key, boolean value){
        if(value){
            sharedPreferences.edit().putBoolean(key,true).apply();
        } else {
            sharedPreferences.edit().remove(key).apply();
        }
    }

    public void setPreference(String key, int value){
        if(value > 0){
            sharedPreferences.edit().putInt(key,value).apply();
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

    public void registerEntityHolder(AbstractPropertyHolder holder, MainActivity context) {

        if(holder.getType() != null) {
            if (holder instanceof AbstractViewHolder) {
                if (holder.dependsOnEvent()) {
                    systemViewBus.registerOrUpdate((AbstractViewHolder) holder);
                }
                if (holder.dependsOnUser()) {
                    userViewHolders.put(holder.getType(), (AbstractViewHolder) holder);
                }
            } else {
                if (holder.dependsOnEvent()) {
                    systemPropertyBus.registerOrUpdate(holder);
                }
                if (holder.dependsOnUser()) {
                    userPropertyHolders.put(holder.getType(), holder);
                }
            }
        }
    }


    public HashMap<String,AbstractPropertyHolder> getAllHolders(){
        HashMap<String,AbstractPropertyHolder> res = new LinkedHashMap<>();
        for(Map.Entry<String, AbstractPropertyHolder> entry: systemPropertyBus.getHolders().entrySet()){
            res.put(entry.getKey(), entry.getValue());
        }
        return res;
    }

    public AbstractEntityHolder getEntityHolder(String type){
        if(getSystemPropertyBus().getHolder(type) != null) return (AbstractEntityHolder) getSystemPropertyBus().getHolder(type);
//        if(getUserPropertyBus().getHolder(type) != null) return getUserPropertyBus().getHolder(type);
        if(getSystemViewBus().getHolder(type) != null) return (AbstractEntityHolder) getSystemViewBus().getHolder(type);
//        if(getUserViewBus().getHolder(type) != null) return getUserViewBus().getHolder(type);

        return null;
    }

    public void fire(Runnable runnable) {
        systemPropertyBus.postRunnable(runnable);
        systemViewBus.postRunnable(runnable);

    }

    public void fire(String EVENT, Object object){

        Log.i("State","====>>> "+EVENT+":"+object); //NON-NLS
//        Log.i("State","====>>> "+EVENT+":"+object+" //"+Thread.currentThread().getStackTrace()[3]+";"+Thread.currentThread().getStackTrace()[4]); //NON-NLS
        switch(EVENT){
            case Events.ACTIVITY_DESTROY:
                /*if(tracking_disabled() || tracking_error() || tracking_expired()) {
                    Intent intent = new Intent(State.this, WaytousService.class);
                    stopService(intent);
                    System.exit(0);
                }*/
                break;
            default:
                break;
        }
        systemPropertyBus.post(EVENT, object);
        systemViewBus.post(EVENT, object);
    }

    public void fire(String EVENT){
        fire(EVENT, null);
    }

    public PropertiesHolder getPropertiesHolder(){
        return (PropertiesHolder) getSystemPropertyBus().getHolder(PropertiesHolder.TYPE);
    }

    public GeoTrackFilter getGpsFilter() {
        return gpsFilter;
    }

    public boolean isGpsAccessAllowed() {
        return gpsAccessAllowed;
    }

    public void setGpsAccessAllowed(boolean gpsAccessAllowed) {
        this.gpsAccessAllowed = gpsAccessAllowed;
    }

    public Tracking getTracking() {
        return tracking;
    }

    public void setTracking(Tracking tracking) {
        this.tracking = tracking;
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }


    public EventBus<AbstractPropertyHolder> getSystemPropertyBus() {
        return systemPropertyBus;
    }

    public EventBus<AbstractViewHolder> getSystemViewBus() {
        return systemViewBus;
    }

    public LinkedHashMap<String, AbstractPropertyHolder> getUserPropertyHolders() {
        return userPropertyHolders;
    }

    public LinkedHashMap<String, AbstractViewHolder> getUserViewHolders() {
        return userViewHolders;
    }

    public EventBus.Runner getAndroidRunner() {
        return androidRunner;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
