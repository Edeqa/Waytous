package com.edeqa.waytous.holders;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import com.edeqa.waytous.abstracts.AbstractProperty;
import com.edeqa.waytous.abstracts.AbstractPropertyHolder;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.helpers.Utils;
import com.google.maps.android.SphericalUtil;

import org.json.JSONException;
import org.json.JSONObject;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationAccuracy;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesProvider;
import com.edeqa.waytous.State;

import static com.edeqa.waytous.State.EVENTS.ACTIVITY_PAUSE;
import static com.edeqa.waytous.State.EVENTS.ACTIVITY_RESUME;
import static com.edeqa.waytous.State.EVENTS.TRACKING_ACTIVE;
import static com.edeqa.waytousserver.helpers.Constants.REQUEST_TRACKING;

/**
 * Created 01/13/17.
 */
public class GpsHolder extends AbstractPropertyHolder {
    private static final String TYPE = "Gps";

    public static final String REQUEST_LOCATION_SINGLE = "request_location_single";

    private final LocationGooglePlayServicesProvider provider;
    private final SmartLocation.LocationControl smartLocation;

    private long lastGps = 0;

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
    public boolean onEvent(String event, Object object) {
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
                sendLocation(smartLocation.getLastLocation());
                smartLocation.continuous().start(locationUpdateListener);
                break;
        }
        return true;
    }

    @Override
    public AbstractProperty create(MyUser myUser) {
        return null;
    }

    private OnLocationUpdatedListener locationUpdateListener = new OnLocationUpdatedListener() {
        @Override
        public void onLocationUpdated(Location location) {
            if(location == null) return;
            location = Utils.normalizeLocation(State.getInstance().getGpsFilter(), location);
//            State.getInstance().fire(SHOW_INFO,location.getProvider()+":"+ location.getAccuracy());
            Location last = State.getInstance().getMe().getLocation();

            if(LocationManager.GPS_PROVIDER.equals(location.getProvider())) {
                lastGps = location.getTime(); // register last gps-location time
            } else {
                if(location.getTime()-lastGps < 300*1000) return; // if gps-location not provided longer than 5 minutes then accept network-location
            }

            if(last != null) {
                if(location.getAccuracy() > 50) return;
                if(location.getAccuracy() >= last.getAccuracy()) {
                    if(SphericalUtil.computeDistanceBetween(Utils.latLng(last),Utils.latLng(location)) < location.getAccuracy()) return;
                }
            }
            sendLocation(location);
        }
    };

    private void sendLocation(Location location) {
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
}
