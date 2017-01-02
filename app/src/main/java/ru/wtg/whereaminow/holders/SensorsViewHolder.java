package ru.wtg.whereaminow.holders;

import android.app.Activity;
import android.location.Location;
import android.view.WindowManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MapStyleOptions;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationAccuracy;
import io.nlopez.smartlocation.location.config.LocationParams;
import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.helpers.LightSensorManager;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;
import ru.wtg.whereaminow.helpers.Utils;
import ru.wtg.whereaminow.interfaces.SimpleCallback;

import static ru.wtg.whereaminow.State.ACTIVITY_PAUSE;
import static ru.wtg.whereaminow.State.ACTIVITY_RESUME;
import static ru.wtg.whereaminow.State.TRACKING_ACTIVE;
import static ru.wtg.whereaminow.State.TRACKING_DISABLED;
import static ru.wtg.whereaminow.helpers.LightSensorManager.DAY;
import static ru.wtg.whereaminow.helpers.LightSensorManager.NIGHT;

/**
 * Created 11/27/16.
 */
public class SensorsViewHolder extends AbstractViewHolder {

    public static final String REQUEST_LOCATION_SINGLE = "request_location_single";
    public static final String REQUEST_LOCATION_UPDATES = "request_location_updates";

    public static final String REQUEST_MODE_DAY = "request_mode_day";
    public static final String REQUEST_MODE_NIGHT = "request_mode_night";
    public static final String REQUEST_MODE_NORMAL = "request_mode_normal";
    public static final String REQUEST_MODE_SATELLITE = "request_mode_satellite";
    public static final String REQUEST_MODE_TERRAIN = "request_mode_terrain";

    private final Activity context;
    private final LightSensorManager lightSensor;
    private GoogleMap map;

    public SensorsViewHolder(final Activity context) {
        this.context = context;

        onEnvironmentChangeListener.call(DAY);
        lightSensor = new LightSensorManager(context);
        lightSensor.setOnEnvironmentChangeListener(onEnvironmentChangeListener);
    }

    @Override
    public String getType() {
        return "sensors";
    }

    @Override
    public boolean dependsOnEvent() {
        return true;
    }

    @Override
    public boolean dependsOnUser() {
        return false;
    }

    @Override
    public AbstractView create(MyUser myUser) {
        return null;
    }

    @Override
    public boolean onEvent(String event, Object object) {
        switch (event) {
            case TRACKING_ACTIVE:
                SmartLocation.with(context).location().stop();
                context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                lightSensor.enable();
                break;
            case TRACKING_DISABLED:
                context.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                lightSensor.disable();
                enableLocationUpdates();
                onEnvironmentChangeListener.call(DAY);
                break;
            case ACTIVITY_PAUSE:
                SmartLocation.with(context).location().stop();
                context.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                lightSensor.disable();
                break;
            case ACTIVITY_RESUME:
                if(State.getInstance().tracking_active()) {
                    context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    lightSensor.enable();
                } else {
                    enableLocationUpdates();
                    onEnvironmentChangeListener.call(DAY);
                }
                break;
            case REQUEST_LOCATION_SINGLE:
                SmartLocation.with(context).location().oneFix().start(new OnLocationUpdatedListener() {
                    @Override
                    public void onLocationUpdated(final Location location) {
                        State.getInstance().getMe().addLocation(Utils.normalizeLocation(State.getInstance().getGpsFilter(), location));
                        enableLocationUpdates();
                    }
                });
                break;
            case REQUEST_MODE_DAY:
                if(map != null && map.getMapType() == GoogleMap.MAP_TYPE_SATELLITE){
                    break;
                }
                if(map != null) map.setMapStyle(null);
                State.getInstance().getUsers().forAllUsers(new MyUsers.Callback() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        myUser.removeViews();
                    }
                });
                context.setTheme(R.style.DayTheme);
                State.getInstance().getUsers().forAllUsers(new MyUsers.Callback() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        myUser.createViews();
                    }
                });
                break;
            case REQUEST_MODE_NIGHT:
                if(map != null && map.getMapType() == GoogleMap.MAP_TYPE_SATELLITE){
                    break;
                }
                if(map != null) map.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.mapstyle_night));
                State.getInstance().getUsers().forAllUsers(new MyUsers.Callback() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        myUser.removeViews();
                    }
                });
                context.setTheme(R.style.NightTheme);
                State.getInstance().getUsers().forAllUsers(new MyUsers.Callback() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        myUser.createViews();
                    }
                });
                break;
            case REQUEST_MODE_NORMAL:
                if(map != null) map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                lightSensor.enable();
                onEvent(REQUEST_MODE_DAY, null);
                break;
            case REQUEST_MODE_SATELLITE:
                if(map != null){
                    lightSensor.disable();
                    onEvent(REQUEST_MODE_NIGHT, null);
                    map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                }
                break;
            case REQUEST_MODE_TERRAIN:
                if(map != null) map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                lightSensor.disable();
                onEvent(REQUEST_MODE_DAY, null);
                break;
        }
        return true;
    }

    private void enableLocationUpdates(){
        LocationParams.Builder builder = new LocationParams.Builder()
                .setAccuracy(LocationAccuracy.HIGH).setDistance(3).setInterval(1000);
        SmartLocation.with(context).location().continuous().config(builder.build()).start(locationUpdateListener);
    }

    public SensorsViewHolder setMap(GoogleMap map) {
        this.map = map;
        return this;
    }

    private SimpleCallback<String> onEnvironmentChangeListener = new SimpleCallback<String>() {
        @Override
        public void call(String environment) {
            switch(environment){
                case DAY:
                    State.getInstance().fire(REQUEST_MODE_DAY);
                    break;
                case NIGHT:
                    State.getInstance().fire(REQUEST_MODE_NIGHT);
                    break;
            }
        }
    };

    private OnLocationUpdatedListener locationUpdateListener = new OnLocationUpdatedListener() {
        @Override
        public void onLocationUpdated(final Location location) {
            if(!State.getInstance().tracking_active()) {
                State.getInstance().getMe().addLocation(Utils.normalizeLocation(State.getInstance().getGpsFilter(), location));
            }
        }
    };
}
