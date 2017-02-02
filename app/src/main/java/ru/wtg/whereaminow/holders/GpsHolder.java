package ru.wtg.whereaminow.holders;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.maps.android.SphericalUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationAccuracy;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesProvider;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.Utils;

import static ru.wtg.whereaminow.State.EVENTS.ACTIVITY_PAUSE;
import static ru.wtg.whereaminow.State.EVENTS.ACTIVITY_RESUME;
import static ru.wtg.whereaminow.State.EVENTS.TRACKING_ACTIVE;
import static ru.wtg.whereaminow.holders.InfoViewHolder.SHOW_INFO;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_TRACKING;

/**
 * Created 01/13/17.
 */
public class GpsHolder extends AbstractPropertyHolder {
    public static final String REQUEST_LOCATION_SINGLE = "request_location_single";
    private static final String TYPE = "Gps";
    private final LocationGooglePlayServicesProvider provider;
    private final SmartLocation.LocationControl smartLocation;
    private OnLocationUpdatedListener locationUpdateListener = new OnLocationUpdatedListener() {
        @Override
        public void onLocationUpdated(Location location) {
            if(location == null) return;
            location = Utils.normalizeLocation(State.getInstance().getGpsFilter(), location);

//            State.getInstance().fire(SHOW_INFO, location.getAccuracy() + ", " + location.getSpeed()+", " + origin.getSpeed());
            Location last = State.getInstance().getMe().getLocation();

            if(last != null) {
                if(location.getAccuracy() >= last.getAccuracy()) {
                    if(SphericalUtil.computeDistanceBetween(Utils.latLng(last),Utils.latLng(location)) < location.getAccuracy()) return;
                }
            }

            if(State.getInstance().tracking_active()) {
                try {
                    JSONObject message = Utils.locationToJson(location);
                    State.getInstance().getTracking().sendMessage(REQUEST_TRACKING, message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            State.getInstance().getMe().addLocation(location);
        }
    };

    public GpsHolder(Context context) {
        provider = new LocationGooglePlayServicesProvider();
        provider.setCheckLocationSettings(true);

        LocationParams.Builder params = new LocationParams.Builder().setAccuracy(LocationAccuracy.HIGH).setDistance(3).setInterval(1000);
        smartLocation = new SmartLocation.Builder(context).build().location().config(params.build());
    }

    @Override
    public String getType(){
        return TYPE;
    }

    @Override
    public boolean dependsOnUser() {
        return false;
    }

    @Override
    public boolean dependsOnEvent() {
        return true;
    }

    @Override
    public boolean onEvent(String event, Object object) throws URISyntaxException {
        switch (event) {
            case REQUEST_LOCATION_SINGLE:
                smartLocation.stop();
                smartLocation.continuous().start(locationUpdateListener);
                break;
            case ACTIVITY_RESUME:
                locationUpdateListener.onLocationUpdated(smartLocation.getLastLocation());
                smartLocation.continuous().start(locationUpdateListener);
                break;
            case ACTIVITY_PAUSE:
                if(State.getInstance().tracking_disabled()) {
                    try {
                        smartLocation.stop();
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case TRACKING_ACTIVE:
//                smartLocation.oneFix().start(locationUpdateListener);
                locationUpdateListener.onLocationUpdated(smartLocation.getLastLocation());
                smartLocation.continuous().start(locationUpdateListener);
                break;
        }
        return true;
    }

    @Override
    public AbstractProperty create(MyUser myUser) {
        return null;
    }
}
