package ru.wtg.whereaminow.helpers;

import android.content.Context;
import android.os.Build;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by tujger on 10/2/16.
 */
public class MyWebSocketClient extends WebSocketClient {

    public MyWebSocketClient(URI serverURI) {
        super(serverURI);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("WEBSOCKET:OPENED:" + Build.MANUFACTURER + " " + Build.MODEL);
    }

    @Override
    public void onMessage(String s) {
        final String message = s;
        System.out.println("WEBSOCKET:MESSAGE:" + s);

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
        System.out.println("WEBSOCKET:CLOSED:" + s);
    }

    @Override
    public void onError(Exception e) {
        System.out.println("WEBSOCKET:ERROR:" + e.getMessage());
    }
}
