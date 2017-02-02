package ru.wtg.whereaminow.helpers;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.neovisionaries.ws.client.PayloadGenerator;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketState;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.interfaces.EntityHolder;
import ru.wtg.whereaminow.interfaces.Tracking;
import ru.wtg.whereaminow.interfaces.TrackingCallback;

import static ru.wtg.whereaminow.State.EVENTS.CHANGE_NAME;
import static ru.wtg.whereaminow.State.EVENTS.TRACKING_ACTIVE;
import static ru.wtg.whereaminow.State.EVENTS.TRACKING_CONNECTING;
import static ru.wtg.whereaminow.State.EVENTS.TRACKING_DISABLED;
import static ru.wtg.whereaminow.State.EVENTS.TRACKING_RECONNECTING;
import static ru.wtg.whereaminow.holders.MessagesHolder.PRIVATE_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.DATABASE_SECTION_OPTIONS_WELCOME_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.DATABASE_SECTION_PRIVATE;
import static ru.wtg.whereaminowserver.helpers.Constants.DATABASE_SECTION_PUBLIC;
import static ru.wtg.whereaminowserver.helpers.Constants.DATABASE_SECTION_USERS_DATA;
import static ru.wtg.whereaminowserver.helpers.Constants.HTTP_PORT;
import static ru.wtg.whereaminowserver.helpers.Constants.INACTIVE_USER_DISMISS_DELAY;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_CHANGE_NAME;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_CHECK_USER;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_DELIVERY_CONFIRMATION;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_DEVICE_ID;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_HASH;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_JOIN_TOKEN;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_MANUFACTURER;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_MODEL;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_NEW_TOKEN;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_OS;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_PUSH;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_TIMESTAMP;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_TOKEN;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_UPDATE;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_WELCOME_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_CONTROL;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_INITIAL;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_NUMBER;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_SIGN;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_ACCEPTED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_CHECK;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_ERROR;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_UPDATED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_TOKEN;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_DISMISSED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_JOINED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_NAME;
import static ru.wtg.whereaminowserver.helpers.Constants.WSSFB_PORT;
import static ru.wtg.whereaminowserver.helpers.Constants.WSS_SERVER_HOST;

/**
 * Created 1/29/17.
 */

public class MyTrackingFB implements Tracking {


    private final static int CONNECTION_TIMEOUT = 5;
    private final static int RECONNECTION_DELAY = 5;

    private Map<DatabaseReference, Object> refs;

    private State state;
    private URI serverUri;
    private TrackingCallback trackingListener;
    private WebSocket webSocket;
    private JSONObject builder;
    private FirebaseDatabase database;
    private DatabaseReference ref;
    private Handler handler = new Handler(Looper.myLooper());

    private String status = TRACKING_DISABLED;
    private String token;

    private boolean newTracking;
    private ValueEventListener usersDataNameListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            System.out.println("usersDataNameListenerChanged:"+dataSnapshot.getKey()+":"+dataSnapshot.getValue());
            try {
                int number = Integer.parseInt(dataSnapshot.getRef().getParent().getKey());
                final String name = String.valueOf(dataSnapshot.getValue());
                state.getUsers().forUser(number, new MyUsers.Callback() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        if (!name.equals(myUser.getProperties().getName())) {
                            myUser.fire(CHANGE_NAME, name);
                        }
                    }
                });
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };
    private ValueEventListener usersDataActiveListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
