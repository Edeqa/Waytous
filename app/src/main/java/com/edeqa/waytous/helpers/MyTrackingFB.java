package com.edeqa.waytous.helpers;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.helpers.interfaces.Runnable2;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytous.R;
import com.edeqa.waytous.State;
import com.edeqa.waytous.abstracts.AbstractPropertyHolder;
import com.edeqa.waytous.interfaces.EntityHolder;
import com.edeqa.waytous.interfaces.Tracking;
import com.edeqa.waytous.interfaces.TrackingCallback;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import static com.edeqa.waytous.Constants.LIFETIME_INACTIVE_USER;
import static com.edeqa.waytous.Constants.OPTIONS;
import static com.edeqa.waytous.Constants.REQUEST;
import static com.edeqa.waytous.Constants.REQUEST_CHANGE_NAME;
import static com.edeqa.waytous.Constants.REQUEST_CHECK_USER;
import static com.edeqa.waytous.Constants.REQUEST_DELIVERY_CONFIRMATION;
import static com.edeqa.waytous.Constants.REQUEST_HASH;
import static com.edeqa.waytous.Constants.REQUEST_JOIN_GROUP;
import static com.edeqa.waytous.Constants.REQUEST_KEY;
import static com.edeqa.waytous.Constants.REQUEST_MANUFACTURER;
import static com.edeqa.waytous.Constants.REQUEST_MODEL;
import static com.edeqa.waytous.Constants.REQUEST_NEW_GROUP;
import static com.edeqa.waytous.Constants.REQUEST_OS;
import static com.edeqa.waytous.Constants.REQUEST_PUSH;
import static com.edeqa.waytous.Constants.REQUEST_SIGN_PROVIDER;
import static com.edeqa.waytous.Constants.REQUEST_TIMESTAMP;
import static com.edeqa.waytous.Constants.REQUEST_TOKEN;
import static com.edeqa.waytous.Constants.REQUEST_UID;
import static com.edeqa.waytous.Constants.REQUEST_UPDATE;
import static com.edeqa.waytous.Constants.REQUEST_WELCOME_MESSAGE;
import static com.edeqa.waytous.Constants.RESPONSE_CONTROL;
import static com.edeqa.waytous.Constants.RESPONSE_INITIAL;
import static com.edeqa.waytous.Constants.RESPONSE_MESSAGE;
import static com.edeqa.waytous.Constants.RESPONSE_NUMBER;
import static com.edeqa.waytous.Constants.RESPONSE_PRIVATE;
import static com.edeqa.waytous.Constants.RESPONSE_SIGN;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS_ACCEPTED;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS_CHECK;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS_ERROR;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS_UPDATED;
import static com.edeqa.waytous.Constants.RESPONSE_TOKEN;
import static com.edeqa.waytous.Constants.USER_DISMISSED;
import static com.edeqa.waytous.Constants.USER_JOINED;
import static com.edeqa.waytous.Constants.USER_NAME;
import static com.edeqa.waytous.helpers.Events.CHANGE_NAME;
import static com.edeqa.waytous.helpers.Events.TRACKING_ACTIVE;
import static com.edeqa.waytous.helpers.Events.TRACKING_CONNECTING;
import static com.edeqa.waytous.helpers.Events.TRACKING_DISABLED;
import static com.edeqa.waytous.helpers.Events.TRACKING_RECONNECTING;
import static com.edeqa.waytous.holders.property.MessagesHolder.PRIVATE_MESSAGE;
import static org.apache.http.conn.ssl.SSLSocketFactory.TLS;

/**
 * Created 1/29/17.
 */

public class MyTrackingFB implements Tracking {

    @SuppressWarnings("unused")
    private static final String LOG = "MyTrackingFB"; //NON-NLS

    @SuppressWarnings("unused")
    private final static int CONNECTION_TIMEOUT = 5;
    private final static int RECONNECTION_DELAY = 5;

    private final Map<DatabaseReference, Object> refs;

    private final State state;
    private URI serverUri;
    private TrackingCallback trackingListener;
    private WebSocket webSocket;
    private JSONObject builder;
    private final FirebaseDatabase database;
    private DatabaseReference ref;

    private String status = TRACKING_DISABLED;
    private String token;

    private boolean newTracking;
    private ScheduledFuture<?> scheduled;
    private Runnable onSendSuccess;
    private Runnable1<Throwable> onSendFailure;


