package ru.wtg.whereaminow.service_helpers;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ru.wtg.whereaminow.MainActivity;
import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.helpers.MyMarker;
import ru.wtg.whereaminow.helpers.MyUsers;
import ru.wtg.whereaminow.helpers.State;
import ru.wtg.whereaminow.helpers.Utils;

import static ru.wtg.whereaminowserver.helpers.Constants.BROADCAST;
import static ru.wtg.whereaminowserver.helpers.Constants.BROADCAST_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.LOCATION_UPDATES_DELAY;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_INITIAL;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_NUMBER;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_ACCEPTED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_DISCONNECTED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_ERROR;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_UPDATED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_TOKEN;
import static ru.wtg.whereaminowserver.helpers.Constants.TRACKING_ACTIVE;
import static ru.wtg.whereaminowserver.helpers.Constants.TRACKING_CONNECTING;
import static ru.wtg.whereaminowserver.helpers.Constants.TRACKING_DISABLED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_ACCURACY;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_ALTITUDE;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_BEARING;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_COLOR;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_DISMISSED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_JOINED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_LATITUDE;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_LONGITUDE;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_NUMBER;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_PROVIDER;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_SPEED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_TIMESTAMP;
import static ru.wtg.whereaminowserver.helpers.Constants.WSS_SERVER_URL;

/**
 * Created by tujger on 10/8/16.
 */

public class MyTracking {

    private State state;
    private MyWebSocketClient webClient;
    private LocationManager locationManager = null;
    private Service context;

    public MyTracking(Service service) {
        this.context = service;

        state = State.getInstance();
        state.setStatus(TRACKING_DISABLED);
        webClient = MyWebSocketClient.getInstance(WSS_SERVER_URL);
        webClient.setContext(service);
        webClient.setTracking(this);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

    }

    public LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            System.out.println("Service:onLocationChanged");

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
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_UPDATES_DELAY, 1, locationListener);
    }

    public void start() {
        state.setStatus(TRACKING_CONNECTING);

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_navigation_twinks_white_24dp)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText("Doing some work...")
                .setContentIntent(pendingIntent).build();

        context.startForeground(1976, notification);

        webClient.setToken(null);
        webClient.start();
        enableLocationManager();
    }

    public void stop() {
        context.stopForeground(true);
        state.setStatus(TRACKING_DISABLED);
        webClient.stop();

        state.getUsers().removeAllUsersExceptMe();
        state.setNumber(0);

        locationManager.removeUpdates(locationListener);
    }

    public void cancel() {
        stop();
        webClient.removeToken();
    }

    public void fromServer(JSONObject o) {
        System.out.println("FROMSERVER:" + o);

        try {
            switch (o.getString(RESPONSE_STATUS)) {
                case RESPONSE_STATUS_DISCONNECTED:
                    state.setStatus(TRACKING_DISABLED);
                    break;
                case RESPONSE_STATUS_ACCEPTED:
                    state.setStatus(TRACKING_ACTIVE);
                    if (o.has(RESPONSE_TOKEN)) {
                        state.setToken(o.getString(RESPONSE_TOKEN));
                    }
                    if (o.has(RESPONSE_NUMBER)) {
                        int newNumber = o.getInt(RESPONSE_NUMBER);
                        state.getUsers().setMyNumber(newNumber);
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
                    state.setStatus(TRACKING_DISABLED);
                    break;
                case RESPONSE_STATUS_UPDATED:
//                        System.out.println("RESPONSE_STATUS_UPDATED:"+o);

                    if (o.has(USER_DISMISSED)) {
                        int number = o.getInt(USER_DISMISSED);
                        state.getUsers().forUser(number,new MyUsers.Callback() {
                            @Override
                            public void call(Integer number, final MyMarker marker) {
                                System.out.println("DISMISS:"+number+":"+marker);
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    public void run() {
                                        marker.remove();
                                    }
                                });

                            }
                        });
                    } else if (o.has(USER_JOINED)) {
                        int number = o.getInt(USER_JOINED);
                        state.getUsers().addUser(o);
                    }
                    if (o.has(USER_PROVIDER)) {
                        final Location location = Utils.jsonToLocation(o);
                        int number = o.getInt(USER_NUMBER);

                        state.getUsers().forUser(number,new MyUsers.Callback() {
                            @Override
                            public void call(Integer number, MyMarker marker) {
                                marker.addLocation(location);
                            }
                        });
                    }
                    break;
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        context.sendBroadcast(new Intent(BROADCAST)
                .putExtra(BROADCAST_MESSAGE, o.toString()));

    }

    public void join(String token) {

        state.setStatus(TRACKING_CONNECTING);
        state.setToken(token);

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_navigation_twinks_white_24dp)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText("Doing some work...")
                .setContentIntent(pendingIntent).build();

        context.startForeground(1976, notification);

//        System.out.println("SETTOKEN:" + token);
        webClient.setToken(token);
        webClient.start();
        enableLocationManager();
    }
}
