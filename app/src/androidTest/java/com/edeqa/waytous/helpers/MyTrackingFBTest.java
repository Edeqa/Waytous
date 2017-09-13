package com.edeqa.waytous.helpers;

import android.util.Log;

import com.edeqa.waytous.State;
import com.edeqa.waytous.interfaces.TrackingCallback;
import com.edeqa.waytousserver.helpers.Common;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static com.edeqa.waytous.helpers.Events.TRACKING_ACTIVE;
import static com.edeqa.waytous.helpers.Events.TRACKING_DISABLED;
import static com.edeqa.waytous.helpers.Events.TRACKING_RECONNECTING;
import static com.edeqa.waytousserver.helpers.Constants.REQUEST_KEY;
import static com.edeqa.waytousserver.helpers.Constants.REQUEST_MESSAGE;
import static com.edeqa.waytousserver.helpers.Constants.REQUEST_TIMESTAMP;
import static com.edeqa.waytousserver.helpers.Constants.REQUEST_TRACKING;
import static com.edeqa.waytousserver.helpers.Constants.RESPONSE_NUMBER;
import static com.edeqa.waytousserver.helpers.Constants.RESPONSE_STATUS;
import static com.edeqa.waytousserver.helpers.Constants.RESPONSE_STATUS_ACCEPTED;
import static com.edeqa.waytousserver.helpers.Constants.RESPONSE_STATUS_UPDATED;
import static com.edeqa.waytousserver.helpers.Constants.SENSITIVE;
import static com.edeqa.waytousserver.helpers.Constants.USER_ACCURACY;
import static com.edeqa.waytousserver.helpers.Constants.USER_ALTITUDE;
import static com.edeqa.waytousserver.helpers.Constants.USER_BEARING;
import static com.edeqa.waytousserver.helpers.Constants.USER_JOINED;
import static com.edeqa.waytousserver.helpers.Constants.USER_LATITUDE;
import static com.edeqa.waytousserver.helpers.Constants.USER_LONGITUDE;
import static com.edeqa.waytousserver.helpers.Constants.USER_MESSAGE;
import static com.edeqa.waytousserver.helpers.Constants.USER_NUMBER;
import static com.edeqa.waytousserver.helpers.Constants.USER_PROVIDER;
import static com.edeqa.waytousserver.helpers.Constants.USER_SPEED;
import static junit.framework.Assert.assertEquals;

/**
 * Created 9/11/2017.
 */
public class MyTrackingFBTest {

    private final static String TOKEN = "TEST";

    private MyTrackingFB tracking;
    private String link;
    final Object syncObject = new Object();


    @Before
    public void setUp() throws Exception {


    }

    @Test
    public void testCorrectToken() throws Exception {
        link = "http://" + SENSITIVE.getServerHost() + Common.getWrappedHttpPort() + "/track/" + TOKEN;
        tracking = new MyTrackingFB(link);
        State.getInstance().setTracking(tracking);
        tracking.setTrackingListener(onTrackingListener);
        tracking.setToken(TOKEN);
        tracking.start();
        assertEquals(TRACKING_RECONNECTING, tracking.getStatus());
        synchronized (syncObject){
            syncObject.wait();
        }

        tracking.put("BOOLEAN", true);
        tracking.put("NUMBER", 1);
        tracking.put("STRING", "test");
//        tracking.send();

        assertEquals(TRACKING_ACTIVE, tracking.getStatus());

        assertEquals(TOKEN, tracking.getToken());
        assertEquals(link, tracking.getTrackingUri());

        tracking.stop();
        assertEquals(TRACKING_DISABLED, tracking.getStatus());


    }

    @Test
    public void testWrongToken() throws Exception {
        link = "http://" + SENSITIVE.getServerHost() + Common.getWrappedHttpPort() + "/track/" + TOKEN + TOKEN;
        tracking = new MyTrackingFB(link);
        State.getInstance().setTracking(tracking);
        tracking.setTrackingListener(onTrackingListener);
        tracking.setToken(TOKEN + TOKEN);
        tracking.start();
        assertEquals(TRACKING_RECONNECTING, tracking.getStatus());

        synchronized (syncObject){
            syncObject.wait();
        }

        assertEquals(TRACKING_DISABLED, tracking.getStatus());

    }


    private TrackingCallback onTrackingListener = new TrackingCallback() {

        @Override
        public void onCreating() {
            assertEquals(true, true);
            synchronized (syncObject){
                syncObject.notify();
            }
        }

        @Override
        public void onJoining(String tokenId) {
            assertEquals(true, tokenId.startsWith(TOKEN));
            synchronized (syncObject){
                syncObject.notify();
            }
        }

        @Override
        public void onReconnecting() {
            assertEquals(true, false);
            synchronized (syncObject){
                syncObject.notify();
            }
        }

        @Override
        public void onClose() {
            assertEquals(true, false);
            synchronized (syncObject){
                syncObject.notify();
            }
        }

        @Override
        public void onAccept(JSONObject o) {
            try {
                assertEquals(RESPONSE_STATUS_ACCEPTED, o.getString(RESPONSE_STATUS));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            synchronized (syncObject){
                syncObject.notify();
            }
        }

        @Override
        public void onReject(String reason) {
            Log.d("TEST:onReject", reason);
            assertEquals(true, reason.startsWith("This group is expired"));
            synchronized (syncObject){
                syncObject.notify();
            }
        }

        @Override
        public void onStop() {
            assertEquals(true, true);
            synchronized (syncObject){
                syncObject.notify();
            }
        }

        @Override
        public void onMessage(final JSONObject o) {
            Log.d("TEST", o.toString());
            try {
                String status = o.getString(RESPONSE_STATUS);
                switch(status) {
                    case RESPONSE_STATUS_UPDATED:
                        assertEquals(false, o.has(RESPONSE_NUMBER) && o.has(USER_JOINED));
                        break;
                    case REQUEST_TRACKING:
                        assertEquals(true, o.has(USER_NUMBER)
                                && o.has(USER_PROVIDER)
                                && o.has(USER_LATITUDE)
                                && o.has(USER_LONGITUDE)
                                && o.has(USER_ALTITUDE)
                                && o.has(USER_SPEED)
                                && o.has(USER_ACCURACY)
                                && o.has(USER_BEARING)
                                && o.has(REQUEST_KEY)
                                && o.has(REQUEST_TIMESTAMP)
                        );
                        break;
                    case REQUEST_MESSAGE:
                        Log.d("TEST",o.toString());
                        assertEquals(true, o.has(USER_NUMBER)
                                && o.has(USER_MESSAGE)
                                && o.has(REQUEST_KEY)
                                && o.has(REQUEST_TIMESTAMP)
                        );
                        break;

                    default:
                        assertEquals("", o.toString());
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    };

}