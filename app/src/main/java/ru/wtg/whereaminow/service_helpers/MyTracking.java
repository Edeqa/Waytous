package ru.wtg.whereaminow.service_helpers;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

import ru.wtg.whereaminow.MainActivity;
import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.WhereAmINowService;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;
import ru.wtg.whereaminow.helpers.State;
import ru.wtg.whereaminow.helpers.Utils;

import static ru.wtg.whereaminowserver.helpers.Constants.BROADCAST;
import static ru.wtg.whereaminowserver.helpers.Constants.BROADCAST_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.LOCATION_UPDATES_DELAY;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_INITIAL;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_NUMBER;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_ACCEPTED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_DISCONNECTED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_ERROR;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_STOPPED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_UPDATED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_TOKEN;
import static ru.wtg.whereaminowserver.helpers.Constants.TRACKING_ACTIVE;
import static ru.wtg.whereaminowserver.helpers.Constants.TRACKING_CONNECTING;
import static ru.wtg.whereaminowserver.helpers.Constants.TRACKING_DISABLED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_ACCURACY;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_ALTITUDE;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_BEARING;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_DISMISSED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_JOINED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_LATITUDE;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_LONGITUDE;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_NAME;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_NUMBER;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_PROVIDER;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_SPEED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_TIMESTAMP;
import static ru.wtg.whereaminowserver.helpers.Constants.WSS_SERVER_HOST;

/**
 * Created by tujger on 10/8/16.
 */

public class MyTracking {

    private State state;
    private MyWebSocketClient webClient;
    private LocationManager locationManager = null;
    private URI serverUri;
    private int status = TRACKING_DISABLED;

    public MyTracking() throws URISyntaxException {
        this(WSS_SERVER_HOST);
    }

    public MyTracking(String host) throws URISyntaxException {
        this(new URI("wss://"+host+":8081"));
    }

