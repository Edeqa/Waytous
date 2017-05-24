package com.edeqa.waytousserver.helpers;

import com.edeqa.waytousserver.interfaces.DataProcessorConnection;
import com.sun.net.httpserver.HttpExchange;

import java.net.InetSocketAddress;

/**
 * Created 5/24/2017.
 */
public class HttpDPConnection implements DataProcessorConnection {

    private final HttpExchange exchange;

    public HttpDPConnection(HttpExchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public boolean isOpen() {
        return true;//conn.isOpen();
    }

    @Override
    public InetSocketAddress getRemoteSocketAddress() {
        return exchange.getRemoteAddress();
    }

    @Override
    public void send(String string) {
        Utils.sendResult.call(exchange, 200, Constants.MIME.APPLICATION_JSON, string.getBytes());
    }

    @Override
    public void close() {
//            conn.close();
    }
}
