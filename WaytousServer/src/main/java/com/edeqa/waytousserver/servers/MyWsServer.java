package com.edeqa.waytousserver.servers;

import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.interfaces.WssServer;

import org.java_websocket.WebSocket;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.edeqa.waytousserver.helpers.Constants.LIFETIME_INACTIVE_GROUP;


/**
 * Created 10/5/16.
 */

public class MyWsServer extends WebSocketServer implements WssServer {

    private final AbstractDataProcessor processor;
    private static boolean validationStarted = false;

    public MyWsServer(int port, final AbstractDataProcessor processor) {
        super(new InetSocketAddress(port));
        this.processor = processor;

        if(!validationStarted) {
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    processor.validateGroups();
                }
            }, 0/*LIFETIME_INACTIVE_GROUP*/, LIFETIME_INACTIVE_GROUP, TimeUnit.SECONDS);
            validationStarted = true;
        }
    }

    /*   @Override
    public ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(WebSocket conn, Draft draft, ClientHandshake request) throws InvalidDataException {
        System.out.println("HANDSHAKE:"+conn+":"+draft+":"+request);

        return super.onWebsocketHandshakeReceivedAsServer(conn, draft, request);
    }
*/
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        Common.log("Ws","onOpen:"+conn.getRemoteSocketAddress(),handshake.getResourceDescriptor() );
        processor.onOpen(new WSConnection(conn), handshake);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Common.log("Ws","onClose:"+conn.getRemoteSocketAddress(),"code:"+code, "reason:"+reason);
        processor.onClose(new WSConnection(conn), code, reason, remote);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Common.log("Ws","onMessage:"+conn.getRemoteSocketAddress(), message.length() > 200 ? "("+message.length() + " byte(s))" : message );
        processor.onMessage(new WSConnection(conn), message);
    }

//    @Override
//    public void onWebsocketPong(WebSocket conn, Framedata f) {
//        super.onWebsocketPong(conn, f);
//        System.out.println("PONG:"+conn.getRemoteSocketAddress()+":"+f);
//    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        Common.log("Ws","onError:"+conn.getRemoteSocketAddress(),"exception:"+ex.getMessage());
        processor.onError(new WSConnection(conn), ex);
    }

    @Override
    public void onWebsocketPing(WebSocket conn, Framedata f) {
        super.onWebsocketPing(conn, f);
        processor.onWebSocketPing(new WSConnection(conn), f);
    }

    /**
     * Sends <var>text</var> to all currently connected WebSocket clients.
     *
     * @param text The String to send across the network.
     */
    public void sendToAll(String text, WebSocket insteadConnection) {
        Collection<WebSocket> con = connections();
//        synchronized (con) {
            for (WebSocket c : con) {
                if (insteadConnection != null && c == insteadConnection) continue;
                System.out.println("WSS:to:" + c.getRemoteSocketAddress() + ":" + text);
                c.send(text);
            }
//        }
    }

    public boolean parse(BufferedReader sysin) throws IOException, InterruptedException {
        String in = sysin.readLine();
        System.out.println("READ:" + in);
//                        s.sendToAll(in);
        if (in.equals("exit")) {
            stop();
            return false;
        } else if (in.equals("restart")) {
            stop();
            start();
            return false;
        }
        return true;
    }

    class WSConnection implements AbstractDataProcessor.Connection {

        private final WebSocket conn;

        public WSConnection(WebSocket conn) {
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


}
