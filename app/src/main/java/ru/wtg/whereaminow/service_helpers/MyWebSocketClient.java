package ru.wtg.whereaminow.service_helpers;

import android.content.Context;
import android.os.Build;
import android.preference.PreferenceManager;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import ru.wtg.whereaminow.helpers.State;
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

/**
 * Created by tujger on 10/2/16.
 */
public class MyWebSocketClient {

    private static URI uri;
    private static volatile MyWebSocketClient instance = null;
    private WebSocketClient webSocketClient;
    private State state;
    private JSONObject post;
    private JSONObject builder;
    private MyTracking tracking;
    private String token;
    private boolean wasConnected = false;

    private MyWebSocketClient(URI serverURI) {
        webSocketClient = new MWebSocketClient(serverURI);
        state = State.getInstance();
    }

    public static MyWebSocketClient getInstance(String serverURI) {
        if (instance == null) {
            synchronized (MyWebSocketClient.class) {
                if (instance == null) {
                    try {
                        uri = new URI(serverURI);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }

                    instance = new MyWebSocketClient(uri);
                }
            }
        }
        return instance;
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
            final String message = s;
//            System.out.println("WEBSOCKET:MESSAGE:" + s);
            JSONObject o;
            try {
                o = new JSONObject(s);
                if (!o.has(RESPONSE_STATUS)) return;
                switch (o.getString(RESPONSE_STATUS)) {
                    case RESPONSE_STATUS_CONNECTED:
//                        System.out.println("RESPONSE_STATUS_CONNECTED");
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
//                        System.out.println("RESPONSE_STATUS_UPDATED");
                        tracking.fromServer(o);
                        break;
                    case RESPONSE_STATUS_ERROR:
//                        System.out.println("RESPONSE_STATUS_ERROR");
                        wasConnected = false;
                        setToken(null);
                        tracking.fromServer(o);
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                System.out.println("Message got: " + s);
            }

    /*
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView textView = (TextView)findViewById(R.id.messages);
                            textView.setText(textView.getText() + "\n" + message);
                        }
                    });
    */
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
//        System.out.println("WEBSOCKET:REQUEST_JOIN_TOKEN:" + getToken());
        put(REQUEST, REQUEST_JOIN_TOKEN);
        put(REQUEST_TOKEN, getToken());

        if(!wasConnected) {
            put(REQUEST_DEVICE_ID, State.getInstance().getDeviceId());
            put(REQUEST_MODEL, Build.MODEL);
            put(REQUEST_MANUFACTURER, Build.MANUFACTURER);
            put(REQUEST_OS, "android");
        }
//        setHash(Utils.getEncryptedHash(getControl() + ":" + getToken()));

        send();
    }

    private void requestNewToken() {
//        System.out.println("WEBSOCKET:REQUEST_NEW_TOKEN");
        put(REQUEST, REQUEST_NEW_TOKEN);

        put(REQUEST_DEVICE_ID, State.getInstance().getDeviceId());
        put(REQUEST_MODEL, Build.MODEL);
        put(REQUEST_MANUFACTURER, Build.MANUFACTURER);
        put(REQUEST_OS, "android");

        send();
    }

    private void checkUser(JSONObject o) {
        try {
            if (o.has(RESPONSE_CONTROL)) {
                String control = o.getString(RESPONSE_CONTROL);
                String deviceId = State.getInstance().getDeviceId();
                String hash = Utils.getEncryptedHash(control + ":" + deviceId);
//                System.out.println("CHECK:"+control+":"+deviceId+":"+hash);
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
//            builder.put(REQUEST_TOKEN, getToken());
//            builder.put(REQUEST_HASH, getHash());
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
//            System.out.println("webSocketClient.getReadyState:" + webSocketClient.getReadyState());
            switch (webSocketClient.getReadyState()) {
                case OPEN:
                    o.put("timestamp", new Date().getTime());
                    webSocketClient.send(o.toString());
                    break;
                case CLOSED:
//                    System.out.println("RECONNECT");
                    o.put("timestamp", new Date().getTime());
//                o.put("reconnect", "NewToken");
                    post = o;
                    webSocketClient = new MWebSocketClient(uri);
                    webSocketClient.connect();
                    break;
                case NOT_YET_CONNECTED:
                    o.put("timestamp", new Date().getTime());
                    post = o;
                    webSocketClient.connect();
//                webSocketClient.send(o.toString());
                    break;
                case CONNECTING:
//                webSocketClient.send(o.toString());
                    break;
                case CLOSING:
//                webSocketClient.send(o.toString());
                    break;

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
//        if(webSocketClient.getReadyState())
        webSocketClient.close();
    }

    public void start() {
        webSocketClient = new MWebSocketClient(uri);
        webSocketClient.connect();
    }

}
