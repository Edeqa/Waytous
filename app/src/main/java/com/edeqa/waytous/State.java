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
import com.edeqa.waytous.abstracts.AbstractProperty;
import com.edeqa.waytous.abstracts.AbstractPropertyHolder;
import com.edeqa.waytous.abstracts.AbstractView;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.helpers.Events;
import com.edeqa.waytous.helpers.GeoTrackFilter;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.helpers.MyUsers;
import com.edeqa.waytous.holders.GpsHolder;
import com.edeqa.waytous.holders.LoggerHolder;
import com.edeqa.waytous.holders.MessagesHolder;
import com.edeqa.waytous.holders.NotificationHolder;
import com.edeqa.waytous.holders.PropertiesHolder;
import com.edeqa.waytous.holders.TrackingHolder;
import com.edeqa.waytous.interfaces.EntityHolder;
import com.edeqa.waytous.interfaces.Runnable2;
import com.edeqa.waytous.interfaces.Tracking;
import com.edeqa.waytousserver.helpers.Constants;
import com.edeqa.waytousserver.helpers.SensitiveData;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.edeqa.waytous.helpers.Events.CHANGE_COLOR;
import static com.edeqa.waytous.helpers.Events.CHANGE_NAME;
import static com.edeqa.waytous.helpers.Events.MAKE_ACTIVE;
import static com.edeqa.waytous.helpers.Events.SELECT_USER;
import static com.edeqa.waytous.holders.PropertiesHolder.PREFERENCE_MY_NAME;

public class State extends MultiDexApplication {

    public static final int API = 1;

    private static State instance = null;

    private HashMap<String, EntityHolder> entityHolders = new LinkedHashMap<>();
    private HashMap<String, EntityHolder> userEntityHolders = new LinkedHashMap<>();
    private HashMap<String, AbstractViewHolder> viewHolders = new LinkedHashMap<>();
    private HashMap<String, AbstractViewHolder> userViewHolders = new LinkedHashMap<>();

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
    private LinkedHashMap<String, AbstractViewHolder> userViewHolders2;

    public static State getInstance() {
        return instance ;
    }

