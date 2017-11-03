package com.edeqa.waytous.helpers;

import android.util.Log;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytous.State;
import com.edeqa.waytous.interfaces.TrackingCallback;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static com.edeqa.waytous.Constants.OPTIONS;
import static com.edeqa.waytous.Constants.REQUEST_KEY;
import static com.edeqa.waytous.Constants.REQUEST_MESSAGE;
import static com.edeqa.waytous.Constants.REQUEST_TIMESTAMP;
import static com.edeqa.waytous.Constants.REQUEST_TRACKING;
import static com.edeqa.waytous.Constants.RESPONSE_NUMBER;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS_ACCEPTED;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS_UPDATED;
import static com.edeqa.waytous.Constants.USER_ACCURACY;
import static com.edeqa.waytous.Constants.USER_ALTITUDE;
import static com.edeqa.waytous.Constants.USER_BEARING;
import static com.edeqa.waytous.Constants.USER_JOINED;
import static com.edeqa.waytous.Constants.USER_LATITUDE;
import static com.edeqa.waytous.Constants.USER_LONGITUDE;
import static com.edeqa.waytous.Constants.USER_MESSAGE;
import static com.edeqa.waytous.Constants.USER_NUMBER;
import static com.edeqa.waytous.Constants.USER_PROVIDER;
import static com.edeqa.waytous.Constants.USER_SPEED;
import static com.edeqa.waytous.helpers.Events.TRACKING_ACTIVE;
import static com.edeqa.waytous.helpers.Events.TRACKING_CONNECTING;
import static com.edeqa.waytous.helpers.Events.TRACKING_DISABLED;
import static com.edeqa.waytous.helpers.Events.TRACKING_RECONNECTING;
import static junit.framework.Assert.assertEquals;

