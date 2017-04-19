package com.edeqa.waytous.interfaces;

import org.json.JSONObject;

/**
 * Created 1/31/17.
 */
public interface Tracking {

    String TRACKING_URI = "uri";

    void start();

    void stop();

    Tracking put(String key, String value);

    Tracking put(String key, Boolean value);

    Tracking put(String key, Number value);

    void send();

    void send(String text);

    void send(JSONObject o);

    /*   public void sendUpdate(JSONObject o) {
               try {
                   o.put(REQUEST, REQUEST_UPDATE);
               } catch (JSONException e) {
                   e.printStackTrace();
               }
               send(o);
           }
       */
    void sendUpdate();

    void sendMessage(String key, String value);

    void sendMessage(String type, JSONObject json);

    String getStatus();

    void setStatus(String status);

    void setTrackingListener(TrackingCallback trackingListener);

    void postMessage(JSONObject json);

    String getToken();

    void setToken(String token);

    String getTrackingUri();
}
