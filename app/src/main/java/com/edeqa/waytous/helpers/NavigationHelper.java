package com.edeqa.waytous.helpers;

import android.location.Location;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

/**
 * Created 9/17/17.
 */

public class NavigationHelper implements Serializable {

    private static final long serialVersionUID = -6978147387428739440L;

    public static final String MODE_DRIVING = "driving";
    public static final String MODE_WALKING = "walking";
    public static final String MODE_BICYCLING = "bicycling";

    private static final String pattern = "https://maps.googleapis.com/maps/api/directions/json?origin=%g,%g&destination=%g,%g&alternatives=false&mode=%s";

    private transient Runnable1 onStart;
    private transient Runnable1 onStop;
    private transient Runnable1<String> onRequest;
    private transient Runnable1 onUpdate;

    private transient Location start;
    private transient Location current;
    private transient Location destination;

    private String mode = MODE_DRIVING;

    private long lastUpdate;

    private boolean avoidHighways;
    private boolean avoidTolls;
    private boolean avoidFerries;
    private boolean active;


    public NavigationHelper() {


    }

    public void update() {

        String req = String.format(pattern, start.getLatitude(), start.getLongitude(), destination.getLatitude(), destination.getLongitude(), mode);

        if(isAvoidHighways()) req += "&avoid=highways";
        if(isAvoidTolls()) req += "&avoid=tolls";
        if(isAvoidFerries()) req += "&avoid=ferries";

        if(onRequest != null) {
            onRequest.call(req);
        }

        final String res;
        try {
            res = Misc.getUrl(req);

            lastUpdate = new Date().getTime();
            JSONObject o = new JSONObject(res);

            System.out.println("NAVRESOLVED:"+o);
//            final String text = o.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline").getString("points");
//            points = PolyUtil.decode(text);
//            String distanceText = o.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").getString("text");
//            String durationText = o.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getString("text");
//            title = distanceText + "\n" + durationText;
//
//            int distance = o.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").getInt("value");

            if(onUpdate != null) onUpdate.call(null);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void start() {
        if(isActive()) return;

        setActive(true);
        if(onStart != null) onStart.call(null);
    }

    public void hide() {
        if(!isActive()) return;

        setActive(false);
        if(onStop != null) onStop.call(null);
    }

    public boolean isAvoidHighways() {
        return avoidHighways;
    }

    public NavigationHelper setAvoidHighways(boolean avoidHighways) {
        this.avoidHighways = avoidHighways;
        return this;
    }

    public boolean isAvoidTolls() {
        return avoidTolls;
    }

    public NavigationHelper setAvoidTolls(boolean avoidTolls) {
        this.avoidTolls = avoidTolls;
        return this;
    }

    public boolean isAvoidFerries() {
        return avoidFerries;
    }

    public NavigationHelper setAvoidFerries(boolean avoidFerries) {
        this.avoidFerries = avoidFerries;
        return this;
    }

    public String getMode() {
        return mode;
    }

    public NavigationHelper setMode(String mode) {
        this.mode = mode;
        return this;
    }

    public Runnable1 getOnUpdate() {
        return onUpdate;
    }

    public NavigationHelper setOnUpdate(Runnable1 onUpdate) {
        this.onUpdate = onUpdate;
        return this;
    }

    public Runnable1 getOnStart() {
        return onStart;
    }

    public NavigationHelper setOnStart(Runnable1 onStart) {
        this.onStart = onStart;
        return this;
    }

    public boolean isActive() {
        return active;
    }

    public NavigationHelper setActive(boolean active) {
        this.active = active;
        return this;
    }

    public Location getStart() {
        return start;
    }

    public NavigationHelper setStart(Location start) {
        this.start = start;
        return this;
    }

    public Location getDestination() {
        return destination;
    }

    public NavigationHelper setDestination(Location destination) {
        this.destination = destination;
        return this;
    }

    public Location getCurrent() {
        return current;
    }

    public NavigationHelper setCurrent(Location current) {
        this.current = current;
        return this;
    }

    public Runnable1 getOnRequest() {
        return onRequest;
    }

    public NavigationHelper setOnRequest(Runnable1<String> onRequest) {
        this.onRequest = onRequest;
        return this;
    }

    public Runnable1 getOnStop() {
        return onStop;
    }

    public NavigationHelper setOnStop(Runnable1 onStop) {
        this.onStop = onStop;
        return this;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public NavigationHelper setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
        return this;
    }
}
