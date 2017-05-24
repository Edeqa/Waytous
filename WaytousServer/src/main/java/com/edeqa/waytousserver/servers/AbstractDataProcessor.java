package com.edeqa.waytousserver.servers;

import com.edeqa.waytousserver.helpers.CheckReq;
import com.edeqa.waytousserver.helpers.MyGroup;
import com.edeqa.waytousserver.helpers.MyUser;
import com.edeqa.waytousserver.interfaces.Callable1;
import com.edeqa.waytousserver.interfaces.DataProcessorConnection;
import com.edeqa.waytousserver.interfaces.FlagHolder;
import com.edeqa.waytousserver.interfaces.RequestHolder;

import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created 10/5/16.
 */

abstract public class AbstractDataProcessor {

    final ConcurrentHashMap<String, MyGroup> groups;
    final ConcurrentHashMap<String, MyGroup> ipToToken;
    final ConcurrentHashMap<String, MyUser> ipToUser;
    final ConcurrentHashMap<String, CheckReq> ipToCheck;
    final HashMap<String,RequestHolder> requestHolders;
    protected final HashMap<String,FlagHolder> flagHolders;

    abstract public void validateGroups();

    abstract public void validateUsers();

    public AbstractDataProcessor() {
        groups = new ConcurrentHashMap<>();
        ipToToken = new ConcurrentHashMap<>();
        ipToUser = new ConcurrentHashMap<>();
        ipToCheck = new ConcurrentHashMap<>();

        requestHolders = new LinkedHashMap<>();

        LinkedList<String> classes = getRequestHoldersList();

        if(classes != null) {
            for (String s : classes) {
                try {
                    Class<RequestHolder> _tempClass = (Class<RequestHolder>) Class.forName("com.edeqa.waytousserver.holders.request." + s);
                    Constructor<RequestHolder> ctor = _tempClass.getDeclaredConstructor(AbstractDataProcessor.class);
                    registerRequestHolder(ctor.newInstance(this));
                } catch (Exception e) {
                    System.err.println("Trying to instantiate "+s);
                    e.printStackTrace();
                }
            }
        }

        flagHolders = new LinkedHashMap<>();
        classes = getFlagsHoldersList();
        if(classes != null) {
            for (String s : classes) {
                try {
                    Class<FlagHolder> _tempClass = (Class<FlagHolder>) Class.forName("com.edeqa.waytousserver.holders.flag." + s);
                    Constructor<FlagHolder> ctor = _tempClass.getDeclaredConstructor(AbstractDataProcessor.class);
                    registerFlagHolder(ctor.newInstance(this));
                } catch (Exception e) {
                    System.out.println("Trying to instantiate "+s);
                    e.printStackTrace();
                }
            }
        }
    }

    abstract public LinkedList<String> getRequestHoldersList();

    abstract public LinkedList<String> getFlagsHoldersList();

    public void registerRequestHolder(RequestHolder holder) {
        if(holder.getType() == null) return;
        requestHolders.put(holder.getType(), holder);
    }

    public void registerFlagHolder(FlagHolder holder) {
        if(holder.getType() == null) return;
        flagHolders.put(holder.getType(), holder);
    }

    final public void onOpen(DataProcessorConnection conn, ClientHandshake handshake) {
        try {
//            conn.send("{\"" + RESPONSE_STATUS + "\":\""+RESPONSE_STATUS_CONNECTED+"\",\"version\":" + SERVER_BUILD + "}");
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void onClose(DataProcessorConnection conn, int code, String reason, boolean remote) {
//        System.out.println("WSS:on close:" + conn.getRemoteSocketAddress() + " disconnected:by client:"+remote+":"+code+":"+reason);
//        this.sendToAll( conn + " has left the room!" );
        String ip = conn.getRemoteSocketAddress().toString();
        if(ipToCheck.containsKey(ip)) ipToCheck.remove(ip);

    }

    abstract public void onMessage(final DataProcessorConnection conn, String message);

    final public void onError(DataProcessorConnection conn, Exception ex) {
        ex.printStackTrace();
        if (conn != null && conn.getRemoteSocketAddress() != null) {
            String ip = conn.getRemoteSocketAddress().toString();
            if(ipToToken.containsKey(ip)) ipToToken.remove(ip);
            if(ipToUser.containsKey(ip)) ipToUser.remove(ip);
            if(ipToCheck.containsKey(ip)) ipToCheck.remove(ip);
            // some errors like port binding failed may not be assignable to a specific websocket
        }
    }

    public void onWebSocketPing(DataProcessorConnection conn, Framedata f) {
        try {
            String ip = conn.getRemoteSocketAddress().toString();
            if (ipToUser.containsKey(ip)) {
                ipToUser.get(ip).setChanged();
            }
//            System.out.println("PING:" + conn.getRemoteSocketAddress() + ":" + f);
        } catch ( Exception e) {
            e.printStackTrace();
        }
    }

    abstract public void createGroup(MyGroup group, Callable1<JSONObject> onsuccess, Callable1<JSONObject> onerror);

    abstract public void deleteGroup(String groupId, Callable1<JSONObject> onsuccess, Callable1<JSONObject> onerror);

    abstract public void switchPropertyInGroup(String groupId, String property, Callable1<JSONObject> onsuccess, Callable1<JSONObject> onerror);

    abstract public void modifyPropertyInGroup(String groupId, String property, Serializable value, Callable1<JSONObject> onsuccess, Callable1<JSONObject> onerror);

    abstract public void removeUser(String groupId, Long userNumber, Callable1<JSONObject> onsuccess, Callable1<JSONObject> onerror);

    abstract public void switchPropertyForUser(String groupId, Long userNumber, String property, Boolean value, Callable1<JSONObject> onsuccess, Callable1<JSONObject> onerror);

    public ConcurrentHashMap<String, MyGroup> getGroups(){
        return groups;
    }

    public ConcurrentHashMap<String, MyGroup> getIpToToken(){
        return ipToToken;
    }

    public ConcurrentHashMap<String, MyUser> getIpToUser(){
        return ipToUser;
    }

    public ConcurrentHashMap<String, CheckReq> getIpToCheck(){
        return ipToCheck;
    }

}