    public MyTracking(URI serverUri) {
        this.serverUri = serverUri;
        System.out.println("CONNECTTO:"+serverUri.toString());
        state = State.getInstance();
        try {
            webClient = new MyWebSocketClient(this.serverUri);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        webClient.setTracking(this);
        locationManager = (LocationManager) state.getApplication().getSystemService(Context.LOCATION_SERVICE);
    }

    public LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            System.out.println("Service:onLocationChanged");

            if(status == TRACKING_ACTIVE) {
                state.getMe().addLocation(location);
                try {
                    JSONObject o = Utils.locationToJson(location);
                    o.put(RESPONSE_STATUS,RESPONSE_STATUS_UPDATED);
                    o.put(RESPONSE_NUMBER,state.getUsers().getMyNumber());
                    fromServer(o);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                webClient.put(USER_LATITUDE, location.getLatitude());
                webClient.put(USER_LONGITUDE, location.getLongitude());
                webClient.put(USER_ACCURACY, location.getAccuracy());
                webClient.put(USER_ALTITUDE, location.getAltitude());
                webClient.put(USER_BEARING, location.getBearing());
                webClient.put(USER_SPEED, location.getSpeed());
                webClient.put(USER_PROVIDER, location.getProvider());
                webClient.put(USER_TIMESTAMP, location.getTime());

                webClient.sendUpdate();
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            System.out.println("Service:onProviderDisabled:" + provider);
        }

        public void onProviderEnabled(String provider) {
            System.out.println("Service:onProviderEnabled:" + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            System.out.println("Service:onStatusChanged:" + provider);
        }
    };

    private void enableLocationManager() {
//        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
//            System.err.println("Service:NOT locationManager.isProviderEnabled");
//            return;
//        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_UPDATES_DELAY, 1, locationListener);
//        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_UPDATES_DELAY, 1, locationListener);
    }

    public void start() {
        webClient.setToken(null);
        doTrack();
    }

    public void join(String token) {
        webClient.setToken(token);
        doTrack();
    }

    private void doTrack(){
        setStatus(TRACKING_CONNECTING);

        Intent notificationIntent = new Intent(state.getApplication(), MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(state.getApplication(), 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(state.getApplication())
                .setSmallIcon(R.drawable.ic_navigation_twinks_white_24dp)
                .setContentTitle(state.getApplication().getString(R.string.app_name))
                .setContentText("Doing some work...")
                .setAutoCancel(true)
                .addAction(R.drawable.ic_navigation_twinks_black_24dp, "View tracking", pendingIntent)
                .addAction(R.drawable.ic_clear_black_24dp, "Stop tracking",
                        PendingIntent.getService(state.getApplication(), (int) System.currentTimeMillis(), new Intent(state.getApplication(), WhereAmINowService.class).putExtra("mode", "stop"),0))
                .setContentIntent(pendingIntent)
                .build();

        state.setNotification(notification);
        state.getService().startForeground(1976, notification);

        webClient.start();
        enableLocationManager();
    }

    public void stop() {
        try {
            JSONObject o = new JSONObject();
            o.put(RESPONSE_STATUS,RESPONSE_STATUS_STOPPED);
            fromServer(o);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void fromServer(final JSONObject o) {
        System.out.println("FROMSERVER:" + o);
        if(status == TRACKING_DISABLED) return;
        try {
            switch (o.getString(RESPONSE_STATUS)) {
                case RESPONSE_STATUS_DISCONNECTED:
                    if(state.disconnected()) return;
//                    setStatus(TRACKING_DISABLED);
                    break;
                case RESPONSE_STATUS_ACCEPTED:
                    setStatus(TRACKING_ACTIVE);
                    if (o.has(RESPONSE_TOKEN)) {
                        state.setToken(o.getString(RESPONSE_TOKEN));
                    }
                    if (o.has(RESPONSE_NUMBER)) {
                        state.getMe().removeViews();
                        state.getUsers().setMyNumber(o.getInt(RESPONSE_NUMBER));
                    }
                    if (o.has(RESPONSE_INITIAL)) {
                        JSONArray initialUsers = o.getJSONArray(RESPONSE_INITIAL);
                        for (int i = 0; i < initialUsers.length(); i++) {
                            JSONObject u = initialUsers.getJSONObject(i);
                            state.getUsers().addUser(u);
                        }
                    }
                    break;
                case RESPONSE_STATUS_ERROR:
                    stop();
                    setStatus(TRACKING_DISABLED);
                    break;
                case RESPONSE_STATUS_UPDATED:
                    if (o.has(USER_DISMISSED)) {
                        int number = o.getInt(USER_DISMISSED);
                        state.getUsers().forUser(number,new MyUsers.Callback() {
                            @Override
                            public void call(Integer number, final MyUser myUser) {
                                myUser.setActive(false);
                            }
                        });
                    } else if (o.has(USER_JOINED)) {
                        int number = o.getInt(USER_JOINED);
                        state.getUsers().addUser(o);
                        state.getUsers().forUser(number,new MyUsers.Callback() {
                            @Override
                            public void call(Integer number, MyUser myUser) {
                                myUser.setActive(true);
                            }
                        });
                    }
                    if (o.has(USER_PROVIDER)) {
                        final Location location = Utils.jsonToLocation(o);
                        int number = o.getInt(USER_NUMBER);
                        state.getUsers().forUser(number,new MyUsers.Callback() {
                            @Override
                            public void call(Integer number, MyUser myUser) {
//                                myUser.addLocation(location);
                            }
                        });
                    }
                    if (o.has(USER_NAME)) {
                        int number = o.getInt(USER_NUMBER);
                        String name = o.getString(USER_NAME);
                        state.getUsers().setNameFor(number,name);
                    }
                    break;
                case RESPONSE_STATUS_STOPPED:
                    locationManager.removeUpdates(locationListener);
                    state.getUsers().removeAllUsersExceptMe();
                    setStatus(TRACKING_DISABLED);
                    state.getService().stopForeground(true);
                    state.setNotification(null);

                    webClient.removeToken();
                    webClient.stop();
                    System.out.println("STOPACCEPTED");
//                    return;
                    break;
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        state.getApplication().sendBroadcast(new Intent(BROADCAST).putExtra(BROADCAST_MESSAGE, o.toString()));

    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getHost(){
        return serverUri.getHost();
    }

    public void sendMessage(String key,String value){
        webClient.put(key, value);
        webClient.sendUpdate();
    }

}
