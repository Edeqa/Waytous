package com.edeqa.waytous.helpers;

import android.location.Location;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.helpers.interfaces.Runnable2;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created 9/17/17.
 */

public class NavigationHelper implements Serializable {

    private static final long serialVersionUID = -6978147387428739440L;

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static final String MODE_DRIVING = "driving";
    public static final String MODE_WALKING = "walking";
    public static final String MODE_BICYCLING = "bicycling";

    public static final int ERROR_JSON = 1;
    public static final int ERROR_OVER_QUERY_LIMIT = 2;

    public static final int TYPE_STARTED = 0;
    public static final int TYPE_DISTANCE = 1;
    public static final int TYPE_DURATION = 2;

    private static final String pattern = "https://maps.googleapis.com/maps/api/directions/json?origin=%g,%g&destination=%g,%g&mode=%s&alternatives=true";

    private transient Runnable onStart;
    private transient Runnable onStop;
    private transient Runnable1<String> onRequest;
    private transient Runnable2<Integer, String> onUpdate;
    private transient Runnable1<Throwable> onErrorThrowable;
    private transient Runnable2<Integer, String> onError;

    private transient Location startLocation;
    private transient Location currentLocation;
    private transient Location endLocation;

    private transient ArrayList<Route> routes;

    public enum Mode {
        DRIVING,WALKING,BICYCLING
    }

    private Mode mode = Mode.DRIVING;
    private String apiKey;

    private long lastUpdate;

    private boolean avoidHighways;
    private boolean avoidTolls;
    private boolean avoidFerries;
    private boolean active;


    public NavigationHelper() {
    }

