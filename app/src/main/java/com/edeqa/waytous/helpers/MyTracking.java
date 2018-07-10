package com.edeqa.waytous.helpers;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Consumer;
import com.edeqa.waytous.State;
import com.edeqa.waytous.interfaces.Tracking;
import com.edeqa.waytous.interfaces.TrackingCallback;
import com.neovisionaries.ws.client.PayloadGenerator;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketState;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import static com.edeqa.waytous.Constants.LIFETIME_INACTIVE_USER;
import static com.edeqa.waytous.Constants.REQUEST;
import static com.edeqa.waytous.Constants.REQUEST_CHECK_USER;
import static com.edeqa.waytous.Constants.REQUEST_HASH;
import static com.edeqa.waytous.Constants.REQUEST_JOIN_GROUP;
import static com.edeqa.waytous.Constants.REQUEST_LEAVE;
import static com.edeqa.waytous.Constants.REQUEST_MANUFACTURER;
import static com.edeqa.waytous.Constants.REQUEST_MODEL;
import static com.edeqa.waytous.Constants.REQUEST_NEW_GROUP;
import static com.edeqa.waytous.Constants.REQUEST_OS;
import static com.edeqa.waytous.Constants.REQUEST_TIMESTAMP;
import static com.edeqa.waytous.Constants.REQUEST_TOKEN;
import static com.edeqa.waytous.Constants.REQUEST_UID;
import static com.edeqa.waytous.Constants.REQUEST_UPDATE;
import static com.edeqa.waytous.Constants.RESPONSE_CONTROL;
import static com.edeqa.waytous.Constants.RESPONSE_INITIAL;
import static com.edeqa.waytous.Constants.RESPONSE_MESSAGE;
import static com.edeqa.waytous.Constants.RESPONSE_NUMBER;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS_ACCEPTED;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS_CHECK;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS_ERROR;
import static com.edeqa.waytous.Constants.RESPONSE_TOKEN;
import static com.edeqa.waytous.Constants.OPTIONS;
import static com.edeqa.waytous.Constants.USER_NAME;
import static com.edeqa.waytous.helpers.Events.MAKE_ACTIVE;
import static com.edeqa.waytous.helpers.Events.TRACKING_ACTIVE;
import static com.edeqa.waytous.helpers.Events.TRACKING_CONNECTING;
import static com.edeqa.waytous.helpers.Events.TRACKING_DISABLED;
import static com.edeqa.waytous.helpers.Events.TRACKING_RECONNECTING;

/**
 * Created 10/8/16.
 */

@SuppressWarnings("ALL")
public class MyTracking implements Tracking {

    private final static int CONNECTION_TIMEOUT = 5;
    private final static int RECONNECTION_DELAY = 5;

//    private NetworkStateChangeReceiver receiver;
    private State state;
    private URI serverUri;
    private String status = TRACKING_DISABLED;
    private TrackingCallback trackingListener;
    private WebSocket webSocket;
    private JSONObject builder;
//    private JSONObject posted;
    private String token;
    private boolean newTracking;
    private Handler handler = new Handler(Looper.myLooper());

