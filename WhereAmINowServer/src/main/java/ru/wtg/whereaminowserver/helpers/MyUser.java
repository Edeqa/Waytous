package ru.wtg.whereaminowserver.helpers;

import org.java_websocket.WebSocket;
import org.json.JSONObject;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;

/**
 * Created by tujger on 10/9/16.
 */

public class MyUser {
    private WebSocket webSocket;
    private String deviceId;
    private String control;
    private String token;
    private String model;
    private String manufacturer;
    private String os;
    private String name;
    private long timestamp;


    public MyUser(WebSocket webSocket, String deviceId){
        this.webSocket = webSocket;
        this.deviceId = deviceId;
        timestamp = new Date().getTime();

        newControl();
        System.out.println("USER CONTROL:"+control);
        calculateHash();
    }


    public void setToken(String token) {
        this.token = token;
    }

    public String calculateHash(){
        return calculateHash(control);
    }

    public String calculateHash(String control){
        return Utils.getEncryptedHash(control + ":" + deviceId);
    }

    public String getControl(){
        return control;
    }

    public String newControl(){
        control = Utils.getUnique();
        return control;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getModel() {
        return model;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getAddress() {
        if(webSocket == null) return null;
        if(webSocket.getRemoteSocketAddress() == null) return null;
        return webSocket.getRemoteSocketAddress().toString();
    }

    public WebSocket getConnection() {
        return webSocket;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String toString(){
        String res = "";
        res+= "deviceId:"+deviceId;
        res+= ", address:"+webSocket.getRemoteSocketAddress();
        res+= ", timestamp:"+getTimestamp()+"/"+new Date(getTimestamp()).toString();
        res+= ", control:"+getControl();
        if(name != null) res+= ", name:"+name;
        if(model != null) res+= ", model:"+model;
        if(manufacturer != null) res+= ", manufacturer:"+manufacturer;
        if(os != null) res+= ", os:"+os;

        return res;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void send(JSONObject o){
        send(o.toString());
    }

    public void send(String text){
        webSocket.send(text);
    }

    public void disconnect(){
        webSocket.close();
    }

    public long getTimestamp() {
        return timestamp;
    }
}
