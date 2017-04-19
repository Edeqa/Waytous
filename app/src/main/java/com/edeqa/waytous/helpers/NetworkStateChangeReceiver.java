package com.edeqa.waytous.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.edeqa.waytous.State;
import com.edeqa.waytous.interfaces.Tracking;

import static com.edeqa.waytous.State.EVENTS.TRACKING_ACTIVE;
import static com.edeqa.waytous.State.EVENTS.TRACKING_CONNECTING;
import static com.edeqa.waytous.State.EVENTS.TRACKING_ERROR;
import static com.edeqa.waytous.State.EVENTS.TRACKING_RECONNECTING;

/**
 * Created 12/30/16.
 */

public class NetworkStateChangeReceiver extends BroadcastReceiver {

    private Tracking tracking;
    private boolean connected = false;

    public NetworkStateChangeReceiver(){

    }

    public NetworkStateChangeReceiver(Tracking tracking) {
        this.tracking = tracking;
        ConnectivityManager connectivity = (ConnectivityManager) State.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();

        if(networkInfo != null) {
            connected = networkInfo.getState() == NetworkInfo.State.CONNECTED;
        }
        State.getInstance().registerReceiver(this, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(tracking == null) return;
        if (intent.getAction() != null && intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
//                ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            Bundle extras = intent.getExtras();

            NetworkInfo info = extras.getParcelable("networkInfo");

            if (info.getState() == NetworkInfo.State.CONNECTED) {
                if(!connected) {
                    /*switch (tracking.getStatus()) {
                        case TRACKING_RECONNECTING:
                                System.out.println("RECONNECT");
                            tracking.reconnect();
//                            String trackingUri = State.getInstance().getStringPreference(TRACKING_URI, null);
//                            State.getInstance().setToken(null);
//                                state.setPreference(TRACKING_URI, null);
//                            if (trackingUri != null) {
//                                State.getInstance().fire(TRACKING_JOIN, Uri.parse(trackingUri));
//                            }
//                                System.out.println("DO RECONNECT");
                            break;
                        case TRACKING_ERROR:
                                System.out.println("TRACKING_ERROR");
                            tracking.setStatus(TRACKING_ERROR);
                            State.getInstance().fire(TRACKING_ERROR);
                            break;
                    }*/
                }
                connected = true;
            } else {
                if(connected) {
                    switch (tracking.getStatus()) {
                        case TRACKING_ACTIVE:
                                System.out.println("DISCONNECTED TO RECONNECT");
                            tracking.setStatus(TRACKING_RECONNECTING);
                            State.getInstance().fire(TRACKING_RECONNECTING);
                            break;
                        case TRACKING_CONNECTING:
                                System.out.println("DISCONNECTED");
                            tracking.setStatus(TRACKING_ERROR);
                            State.getInstance().fire(TRACKING_ERROR);
                            break;
                    }
                }
                connected = false;
            }

        }
    }

    public void unregister(){
        try {
            State.getInstance().unregisterReceiver(this);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}