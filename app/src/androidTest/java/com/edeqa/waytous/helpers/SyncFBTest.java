package com.edeqa.waytous.helpers;

import android.util.Log;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Callable2;
import com.edeqa.helpers.interfaces.Runnable2;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytous.State;
import com.edeqa.waytous.interfaces.TrackingCallback;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Date;
import java.util.Map;

import static com.edeqa.waytous.Constants.OPTIONS;
import static com.edeqa.waytous.Constants.REQUEST_KEY;
import static com.edeqa.waytous.Constants.REQUEST_MESSAGE;
import static com.edeqa.waytous.Constants.REQUEST_SIGN_PROVIDER;
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
import static com.edeqa.waytous.helpers.Events.TRACKING_RECONNECTING;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.*;

/**
 * Created 10/10/2017.
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SyncFBTest {

    private final static String TOKEN = "TEST";

    final Object syncObject = new Object();
    final Object syncTracking = new Object();
    SyncFB sync;
    MyTrackingFB tracking;
    String resultKey;
    Object resultValue;

    @Before
    public void setUp() throws Exception {

        if(State.getInstance().getTracking() == null) {
            String link = "http://" + OPTIONS.getServerHost() + Utils.getWrappedHttpPort() + "/track/" + TOKEN;

//            String deviceId = "test:"+ Misc.getUnique();
//            State.getInstance().setDeviceId(deviceId);
            State.getInstance().setDeviceId(State.getInstance().getDeviceId());
            State.getInstance().getMe().getProperties().setName("Test " + Math.round(Math.round(Math.random()*100)));

            Log.d("UserID",Misc.getEncryptedHash(State.getInstance().getDeviceId()));

            tracking = new MyTrackingFB(link);
            State.getInstance().setTracking(tracking);
            tracking.setTrackingListener(onTrackingListener);
            tracking.setToken(TOKEN);
            tracking.start();
            assertEquals(TRACKING_RECONNECTING, tracking.getStatus());
            synchronized (syncTracking) {
                syncTracking.wait();
            }
            assertEquals(TRACKING_ACTIVE, tracking.getStatus());
        }

        sync = new SyncFB()
                .setDebug(true)
                .setType(SyncFB.Type.ACCOUNT_PRIVATE)
                .setUid(Misc.getEncryptedHash(State.getInstance().getDeviceId()))
                .setUserNumber("0")
                .setOnGetValue(new Callable2<Object, String, Object>() {
                    @Override
                    public Object call(String key, Object value) {
                        System.out.println("Got Value: " + key + ", [value]: " + value);
                        resultKey = key;
                        resultValue = value;
                        return null;
                    }
                })
                .setOnFinish(new Runnable2<SyncFB.Mode, String>() {
                    @Override
                    public void call(SyncFB.Mode mode, String key) {
                        System.out.println("Finish: " + key + ", [mode]: " + mode);
                        synchronized (syncObject) {
                            syncObject.notify();
                        }
                    }
                })
                .setOnError(new Runnable2<String, Throwable>() {
                    @Override
                    public void call(String key, Throwable error) {
                        System.err.println("Error: " + key);
                        error.printStackTrace();

                        assertTrue(error.getMessage(), false);

                        synchronized (syncObject) {
                            syncObject.notify();
                        }
                    }
                })
                .setReference(FirebaseDatabase.getInstance().getReference());

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void getUserNumber() throws Exception {
        // TODO

    }

    @Test
    public void setUserNumber() throws Exception {
        // TODO

    }

    @Test
    public void setUserNumber1() throws Exception {
        // TODO

    }

    @Test
    public void getValues() throws Exception {

        // when value is adding with log == true then this action will be registered in history
        sync.setLog(true);
        sync.setKey(Firebase.CHANGED);
        sync.overrideRemoteValue(ServerValue.TIMESTAMP);
        synchronized (syncObject) {
            syncObject.wait();
        }

        // then get values from history
        sync.setKey(Firebase.HISTORY);
        sync.getValues();

        synchronized (syncObject) {
            syncObject.wait();
        }
        assertTrue(resultKey.startsWith("-"));
        assertEquals("p/ch", ((Map)resultValue).get(Firebase.KEYS));
        assertEquals(SyncFB.Mode.OVERRIDE_REMOTE.toString(), ((Map)resultValue).get(Firebase.MODE));
        Long value = (Long) ((Map)resultValue).get(Firebase.VALUE);
        assertTrue(value > 1507670266313L);
        assertTrue(value < (new Date().getTime() + 10000));

    }

    @Test
    public void ready() throws Exception {
        assertTrue(sync.ready());
    }

    @Test
    public void getValue() throws Exception {
        sync.setKey(REQUEST_SIGN_PROVIDER);
        sync.getValue();

        synchronized (syncObject) {
            syncObject.wait();
        }

        assertEquals("sign-provider", resultKey);
        assertEquals("anonymous", resultValue);

    }

    @Test
    public void syncValue() throws Exception {
        // TODO

    }

    @Test
    public void updateRemoteValue() throws Exception {
        // TODO

    }

    @Test
    public void overrideRemoteValue() throws Exception {


    }

    @Test
    public void updateLocalValue() throws Exception {
        // TODO

    }

    @Test
    public void overrideLocalValue() throws Exception {
        // TODO

    }

    @Test
    public void syncValues() throws Exception {
        // TODO

    }

    @Test
    public void watch() throws Exception {
        // TODO

    }

    @Test
    public void watchChanges() throws Exception {
        // TODO

    }

    @Test
    public void setDebug() throws Exception {
        // TODO

    }

    @Test
    public void setOnGetValue() throws Exception {
        // TODO

    }

    @Test
    public void setOnAddRemoteValue() throws Exception {
        // TODO

    }

    @Test
    public void setOnUpdateRemoteValue() throws Exception {
        // TODO

    }

    @Test
    public void setOnRemoveRemoteValue() throws Exception {
        // TODO

    }

    @Test
    public void setOnSaveRemoteValue() throws Exception {
        // TODO

    }

    @Test
    public void setOnAddLocalValue() throws Exception {
        // TODO

    }

    @Test
    public void setOnUpdateLocalValue() throws Exception {
        // TODO

    }

    @Test
    public void setOnRemoveLocalValue() throws Exception {
        // TODO

    }

    @Test
    public void setOnSaveLocalValue() throws Exception {
        // TODO

    }

    @Test
    public void setOnFinish() throws Exception {
        // TODO

    }

    @Test
    public void setOnError() throws Exception {
        // TODO

    }

    @Test
    public void getReference() throws Exception {
        // TODO

    }

    @Test
    public void setReference() throws Exception {
        // TODO

    }

    @Test
    public void getMode() throws Exception {
        // TODO

    }

    @Test
    public void setMode() throws Exception {
        // TODO

    }

    @Test
    public void isLog() throws Exception {
        // TODO

    }

    @Test
    public void setLog() throws Exception {
        // TODO

    }

    @Test
    public void getKey() throws Exception {
        // TODO

    }

    @Test
    public void setKey() throws Exception {
        // TODO

    }

    @Test
    public void getChild() throws Exception {
        // TODO

    }

    @Test
    public void setChild() throws Exception {
        // TODO

    }

    @Test
    public void getUid() throws Exception {
        // TODO

    }

    @Test
    public void setUid() throws Exception {
        // TODO

    }

    @Test
    public void getType() throws Exception {
        // TODO

    }

    @Test
    public void setType() throws Exception {
        // TODO

    }

    @Test
    public void getGroup() throws Exception {
        // TODO

    }

    @Test
    public void setGroup() throws Exception {
        // TODO

    }




    private TrackingCallback onTrackingListener = new TrackingCallback() {

        @Override
        public void onCreating() {
            assertEquals(true, true);
        }

        @Override
        public void onJoining(String tokenId) {
            assertEquals(true, tokenId.startsWith(TOKEN));
        }

        @Override
        public void onReconnecting() {
            assertEquals(true, false);
        }

        @Override
        public void onClose() {
            assertEquals(true, false);
        }

        @Override
        public void onAccept(JSONObject o) {
            try {
                assertEquals(RESPONSE_STATUS_ACCEPTED, o.getString(RESPONSE_STATUS));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onReject(String reason) {
            Log.e("TEST:onReject", reason);
            assertEquals(true, reason.startsWith("This group is expired"));
        }

        @Override
        public void onStop() {
            assertEquals(true, true);
        }

        @Override
        public void onMessage(final JSONObject o) {
            Log.d("TEST:", o.toString());
            synchronized (syncTracking){
                syncTracking.notify();
            }
        }

    };


}