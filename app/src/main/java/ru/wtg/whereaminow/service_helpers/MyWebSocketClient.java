package ru.wtg.whereaminow.service_helpers;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.helpers.Utils;

import static org.java_websocket.WebSocket.READYSTATE.CLOSED;
import static org.java_websocket.WebSocket.READYSTATE.OPEN;
import static ru.wtg.whereaminow.State.TRACKING_ACTIVE;
import static ru.wtg.whereaminow.State.TRACKING_DISABLED;
import static ru.wtg.whereaminow.State.TRACKING_ERROR;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_CHECK_USER;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_DEVICE_ID;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_HASH;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_JOIN_TOKEN;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_MANUFACTURER;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_MODEL;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_NEW_TOKEN;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_OS;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_TIMESTAMP;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_TOKEN;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_UPDATE;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_CONTROL;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_ACCEPTED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_CHECK;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_CONNECTED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_DISCONNECTED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_ERROR;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_UPDATED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_TOKEN;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_NAME;

/**
 * Created 10/2/16.
 */
public class MyWebSocketClient {

    private final static int PING_DELAY = 600;

    private URI uri;
    private WebSocketClient webSocketClient;
    private State state;
    private JSONObject builder;
    private MyTracking tracking;
    private String token;
    private boolean wasConnected = false;
    private JSONObject posted;

    MyWebSocketClient(URI uri) throws URISyntaxException {
        this.uri = uri;
        webSocketClient = new MWebSocketClient(uri);
        state = State.getInstance();
    }

    public void setTracking(MyTracking tracking) {
        this.tracking = tracking;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        if(token == null || !token.equals(this.token)){
            wasConnected = false;
        }
        this.token = token;
    }

    public boolean hasToken() {
        return token != null;
    }

    public void removeToken() {
        state.setPreference(RESPONSE_TOKEN, null);
        setToken(null);
    }

    private class MWebSocketClient extends WebSocketClient {
        public MWebSocketClient(URI serverURI) {
            super(serverURI/*, new Draft_10(), null, 15*/);
        }

        @Override
        public void onOpen(ServerHandshake serverHandshake) {
            if(posted != null) {
                webSocketClient.send(posted.toString());
                posted = null;
            }
//            System.out.println("WEBSOCKET:OPENED:" + post);
            /*if (post != null) {
                post = null;
            }*/
        }

