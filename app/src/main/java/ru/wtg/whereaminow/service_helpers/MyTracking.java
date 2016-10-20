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
import android.support.v7.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;

import ru.wtg.whereaminow.MainActivity;
import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.helpers.State;
import ru.wtg.whereaminowserver.helpers.Constants;

import static ru.wtg.whereaminowserver.helpers.Constants.BROADCAST;
import static ru.wtg.whereaminowserver.helpers.Constants.BROADCAST_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.TRACKING_ACTIVE;
import static ru.wtg.whereaminowserver.helpers.Constants.TRACKING_DISABLED;
import static ru.wtg.whereaminowserver.helpers.Constants.WSS_SERVER_URL;

/**
 * Created by tujger on 10/8/16.
 */

public class MyTracking {

    private State state;
    private MyWebSocketClient webClient;
    private LocationManager locationManager = null;
    private Service context;

    public MyTracking(Service service){
        this.context = service;

        state = State.getInstance();
        webClient = MyWebSocketClient.getInstance(WSS_SERVER_URL);
        webClient.setContext(service);
        webClient.setTracking(this);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

    }

    public LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            System.out.println("Service:onLocationChanged");

            webClient.put("lat",location.getLatitude());
            webClient.put("lng",location.getLongitude());
            webClient.put("acc",location.getAccuracy());
            webClient.put("alt",location.getAltitude());
            webClient.put("brn",location.getBearing());
            webClient.put("spd",location.getSpeed());
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

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);
    }

    public void start() {
        state.setStatus(TRACKING_ACTIVE);

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
        locationManager.removeUpdates(locationListener);
    }

    public void cancel(){
        stop();
        webClient.removeToken();
    }

    public void fromServer(JSONObject o){
        System.out.println(o);
        context.sendBroadcast(new Intent(BROADCAST)
                .putExtra(BROADCAST_MESSAGE,o.toString()));

    }

    public void join(String token) {

        state.setStatus(TRACKING_ACTIVE);

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_navigation_twinks_white_24dp)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText("Doing some work...")
                .setContentIntent(pendingIntent).build();

        context.startForeground(1976, notification);

        System.out.println("SETTOKEN:"+token);
        webClient.setToken(token);
        webClient.start();
        enableLocationManager();
    }
}