//            System.out.println("usersDataActiveListenerChanged:"+dataSnapshot.getRef().getParent().getKey()+":"+dataSnapshot.getValue());

            try {
                int number = Integer.parseInt(dataSnapshot.getRef().getParent().getKey());
                final boolean active = Boolean.parseBoolean(String.valueOf(dataSnapshot.getValue()));
                state.getUsers().forUser(number, new MyUsers.Callback() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        if (myUser.getProperties().isActive() != active) {
                            try {
                                JSONObject o = new JSONObject();
                                o.put(RESPONSE_STATUS, RESPONSE_STATUS_UPDATED);
                                o.put(active ? USER_JOINED : USER_DISMISSED, number);
                                o.put(RESPONSE_NUMBER, number);
                                trackingListener.onMessage(o);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                });
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };
    private ChildEventListener userPublicDataListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//            System.out.println("userDependentListenerADDED:"+dataSnapshot.getKey()+":"+dataSnapshot.getRef().getParent().getParent().getKey()+":"+dataSnapshot.getValue()+":"+s);
            try {
                JSONObject o = new JSONObject((Map<String, String>) dataSnapshot.getValue());
                o.put(RESPONSE_NUMBER, Integer.parseInt(dataSnapshot.getRef().getParent().getKey()));
                o.put(RESPONSE_STATUS, dataSnapshot.getRef().getParent().getParent().getKey());
                o.put("key", dataSnapshot.getKey());

                trackingListener.onMessage(o);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };
    private ChildEventListener usersDataListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//            System.out.println("usersDataListenerADDED:"+dataSnapshot.getValue().getClass()+":"+dataSnapshot.getKey()+":"+dataSnapshot.getValue()+":"+s);

//            System.out.println(state.getMe().getProperties().getNumber()+":"+dataSnapshot.getKey()+":"+(state.getMe().getProperties().getNumber()+"").equals(dataSnapshot.getKey()));

            if(!(state.getMe().getProperties().getNumber()+"").equals(dataSnapshot.getKey())) {
                try {
                    JSONObject o = new JSONObject((Map<String, String>) dataSnapshot.getValue());
                    o.put(RESPONSE_NUMBER, Integer.parseInt(dataSnapshot.getKey()));
                    o.put(RESPONSE_INITIAL, true);

                    MyUser user = State.getInstance().getUsers().addUser(o);
                    user.setUser(true);

                    registerValueListener(ref.child(DATABASE_SECTION_USERS_DATA).child(""+user.getProperties().getNumber()).child("name"),usersDataNameListener);
                    registerValueListener(ref.child(DATABASE_SECTION_USERS_DATA).child(""+user.getProperties().getNumber()).child("active"),usersDataActiveListener);

                    usersDataNameListener.onDataChange(dataSnapshot.child("name"));
                    usersDataActiveListener.onDataChange(dataSnapshot.child("active"));

                    for(Map.Entry<String,EntityHolder> entry: state.getAllHolders().entrySet()) {
                        if(entry.getValue().isSaveable()) {
                            registerChildListener(ref.child(DATABASE_SECTION_PUBLIC).child(entry.getKey()).child(""+user.getProperties().getNumber()), userPublicDataListener).limitToLast(1);
                        }
                    }
                    trackingListener.onAccept(o);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            onChildChanged(dataSnapshot, s);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };
    private ChildEventListener userPrivateDataListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//            System.out.println("userPrivateAdded:"+dataSnapshot.getKey()+":"+dataSnapshot.getRef().getParent().getParent().getKey()+":"+dataSnapshot.getValue()+":"+s);
            try {
                JSONObject o = new JSONObject((Map<String, String>) dataSnapshot.getValue());

                int from = Integer.parseInt(o.getString("from"));
                o.remove("from");
                o.put(RESPONSE_NUMBER, from);
                o.put(RESPONSE_STATUS, dataSnapshot.getRef().getParent().getParent().getKey());
                o.put("key", dataSnapshot.getKey());
                o.put(PRIVATE_MESSAGE, true);

                trackingListener.onMessage(o);

                dataSnapshot.getRef().removeValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };
    @SuppressWarnings("FieldCanBeLocal")
    private WebSocketAdapter webSocketListener = new WebSocketAdapter() {
        @Override
        public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
            super.onConnected(websocket, headers);
            if(TRACKING_DISABLED.equals(getStatus())) return;
            Log.i("MyTrackingFB","onConnected");
            if(newTracking) {
                put(REQUEST, REQUEST_NEW_TOKEN);
            } else {
                put(REQUEST, REQUEST_JOIN_TOKEN);

                String path = MyTrackingFB.this.serverUri.getPath();
                if(path != null) {
                    String[] parts = path.split("/");
                    if(parts.length > 2) {
                        setToken(parts[2]);
                    }
                }
                put(REQUEST_TOKEN, getToken());
                put(REQUEST_DEVICE_ID, State.getInstance().getDeviceId());
            }
            if(!TRACKING_RECONNECTING.equals(getStatus())) {
                put(REQUEST_DEVICE_ID, State.getInstance().getDeviceId());
            }
            put(REQUEST_MODEL, Build.MODEL);
            put(REQUEST_MANUFACTURER, Build.MANUFACTURER);
            put(REQUEST_OS, "android");
            if(state.getMe().getProperties().getName() != null && state.getMe().getProperties().getName().length()>0){
                put(USER_NAME,state.getMe().getProperties().getName());
            }
            send();
        }

        @Override
        public void onTextMessage(WebSocket websocket, String message) {
            if(TRACKING_DISABLED.equals(getStatus())) return;
            try {
                final JSONObject o = new JSONObject(message);
                if (!o.has(RESPONSE_STATUS)) return;
                switch (o.getString(RESPONSE_STATUS)) {
                    case RESPONSE_STATUS_CHECK:
                        if (o.has(RESPONSE_CONTROL)) {
                            String control = o.getString(RESPONSE_CONTROL);
                            String deviceId = State.getInstance().getDeviceId();
                            String hash = Utils.getEncryptedHash(control + ":" + deviceId);
                            put(REQUEST,REQUEST_CHECK_USER);
                            put(REQUEST_HASH,hash);
                            send();
                        }
                        break;
                    case RESPONSE_STATUS_ACCEPTED:
                        newTracking = false;

                        if (o.has(RESPONSE_SIGN)) {
                            String authToken = o.getString(RESPONSE_SIGN);
                            o.remove(RESPONSE_SIGN);

                            FirebaseAuth.getInstance().signInWithCustomToken(authToken).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    try {
                                        setStatus(TRACKING_ACTIVE);
                                        if (o.has(RESPONSE_TOKEN)) {
                                            setToken(o.getString(RESPONSE_TOKEN));
                                        }
                                        if (o.has(RESPONSE_NUMBER)) {
                                            state.getUsers().setMyNumber(o.getInt(RESPONSE_NUMBER));
                                        }
                                        o.put(RESPONSE_INITIAL, true);

                                        ref = database.getReference().child(getToken());

                                        registerChildListener(ref.child(DATABASE_SECTION_USERS_DATA),usersDataListener);
                                        for(Map.Entry<String,EntityHolder> entry: state.getAllHolders().entrySet()) {
                                            if(entry.getValue().isSaveable()) {
                                                registerChildListener(ref.child(DATABASE_SECTION_PRIVATE).child(entry.getKey()).child(""+state.getMe().getProperties().getNumber()),userPrivateDataListener);
                                            }
                                        }
                                        trackingListener.onAccept(o);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e1) {
                                    try {
                                        setStatus(TRACKING_DISABLED);

                                        String reason = "";
                                        if (o.has(RESPONSE_MESSAGE)) {
                                            reason = o.getString(RESPONSE_MESSAGE);
                                        }
                                        trackingListener.onReject(reason);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } else {
                            setStatus(TRACKING_DISABLED);

                            String reason = "Old version of server";
                            trackingListener.onReject(reason);
                        }
                        break;
                    case RESPONSE_STATUS_ERROR:
                        setStatus(TRACKING_DISABLED);

                        String reason = "";
                        if (o.has(RESPONSE_MESSAGE)) {
                            reason = o.getString(RESPONSE_MESSAGE);
                        }
                        trackingListener.onReject(reason);
                        break;
                    default:
                        trackingListener.onMessage(o);
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(WebSocket websocket, WebSocketException cause) {
            if(TRACKING_DISABLED.equals(getStatus())) return;
//            Log.i("MyTrackingFB","onError:" + websocket.getState() + ":" + cause.getMessage());

            if(websocket.getState() == WebSocketState.CLOSED) {
                if(newTracking) {
                    setStatus(TRACKING_DISABLED);
                    trackingListener.onReject(cause.getLocalizedMessage());
                } else {
                    reconnect();
                }
            }
        }

        @Override
        public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame,
                                   WebSocketFrame clientCloseFrame, boolean closedByServer) {
            Log.i("MyTrackingFB","onDisconnected:websocket:"+websocket+", closeByServer=" + closedByServer+", isNewTracking="+newTracking);

            if (closedByServer) {
            } else if(!closedByServer && serverCloseFrame == null && clientCloseFrame != null) {
                if(newTracking) {
                    trackingListener.onStop();
                } else {
                    trackingListener.onClose();
                    reconnect();
                }
            }
        }

        @Override
        public void onUnexpectedError(WebSocket websocket, WebSocketException cause) {
            Log.i("MyTrackingFB","onUnexpectedError:" + websocket.getState() + ":" + cause.getMessage());
            reconnect();
        }
    };

    public MyTrackingFB() {
        this(WSS_SERVER_HOST, true);
    }

    public MyTrackingFB(String host) {
        this(host, false);
    }

    private MyTrackingFB(String stringUri, final boolean isNewTracking) {
        Log.i("MyTrackingFB","create:" + stringUri);

        try {
            URI uri = new URI(stringUri);
            this.serverUri = new URI("ws://" + uri.getHost() + ":" + WSSFB_PORT + uri.getPath());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        this.newTracking = isNewTracking;
        state = State.getInstance();
        database = FirebaseDatabase.getInstance();
        refs = new HashMap<>();

        try {
            WebSocketFactory factory = new WebSocketFactory().setConnectionTimeout(CONNECTION_TIMEOUT*1000);
            SSLContext context = SSLContext.getDefault();//NaiveSSLContext.getInstance("TLS");
            factory.setSSLContext(context);
            webSocket = factory.createSocket(serverUri.toString());

            Log.i("MyTrackingFB","createWebSocket:" + webSocket + ", uri:" + serverUri.toString());

            webSocket.addListener(webSocketListener);

            webSocket.setPingInterval(INACTIVE_USER_DISMISS_DELAY / 2 * 1000);
            webSocket.setPingPayloadGenerator(new PayloadGenerator() {
                @Override
                public byte[] generate() {
                    return "1".getBytes();
                }
            });
        } catch (NoSuchAlgorithmException | IOException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void start() {
        state.getService().startForeground(1976, state.getNotification());
//        receiver = new NetworkStateChangeReceiver(this);
        if(newTracking) {
            setStatus(TRACKING_CONNECTING);
            trackingListener.onCreating();
        } else {
            setStatus(TRACKING_RECONNECTING);
            trackingListener.onJoining(getToken());
        }
        webSocket.connectAsynchronously();
    }

    private void reconnect() {
        if(TRACKING_DISABLED.equals(getStatus())) return;
        Log.i("MyTrackingFB","reconnect");
        setStatus(TRACKING_RECONNECTING);
        trackingListener.onReconnecting();
        try {
            webSocket = webSocket.recreate();
        } catch (IOException e) {
            e.printStackTrace();
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                new Thread(new ReconnectRunnable()).start();
            }
        },RECONNECTION_DELAY*1000);
    }

    @Override
    public void stop() {
        setStatus(TRACKING_DISABLED);
        Map<String, Object> updates = new HashMap<>();
        updates.put("active", false);
        updates.put("changed", ServerValue.TIMESTAMP);
        ref.child(DATABASE_SECTION_USERS_DATA + "/" + state.getMe().getProperties().getNumber()).updateChildren(updates);

        for(Map.Entry<DatabaseReference,Object> entry: refs.entrySet()) {
            System.out.println("LISTENER:"+entry.getKey().getRef());

            if(entry.getValue() instanceof ValueEventListener) {
                entry.getKey().removeEventListener((ValueEventListener) entry.getValue());
            } else if(entry.getValue() instanceof ChildEventListener) {
                entry.getKey().removeEventListener((ChildEventListener) entry.getValue());
            }
        }

        FirebaseAuth.getInstance().signOut();
        trackingListener.onStop();
        state.getService().stopForeground(true);
    }

    @Override
    public MyTrackingFB put(String key, String value) {
        if (builder == null) builder = new JSONObject();
        try {
            builder.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public MyTrackingFB put(String key, Boolean value) {
        if (builder == null) builder = new JSONObject();
        try {
            builder.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public MyTrackingFB put(String key, Number value) {
        if (builder == null) builder = new JSONObject();
        try {
            builder.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public void send() {
        if (builder == null) builder = new JSONObject();
        send(builder);
        builder = null;
    }

    @Override
    public void send(String text) {
        put(REQUEST, text);
        send();
    }

    @Override
    public void send(JSONObject o) {
        try {
            o.put(REQUEST_TIMESTAMP, new Date().getTime());
            String type = o.getString(REQUEST);
            if(REQUEST_NEW_TOKEN.equals(type) || REQUEST_JOIN_TOKEN.equals(type) || REQUEST_CHECK_USER.equals(type)) {
                switch (webSocket.getState()) {
                    case CREATED:
                        break;
                    case CONNECTING:
                        break;
                    case OPEN:
                        webSocket.sendText(o.toString());
                        break;
                    case CLOSING:
                        break;
                    case CLOSED:
                        reconnect();
                        break;
                }
            } else if (ref != null) {
                if(REQUEST_CHANGE_NAME.equals(type)) {
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put(USER_NAME, o.get(USER_NAME));
                    childUpdates.put("changed", ServerValue.TIMESTAMP);
                    ref.child(DATABASE_SECTION_USERS_DATA).child(""+state.getMe().getProperties().getNumber()).updateChildren(childUpdates);
                    return;
                } else if(REQUEST_WELCOME_MESSAGE.equals(type)) {
                    if(state.getMe().getProperties().getNumber() == 0) {
                        ref.child(DATABASE_SECTION_OPTIONS_WELCOME_MESSAGE).setValue(o.get(REQUEST_WELCOME_MESSAGE));
                    }
                    return;
                }

                EntityHolder holder = state.getAllHolders().get(type);

                if(holder == null) return;

                Map<String, Object> updates = new HashMap<>();

                String path;
                if(holder.isSaveable()) {

                    String key = ref.push().getKey();
                    Map<String,Object> data = new HashMap();
                    Iterator<String> keys = o.keys();
                    while(keys.hasNext()) {
                        String k = keys.next();
                        data.put(k,o.get(k));
                    }
                    data.remove(REQUEST);
                    data.remove(REQUEST_PUSH);
                    data.remove(REQUEST_DELIVERY_CONFIRMATION);

                    if(data.containsKey("to")) {
                        String to = String.valueOf(data.get("to"));
                        data.remove("to");
                        data.put("from", state.getMe().getProperties().getNumber());
                        path = DATABASE_SECTION_PRIVATE + "/" + type + "/" + to;
                    } else {
                        path = DATABASE_SECTION_PUBLIC + "/" + type + "/" + state.getMe().getProperties().getNumber();
                    }

                    updates.put(path + "/" + key, data);
                    updates.put(DATABASE_SECTION_USERS_DATA + "/" + state.getMe().getProperties().getNumber() + "/changed", ServerValue.TIMESTAMP);
                    Task<Void> a = ref.updateChildren(updates);
                    a.addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            System.out.println("SUCCESS:");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            System.out.println("FAIL:"+e.getMessage());
                        }
                    });
                }
            } else {
                System.err.println("ERROR SENDING");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendUpdate() {
        if (builder == null) builder = new JSONObject();
        try {
            if(!builder.has(REQUEST)) {
                builder.put(REQUEST, REQUEST_UPDATE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        send();
    }

    @Override
    public void sendMessage(String key, String value){
        put(key, value);
        sendUpdate();
    }

    @Override
    public void sendMessage(String type, JSONObject json){
        Iterator<String> iter = json.keys();
        while(iter.hasNext()){
            String entry = iter.next();
            try {
                Object value = json.get(entry);
                if(value instanceof String){
                    put(entry, json.getString(entry));
                } else if(value instanceof Double){
                    put(entry, ((Double)json.get(entry)).floatValue());
                } else if(value instanceof Number){
                    put(entry, (Number) json.get(entry));
                } else if(value instanceof Boolean){
                    put(entry, json.getBoolean(entry));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        put(REQUEST, type);
        sendUpdate();
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public void setTrackingListener(TrackingCallback trackingListener) {
        this.trackingListener = trackingListener;
    }

    @Override
    public void postMessage(JSONObject json) {
        trackingListener.onMessage(json);
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String getTrackingUri() {
        return "http://" + serverUri.getHost() + ":" + HTTP_PORT + "/track/" + getToken();
    }

    private DatabaseReference registerChildListener(DatabaseReference ref, ChildEventListener listener) {
        ref.addChildEventListener(listener);
        refs.put(ref,listener);
        return ref;
    }

    private void registerValueListener(DatabaseReference ref, ValueEventListener listener) {
        ref.addValueEventListener(listener);
        refs.put(ref,listener);
    }

    private class ReconnectRunnable implements Runnable {
        @Override
        public void run() {
            if(TRACKING_DISABLED.equals(getStatus())) return;
            try {
                Log.i("MyTrackingFB","reconnectRunnable");
                webSocket.connect();
            } catch (WebSocketException e) {
                Log.e("MyTrackingFB","reconnectRunnable:error:" + e.getMessage());
                reconnect();
            }
        }
    }


}