        @Override
        public void onMessage(String s) {
//            System.out.println("WEBSOCKET:ONMESSAGE:" + s);
            if(TRACKING_DISABLED.equals(tracking.getStatus())) return;
            JSONObject o;
            try {
                o = new JSONObject(s);
                if (!o.has(RESPONSE_STATUS)) return;
                switch (o.getString(RESPONSE_STATUS)) {
                    case RESPONSE_STATUS_CONNECTED:
                        if(hasToken()){
                            requestJoinToken();
                        } else {
                            requestNewToken();
                        }
                        break;
                    case RESPONSE_STATUS_CHECK:
                        checkUser(o);
                        break;
                    case RESPONSE_STATUS_ACCEPTED:
                        tokenAccepted(o);
                        break;
                    case RESPONSE_STATUS_UPDATED:
                        tracking.fromServer(o);
                        break;
                    case RESPONSE_STATUS_ERROR:
                        tracking.setStatus(TRACKING_ERROR);
                        wasConnected = false;
                        setToken(null);
                        tracking.fromServer(o);
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                System.out.println("Message got: " + s);
            }

        }

        @Override
        public void onClose(int i, String s, boolean b) {
//            System.out.println("WEBSOCKET:CONNECTION_CLOSE:" + s);
            if(TRACKING_DISABLED.equals(tracking.getStatus())) return;
            JSONObject o = new JSONObject();
            try {
                o.put(RESPONSE_STATUS, RESPONSE_STATUS_DISCONNECTED);
                o.put(RESPONSE_MESSAGE, s);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            tracking.fromServer(o);
        }

        @Override
        public void onError(Exception e) {
//            System.out.println("WEBSOCKET:CONNECTION_ERROR:" + e.getMessage());
        }

    }

    private void tokenAccepted(JSONObject o) {
        wasConnected = true;
//        System.out.println("====================================");
        try {
            if (o.has(RESPONSE_TOKEN)) {
                setToken(o.getString(RESPONSE_TOKEN));
                state.setPreference(RESPONSE_TOKEN, getToken());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        System.out.println("PUTTOKEN:"+getToken());
        new Thread(new PingTask(MyWebSocketClient.this)).start();
        tracking.fromServer(o);
        tracking.setStatus(TRACKING_ACTIVE);
    }

    private void requestJoinToken() {
        put(REQUEST, REQUEST_JOIN_TOKEN);
        put(REQUEST_TOKEN, getToken());

        if(!wasConnected) {
            put(REQUEST_DEVICE_ID, State.getInstance().getDeviceId());
            put(REQUEST_MODEL, Build.MODEL);
            put(REQUEST_MANUFACTURER, Build.MANUFACTURER);
            put(REQUEST_OS, "android");
            if(state.getMe().getProperties().getName() != null && state.getMe().getProperties().getName().length()>0){
                put(USER_NAME,state.getMe().getProperties().getName());
            }
        }

        send();
    }

    private void requestNewToken() {
        put(REQUEST, REQUEST_NEW_TOKEN);

        put(REQUEST_DEVICE_ID, State.getInstance().getDeviceId());
        put(REQUEST_MODEL, Build.MODEL);
        put(REQUEST_MANUFACTURER, Build.MANUFACTURER);
        put(REQUEST_OS, "android");
        if(state.getMe().getProperties().getName() != null && state.getMe().getProperties().getName().length()>0){
            put(USER_NAME,state.getMe().getProperties().getName());
        }

        send();
    }

    private void checkUser(JSONObject o) {
        try {
            if (o.has(RESPONSE_CONTROL)) {
                String control = o.getString(RESPONSE_CONTROL);
                String deviceId = State.getInstance().getDeviceId();
                String hash = Utils.getEncryptedHash(control + ":" + deviceId);
                put(REQUEST,REQUEST_CHECK_USER);
                put(REQUEST_HASH,hash);
                send();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public MyWebSocketClient put(String key, String value) {
        if (builder == null) builder = new JSONObject();
        try {
            builder.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    public MyWebSocketClient put(String key, Boolean value) {
        if (builder == null) builder = new JSONObject();
        try {
            builder.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    public MyWebSocketClient put(String key, Number value) {
        if (builder == null) builder = new JSONObject();
        try {
            builder.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    public void sendUpdate(JSONObject o) {
        try {
            o.put(REQUEST, REQUEST_UPDATE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        send(o);
    }

    public void send() {
        if (builder == null) builder = new JSONObject();
        send(builder);
        builder = null;
    }

    public void sendUpdate() {
        if (builder == null) builder = new JSONObject();
        try {
            builder.put(REQUEST, REQUEST_UPDATE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        send(builder);
        builder = null;
    }

    public void send(String text) {
        JSONObject o = new JSONObject();
        try {
            o.put(REQUEST, text);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        send(o);
    }

    public void send(JSONObject o) {
        try {
            o.put(REQUEST_TIMESTAMP, new Date().getTime());
            System.out.println("SEND:"+o);
            switch (webSocketClient.getReadyState()) {
                case OPEN:
                    webSocketClient.send(o.toString());
                    break;
                case CLOSED:
//                    o.put(REQUEST_TIMESTAMP, new Date().getTime());
                    posted = o;
                    webSocketClient = new MWebSocketClient(uri);
                    webSocketClient.connect();
                    break;
                case NOT_YET_CONNECTED:
//                    o.put(REQUEST_TIMESTAMP, new Date().getTime());
                    posted = o;
                    webSocketClient.connect();
                    break;
                case CONNECTING:
                    break;
                case CLOSING:
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            try {
                o.put(REQUEST_TIMESTAMP, new Date().getTime());
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            posted = o;
            webSocketClient = new MWebSocketClient(uri);
            webSocketClient.connect();
        }
    }

    public void stop() {
        webSocketClient.close();
    }

    public void start() {
        webSocketClient = new MWebSocketClient(uri);
        webSocketClient.connect();
    }

    public void reconnect() {
        stop();
        System.out.println("RECONNECT:STOP");
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
        System.out.println("RECONNECT:START");
                start();
            }
        },5000);

    }

    private class PingTask implements Runnable {
        private final MyWebSocketClient client;

        PingTask(MyWebSocketClient client) {
            this.client = client;
        }
        @Override
        public void run() {
            while(true) {
                try {
                    Thread.sleep(PING_DELAY * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (client.webSocketClient.getReadyState() == CLOSED) {
                    return;
                }

                if (client.webSocketClient.getReadyState() == OPEN) {
                    client.sendUpdate();
                }
            }
        }
    }

}