    public MyTrackingFB() {
        this("https://" + OPTIONS.getServerHost(), true); //NON-NLS
    }

    public MyTrackingFB(String host) {
        this(host, false);
    }

    private MyTrackingFB(String stringUri, final boolean isNewTracking) {
        Utils.log("MyTrackingFB","create:" + stringUri); //NON-NLS

        try {
            URI uri = new URI(stringUri);
            this.serverUri = new URI("ws://" + uri.getHost() + ":" + OPTIONS.getWsPortFirebase() + uri.getPath()); //NON-NLS
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        this.newTracking = isNewTracking;
        state = State.getInstance();
        database = FirebaseDatabase.getInstance();
        refs = new HashMap<>();

        try {
//            SSLContext context = NaiveSSLContext.getInstance("TLS");
//            SSLContext context = SSLContext.getInstance("TLS");
//            WebSocketFactory factory = new WebSocketFactory().setConnectionTimeout(CONNECTION_TIMEOUT*1000);
//            WebSocketFactory factory = new DefaultSSLWebSocketClientFactory( context );
//                    new WebSocketFactory().setConnectionTimeout(CONNECTION_TIMEOUT*1000);

//            SSLContext sslContext = null;
//            sslContext = SSLContext.getInstance("TLS");
//            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);


//            factory.setSSLContext(context);
//            connection = factory.createSocket(serverUri.toString());
//            connection.setWebSocketFactory( new DefaultSSLWebSocketClientFactory( sslContext ) );



//            String STORETYPE = "JKS";
//            String KEYSTORE = "../../keystore.jks";
//            String STOREPASSWORD = WaytousServer.getSensitive().getSSLCertificatePassword();
//            String KEYPASSWORD = WaytousServer.getSensitive().getSSLCertificatePassword();
//
//            KeyStore ks = KeyStore.getInstance(STORETYPE);
//            File kf = new File(KEYSTORE);
//            ks.load(new FileInputStream(kf), STOREPASSWORD.toCharArray());

//    KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
//    kmf.init(ks, KEYPASSWORD.toCharArray());
//    TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
//    tmf.init(ks);



//            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
//            kmf.init(ks, KEYPASSWORD.toCharArray());
//            TrustManagerFactory tmf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
//            tmf.init(ks);
//
//            SSLContext context = SSLContext.getInstance("TLS");
////    sslContext.init(null, null, null);
//            context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);


            WebSocketFactory factory = new WebSocketFactory();
            SSLContext context = NaiveSSLContext.getInstance(TLS);
            context.init(null,null,null);
            factory.setSSLContext(context);
            webSocket = factory.createSocket(serverUri.toString());

            Utils.log("MyTrackingFB","createWebSocket:" + webSocket + ", uri:" + serverUri.toString()); //NON-NLS
            webSocket.addListener(webSocketListener);

            webSocket.setPingInterval(LIFETIME_INACTIVE_USER / 2 * 1000);
            webSocket.setPingPayloadGenerator(new PayloadGenerator() {
                @Override
                public byte[] generate() {
                    return "1".getBytes();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
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
        Utils.log("MyTrackingFB","reconnect"); //NON-NLS
        setStatus(TRACKING_RECONNECTING);
        trackingListener.onReconnecting();
        try {
            webSocket = webSocket.recreate();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Handler handler = new Handler(Looper.myLooper());
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

        if(scheduled != null) {
            scheduled.cancel(true);
            scheduled = null;
        }

        if(ref != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put(Firebase.ACTIVE, false);
            updates.put(Firebase.CHANGED, ServerValue.TIMESTAMP);
            ref.child(Firebase.USERS).child(Firebase.PUBLIC).child(""+state.getMe().getProperties().getNumber()).updateChildren(updates);

/*
            Iterator<Map.Entry<DatabaseReference, Object>> iter = refs.entrySet().iterator();
            while(iter.hasNext()) {
                Map.Entry<DatabaseReference, Object> entry = iter.next();
                if (entry.getValue() instanceof ValueEventListener) {
                    entry.getKey().removeEventListener((ValueEventListener) entry.getValue());
                } else if (entry.getValue() instanceof ChildEventListener) {
                    entry.getKey().removeEventListener((ChildEventListener) entry.getValue());
                }
            }
*/

            for (Map.Entry<DatabaseReference, Object> entry : refs.entrySet()) {
                if (entry.getValue() instanceof ValueEventListener) {
                    entry.getKey().removeEventListener((ValueEventListener) entry.getValue());
                } else if (entry.getValue() instanceof ChildEventListener) {
                    entry.getKey().removeEventListener((ChildEventListener) entry.getValue());
                }
            }
// remove public data of this user
            for (Map.Entry<String, AbstractPropertyHolder> entry : state.getAllHolders().entrySet()) {
                if (entry.getValue() != null && entry.getValue().isSaveable() && entry.getValue().isEraseable()) {
                    ref.child(Firebase.PUBLIC).child(entry.getKey()).child("" + state.getMe().getProperties().getNumber()).removeValue();
                }
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
            if(REQUEST_NEW_GROUP.equals(type) || REQUEST_JOIN_GROUP.equals(type) || REQUEST_CHECK_USER.equals(type)) {
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
                    childUpdates.put(Firebase.NAME, o.get(USER_NAME));
                    childUpdates.put(Firebase.CHANGED, ServerValue.TIMESTAMP);
                    ref.child(Firebase.USERS).child(Firebase.PUBLIC).child(""+state.getMe().getProperties().getNumber()).updateChildren(childUpdates);
                    return;
                } else if(REQUEST_WELCOME_MESSAGE.equals(type)) {
                    if(state.getMe().getProperties().getNumber() == 0) {
                        ref.child(Firebase.WELCOME_MESSAGE).setValue(o.get(REQUEST_WELCOME_MESSAGE));
                    }
                    return;
                }

                EntityHolder holder = state.getAllHolders().get(type);

                if(holder == null || !holder.isSaveable()) return;

                Map<String, Object> updates = new HashMap<>();

                String key = ref.push().getKey();
                Map<String,Object> data = new HashMap<>();
                Iterator<String> keys = o.keys();
                while(keys.hasNext()) {
                    String k = keys.next();
                    data.put(k,o.get(k));
                }
                data.remove(REQUEST);
                data.remove(REQUEST_PUSH);
                data.remove(REQUEST_DELIVERY_CONFIRMATION);

                String path;
                if(data.containsKey(RESPONSE_PRIVATE)) {
                    String to = String.valueOf(data.get(RESPONSE_PRIVATE));
                    data.remove(RESPONSE_PRIVATE);
                    data.put("from", state.getMe().getProperties().getNumber()); //NON-NLS
                    path = Firebase.PRIVATE + "/" + type + "/" + to;
                } else {
                    path = Firebase.PUBLIC + "/" + type + "/" + state.getMe().getProperties().getNumber();
                }
                updates.put(path + "/" + key, data);
                updates.put(Firebase.USERS + "/" + Firebase.PUBLIC + "/" + state.getMe().getProperties().getNumber() + "/" + Firebase.CHANGED, ServerValue.TIMESTAMP);
                Task<Void> a = ref.updateChildren(updates);
                a.addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if(getOnSendSuccess() != null) getOnSendSuccess().run();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Utils.err(MyTrackingFB.this, "send:", e.getMessage(),e); //NON-NLS
                        if(getOnSendFailure() != null) getOnSendFailure().call(e);
                    }
                });
            } else {
                Utils.log(MyTrackingFB.this, "send:", "Error sending"); //NON-NLS
                if(getOnSendFailure() != null) getOnSendFailure().call(new Throwable("Error sending: " + o)); //NON-NLS
            }
        } catch (JSONException e) {
            //noinspection ConstantConditions
            Utils.err("Invalid JSON: " + (o != null ? o.toString() : "null")); //NON-NLS
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
    public TrackingCallback getTrackingListener() {
        return trackingListener;
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
        return "http://" + serverUri.getHost() + (OPTIONS.getHttpPortMasked() == 80 ? "" : ":" + OPTIONS.getHttpPortMasked()) + "/track/" + getToken(); //NON-NLS
    }

    @SuppressWarnings("UnusedReturnValue")
    private DatabaseReference registerChildListener(DatabaseReference ref, ChildEventListener listener, int limit) {
        if(limit >=0) {
            if(limit > 1000) limit = 1000;
            ref.limitToLast(limit).addChildEventListener(listener);
        } else {
            ref.addChildEventListener(listener);
        }
        refs.put(ref,listener);
        return ref;
    }

    private void registerValueListener(DatabaseReference ref, ValueEventListener listener) {
        ref.addValueEventListener(listener);
        refs.put(ref,listener);
    }

    @Override
    public void setOnSendSuccess(Runnable onSendSuccess) {
        this.onSendSuccess = onSendSuccess;
    }

    private Runnable getOnSendSuccess() {
        return onSendSuccess;
    }

    @Override
    public void setOnSendFailure(Runnable1<Throwable> onSendFailure) {
        this.onSendFailure = onSendFailure;
    }

    private Runnable1<Throwable> getOnSendFailure() {
        return onSendFailure;
    }


    private class ReconnectRunnable implements Runnable {
        @Override
        public void run() {
            if(TRACKING_DISABLED.equals(getStatus())) return;
            try {
                Utils.log("MyTrackingFB","reconnectRunnable"); //NON-NLS
                webSocket.connect();
            } catch (WebSocketException e) {
                Utils.err("MyTrackingFB","reconnectRunnable:error:" + e.getMessage()); //NON-NLS
                reconnect();
            }
        }
    }

    @SuppressWarnings("FieldCanBeLocal")
    private final WebSocketAdapter webSocketListener = new WebSocketAdapter() {

        @Override
        public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
            super.onConnected(websocket, headers);

//            System.out.println("ONCONNECTED:"+getStatus()+":"+websocket+":"+headers);
            if(TRACKING_DISABLED.equals(getStatus())) return;
            Utils.log("MyTrackingFB","onConnected"); //NON-NLS
            if(newTracking) {
                put(REQUEST, REQUEST_NEW_GROUP);
            } else {
                put(REQUEST, REQUEST_JOIN_GROUP);

                String path = MyTrackingFB.this.serverUri.getPath();
                if(path != null) {
                    String[] parts = path.split("/");
                    if(parts.length > 2) {
                        setToken(parts[2]);
                    }
                }
                put(REQUEST_TOKEN, getToken());
                String uid = State.getInstance().fetchUid();
                if(uid.length() > 40) uid = Misc.getEncryptedHash(uid);
                put(REQUEST_UID, uid);
            }
            if(!TRACKING_RECONNECTING.equals(getStatus())) {
                String uid = State.getInstance().fetchUid();
                if(uid.length() > 40) uid = Misc.getEncryptedHash(uid);
                put(REQUEST_UID, uid);
            }
            put(REQUEST_SIGN_PROVIDER, state.fetchSignProvider());
            put(REQUEST_MODEL, Build.MODEL);
            put(REQUEST_MANUFACTURER, Build.MANUFACTURER);
            put(REQUEST_OS, "android"); //NON-NLS

            if(state.getMe().getProperties().getName() != null && state.getMe().getProperties().getName().length()>0){
                put(USER_NAME,state.getMe().getProperties().getName());
            }
            send();
        }

        @Override
        public void onTextMessage(WebSocket websocket, String message) {
            Utils.log(MyTrackingFB.this,"onTextMessage:", "Message="+message); //NON-NLS
            if(TRACKING_DISABLED.equals(getStatus())) return;
            try {
                final JSONObject o = new JSONObject(message);
                if (!o.has(RESPONSE_STATUS)) return;
                switch (o.getString(RESPONSE_STATUS)) {
                    case RESPONSE_STATUS_CHECK:
                        if (o.has(RESPONSE_CONTROL)) {
                            String control = o.getString(RESPONSE_CONTROL);
                            String uid = State.getInstance().fetchUid();
                            if(uid.length() > 40) uid = Misc.getEncryptedHash(uid);
                            String hash = Misc.getEncryptedHash(control + ":" + uid);
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
                                        if(!getStatus().equals(TRACKING_DISABLED)) {
                                            setStatus(TRACKING_ACTIVE);
                                            if (o.has(RESPONSE_TOKEN)) {
                                                setToken(o.getString(RESPONSE_TOKEN));
                                            }
                                            if (o.has(RESPONSE_NUMBER)) {
                                                state.getUsers().setMyNumber(o.getInt(RESPONSE_NUMBER));
                                            }
                                            o.put(RESPONSE_INITIAL, true);

                                            Utils.log(MyTrackingFB.this, "onTextMessage:", "Snapshot=" + authResult.getUser().getUid()); //NON-NLS

                                            ref = database.getReference().child(Firebase.SECTION_GROUPS).child(getToken());


                                            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
                                            scheduled = executor.scheduleAtFixedRate(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (State.getInstance().tracking_active()) {
                                                        Map<String, Object> updates = new HashMap<>();
                                                        updates.put(Firebase.ACTIVE, true);
                                                        updates.put(Firebase.CHANGED, ServerValue.TIMESTAMP);
                                                        ref.child(Firebase.USERS).child(Firebase.PUBLIC)
                                                                .child("" + state.getMe().getProperties().getNumber())
                                                                .updateChildren(updates);
                                                    }
                                                }
                                            }, 0, 1, TimeUnit.MINUTES);

                                            registerValueListener(ref.child(Firebase.OPTIONS).child(Firebase.CREATED), groupListener);

                                            registerValueListener(ref.child(Firebase.USERS).child(Firebase.PUBLIC).child("" + state.getUsers().getMyNumber()).child(Firebase.ACTIVE), userActiveListener);

                                            registerChildListener(ref.child(Firebase.USERS).child(Firebase.PUBLIC), usersDataListener, -1);

                                            for (Map.Entry<String, AbstractPropertyHolder> entry : state.getAllHolders().entrySet()) {
                                                if(entry.getValue().isSaveable()) {
                                                    registerChildListener(ref.child(Firebase.PRIVATE).child(entry.getKey()).child("" + state.getMe().getProperties().getNumber()), userPrivateDataListener, -1);
                                                }
                                            }

                                            trackingListener.onAccept(o);
                                        } else {
                                            stop();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e1) {
                                    e1.printStackTrace();
                                    try {
                                        setStatus(TRACKING_DISABLED);
                                        String reason = State.getInstance().getString(R.string.cannot_join_server_responds_s, e1.getMessage());
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

                            String reason = state.getString(R.string.cannot_join_server_is_too_old);
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
//            Utils.log("MyTrackingFB","onError:" + websocket.getState() + ":" + cause.getMessage());

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
            Utils.log("MyTrackingFB","onDisconnected:websocket:"+websocket+", closeByServer=" + closedByServer+", isNewTracking="+newTracking); //NON-NLS

            if(!closedByServer && serverCloseFrame == null && clientCloseFrame != null) {
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
            Utils.log("MyTrackingFB","onUnexpectedError:" + websocket.getState() + ":" + cause.getMessage()); //NON-NLS
            reconnect();
        }
    };

    private final ValueEventListener groupListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            try {
                switch (databaseError.getCode()) {
                    case DatabaseError.PERMISSION_DENIED:
                        setStatus(TRACKING_DISABLED);
                        String reason = state.getString(R.string.group_has_been_removed);
                        trackingListener.onReject(reason);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private final ChildEventListener usersDataListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//            System.out.println("usersDataListenerADDED:"+dataSnapshot.getValue().getClass()+":"+dataSnapshot.getKey()+":"+dataSnapshot.getValue()+":"+s);

//            System.out.println(state.getMe().getProperties().getNumber()+":"+dataSnapshot.getKey()+":"+(state.getMe().getProperties().getNumber()+"").equals(dataSnapshot.getKey()));

            if(!(state.getMe().getProperties().getNumber()+"").equals(dataSnapshot.getKey())) {
                try {
                    //noinspection unchecked
                    JSONObject o = new JSONObject((Map<String, String>) dataSnapshot.getValue());
                    o.put(RESPONSE_NUMBER, Integer.parseInt(dataSnapshot.getKey()));
                    o.put(RESPONSE_INITIAL, true);
                    MyUser user = State.getInstance().getUsers().addUser(o);
                    user.setUser(true);

                    registerValueListener(ref.child(Firebase.USERS).child(Firebase.PUBLIC).child(""+user.getProperties().getNumber()).child(Firebase.NAME),usersDataNameListener);
                    registerValueListener(ref.child(Firebase.USERS).child(Firebase.PUBLIC).child(""+user.getProperties().getNumber()).child(Firebase.ACTIVE),usersDataActiveListener);
                    registerValueListener(ref.child(Firebase.USERS).child(Firebase.PUBLIC).child(""+user.getProperties().getNumber()).child(Firebase.CHANGED), usersDataChangedListener);

//                    usersDataNameListener.onDataChange(dataSnapshot.child(Constants.DATABASE.NAME));
//                    usersDataActiveListener.onDataChange(dataSnapshot.child("active"));

                    for(Map.Entry<String,AbstractPropertyHolder> entry: state.getAllHolders().entrySet()) {
                        if(entry.getValue().isSaveable()) {
                            registerChildListener(ref.child(Firebase.PUBLIC).child(entry.getKey()).child(""+user.getProperties().getNumber()), userPublicDataListener,1);
                        }
                    }
//                    trackingListener.onAccept(o);
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

    private final ChildEventListener userPublicDataListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//            System.out.println("userDependentListenerADDED:"+dataSnapshot.getKey()+":"+dataSnapshot.getRef().getParent().getParent().getKey()+":"+dataSnapshot.getValue()+":"+s);
            try {
                //noinspection unchecked
                JSONObject o = new JSONObject((Map<String, String>) dataSnapshot.getValue());
                o.put(RESPONSE_NUMBER, Integer.parseInt(dataSnapshot.getRef().getParent().getKey()));
                o.put(RESPONSE_STATUS, dataSnapshot.getRef().getParent().getParent().getKey());
                o.put(REQUEST_KEY, dataSnapshot.getKey());

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

    private final ChildEventListener userPrivateDataListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//            System.out.println("userPrivateAdded:"+dataSnapshot.getKey()+":"+dataSnapshot.getRef().getParent().getParent().getKey()+":"+dataSnapshot.getValue()+":"+s);
            try {
                //noinspection unchecked
                JSONObject o = new JSONObject((Map<String, String>) dataSnapshot.getValue());

                int from = Integer.parseInt(o.getString("from")); //NON-NLS
                o.remove("from"); //NON-NLS
                o.put(RESPONSE_NUMBER, from);
                o.put(RESPONSE_STATUS, dataSnapshot.getRef().getParent().getParent().getKey());
                o.put(REQUEST_KEY, dataSnapshot.getKey());
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

    private final ValueEventListener usersDataNameListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
//            Utils.log(MyTrackingFB.this, "usersDataNameListenerChanged:", "dataSnapShot="+dataSnapshot); //NON-NLS
            try {
                int number = Integer.parseInt(dataSnapshot.getRef().getParent().getKey());
                final String name = String.valueOf(dataSnapshot.getValue());
                state.getUsers().forUser(number, new Runnable2<Integer, MyUser>() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        if (!name.equals("" + myUser.getProperties().getName())) {
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

    private final ValueEventListener usersDataActiveListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
//            System.out.println("usersDataActiveListenerChanged:"+dataSnapshot.getRef().getParent().getKey()+":"+dataSnapshot.getValue());

            try {
                int number = Integer.parseInt(dataSnapshot.getRef().getParent().getKey());
                final boolean active = Boolean.parseBoolean(String.valueOf(dataSnapshot.getValue()));
                state.getUsers().forUser(number, new Runnable2<Integer, MyUser>() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        if (myUser.getProperties().isActive() != active) {
                            try {
                                JSONObject o = new JSONObject();
                                o.put(RESPONSE_STATUS, RESPONSE_STATUS_UPDATED);
                                o.put(RESPONSE_NUMBER, number);
                                o.put(active ? USER_JOINED : USER_DISMISSED, number);
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

    private final ValueEventListener usersDataChangedListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Utils.log(this, "usersDataChangedListener:"+dataSnapshot.getRef().getParent().getKey()+":"+dataSnapshot.getValue()); //NON-NLS
            try {
                int number = Integer.parseInt(dataSnapshot.getRef().getParent().getKey());
                if(dataSnapshot.getValue() != null) {
                    final long changed = (long) dataSnapshot.getValue();
                    state.getUsers().forUser(number, new Runnable2<Integer, MyUser>() {
                        @Override
                        public void call(Integer number, MyUser myUser) {
                            try {
                                JSONObject o = new JSONObject();
                                o.put(RESPONSE_STATUS, RESPONSE_STATUS_UPDATED);
                                o.put(RESPONSE_NUMBER, number);
                                o.put(REQUEST_TIMESTAMP, changed);
                                trackingListener.onMessage(o);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

    private final ValueEventListener userActiveListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
//            System.out.println("usersDataActiveListenerChanged:"+dataSnapshot.getRef().getParent().getKey()+":"+dataSnapshot.getValue());
            if(dataSnapshot.getValue() == null) {
                stop();
            } else if(!((Boolean) dataSnapshot.getValue())) {
                switch (webSocket.getState()) {
                    case CLOSED:
                        reconnect();
                        break;
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

}
