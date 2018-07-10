package com.edeqa.waytous.helpers;

import android.util.Log;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.BiFunction;
import com.edeqa.helpers.interfaces.BiConsumer;
import com.edeqa.helpers.interfaces.TriConsumer;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.edeqa.waytous.Constants.OPTIONS;
import static com.edeqa.waytous.Constants.REQUEST_SIGN_PROVIDER;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS_ACCEPTED;
import static com.edeqa.waytous.Firebase.SYNCED;
import static com.edeqa.waytous.helpers.Events.TRACKING_ACTIVE;
import static com.edeqa.waytous.helpers.Events.TRACKING_RECONNECTING;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
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
    String testValue;

    @Before
    public void setUp() throws Exception {

        if(State.getInstance().getTracking() == null) {
            String link = "http://" + OPTIONS.getServerHost() + Utils.getWrappedHttpPort() + "/track/" + TOKEN;

//            String deviceId = "test:"+ Misc.getUnique();
//            State.getInstance().setUid(deviceId);
            State.getInstance().setUid(State.getInstance().fetchUid());
            State.getInstance().getMe().getProperties().setName("Test " + Math.round(Math.round(Math.random()*100)));

            Log.d("UserID",State.getInstance().fetchUid());

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
                .setUid(State.getInstance().fetchUid())
                .setUserNumber("0")
                .setOnGetValue(new BiFunction<String, Object, Object>() {
                    @Override
                    public Object apply(String key, Object value) {
                        System.out.println("Got value: " + key + ", [value]: " + value);
                        resultKey = key;
                        resultValue = value;
                        return value;
                    }
                })
                .setOnSaveLocalValue(new TriConsumer<String, Object, Object>() {
                    @Override
                    public void accept(String key, Object newValue, Object oldValue) {
                        System.out.println("Save local: " + key + ", [new]: " + newValue + ", [old]: " + oldValue);
                        resultKey = key;
                        resultValue = newValue;
                    }
                })
                .setOnFinish(new BiConsumer<SyncFB.Mode, String>() {
                    @Override
                    public void accept(SyncFB.Mode mode, String key) {
                        System.out.println("Finish: " + key + ", [mode]: " + mode);
                        synchronized (syncObject) {
                            syncObject.notify();
                        }
                    }
                })
                .setOnError(new BiConsumer<String, Throwable>() {
                    @Override
                    public void accept(String key, Throwable error) {
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
        assertEquals("0", sync.getUserNumber());
    }

    @Test
    public void setUserNumber() throws Exception {
        sync.setUserNumber(10);
        assertEquals("10", sync.getUserNumber());
    }

    @Test
    public void setUserNumber1() throws Exception {
        sync.setUserNumber("10");
        assertEquals("10", sync.getUserNumber());
    }

    @Test
    public void getValues() throws Exception {

        // when value is adding with log == true then this action will be registered in history
        sync.setLog(true);
        sync.setKey(Firebase.CHANGED);
        sync.overrideRemoteValue(ServerValue.TIMESTAMP);
        waitObject();
        sync.setLog(false);

        // then get values from history
        sync.setKey(Firebase.HISTORY);
        sync.getValues();

        waitObject();
        assertTrue(resultKey.startsWith("-"));
        assertEquals("p/ch", ((Map)resultValue).get(Firebase.KEYS));
        assertEquals(SyncFB.Mode.OVERRIDE_REMOTE.toString(), ((Map)resultValue).get(Firebase.MODE));
        Long value = (Long) ((Map)resultValue).get(Firebase.VALUE);
        assertTrue(value >= 1507670266313L);
        assertTrue(value <= (new Date().getTime() + 10000));

    }

    @Test
    public void ready() throws Exception {
        assertTrue(sync.ready());
    }

    @Test
    public void getValue() throws Exception {
        sync.setKey(REQUEST_SIGN_PROVIDER);
        sync.getValue();

        waitObject();

        assertEquals("sign-provider", resultKey);
        assertEquals("admin", resultValue);

    }

    @Test
    public void syncValue() throws Exception {

        String key = "test-both";
        String value = "test-value-" + Math.random();

        // sync String value
        sync.setKey(key);
        sync.getValue();
        waitObject();
        assertEquals(null, resultValue);

        sync.syncValue(value);
        waitObject();

        sync.getValue();
        waitObject();
        assertEquals(value, resultValue);

        sync.syncValue(value + value);
        waitObject();

        sync.getValue();
        waitObject();
        assertEquals(value, resultValue);

        sync.removeRemoteValue();
        waitObject();

        sync.getValue();
        waitObject();
        assertEquals(null, resultValue);

        // test Map value
        key = "test-map-both";

        sync.setKey(key);
        Map testMap = new HashMap();
        testMap.put("a","b");
        testMap.put("c","d-" + Math.random());

        sync.getValue();
        waitObject();
        assertEquals(null, resultValue);

        sync.syncValue(testMap);
        waitObject();

        sync.getValue();
        waitObject();
        assertEquals(testMap.get("c"), ((Map)resultValue).get("c"));

        testMap = (Map) resultValue;
        testMap.remove(SYNCED);
        testMap.put("c","d-" + Math.random());

        sync.syncValue(testMap);
        waitObject();

        sync.getValue();
        waitObject();
        assertEquals(testMap.get("c"), ((Map)resultValue).get("c"));

        testMap = (Map) resultValue;
        testMap.put(SYNCED, (Long)testMap.get(SYNCED) - 10000);
        testMap.put("c","d-" + Math.random());

        sync.syncValue(testMap);
        waitObject();

        sync.getValue();
        waitObject();
        assertNotEquals(testMap.get("c"), ((Map)resultValue).get("c"));

        testMap = (Map) resultValue;

        sync.syncValue(null);
        waitObject();

        sync.getValue();
        waitObject();
        assertEquals(testMap.get("c"), ((Map)resultValue).get("c"));

        sync.removeRemoteValue();
        waitObject();

        sync.getValue();
        waitObject();
        assertEquals(null, resultValue);

    }

    @Test
    public void overrideRemoteValue() throws Exception {

        // test String key
        testValue = "test-value-" + Math.random();

        sync.setKey("test-key");
        sync.overrideRemoteValue(testValue);
        waitObject();

        sync.getValue();

        waitObject();

        assertEquals("test-key", resultKey);
        assertEquals(testValue, resultValue);

        // test Map key
        sync.setKey("test-map");
        Map testMap = new HashMap();
        testMap.put("a","b");
        testMap.put("c","d-" + Math.random());

        sync.overrideRemoteValue(testMap);
        waitObject();

        sync.getValue();

        waitObject();

        assertEquals("test-map", resultKey);
        assertEquals(testMap.get("a"), ((Map)resultValue).get("a"));
        assertEquals(testMap.get("c"), ((Map)resultValue).get("c"));

    }

    @Test
    public void updateRemoteValue() throws Exception {

        // test String key
        testValue = "test-value-" + Math.random();
        String testKey = ("test-key-" + Math.random()).replaceAll("\\.", "");
        sync.setKey(testKey);

        sync.getValue();

        waitObject();

        assertEquals(testKey, resultKey);
        assertEquals(null, resultValue);

        sync.updateRemoteValue(testValue);

        waitObject();

        sync.getValue();

        waitObject();

        assertEquals(testKey, resultKey);
        assertEquals(testValue, resultValue);

        String newTestValue = "test-value-" + Math.random();
        sync.updateRemoteValue(newTestValue);

        waitObject();

        sync.getValue();

        waitObject();

        assertEquals(testKey, resultKey);
        assertEquals(testValue, resultValue);

        sync.overrideRemoteValue(newTestValue);

        waitObject();

        sync.getValue();

        waitObject();

        assertEquals(testKey, resultKey);
        assertEquals(newTestValue, resultValue);

        sync.removeRemoteValue();
        System.out.println("D:");
        waitObject();


        // test Map key
        sync.setKey("test-map");
        Map testMap = new HashMap();
        testMap.put("a","b");
        testMap.put("c","d-" + Math.random());

        sync.updateRemoteValue(testMap);

        waitObject();

        sync.getValue();

        waitObject();

        assertEquals("test-map", resultKey);
        assertEquals(testMap.get("a"), ((Map)resultValue).get("a"));
        assertNotEquals(testMap.get("c"), ((Map)resultValue).get("c"));

    }

    @Test
    public void updateLocalValue() throws Exception {

        testValue = "test-value";

        sync.setKey("test-key");
        sync.updateLocalValue(testValue);

        waitObject();

        assertEquals(null, resultKey);
        assertNotEquals(testValue, resultValue);

        testValue = "";

        sync.setKey("test-key");
        sync.updateLocalValue(testValue);

        waitObject();

        assertEquals("test-key", resultKey);
        assertNotEquals(testValue, resultValue);

    }

    @Test
    public void overrideLocalValue() throws Exception {

        testValue = "test-value";

        sync.setKey("test-key");
        sync.overrideLocalValue(testValue);

        waitObject();

        assertEquals("test-key", resultKey);
        assertNotEquals(testValue, resultValue);

        testValue = "";

        sync.setKey("test-key");
        sync.overrideLocalValue(testValue);

        waitObject();

        assertEquals("test-key", resultKey);
        assertNotEquals(testValue, resultValue);

    }

    @Test
    public void syncValues() throws Exception {

        sync.setKey("test-sync-values");

        // sync values not existing on server
        ArrayList<Map<String, Object>> values = new ArrayList<>();
        HashMap<String, Object> value1 = new HashMap<>();
        value1.put("a", "a1");
        value1.put("b", "b1");

        HashMap<String, Object> value2 = new HashMap<>();
        value2.put("a", "a2");
        value2.put("b", "b2");
        value2.put(SYNCED, 1508165565800L);
        value2.put(Firebase.KEYS, "-Kw_etFOpRlTRlWchhMy");

        values.add(value1);
        values.add(value2);

        sync.syncValues(values);
        waitObject();
        assertEquals(2, values.size());

        sync.getValues();
        waitObject();

        System.out.println("VALUES1:"+values);
        assertTrue(resultKey.startsWith("-"));
        assertEquals(values.get(0).get(SYNCED), ((Map)resultValue).get(SYNCED));
        assertEquals(values.get(0).get("a"), ((Map)resultValue).get("a"));
        assertEquals(values.get(0).get("b"), ((Map)resultValue).get("b"));


        // sync values:
        // - one already exists,
        // - one new
        HashMap<String, Object> value3 = new HashMap<>();
        value3.put("a", "a3");
        value3.put("b", "b3");

        values.clear();
        values.add(value3);
        values.add(value2);

        sync.syncValues(values);
        waitObject();

        assertEquals(3, values.size());

        sync.getValues();
        waitObject();

        System.out.println("VALUES2:"+values);
        assertTrue(resultKey.startsWith("-"));
        assertEquals(value3.get(SYNCED), ((Map)resultValue).get(SYNCED));
        assertEquals(value3.get("a"), ((Map)resultValue).get("a"));
        assertEquals(value3.get("b"), ((Map)resultValue).get("b"));


        // sync values:
        // - one value updated but not updated SYNCED-value;
        // - second updated and increased SYNCED-value too;
        // - third changed and decreased SYNCED-value
        // - fourth value doesn't have SYNCED-value

        values.get(0).put("c","c1");

        values.get(1).put("c","c2");
        values.get(1).put(Firebase.SYNCED, (long) values.get(1).get(Firebase.SYNCED) + 10);

        values.get(2).put("c","c2");
        values.get(2).put(Firebase.SYNCED, (long) values.get(1).get(Firebase.SYNCED) - 10);

        HashMap<String, Object> value4 = new HashMap<>();
        value4.put("a", "a4");
        value4.put("b", "b4");
        values.add(value4);

        sync.syncValues(values);
        waitObject();

        System.out.println("VALUES3:"+values);
        assertEquals(4, values.size());

        sync.getValues();
        waitObject();

        System.out.println("VALUES4:"+values);
        assertTrue(resultKey.startsWith("-"));
        assertEquals(value4.get(SYNCED), ((Map)resultValue).get(SYNCED));
        assertEquals(value4.get("a"), ((Map)resultValue).get("a"));
        assertEquals(value4.get("b"), ((Map)resultValue).get("b"));

        // local values are empty
        values.clear();

        sync.syncValues(values);
        waitObject();

        System.out.println("VALUES5:"+values);
        assertEquals(4, values.size());

        // remove test values
        sync.removeRemoteValue();
        waitObject();

        resultKey = null;

        sync.getValues();
        waitObject();

        assertEquals(null, resultKey);
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
    }

    @Test
    public void setOnGetValue() throws Exception {
    }

    @Test
    public void setOnAddRemoteValue() throws Exception {
    }

    @Test
    public void setOnUpdateRemoteValue() throws Exception {
    }

    @Test
    public void setOnRemoveRemoteValue() throws Exception {
    }

    @Test
    public void setOnSaveRemoteValue() throws Exception {
    }

    @Test
    public void setOnAddLocalValue() throws Exception {
    }

    @Test
    public void setOnUpdateLocalValue() throws Exception {
    }

    @Test
    public void setOnRemoveLocalValue() throws Exception {
    }

    @Test
    public void setOnSaveLocalValue() throws Exception {
    }

    @Test
    public void setOnError() throws Exception {
    }

    @Test
    public void getReference() throws Exception {
        assertEquals("https://waytous-beta.firebaseio.com", sync.getReference().toString());
    }

    @Test
    public void setReference() throws Exception {
        sync.setReference(null);
        assertEquals(null, sync.getReference());
    }

    @Test
    public void getMode() throws Exception {
        assertEquals(null, sync.getMode());
    }

    @Test
    public void setMode() throws Exception {
        sync.setMode(SyncFB.Mode.SKIP);
        assertEquals(SyncFB.Mode.SKIP, sync.getMode());
    }

    @Test
    public void isLog() throws Exception {
        assertFalse(sync.isLog());
    }

    @Test
    public void setLog() throws Exception {
        sync.setLog(true);
        assertTrue(sync.isLog());

        sync.setLog(false);
        assertFalse(sync.isLog());
    }

    @Test
    public void getKey() throws Exception {
        assertEquals(null, sync.getKey());
    }

    @Test
    public void setKey() throws Exception {
        sync.setKey(SYNCED);
        assertEquals(SYNCED, sync.getKey());
    }

    @Test
    public void getChild() throws Exception {
        assertEquals(null, sync.getChild());
    }

    @Test
    public void setChild() throws Exception {
        sync.setChild(Firebase.TERMS_OF_SERVICE_CONFIRMED);
        assertEquals(Firebase.TERMS_OF_SERVICE_CONFIRMED, sync.getChild());
    }

    @Test
    public void getUid() throws Exception {
        assertEquals(State.getInstance().fetchUid(), sync.getUid());
    }

    @Test
    public void setUid() throws Exception {
        String uid = Misc.getUnique();
        sync.setUid(uid);
        assertEquals(uid, sync.getUid());
    }

    @Test
    public void getType() throws Exception {
        assertEquals(SyncFB.Type.ACCOUNT_PRIVATE, sync.getType());
    }

    @Test
    public void setType() throws Exception {
        sync.setType(SyncFB.Type.ACCOUNT_PRIVATE);
        assertEquals(SyncFB.Type.ACCOUNT_PRIVATE, sync.getType());
    }

    @Test
    public void getGroup() throws Exception {
        assertEquals(null, sync.getGroup());
    }

    @Test
    public void setGroup() throws Exception {
        String group = Misc.getUnique();
        sync.setGroup(group);
        assertEquals(group, sync.getGroup());
    }

    @Test
    public void removeRemoteValue() throws Exception {
    }

    @Test
    public void removeLocalValue() throws Exception {
    }

    @Test
    public void setOnFinish() throws Exception {
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


    private void waitObject() {
        synchronized (syncObject) {
            try {
                syncObject.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}