package ru.wtg.whereaminow;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import ru.wtg.whereaminow.helpers.GeoTrackFilter;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;
import ru.wtg.whereaminow.holders.AbstractProperty;
import ru.wtg.whereaminow.holders.AbstractPropertyHolder;
import ru.wtg.whereaminow.holders.AbstractView;
import ru.wtg.whereaminow.holders.AbstractViewHolder;
import ru.wtg.whereaminow.holders.MessagesHolder;
import ru.wtg.whereaminow.holders.NotificationHolder;
import ru.wtg.whereaminow.holders.PropertiesHolder;
import ru.wtg.whereaminow.interfaces.EntityHolder;
import ru.wtg.whereaminow.service_helpers.MyTracking;

import static ru.wtg.whereaminow.ExceptionActivity.EXCEPTION;
import static ru.wtg.whereaminowserver.helpers.Constants.TRACKING_ACTIVE;
import static ru.wtg.whereaminowserver.helpers.Constants.TRACKING_CONNECTING;
import static ru.wtg.whereaminowserver.helpers.Constants.TRACKING_DISABLED;
import static ru.wtg.whereaminowserver.helpers.Constants.TRACKING_GPS_REJECTED;

public class State extends MultiDexApplication {

    public static final int API = 1;

    public static final String SELECT_USER = "select";
    public static final String UNSELECT_USER = "unselect";
    public static final String MAKE_ACTIVE = "make_active";
    public static final String MAKE_INACTIVE = "make_inactive";
    public static final String CHANGE_NAME = "change_name";
    public static final String CHANGE_NUMBER = "change_number";
    public static final String CHANGE_COLOR = "change_color";
    public static final String CHANGE_TYPE = "change_type";
    public static final String CREATE_CONTEXT_MENU = "create_context_menu";
    public static final String CREATE_OPTIONS_MENU = "create_options_menu";
    public static final String PREPARE_OPTIONS_MENU = "prepare_options_menu";
    public static final String PREPARE_FAB = "prepare_fab";
    public static final String CREATE_DRAWER = "create_drawer";
    public static final String PREPARE_DRAWER = "prepare_drawer";

    public static final String ACTIVITY_CREATE = "activity_create";
    public static final String ACTIVITY_PAUSE = "activity_pause";
    public static final String ACTIVITY_RESUME = "activity_resume";
    public static final String ACTIVITY_DESTROY = "activity_destroy";
    public static final String ACTIVITY_RESULT = "activity_result";

    public static final String TRACKING_NEW = "tracking_new";
    public static final String TRACKING_JOIN = "tracking_join";
    public static final String TRACKING_STOP = "tracking_stop";
    public static final String TRACKING_STARTED = "tracking_started";
    public static final String TRACKING_ACCEPTED = "tracking_accepted";
    public static final String TRACKING_STOPPED = "tracking_stopped";
    public static final String CONNECTION_DISCONNECTED = "disconnected";
    public static final String CONNECTION_ERROR = "error";
    public static final String TOKEN_CREATED = "token_created";
    public static final String TOKEN_CHANGED = "token_changed";

    private MyTracking tracking;

    private static State instance = null;
    private static WhereAmINowService service;

    private HashMap<String, AbstractProperty> userEntityEvents = new HashMap<>();
    private HashMap<String, AbstractPropertyHolder> entityEvents = new HashMap<>();
    private HashMap<String, AbstractView> userViewEvents = new HashMap<>();
    private HashMap<String, AbstractViewHolder> viewEvents = new HashMap<>();

    private SharedPreferences sharedPreferences;
    private MyUsers users;
    private MyUser me;
    private GeoTrackFilter gpsFilter;
    private Notification notification;
    private String deviceId;
    private String token;
    private boolean gpsAccessAllowed;
    private boolean gpsAccessRequested;
    private boolean serviceBound;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        registerEntityHolder(new PropertiesHolder(this));
//        registerEntityHolder(new LoggerHolder());
        registerEntityHolder(new MessagesHolder(this));
        registerEntityHolder(new NotificationHolder(this));

        gpsFilter = new GeoTrackFilter(1.);

        MyUser me = State.getInstance().getMe();
        if(me == null){
            me = new MyUser();
            setMe(me);
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
        return instance ;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        users.forAllUsers(new MyUsers.Callback() {
            @Override
            public void call(Integer number, MyUser myUser) {
                myUser.removeViews();
//                Location lastLocation = myUser.getLocation();
//                myUser.getLocations().clear();
//                myUser.addLocation(lastLocation);
                myUser.createViews();
            }
        });
    }

