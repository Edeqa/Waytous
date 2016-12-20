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

import static ru.wtg.whereaminowserver.helpers.Constants.HTTP_SERVER_HOST;
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
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_PRIVATE;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_ACCEPTED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_CHECK;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_CONNECTED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_ERROR;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_UPDATED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_TOKEN;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_WELCOME_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_COLOR;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_DISMISSED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_JOINED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_NAME;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_PROVIDER;

/**
 * Created 10/5/16.
 */

public class MyWssServer extends WebSocketServer {

    public HashMap<String, MyToken> tokens;
    public HashMap<String, MyToken> ipToToken;
    public HashMap<String, MyUser> ipToUser;
    public HashMap<String, CheckReq> ipToCheck;

    public MyWssServer(int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
        tokens = new HashMap<String, MyToken>();
        ipToToken = new HashMap<String, MyToken>();
        ipToUser = new HashMap<String, MyUser>();
        ipToCheck = new HashMap<String, CheckReq>();

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
        JSONObject request, response = new JSONObject();

        request = new JSONObject(message);
        if(!request.has(REQUEST)) return;

        String req = request.getString(REQUEST);
        if(REQUEST_NEW_TOKEN.equals(req)) {
            if (request.has(REQUEST_DEVICE_ID)) {
                MyToken token = new MyToken();
                MyUser user = new MyUser(conn, request.getString(REQUEST_DEVICE_ID));
                user.setManufacturer(request.getString(REQUEST_MANUFACTURER));
                user.setModel(request.getString(REQUEST_MODEL));
                user.setOs(request.getString(REQUEST_OS));
                if(request.has(USER_NAME)) user.setName(request.getString(USER_NAME));

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
                response.put(RESPONSE_STATUS, RESPONSE_STATUS_ACCEPTED);
                response.put(RESPONSE_TOKEN, token.getId());
                response.put(RESPONSE_NUMBER, user.getNumber());

                ipToToken.put(ip, token);
                ipToUser.put(ip, user);
                System.out.println("WSS:TOKEN:" + token);

            } else {
                response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                response.put(RESPONSE_MESSAGE, "Your device id is not defined");
            }

            System.out.println("WSS:NEW_TOKEN:TO:" + response);

            Utils.pause(2);//FIXME remove pause

            conn.send(response.toString());
        } else if(REQUEST_JOIN_TOKEN.equals(req)){
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
                            if(request.has(USER_NAME)) check.name = request.getString(USER_NAME);

                            response.put(RESPONSE_STATUS, RESPONSE_STATUS_CHECK);
                            response.put(RESPONSE_CONTROL,check.control);
                            ipToCheck.put(ip,check);
                        } else {

                            user = new MyUser(conn, request.getString(REQUEST_DEVICE_ID));
                            user.setManufacturer(request.getString(REQUEST_MANUFACTURER));
                            user.setModel(request.getString(REQUEST_MODEL));
                            user.setOs(request.getString(REQUEST_OS));
                            if(request.has(USER_NAME) && request.getString(USER_NAME) != null && request.getString(USER_NAME).length()>0){
                                user.setName(request.getString(USER_NAME));
                            }
                            token.addUser(user);

                            response.put(RESPONSE_STATUS, RESPONSE_STATUS_ACCEPTED);
                            response.put(RESPONSE_NUMBER, user.getNumber());

                            ipToToken.put(ip, token);
                            ipToUser.put(ip, user);

                            token.sendInitialTo(response,user);

                            JSONObject o = new JSONObject();
                            o.put(RESPONSE_STATUS,RESPONSE_STATUS_UPDATED);
                            o.put(USER_COLOR,user.getColor());
                            o.put(USER_JOINED,user.getNumber());
                            if(user.getName() != null && user.getName().length()>0){
                                o.put(USER_NAME,user.getName());
                            }
                            token.sendToAllFrom(o,user);
                            return;
                        }

                    } else {
                        CheckReq check = new CheckReq();
                        check.control = Utils.getUnique();
                        check.token = token;

                        response.put(RESPONSE_STATUS, RESPONSE_STATUS_CHECK);
                        response.put(RESPONSE_CONTROL,check.control);
                        ipToCheck.put(ip,check);
                    }
                } else {
                    response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                    response.put(RESPONSE_MESSAGE, "This tracking is expired.");
                }
            } else {
                response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                response.put(RESPONSE_MESSAGE, "Wrong request (token not defined).");
            }
            conn.send(response.toString());
            System.out.println("WSS:JOIN_TOKEN:TO:" + response);
        } else if(REQUEST_CHECK_USER.equals(req)){
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
                        response.put(RESPONSE_STATUS, RESPONSE_STATUS_ACCEPTED);
                        response.put(RESPONSE_NUMBER, user.getNumber());
                        user.setConnection(conn);
                        user.setChanged();
                        if(check.name != null && check.name.length()>0){
                            user.setName(check.name);
                        }

                        ipToToken.put(ip,check.token);
                        ipToUser.put(ip,user);

                        check.token.sendInitialTo(response,user);

                        JSONObject o = new JSONObject();//user.getPosition().toJSON();
                        o.put(RESPONSE_STATUS,RESPONSE_STATUS_UPDATED);
                        o.put(USER_COLOR,user.getColor());
                        o.put(USER_JOINED,user.getNumber());
                        if(check.name != null && check.name.length()>0){
                            o.put(USER_NAME,check.name);
                        }
                        check.token.sendToAllFrom(o,user);
                        return;
//                            response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
//                            response.put(RESPONSE_MESSAGE, "User not granted.");
                    } else {
                        System.out.println("USER NOT ACCEPTED:"+hash);
                        if(ipToToken.containsKey(ip)) ipToToken.remove(ip);
                        if(ipToUser.containsKey(ip)) ipToUser.remove(ip);

                        response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                        response.put(RESPONSE_MESSAGE, "Cannot join to tracking (user not accepted).");
                    }

                    ipToCheck.remove(ip);
                } else {
                    response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                    response.put(RESPONSE_MESSAGE, "Cannot join to tracking (user not authorized).");
                }
            } else {
                response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                response.put(RESPONSE_MESSAGE, "Cannot join to tracking (hash not defined).");
            }

            System.out.println("WSS:CHECK_USER:TO:" + response);
            Utils.pause(2);//FIXME remove pause
            conn.send(response.toString());
        } else if(REQUEST_UPDATE.equals(req)) {
            if (ipToToken.containsKey(ip)) {
                MyToken token = ipToToken.get(ip);
                MyUser user = ipToUser.get(ip);

                JSONObject o = new JSONObject();
                if(request.has(USER_NAME)){
                    o.put(USER_NAME,request.getString(USER_NAME));
                    user.setName(request.getString(USER_NAME));
                    token.setChanged();
                }
                if(request.has(USER_PROVIDER)){
                    user.addPosition(request);
                    token.setChanged();
                    o = user.getPosition().toJSON();
                }
                if(request.has(USER_MESSAGE)){
                    o.put(USER_MESSAGE,request.getString(USER_MESSAGE));
                }
                o.put(RESPONSE_STATUS, RESPONSE_STATUS_UPDATED);
                if(request.has(RESPONSE_PRIVATE)) {
                    o.put(RESPONSE_PRIVATE, request.getInt(RESPONSE_PRIVATE));
                    token.sendToFrom(o, request.getInt(RESPONSE_PRIVATE), user);
                } else if(request.has(RESPONSE_WELCOME_MESSAGE)) {
                    String text = request.getString(RESPONSE_WELCOME_MESSAGE);
                    if(user.getNumber() == 0 && text != null) {
                        token.setWelcomeMessage(text);
                    }
                } else {
                    token.sendToAllFrom(o, user);
                }

                System.out.println("REQUEST_UPDATE:TOKEN AND USER FOUND:" + token.getId() + ":" + user);
            } else {
                System.out.println("REQUEST_UPDATE:TOKEN NOT FOUND");
                response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                response.put(RESPONSE_MESSAGE, "Tracking not exists.");
                conn.send(response.toString());
                conn.close();
            }
//        this.sendToAll(message, conn);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        if (conn != null) {
            System.out.println("WSS:ONERROR:" + conn.getRemoteSocketAddress() + ": " + ex.getMessage());

            String ip = conn.getRemoteSocketAddress().toString();
            if(ipToToken.containsKey(ip)) ipToToken.remove(ip);
            if(ipToUser.containsKey(ip)) ipToUser.remove(ip);
            if(ipToCheck.containsKey(ip)) ipToCheck.remove(ip);
            // some errors like port binding failed may not be assignable to a specific websocket
        }
    }

    /**
     * Sends <var>text</var> to all currently connected WebSocket clients.
     *
     * @param text The String to send across the network.
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
        String name;
        public long timestamp;

        public CheckReq() {
            timestamp = new Date().getTime();
        }

    }

}
