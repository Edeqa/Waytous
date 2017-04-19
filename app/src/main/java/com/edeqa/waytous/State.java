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

import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.helpers.GeoTrackFilter;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.helpers.MyUsers;
import com.edeqa.waytous.holders.GpsHolder;
import com.edeqa.waytous.holders.MessagesHolder;
import com.edeqa.waytous.holders.NotificationHolder;
import com.edeqa.waytous.holders.PropertiesHolder;
import com.edeqa.waytous.holders.TrackingHolder;
import com.edeqa.waytous.interfaces.EntityHolder;
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

import static com.edeqa.waytous.State.EVENTS.CHANGE_COLOR;
import static com.edeqa.waytous.State.EVENTS.CHANGE_NAME;
import static com.edeqa.waytous.State.EVENTS.MAKE_ACTIVE;
import static com.edeqa.waytous.State.EVENTS.SELECT_USER;

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

        registerEntityHolder(new PropertiesHolder(this),null); // ---> need to be first!
        registerEntityHolder(new TrackingHolder(this),null); // ---> need to be second!
//        registerEntityHolder(new LoggerHolder(),null);
        registerEntityHolder(new MessagesHolder(this),null); // ---> need to be before NotificationHolder
        registerEntityHolder(new NotificationHolder(this),null); // ---> need to be after MessagesHolder
        registerEntityHolder(new GpsHolder(this),null);

        gpsFilter = new GeoTrackFilter(1.);

        MyUser me = State.getInstance().getMe();
        if(me == null){
            me = new MyUser();
            setMe(me);
            me.setUser(true);
            me.fire(SELECT_USER, 0);

            String name = getStringPreference("my_name",null);
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
        users.forAllUsers(new MyUsers.Callback() {
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
        return tracking == null || EVENTS.TRACKING_DISABLED.equals(tracking.getStatus());
    }

    public boolean tracking_connecting() {
        return tracking != null && EVENTS.TRACKING_CONNECTING.equals(tracking.getStatus());
    }

    public boolean tracking_reconnecting() {
        return tracking != null && EVENTS.TRACKING_RECONNECTING.equals(tracking.getStatus());
    }

    public boolean tracking_error() {
        return tracking != null && EVENTS.TRACKING_ERROR.equals(tracking.getStatus());
    }

    public boolean tracking_expired() {
        return tracking != null && EVENTS.TRACKING_EXPIRED.equals(tracking.getStatus());
    }

    public boolean tracking_active() {
        return tracking != null &&  EVENTS.TRACKING_ACTIVE.equals(tracking.getStatus());
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

    public void registerEntityHolder(EntityHolder holder, MainActivity context) {
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

    public HashMap<String,EntityHolder> getAllHolders(){
        HashMap<String,EntityHolder> res = new LinkedHashMap<>();
        for(Map.Entry<String,EntityHolder> entry: entityHolders.entrySet()){
            res.put(entry.getKey(),entry.getValue());
        }
        for(Map.Entry<String,EntityHolder> entry: userEntityHolders.entrySet()){
            res.put(entry.getKey(),entry.getValue());
        }
        for(Map.Entry<String,AbstractViewHolder> entry: viewHolders.entrySet()){
            res.put(entry.getKey(),entry.getValue());
        }
        for(Map.Entry<String,AbstractViewHolder> entry: userViewHolders.entrySet()){
            res.put(entry.getKey(),entry.getValue());
        }
        return res;
    }

    public void clearViewHolders(){
        viewHolders.clear();
//        viewEvents.clear();
        userViewHolders.clear();
//        userViewEvents.clear();
    }

    public EntityHolder getEntityHolder(String type){
        if(entityHolders.containsKey(type)) return entityHolders.get(type);
        if(userEntityHolders.containsKey(type)) return userEntityHolders.get(type);
        if(viewHolders.containsKey(type)) return viewHolders.get(type);
        if(userViewHolders.containsKey(type)) return userViewHolders.get(type);
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
    }

    public void fire(final String EVENT){
        switch(EVENT){
            case EVENTS.ACTIVITY_DESTROY:
                if(tracking_disabled() || tracking_error() || tracking_expired()) {
                    clearViewHolders();
                    entityHolders.clear();
//                    entityEvents.clear();
                    userEntityHolders.clear();
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
        if(entityHolders.containsKey(PropertiesHolder.TYPE)) {
            return (PropertiesHolder) entityHolders.get(PropertiesHolder.TYPE);
        } else {
            return null;
        }
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

    public static class EVENTS {
        public static final String SELECT_USER = "select";
        public static final String SELECT_SINGLE_USER = "select_single";
        public static final String UNSELECT_USER = "unselect";
        public static final String MAKE_ACTIVE = "make_active";
        public static final String MAKE_INACTIVE = "make_inactive";
        public static final String CHANGE_NAME = "change_name";
        public static final String CHANGE_NUMBER = "change_number";
        public static final String CHANGE_COLOR = "change_color";
        public static final String SYSTEM_MESSAGE = "system_message";
        public static final String MAP_MY_LOCATION_BUTTON_CLICKED = "map_my_location_button_clicked";
        public static final String MARKER_CLICK = "marker_click";

        public static final String CREATE_CONTEXT_MENU = "create_context_menu";
        public static final String CREATE_OPTIONS_MENU = "create_options_menu";
        public static final String PREPARE_OPTIONS_MENU = "prepare_options_menu";
        public static final String PREPARE_FAB = "prepare_fab";
        public static final String CREATE_DRAWER = "create_drawer";
        public static final String PREPARE_DRAWER = "prepare_drawer";
        public static final String DROPPED_TO_USER = "dropped_to_user";

        public static final String ACTIVITY_CREATE = "activity_create";
        public static final String ACTIVITY_PAUSE = "activity_pause";
        public static final String ACTIVITY_RESUME = "activity_resume";
        public static final String ACTIVITY_DESTROY = "activity_destroy";
        public static final String ACTIVITY_RESULT = "activity_result";

        public static final String TRACKING_NEW = "tracking_new";
        public static final String TRACKING_JOIN = "tracking_join";
        public static final String TRACKING_STOP = "tracking_stop";
        public static final String TRACKING_DISABLED = "tracking_disabled";
        public static final String TRACKING_CONNECTING = "tracking_connecting";
        public static final String TRACKING_ACTIVE = "tracking_active";
        public static final String TRACKING_RECONNECTING = "tracking_reconnecting";
        public static final String TRACKING_EXPIRED = "tracking_expired";
        public static final String TRACKING_ERROR = "tracking_error";
        public static final String TOKEN_CREATED = "token_created";

        public static final String MOVING_CLOSE_TO = "moving_close_to";
        public static final String MOVING_AWAY_FROM = "moving_away_from";
    }

}