    public WhereAmINowService getService() {
        return service;
    }

    public void setService(WhereAmINowService service) {
        State.service = service;
    }

    public Context getApplication(){
        return this;
    }

    public boolean disconnected() {
        return tracking == null || tracking.getStatus() == TRACKING_DISABLED;
    }

    public boolean rejected() {
        return tracking != null && tracking.getStatus() == TRACKING_GPS_REJECTED;
    }

    public boolean tracking() {
        return tracking != null && tracking.getStatus() == TRACKING_ACTIVE;
    }

    public boolean connecting() {
        return tracking != null && tracking.getStatus() == TRACKING_CONNECTING;
    }

    public String getDeviceId() {
        if(deviceId == null) {
            deviceId = getStringPreference("device_id", null);
            if(deviceId == null) {
                deviceId = UUID.randomUUID().toString();
                setPreference("device_id", deviceId);
            }
        }
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


    public void setGpsAccessRequested(boolean gpsAccessRequested) {
        this.gpsAccessRequested = gpsAccessRequested;
    }

    public boolean isGpsAccessRequested() {
        return gpsAccessRequested;
    }


    public String getStringPreference(String key, String defaultValue){
        return sharedPreferences.getString(key,defaultValue);
    }

    public boolean getBooleanPreference(String key, boolean defaultValue){
        return sharedPreferences.getBoolean(key,defaultValue);
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
            sharedPreferences.edit().putBoolean(key,value).apply();
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

    private HashMap<String,EntityHolder> entityHolders = new LinkedHashMap<>();
    private HashMap<String,EntityHolder> userEntityHolders = new LinkedHashMap<>();
    private HashMap<String,AbstractViewHolder> viewHolders = new LinkedHashMap<>();
    private HashMap<String,AbstractViewHolder> userViewHolders = new LinkedHashMap<>();

    public void registerEntityHolder(EntityHolder holder) {
        if(holder.getType() == null) return;
        if(holder instanceof AbstractViewHolder){
            if(holder.dependsOnEvent()) {
                viewHolders.put(holder.getType(), (AbstractViewHolder) holder);
            }
            if(holder.dependsOnUser()) {
                userViewHolders.put(holder.getType(), (AbstractViewHolder) holder);
            }
        } else {
            if(holder.dependsOnEvent()) {
                entityHolders.put(holder.getType(), holder);
            }
            if(holder.dependsOnUser()) {
                userEntityHolders.put(holder.getType(), holder);
            }
        }
//        updateEvents();
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
        viewEvents.clear();
        userViewHolders.clear();
        userViewEvents.clear();
    }

    public EntityHolder getEntityHolder(String type){
        if(entityHolders.containsKey(type)) return entityHolders.get(type);
        if(userEntityHolders.containsKey(type)) return userEntityHolders.get(type);
        if(viewHolders.containsKey(type)) return viewHolders.get(type);
        if(userViewHolders.containsKey(type)) return userViewHolders.get(type);
        return null;
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

    private void updateEvents(){
        entityEvents.clear();
        userEntityEvents.clear();
        viewEvents.clear();
        userViewEvents.clear();

/*        userViewEvents.addAll(Arrays.asList(SELECT_USER, UNSELECT_USER,
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
        }*/
        System.out.println("ENTITYACTIONS:"+ entityEvents);
        System.out.println("USERENTITYACTIONS:"+ userEntityEvents);
        System.out.println("VIEWACTIONS:"+ viewEvents);
        System.out.println("USERVIEWACTIONS:"+ userViewEvents);
    }

    public void fire(final String EVENT, final Object object){
        for(Map.Entry<String,EntityHolder> entry: getEntityHolders().entrySet()){
            if(entry.getValue() != null){
                try {
                    if(!entry.getValue().onEvent(EVENT, object)) break;
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
                            if(!entry.getValue().onEvent(EVENT, object)) break;
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
            case ACTIVITY_DESTROY:
                if(disconnected()) {
                    clearViewHolders();
                    entityHolders.clear();
                    entityEvents.clear();
                    userEntityHolders.clear();
                    userEntityEvents.clear();

                    Intent intent = new Intent(State.this, WhereAmINowService.class);
                    stopService(intent);
                    System.exit(0);
                }
                break;
            default:
                break;
        }
        fire(EVENT, null);
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

    public MyTracking getTracking() {
        return tracking;
    }

    public void setTracking(MyTracking tracking) {
        this.tracking = tracking;
    }

}