/**
 * Created 9/11/2017.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MyTrackingFBTest {

    private final static String TOKEN = "TEST";

    private MyTrackingFB tracking;
    private String link;
    final Object syncObject = new Object();
    final Object syncMessage = new Object();


    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {
        State.getInstance().setTracking(null);
    }

    /*
    new group
        new user
        existing user
    join group
        new user
            - correct group
            - incorrect group
        existing user
            - correct group correct user
            - correct group incorrect user
            - incorrect group correct user
            - inccorrect group incorrect user
     */


    @Test
    public void testNewGroup1_NewUser() throws Exception {

        link = "http://" + OPTIONS.getServerHost() + Utils.getWrappedHttpPort() + "/track/" + TOKEN;

        String deviceId = "test:"+ Misc.getUnique();
        State.getInstance().setUid(deviceId);
        State.getInstance().getMe().getProperties().setName("Test " + Math.round(Math.round(Math.random()*100)));

        tracking = new MyTrackingFB();
        State.getInstance().setTracking(tracking);
        tracking.setTrackingListener(onTrackingListener);
        tracking.start();
        assertEquals(TRACKING_CONNECTING, tracking.getStatus());
        synchronized (syncObject){
            syncObject.wait();
        }

        tracking.put("BOOLEAN", true);
        tracking.put("NUMBER", 1);
        tracking.put("STRING", "test");
//        tracking.send();

        assertEquals(TRACKING_ACTIVE, tracking.getStatus());

        assertEquals(true, tracking.getTrackingUri().length() > 0);

//        tracking.send(CHANGE_NAME, "test");
//        synchronized (syncMessage){
//            syncMessage.wait();
//        }


        tracking.stop();
        assertEquals(TRACKING_DISABLED, tracking.getStatus());

    }

    @Test
    public void testNewGroup2_ExistingUser() throws Exception {
        link = "http://" + OPTIONS.getServerHost() + Utils.getWrappedHttpPort() + "/track/" + TOKEN;

        tracking = new MyTrackingFB();
        State.getInstance().setTracking(tracking);
        tracking.setTrackingListener(onTrackingListener);
        tracking.start();
        assertEquals(TRACKING_CONNECTING, tracking.getStatus());
        synchronized (syncObject){
            syncObject.wait();
        }

        tracking.put("BOOLEAN", true);
        tracking.put("NUMBER", 1);
        tracking.put("STRING", "test");
//        tracking.send();

        assertEquals(TRACKING_ACTIVE, tracking.getStatus());

        assertEquals(true, tracking.getTrackingUri().length() > 0);

        tracking.stop();
        assertEquals(TRACKING_DISABLED, tracking.getStatus());
    }

    @Test
    public void testCorrectGroup1_NewUser() throws Exception {
        link = "http://" + OPTIONS.getServerHost() + Utils.getWrappedHttpPort() + "/track/" + TOKEN;

        String deviceId = "test:"+ Misc.getUnique();
        State.getInstance().setUid(deviceId);
        State.getInstance().getMe().getProperties().setName("Test " + Math.round(Math.round(Math.random()*100)));

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
    public void testCorrectGroup2_ExistingUser() throws Exception {
        link = "http://" + OPTIONS.getServerHost() + Utils.getWrappedHttpPort() + "/track/" + TOKEN;

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
    public void testCorrectGroup_Track() throws Exception {
        link = "http://" + OPTIONS.getServerHost() + Utils.getWrappedHttpPort() + "/track/" + TOKEN;
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
    public void testCorrectGroup_Group() throws Exception {
        link = "http://" + OPTIONS.getServerHost() + Utils.getWrappedHttpPort() + "/group/" + TOKEN;
        tracking = new MyTrackingFB(link);

        link = "http://" + OPTIONS.getServerHost() + Utils.getWrappedHttpPort() + "/track/" + TOKEN;
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

        tracking.setOnSendSuccess(new Runnable() {
            @Override
            public void run() {
                System.out.println("TESTTEST:");
            }
        });

        tracking.setOnSendFailure(new Runnable1<Throwable>() {
            @Override
            public void call(Throwable error) {
                System.out.println("ERRERR:");

            }
        });
        tracking.send();


        assertEquals(TRACKING_ACTIVE, tracking.getStatus());

        assertEquals(TOKEN, tracking.getToken());
        assertEquals(link, tracking.getTrackingUri());

        tracking.stop();
        assertEquals(TRACKING_DISABLED, tracking.getStatus());

    }

    @Test
    public void testWrongGroup_Track() throws Exception {
        link = "http://" + OPTIONS.getServerHost() + Utils.getWrappedHttpPort() + "/track/" + TOKEN + TOKEN;
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

    @Test
    public void testWrongGroup_Group() throws Exception {
        link = "http://" + OPTIONS.getServerHost() + Utils.getWrappedHttpPort() + "/group/" + TOKEN + TOKEN;
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



    public TrackingCallback onTrackingListener = new TrackingCallback() {

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
            Log.e("TEST:onReject", reason);
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
            Log.d("TEST:", o.toString());
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
                synchronized (syncMessage){
                    syncMessage.notify();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    };


    @Test
    public void start() throws Exception {
    }

    @Test
    public void stop() throws Exception {
    }

    @Test
    public void put() throws Exception {
    }

    @Test
    public void put1() throws Exception {
    }

    @Test
    public void put2() throws Exception {
    }

    @Test
    public void send() throws Exception {
    }

    @Test
    public void send1() throws Exception {
    }

    @Test
    public void send2() throws Exception {
    }

    @Test
    public void sendUpdate() throws Exception {
    }

    @Test
    public void sendMessage() throws Exception {
    }

    @Test
    public void sendMessage1() throws Exception {
    }

    @Test
    public void getStatus() throws Exception {
    }

    @Test
    public void setStatus() throws Exception {
    }

    @Test
    public void setTrackingListener() throws Exception {
    }

    @Test
    public void postMessage() throws Exception {
    }

    @Test
    public void getToken() throws Exception {
    }

    @Test
    public void setToken() throws Exception {
    }

    @Test
    public void getTrackingUri() throws Exception {
    }

}