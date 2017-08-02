package com.edeqa.waytous.holders.view;

import android.view.MenuItem;
import android.view.WindowManager;

import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;
import com.edeqa.waytous.State;
import com.edeqa.waytous.abstracts.AbstractView;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.helpers.LightSensorManager;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.interfaces.Runnable1;
import com.edeqa.waytous.interfaces.Runnable2;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MapStyleOptions;

import static com.edeqa.waytous.helpers.Events.ACTIVITY_PAUSE;
import static com.edeqa.waytous.helpers.Events.ACTIVITY_RESUME;
import static com.edeqa.waytous.helpers.Events.CREATE_DRAWER;
import static com.edeqa.waytous.helpers.Events.PREPARE_DRAWER;
import static com.edeqa.waytous.helpers.Events.TRACKING_ACTIVE;
import static com.edeqa.waytous.helpers.Events.TRACKING_DISABLED;
import static com.edeqa.waytous.helpers.LightSensorManager.DAY;
import static com.edeqa.waytous.helpers.LightSensorManager.NIGHT;


/**
 * Created 11/27/16.
 */
@SuppressWarnings("WeakerAccess")
public class SensorsViewHolder extends AbstractViewHolder {

    public static final String REQUEST_MODE_DAY = "request_mode_day"; //NON-NLS
    public static final String REQUEST_MODE_NIGHT = "request_mode_night"; //NON-NLS
    public static final String REQUEST_MODE_NORMAL = "request_mode_normal"; //NON-NLS
    public static final String REQUEST_MODE_SATELLITE = "request_mode_satellite"; //NON-NLS
    public static final String REQUEST_MODE_TERRAIN = "request_mode_terrain"; //NON-NLS
    public static final String REQUEST_MODE_TRAFFIC = "request_mode_traffic"; //NON-NLS

    private final LightSensorManager lightSensor;

    private String currentEnvironment = DAY;

    private GoogleMap map;

    private Runnable1<String> onEnvironmentChangeListener = new Runnable1<String>() {
        @Override
        public void call(String environment) {
            switch(environment){
                case DAY:
                    if(!DAY.equals(currentEnvironment)) {
                        currentEnvironment = DAY;
                        State.getInstance().fire(REQUEST_MODE_DAY);
                    }
                    break;
                case NIGHT:
                    if(!NIGHT.equals(currentEnvironment)) {
                        currentEnvironment = NIGHT;
                        State.getInstance().fire(REQUEST_MODE_NIGHT);
                    }
                    break;
            }
        }
    };

