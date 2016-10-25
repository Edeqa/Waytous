package ru.wtg.whereaminowserver.helpers;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static ru.wtg.whereaminowserver.helpers.Constants.HTTP_SERVER_URL;
import static ru.wtg.whereaminowserver.helpers.Constants.LIFETIME_INACTIVE_TOKEN;
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
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_NUMBER;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_CHECK;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_CONNECTED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_ERROR;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_ACCEPTED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_UPDATED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_TOKEN;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_COLOR;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_DISMISSED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_JOINED;

/**
 * Created by tujger on 10/5/16.
 */

public class MyWssServer extends WebSocketServer {

    public HashMap<String, MyToken> tokens;
    public HashMap<String, MyToken> ipToToken;
    public HashMap<String, MyUser> ipToUser;
    public HashMap<String, CheckReq> ipToCheck;

    public MyWssServer(int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
        tokens = new HashMap<>();
        ipToToken = new HashMap<>();
        ipToUser = new HashMap<>();
        ipToCheck = new HashMap<>();

        String a = HTTP_SERVER_URL;

    }

    public MyWssServer(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
//        this.sendToAll( "new connection: " + handshake.getResourceDescriptor() );
        System.out.println("WSS:ONOPEN:" + conn.getRemoteSocketAddress() + " connected");

        conn.send("{\""+ RESPONSE_STATUS +"\":\""+ RESPONSE_STATUS_CONNECTED +"\"}");


    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
//        this.sendToAll( conn + " has left the room!" );
        String ip = conn.getRemoteSocketAddress().toString();
        if(ipToToken.containsKey(ip)){
            final MyToken token = ipToToken.get(ip);
            ipToToken.remove(ip);

            try {
                MyUser user = ipToUser.get(ip);
                JSONObject o = new JSONObject();
                o.put(RESPONSE_STATUS, RESPONSE_STATUS_UPDATED);
                o.put(USER_DISMISSED, user.getNumber());
                token.sendToAllFrom(o, user);
            } catch (Exception e){
                e.printStackTrace();
            }

            final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
            executor.schedule(new Runnable() {
                @Override
                public void run() {
                    if(token.isEmpty() && new Date().getTime() - token.getChanged() >= LIFETIME_INACTIVE_TOKEN) {
                        tokens.remove(token.getId());
                    }
                }
            }, LIFETIME_INACTIVE_TOKEN+10, TimeUnit.SECONDS);

        }
        if(ipToUser.containsKey(ip)) ipToUser.remove(ip);
        if(ipToCheck.containsKey(ip)) ipToCheck.remove(ip);

        System.out.println("WSS:ONCLOSE:" + conn.getRemoteSocketAddress() + " disconnected");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("WSS:FROM:" + conn.getRemoteSocketAddress() + ": " + message);

        String ip = conn.getRemoteSocketAddress().toString();
        JSONObject request, responce = new JSONObject();

        request = new JSONObject(message);
        if(!request.has(REQUEST)) return;

        switch(request.getString(REQUEST)) {
            case REQUEST_NEW_TOKEN:
                String deviceId = "";
                if (request.has(REQUEST_DEVICE_ID)) {
                    MyToken token = new MyToken();
                    MyUser user = new MyUser(conn, request.getString(REQUEST_DEVICE_ID));
                    user.setManufacturer(request.getString(REQUEST_MANUFACTURER));
                    user.setModel(request.getString(REQUEST_MODEL));
                    user.setOs(request.getString(REQUEST_OS));

                    token.addUser(user);
                    tokens.put(token.getId(), token);

//                    if(request.has(REQUEST_MODEL)){
//                        user.setModel(request.getString(REQUEST_MODEL));
//                    }
//                    if(request.has(REQUEST_MANUFACTURER)){
//                        user.setManufacturer(request.getString(REQUEST_MANUFACTURER));
//                    }
//                    if(request.has(REQUEST_OS)){
//                        user.setOs(request.getString(REQUEST_OS));
//                    }
                    responce.put(RESPONSE_STATUS, RESPONSE_STATUS_ACCEPTED);
                    responce.put(RESPONSE_TOKEN, token.getId());
                    responce.put(RESPONSE_NUMBER, user.getNumber());

                    ipToToken.put(ip, token);
                    ipToUser.put(ip, user);
                    System.out.println("WSS:TOKEN:" + token);

                } else {
                    responce.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                    responce.put(RESPONSE_MESSAGE, "Your device id is not defined");
                }

                System.out.println("WSS:TO:" + responce);
                conn.send(responce.toString());
                break;
            case REQUEST_JOIN_TOKEN:
                if (request.has(REQUEST_TOKEN)) {
                    String tokenId = request.getString(REQUEST_TOKEN);
                    if (tokens.containsKey(tokenId)) {
                        MyToken token = tokens.get(tokenId);

                        if(request.has(REQUEST_DEVICE_ID)){
                            String deviceId1 = request.getString(REQUEST_DEVICE_ID);
                            MyUser user = null;
                            for(Map.Entry<String,MyUser> x:token.users.entrySet()){
                                if(deviceId1.equals(x.getValue().getDeviceId())){
                                    user = x.getValue();
                                    break;
                                }
                            }
                            if(user != null) {
                                CheckReq check = new CheckReq();
                                check.control = Utils.getUnique();
                                check.token = token;

                                responce.put(RESPONSE_STATUS, RESPONSE_STATUS_CHECK);
                                responce.put(RESPONSE_CONTROL,check.control);
                                ipToCheck.put(ip,check);
                            } else {

                                user = new MyUser(conn, request.getString(REQUEST_DEVICE_ID));
                                user.setManufacturer(request.getString(REQUEST_MANUFACTURER));
                                user.setModel(request.getString(REQUEST_MODEL));
                                user.setOs(request.getString(REQUEST_OS));
                                token.addUser(user);

                                responce.put(RESPONSE_STATUS, RESPONSE_STATUS_ACCEPTED);
                                responce.put(RESPONSE_NUMBER, user.getNumber());

                                ipToToken.put(ip, token);
                                ipToUser.put(ip, user);

                                token.sendInitialTo(responce,user);

                                JSONObject o = new JSONObject();
                                o.put(RESPONSE_STATUS,RESPONSE_STATUS_UPDATED);
                                o.put(USER_COLOR,user.getColor());
                                o.put(USER_JOINED,user.getNumber());

                                token.sendToAllFrom(o,user);
                            }

                        } else {
                            CheckReq check = new CheckReq();
                            check.control = Utils.getUnique();
                            check.token = token;

                            responce.put(RESPONSE_STATUS, RESPONSE_STATUS_CHECK);
                            responce.put(RESPONSE_CONTROL,check.control);
                            ipToCheck.put(ip,check);
                        }
                    } else {
                        responce.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                        responce.put(RESPONSE_MESSAGE, "This tracking is expired.");
                    }
                } else {
                    responce.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                    responce.put(RESPONSE_MESSAGE, "Wrong request (token not defined).");
                }
                System.out.println("WSS:TO:" + responce);
                conn.send(responce.toString());
                break;
            case REQUEST_CHECK_USER:
                if(request.has(REQUEST_HASH)) {
                    String hash = request.getString((REQUEST_HASH));
                    System.out.println(("HASH:"+hash+":"+ipToCheck));
                    if(ipToCheck.containsKey(ip)){
                        CheckReq check = ipToCheck.get(ip);

                        MyUser user = null;
                        for(Map.Entry<String,MyUser> x:check.token.users.entrySet()){
                            System.out.println("CHECK:"+check.control+":"+x.getValue().getDeviceId()+":"+x.getValue().calculateHash(check.control));
                            if(hash.equals(x.getValue().calculateHash(check.control))){
                                user = x.getValue();
                                break;
                            }
                        }
                        if(user != null) {
                            System.out.println("USER ACCEPTED:"+user);
                            responce.put(RESPONSE_STATUS, RESPONSE_STATUS_ACCEPTED);
                            responce.put(RESPONSE_NUMBER, user.getNumber());
                            user.setConnection(conn);
                            user.setChanged();

                            ipToToken.put(ip,check.token);
                            ipToUser.put(ip,user);

                            check.token.sendInitialTo(responce,user);

                            JSONObject o = new JSONObject();//user.getPosition().toJSON();
                            o.put(RESPONSE_STATUS,RESPONSE_STATUS_UPDATED);
                            o.put(USER_COLOR,user.getColor());
                            o.put(USER_JOINED,user.getNumber());
                            check.token.sendToAllFrom(o,user);

//                            responce.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
//                            responce.put(RESPONSE_MESSAGE, "User not granted.");
                        } else {
                            System.out.println("USER NOT ACCEPTED:"+hash);
                            if(ipToToken.containsKey(ip)) ipToToken.remove(ip);
                            if(ipToUser.containsKey(ip)) ipToUser.remove(ip);

                            responce.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                            responce.put(RESPONSE_MESSAGE, "Cannot join to tracking (user not accepted).");
                        }

                        ipToCheck.remove(ip);
                    } else {
                        responce.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                        responce.put(RESPONSE_MESSAGE, "Cannot join to tracking (user not authorized).");
                    }
                } else {
                    responce.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                    responce.put(RESPONSE_MESSAGE, "Cannot join to tracking (hash not defined).");
                }

                System.out.println("WSS:TO:" + responce);
                conn.send(responce.toString());
            case REQUEST_UPDATE:
                if(ipToToken.containsKey(ip)){
                    MyToken token = ipToToken.get(ip);
                    MyUser user = ipToUser.get(ip);

                    user.addPosition(request);
                    token.setChanged();

                    JSONObject o = user.getPosition().toJSON();
                    o.put(RESPONSE_STATUS,RESPONSE_STATUS_UPDATED);
                    token.sendToAllFrom(o,user);

                    System.out.println("REQUEST_UPDATE:TOKEN AND USER FOUND:"+token.getId()+":"+user);
                } else {
                    System.out.println("REQUEST_UPDATE:TOKEN NOT FOUND");
                    responce.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                    responce.put(RESPONSE_MESSAGE, "Tracking not exists.");
                    conn.send(responce.toString());
                    conn.close();
                }

                break;
//        this.sendToAll(message, conn);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        if (conn != null) {
            System.out.println("WSS:ONERROR:" + conn.getRemoteSocketAddress() + ": " + ex.getMessage());

            // some errors like port binding failed may not be assignable to a specific websocket
        }
    }

    /**
     * Sends <var>text</var> to all currently connected WebSocket clients.
     *
     * @param text The String to send across the network.
     * @throws InterruptedException When socket related I/O errors occur.
     */
    public void sendToAll(String text, WebSocket insteadConnection) {
        Collection<WebSocket> con = connections();
        synchronized (con) {
            for (WebSocket c : con) {
                if (insteadConnection != null && c == insteadConnection) continue;
                System.out.println("WSS:TO:" + c.getRemoteSocketAddress() + ":" + text);
                c.send(text);
            }
        }
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

    public void removeUser(String tokenId,String id){
        if(tokenId != null && id != null && tokens.containsKey(tokenId)){
            MyToken t = tokens.get(tokenId);
            MyUser user = t.users.get(id);
            if(user != null){
                JSONObject response = new JSONObject();
                response.put(RESPONSE_STATUS, USER_DISMISSED);
                response.put(RESPONSE_MESSAGE,"You have been dismissed.");
                user.send(response);
                user.disconnect();
                t.removeUser(id);
            }
        }
    }

    public class CheckReq {

        MyToken token;
        String control;
        public long timestamp;

        public CheckReq() {
            timestamp = new Date().getTime();
        }

    }

}
