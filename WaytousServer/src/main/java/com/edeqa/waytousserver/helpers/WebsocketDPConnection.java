package com.edeqa.waytousserver.helpers;

import com.edeqa.waytousserver.interfaces.DataProcessorConnection;

import org.java_websocket.WebSocket;

import java.net.InetSocketAddress;

/**
 * Created 5/24/2017.
 */
public class WebsocketDPConnection implements DataProcessorConnection {

    private final WebSocket conn;

    public WebsocketDPConnection(WebSocket conn) {
        this.conn = conn;
    }

    @Override
    public boolean isOpen() {
        return conn.isOpen();
    }

    @Override
    public InetSocketAddress getRemoteSocketAddress() {
        return conn.getRemoteSocketAddress();
    }

    @Override
    public void send(String string) {
        conn.send(string);
    }

    @Override
    public void close() {
        conn.close();
    }
}