    public SensorsViewHolder(final MainActivity context) {
        super(context);

        onEnvironmentChangeListener.call(DAY);
        lightSensor = new LightSensorManager(context);
        lightSensor.setOnEnvironmentChangeListener(onEnvironmentChangeListener);

        Object m = State.getInstance().getPropertiesHolder().loadFor(getType());
        if(m != null) {
            switch ((int) m) {
                case GoogleMap.MAP_TYPE_SATELLITE:
                    State.getInstance().fire(REQUEST_MODE_SATELLITE);
                    break;
                case GoogleMap.MAP_TYPE_TERRAIN:
                    State.getInstance().fire(REQUEST_MODE_TERRAIN);
                    break;
            }
        }
        m = State.getInstance().getPropertiesHolder().loadFor(getType() + "_traffic"); //NON-NLS
        if(m != null && (Boolean)m) {
            State.getInstance().fire(REQUEST_MODE_TRAFFIC, m);
        }

        setMap(context.getMap());
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
                context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                lightSensor.enable();
                break;
            case TRACKING_DISABLED:
                context.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                lightSensor.disable();
                onEnvironmentChangeListener.call(DAY);
                break;
            case ACTIVITY_PAUSE:
                context.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                lightSensor.disable();
                break;
            case ACTIVITY_RESUME:
                if(State.getInstance().tracking_active()) {
                    context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    lightSensor.enable();
                } else {
                    onEnvironmentChangeListener.call(DAY);
                }
                break;
            case REQUEST_MODE_DAY:
                if(context.getMap() != null && map.getMapType() == GoogleMap.MAP_TYPE_SATELLITE){
                    break;
                }
                if(map != null) map.setMapStyle(null);
                State.getInstance().getPropertiesHolder().saveFor(getType(), null);

                State.getInstance().getUsers().forAllUsers(new Runnable2<Integer, MyUser>() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        myUser.removeViews();
                    }
                });
                context.setTheme(R.style.DayTheme);
                State.getInstance().getUsers().forAllUsers(new Runnable2<Integer, MyUser>() {
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
                State.getInstance().getPropertiesHolder().saveFor(getType(), null);

                State.getInstance().getUsers().forAllUsers(new Runnable2<Integer, MyUser>() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        myUser.removeViews();
                    }
                });
                context.setTheme(R.style.NightTheme);
                State.getInstance().getUsers().forAllUsers(new Runnable2<Integer, MyUser>() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        myUser.createViews();
                    }
                });
                break;
            case REQUEST_MODE_NORMAL:
                if(map != null) map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                State.getInstance().getPropertiesHolder().saveFor(getType(), null);

                lightSensor.enable();
                onEvent(REQUEST_MODE_DAY, null);
                break;
            case REQUEST_MODE_SATELLITE:
                if(map != null){
                    lightSensor.disable();
                    onEvent(REQUEST_MODE_NIGHT, null);
                    map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    State.getInstance().getPropertiesHolder().saveFor(getType(), GoogleMap.MAP_TYPE_SATELLITE);
                }
                break;
            case REQUEST_MODE_TERRAIN:
                if(map != null) map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                State.getInstance().getPropertiesHolder().saveFor(getType(), GoogleMap.MAP_TYPE_TERRAIN);
                lightSensor.disable();
                onEvent(REQUEST_MODE_DAY, null);
                break;
            case REQUEST_MODE_TRAFFIC:
                boolean state = !map.isTrafficEnabled();
                if(object != null) {
                    state = (boolean) object;
                }
                map.setTrafficEnabled(state);
                State.getInstance().getPropertiesHolder().saveFor(getType() + "_traffic", state); //NON-NLS
                break;
            case CREATE_DRAWER:
                DrawerViewHolder.ItemsHolder adder = (DrawerViewHolder.ItemsHolder) object;
                adder.add(R.id.drawer_section_map, R.string.traffic, R.string.traffic, R.drawable.ic_traffic_black_24dp).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        State.getInstance().fire(REQUEST_MODE_TRAFFIC);
                        return false;
                    }
                });
                adder.add(R.id.drawer_section_map, R.string.satellite, R.string.satellite, R.drawable.ic_satellite_black_24dp).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (context.getMap() != null && context.getMap().getMapType() != GoogleMap.MAP_TYPE_SATELLITE) {
                            State.getInstance().fire(REQUEST_MODE_SATELLITE);
                        } else {
                            State.getInstance().fire(REQUEST_MODE_NORMAL);
                        }
                        return false;
                    }
                });
                adder.add(R.id.drawer_section_map, R.string.terrain, R.string.terrain, R.drawable.ic_terrain_black_24dp).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (context.getMap() != null && context.getMap().getMapType() != GoogleMap.MAP_TYPE_TERRAIN) {
                            State.getInstance().fire(REQUEST_MODE_TERRAIN);
                        } else {
                            State.getInstance().fire(REQUEST_MODE_NORMAL);
                        }
                        return false;
                    }
                });
                break;
            case PREPARE_DRAWER:
                adder = (DrawerViewHolder.ItemsHolder) object;
                adder.findItem(R.string.satellite).setChecked(context.getMap().getMapType() == GoogleMap.MAP_TYPE_SATELLITE);
                adder.findItem(R.string.terrain).setChecked(context.getMap().getMapType() == GoogleMap.MAP_TYPE_TERRAIN);
                adder.findItem(R.string.traffic).setChecked(context.getMap().isTrafficEnabled());
                break;

        }
        return true;
    }

    public SensorsViewHolder setMap(GoogleMap map) {
        this.map = map;
        return this;
    }

}