    @Override
    public void onCreate() {

//        Constants.SENSITIVE = new SensitiveData(new String[]{""+BuildConfig.DEBUG});
        try {
            InputStream stream = getAssets().open("options.json");

            Reader reader = new InputStreamReader(stream);
            Constants.SENSITIVE = new SensitiveData(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onCreate();
        instance = this;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        systemPropertyBus = new EventBus("SystemEntityHolder");
        systemViewBus = new EventBus("SystemViewHolder");
        userPropertyHolders = new LinkedHashMap<>();
        userViewHolders2 = new LinkedHashMap<>();

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
        registerEntityHolder(new LoggerHolder(),null);

        gpsFilter = new GeoTrackFilter(1.);

        MyUser me = State.getInstance().getMe();
        if(me == null){
            me = new MyUser();
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
                Log.e("Waytous", "State", paramThrowable);

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
//        bus.register(holder); // ---> need to be first!


/*
        bus.register(new TrackingHolder(this)); // ---> need to be second!
        bus.register(new LoggerHolder());
        bus.register(new MessagesHolder(this)); // ---> need to be before NotificationHolder
        bus.register(new NotificationHolder(this)); // ---> need to be after MessagesHolder
        bus.register(new GpsHolder(this));
*/

        /*if(holder.getType() != null) {

            if (holder.dependsOnUser()) {
                userPropertyHolders.put(holder.getType(), holder);
            } else if (holder instanceof AbstractViewHolder) {
                getSystemViewBus().register(holder);
            } else if (holder instanceof AbstractPropertyHolder) {
                getSystemPropertyBus().register(holder);
            }
        }*/

        if(holder.getType() != null) {
            if (holder instanceof AbstractViewHolder) {
                if (holder.dependsOnEvent()) {
                    systemViewBus.register(holder);

/*
                    if(viewHolders.containsKey(holder.getType()) && viewHolders.get(holder.getType()) != null) {
                        viewHolders.get(holder.getType()).setContext(context);
                    } else {
                        viewHolders.put(holder.getType(), (AbstractViewHolder) holder);
                    }
*/
                }
                if (holder.dependsOnUser()) {
                    userViewHolders2.put(holder.getType(), (AbstractViewHolder) holder);

/*
                    if(userViewHolders.containsKey(holder.getType()) && userViewHolders.get(holder.getType()) != null) {
                        userViewHolders.get(holder.getType()).setContext(context);
                    } else {
                        userViewHolders.put(holder.getType(), (AbstractViewHolder) holder);
                    }
*/
                }
            } else {
                if (holder.dependsOnEvent()) {
                    systemPropertyBus.register(holder);
                }
                if (holder.dependsOnUser()) {
                    userPropertyHolders.put(holder.getType(), holder);
                }
            }
        }

/*
        if(holder.getType() != null) {
            if (holder instanceof AbstractViewHolder) {
                if (holder.dependsOnEvent()) {
                    if(viewHolders.containsKey(holder.getType()) && viewHolders.get(holder.getType()) != null) {
                        viewHolders.get(holder.getType()).setContext(context);
                    } else {
                        viewHolders.put(holder.getType(), (AbstractViewHolder) holder);
                    }
                }
                if (holder.dependsOnUser()) {
                    if(userViewHolders.containsKey(holder.getType()) && userViewHolders.get(holder.getType()) != null) {
                        userViewHolders.get(holder.getType()).setContext(context);
                    } else {
                        userViewHolders.put(holder.getType(), (AbstractViewHolder) holder);
                    }
                }
            } else {
                if (holder.dependsOnEvent()) {
                    entityHolders.put(holder.getType(), holder);
                }
                if (holder.dependsOnUser()) {
                    userEntityHolders.put(holder.getType(), holder);
                }
            }
        }
*/
    }

    public HashMap<String,EntityHolder> getEntityHolders(){
        return entityHolders;
    }

    public HashMap<String,EntityHolder> getUserEntityHolders(){
        return userEntityHolders;
    }

    public HashMap<String,AbstractViewHolder> getViewHolders(){
        return viewHolders;
    }

    public HashMap<String,AbstractViewHolder> getUserViewHolders(){
        return userViewHolders;
    }

    public HashMap<String,AbstractPropertyHolder> getAllHolders(){
        HashMap<String,AbstractPropertyHolder> res = new LinkedHashMap<>();
        for(AbstractPropertyHolder item: systemPropertyBus.getHolders()){
            res.put(item.getType(), item);
        }
//        for(AbstractPropertyHolder item: userPropertyHolders.getHolders()){
//            res.put(item.getType(), item);
//        }
        return res;
    }

    public void clearViewHolders(){
        viewHolders.clear();
//        viewEvents.clear();
        userViewHolders.clear();
//        userViewEvents.clear();
    }

    public AbstractEntityHolder getEntityHolder(String type){
        if(getSystemPropertyBus().getHolder(type) != null) return getSystemPropertyBus().getHolder(type);
//        if(getUserPropertyBus().getHolder(type) != null) return getUserPropertyBus().getHolder(type);
        if(getSystemViewBus().getHolder(type) != null) return getSystemViewBus().getHolder(type);
//        if(getUserViewBus().getHolder(type) != null) return getUserViewBus().getHolder(type);

/*
        if(entityHolders.containsKey(type)) return entityHolders.get(type);
        if(userEntityHolders.containsKey(type)) return userEntityHolders.get(type);
        if(viewHolders.containsKey(type)) return viewHolders.get(type);
        if(userViewHolders.containsKey(type)) return userViewHolders.get(type);
*/
        return null;
    }

 /*   private void updateEvents(){
        entityEvents.clear();
        userEntityEvents.clear();
        viewEvents.clear();
        userViewEvents.clear();

        userViewEvents.addAll(Arrays.asList(SELECT_USER, UNSELECT_USER,
                CHANGE_NUMBER,CHANGE_COLOR,
                CREATE_CONTEXT_MENU, PREPARE_OPTIONS_MENU,
                MAKE_ACTIVE,MAKE_INACTIVE));

        for(Map.Entry<String, EntityHolder> entry: getEntityHolders().entrySet()){
            entityEvents.addAll(Arrays.asList(entry.getValue().exportOwnEvents()));
        }
        for(Map.Entry<String, EntityHolder> entry: getUserEntityHolders().entrySet()){
            userEntityEvents.addAll(Arrays.asList(entry.getValue().exportOwnEvents()));
        }
        for(Map.Entry<String, AbstractViewHolder> entry: getViewHolders().entrySet()){
            viewEvents.addAll(Arrays.asList(entry.getValue().exportOwnEvents()));
        }
        for(Map.Entry<String, AbstractViewHolder> entry: getUserViewHolders().entrySet()){
            userViewEvents.addAll(Arrays.asList(entry.getValue().exportOwnEvents()));
        }
        System.out.println("ENTITYACTIONS:"+ entityEvents);
        System.out.println("USERENTITYACTIONS:"+ userEntityEvents);
        System.out.println("VIEWACTIONS:"+ viewEvents);
        System.out.println("USERVIEWACTIONS:"+ userViewEvents);
    }*/

    public void fire(final String EVENT, final Object object){
        systemPropertyBus.post(EVENT, object);
        systemViewBus.post(EVENT, object);
/*
        continueFiring.set(true);
        for(Map.Entry<String,EntityHolder> entry: getEntityHolders().entrySet()){
            if(entry.getValue() != null){
                try {
                    if(!continueFiring.get()) break;
                    continueFiring.set(entry.getValue().onEvent(EVENT, object));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                for(Map.Entry<String,AbstractViewHolder> entry: getViewHolders().entrySet()){
                    if(entry.getValue() != null){
                        try {
                            if(!continueFiring.get()) break;
                            continueFiring.set(entry.getValue().onEvent(EVENT, object));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
*/
    }

    public void fire(final String EVENT){
        switch(EVENT){
            case Events.ACTIVITY_DESTROY:
                if(tracking_disabled() || tracking_error() || tracking_expired()) {

//                    clearViewHolders();
//                    entityHolders.clear();
//                    entityEvents.clear();
//                    userEntityHolders.clear();
//                    userEntityEvents.clear();

                    Intent intent = new Intent(State.this, WaytousService.class);
                    stopService(intent);
                    System.exit(0);
                }
                break;
            default:
                break;
        }
        fire(EVENT, null);
    }

    public PropertiesHolder getPropertiesHolder(){
        return (PropertiesHolder) getSystemPropertyBus().getHolder(PropertiesHolder.TYPE);
/*
        if(entityHolders.containsKey(PropertiesHolder.TYPE)) {
            return (PropertiesHolder) entityHolders.get(PropertiesHolder.TYPE);
        } else {
            return null;
        }
*/
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

    public LinkedHashMap<String, AbstractViewHolder> getUserViewHolders2() {
        return userViewHolders2;
    }

    public EventBus.Runner getAndroidRunner() {
        return androidRunner;
    }


}
