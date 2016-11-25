package ru.wtg.whereaminow.service_helpers;

import android.os.Build;
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

import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_CHECK_USER;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_DEVICE_ID;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_HASH;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_JOIN_TOKEN;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_MANUFACTURER;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_MODEL;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_NEW_TOKEN;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_OS;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_TOKEN;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_UPDATE;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_CONTROL;
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

    private URI uri;
    private static volatile MyWebSocketClient instance = null;
    private WebSocketClient webSocketClient;
    private State state;
    private JSONObject builder;
    private MyTracking tracking;
    private String token;
    private boolean wasConnected = false;

    MyWebSocketClient(URI uri) throws URISyntaxException {
        this.uri = uri;
        webSocketClient = new MWebSocketClient(uri);
        state = State.getInstance();
    }

    /*public static MyWebSocketClient getInstance(String host) {
        if (instance == null) {
            synchronized (MyWebSocketClient.class) {
                if (instance == null) {
                    try {
                        uri = new URI("wss://"+host+":8081");
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }

                    instance = new MyWebSocketClient(uri);
                }
            }
        }
        return instance;
    }*/

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
        PreferenceManager.getDefaultSharedPreferences(state.getApplication()).edit().remove(RESPONSE_TOKEN).apply();
        setToken(null);
    }

    private class MWebSocketClient extends WebSocketClient {
        public MWebSocketClient(URI serverURI) {
            super(serverURI);
        }

        @Override
        public void onOpen(ServerHandshake serverHandshake) {
//            System.out.println("WEBSOCKET:OPENED:" + post);
            /*if (post != null) {
                webSocketClient.send(post.toString());
                post = null;
            }*/
        }

        @Override
        public void onMessage(String s) {
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
                    case RESPONSE_STATUS_DISCONNECTED:
                    case RESPONSE_STATUS_UPDATED:
                        tracking.fromServer(o);
                        break;
                    case RESPONSE_STATUS_ERROR:
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
//            System.out.println("WEBSOCKET:CLOSED:" + s);
            JSONObject o = new JSONObject();
            try {
                o.put(RESPONSE_STATUS, RESPONSE_STATUS_DISCONNECTED);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            tracking.fromServer(o);
//            tracking.stop();
        }

        @Override
        public void onError(Exception e) {
//            System.out.println("WEBSOCKET:ERROR:" + e.getMessage());
        }

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

    private void tokenAccepted(JSONObject o) {
//        System.out.println("RESPONSE_STATUS_ACCEPTED:" + o);
        wasConnected = true;
        try {
            if (o.has(RESPONSE_TOKEN)) {
                setToken(o.getString(RESPONSE_TOKEN));

                PreferenceManager.getDefaultSharedPreferences(state.getApplication()).edit()
                        .putString(RESPONSE_TOKEN, getToken()).apply();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        tracking.fromServer(o);
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
            switch (webSocketClient.getReadyState()) {
                case OPEN:
                    o.put("timestamp", new Date().getTime());
                    webSocketClient.send(o.toString());
                    break;
                case CLOSED:
                    o.put("timestamp", new Date().getTime());
                    webSocketClient = new MWebSocketClient(uri);
                    webSocketClient.connect();
                    break;
                case NOT_YET_CONNECTED:
                    o.put("timestamp", new Date().getTime());
                    webSocketClient.connect();
                    break;
                case CONNECTING:
                    break;
                case CLOSING:
                    break;

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        webSocketClient.close();
    }

    public void start() {
        webSocketClient = new MWebSocketClient(uri);
        webSocketClient.connect();
    }

}
