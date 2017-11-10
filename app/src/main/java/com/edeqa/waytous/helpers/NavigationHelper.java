package com.edeqa.waytous.helpers;

import android.location.Location;

import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.helpers.interfaces.Runnable2;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;

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

@SuppressWarnings({"WeakerAccess", "unused", "UnusedReturnValue"})
public class NavigationHelper implements Serializable {

    private static final long serialVersionUID = -6978147387428739440L;

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static final int ERROR_JSON = 1;
    public static final int ERROR_OVER_QUERY_LIMIT = 2;

    public static int distanceForRebuildForce = 20;
    public static int maxFailsCount = 10;
    public static int afterFailDelay = 2;

    @SuppressWarnings("HardCodedStringLiteral")
    private static final String pattern = "https://maps.googleapis.com/maps/api/directions/json?origin=%g,%g&destination=%g,%g&mode=%s";

    private transient EventBus.Runner runner;

    private transient Runnable onStart;
    private transient Runnable onStop;
    private transient Runnable1<String> onRequest;
    private transient Runnable2<Type, Object> onUpdate;
    private transient Runnable1<Throwable> onErrorThrowable = new Runnable1<Throwable>() {
        @Override
        public void call(Throwable arg) {
            arg.printStackTrace();
        }
    };
    private transient Runnable2<Integer, String> onError;

    private volatile transient Location startLocation;
    private transient Location previousStartLocation;
    private volatile transient Location endLocation;
    private transient Location previousEndLocation;
    private volatile transient Location currentLocation;
    private transient Location previousCurrentLocation;

    private transient ArrayList<Route> routes;

    private transient Mode mode = Mode.DRIVING;
    private String apiKey;

    private long lastUpdate;
    private long lastTry;
    private int failsCount;

    private int activeRoute;

    private boolean avoidHighways;
    private boolean avoidTolls;
    private boolean avoidFerries;

    private volatile boolean active;