    public void update() {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                String req = String.format(pattern, startLocation.getLatitude(), startLocation.getLongitude(), endLocation.getLatitude(), endLocation.getLongitude(), mode.toString().toLowerCase());

                if(isAvoidHighways()) req += "&avoid=highways";
                if(isAvoidTolls()) req += "&avoid=tolls";
                if(isAvoidFerries()) req += "&avoid=ferries";

                if(onRequest != null) {
                    onRequest.call(req);
                }
                String res = null;
                try {
                    res = Misc.getUrl(req);

                    lastUpdate = new Date().getTime();
                    JSONObject o = new JSONObject(res);

                    switch(o.getString("status")) {
                        case "OK":
                            routes = new ArrayList<Route>();
                            for(int i = 0; i < o.getJSONArray("routes").length(); i++) {
                                try {
                                    Route route = new Route((JSONObject) o.getJSONArray("routes").get(i));
                                    routes.add(route);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if(onUpdate != null && routes.size() > 0) {
                                onUpdate.call(TYPE_DISTANCE, routes.get(0).fetchDistance());
                                onUpdate.call(TYPE_DURATION, routes.get(0).fetchDuration());
                            }
                            break;
                        case "OVER_QUERY_LIMIT":
                            String message = "Daily request quota for Direction API is exceeded";
                            if(onErrorThrowable != null) onErrorThrowable.call(new Exception(message));
                            if(onError != null) onError.call(ERROR_OVER_QUERY_LIMIT, message);
                            break;
                    }


//                    System.out.println("NAVRESOLVED:"+o);
//            final String text = o.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline").getString("points");
//            points = PolyUtil.decode(text);
//            String distanceText = o.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").getString("text");
//            String durationText = o.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getString("text");
//            title = distanceText + "\n" + durationText;
//
//            int distance = o.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").getInt("value");

                    if(onUpdate != null) onUpdate.call(TYPE_STARTED, null);

                } catch (Exception e) {
                    e.printStackTrace();
                    String message = "Incorrect response: " + res;
                    if(onErrorThrowable != null) onErrorThrowable.call(new JSONException(message));
                    if(onError != null) onError.call(ERROR_JSON, message);
                }
            }
        });

    }

    public void start() {
        if(isActive()) return;

        setActive(true);

        if(onStart != null) onStart.run();
        update();
    }

    public void stop() {
        if(!isActive()) return;

        setActive(false);
        if(onStop != null) onStop.run();
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
        return mode.toString();
    }

    public NavigationHelper setMode(Mode mode) {
        this.mode = mode;
        return this;
    }

    public Runnable2 getOnUpdate() {
        return onUpdate;
    }

    public NavigationHelper setOnUpdate(Runnable2 onUpdate) {
        this.onUpdate = onUpdate;
        return this;
    }

    public Runnable getOnStart() {
        return onStart;
    }

    public NavigationHelper setOnStart(Runnable onStart) {
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

    public Location getStartLocation() {
        return startLocation;
    }

    public NavigationHelper setStartLocation(Location startLocation) {
        this.startLocation = startLocation;
        return this;
    }

    public Location getEndLocation() {
        return endLocation;
    }

    public NavigationHelper setEndLocation(Location endLocation) {
        this.endLocation = endLocation;
        return this;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public NavigationHelper setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
        return this;
    }

    public Runnable1 getOnRequest() {
        return onRequest;
    }

    public NavigationHelper setOnRequest(Runnable1<String> onRequest) {
        this.onRequest = onRequest;
        return this;
    }

    public Runnable getOnStop() {
        return onStop;
    }

    public NavigationHelper setOnStop(Runnable onStop) {
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

    public String getApiKey() {
        return apiKey;
    }

    public NavigationHelper setApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public Runnable2<Integer, String> getOnError() {
        return onError;
    }

    public NavigationHelper setOnError(Runnable2<Integer, String> onError) {
        this.onError = onError;
        return this;
    }

    public Runnable1<Throwable> getOnErrorThrowable() {
        return onErrorThrowable;
    }

    public NavigationHelper setOnError(Runnable1<Throwable> onError) {
        this.onErrorThrowable = onError;
        return this;
    }

    public class Route {

        private List<Leg> legs;
        private final List<LatLng> points;
        private String summary;
        private String copyrights;


        public Route(JSONObject route) throws JSONException {

            if(route.has("copyrights")) copyrights = route.getString("copyrights");
            if(route.has("summary")) summary = route.getString("summary");

            String overview = route.getJSONObject("overview_polyline").getString("points");
            points = PolyUtil.decode(overview);

            legs = new ArrayList<>();
            for(int i = 0; i < route.getJSONArray("legs").length(); i++) {
                try {
                    Leg leg = new Leg((JSONObject) route.getJSONArray("legs").get(i));
                    legs.add(leg);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }

        }

        @Override
        public String toString() {
            return "Route{" +
                    "summary='" + summary + '\'' +
                    ", copyrights='" + copyrights + '\'' +
                    ", points=" + points.size() +
                    ", legs.count=" + legs.size() +
                    ", legs=" + legs +
                    '}';
        }

        public String fetchDistance() {
            return Misc.formatLengthToLocale(legs.get(0).getDistance());
        }

        public String fetchDuration() {
            return Misc.toDateString(legs.get(0).getDuration() * 1000);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public class Leg {

        private List<Step> steps;

        private LatLng startLocation;
        private LatLng endLocation;
        private final String startAddress;
        private final String endAddress;
        private final int distance;
        private final int duration;

        public Leg(JSONObject leg) throws JSONException {

            startLocation = new LatLng(leg.getJSONObject("start_location").getDouble("lat"), leg.getJSONObject("start_location").getDouble("lng"));
            endLocation = new LatLng(leg.getJSONObject("end_location").getDouble("lat"), leg.getJSONObject("end_location").getDouble("lng"));
            startAddress = leg.getString("start_address");
            endAddress = leg.getString("end_address");
            distance = leg.getJSONObject("distance").getInt("value");
            duration = leg.getJSONObject("duration").getInt("value");

            steps = new ArrayList<>();
            for(int i = 0; i < leg.getJSONArray("steps").length(); i++) {
                try {
                    Step step = new Step((JSONObject) leg.getJSONArray("steps").get(i));
                    steps.add(step);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public int getDistance() {
            return distance;
        }

        public int getDuration() {
            return duration;
        }

        @Override
        public String toString() {
            return "Leg{" +
                    "startAddress='" + startAddress + '\'' +
                    ", endAddress='" + endAddress + '\'' +
                    ", distance=" + distance +
                    ", duration=" + duration +
                    ", steps.count=" + steps.size() +
                    ", steps=" + steps +
                    '}';
        }
    }

    @SuppressWarnings("WeakerAccess")
    public class Step {

        private List<LatLng> points;
        private LatLng startLocation;
        private LatLng endLocation;
        private String htmlInstruction;
        private String maneuver;
        private String mode;
        private int distance;
        private int duration;

        public Step(JSONObject step) throws JSONException {

            if(step.has("start_location")) startLocation = new LatLng(step.getJSONObject("start_location").getDouble("lat"), step.getJSONObject("start_location").getDouble("lng"));
            if(step.has("end_location")) endLocation = new LatLng(step.getJSONObject("end_location").getDouble("lat"), step.getJSONObject("end_location").getDouble("lng"));
            if(step.has("distance")) distance = step.getJSONObject("distance").getInt("value");
            if(step.has("duration")) duration = step.getJSONObject("duration").getInt("value");

            if(step.has("html_instructions")) htmlInstruction = step.getString("html_instructions");
            if(step.has("maneuver")) maneuver = step.getString("maneuver");
            if(step.has("travel_mode")) mode = step.getString("travel_mode");

            if(step.has("polyline")) {
                String polyline = step.getJSONObject("polyline").getString("points");
                points = PolyUtil.decode(polyline);
            }

        }

        public String getManeuver() {
            return maneuver;
        }

        public String getMode() {
            return mode;
        }

        public int getDistance() {
            return distance;
        }

        public int getDuration() {
            return duration;
        }

        @Override
        public String toString() {
            return "Step{" +
                    "points=" + (points != null ? points.size() : 0) +
                    ", htmlInstruction='" + htmlInstruction + '\'' +
                    ", maneuver='" + maneuver + '\'' +
                    ", mode='" + mode + '\'' +
                    ", distance=" + distance +
                    ", duration=" + duration +
                    '}';
        }
    }

}
