package ru.wtg.whereaminowserver.servers;

import org.java_websocket.WebSocket;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ru.wtg.whereaminowserver.helpers.CheckReq;
import ru.wtg.whereaminowserver.helpers.Common;
import ru.wtg.whereaminowserver.helpers.MyToken;
import ru.wtg.whereaminowserver.helpers.MyUser;
import ru.wtg.whereaminowserver.helpers.Utils;
import ru.wtg.whereaminowserver.interfaces.FlagHolder;
import ru.wtg.whereaminowserver.interfaces.RequestHolder;
import ru.wtg.whereaminowserver.interfaces.WssServer;

import static ru.wtg.whereaminowserver.helpers.Constants.LIFETIME_INACTIVE_TOKEN;
import static ru.wtg.whereaminowserver.helpers.Constants.INACTIVE_USER_DISMISS_DELAY;
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
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_CONTROL;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_NUMBER;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_PRIVATE;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_ACCEPTED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_CHECK;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_ERROR;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_UPDATED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_TOKEN;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_COLOR;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_DISMISSED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_JOINED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_NAME;

/**
 * Created 10/5/16.
 */

public class MyWsServer extends WebSocketServer implements WssServer {

    private final AbstractWainProcessor processor;

    public MyWsServer(int port, final AbstractWainProcessor processor) throws UnknownHostException {
        super(new InetSocketAddress(port));
        this.processor = processor;

        new Thread(new Runnable() {
            @Override
            public void run() {
                processor.dismissInactiveUsers();
            }
        }).start();

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

    class WSConnection implements AbstractWainProcessor.Connection {

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
