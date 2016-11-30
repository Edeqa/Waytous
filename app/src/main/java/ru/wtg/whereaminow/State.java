package ru.wtg.whereaminow;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import ru.wtg.whereaminow.helpers.GeoTrackFilter;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;
import ru.wtg.whereaminow.holders.AbstractViewHolder;
import ru.wtg.whereaminow.holders.LoggerHolder;
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

    public static final String SELECT_USER = "select";
    public static final String UNSELECT_USER = "unselect";
    public static final String CHANGE_NAME = "change_name";
    public static final String CHANGE_NUMBER = "change_number";
    public static final String CHANGE_COLOR = "change_color";
    public static final String CREATE_CONTEXT_MENU = "create_context_menu";
    public static final String CREATE_OPTIONS_MENU = "create_options_menu";
    public static final String CREATE_FAB_MENU = "create_fab_menu";
    public static final String CREATE_DRAWER_MENU = "create_drawer_menu";

    public static final String ADJUST_ZOOM = "adjust_zoom";
    public static final String MAKE_ACTIVE = "make_active";
    public static final String MAKE_INACTIVE = "make_inactive";

    public static final String NEW_TRACKING = "new_tracking";
    public static final String JOIN_TRACKING = "join_tracking";
    public static final String DISCONNECTED = "disconnected";
    public static final String STOP_TRACKING = "stop_tracking";
    public static final String ACCEPTED = "accepted";
    public static final String ERROR = "error";
    public static final String STARTED = "started";
    public static final String STOPPED = "stopped";
    public static final String SEND_LINK = "send_link";
    public static final String NEW_MESSAGE = "new_message";
    public static final String SEND_MESSAGE = "send_message";
    public static final String PRIVATE_MESSAGE = "private_message";
    public static final String USER_MESSAGE = "user_message";
    public static final String SHOW_MESSAGES = "show_messages";

    private static State instance = null;
    private static WhereAmINowService service;
    private static MainActivity activity;
    private SharedPreferences sharedPreferences;

    private ArrayList<String> userEntityActions = new ArrayList<>();
    private ArrayList<String> entityActions = new ArrayList<>();
    private ArrayList<String> userViewActions = new ArrayList<>();
    private ArrayList<String> viewActions = new ArrayList<>();


    private String deviceId;
    private String token;
    private MyUsers users;
    private MyUser me;
    public MyTracking myTracking;
    private Notification notification;
    private boolean gpsAccessAllowed;
    private boolean gpsAccessRequested;
    private boolean serviceBound;
    private GeoTrackFilter gpsFilter;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        registerEntityHolder(new PropertiesHolder());
//        registerEntityHolder(new LoggerHolder(this));
        registerEntityHolder(new MessagesHolder());
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
//        updateActions();
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
        viewActions.clear();
        userViewHolders.clear();
        userViewActions.clear();
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

    private void updateActions(){
        entityActions.clear();
        userEntityActions.clear();
        viewActions.clear();
        userViewActions.clear();

        userViewActions.addAll(Arrays.asList(SELECT_USER, UNSELECT_USER,
                CHANGE_NUMBER,CHANGE_COLOR,
                CREATE_CONTEXT_MENU,CREATE_OPTIONS_MENU,
                ADJUST_ZOOM,MAKE_ACTIVE,MAKE_INACTIVE));

        for(Map.Entry<String, EntityHolder> entry: getEntityHolders().entrySet()){
            entityActions.addAll(Arrays.asList(entry.getValue().getOwnEvents()));
        }
        for(Map.Entry<String, EntityHolder> entry: getUserEntityHolders().entrySet()){
            userEntityActions.addAll(Arrays.asList(entry.getValue().getOwnEvents()));
        }
        for(Map.Entry<String, AbstractViewHolder> entry: getViewHolders().entrySet()){
            viewActions.addAll(Arrays.asList(entry.getValue().getOwnEvents()));
        }
        for(Map.Entry<String, AbstractViewHolder> entry: getUserViewHolders().entrySet()){
            userViewActions.addAll(Arrays.asList(entry.getValue().getOwnEvents()));
        }
        System.out.println("ENTITYACTIONS:"+ entityActions);
        System.out.println("USERENTITYACTIONS:"+ userEntityActions);
        System.out.println("VIEWACTIONS:"+ viewActions);
        System.out.println("USERVIEWACTIONS:"+ userViewActions);
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
}