    @SuppressWarnings("FieldCanBeLocal")
    private WebSocketAdapter webSocketListener = new WebSocketAdapter() {
        @Override
        public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
            super.onConnected(websocket, headers);
            if(TRACKING_DISABLED.equals(getStatus())) return;
            if(newTracking) {
                put(REQUEST, REQUEST_NEW_GROUP);
            } else {
                put(REQUEST, REQUEST_JOIN_GROUP);

                String path = MyTracking.this.serverUri.getPath();
                if(path != null) {
                    String[] parts = path.split("/");
                    if(parts.length > 2) {
                        setToken(parts[2]);
                    }
                }
                put(REQUEST_TOKEN, getToken());
                put(REQUEST_UID, State.getInstance().fetchUid());
            }
            if(!TRACKING_RECONNECTING.equals(getStatus())) {
                put(REQUEST_UID, State.getInstance().fetchUid());
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
//            Log.i("MyTracking","onTextMessage");

            try {
                JSONObject o = new JSONObject(message);
                if (!o.has(RESPONSE_STATUS)) return;
                switch (o.getString(RESPONSE_STATUS)) {
                    case RESPONSE_STATUS_CHECK:
                        if (o.has(RESPONSE_CONTROL)) {
                            String control = o.getString(RESPONSE_CONTROL);
                            String uid = State.getInstance().fetchUid();
                            String hash = Misc.getEncryptedHash(control + ":" + uid);
                            put(REQUEST,REQUEST_CHECK_USER);
                            put(REQUEST_HASH,hash);
                            send();
                        }
                        break;
                    case RESPONSE_STATUS_ACCEPTED:
                        newTracking = false;
                        setStatus(TRACKING_ACTIVE);
                        if (o.has(RESPONSE_TOKEN)) {
                            setToken(o.getString(RESPONSE_TOKEN));
                        }
                        if (o.has(RESPONSE_NUMBER)) {
                            State.getInstance().getUsers().setMyNumber(o.getInt(RESPONSE_NUMBER));
                        }
                        if (o.has(RESPONSE_INITIAL)) {
                            JSONArray initialUsers = o.getJSONArray(RESPONSE_INITIAL);
                            for (int i = 0; i < initialUsers.length(); i++) {
                                JSONObject u = initialUsers.getJSONObject(i);
                                MyUser user = State.getInstance().getUsers().addUser(u);
                                user.setUser(true);
                                user.fire(MAKE_ACTIVE);
                            }
                        }

                        trackingListener.onAccept(o);
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
            Utils.log(this, "onError:", "webSocket=" + websocket.getState(), "cause=" + cause.getMessage());

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
       //     if(TRACKING_DISABLED.equals(getStatus())) return;
            Utils.log(this,"onDisconnected:", "serverCloseFrame="+serverCloseFrame.toString(),"clientCloseFrame="+clientCloseFrame.toString(), "loseByServer=" + closedByServer, "isNewTracking="+newTracking);

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
            Utils.log(this, "onUnexpectedError:", "webSocket=" + websocket.getState(), "cause=" + cause.getMessage());
            reconnect();
        }

/*
        @Override
        public void onPingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
            super.onPingFrame(websocket, frame);
            System.out.println("SEND PING FRAME");
        }

        @Override
        public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
            super.onPongFrame(websocket, frame);
            if(TRACKING_DISABLED.equals(getStatus())) return;
            websocket.sendPing("Are you there?");
        }
*/
    };

    public MyTracking() {
        this("https://" + OPTIONS.getServerHost(), true);
    }

    public MyTracking(String host) {
        this(host, false);
    }

    private MyTracking(String stringUri, final boolean isNewTracking) {
        Log.i("MyTracking","create:" + stringUri);

        try {
            URI uri = new URI(stringUri);
            this.serverUri = new URI("ws://" + uri.getHost() + ":" + OPTIONS.getWssPortDedicated() + uri.getPath());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        this.newTracking = isNewTracking;
        state = State.getInstance();

        try {
            WebSocketFactory factory = new WebSocketFactory().setConnectionTimeout(CONNECTION_TIMEOUT*1000);
            SSLContext context = SSLContext.getDefault();//NaiveSSLContext.getInstance("TLS");
            factory.setSSLContext(context);
            webSocket = factory.createSocket(serverUri.toString());

            Log.i("MyTracking","createWebSocket:" + webSocket + ", uri:" + serverUri.toString());

            webSocket.addListener(webSocketListener);

            webSocket.setPingInterval(LIFETIME_INACTIVE_USER / 2 * 1000);
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
        Utils.log(this, "start");
        webSocket.connectAsynchronously();
    }

    private void reconnect() {
        if(TRACKING_DISABLED.equals(getStatus())) return;
        Utils.log(this,"reconnect");
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

    public void stop() {
        setStatus(TRACKING_DISABLED);
        /*try {
            receiver.unregister();
        } catch(Exception e){
            e.printStackTrace();
        }*/
        trackingListener.onStop();
        send(REQUEST_LEAVE);
        webSocket.sendClose();
        state.getService().stopForeground(true);
    }

    public MyTracking put(String key, String value) {
        if (builder == null) builder = new JSONObject();
        try {
            builder.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    public MyTracking put(String key, Boolean value) {
        if (builder == null) builder = new JSONObject();
        try {
            builder.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    public MyTracking put(String key, Number value) {
        if (builder == null) builder = new JSONObject();
        try {
            builder.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    public void send() {
        if (builder == null) builder = new JSONObject();
        send(builder);
        builder = null;
    }

    public void send(String text) {
        put(REQUEST, text);
        send();
    }

    public void send(JSONObject o) {
        try {
            o.put(REQUEST_TIMESTAMP, new Date().getTime());
//            System.out.println("WEBSOCKETSEND:"+o);
            switch (webSocket.getState()) {
                case CREATED:
//                    System.out.println("WEBSOCKETSTATE:CONNECT");
                    //connection.connectAsynchronously();
                    break;
                case CONNECTING:
//                    System.out.println("WEBSOCKETSTATE:CONNECTING");
                    break;
                case OPEN:
//                    System.out.println("WEBSOCKETSTATE:OPEN");
                    webSocket.sendText(o.toString());
                    break;
                case CLOSING:
//                    System.out.println("WEBSOCKETSTATE:CLOSING");
//                    reconnect();
                    break;
                case CLOSED:
//                    System.out.println("WEBSOCKETSTATE:CLOSED");
//                    posted = o;
                    reconnect();
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
//            try {
//                o.put(REQUEST_TIMESTAMP, new Date().getTime());
//            } catch (JSONException e1) {
//                e1.printStackTrace();
//            }
//            posted = o;

        }
    }

 /*   public void sendUpdate(JSONObject o) {
        try {
            o.put(REQUEST, REQUEST_UPDATE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        send(o);
    }
*/
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

    public void sendMessage(String key,String value){
        put(key, value);
        sendUpdate();
    }

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

    public void postMessage(JSONObject json) {
        trackingListener.onMessage(json);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTrackingUri() {
        return "http://" + serverUri.getHost() + (OPTIONS.getHttpPortMasked() == 80 ? "" : ":" + OPTIONS.getHttpPortMasked()) + "/track/" + getToken();
    }

    @Override
    public void setOnSendSuccess(Runnable onSendSuccess) {

    }

    @Override
    public void setOnSendFailure(Consumer<Throwable> onSendFailure) {

    }

    private class ReconnectRunnable implements Runnable {
        @Override
        public void run() {
            if(TRACKING_DISABLED.equals(getStatus())) return;
            try {
                Log.i("MyTracking","reconnectRunnable");
                webSocket.connect();
            } catch (WebSocketException e) {
                Log.e("MyTracking","reconnectRunnable:error:" + e.getMessage());
                reconnect();
            }
        }
    }
}