    public static final EventBus.Runner DEFAULT_RUNNER = new EventBus.Runner() {
        @Override
        public void post(final Runnable runnable) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    runnable.run();
                }
            });
        }
    };

    public NavigationHelper() {
        setActiveRoute(0);
    }


    public static int getDistanceForRebuildForce() {
        return distanceForRebuildForce;
    }

    public static void setDistanceForRebuildForce(int distanceForRebuildForce) {
        NavigationHelper.distanceForRebuildForce = distanceForRebuildForce;
    }

    public void start() {
        if(isActive()) return;

        if(runner == null) {
            runner = DEFAULT_RUNNER;
        }

        setActive(true);

        if(getCurrentLocation() == null) {
            setCurrentLocation(getStartLocation());
        }

        previousStartLocation = getStartLocation();
        previousCurrentLocation = getCurrentLocation();
        previousEndLocation = getEndLocation();

        if(onStart != null) runner.post(onStart);
        updatePath(true);
    }

    public void updatePath() {
        updatePath(false);
    }

    public void updatePath(boolean force) {

        long currentTimestamp = new Date().getTime();
        if(!force && currentTimestamp - getLastUpdate() < 5000){
//            throwUpdate();
            return;
        }

        executor.execute(new Runnable() {
            @SuppressWarnings("HardCodedStringLiteral")
            @Override
            public void run() {
                if(!isActive()) return;

                String res = null;
                try {
                    String req = String.format(pattern, getStartLocation().getLatitude(), getStartLocation().getLongitude(), getEndLocation().getLatitude(), getEndLocation().getLongitude(), mode.toString().toLowerCase());

                    if(isAvoidHighways()) req += "&avoid=highways";
                    if(isAvoidTolls()) req += "&avoid=tolls";
                    if(isAvoidFerries()) req += "&avoid=ferries";

                    if(getStartLocation().getLatitude() != getCurrentLocation().getLatitude() &&
                            getStartLocation().getLongitude() != getCurrentLocation().getLongitude()) {
                        req += "&waypoints=" + getCurrentLocation().getLatitude() + "," +getCurrentLocation().getLongitude();
                    }

                    if(onRequest != null) {
                        onRequest.call(req);
                    }

                    lastTry = new Date().getTime();

                    res = Misc.getUrl(req);
                    if(!isActive()) return;
                    JSONObject o = new JSONObject(res);

                    switch(o.getString("status")) {
                        case "OK":
                            setLastUpdate(new Date().getTime());
                            routes = new ArrayList<>();
                            for(int i = 0; i < o.getJSONArray("routes").length(); i++) {
                                try {
                                    Route route = new Route((JSONObject) o.getJSONArray("routes").get(i));
                                    routes.add(route);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if(onUpdate != null && routes.size() > 0) {
                                runner.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Route route = routes.get(getActiveRoute());
                                        onUpdate.call(Type.DISTANCE, route.fetchDistance());
                                        onUpdate.call(Type.DURATION, route.fetchDuration());

                                        if(routes.get(getActiveRoute()).getLegs().size() > 1) {
                                            onUpdate.call(Type.POINTS_BEFORE, PolyUtil.simplify(route.getLegs().get(0).getPoints(), 50));
                                            onUpdate.call(Type.POINTS_AFTER, PolyUtil.simplify(route.getLegs().get(1).getPoints(), 50));
                                        } else {
                                            onUpdate.call(Type.POINTS_BEFORE, new ArrayList<LatLng>());
                                            onUpdate.call(Type.POINTS_AFTER, route.getPoints());
                                        }
                                        onUpdate.call(Type.POINTS, route.getPoints());
                                    }
                                });
                            }
                            break;
                        case "OVER_QUERY_LIMIT":
                            String message = "Daily request quota for Direction API is exceeded";
                            failsCount ++;
                            if(failsCount > maxFailsCount)


                            throwError(ERROR_OVER_QUERY_LIMIT, message);
                            break;
                    }

                    throwUpdate();
                } catch (Exception e) {
                    e.printStackTrace();
                    final String message = "Incorrect response: " + res;
                    throwError(ERROR_JSON, message);
                }
            }
        });
    }

    private void throwError(final int errorCode, final String message) {
        if(onErrorThrowable != null) {
            runner.post(new Runnable() {
                @Override
                public void run() {
                    onErrorThrowable.call(new JSONException(message));
                }
            });
        }
        if(onError != null) {
            runner.post(new Runnable() {
                @Override
                public void run() {
                    onError.call(errorCode, message);
                }
            });
        }
    }

    private void throwUpdate() {
        if(onUpdate != null) {
            runner.post(new Runnable() {
                @Override
                public void run() {
                    onUpdate.call(Type.UPDATED, null);
                }
            });
        }
    }

    public void updateStartLocation(Location location) {
        if(!isActive()) return;

        setStartLocation(location);
        double distance = 0;
        if(previousStartLocation != null) {
            distance = SphericalUtil.computeDistanceBetween(Utils.latLng(location), Utils.latLng(previousStartLocation));
        }

        if(distance > distanceForRebuildForce) {
            previousStartLocation = location;
            updatePath(true);
        } else {
            updatePath();
        }
    }

    public void updateCurrentLocation(Location location) {
        if(!isActive()) return;

        setCurrentLocation(location);
        double distance = 0;

        if(previousCurrentLocation != null) {
            distance = SphericalUtil.computeDistanceBetween(Utils.latLng(location), Utils.latLng(previousCurrentLocation));
        }

        if(distance > distanceForRebuildForce) {
            previousCurrentLocation = location;
            updatePath(true);
        } else {
            updatePath();
        }
    }

    public void updateEndLocation(Location location) {
        if(!isActive()) return;

        setEndLocation(location);
        double distance = 0;
        if(previousEndLocation != null) {
            distance = SphericalUtil.computeDistanceBetween(Utils.latLng(location), Utils.latLng(previousEndLocation));
        }

        if(distance > distanceForRebuildForce) {
            previousEndLocation = location;
            updatePath(true);
        } else {
            updatePath();
        }
    }

    public void stop() {
        if(!isActive()) return;

        setActive(false);
        if(onStop != null) runner.post(onStop);
    }

    /*public void fetchInfo(Type type, Runnable2<Type, Object> callback) {
        switch(type) {
            case DISTANCE:
                break;
            case DURATION:
                break;
            case POINTS:
                break;
            case UPDATED:
                break;
        }
    }*/

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

    public Mode getMode() {
        return mode;
    }

    public NavigationHelper setMode(Mode mode) {
        this.mode = mode;
        return this;
    }

    public NavigationHelper setMode(String mode) {
        try {
            this.mode = Mode.valueOf(mode.toUpperCase());
        } catch (Exception e) {
            this.mode = Mode.DRIVING;
        }
        return this;
    }

    public Runnable2 getOnUpdate() {
        return onUpdate;
    }

    public NavigationHelper setOnUpdate(Runnable2<Type, Object> onUpdate) {
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

    private NavigationHelper setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
        return this;
    }

    public String getApiKey() {
        return apiKey;
    }

    @SuppressWarnings("SameParameterValue")
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

    public EventBus.Runner getRunner() {
        return runner;
    }

    public void setRunner(EventBus.Runner runner) {
        this.runner = runner;
    }

    public int getActiveRoute() {
        return activeRoute;
    }

    @SuppressWarnings("SameParameterValue")
    public NavigationHelper setActiveRoute(int activeRoute) {
        this.activeRoute = activeRoute;
        return this;
    }

    public int getRoutesCount() {
        return routes.size();
    }

    public enum Mode {
        DRIVING,WALKING,BICYCLING
    }

    public enum Type {
        UPDATED, DISTANCE, DURATION, POINTS_BEFORE, POINTS_AFTER, POINTS
    }

    public class Route {

        private List<Leg> legs;

        private final List<LatLng> points;
        private String summary;
        private String copyrights;


        @SuppressWarnings("HardCodedStringLiteral")
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

        public String fetchDistance() {
            return Misc.distanceToString(legs.get(0).getDistance());
        }

        public String fetchDuration() {
            return Misc.durationToString(legs.get(0).getDuration() * 1000);
        }

        public List<Leg> getLegs() {
            return legs;
        }

        public List<LatLng> getPoints() {
            return points;
        }

        @SuppressWarnings("HardCodedStringLiteral")
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
    }

    @SuppressWarnings("WeakerAccess")
    public class Leg {

        private List<Step> steps;

        private List<LatLng> points;

        private LatLng startLocation;
        private LatLng endLocation;
        private final String startAddress;
        private final String endAddress;
        private final int distance;
        private final int duration;

        @SuppressWarnings("HardCodedStringLiteral")
        public Leg(JSONObject leg) throws JSONException {

            startLocation = new LatLng(leg.getJSONObject("start_location").getDouble("lat"), leg.getJSONObject("start_location").getDouble("lng"));
            endLocation = new LatLng(leg.getJSONObject("end_location").getDouble("lat"), leg.getJSONObject("end_location").getDouble("lng"));
            startAddress = leg.getString("start_address");
            endAddress = leg.getString("end_address");
            distance = leg.getJSONObject("distance").getInt("value");
            duration = leg.getJSONObject("duration").getInt("value");

            steps = new ArrayList<>();
            points = new ArrayList<>();
            for(int i = 0; i < leg.getJSONArray("steps").length(); i++) {
                try {
                    Step step = new Step((JSONObject) leg.getJSONArray("steps").get(i));
                    steps.add(step);
                    points.addAll(step.getPoints());
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public List<LatLng> getPoints() {
            return points;
        }

        public int getDistance() {
            return distance;
        }

        public int getDuration() {
            return duration;
        }

        @SuppressWarnings("HardCodedStringLiteral")
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

        @SuppressWarnings("HardCodedStringLiteral")
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
                setPoints(PolyUtil.decode(polyline));
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

        public List<LatLng> getPoints() {
            return points;
        }

        public void setPoints(List<LatLng> points) {
            this.points = points;
        }

        @SuppressWarnings("HardCodedStringLiteral")
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
